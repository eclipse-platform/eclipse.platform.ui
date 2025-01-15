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
 * Extension function interface. Extension functions are grouped into
 * {@link Functions Functions} objects, which are installed on
 * JXPathContexts using the
 * {@link JXPathContext#setFunctions JXPathContext.setFunctions()}
 * call.
 * <p>
 * The Function interface can be implemented directly. However,
 * most of the time JXPath's built-in implementations should suffice.
 * See {@link ClassFunctions ClassFunctions} and
 * {@link PackageFunctions PackageFunctions}.
 */
public interface Function {

    /**
     * Computes the value of the function. Each implementation of Function
     * is responsible for conversion of supplied parameters to the required
     * argument types.
     *
     * @param context can be used to acquire the context in which the
     *    function is being evaluted.
     * @param parameters function arguments
     * @return Object result
     */
    Object invoke(ExpressionContext context, Object[] parameters);
}
