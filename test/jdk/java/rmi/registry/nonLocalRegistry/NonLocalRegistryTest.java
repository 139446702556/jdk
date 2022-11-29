/*
 * Copyright (c) 2017, 2022 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
import jdk.test.lib.Utils;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Set;

/* @test
 * @bug 8174770
 * @summary Verify that Registry rejects non-local access for bind, unbind, rebind.
 *    The test is manual because the (non-local) host running rmiregistry must be supplied as a property.
 * @library /test/lib
 * @run main/manual NonLocalRegistryTest
 */

/**
 * Verify that access checks for Registry.bind(), .rebind(), and .unbind()
 * are prevented on remote access to the registry.
 *
 * This test is a manual test and uses a standard rmiregistry running
 * on a *different* host.
 * The test verifies that the access check is performed *before* the object to be
 * bound or rebound is deserialized.
 *
 * Login or ssh to the different host and invoke {@code $JDK_HOME/bin/rmiregistry}.
 * It will not show any output.
 *
 * On the first host, run the test and provide the hostname or IP address of the
 * different host when prompted in the console, or run jtreg command with system
 * property -Dregistry.host set to the hostname or IP address of the different host.
 */
public class NonLocalRegistryTest extends NonLocalRegistryBase {

    public static void main(String[] args) throws Exception {
        String host = System.getProperty("registry.host");
        if (host == null || host.isEmpty()) {
            NonLocalRegistryBase test = new NonLocalRegistryTest();
            host = Utils.readHostInput(
                    "NonLocalSkeletonTest",
                    instructions,
                    message,
                    TIMEOUT_MS
            );
            if (host == null || host.isEmpty()) {
                throw new RuntimeException(
                        "supply a remote host with -Dregistry.host=hostname");
            }
        }

        // Check if running the test on a local system; it only applies to remote
        String myHostName = InetAddress.getLocalHost().getHostName();
        // Eliminate duplicate IP address and save result into an unmodifiable set.
        Set<InetAddress> myAddrs =
                Set.copyOf(Arrays.asList(InetAddress.getAllByName(myHostName)));
        Set<InetAddress> hostAddrs =
                Set.copyOf(Arrays.asList(InetAddress.getAllByName(host)));
        if (hostAddrs.stream().anyMatch(i -> myAddrs.contains(i))
                || hostAddrs.stream().anyMatch(h -> h.isLoopbackAddress())) {
            throw new RuntimeException("Error: property 'registry.host' must not be the local host%n");
        }

        Registry registry = LocateRegistry.getRegistry(host, Registry.REGISTRY_PORT);

        try {
            registry.bind("foo", null);
            throw new RuntimeException("Remote access should not succeed for method: bind");
        } catch (Exception e) {
            assertIsAccessException(e);
        }

        try {
            registry.rebind("foo", null);
            throw new RuntimeException("Remote access should not succeed for method: rebind");
        } catch (Exception e) {
            assertIsAccessException(e);
        }

        try {
            registry.unbind("foo");
            throw new RuntimeException("Remote access should not succeed for method: unbind");
        } catch (Exception e) {
            assertIsAccessException(e);
        }
    }
}
