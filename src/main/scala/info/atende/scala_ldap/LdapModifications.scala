package info.atende.scala_ldap

import com.unboundid.ldap.sdk.{ModificationType, Modification}

/**
 * @author Giovanni Silva 
 *         04/02/15.
 */
class LdapModifications(val addOperations: Map[String,String], val modifyOperations: Map[String,String],
                        val removeOperations: Map[String,String]) {

  private[scala_ldap] def transformToSDKList: java.util.List[Modification] = {
    import scala.collection.JavaConverters._
    val add = addOperations.map{ case (k,v) => new Modification(ModificationType.ADD, k, v)}.toList
    val remove = removeOperations.map{ case (k,v) => new Modification(ModificationType.DELETE, k, v)}.toList
    val replace = modifyOperations.map{ case (k,v) => new Modification(ModificationType.REPLACE, k, v)}.toList
    val all = add ::: remove ::: replace
    all.asJava
  }
}