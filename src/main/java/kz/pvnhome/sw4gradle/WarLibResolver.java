package kz.pvnhome.sw4gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.idea.IdeaDependency;
import org.gradle.tooling.model.idea.IdeaModule;
import org.gradle.tooling.model.idea.IdeaProject;
import org.gradle.tooling.model.idea.IdeaSingleEntryLibraryDependency;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Resolving non provided WAR dependencies using Gradle API.</p>
 * <p><b>Created:</b> 24.01.2019 14:42:19</p>
 * @author victor
 */
public class WarLibResolver {
   private static final String COMPILE = "COMPILE";
   private static final String PROVIDED = "PROVIDED";

   private Logger logger = LoggerFactory.getLogger(WarLibResolver.class);

   private WarLibResolver() {
   }

   /**
    * Create WarLibResolver.
    */
   public static WarLibResolver resolve() {
      return new WarLibResolver();
   }

   /**
    * Gets all dependencies (and the transitive ones too) as given ShrinkWrap Archive type.
    * @param archive ShrinkWrap archive.
    * @return List of dependencies and transitive ones for configured state.
    */
   @SuppressWarnings("rawtypes")
   public List<? extends Archive> asList(final Class<? extends Archive> archive) {
      final List<Archive> archives = new ArrayList<>();

      final GradleConnector connector = GradleConnector.newConnector();
      connector.forProjectDirectory(new File("."));
      ProjectConnection connection = null;

      Set<String> provided = new HashSet<>();
      //Set<File> compile = new HashSet<>();
      Map<String, IdeaSingleEntryLibraryDependency> compile = new HashMap<>();

      try {
         connection = connector.connect();
         final IdeaProject project = connection.getModel(IdeaProject.class);

         final DomainObjectSet<? extends IdeaModule> modules = project.getChildren();

         for (IdeaModule ideaModule : modules) {
            final DomainObjectSet<? extends IdeaDependency> dependencies = ideaModule.getDependencies();

            for (IdeaDependency ideaDependency : dependencies) {
               if (ideaDependency instanceof IdeaSingleEntryLibraryDependency) {
                  IdeaSingleEntryLibraryDependency dep = (IdeaSingleEntryLibraryDependency) ideaDependency;

                  if (PROVIDED.equals(dep.getScope().getScope())) {
                     provided.add(formatModuleKey(dep));
                  }
               }
            }

            for (IdeaDependency ideaDependency : dependencies) {
               if (ideaDependency instanceof IdeaSingleEntryLibraryDependency) {
                  IdeaSingleEntryLibraryDependency dep = (IdeaSingleEntryLibraryDependency) ideaDependency;

                  if (COMPILE.equals(dep.getScope().getScope())) {
                     String compileKey = formatModuleKey(dep);
                     if (!provided.contains(compileKey)) {
                        IdeaSingleEntryLibraryDependency cDep = compile.get(compileKey);
                        if (cDep == null) {
                           compile.put(compileKey, dep);
                        } else {
                           String ver = dep.getGradleModuleVersion().getVersion();
                           String cVer = cDep.getGradleModuleVersion().getVersion();
                           if (versionMoreThan(ver, cVer)) {
                              logger.debug("replace: %s %s -> %s", compileKey, cVer, ver);
                              compile.put(compileKey, dep);
                           }
                        }
                     }
                  }
               }
            }
         }

         for (IdeaSingleEntryLibraryDependency dep : compile.values()) {
            try {
               File depFile = dep.getFile();
               archives.add(ShrinkWrap.create(ZipImporter.class, depFile.getName()).importFrom(depFile).as(archive));
            } catch (Exception e) {
               logger.error("Error during archiving", e);
               e.printStackTrace();
            }
         }

      } finally {
         if (connection != null) {
            connection.close();
         }
      }

      return archives;
   }

   public static boolean versionMoreThan(String newVersion, String oldVersion) {
      List<Integer> oldVer = stringToVersion(oldVersion);
      List<Integer> newVer = stringToVersion(newVersion);
      if (!oldVer.isEmpty() && !newVer.isEmpty()) {
         int i = 0;
         while (i < oldVer.size() || i < newVer.size()) {
            int n = i < newVer.size() ? newVer.get(i).intValue() : 0;
            int o = i < oldVer.size() ? oldVer.get(i).intValue() : 0;
            if (n > o) {
               return true;
            } else if (n < o) {
               return false;
            }
            i++;
         }
      } else if (oldVer.isEmpty() && !newVer.isEmpty()) {
         return true;
      }
      return false;
   }

   private static List<Integer> stringToVersion(String version) {
      List<Integer> digits = new ArrayList<>();
      if (version != null) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < version.length(); i++) {
            char ch = version.charAt(i);
            if (Character.isDigit(ch)) {
               sb.append(ch);
            } else {
               if (sb.length() > 0) {
                  digits.add(new Integer(sb.toString()));
                  sb.setLength(0);
               }
               if (ch != '.') {
                  break;
               }
            }
         }
         if (sb.length() > 0) {
            digits.add(new Integer(sb.toString()));
         }
      }
      return digits;
   }

   private String formatModuleKey(IdeaSingleEntryLibraryDependency dep) {
      GradleModuleVersion ver = dep.getGradleModuleVersion();
      return ver.getGroup() + ":" + ver.getName();
   }
}