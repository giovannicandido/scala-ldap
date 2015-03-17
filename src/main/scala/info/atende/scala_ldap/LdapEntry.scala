package info.atende.scala_ldap


import com.unboundid.ldap.sdk.{Attribute, Entry}

/**
 * This class provides a data structure for holding information about an LDAP
 * entry.  An entry contains a distinguished name (DN) and a set of attributes.
 *
 * @author Giovanni Silva.
 *         12/13/14
 */
case class LdapEntry(dn: DN, attributes: Option[Seq[LdapAttribute]]) {
  /**
   * Find a attribute by its name
   * @param name The name of attribute
   * @return A Sequence with the attributes that have the same name
   */
  def findAttributeByName(name: String): Seq[LdapAttribute] = {

    attributes.getOrElse(Seq.empty).filter(a => extractNameFromPossibleRange(a.name).equalsIgnoreCase(name))
  }

  /**
   * Removes any parameter from the name of attribute, by example member;range=0-1499 will became member
   * @param name The attribute name
   * @return The attribute without parameters
   */
  private def extractNameFromPossibleRange(name: String): String = {
    val to: Int = name.indexOf(";")
    if(to > 0) name.substring(0, to) else name
  }

  /**
   * Searchs for a attribute with the name and value, if any attribute is finded return True
   * @param attributeName The name of attribute, is case insensitive
   * @param value the value of attribute. Is <b>NOT case insensitive</b>
   * @return True if any attribute has the name and value equals
   */
  def hasAttributeWithValue(attributeName: String, value: String): Boolean = {
    attributes.exists(_.exists(a => a.name.equalsIgnoreCase(attributeName) && a.value == Some(value)))
  }

  /**
   * Get the value of a attribute if any
   * @param name The name of attribute
   * @return The String value if any
   */
  def getFirstAttributeValueWithName(name:String): Option[String] = {
    attributes.flatMap({attrs:Seq[LdapAttribute] => attrs.find(_.name.equalsIgnoreCase(name)).flatMap(_.value)})
  }
  override def toString = s"$dn {${attributes.getOrElse("").toString}}"
}
object LdapEntry {
  implicit def Ldap2DN(value: LdapEntry): DN = value.dn
  /**
   * This function convert a Entry to the equivalent LdapEntry that is used by library consumers
   * @param entry The entry to be converted
   * @return The Equivalent LdapEntry
   */
  private [scala_ldap] def mapFromSDKEntry(entry: Entry): LdapEntry = {
    import scala.collection.JavaConversions._
    val attributes: List[LdapAttribute] = entry.getAttributes.toList.map(a=>new LdapAttribute(a.getName, a.getValues))
    val dn = DN.parseString(entry.getDN).get
    new LdapEntry(dn, Some(attributes))
  }

  /**
   * This function convert a Entry to the equivalent SDK entry. Is not intent to be used by library consumers
   * @param entry The LdapEntry
   * @return The Equivalent SDK entry
   */
  private[scala_ldap] def mapToSDKEntry(entry: LdapEntry): Entry = {
    import scala.collection.JavaConversions._
    val attributes: Seq[Attribute] = entry.attributes.getOrElse(Seq.empty).map(a => new Attribute(a.name, a.getValues.toSeq))
    new Entry(entry.dn.toString, attributes)
  }
}
