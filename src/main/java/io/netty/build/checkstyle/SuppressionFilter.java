/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.build.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.Filter;

import java.util.regex.Pattern;

public class SuppressionFilter extends AutomaticBean implements Filter {

    private static final Pattern JAVA5PATTERN = Pattern.compile("/org/jboss/");

    private Pattern pattern;
    private Pattern examplePattern = Pattern.compile("examples?");

    public void setPattern(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public void setExamplePattern(String pattern) {
        examplePattern = Pattern.compile(pattern);
    }

    @Override
    public boolean accept(AuditEvent evt) {
        String filename = evt.getFileName().replace('\\', '/');
        if (JAVA5PATTERN.matcher(filename).find()) {
            if (evt.getSourceName().endsWith("MissingOverrideCheck")) {
                return false;
            }
        }

        if (pattern.matcher(filename).find()) {
            return false;
        }
        if (examplePattern.matcher(filename).find()) {
            if (evt.getSourceName().endsWith(".JavadocPackageCheck")) {
                return false;
            }
            if (evt.getSourceName().endsWith(".HideUtilityClassConstructorCheck")) {
                return false;
            }
        }
        return true;
    }
}
