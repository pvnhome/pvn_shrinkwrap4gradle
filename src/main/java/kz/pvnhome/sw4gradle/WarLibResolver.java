package kz.pvnhome.sw4gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
      Set<File> compile = new HashSet<>();

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
                     if (!provided.contains(formatModuleKey(dep))) {
                        compile.add(dep.getFile());
                     }
                  }
               }
            }
         }

         for (File dependency : compile) {
            try {
               archives.add(ShrinkWrap.create(ZipImporter.class, dependency.getName()).importFrom(dependency).as(archive));
            } catch (Exception e) {
               logger.error("", e);
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

   private String formatModuleKey(IdeaSingleEntryLibraryDependency dep) {
      GradleModuleVersion ver = dep.getGradleModuleVersion();
      return ver.getGroup() + ":" + ver.getName();
   }
}