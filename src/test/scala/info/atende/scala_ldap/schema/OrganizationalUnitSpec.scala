package info.atende.scala_ldap.schema

import info.atende.scala_ldap.{CN, LdapAttribute, LdapEntry, OU}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

/**
  * @author Giovanni Silva.
  */
@RunWith(classOf[JUnitRunner])
class OrganizationalUnitSpec extends Specification {
  "An OrganizationalUnitMapper" should {
    val dn = CN("test") / OU("ouname")
    val ou = new OrganizationalUnit("group", dn)
    val entry = LdapEntry(dn,Some(Seq(LdapAttribute("objectClass","organizationalUnit"),
      LdapAttribute("displayName","group"))))

    "Map from LdapEntry to organizational unit" in {
      OrganizationalUnit.ouMapper.mapFromEntry(entry) must beSome(ou)
    }

    "Map from LdapEntry with more then one objectClass" in {
      val entry = LdapEntry(dn,Some(Seq(LdapAttribute("objectClass","organizationalUnit"),
        LdapAttribute("objectClass","top"),
        LdapAttribute("displayName","group"))))
      OrganizationalUnit.ouMapper.mapFromEntry(entry) must beSome(ou)
    }
    "Map from Group to LdapEntry" in {
      OrganizationalUnit.ouMapper.mapToEntry(ou) must beSuccessfulTry.withValue(entry)
    }
  }
}
