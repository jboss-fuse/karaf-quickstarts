/*
 *  Copyright 2005-2018 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.jboss.fuse.quickstarts.security.keycloak.wb;

import java.util.Hashtable;
import javax.servlet.Servlet;

import org.jboss.fuse.quickstarts.security.keycloak.wb.servlets.InfoServlet;
import org.jboss.fuse.quickstarts.security.keycloak.wb.servlets.LogoutServlet;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultSecurityConfigurationMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultSecurityConstraintMapping;
import org.ops4j.pax.web.service.whiteboard.HttpContextMapping;
import org.ops4j.pax.web.service.whiteboard.SecurityConfigurationMapping;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

public class Activator implements BundleActivator {

    private ServiceRegistration<ServletContextHelper> contextReg;
    private ServiceRegistration<SecurityConfigurationMapping> securityReg;

    private ServiceRegistration<Servlet> infoServletRegistration;
    private ServiceRegistration<Servlet> logoutServletRegistration;
    private ServiceRegistration<HttpContextMapping> httpContextMappingRegistration;

    @Override
    public void start(BundleContext context) {

        // register /app3 servlet context

        Hashtable<String, Object> props = new Hashtable<>();
        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, "app3");
        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH, "/app3");
        // equivalent of web.xml's /web-app/context-param to configure Keycloak config resolver
        // <context-param>
        //     <param-name>keycloak.config.resolver</param-name>
        //     <param-value>org.keycloak.adapters.osgi.PathBasedKeycloakConfigResolver</param-value>
        // </context-param>
        props.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_INIT_PARAM_PREFIX + "keycloak.config.resolver",
                "org.keycloak.adapters.osgi.PathBasedKeycloakConfigResolver");
        contextReg = context.registerService(ServletContextHelper.class, new ServletContextHelper(context.getBundle()) {
        }, props);

        // register (new in Pax Web 8) security configuration, equivalent of:
        // <login-config>
        //     <auth-method>KEYCLOAK</auth-method>
        // </login-config>
        // <security-constraint>
        //     <web-resource-collection>
        //         <web-resource-name>admin resources</web-resource-name>
        //         <url-pattern>/info/*</url-pattern>
        //     </web-resource-collection>
        //     <web-resource-collection>
        //         <web-resource-name>logout-area</web-resource-name>
        //         <url-pattern>/logout</url-pattern>
        //     </web-resource-collection>
        //     <auth-constraint>
        //         <role-name>admin</role-name>
        //     </auth-constraint>
        // </security-constraint>

        DefaultSecurityConfigurationMapping security = new DefaultSecurityConfigurationMapping();
        // target both contexts
        security.setContextSelectFilter(String.format("(%s=%s)", HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, "app3"));
        security.setAuthMethod("KEYCLOAK");
        security.getSecurityRoles().add("admin");

        DefaultSecurityConstraintMapping adminConstraint = new DefaultSecurityConstraintMapping();
        adminConstraint.setName("admin resources");
        adminConstraint.getAuthRoles().add("admin");
        DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping forAdmin = new DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping();
        forAdmin.setName("admin resources");
        forAdmin.getUrlPatterns().add("/info/*");
        adminConstraint.getWebResourceCollections().add(forAdmin);
        security.getSecurityConstraints().add(adminConstraint);

        // the servlet invoking javax.servlet.http.HttpServletRequest.logout() must be under some constraint,
        // otherwise Keycloak won't notice it
        DefaultSecurityConstraintMapping logoutConstraint = new DefaultSecurityConstraintMapping();
        logoutConstraint.setName("logout-area");
        logoutConstraint.getAuthRoles().add("*");
        DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping forLogout = new DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping();
        forLogout.setName("logout");
        forLogout.getUrlPatterns().add("/logout");
        logoutConstraint.getWebResourceCollections().add(forLogout);
        security.getSecurityConstraints().add(logoutConstraint);

        securityReg = context.registerService(SecurityConfigurationMapping.class, security, null);

        // now, instead of registering servlets via org.osgi.service.http.HttpService.registerServlet()
        // we'll just register them as OSGi services - they'll be processed by pax-web-extender-whiteboard
        // it's important to set "osgi.http.whiteboard.context.select" property, so servlets are registered in the same context
        // as login configuration and constraints
        Hashtable<String, Object> infoProperties = new Hashtable<>();
        infoProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, "(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=app3)");
        infoProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, "info-servlet");
        infoProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, new String[] { "/info/*" });
        infoServletRegistration = context.registerService(Servlet.class, new InfoServlet(), infoProperties);

        Hashtable<String, Object> logoutProperties = new Hashtable<>();
        logoutProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_SELECT, "(" + HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=app3)");
        logoutProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_NAME, "logout-servlet");
        logoutProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, new String[] { "/logout" });
        logoutServletRegistration = context.registerService(Servlet.class, new LogoutServlet(), logoutProperties);
    }

    @Override
    public void stop(BundleContext context) {
        if (logoutServletRegistration != null) {
            logoutServletRegistration.unregister();
        }
        if (infoServletRegistration != null) {
            infoServletRegistration.unregister();
        }
        if (httpContextMappingRegistration != null) {
            httpContextMappingRegistration.unregister();
        }
        if (securityReg != null) {
            securityReg.unregister();
        }
        if (contextReg != null) {
            contextReg.unregister();
        }
    }

}
