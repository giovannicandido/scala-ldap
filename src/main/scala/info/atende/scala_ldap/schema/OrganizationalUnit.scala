package info.atende.scala_ldap.schema


import info.atende.scala_ldap._

/**
 * Represents a LDAP Organizational Unit
 * @author Giovanni Silva 
 *         02/02/15.
 */
case class OrganizationalUnit(name: String, dn: DN)
object OrganizationalUnit {
  implicit val ouMapper = new EntryMapper[OrganizationalUnit] {
    /**
     * Map the object to a LdapEntry
     * @param obj Object to be mapped
     * @return The LdapEntry Equivalent to the object
     */
    override def mapToEntry(obj: OrganizationalUnit): LdapEntry = {
      new LdapEntry(obj.dn, Some(new LdapAttribute("ObjectClass", "Organizational-Unit") :: new LdapAttribute("displayName",obj.name) :: Nil))
    }

    /**
     * Map from a Entry to the equivalent object for this EntryMapper Type
     * @param entry The entry to be mapped
     * @return Optionally the LdapEntry
     */
    override def mapFromEntry(entry: LdapEntry): Option[OrganizationalUnit] = {
      if(entry.hasAttributeWithValue("objectclass","Organizational-Unit")){
        val name = entry.getFirstAttributeValueWithName("displayName").getOrElse("")
        val dn = entry.dn
        Some(new OrganizationalUnit(name, dn))
      }else{
        None
      }
    }

    override def getModificationOperations(obj: OrganizationalUnit): LdapModifications = {
      new LdapModifications(Map.empty, Map("displayName"->obj.name), Map.empty)
    }
  }
}
