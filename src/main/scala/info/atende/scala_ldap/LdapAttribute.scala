package info.atende.scala_ldap

import java.util.Date

import com.unboundid.asn1.ASN1OctetString
import com.unboundid.util.Debug._
import com.unboundid.util.StaticUtils._

/**
 * This class represent a Entry Attribute in LDAP
 * @author Giovanni Silva.
 *         12/13/14
 */
class LdapAttribute(val name: String) {

  /**
   * The array to use as the set of values when there are no values.
   */
  private val NO_VALUES: Array[ASN1OctetString] = new Array[ASN1OctetString](0)
  /**
   * The array to use as the set of byte array values when there are no values.
   */
  private val NO_BYTE_VALUES: Array[Array[Byte]] = new Array[Array[Byte]](0)

  private var values: Array[ASN1OctetString] = NO_VALUES

  /**
   * Creates a new LDAP attribute with the specified value
   * @param  name    The name for this attribute.
   * @param  value  A value for this attribute
   */
  def this(name: String, value: String) = {
    this(name)
    values = Array[ASN1OctetString](new ASN1OctetString(value))
  }

  /**
   * Creates a new LDAP attribute with the specified value
   * @param  name    The name for this attribute.
   * @param  value  A value for this attribute
   */
  def this(name: String, value: Array[Byte]) = {
    this(name)
    values = Array[ASN1OctetString](new ASN1OctetString(value))
  }
  def this(name: String, value: Array[String]) = {
    this(name)
    values = value.map(new ASN1OctetString(_)).toArray
  }

  /**
   * Creates a new LDAP attribute with the specified name and set of values.
   *
   * @param  name    The name for this attribute.
   * @param  values  The set of values for this attribute.
   */
  def this(name: String, values: String*)(implicit e: DummyImplicit) {
    this(name)
    this.values = values.map(new ASN1OctetString(_)).toArray
  }

  /**
   * Creates a new LDAP attribute with the specified name and set of values.
   *
   * @param  name    The name for this attribute.
   * @param  values  The set of values for this attribute.
   */
  def this(name: String, values: Array[Byte]*)(implicit e1: DummyImplicit, e2: DummyImplicit) {
    this(name)
    this.values = values.map(new ASN1OctetString(_)).toArray
  }



  /**
   * Retrieves the value for this attribute as a string.  If this attribute has
   * multiple values, then the first value will be returned.
   *
   * @return  The value for this attribute.
   */
  def value: Option[String] = if (values.length == 0) None else Some(values(0).stringValue())

  /**
   * Retrieves the value for this attribute as a byte array.  If this attribute
   * has multiple values, then the first value will be returned.  The returned
   * array must not be altered by the caller.
   *
   * @return  The value for this attribute
   */
  def valueByteArray: Option[Array[Byte]] = if (values.length == 0) None else Some(values(0).getValue)


  /**
   * Retrieves the value for this attribute as a Boolean.  If this attribute has
   * multiple values, then the first value will be examined.  Values of "true",
   * "t", "yes", "y", "on", and "1" will be interpreted as ``TRUE``.  Values
   * of "false", "f", "no", "n", "off", and "0" will be interpreted as
   * ``FALSE``.
   *
   * @return  The Boolean value for this attribute, or { @code None} if this
   *          attribute does not have any values or the value cannot be parsed
   *          as a Boolean.
   */
  def getValueAsBoolean: Option[Boolean] = {
    if (values.length == 0) {
      None
    } else {
      val lowerValue: String = values(0).stringValue.toLowerCase
      if ((lowerValue == "true") || (lowerValue == "t") || (lowerValue == "yes") || (lowerValue == "y") || (lowerValue == "on") || (lowerValue == "1")) {
        Some(true)
      }
      else if ((lowerValue == "false") || (lowerValue == "f") || (lowerValue == "no") || (lowerValue == "n") || (lowerValue == "off") || (lowerValue == "0")) {
        Some(false)
      } else {
        None
      }
    }
  }

