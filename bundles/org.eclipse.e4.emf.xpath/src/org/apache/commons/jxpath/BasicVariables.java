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

import java.util.HashMap;

/**
 * A basic implementation of the Variables interface that uses a HashMap.
 */
public class BasicVariables implements Variables {
    private static final long serialVersionUID = 2708263960832062725L;

    /**
     * Contains the values of declared variables
     */
    private final HashMap vars = new HashMap();

    /**
     * Returns true if the variable has been defined, even if the
     * value of the variable is null.
     *
     * @param varName is a variable name without the "$" sign
     * @return true if the variable is declared
     */
    @Override
    public boolean isDeclaredVariable(final String varName) {
        return vars.containsKey(varName);
    }

    /**
     * Returns the value of the variable if it is defined,
     * otherwise, throws IllegalArgumentException
     *
     * @param varName is a variable name without the "$" sign
     * @return the value of the variable
     */
    @Override
    public Object getVariable(final String varName) {
        // Note that a variable may be defined with a null value

        if (vars.containsKey(varName)) {
            return vars.get(varName);
        }

        throw new IllegalArgumentException(
            "No such variable: '" + varName + "'");
    }

    /**
     * Defines a new variable with the specified value or modifies
     * the value of an existing variable.
     *
     * @param varName is a variable name without the "$" sign
     * @param value is the new value for the variable, which can be null
     */
    @Override
    public void declareVariable(final String varName, final Object value) {
        vars.put(varName, value);
    }

    /**
     * Removes an existing variable. May throw UnsupportedOperationException.
     *
     * @param varName is a variable name without the "$" sign
     */
    @Override
    public void undeclareVariable(final String varName) {
        vars.remove(varName);
    }

    @Override
    public String toString() {
        return vars.toString();
    }
}
