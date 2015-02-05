package info.atende.scala_ldap

/**
 * This is a Enumeration of scopes used in search operations
 * @author Giovanni Silva 
 *         04/02/15.
 */
object SearchScope extends Enumeration {
  import com.unboundid.ldap.sdk.{SearchScope => sdk}
  type SearchScope = Value
  /**
   * A predefined baseObject scope value, which indicates that only the entry specified by the base DN
   * should be considered.
   */
  val BASE = Value
  /**
   * A predefined singleLevel scope value, which indicates that only entries that are immediate subordinates of
   * the entry specified by the base DN (but not the base entry itself) should be considered.
   */
  val ONE = Value
  /**
   * A predefined wholeSubtree scope value, which indicates that the base entry itself and any
   * subordinate entries (to any depth) should be considered.
   */
  val SUB = Value
  /**
   * A predefined subordinateSubtree scope value, which indicates that any subordinate entries (to any depth) below
   * the entry specified by the base DN should be considered, but the base entry itself should not be considered.
   */
  val SUBORDINATE_SUBTREE = Value

  /**
   * Convert to the equivalent SDK value
   * @param scope SearchScope to convert from
   * @return The underline sdk equivalent
   */
  private [scala_ldap] def convertTOSDK(scope: SearchScope.SearchScope) = {
    scope match {
      case BASE => sdk.BASE
      case ONE => sdk.ONE
      case SUB => sdk.SUB
      case SUBORDINATE_SUBTREE => sdk.SUBORDINATE_SUBTREE
    }
  }
}
