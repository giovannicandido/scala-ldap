package info.atende.scala_ldap
import org.specs2.mutable._
/**
 * @author Giovanni Silva 
 *         03/02/15.
 */
class LdapAttributeSpec extends Specification {
  "LdapAttribute Spec".title

  "A LdapAttribute" should {
    "apply the correct equals" in {
      val a = new LdapAttribute("name","value")
      val b = new LdapAttribute("namE", "value")
      (a == b) should beTrue
      val c = new LdapAttribute("no","value")
      (a == c) should beFalse
      val d = new LdapAttribute("name","othervalue")
      (a == d) should beFalse

      val e = new LdapAttribute("name","value1","value2")
      val f = new LdapAttribute("name","value2","value1")
      (e == f) should beTrue
    }

    "apply the correct hashcode" in {
      val a = new LdapAttribute("name","value")
      val b = new LdapAttribute("namE", "value")
      a.## should equalTo(b.##)

      val c = new LdapAttribute("no","value")
      a.## should not equalTo c.##

      val d = new LdapAttribute("name","othervalue")
      a.## should not equalTo d.##

      val e = new LdapAttribute("name", "value")
      a.## should equalTo(e.##)

      val f = new LdapAttribute("name","value1","value2")
      val g = new LdapAttribute("name","value2","value1")
      f.## should be equalTo g.##
    }

  }
}
