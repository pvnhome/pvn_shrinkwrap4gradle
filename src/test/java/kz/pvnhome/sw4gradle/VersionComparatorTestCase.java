package kz.pvnhome.sw4gradle;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <p>versionMoreThan method testing</p>
 * <p><b>Created:</b> 22.02.2019 16:07:26</p>
 * @author victor
 */
public class VersionComparatorTestCase {
   @Test
   public void versionsNullOrEmpty() {
      assertFalse(WarLibResolver.versionMoreThan(null, ""));
      assertFalse(WarLibResolver.versionMoreThan("", null));
      assertFalse(WarLibResolver.versionMoreThan(null, null));
      assertFalse(WarLibResolver.versionMoreThan("", ""));
      assertTrue(WarLibResolver.versionMoreThan("1.0.0", ""));
      assertTrue(WarLibResolver.versionMoreThan("1.0.0", null));
      assertFalse(WarLibResolver.versionMoreThan("", "1.0.0"));
      assertFalse(WarLibResolver.versionMoreThan(null, "1.0.0"));
   }

   @Test
   public void versionsNoyDigit() {
      assertTrue(WarLibResolver.versionMoreThan("1.0.0", "qqq.qqq.qqq"));
      assertTrue(WarLibResolver.versionMoreThan("1.0.0", "v3.2.1"));
      assertFalse(WarLibResolver.versionMoreThan("x.x.x", "1.0.0"));
      assertFalse(WarLibResolver.versionMoreThan("v2.0.0", "1.0.0"));
   }

   @Test
   public void versions01() {
      assertTrue(WarLibResolver.versionMoreThan("2.0.0", "1.0.0"));
   }

   @Test
   public void versions02() {
      assertFalse(WarLibResolver.versionMoreThan("1.0.0", "2.0.0"));
   }

   @Test
   public void versions03() {
      assertTrue(WarLibResolver.versionMoreThan("2.1.0", "2.0.0"));
   }

   @Test
   public void versions04() {
      assertFalse(WarLibResolver.versionMoreThan("2.0.0", "2.1.0"));
   }

   @Test
   public void versions05() {
      assertTrue(WarLibResolver.versionMoreThan("2.1.2", "2.1.1"));
   }

   @Test
   public void versions06() {
      assertFalse(WarLibResolver.versionMoreThan("2.1.15", "2.1.100"));
   }

   @Test
   public void versions07() {
      assertTrue(WarLibResolver.versionMoreThan("2.1.2", "2.1"));
   }

   @Test
   public void versions08() {
      assertFalse(WarLibResolver.versionMoreThan("2", "2.1.100"));
   }

   @Test
   public void versions09() {
      assertTrue(WarLibResolver.versionMoreThan("2.1.2-vaadin1", "2.1-sp375"));
      assertTrue(WarLibResolver.versionMoreThan("2.1.2sp1", "2.1.1sp3"));
   }

   @Test
   public void versions10() {
      assertFalse(WarLibResolver.versionMoreThan("2sp3", "2.1.100sp3"));
      assertFalse(WarLibResolver.versionMoreThan("2.1.99-vaadin1", "2.1.100-456"));
   }
}
