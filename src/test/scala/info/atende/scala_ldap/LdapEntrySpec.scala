package info.atende.scala_ldap

import com.unboundid.ldap.sdk.{Attribute, Entry}
import org.specs2.matcher.MustMatchers
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
/**
 * @author Giovanni Silva 
 *         03/02/15.
 */
@RunWith(classOf[JUnitRunner])
class LdapEntrySpec extends Specification with MustMatchers {
  "LdapEntry Spec".title

  "A LdapEntry" should {
    val ldapEntry = LdapEntry(CN("1234") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"), LdapAttribute("member;range=0-1499","member"))))
    "find attribute by name" in {
      ldapEntry.findAttributeByName("displayName").head must equalTo(new LdapAttribute("displayname","value"))
      ldapEntry.findAttributeByName("Non exists").isEmpty must beTrue
    }
    "find attribute by name ignoring ranges" in {
      ldapEntry.findAttributeByName("member").head.value must equalTo(Some("member"))
    }
    "see if has a attribute with the given name and value" in {
      ldapEntry.hasAttributeWithValue("displayName","value") must beTrue
      ldapEntry.hasAttributeWithValue("displayName","false") must beFalse
    }
    "return the first attribute with a given name" in {
      ldapEntry.getFirstAttributeValueWithName("displayName") must beSome("value")
    }
    "ignore case of attribute in hasAttributeWithValue" in {
      ldapEntry.hasAttributeWithValue("displAyNaMe","value") must beTrue
    }
    "ignore case of attribute in findAttributeByName" in {
      ldapEntry.findAttributeByName("displAyNaMe").isEmpty must beFalse
    }
    "ignore case of attribute in getFistAttributeValueWithName" in {
      ldapEntry.getFirstAttributeValueWithName("displAyNaMe") must beSome("value")
    }
    "findAttributByName return multiple attributes" in {
          val ldapEntry = LdapEntry(CN("1234") / OU("test"), Some(Seq(LdapAttribute("member","value"), LdapAttribute("member;range=0-1499","member"))))
          val attributes = ldapEntry.findAttributeByName("member")
          attributes  must have size(2)
          attributes must contain(LdapAttribute("member", "value"))
    }
    "getFirstAttributeValueWithName return one value from multiples" in {
          val ldapEntry = LdapEntry(CN("1234") / OU("test"), Some(Seq(LdapAttribute("member","value"), LdapAttribute("member","member"))))
          val attributes = ldapEntry.getFirstAttributeValueWithName("member")
          attributes must beSome("value")
    }
    "implement equals and hashcode correctly" in {
      val a = LdapEntry(CN("123") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      val b = LdapEntry(CN("123") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      (a == b) must beTrue
      a.## mustEqual b.##
    }
    "be usable in collections" in {
      val a = LdapEntry(CN("123") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      val b = LdapEntry(CN("124") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      val seq = Seq(a,b)
      seq.find(_.dn == CN("123") / OU("test")) must beSome
    }
    "implement toString correctly" in {
      val cn = CN("1234") / OU("test")
      val attribute = new LdapAttribute("displayname","value")
      val attribute2 = new LdapAttribute("newAttribute","value kas")
      val ldapEntry = LdapEntry(cn, Some(Seq(attribute,attribute2)))
      ldapEntry.toString mustEqual s"${cn} {List($attribute, $attribute2)}"
    }

    "has objectClassName" in {
      val cn = CN("1234") / OU("test")
      val attribute = new LdapAttribute("displayname","value")
      val attribute2 = new LdapAttribute("newAttribute","value kas")
      val className = LdapAttribute("objectClass","Type")
      val ldapEntry = LdapEntry(cn, Some(Seq(attribute,attribute2, className)))
      ldapEntry.hasObjectClass("type") must beTrue
      val ldapEntry2 = ldapEntry.copy(attributes = Some(Seq(LdapAttribute("objectClass", "newType"))))
      ldapEntry2.hasObjectClass("type") must beFalse
    }

    "has objectClassName Active Directory" in {
      val cn = CN("1234") / OU("test")
      val attribute = new LdapAttribute("displayname","value")
      val attribute2 = new LdapAttribute("newAttribute","value kas")
      val className = LdapAttribute("objectClass","top|Type")
      val ldapEntry = LdapEntry(cn, Some(Seq(attribute,attribute2, className)))
      ldapEntry.hasObjectClass("type") must beTrue
      val ldapEntry2 = ldapEntry.copy(attributes = Some(Seq(LdapAttribute("objectClass", "top|newType"))))
      ldapEntry2.hasObjectClass("type") must beFalse
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
