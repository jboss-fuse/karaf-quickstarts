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
package org.jboss.fuse.quickstarts.security.camel;

import java.security.SecureRandom;
import java.util.Random;
import javax.security.auth.Subject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class RandomSubjectProcessor implements Processor {

    private static final Random RND = new SecureRandom();

    @Override
    public void process(Exchange exchange) {
        String[][] credentials = new String[][] {
                new String[] { "admin", "admin" },
                new String[] { "user", "user" },
                new String[] { "other", "other" }
        };

        // see https://camel.apache.org/components/next/others/spring-security.html

        int idx = RND.nextInt(3);
        // create an Authentication object
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(credentials[idx][0], credentials[idx][1]);

        // wrap it in a Subject
        Subject subject = new Subject();
        subject.getPrincipals().add(token);

        // place the Subject in the In message
        exchange.getIn().setHeader(Exchange.AUTHENTICATION, subject);
        exchange.getIn().setBody("A message from \"" + credentials[idx][0] + "\"");
    }

}
