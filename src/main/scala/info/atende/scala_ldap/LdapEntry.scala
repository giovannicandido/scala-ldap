package info.atende.scala_ldap


import com.unboundid.ldap.sdk.{Attribute, Entry}

/**
 * This class provides a data structure for holding information about an LDAP
 * entry.  An entry contains a distinguished name (DN) and a set of attributes.
 *
 * @author Giovanni Silva.
 *         12/13/14
 */
case class LdapEntry(dn: DN, attributes: Option[List[LdapAttribute]])
object LdapEntry {
  private [scala_ldap] def mapFromSDKEntry(entry: Entry): LdapEntry = {
    import scala.collection.JavaConversions._
    val attributes: List[LdapAttribute] = entry.getAttributes.toList.map(a=>new LdapAttribute(a.getName, a.getValues))
    val dn = DN.parseString(entry.getDN).get
    new LdapEntry(dn, Some(attributes))
  }
  private[scala_ldap] def mapToSDKEntry(entry: LdapEntry): Entry = {
    import scala.collection.JavaConversions._
    val attributes: List[Attribute] = entry.attributes.getOrElse(List.empty).map(a => new Attribute(a.name, a.getValues.toList))
    new Entry(entry.dn.toString, attributes)
  }
}
