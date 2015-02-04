package info.atende.scala_ldap

/**
 * This class represents a result from Ldap in cases that the operation is atomic and do not return any content.
 * Ex.: Delete and update operations
 * @author Giovanni Silva 
 *         29/01/15.
 */
case class LdapResult(statusCode: Int, messageDiagnostic: String)
