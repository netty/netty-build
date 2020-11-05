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

import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class GlibcVersionCheckMojoTest {

    private static final String OBJDUMP_OUTPUT = "\n" +
            "/tmp/libnetty_transport_native_epoll_x86_64.so:     file format elf64-x86-64\n" +
            "\n" +
            "DYNAMIC SYMBOL TABLE:\n" +
            "0000000000003a60 l    d  .init\t0000000000000000              .init\n" +
            "0000000000000000  w   DF *UND*\t0000000000000000  GLIBC_2.9   pipe2\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 memset\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 snprintf\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 shutdown\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 close\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.3.2 epoll_create\n" +
            "0000000000000000  w   D  *UND*\t0000000000000000              __gmon_start__\n" +
            "0000000000000000  w   D  *UND*\t0000000000000000              _Jv_RegisterClasses\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 recvmsg\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 uname\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.5   splice\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 getpeername\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 read\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strncmp\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 malloc\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 fopen\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 unlink\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 setsockopt\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.7   eventfd\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 fgets\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 free\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strlen\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 listen\n" +
            "0000000000000000  w   DF *UND*\t0000000000000000  GLIBC_2.2.5 __cxa_finalize\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 syscall\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 pipe\n" +
            "0000000000000000  w   DF *UND*\t0000000000000000  GLIBC_2.9   epoll_create1\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 sendmsg\n" +
            "0000000000000000      DO *UND*\t0000000000000000  GLIBC_2.2.5 in6addr_any\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strerror\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.3.2 epoll_ctl\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strstr\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strcat\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 getsockopt\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strtol\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 getsockname\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 connect\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 memcpy\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.7   eventfd_read\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 socket\n" +
            "0000000000000000      D  *UND*\t0000000000000000              dladdr\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 sendfile\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 __errno_location\n" +
            "0000000000000000  w   DF *UND*\t0000000000000000  GLIBC_2.10  accept4\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strcpy\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.3.2 epoll_wait\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.7   eventfd_write\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 calloc\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.3.4 __xpg_strerror_r\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 writev\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 fclose\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 recvfrom\n" +
            "0000000000000000      DO *UND*\t0000000000000000  GLIBC_2.2.5 stderr\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 sendto\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 bind\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 fwrite\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.8   timerfd_create\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 fprintf\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.8   timerfd_settime\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 write\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 accept\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 clock_gettime\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 fcntl\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 open\n" +
            "0000000000000000      DF *UND*\t0000000000000000  GLIBC_2.2.5 strndup\n" +
            "000000000020f950 g    D  *ABS*\t0000000000000000  Base        _end\n" +
            "000000000020f864 g    D  *ABS*\t0000000000000000  Base        _edata\n" +
            "0000000000006fa0 g    DF .text\t0000000000000099  Base        JNI_OnUnload_netty_transport_native_epoll\n" +
            "0000000000007040 g    DF .text\t0000000000000099  Base        JNI_OnUnload\n" +
            "000000000020f864 g    D  *ABS*\t0000000000000000  Base        __bss_start\n" +
            "0000000000003a60 g    DF .init\t0000000000000000  Base        _init\n" +
            "000000000000b0c8 g    DF .fini\t0000000000000000  Base        _fini\n" +
            "0000000000006430 g    DF .text\t000000000000000a  Base        JNI_OnLoad\n" +
            "0000000000006440 g    DF .text\t000000000000000a  Base        JNI_OnLoad_netty_transport_native_epoll";

    @Test(expected = MojoFailureException.class)
    public void testMaxVersionLower() throws MojoFailureException {
        GlibcVersionCheckMojo.check(2, 8, 1, OBJDUMP_OUTPUT);
    }

    @Test
    public void testMaxVersionMatch() throws MojoFailureException {
        GlibcVersionCheckMojo.check(2,10, 0, OBJDUMP_OUTPUT);
    }

    @Test
    public void testMaxVersionHigher() throws MojoFailureException {
        GlibcVersionCheckMojo.check(2,10, 1, OBJDUMP_OUTPUT);
    }
}
