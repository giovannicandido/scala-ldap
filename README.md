# scala-ldap

This project is a wrapper for UnboundID SDK to ldap access in a more scala idiomatic way.

Is relative simple and battle tested in one mission critical product app as the underline communication ldap protocol.

It take me a long time to publish this library, and I'm not maintaining anymore, I almost forgot about it. 
But fell free to contact if this stuff is usefull for you (ldap is a pain in the a** :-), I will be happy ;-)

# Getting started

Start looking at the specs for RDN, and see the beauty and possibilities of scala lang:

Instead of:

```
"cn=bla,ou=users,dc=domain,dc=local"
```

Which is error prone, annoying and we do ALL the time.

Do:

```
CN("bla") / OU("users") / DC("domain") / DC("local")
```

Whichi is safe, and compile time checked, you can rest assure that any code with a DN parameter will work:

```
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
```

LdapManager class will do operations in ldap, and did I mention that it handles the connection open close in for us?

Take a look at `LdapManagerSpec`

```
val result = ldapManager.withConnection(f => {
        f.isConnected must beTrue
        c = f
        LdapResult(0, "Fake")
      })
```

The connection will be properly handled, no matter what happens (any exeception).
Also, every operation return a LdapResult if is atomic, or SearchResult if has some results, which makes map operations better.

Every object is a `LdapEntry`, which add some godies on the valila LdapEntry of UnboundID. Check `LdapEntrySpec`

You could do things like:

```
"find attribute by name" in {
      ldapEntry.findAttributeByName("displayName").head must equalTo(new LdapAttribute("displayname","value"))
      ldapEntry.findAttributeByName("Non exists").isEmpty must beTrue
    }
    "find attribute by name ignoring ranges" in {
      ldapEntry.findAttributeByName("member").head.value must equalTo(Some("member"))
    }
    "see if has a attribute with the given name and value" in {
      ldapEntry.hasAttributeWithValue("displayName","value") must beTrue
      ldapEntry.hasAttributeWithValue("displayName","false") must beFalse
    }
    "return the first attribute with a given name" in {
      ldapEntry.getFirstAttributeValueWithName("displayName") must beSome("value")
    }
    "ignore case of attribute in hasAttributeWithValue" in {
      ldapEntry.hasAttributeWithValue("displAyNaMe","value") must beTrue
    }
    
    "findAttributByName return multiple attributes" in {
          val ldapEntry = LdapEntry(CN("1234") / OU("test"), Some(Seq(LdapAttribute("member","value"), LdapAttribute("member;range=0-1499","member"))))
          val attributes = ldapEntry.findAttributeByName("member")
          attributes  must have size(2)
          attributes must contain(LdapAttribute("member", "value"))
    }
    
    "implement equals and hashcode correctly" in {
      val a = LdapEntry(CN("123") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      val b = LdapEntry(CN("123") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      (a == b) must beTrue
      a.## mustEqual b.##
    }
    "be usable in collections" in {
      val a = LdapEntry(CN("123") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      val b = LdapEntry(CN("124") / OU("test"), Some(Seq(new LdapAttribute("displayname","value"))))
      val seq = Seq(a,b)
      seq.find(_.dn == CN("123") / OU("test")) must beSome
    }
```

There is more, check the specs. Thanks :-)
