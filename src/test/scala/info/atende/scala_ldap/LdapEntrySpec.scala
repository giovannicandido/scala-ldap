package info.atende.scala_ldap

import com.unboundid.ldap.sdk.{Attribute, Entry}
import org.specs2.matcher.MustMatchers
import org.specs2.mutable._

/**
 * @author Giovanni Silva 
 *         03/02/15.
 */
class LdapEntrySpec extends Specification with MustMatchers {
  "LdapEntry Spec".title

  "A LdapEntry" should {
    val ldapEntry = LdapEntry(CN("1234") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
    "find attribute by name" in {
      ldapEntry.findAttributeByName("displayName").head must equalTo(new LdapAttribute("displayname","value"))
      ldapEntry.findAttributeByName("Non exists").isEmpty must equalTo(true)
    }
    "see if has a attribute with the given name and value" in {
      ldapEntry.hasAttributeWithValue("displayName","value") must equalTo(true)
      ldapEntry.hasAttributeWithValue("displayName","false") must equalTo(false)
    }
    "return the first attribute with a given name" in {
      ldapEntry.getFirstAttributeValueWithName("displayName") must equalTo(Some("value"))
    }

  }

  "A LdapEntry Object" should {
    "map from entry to sdk" in {
      val entry = LdapEntry(CN("123") / OU("test"), Some(Seq(LdapAttribute("ObjectClass","Organizational-Unit"))))
      val sdk = LdapEntry.mapToSDKEntry(entry)
      "cn=123,ou=test" must equalTo(sdk.getDN)
      "Organizational-Unit" must equalTo (sdk.getAttribute("ObjectClass").getValue)
    }
    "map from sdk to entry" in {
      val entry = LdapEntry(CN("123") / OU("test"), Some(Seq(
        LdapAttribute("ObjectClass","Organizational-Unit"),
        LdapAttribute("displayName","Test")
      )))
      val sdk = new Entry(entry.dn.toString,
        new Attribute("ObjectClass","Organizational-Unit"),
        new Attribute("displayName","Test"))
      LdapEntry.mapFromSDKEntry(sdk) must equalTo(entry)
    }
  }
}
