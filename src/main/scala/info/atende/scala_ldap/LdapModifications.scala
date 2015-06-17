package info.atende.scala_ldap

import com.unboundid.ldap.sdk.{ModificationType, Modification}

/**
 * LdapModifications is a class to perform edit modifications in entry's. Instead of make several connections to
 * update a entry, the manager could receive all operations to perform and batch then in one single connection operation.
 * @author Giovanni Silva 
 *         04/02/15.
 */
class LdapModifications(val addOperations: Map[String,String], val modifyOperations: Map[String,String],
                        val removeOperations: List[String]) {
  /**
   * Convert this modifications to the equivalent SDK modifications
   * @return
   */
  private[scala_ldap] def transformToSDKList: java.util.List[Modification] = {
    import scala.collection.JavaConverters._
    val add = addOperations.map{ case (k,v) => new Modification(ModificationType.ADD, k, v)}.toList
    val remove = removeOperations.map{new Modification(ModificationType.DELETE, _)}
    val replace = modifyOperations.map{ case (k,v) => new Modification(ModificationType.REPLACE, k, v)}.toList
    val all = add ::: remove ::: replace
    all.asJava
  }
}