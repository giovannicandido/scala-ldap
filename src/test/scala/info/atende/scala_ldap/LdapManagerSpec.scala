package info.atende.scala_ldap

import com.unboundid.ldap.listener.{InMemoryDirectoryServer, InMemoryDirectoryServerConfig}
import com.unboundid.ldap.sdk.LDAPConnection
import org.specs2.mutable._

/**
 * @author Giovanni Silva.
 *         12/8/14
 */
class LdapManagerSpec extends Specification {
  val dc = "dc=example,dc=local"
  val userDN = "cn=Administrator,cn=Users," + dc
  val password = "password"
  val host = "localhost"
  val vhost = "192.168.54.136"
  val vpassword = "SuperPass%0254"
  var ds: InMemoryDirectoryServer = _

  "Ldap Manager Spec".title

  step({
    // Create the configuration to use for the server.
    val config: InMemoryDirectoryServerConfig =
      new InMemoryDirectoryServerConfig(dc)

    config.addAdditionalBindCredentials(userDN, password)

    // Create the directory server instance, populate it with data from the
    // "test-data.ldif" file, and start listening for client connections.
    ds = new InMemoryDirectoryServer(config)
    //  ds.importFromLDIF(true, "test-data.ldif");

    ds.startListening()
  })

  "LdapManager" should {
    "connect to a ldap server and disconnect after function is executed" in {
      val ldapManager = new LdapManager(userDN, password, host, ds.getListenPort)
      var c: LDAPConnection = null
      ldapManager.withConnection(f => {
        f.isConnected shouldEqual true
        c = f
      })
      // After the function is executed the connection should be closed
      c.isConnected shouldEqual false
    }

    "create a connection to be used" in {
      val ldapManager = new LdapManager(userDN, password, host, ds.getListenPort)
      val connection = ldapManager.connect
      connection.isSuccess shouldEqual true
      connection.get.isConnected shouldEqual true
    }
  }

  step({
    ds.shutDown(true)
  })

}
