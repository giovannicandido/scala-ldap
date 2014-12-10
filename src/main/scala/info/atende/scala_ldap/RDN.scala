package info.atende.scala_ldap

import scala.annotation.tailrec

/**
 * Represents a RDN (Relative Distinguished Name).
 * This class is immutable
 * @author Giovanni Silva.
 *         12/10/14
 */
trait RDN {
  val Type: String
  val value: String
  private val listSpecialCaracteres = List(',', '\\', '#', '+', '<', '>', ';', '"', '=')

  /**
   * This concats the RDN transforming in a DN. By example CN("a") / OU("o") equals cn=a,ou=o
   * The intent use is to create typesafe Distinguish Names
   * @param rdn A Relative Distinguish Name
   * @return An Distinguish Name
   */
  def /(rdn: RDN): DN = {
    DN(List(this, rdn))
  }

  /**
   * This concats the DN transforming in a new DN. By example CN("a") / OU("o") / (OU("test") / OU("inner")
   * equals cn=a,ou=o,ou=test,ou=inner
   *
   * The intent use is to create typesafe Distinguish Names
   * @param dn A Distinguish Name
   * @return A Distinguish Name composed of the two
   */
  def /(dn: DN): DN = {
    DN(dn.values :+ this)
  }

  override def toString = s"$Type=${parseString(value)}"

  protected def parseString(value: String): String = {

    @tailrec
    def replace(value: String, position: Int): String = {
      if (position >= listSpecialCaracteres.length) {
        value
      } else {
        val caracter = listSpecialCaracteres(position)
        replace(value.replace(caracter.toString, s"\\$caracter"), position + 1)
      }
    }
    replace(value, 0)
  }
}

/**
 * Domain Component
 * @param value Value for the RDN
 */
case class DC(value: String) extends RDN {
  override val Type: String = "dc"
}

/**
 * Common Name
 * @param value Value for the RDN
 */
case class CN(value: String) extends RDN {
  override val Type: String = "cn"
}

/**
 * Organizational Unit
 * @param value Value for the RDN
 */
case class OU(value: String) extends RDN {
  override val Type: String = "ou"
}

/**
 * Organizational Name
 * @param value Value for the RDN
 */
case class O(value: String) extends RDN {
  override val Type: String = "o"
}

/**
 * Street Address
 * @param value Value for the RDN
 */
case class STREET(value: String) extends RDN {
  override val Type: String = "street"
}

/**
 * Locality Name
 * @param value Value for the RDN
 */
case class L(value: String) extends RDN {
  override val Type: String = "l"
}

/**
 * State or province name
 * @param value Value for the RDN
 */
case class ST(value: String) extends RDN {
  override val Type: String = "st"
}

/**
 * Country Name
 * @param value Value for the RDN
 */
case class C(value: String) extends RDN {
  override val Type: String = "c"
}

/**
 * User ID
 * @param value Value for the RDN
 */
case class UID(value: String) extends RDN {
  override val Type: String = "uid"
}
