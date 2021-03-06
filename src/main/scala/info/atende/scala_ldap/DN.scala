package info.atende.scala_ldap

/**
 * This trait Represents a Distinguished Name.
 * A distinguished name is a collection of [[info.atende.scala_ldap.RDN]] separated by ","
 *
 * The intention of this class is provide a typesafe way to compose DN. By example
 * {{{
 *   // Returns a new DN cn=name,ou=example
 *   CN("name") / OU("example")
 * }}}
 * @author Giovanni Silva.
 *         12/9/14
 */
case class DN(values: List[RDN]) {

  /**
   * This concats the RDN transforming in a new DN. By example CN("a") / OU("o") equals cn=a,ou=o
   * The intent use is to create typesafe Distinguish Names
   * @param rdn A Relative Distinguish Name
   * @return A Distinguish Name
   */
  def /(rdn: RDN): DN = {
    DN(values :+ rdn)
  }

  /**
   * This concat the DN to form a new DN
   * @param dn The the to concat
   * @return A new DN composed by the two
   */
  def /(dn: DN): DN = {
    DN(values ++ dn.values)
  }

  /**
   * Return the first RDN of the DN
   * @return The RDN of DN Ex.: A DN composed of CN=Users,OU=Test should return CN=Users
   */
  def rdn:RDN = values.head

  /**
   * Return the last elements of the DN, or a empty DN
   * @return a new DN or empty Ex.: A DN composed of CN=Users,OU=Test,DC=Example should return OU=Test,DC=Example
   */
  def base:DN = DN(values.tail)

  /**
   * Test if this DN is empty
   * @return If the DN is empty
   */
  def isEmpty: Boolean = values.isEmpty
  override def toString = values.map(_.toString).mkString(",")
}

object DN {
  def parseString(dn:String): Option[DN] = {
    val split = dn.split(",")
    var mutableDN: DN = null
    val rdns = split.map(RDN.parseString)
    if(rdns.contains(None) || rdns.isEmpty){
      None
    }else{
      val resultDN = rdns.flatten.foldLeft[DN](DN(List.empty)){(a,b) => a / b}
      Some(resultDN)
    }
  }
}
