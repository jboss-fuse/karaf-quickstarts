spring-security-httpservice-blueprint: demonstrates a bundle with Whiteboard registered servlet and Spring-Security filter
==========================
Author: Fuse Team  
Level: Intermediate  
Technologies: Spring Security, PAX-WEB, Blueprint
Summary: a bundle with Whiteboard registered servlet and Spring-Security filter
Target Product: Fuse  
Source: <https://github.com/jboss-fuse/karaf-quickstarts/tree/7.x.redhat-7-x/security/spring-security/spring-security-httpservice-blueprint>


What is it?
-----------
This quickstart demonstrates how to create an OSGi bundle that registers a servlet and at the same time, full Spring Security
_filter chain_ is registered using Spring Security XML DSL.
The Spring Security namespace is handled using aries-blueprint-spring feature, which allows to handle Spring namespaces
using blueprint technology.


System requirements
-------------------
Before building and running this quick start you need:

* Maven 3.5.0 or higher
* JDK 1.8
* Red Hat Fuse 7


Build and Deploy the Quickstart
-------------------------------

To build the quick start:

1. Change your working directory to `spring-security-httpservice-blueprint` directory.
2. Run `mvn clean install` to build the quickstart.
3. Start Red Hat Fuse 7 by running bin/fuse (on Linux) or bin\fuse.bat (on Windows).
4. In the Red Hat Fuse console, enter the following commands to install required Spring Security features

        feature:install -v spring-security
        feature:install -v aries-blueprint-spring

5. In the Red Hat Fuse console, enter the following command to install `spring-security-httpservice-blueprint` quickstart:

        install -s mvn:org.jboss.fuse.quickstarts.security/spring-security-httpservice-blueprint/${project.version}

Use the bundle
--------------

After installing the bundle and required Spring Security and Aries Blueprint Spring features, it is enough to navigate
to http://localhost:8181/spring-security-blueprint/start page. This page informs that anonymous user accessed the page.
The login form is configured to send the credentials to Spring Security form login processor. There are 2 users
configured using Spring Security `authentication-manager`:

* user/user with `ROLE_USER`
* admin/admin with `ROLE_USER` and `ROLE_ADMIN`

After logging in, the main page is displayed showing the information about logged in user. If the user lacks `ROLE_ADMIN`
role, it is not possible to navigate to http://localhost:8181/spring-security-blueprint/admin/start - administrator area.
`admin` user can navigate both to http://localhost:8181/spring-security-blueprint/user/start and
http://localhost:8181/spring-security-blueprint/admin/start pages.

After logging out, Spring Security clears the remembered credentials and navigates back to the login page.
    
Undeploy the Bundle
-------------------

To stop and undeploy the bundle in Fuse:

1. Enter `bundle:list -s` command to retrieve your bundle id and symbolic name
2. To stop and uninstall the bundle enter

        bundle:uninstall <id>

    or (uninstall by symbolic name)

        bundle:uninstall spring-security-httpservice-blueprint
