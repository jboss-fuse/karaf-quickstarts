spring-security-camel: demonstrates a bundle with Camel route using Spring-Security mechanisms
==========================
Author: Fuse Team  
Level: Intermediate  
Technologies: Spring Security, Camell
Summary: a bundle with Camel route using Spring-Security mechanisms
Target Product: Fuse  
Source: <https://github.com/jboss-fuse/karaf-quickstarts/tree/7.x.redhat-7-x/security/spring-security/spring-security-camel>


What is it?
-----------
This quickstart demonstrates how to create an OSGi bundle that contains a Camel route using Spring Security role-based
access control.


System requirements
-------------------
Before building and running this quick start you need:

* Maven 3.5.0 or higher
* JDK 1.8
* Red Hat Fuse 7


Build and Deploy the Quickstart
-------------------------------

To build the quick start:

1. Change your working directory to `spring-security-camel` directory.
2. Run `mvn clean install` to build the quickstart.
3. Start Red Hat Fuse 7 by running bin/fuse (on Linux) or bin\fuse.bat (on Windows).
4. In the Red Hat Fuse console, enter the following commands to install required features

        feature:install -v spring-security
        feature:install -v aries-blueprint-spring
        feature:install -v camel-spring-security

5. In the Red Hat Fuse console, enter the following command to install `spring-security-camel` quickstart:

        install -s mvn:org.jboss.fuse.quickstarts.security/spring-security-camel/${project.version}

Use the bundle
--------------

After installing the bundle and required Camel, Spring Security and Aries Blueprint Spring features, an example
route will start automatically. Every 5 seconds, a org.jboss.fuse.quickstarts.security.camel.RandomSubjectProcessor
will be called and will set random (one of three) set of credentials as `CamelAuthentication` header. This header
is used by `<policy>` element of the route using this blueprint bean:

```xml
<camel-security:authorizationPolicy id="admin" access="ROLE_ADMIN"
                                    authenticationManager="authenticationManager"
                                    accessDecisionManager="accessDecisionManager" />
```

Depending on the roles associated with the set (random) subject, the message will be processed or one of the exception
handlers will be invoked:

* org.springframework.security.access.AccessDeniedException - when the subject doesn't have required permissions
* org.springframework.security.authentication.BadCredentialsException - when the subject is not authenticated
    
Undeploy the Bundle
-------------------

To stop and undeploy the bundle in Fuse:

1. Enter `bundle:list -s` command to retrieve your bundle id and symbolic name
2. To stop and uninstall the bundle enter

        bundle:uninstall <id>

    or (uninstall by symbolic name)

        bundle:uninstall spring-security-camel
