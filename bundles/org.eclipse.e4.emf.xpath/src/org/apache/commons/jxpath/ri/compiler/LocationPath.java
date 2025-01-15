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

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.axes.InitialContext;

/**
 */
public class LocationPath extends Path {

    private final boolean absolute;

    /**
     * Create a new LocationPath.
     * @param absolute whether this is an absolute path
     * @param steps to evaluate
     */
    public LocationPath(final boolean absolute, final Step[] steps) {
        super(steps);
        this.absolute = absolute;
    }

    /**
     * Learn whether this LocationPath is absolute.
     * @return boolean
     */
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public boolean computeContextDependent() {
        return !absolute || super.computeContextDependent();
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        final Step[] steps = getSteps();
        if (steps != null) {
            for (int i = 0; i < steps.length; i++) {
                if (i > 0 || absolute) {
                    buffer.append('/');
                }
                buffer.append(steps[i]);
            }
        }
        return buffer.toString();
    }

    @Override
    public Object compute(final EvalContext context) {
        // Create a chain of contexts
        EvalContext rootContext;
        if (isAbsolute()) {
            rootContext = context.getRootContext().getAbsoluteRootContext();
        }
        else {
            rootContext = new InitialContext(context);
        }
        return evalSteps(rootContext);
    }

    @Override
    public Object computeValue(final EvalContext context) {
        // Create a chain of contexts
        EvalContext rootContext;
        if (isAbsolute()) {
            rootContext = context.getRootContext().getAbsoluteRootContext();
        }
        else {
            rootContext = new InitialContext(context);
        }
        return getSingleNodePointerForSteps(rootContext);
    }
}
