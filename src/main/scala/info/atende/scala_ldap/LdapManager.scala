package info.atende.scala_ldap

import com.unboundid.ldap.sdk._
import com.unboundid.ldap.sdk.extensions.PasswordModifyExtendedRequest
import com.unboundid.util.ssl.{TrustAllTrustManager, SSLUtil}

import scala.util.{Failure, Success, Try}

/**
 * This class provide all operation in the Ldap, it also handles connections
 * @author Giovanni Silva.
 *         12/8/14
 */
class LdapManager(host: String, var password: String = null, var port: Int = LdapManager.DEFAULT_PORT,
                  var useSSL: Boolean = false, var userDN: DN = null, var keepConnection: Boolean = false) {
  val DEFAULT_TIMEOUT = 500
  var reuseConnection: LDAPConnection = _
  //--- Public API Starts

  /**
   * Execute an arbitrary function that depends on a connection. It forces the connection to close after the use
   * This function creates and handle the connection for you. It close the connection if any
   * exception was throw and return a Failure for recover.
   * Its used internally by this class. This method expose the underlining API from the UnboundSDK to the caller
   * its not recommend to use this method, use only if you need the flexibility provided by the underline API
   * @param f Function to execute, it must return the  LdapResult in success
   */
  def withConnection(f: LDAPConnection => LdapResult): Try[LdapResult] = {
    connect match {
      case Success(connection) =>
        try {
          val result = f(connection)
          Success(result)
        }catch {
          case e: Exception => Failure(e)
        } finally {
          if(!keepConnection)
            connection.close()
        }
      case Failure(ex) =>
        Failure(ex)

    }

  }

  /**
   * Connect to the server. With or without the credentials
   * @return The success LDAPConnection or Failure
   */
  def connect: Try[LDAPConnection] = {
    /**
     * Connect to the server
     * @return An LDAPConnection
     */
    def connectImpl(): Try[LDAPConnection] ={
      Try {
        val connection = if(useSSL) {
          val sslUtil = new SSLUtil()
          new LDAPConnection(sslUtil.createSSLSocketFactory())
        }else {
          new LDAPConnection()
        }
        connection.connect(host, port, DEFAULT_TIMEOUT)
        connection.bind(userDN.toString, password)
        connection
      }
    }
    if(keepConnection){

      if(reuseConnection != null && reuseConnection.isConnected){
        Success(reuseConnection)
      }else{
        val tryConnection = connectImpl()
        reuseConnection = tryConnection.getOrElse(null)
        tryConnection
      }
    }else{
      connectImpl()
    }
  }

  /**
   * Add the entry to ldap server mapping the attributes
   * @param entry The entry to be added
   * @return A success result of operation or a Failure with the exception
   */
  def add(entry: LdapEntry): Try[LdapResult] ={
    withConnection(c => {
      val result = c.add(LdapEntry.mapToSDKEntry(entry))
      LdapResult(result.getResultCode.intValue(), result.getDiagnosticMessage)
    })
  }
  /**
   * Add the entry to ldap server mapping the attributes
   * @param obj The object to be added
   * @return A success result of operation or a Failure with the exception
   */
  def add[T](obj: T)(implicit mapper: EntryMapper[T]): Try[LdapResult] = {
    mapper.mapToEntry(obj).flatMap(add)
  }

  /**
   * This method search and return the first element found if any, using a exact match
   * Ex.: CN=123,OU=Example,DC=example,DC=local will match only if exist exact as is
   * @param rdn The RDN atribute of the element. Ex.: CN("1234")
   * @param base The base to search the element, only elements that are direct childs of the base should be considered
   * @return The LdapEntry if Any
   */
  def lookup(rdn: RDN, base: DN): Option[LdapEntry] = {
    connect match {
      case Success(c) =>
        try {
          val result = c.search(base.toString, com.unboundid.ldap.sdk.SearchScope.ONE, s"($rdn)")
          if(result.getEntryCount > 0){
            val entry = result.getSearchEntries.get(0)
            Some(LdapEntry.mapFromSDKEntry(entry))
          }else {
            None
          }
        } catch {
          case ex: Exception => None
        }
        finally {
          c.close()
        }
      case Failure(ex) =>
        None
    }
  }
  /**
   * This method search and return the first element found if any, using a exact match
   * Ex.: CN=123,OU=Example,DC=example,DC=local will match only if exist exact as is
   * @return The LdapEntry if Any
   */
  def lookup(dn: DN): Option[LdapEntry] = {
    val base = DN(dn.values.tail)
    val rdn = dn.values.head
    lookup(rdn, base)
  }

  /**
   * Try to delete the entry by its Distinguished Name
   * @param dn The Distinguished Name representing the object to delete
   * @return The LdapResult or any exceptions
   */
  def delete(dn: DN): Try[LdapResult] = {
    withConnection( c => {
      val result = c.delete(dn.toString)
      LdapResult(result.getResultCode.intValue(), result.getDiagnosticMessage)
    })
  }

  /**
   * Try to delete the object by its Distinguished Name
   * @param obj The object to be deleted
   * @param mapper A mapper to convert the objet to LdapEntry
   * @tparam T Type of object to delete
   * @return The LdapResult or any exceptions
   */
  def delete[T](obj: T)(implicit mapper: EntryMapper[T]): Try[LdapResult] = {
    for {
      entry <- mapper.mapToEntry(obj)
      result <- delete(entry.dn)
    } yield result
  }

  /**
   * Try to modify an Ldap DN with the LdapModifications
   * @param dn The DN to Modify
   * @param modifications The modifications to perform, add, replace or delete attributes
   * @return The LdapResult from the operation
   */
  def modify(dn: DN, modifications: LdapModifications): Try[LdapResult] = {
    withConnection(c => {
      val result = c.modify(dn.toString, modifications.transformToSDKList)
      new LdapResult(result.getResultCode.intValue(), result.getDiagnosticMessage)
    })
  }
  /**
   * Try to modify an Ldap DN with the LdapModifications, this modifications will replace all attributes of the object
   * with the the new object
   * @param obj The obj to alter
   * @param mapper A mapper to convert the objet to LdapEntry
   * @return The LdapResult from the operation
   */
  def modify[T](obj: T)(implicit mapper: EntryMapper[T]): Try[LdapResult] = {
    for {
      entry <- mapper.mapToEntry(obj)
      result <- modify(entry.dn, mapper.getModificationOperations(obj))
    } yield result
  }

  /**
   * Processes a search operation with the provided information.  The search
   * result entries and references will be collected internally and included in
   * the {@code Seq[LdapEntry]} object that is returned.
   * <BR><BR>
   *
   * @param  baseDN      The base DN for the search request.  It must not be
   *                     { @code null}.
   * @param  scope       The scope that specifies the range of entries that
   *                     should be examined for the search.
   * @param  filter      The filter to use to
   *                     identify matching entries.  It must not be
   *                     { @code null}.

   *
   * @return  A collection of LdapEntry
   *
   */
  def search(baseDN: DN, filter: Filter, scope: SearchScope.SearchScope): Try[SearchResult] = {
    import scala.collection.JavaConverters._
    var listEntries: Seq[LdapEntry] = Seq.empty
    val result = withConnection(c => {
      val result = c.search(baseDN.toString, SearchScope.convertTOSDK(scope), filter)
      if(result.getEntryCount > 0){
        listEntries = result.getSearchEntries.asScala.map(LdapEntry.mapFromSDKEntry).toSeq
      }
      new LdapResult(result.getResultCode.intValue(), result.getDiagnosticMessage)
    })

    result.map(SearchResult(_,listEntries))
  }
  /**
    * Processes a search operation with the provided information.  The search
    * result entries and references will be collected internally and included in
    * the {@code Seq[LdapEntry]} object that is returned.
    * <BR><BR>
    *
    * @param  baseDN      The base DN for the search request.  It must not be
    *                     { @code null}.
    * @param  scope       The scope that specifies the range of entries that
    *                     should be examined for the search.
    * @param  filter      The string representation of the filter to use to
    *                     identify matching entries.  It must not be
    *                     { @code null}.

    *
    * @return  A collection of LdapEntry
    *
    */
  def search(baseDN: DN, filter: String, scope: SearchScope.SearchScope): Try[SearchResult] = {
    search(baseDN, Filter.create(filter), scope)
  }

  /**
   * Try to change the password for the user in Ldap
   * @param userDN The user reset the password
   * @param newPassword The new password
   * @param mustChangeOnNextLogon If the user need to change password on next logon. Default to false
   * @return The result of operation
   */
  def changePassword(userDN: DN, newPassword: String, mustChangeOnNextLogon: Boolean = false): Try[LdapResult] = {
    import scala.collection.JavaConverters._
    val quotedPassword = '"' + newPassword + '"'
    val quotedPasswordBytes = quotedPassword.getBytes("UTF-16LE")

    withConnection(c => {
      val modification = new Modification(ModificationType.REPLACE, "unicodePwd", quotedPasswordBytes)
      var modificationList =  modification :: Nil
      if(mustChangeOnNextLogon) {
        modificationList = modificationList :+ new Modification(ModificationType.REPLACE, "pwdLastSet", "0")
      }
      val result = c.modify(userDN.toString, modificationList.asJava)
      new LdapResult(result.getResultCode.intValue(), result.getDiagnosticMessage)
    })
  }

  /**
   * Execute a modifyDNRequest on server
   * @param request The modifyDNRequest
   * @return The result operation
   */
  def modifyDn(request: ModifyDNRequest): Try[LdapResult] = {
    withConnection(connection => {
      val result = connection.modifyDN(request)
      LdapResult(result.getResultCode.intValue(), result.getDiagnosticMessage)
    })
  }

  /**
   * Close a persisted connection if any
   */
  def closeConnection: Unit = {
    if(reuseConnection != null)
      reuseConnection.close()
  }

}

object LdapManager {
  val DEFAULT_PORT = 389
  val DEFAULT_SSL_PORT = 636

}
