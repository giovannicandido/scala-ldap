package info.atende.scala_ldap.schema

import info.atende.scala_ldap.{LdapAttribute, LdapEntry, OU, CN}
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
/**
 * This is the schema.Group spec
 * @author Giovanni Silva
 */
@RunWith(classOf[JUnitRunner])
class GroupSpec extends Specification {
  "An Group schema" should {
    val dn = CN("test") / OU("groupName")
    val group = new Group(dn)
    val entry = LdapEntry(dn,Some(Seq(LdapAttribute("objectClass","group"),
      LdapAttribute("groupType","2147483650"), LdapAttribute("objectClass","top"))))
    "Map from LdapEntry to Group in the companion object" in {
      Group.groupMapper.mapFromEntry(entry) must beSome(group)
    }
    "Map from Group to LdapEntry the companion object" in {
      Group.groupMapper.mapToEntry(group) must beSuccessfulTry.withValue(entry)
    }
  }
}
