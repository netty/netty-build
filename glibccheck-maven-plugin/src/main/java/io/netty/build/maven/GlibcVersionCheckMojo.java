/*
 * Copyright 2020 The Netty Project
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
package io.netty.build.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verify that a native library is usable on a specific GLIBC version.
 */
@Mojo(name= "versioncheck", defaultPhase = LifecyclePhase.VERIFY)
public final class GlibcVersionCheckMojo extends AbstractMojo {
    // Pattern / regex to extract the GLIBC version dependencies.
    private static final Pattern GLIBC_PATTERN = Pattern.compile(".+ GLIBC_([0-9]).([0-9]+)(.([0-9]+))? (.+)");

    @Parameter( property = "versioncheck.maxGlibcVersion", required = true)
    private String maxGlibcVersion;
    @Parameter( property = "versioncheck.objdump")
    private String objdump;
    @Parameter( property = "versioncheck.nativeLib", required = true)
    private File nativeLib;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (objdump == null) {
            // Try to detect the objdump installation.
            String osname = System.getProperty("os.name", "").toLowerCase(Locale.US)
                    .replaceAll("[^a-z0-9]+", "");
            boolean osx = osname.startsWith("macosx") || osname.startsWith("osx");
            if (osx) {
                objdump = checkObjdumpExists("/usr/local/opt/binutils/bin/gobjdump", "brew install binutils");
            } else {
                objdump = checkObjdumpExists("/usr/bin/objdump", "apt-get|yum install binutils");
            }
        }
        String[] versionParts = maxGlibcVersion.split("\\.");
        if (versionParts.length < 1 || versionParts.length > 3) {
            throw new MojoExecutionException("Unable to parse maxGlibcVersion: " + maxGlibcVersion);
        }

        // Parse the major, minor and bugfix versions
        final int majorVersion;
        final int minorVersion;
        final int bugFixVersion;
        try {
            majorVersion = Integer.parseInt(versionParts[0]);
            if (versionParts.length == 1) {
                minorVersion = 0;
                bugFixVersion = 0;
            } else {
                minorVersion = Integer.parseInt(versionParts[1]);
                if (versionParts.length == 2) {
                    bugFixVersion = 0;
                } else {
                    bugFixVersion = Integer.parseInt(versionParts[2]);
                }
            }
        } catch (NumberFormatException e) {
            throw new MojoExecutionException("Unable to parse maxGlibcVersion: " + maxGlibcVersion, e);
        }

        if ( !nativeLib.isFile() ) {
            throw new MojoExecutionException(nativeLib + " is not a file");
        }
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            String cmd = objdump + " -T " + nativeLib;
            Process process = Runtime.getRuntime()
                    .exec(cmd);
            int rcode = process.waitFor();
            if (rcode != 0) {
                throw new MojoExecutionException(cmd + " exit with return code " + rcode);
            }
            in = process.getInputStream();

            byte[] bytes = new byte[8192];
            int i;
            while ((i = in.read(bytes)) != -1) {
                out.write(bytes, 0, i);
            }
            out.flush();
            check(majorVersion, minorVersion, bugFixVersion, new String(out.toByteArray(), StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Interrupted while waiting for objdump to complete", e);
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to execute objdump", e);
        } finally {
            closeSilently(in);
            closeSilently(out);
        }
    }

    private static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }

    // Package-private for easy testing
    static void check(int major, int minor, int bugfix, String in) throws MojoFailureException {
        StringTokenizer tokenizer = new StringTokenizer(in, "\n");
        while (tokenizer.hasMoreElements()) {
            String line = tokenizer.nextToken();
            Matcher matcher = GLIBC_PATTERN.matcher(line);
            if (matcher.matches()) {
                int foundMajor = Integer.parseInt(matcher.group(1));
                int foundMinor = Integer.parseInt(matcher.group(2));
                int foundBugfix = matcher.group(4) == null ? 0 : Integer.parseInt(matcher.group(4));
                String function = matcher.group(5).trim();
                if (foundMajor > major) {
                    failure(major, minor, bugfix, foundMajor, foundMinor, foundBugfix, function);
                } else if (foundMajor == major) {
                    if (foundMinor > minor) {
                        failure(major, minor, bugfix, foundMajor, foundMinor, foundBugfix, function);
                    } else if (foundMinor == minor) {
                        if (foundBugfix > bugfix) {
                            failure(major, minor, bugfix, foundMajor, foundMinor, foundBugfix, function);
                        }
                    }
                }
            }
        }
    }

    private static String checkObjdumpExists(String objdump, String installInstructions) throws MojoExecutionException {
        File f = new File(objdump);
        if (!f.exists() || !f.canExecute()) {
            throw new MojoExecutionException("Unable to execute '" + objdump + "'." +
                    " May need to install it via '" + installInstructions + "'.");
        }
        return objdump;
    }

    private static void failure(int major, int minor, int bugfix, int foundMajor,
                                int foundMinor, int foundBugfix, String function) throws MojoFailureException{
        throw new MojoFailureException("Required GLIBC " +
                glibcVersion(foundMajor, foundMinor, foundBugfix) + " > " +
                glibcVersion(major, minor, bugfix) + ". Required by '" + function + "'.");
    }

    private static String glibcVersion(int major, int minor, int bugfix) {
        return major + "." + minor + "." + bugfix;
    }
}
