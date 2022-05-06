package kz.pvnhome.sw4gradle;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

/**
 * <p>WarLibResolver testing.</p>
 * <p><b>Created:</b> 24.01.2019 14:54:14</p>
 * @author victor
 */
public class WarLibResolverTestCase {
   @Test
   @SuppressWarnings("rawtypes")
   public void resolve() {
      List<? extends Archive> libs = WarLibResolver.resolve().asList(JavaArchive.class);

      libs.sort((a1, a2) -> a1.getName().compareTo(a2.getName()));

      assertEquals(4, libs.size());
      assertEquals("gradle-tooling-api-7.4.1.jar", libs.get(0).getName());
      assertEquals("shrinkwrap-api-1.2.6.jar", libs.get(1).getName());
      assertEquals("slf4j-api-1.7.30.jar", libs.get(2).getName());
      assertEquals("slf4j-simple-1.7.25.jar", libs.get(3).getName());

      for (Archive a : libs) {
         System.out.println(a.toString());
      }
   }
}
