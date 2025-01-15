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

/**
 * A delegate of {@link JXPathContext} that implements the XPath {@code "id()"}
 * function. This delegate is only used when there is no default implementation
 * of the {@code id()} function. For example, it is <em>not</em> used
 * when the root of the context is a DOM Node.
 */
public interface IdentityManager {

    /**
     * Finds a node by its ID.
     * @param context JXPathContext
     * @param id String
     * @return Pointer
     */
    Pointer getPointerByID(JXPathContext context, String id);
}
