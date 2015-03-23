package info.atende.scala_ldap

import com.unboundid.ldap.listener.{InMemoryDirectoryServer, InMemoryDirectoryServerConfig}
import com.unboundid.ldap.sdk.{LDAPSearchException, LDAPConnection}
import com.unboundid.ldif.LDIFReader
import info.atende.scala_ldap.schema.{Group, OrganizationalUnit}
import org.specs2.mutable._

import scala.io.Source
import scala.util.{Success, Failure}

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

  def getManager: LdapManager = new LdapManager(host, userDN= userDN, password=password, port = ds.getListenPort)
  def getManagerWithPersistentConnection: LdapManager = new LdapManager(host, userDN= userDN, password=password,
    port = ds.getListenPort, keepConnection = true)
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
      val ldapManager = new LdapManager(host, userDN= userDN, password=password, port = ds.getListenPort)
      var c: LDAPConnection = null
      ldapManager.withConnection(f => {
        f.isConnected must beTrue
        c = f
        LdapResult(0, "Fake")
      })
      // After the function is executed the connection should be closed
      c.isConnected shouldEqual false
    }

    "create a connection to be used" in {
      val ldapManager = getManager
      val connection = ldapManager.connect
      connection.isSuccess must beTrue
      connection.get.isConnected must beTrue
    }

    "configure to create only one connection in the lifecycle of object" in {
      val ldapManager = getManagerWithPersistentConnection
      val connection1 = ldapManager.connect
      val connection2 = ldapManager.connect
      connection1.isSuccess must beTrue
      connection2.isSuccess must beTrue
      connection1.get.getConnectionID must beEqualTo(connection2.get.getConnectionID)
      var connectionId1: Long = 0
      var connectionId2: Long = 1
      ldapManager.withConnection(f => {
        f.isConnected must beTrue
        connectionId1 = f.getConnectionID
        LdapResult(0, "Fake")
      })
      ldapManager.withConnection(f => {
        f.isConnected must beTrue
        connectionId2 = f.getConnectionID
        LdapResult(0, "Fake")
      })
      ldapManager.closeConnection
      connectionId1 must equalTo(connectionId2)

    }

    "Close a persistent connection" in {
      val ldapManager = getManagerWithPersistentConnection
      val connection = ldapManager.connect
      connection.isSuccess must beTrue
      connection.get.isConnected must beTrue
      ldapManager.closeConnection
      connection.get.isConnected must beFalse
      val connection2 = ldapManager.connect
      connection2 must beFailedTry
    }

    "add and lookup and delete a LdapEntry using DN" in {
      val manager = getManager
      val entry = new LdapEntry(CN("user") / CN("Users") / dc, Some(List(new LdapAttribute("objectClass: top"))))
      val success = manager.add(entry)
      success.isSuccess must beTrue
      val result = manager.lookup(CN("user"), CN("Users") / dc)

      result.isDefined should beTrue

      result.get.dn must beEqualTo(entry.dn)

      val deleteResult = manager.delete(entry.dn)
      deleteResult.isSuccess must beTrue

      val result2 = manager.lookup(entry.dn)
      result2.isDefined should beFalse

    }

    "add and lookup and delete a object that has a EntryMapper Implementation" in {
      val manager = getManager
      val ou = new OrganizationalUnit("This is the displayName", OU("new") / CN("Users") / dc)
      manager.add(ou).isSuccess must beTrue
      manager.lookup(ou.dn).isDefined must beTrue
      manager.delete(ou)
      manager.lookup(ou.dn).isDefined must beFalse
    }

    "modify a DN object" in {
      val manager = getManager
      val newDisplayName = "new display name"
      val ou = new OrganizationalUnit("This is the displayName", OU("newToModify") / CN("Users") / dc)
      manager.add(ou).isSuccess mustEqual true
      manager.modify(ou.dn, new LdapModifications(Map.empty,Map("displayName" -> newDisplayName),Map.empty))
        .isSuccess mustEqual true
      manager.lookup(ou.dn).get.hasAttributeWithValue("displayName", newDisplayName) mustEqual true

    }

    "modify the object with its modifications" in {
      val manager = getManager
      val newDisplayName = "new display name"
      val ou = new OrganizationalUnit("This is the displayName", OU("newToModify1") / CN("Users") / dc)
      val newOu = ou.copy(name = newDisplayName)
      manager.add(ou).isSuccess must beTrue
      manager.modify(newOu)
        .isSuccess mustEqual true
      manager.lookup(ou.dn).get.getFirstAttributeValueWithName("displayName").get mustEqual newDisplayName

    }

    "search objects in the base scope" in {
      val manager = getManager

      // Add some Groups
      val g1 = new Group(CN("g1") / CN("Users") / dc )
      val g2 = new Group(CN("g2") / CN("Users") / dc )
      manager.add(g1).isSuccess must beTrue
      manager.add(g2).isSuccess must beTrue
      val result = manager.search(CN("Users") / dc, "(objectclass=Group)", SearchScope.ONE)
      result must beSuccessfulTry
      result.get.entries.size must beGreaterThan(0)
      result.get.entries.find(_.dn == g1.dn) must beSome
      result.get.entries.find(_.dn == g2.dn) must beSome
    }

    "return the ldap result in searchs" in {
      val manager = getManager
      val result = manager.search(OU("Não existe") / dc, "(objectclass=Group)", SearchScope.SUB)
      result must beFailedTry.like {
        case e: LDAPSearchException => e.getSearchResult.getResultCode.intValue() == 32 &&
          e.getSearchResult.getDiagnosticMessage.startsWith("Unable to perform the search")
      }
    }

  }

  step({
    ds.shutDown(true)
  })

}
