package info.atende.scala_ldap

import org.specs2.mutable._

/**
 * Specifications for Distiguish Name
 * @author Giovanni Silva.
 *         12/9/14
 */
class DNSpec extends Specification {
  "Distinguish Name Spec".title
  "DN (Distinguish Name) and RDN (Relative Distinguish Name)".txt

  "An RDN" should {
    "override toString correctly" in {
      val dn = CN("Administrator")
      dn.toString shouldEqual "cn=Administrator"
    }

    "concat RDN in DN" in {
      val cn = CN("Administrador") / OU("Test") / DC("example") / DC("com")
      cn.toString shouldEqual "cn=Administrador,ou=Test,dc=example,dc=com"

      val ou = OU("Test") / CN("Users") / DC("example") / DC("com")
      ou.toString shouldEqual "ou=Test,cn=Users,dc=example,dc=com"

      cn.isInstanceOf[DN] shouldEqual true
      ou.isInstanceOf[DN] shouldEqual true

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
      dn.isInstanceOf[DN] shouldEqual true
      val dn2 = dn / OU("test")
      dn2.isInstanceOf[DN] shouldEqual true
      dn2.toString shouldEqual "cn=new,ou=first,ou=test"
    }


    "contact DN" in {
      val dn = CN("new") / OU("first")
      dn.isInstanceOf[DN] shouldEqual true
      val dn2 = OU("test") / OU("test2")
      dn2.isInstanceOf[DN] shouldEqual true
      val concatDN = dn / dn2
      concatDN.toString shouldEqual "cn=new,ou=first,ou=test,ou=test2"
    }

    "override toString correctly" in {
      val dn = CN("Admin") / OU("test")
      dn.toString shouldEqual "cn=Admin,ou=test"
    }
  }
}
