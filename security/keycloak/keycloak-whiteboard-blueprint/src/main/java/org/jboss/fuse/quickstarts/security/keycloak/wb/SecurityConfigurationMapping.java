/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.fuse.quickstarts.security.keycloak.wb;

import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultSecurityConfigurationMapping;
import org.ops4j.pax.web.extender.whiteboard.runtime.DefaultSecurityConstraintMapping;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

public class SecurityConfigurationMapping extends DefaultSecurityConfigurationMapping {

    public SecurityConfigurationMapping() {
        setContextSelectFilter(String.format("(%s=%s)", HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME, "app4"));
        setAuthMethod("KEYCLOAK");
        getSecurityRoles().add("admin");

        DefaultSecurityConstraintMapping adminConstraint = new DefaultSecurityConstraintMapping();
        adminConstraint.setName("admin resources");
        adminConstraint.getAuthRoles().add("admin");
        DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping forAdmin = new DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping();
        forAdmin.setName("admin resources");
        forAdmin.getUrlPatterns().add("/info/*");
        adminConstraint.getWebResourceCollections().add(forAdmin);
        getSecurityConstraints().add(adminConstraint);

        // the servlet invoking javax.servlet.http.HttpServletRequest.logout() must be under some constraint,
        // otherwise Keycloak won't notice it
        DefaultSecurityConstraintMapping logoutConstraint = new DefaultSecurityConstraintMapping();
        logoutConstraint.setName("logout-area");
        logoutConstraint.getAuthRoles().add("*");
        DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping forLogout = new DefaultSecurityConstraintMapping.DefaultWebResourceCollectionMapping();
        forLogout.setName("logout");
        forLogout.getUrlPatterns().add("/logout");
        logoutConstraint.getWebResourceCollections().add(forLogout);
        getSecurityConstraints().add(logoutConstraint);
    }

}
