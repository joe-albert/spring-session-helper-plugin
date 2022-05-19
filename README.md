spring-session-helper Grails plugin
===================================

This plugin enables integration of Grails flash scope and other mutable session variables with [Spring Session](http://spring.session).  This allows an app to be run
in a high availability configuration without needing sticky session or container clustering.  Presumably the Spring Session backing store will itself be run in a
HA configuration (eg for Redis by using AWS elasticache)

## Using this plugin

Add the plugin to your gradle dependencies, along with the Spring Session implementation to use and the matching Spring Boot Data Starter, eg for Redis support use:

```gradle
dependencies {
  compile 'org.grails.plugins:spring-session-helper:x.y'
  compile 'org.springframework.boot:spring-boot-starter-data-mongodb'
  compile 'org.springframework.session:spring-session-helper:spring-session-data-mongodb'
}
```

Theoretically this plugin will work with any Spring Session implementation.

### Config setup

By default the spring session filter is disabled after adding this plugin via setting `spring.session.enabled: false`
and `spring.session.store-type: 'none'`.  The config property `spring.session.enabled` is only used by
this plugin and potentially by your own app, it's not a official Spring Session property.
By setting these two properties thus, this plugin and the associated Spring Session dependencies may be included without
compromising the ability to run with Tomcat's regular in-memory session store for development.

If no Spring Session store is configured, then this plugin also does nothing at runtime.

To enable Spring Session, first set:

```yaml
spring.session.enabled: false # Make this the default in application.yml and then override in external config to enable for production
spring.session.store-type: 'mongodb'
```

Then setup the Spring Session store data repository, eg by default the MongoDB session store will use the default Spring Data Mongo client, eg:

```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: sessions
```

Finally, you can adjust the config for this plugin:

```yaml
spring.session.grails:
  sync-flash-scope: true # enables/disables flash scope support, default true
  mutable-attributes:
   - 'mutable-session-attribute-name' # set the session attribute names that should always be synced because they contain mutable state
   - 'another-session-scoped-variable'
```

### Grails `withForm`

Grails stores the `withForm` tokens in a mutable session variable, so you should add `SYNCHRONIZER_TOKENS_HOLDER` to your `mutable-attributes` list if using the `withForm` controller method, like so:

```yaml
spring.session.grails.mutable-attributes:
 - SYNCHRONIZER_TOKENS_HOLDER
 - etc
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

### MongoDB sesssion repo notes

MongoDBs AutoConfiguration and the MongoHttpSession need to be enabled via
Java annotations on a Spring Boot config class.  It also requires a session serialiser to be defined.
A simple way of doing so that ensures no additional beans are created in a dev environment is to define the follow static class in your Grails init `Application.groovy`:

```groovy
    @Configuration
    @ConditionalOnProperty(value = "spring.session.enabled", havingValue = "true)
    @Import(MongoAutoConfiguration) // unsure if this is disabled by grails?
    @EnableMongoHttpSession
    @EnableConfigurationProperties(MongoProperties)
    static class MongoSessionConfig {
    
        @Bean
        JdkMongoSessionConverter jdkMongoSessionConverter() {
            return new JdkMongoSessionConverter(Duration.ofMinutes(15L));
        }
    }
```

### Hazelcast notes

This section to be completed

### AWS Elasticache notes

AWS Elasticache prevents the use of CONFIG commands via the redis protocol.  To make Spring Session Redis work with elasticache,
configure a `@Bean` in your `Application` like so:

```groovy
    @ConditionalOnProperty('spring.session.disable-redis-config-action')
    @Bean
    ConfigureRedisAction configureRedisAction() {
        ConfigureRedisAction.NO_OP
    }
```

Then you can enable / disable the No-op `ConfigureRedisAction` with `spring.session.disable-redis-config-action` property.

You also need to configure your Elasticache instance manually, see [here](https://docs.spring.io/spring-session/docs/current/reference/html5/#api-redisoperationssessionrepository-sessiondestroyedevent) and [here](https://github.com/spring-projects/spring-session/issues/124#issuecomment-71525940) for more details.

### Implementation notes

This plugin uses the Spring Boot config properties annotation processor to get and configure the properties.  It enables auto-scan[1] on it's 
own `@Configuration` class.  This `@Configuration` only executes in the presence of a `SessionRepository` bean and will create a Spring 
`OncePerRequestFilter` (a Servlet filter) and insert it into the filter chain just after the Spring session filter.  The 
filter will then force the Grails flash scope object and any additional mutable variables configured, if it exists or existed 
to be re-set into the session each request.

[1]: TODO use spring.factories in later Spring Boot versions, possibly Grails 3.3?
