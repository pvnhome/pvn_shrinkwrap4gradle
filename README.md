# ShrinkWrap resolver for Gradle

ShrinkWrap resolver is the utility to obtain artifacts from a Gradle based projects depengencies using Gradle Tooling API.

## Resolution of libraries included in WAR lib folder

Resolution of libraries included in WAR lib folder is very simple:

```java
Collection<? extends Archive> libs = WarLibResolver.resolve().asList(JavaArchive.class);

WebArchive war = ShrinkWrap.create(WebArchive.class, "iris-api-ee.war")
                           // add other artifacts
                           // .addClass(AppConsts.class)
                           // .addClass(AppProps.class)
                           // etc.
                           .addAsLibraries((Collection<? extends Archive<?>>)libs);
```
