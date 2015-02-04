package info.atende.scala_ldap

import com.unboundid.ldap.sdk._
import com.unboundid.util.ssl.SSLUtil

import scala.util.{Failure, Success, Try}

/**
 * This class provide all operation in the Ldap, it also handles connections
 * @author Giovanni Silva.
 *         12/8/14
 */
class LdapManager(host: String) {
  private var port: Int = _
  private var userDN: DN = _
  private var password: String = _
  private var useSSL = false

  /**
   * Create a connection with host and port. Does not use credentials
   * @param host Host to connect
   * @param port Port
   */
  def this(host: String, port: Int) = {
    this(host)
    this.port = port
  }

  /**
   * Create a connection with host and port. Does not use credentials
   * @param host Host to connect
   * @param port Port
   * @param useSSL True if the connection should use SSL
   */
  def this(host: String, port: Int, useSSL: Boolean) = {
    this(host)
    this.port = port
    this.useSSL = useSSL
  }

  /**
   * Connect with the credentials, host port and maybe ssl
   * @param userDN The user dn to autenticate, example cn=Admin,cn=User
   * @param password The password to autenticate
   * @param host The host to connect
   * @param port The port to connect default to LdapManager.DEFAULT_PORT
   * @param useSSL If true use SSL to connect. Default to false
   */
  def this(userDN: DN, password: String, host: String, port: Int = LdapManager.DEFAULT_PORT, useSSL: Boolean = false) = {
    this(host)
    this.port = port
    this.userDN = userDN
    this.password = password
    this.useSSL = useSSL
  }

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
          case e: Throwable => Failure(e)
        } finally {
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
    val connection = new LDAPConnection()
    if (useSSL) {
      val sslUtil = new SSLUtil()
      connection.setSocketFactory(sslUtil.createSSLSocketFactory())
    }
    try {
      if (port != 0)
        connection.connect(host, port)
      else if (useSSL)
        connection.connect(host, LdapManager.DEFAULT_SSL_PORT)
      else
        connection.connect(host, LdapManager.DEFAULT_PORT)
      if (userDN != null)
        connection.bind(userDN.toString, password)
      Success(connection)
    } catch {
      case e: Throwable =>
        connection.close()
        Failure(e)
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
    val entry = mapper.mapToEntry(obj)
    add(entry)
  }

  /**
   * This method search and return the first element found if any
   * @param rdn The RDN atribute of the element. Ex.: CN("1234")
   * @param base The base to search the element, only elements that are direct childs of the base should be considered
   * @return The LdapEntry if Any
   */
  def lookup(rdn: RDN, base: DN): Option[LdapEntry] = {
    connect match {
      case Success(c) =>
        try {
          val result = c.search(base.toString, SearchScope.ONE, s"($rdn)")
          if(result.getEntryCount > 0){
            val entry = result.getSearchEntries.get(0)
            Some(LdapEntry.mapFromSDKEntry(entry))
          }else {
            None
          }
        } finally {
          c.close()
        }
      case Failure(ex) =>
        None
    }
  }
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
    val entry = mapper.mapToEntry(obj)
    delete(entry.dn)
  }

}

object LdapManager {
  val DEFAULT_PORT = 389
  val DEFAULT_SSL_PORT = 636

}
