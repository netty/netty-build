/*
 * Copyright 2013 The Netty Project
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

import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class NewlineCheck extends AbstractFileSetCheck {

    private enum NewlineType {
        UNKNOWN,
        CR,
        LF,
        CRLF,
        LFCR,
        RS
    }

    private static final Pattern NON_CRLF = Pattern.compile("(?:[^\r]\n|\r[^\n])");
    private static final Pattern NON_LFCR = Pattern.compile("(?:[^\n]\r|\n[^\r])");

    private static final NewlineType NEWLINE_TYPE;

    static {
        final String newline = System.lineSeparator();
        if ("\n".equals(newline)) {
            NEWLINE_TYPE = NewlineType.LF;
        } else if ("\r".equals(newline)) {
            NEWLINE_TYPE = NewlineType.CR;
        } else if ("\r\n".equals(newline)) {
            NEWLINE_TYPE = NewlineType.CRLF;
        } else if ("\n\r".equals(newline)) {
            NEWLINE_TYPE = NewlineType.LFCR;
        } else if ("\u001E".equals(newline)) {
            NEWLINE_TYPE = NewlineType.RS;
        } else {
            NEWLINE_TYPE = NewlineType.UNKNOWN;
        }
    }

    @Override
    protected void processFiltered(File aFile, List<String> aLines) {
        String text = FileText.fromLines(aFile, aLines).getFullText().toString();

        switch (NEWLINE_TYPE) {
        case LF:
            if (text.indexOf('\r') >= 0) {
                reportNewlineViolation();
            }
            break;
        case CR:
            if (text.indexOf('\n') >= 0) {
                reportNewlineViolation();
            }
            break;
        case CRLF:
            if (NON_CRLF.matcher(text).find()) {
                reportNewlineViolation();
            }
            break;
        case LFCR:
            if (NON_LFCR.matcher(text).find()) {
                reportNewlineViolation();
            }
            break;
        case RS:
            if (text.indexOf('\u001E') >= 0) {
                reportNewlineViolation();
            }
            break;
        }
    }

    private void reportNewlineViolation() {
        log(0, "invalid newline character (expected: " + NEWLINE_TYPE + ')');
    }
}
