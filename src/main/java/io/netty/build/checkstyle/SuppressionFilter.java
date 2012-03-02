package io.netty.build.checkstyle;

import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.api.FilterSet;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocPackageCheck;

public class SuppressionFilter extends AutomaticBean implements Filter {

    private static final Pattern JAVA5PATTERN = Pattern.compile("/org/jboss/");
    
    private Pattern pattern;
    private Pattern examplePattern = Pattern.compile("examples?");
    
    public void setPattern(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }
    
    public void setExamplePattern(String pattern) {
        this.examplePattern = Pattern.compile(pattern);
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
        }
        return true;
    }
}
