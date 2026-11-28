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
package org.apache.jackrabbit.core.version;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.name.NameConstants;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jahia switch that turns off the JCR versioning of the repository.
 * <p>
 * Once disabled, the repository creates no version history, treats every node as checked out, and
 * refuses every call of the version API. A save removes the versioning properties of each node it
 * writes. The switch goes one way: nothing turns the versioning on again in the same JVM.
 */
public final class LegacyVersioning {

    private static final Logger log = LoggerFactory.getLogger(LegacyVersioning.class);

    /**
     * The properties that the versioning writes on a versionable node.
     */
    public static final Set<Name> NODE_PROPERTIES = Collections.unmodifiableSet(new HashSet<Name>(Arrays.asList(
            NameConstants.JCR_VERSIONHISTORY,
            NameConstants.JCR_BASEVERSION,
            NameConstants.JCR_PREDECESSORS,
            NameConstants.JCR_ISCHECKEDOUT,
            NameConstants.JCR_MERGEFAILED,
            NameConstants.JCR_ACTIVITY,
            NameConstants.JCR_CONFIGURATION)));

    /**
     * The property on the root of the version storage that records the switch. The repository
     * reads it at startup, so the switch survives a restart.
     */
    public static final Name MARKER = NameFactoryImpl.getInstance().create(Name.NS_REP_URI, "legacyVersioningDisabled");

    private static volatile boolean disabled;

    private LegacyVersioning() {
    }

    /**
     * Turns the versioning off for the life of the JVM.
     */
    public static synchronized void disable() {
        if (!disabled) {
            disabled = true;
            log.info("JCR versioning is disabled");
        }
    }

    public static boolean isDisabled() {
        return disabled;
    }

    /**
     * Throws when the versioning is disabled.
     *
     * @param operation the name of the refused operation, for the message
     * @throws UnsupportedRepositoryOperationException when the versioning is disabled
     */
    public static void check(String operation) throws UnsupportedRepositoryOperationException {
        if (disabled) {
            throw new UnsupportedRepositoryOperationException(
                    "JCR versioning is disabled: " + operation + " is not supported");
        }
    }
}
