package info.atende.scala_ldap.schema

import info.atende.scala_ldap._

/**
 * This class represents a LDAP group of users
 * @author Giovanni Silva 
 *         04/02/15.
 */
case class Group(dn: DN)
object Group {
  implicit val groupMapper = new EntryMapper[Group] {
    /**
     * Map the object to a LdapEntry
     * @param obj Object to be mapped
     * @return The LdapEntry Equivalent to the object
     */
    override def mapToEntry(obj: Group): LdapEntry = {
      new LdapEntry(obj.dn, Some(Seq(
        LdapAttribute("objectClass","group"),
        LdapAttribute("groupType","2147483650"),
        LdapAttribute("objectClass","top")
      )))
    }

    /**
     * Map from a Entry to the equivalent object for this EntryMapper Type
     * @param entry The entry to be mapped
     * @return Optionally the LdapEntry
     */
    override def mapFromEntry(entry: LdapEntry): Option[Group] = {
      if(entry.hasAttributeWithValue("objectClass", "group")){
        Some(Group(entry.dn))
      }else{
        None
      }
    }

    override def getModificationOperations(obj: Group): LdapModifications = new LdapModifications(Map.empty, Map.empty, Map.empty)
  }
}
