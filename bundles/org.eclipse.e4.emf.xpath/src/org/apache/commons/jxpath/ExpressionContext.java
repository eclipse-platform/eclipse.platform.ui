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
package org.apache.commons.jxpath;

import java.util.List;

/**
 * If an extenstion function has an argument of type ExpressionContext,
 * it can gain access to the current node of an XPath expression context.
 * <p>
 * Example:
 * <blockquote><pre>
 * public class MyExtenstionFunctions {
 *    public static String objectType(ExpressionContext context){
 *       Object value = context.getContextNodePointer().getValue();
 *       if (value == null){
 *           return "null";
 *       }
 *       return value.getClass().getName();
 *    }
 * }
 * </pre></blockquote>
 *
 * You can then register this extension function using a {@link ClassFunctions
 * ClassFunctions} object and call it like this:
 * <blockquote><pre>
 *   "/descendent-or-self::node()[ns:objectType() = 'java.util.Date']"
 * </pre></blockquote>
 * This expression will find all nodes of the graph that are dates.
 */
public interface ExpressionContext {

    /**
     * Gets the JXPathContext in which this function is being evaluated.
     *
     * @return A list representing the current context nodes.
     */
    JXPathContext getJXPathContext();

    /**
     * Gets the current context node.
     *
     * @return The current context node pointer.
     */
    Pointer getContextNodePointer();

    /**
     * Gets the current context node list.  Each element of the list is
     * a Pointer.
     *
     * @return A list representing the current context nodes.
     */
    List getContextNodeList();

    /**
     * Returns the current context position.
     * @return int
     */
    int getPosition();
}
