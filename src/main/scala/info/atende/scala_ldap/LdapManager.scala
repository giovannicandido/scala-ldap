package info.atende.scala_ldap

import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.util.ssl.SSLUtil

import scala.util.{Failure, Success, Try}

/**
 * This class provide all operation in the Ldap, it also handles connections
 * @author Giovanni Silva.
 *         12/8/14
 */
class LdapManager(host: String) {
  private var port: Int = _
  private var userDN: String = _
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
  def this(userDN: String, password: String, host: String, port: Int = LdapManager.DEFAULT_PORT, useSSL: Boolean = false) = {
    this(host)
    this.port = port
    this.userDN = userDN
    this.password = password
    this.useSSL = useSSL
  }

  //--- Public API Starts

  /**
   * Execute an arbitrary function that depends on a connection. It forces the connection to close after the use
   * This function creates and handle the connection for you
   * @param f Function to execute
   */
  def withConnection(f: LDAPConnection => Unit): Unit = {
    connect match {
      case Success(connection) =>
        f(connection)
        connection.close()
      case Failure(ex) =>
        ex.printStackTrace()

    }

  }

  /**
   * Connect to the server. With or without the credentials
   * @return True if the connection success false and a message otherwise
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
        connection.bind(userDN, password)
      Success(connection)
    } catch {
      case e: Throwable =>
        connection.close()
        Failure(e)
    }
  }

}

object LdapManager {
  val DEFAULT_PORT = 389
  val DEFAULT_SSL_PORT = 636

}
