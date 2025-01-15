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

/**
 * Implementation of {@link Expression} for the operation "!=".
 */
public class CoreOperationNotEqual extends CoreOperationCompare {

    /**
     * Create a new CoreOperationNotEqual.
     * @param arg1 left operand
     * @param arg2 right operand
     */
    public CoreOperationNotEqual(final Expression arg1, final Expression arg2) {
        super(arg1, arg2, true);
    }

    @Override
    public String getSymbol() {
        return "!=";
    }
}
