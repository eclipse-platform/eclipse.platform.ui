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
 * Implementation of {@link Expression} for the operation "&gt;=".
 */
public class CoreOperationGreaterThanOrEqual extends
        CoreOperationRelationalExpression {

    /**
     * Create a new CoreOperationGreaterThanOrEqual.
     * @param arg1 operand 1
     * @param arg2 operand 2
     */
    public CoreOperationGreaterThanOrEqual(final Expression arg1, final Expression arg2) {
        super(new Expression[] { arg1, arg2 });
    }

    @Override
    protected boolean evaluateCompare(final int compare) {
        return compare >= 0;
    }

    @Override
    public String getSymbol() {
        return ">=";
    }
}
