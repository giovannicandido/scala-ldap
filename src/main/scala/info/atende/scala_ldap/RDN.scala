package info.atende.scala_ldap

import scala.annotation.tailrec

/**
 * Represents a RDN (Relative Distinguished Name).
 * This class is immutable
 * @author Giovanni Silva.
 *         12/10/14
 */
trait RDN {
  def apply(s: String): RDN

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
    DN(dn.values.::(this))
  }
  
  def parseString(rdn: String): Option[RDN] = {
    if(!rdn.toLowerCase.startsWith(Type) || rdn.split("=").size != 2) {
      None
    }else{
      Some(this(rdn.split("=")(1)))
    }
  }

  override def toString = s"$Type=${parseSpecialCaracteres(value)}"

  protected def parseSpecialCaracteres(value: String): String = {

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

  override def equals(o: scala.Any): Boolean = o match {
    case r: RDN => r.value.equals(value)
    case _ => false
  }

  override def hashCode(): Int = value.##
}

object RDN {
  def parseString(rdn: String): Option[RDN] = {
    val rdnLowerCase = rdn.toLowerCase
    if(rdnLowerCase.startsWith("dc=")){
      DC("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("cn=")){
      CN("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("ou=")){
      OU("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("o=")){
      O("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("street=")){
      STREET("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("l=")){
      L("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("st=")){
      ST("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("c=")){
      C("").parseString(rdn)
    }else if(rdnLowerCase.startsWith("uid=")){
      UID("").parseString(rdn)
    }else{
      None
    }
  }
}

/**
 * Domain Component
 * @param value Value for the RDN
 */
case class DC(value: String) extends RDN {
  override val Type: String = "dc"
  override def apply(value: String) = new DC(value)
}

/**
 * Common Name
 * @param value Value for the RDN
 */
case class CN(value: String) extends RDN {
  override val Type: String = "cn"
  override def apply(value: String) = new CN(value)
}

/**
 * Organizational Unit
 * @param value Value for the RDN
 */
case class OU(value: String) extends RDN {
  override val Type: String = "ou"
  override def apply(value: String) = new OU(value)
}

/**
 * Organizational Name
 * @param value Value for the RDN
 */
case class O(value: String) extends RDN {
  override val Type: String = "o"
  override def apply(value: String) = new O(value)
}

/**
 * Street Address
 * @param value Value for the RDN
 */
case class STREET(value: String) extends RDN {
  override val Type: String = "street"
  override def apply(value: String) = new STREET(value)
}

/**
 * Locality Name
 * @param value Value for the RDN
 */
case class L(value: String) extends RDN {
  override val Type: String = "l"
  override def apply(value: String) = new L(value)
}

/**
 * State or province name
 * @param value Value for the RDN
 */
case class ST(value: String) extends RDN {
  override val Type: String = "st"
  override def apply(value: String) = new ST(value)
}

/**
 * Country Name
 * @param value Value for the RDN
 */
case class C(value: String) extends RDN {
  override val Type: String = "c"
  override def apply(value: String) = new C(value)
}

/**
 * User ID
 * @param value Value for the RDN
 */
case class UID(value: String) extends RDN {
  override val Type: String = "uid"
  override def apply(value: String) = new UID(value)
}
