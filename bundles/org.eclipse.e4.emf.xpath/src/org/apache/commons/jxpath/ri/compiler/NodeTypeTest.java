/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.compiler;

import org.apache.commons.jxpath.ri.Compiler;

/**
 */
public class NodeTypeTest extends NodeTest {
    private final int nodeType;

    /**
     * Create a new NodeTypeTest.
     * @param nodeType to match
     */
    public NodeTypeTest(final int nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Gets the nodeType.
     * @return int
     */
    public int getNodeType() {
        return nodeType;
    }

    @Override
    public String toString() {
        return nodeTypeToString(nodeType) + "()";
    }

    /**
     * Render the given node type as a String.
     * @param code int
     * @return String
     */
    public static String nodeTypeToString(final int code) {
        switch (code) {
            case Compiler.NODE_TYPE_NODE :
                return "node";
            case Compiler.NODE_TYPE_TEXT :
                return "text";
            case Compiler.NODE_TYPE_COMMENT :
                return "comment";
            case Compiler.NODE_TYPE_PI :
                return "processing-instruction";
            default:
                return "UNKNOWN";
        }
    }
}
