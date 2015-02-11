package info.atende.scala_ldap

/**
 * This class represent results from search operations
 * @author Giovanni Silva 
 *         11/02/15.
 */
case class SearchResult(result: LdapResult, entries: Seq[LdapEntry])
