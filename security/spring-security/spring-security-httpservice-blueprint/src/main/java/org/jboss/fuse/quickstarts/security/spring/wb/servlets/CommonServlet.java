/**
 *  Copyright 2005-2015 Red Hat, Inc.
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
package org.jboss.fuse.quickstarts.security.spring.wb.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommonServlet extends HttpServlet {

    public static org.slf4j.Logger LOG = LoggerFactory.getLogger(CommonServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getRequestURI().endsWith("/login")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"))) {
            StringWriter sw = new StringWriter();
            byte[] buf = new byte[4096];
            int read;
            try (InputStream is = getClass().getResourceAsStream("/login.html")) {
                while ((read = is.read(buf, 0, 4096)) > 0) {
                    sw.write(new String(buf, 0, read));
                }
            }

            String page = sw.toString();
            if (req.getParameter("error") != null) {
                page = page.replace("${error}", "Bad credentials");
            } else {
                page = page.replace("${error}", "");
            }

            resp.getWriter().println(page);
        } else if (auth.isAuthenticated()) {
            StringWriter sw = new StringWriter();
            byte[] buf = new byte[4096];
            int read;
            String pageLocation = "/page.html";
            if (req.getRequestURI().endsWith("/error")) {
                pageLocation = "/error.html";
            }
            try (InputStream is = getClass().getResourceAsStream(pageLocation)) {
                while ((read = is.read(buf, 0, 4096)) > 0) {
                    sw.write(new String(buf, 0, read));
                }
            }

            String page = sw.toString();
            page = page.replace("${user}", auth.getName());
            page = page.replace("${class}", auth.getPrincipal().getClass().getName());
            page = page.replace("${location}", req.getRequestURI());

            resp.getWriter().println(page);
        }
        resp.getWriter().flush();
        resp.getWriter().close();
    }

}
