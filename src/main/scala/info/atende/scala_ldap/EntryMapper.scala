package info.atende.scala_ldap

import scala.util.Try

/**
 * This trait provide a method signature for conversions between objects and LdapEntry.
 * It is used by LdapManger to provide the same behavior in methods that accept an entry as parameter.
 * The library user could provide EntryMapper for its types to persist in the Ldap Directory
 * @author Giovanni Silva 
 *         02/02/15.
 */
trait EntryMapper[T] {
  /**
   * Map the object to a LdapEntry
   * @param obj Object to be mapped
   * @return The LdapEntry Equivalent to the object
   */
  def mapToEntry(obj: T): Try[LdapEntry]

  /**
   * Map from a Entry to the equivalent object for this EntryMapper Type
   * @param entry The entry to be mapped
   * @return Optionally the LdapEntry
   */
  def mapFromEntry(entry: LdapEntry): Option[T]

  def getModificationOperations(obj: T): LdapModifications
}
