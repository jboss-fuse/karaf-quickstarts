camel-linkedin: Demonstrates the camel-linkedin component
======================================================
Author: Fuse Team  
Level: Beginner  
Technologies: Camel, Blueprint  
Summary: This quickstart demonstrates how to use the camel-linkedin component in Camel to search companies
Target Product: Fuse  
Source: <https://github.com/jboss-fuse/karaf-quickstarts/tree/7.x.redhat-7-x/camel/camel-linkedin>



What is it?
-----------

This quick start shows how to use Apache Camel, and its OSGi integration to use the OSGi config admin and search companies from LinkedIn.

This quick start combines use of the Camel LinkedIn component to search companies, and write them to a simple text file.

In studying this quick start you will learn:

* how to define a Camel route using the Blueprint XML syntax
* how to build and deploy an OSGi bundle in Red Hat Fuse
* how to use OSGi config admin in Red Hat Fuse
* how to use the Camel LinkedIn component

NOTE: LinkedIn has started responding to login forms using a CAPTCHA, which makes it impossible to use the username/password login approach for a standalone headless Fuse process. As an alternative the component was enhanced so that a LinkedIn access token can be generated and configured in an `accessToken` component property. Also an `expiryTime` property allows configuring the token expiry time in milliseconds since the Unix Epoch, which defaults to 60 days if not provided. The component tries to login again if the token expires, but the username/password approach is likely to result in a CAPTCHA response and a failed login. Hence the component is unable to automatically refresh tokens on its own.

The token can be generated using a web browser and a utility such as curl and following the OAuth login process for LinkedIn as documented at https://developer.linkedin.com/docs/oauth2. As the document describes, user needs to login using a browser to generate an authorization code, which in turn is used to generate an access token to be configured in the LinkedIn component. The generated token is valid for 60 days, so the process has to be repeated periodically to manually update the token and the Fuse application needs to be restarted with the refreshed token.

For more information see:

* https://access.redhat.com/documentation/en-us/red_hat_fuse/7.0/html/apache_camel_component_reference/linkedin-component for more information about the Camel Box component
* https://access.redhat.com/documentation/en-us/red_hat_fuse/ for more information about using Red Hat Fuse

System requirements
-------------------

Before building and running this quick start you need:

* Maven 3.3.1 or higher
* JDK 1.8
* Red Hat Fuse 7

Build and Deploy the Quickstart
-------------------------

1. Change your working directory to `camel-linkedin` directory.
* Run `mvn clean install` to build the quickstart.
* Start Red Hat Fuse 7 by running bin/fuse (on Linux) or bin\fuse.bat (on Windows).
* Create the following configuration file in the etc/ directory of your Red Hat Fuse installation:

  InstallDir/etc/org.jboss.fuse.quickstarts.camel.linkedin.cfg
  Edit the org.jboss.fuse.quickstarts.camel.linkedin.cfg file with a text editor and add the following contents:

  accessToken=<LinkedIn access token>
  clientId=<LinkedIn client id>
  clientSecret=<LinkedIn client secret>

* In the Red Hat Fuse console, enter the following commands:

        feature:install camel-linkedin
        bundle:install -s mvn:org.jboss.fuse.quickstarts/camel-linkedin/${project.version}

* Fuse should give you an id when the bundle is deployed

* You can check that everything is ok by issuing  the command:

        bundle:list
   your bundle should be present at the end of the list


Use the bundle
---------------------

To use the application be sure to have deployed the quickstart in Fuse as described above. 

1. As soon as the Camel route has been started, you will see a directory `work/camel-linkedin/output` in your Red Hat Fuse installation.
2. Wait a few moments and you will see the LinkedIn user's connections downloaded to your the companies.txt file.
The route will continue polling LinkedIn every 15 minutes and overwrite the output file.
3. Use `log:display` to check out the business logging.
        Poll received <n> companies for search term 'Red Hat'
        Writing companies to companies.txt
        Done downloading companies for search term 'Red Hat'

Undeploy the Archive
--------------------

To stop and undeploy the bundle in Fuse:

1. Enter `bundle:list` command to retrieve your bundle id
2. To stop and uninstall the bundle enter

        bundle:uninstall <id>
