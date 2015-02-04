package info.atende.scala_ldap

import com.unboundid.ldap.listener.{InMemoryDirectoryServer, InMemoryDirectoryServerConfig}
import com.unboundid.ldap.sdk.LDAPConnection
import com.unboundid.ldif.LDIFReader
import info.atende.scala_ldap.schema.OrganizationalUnit
import org.specs2.mutable._

import scala.io.Source

/**
 * @author Giovanni Silva.
 *         12/8/14
 */
class LdapManagerSpec extends Specification {
  val dc = DC("example") / DC("local")
  val userDN = CN("Administrator") / CN("Users") / dc
  val password = "password"
  val host = "localhost"
  val vhost = "192.168.54.136"
  val vpassword = "SuperPass%0254"
  val structureLdif = "/structure.ldif"
  val adschema = "/adschema.ldif"
  val adConfiguration = "/configuration.ldif"
  var ds: InMemoryDirectoryServer = _

  def getManager: LdapManager = new LdapManager(userDN, password, host, ds.getListenPort)

  "Ldap Manager Spec".title

  step({
    // Create the configuration to use for the server.
    val config: InMemoryDirectoryServerConfig =
      new InMemoryDirectoryServerConfig(dc.toString)

    config.addAdditionalBindCredentials(userDN.toString, password)
    config.setSchema(null)

    // Create the directory server instance, populate it with data from the
    // "test-data.ldif" file, and start listening for client connections.
    ds = new InMemoryDirectoryServer(config)

    ds.importFromLDIF(false, new LDIFReader(Source.fromURL(getClass.getResource(adConfiguration)).bufferedReader()))
    ds.importFromLDIF(false, new LDIFReader(Source.fromURL(getClass.getResource(adschema)).bufferedReader()))
    ds.importFromLDIF(false, new LDIFReader(Source.fromURL(getClass.getResource(structureLdif)).bufferedReader()))


    ds.startListening()
  })

  "LdapManager" should {
    "connect to a ldap server and disconnect after function is executed" in {
      val ldapManager = new LdapManager(userDN, password, host, ds.getListenPort)
      var c: LDAPConnection = null
      ldapManager.withConnection(f => {
        f.isConnected shouldEqual true
        c = f
        LdapResult(0, "Fake")
      })
      // After the function is executed the connection should be closed
      c.isConnected shouldEqual false
    }

    "create a connection to be used" in {
      val ldapManager = getManager
      val connection = ldapManager.connect
      connection.isSuccess shouldEqual true
      connection.get.isConnected shouldEqual true
    }

    "add and lookup and delete a LdapEntry using DN" in {
      val manager = getManager
      val entry = new LdapEntry(CN("user") / CN("Users") / dc, Some(List(new LdapAttribute("objectClass: top"))))
      val success = manager.add(entry)
      success.isSuccess must beEqualTo(true)
      val result = manager.lookup(CN("user"), CN("Users") / dc)

      result.isDefined should beEqualTo(true)

      result.get.dn must beEqualTo(entry.dn)

      val deleteResult = manager.delete(entry.dn)
      deleteResult.isSuccess must beEqualTo(true)

      val result2 = manager.lookup(entry.dn)
      result2.isDefined should beEqualTo(false)

    }

    "add and lookup and delete a object that has a EntryMapper Implementation" in {
      val manager = getManager
      val ou = new OrganizationalUnit("This is the displayName", OU("new") / CN("Users") / dc)
      manager.add(ou).isSuccess mustEqual(true)
      manager.lookup(ou.dn).isDefined mustEqual(true)
      manager.delete(ou)
      manager.lookup(ou.dn).isDefined mustEqual(false)
    }

  }

  step({
    ds.shutDown(true)
  })

}