  /**
   * Retrieves the value for this attribute as a Date, formatted using the
   * generalized time syntax.  If this attribute has multiple values, then the
   * first value will be examined.
   *
   * @return  The Date value for this attribute, or { @code None} if this
   *          attribute does not have any values or the value cannot be parsed
   *          as a Date.
   */
  def getValueAsDate: Option[Date] = {
    if (values.length == 0) {
      return None
    }
    try {
      Some(decodeGeneralizedTime(values(0).stringValue))
    }
    catch {
      case e: Exception =>
        debugException(e)
        None
    }
  }

  /**
   * Retrieves the value for this attribute as a DN.  If this attribute has
   * multiple values, then the first value will be examined.
   *
   * @return  The DN value for this attribute, or { @code None} if this attribute
   *          does not have any values or the value cannot be parsed as a DN.
   */
  def getValueAsDN: Option[DN] = {
    if (values.length == 0) {
      return None
    }
    try {
      val dn = new com.unboundid.ldap.sdk.DN(values(0).stringValue)
      DN.parseString(dn.toString)
    }
    catch {
      case e: Exception =>
        debugException(e)
        None
    }
  }

  /**
   * Retrieves the value for this attribute as an Integer.  If this attribute
   * has multiple values, then the first value will be examined.
   *
   * @return  The Integer value for this attribute, or { @code None} if this
   *          attribute does not have any values or the value cannot be parsed
   *          as an Integer.
   */
  def getValueAsInteger: Option[Integer] = {
    if (values.length == 0) {
      return None
    }
    try {
      val value = values(0).stringValue.toInt
      Some(value)
    }
    catch {
      case nfe: NumberFormatException =>
        debugException(nfe)
        None
    }
  }

  /**
   * Retrieves the value for this attribute as a Long.  If this attribute has
   * multiple values, then the first value will be examined.
   *
   * @return  The Long value for this attribute, or { @code None} if this
   *          attribute does not have any values or the value cannot be parsed
   *          as a Long.
   */
  def getValueAsLong: Option[Long] = {
    if (values.length == 0) {
      return null
    }
    try {
      val value = values(0).stringValue.toLong
      Some(value)
    }
    catch {
      case nfe: NumberFormatException =>
        debugException(nfe)
        None
    }
  }

  /**
   * Retrieves the set of values for this attribute as strings.  The returned
   * array must not be altered by the caller.
   *
   * @return  The set of values for this attribute, or an empty array if it does
   *          not have any values.
   */
  def getValues: Array[String] = {
    if (values.length == 0) {
      return NO_STRINGS
    }
    values.map(_.toString())
  }

  /**
   * Retrieves the set of values for this attribute as byte arrays.  The
   * returned array must not be altered by the caller.
   *
   * @return  The set of values for this attribute, or an empty array if it does
   *          not have any values.
   */
  def getValueByteArrays: Array[Array[Byte]] = {
    if (values.length == 0) {
      return NO_BYTE_VALUES
    }
    values.map(_.getValue)
  }

  /**
   * Retrieves the number of values for this attribute.
   *
   * @return  The number of values for this attribute.
   */
  def size: Int = {
    values.length
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[LdapAttribute]

  override def equals(other: Any): Boolean = {
    if(other.isInstanceOf[LdapAttribute]) {
      val o = other.asInstanceOf[LdapAttribute]
      (o canEqual this) &&
        getValues.toSeq.sorted == o.getValues.toSeq.sorted &&
        name.equalsIgnoreCase(o.name)
    }else{
      false
    }
  }

  override def hashCode(): Int = {
    val state = Seq(getValues.toSeq.sorted, name.toLowerCase)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = {
    s"$name:${getValues.mkString("|")}"
  }
}
object LdapAttribute {
  def apply(name: String, value: String) = new LdapAttribute(name, value)
}
