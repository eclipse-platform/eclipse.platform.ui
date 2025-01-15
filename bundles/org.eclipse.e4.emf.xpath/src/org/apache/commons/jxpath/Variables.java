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

import java.io.Serializable;

/**
 * Variables provide access to a global set of values accessible via XPath.
 * XPath can reference variables using the {@code "$varname"} syntax.
 * To use a custom implementation of this interface, pass it to
 * {@link JXPathContext#setVariables JXPathContext.setVariables()}
 */
public interface Variables extends Serializable {

    /**
     * Returns true if the specified variable is declared.
     * @param varName variable name
     * @return boolean
     */
    boolean isDeclaredVariable(String varName);

    /**
     * Returns the value of the specified variable.
     * @param varName variable name
     * @return Object value
     * @throws IllegalArgumentException if there is no such variable.
     */
    Object getVariable(String varName);

    /**
     * Defines a new variable with the specified value or modifies
     * the value of an existing variable.
     * May throw UnsupportedOperationException.
     * @param varName variable name
     * @param value to declare
     */
    void declareVariable(String varName, Object value);

    /**
     * Removes an existing variable. May throw UnsupportedOperationException.
     *
     * @param varName is a variable name without the "$" sign
     */
    void undeclareVariable(String varName);
}
