spring-session-helper Grails plugin
===================================

This plugin enables integration of Grails flash scope and other mutable session variables with [Spring Session](http://spring.session).  This allows an app to be run
in a high availability configuration without needing sticky session or container clustering.  Presumably the Spring Session backing store will itself be run in a
HA configuration (eg for Redis by using AWS elasticache)

## Using this plugin

Add the plugin to your gradle dependencies, along with the Spring Boot and Spring Session implementation to use, eg for Redis support use:

```gradle
dependencies {
  compile 'org.grails.plugins:spring-session-helper:x.y'
  compile 'org.springframework.boot:spring-boot-starter-data-redis'
  compile 'org.springframework.session:spring-session-helper:spring-session-data-redis'
}
```

Theoretically this plugin will work with any Spring Session implementation.

### Config setup

By default the spring session filter is disabled after adding this plugin via setting `spring.session.store-type: 'none'`.
Therefore this plugin and the associated Spring Session dependencies may be included without compromising the ability to 
run with Tomcat's regular in-memory session store.

If no Spring Session store is configured, then this plugin also does nothing at runtime.

To enable Spring Session, first set:

```yaml
spring.session.store-type: 'redis'
```

Then setup the Spring Session store layer, eg by default the Redis session store will use the default Spring Redis connection factory, eg:

```yaml
spring:
  redis:
    host: 'cluster-hostname'
    password: 'password'
    port: 'cluster port'
```

Finally, you can adjust the config for this plugin:

```yaml
spring.session.grails:
  sync-flash-scope: true # enables/disables flash scope support, default true
  mutable-attributes:
   - 'mutable-session-attribute-name' # set the session attribute names that should always be synced because they contain mutable state
   - 'another-session-scoped-variable'
```

### Mutable Session Variables

***NOTE*** It's generally a bug to mutate the variable stored in the session, eg:

```groovy
def var = request.getSession(false)?.get('someVar')
var.foo = 'bar'
```

Becuase this will open you up to race conditions and other difficult to track concurrency issues as there is no locking mechanism in place.

A better method to do this would be:

```groovy
def session = request.getSession(false)
def var = session?.get('someVar')
session?.put('someVar', var.copyWith(foo: 'bar'))
```

Because we're replacing the object stored at 'someVar' with a new copy of the original, we aren't mutating some state that is potentially being read by another thread.  It also happens to work seamlessly with Spring Session, as Spring Session tracks session variables by use of Session methods like put and remove.

### Other Spring Session notes

Most Spring Session backing store implementations will need to serialise the objects being stored in the sesssion.  This means
its imperative that all objects we're storing are supported by the serialisation method employed by the backing store.  For the Redis store
the storage method is, by default, JDK serialisation, so all objects put into the session need to implement `java.io.Serializable`.  Other 
serialisations may be possible, eg JSON via Jackson.

### Implementation notes

This plugin uses the Spring Boot config properties annotation processor to get and configure the properties.  It enables auto-scan[1] on it's 
own `@Configuration` class.  This `@Configuration` only executes in the presence of a `SessionRepository` bean and will create a Spring 
`OncePerRequestFilter` (a Servlet filter) and insert it into the filter chain just after the Spring session filter.  The 
filter will then force the Grails flash scope object and any additional mutable variables configured, if it exists or existed 
to be re-set into the session each request.

[1]: TODO use spring.factories in later Spring Boot versions, possibly Grails 3.3?
