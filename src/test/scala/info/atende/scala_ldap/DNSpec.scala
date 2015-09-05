package info.atende.scala_ldap

import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
/**
 * Specifications for Distiguish Name
 * @author Giovanni Silva.
 *         12/9/14
 */
@RunWith(classOf[JUnitRunner])
class DNSpec extends Specification {
  "Distinguish Name Spec".title
  "DN (Distinguish Name) and RDN (Relative Distinguish Name)".txt

  "An RDN" should {
    "override toString correctly" in {
      val dn = CN("Administrator")
      dn.toString shouldEqual "cn=Administrator"
    }

    "override equals and hashcode correctly" in {
      val a = CN("info")
      val b = CN("info")
      a mustEqual b
      a.## mustEqual b.##
    }

    "concat RDN in DN" in {
      val cn = CN("Administrador") / OU("Test") / DC("example") / DC("com")
      cn.toString shouldEqual "cn=Administrador,ou=Test,dc=example,dc=com"

      val ou = OU("Test") / CN("Users") / DC("example") / DC("com")
      ou.toString shouldEqual "ou=Test,cn=Users,dc=example,dc=com"

      cn.isInstanceOf[DN] must beTrue
      ou.isInstanceOf[DN] must beTrue

    }
    "concat RDN / DN in DN" in {
      val rdn = CN("users")
      val dn = DC("example") / DC("local")
      (rdn / dn).toString shouldEqual "cn=users,dc=example,dc=local"
    }
    "parse the string value" in {
      val dn = CN("cn=Admin")
      val dnOu = CN("ou=Admin")
      dn.toString shouldEqual "cn=cn\\=Admin"
      dnOu.toString shouldEqual "cn=ou\\=Admin"
      val dnOu2 = OU("ou=Admin")
      dnOu2.toString shouldEqual "ou=ou\\=Admin"
    }
  }

  "An DN" should {
    "concact RDN" in {
      val dn = CN("new") / OU("first")
      dn.isInstanceOf[DN] must beTrue
      val dn2 = dn / OU("test")
      dn2.isInstanceOf[DN] must beTrue
      dn2.toString shouldEqual "cn=new,ou=first,ou=test"
    }

    "override equals and hashcode correctly" in {
      val a = OU("info") / OU("main")
      val b = OU("info") / OU("main")
      a mustEqual b
      a.## mustEqual b.##
    }

    "contact DN" in {
      val dn = CN("new") / OU("first")
      dn.isInstanceOf[DN] must beTrue
      val dn2 = OU("test") / OU("test2")
      dn2.isInstanceOf[DN] must beTrue
      val concatDN = dn / dn2
      concatDN.toString shouldEqual "cn=new,ou=first,ou=test,ou=test2"
    }

    "override toString correctly" in {
      val dn = CN("Admin") / OU("test")
      dn.toString shouldEqual "cn=Admin,ou=test"
    }
  }
}
