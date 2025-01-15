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
 */
public class ProcessingInstructionTest extends NodeTest {
    private final String target;

    /**
     * Create a new ProcessingInstructionTest.
     * @param target string
     */
    public ProcessingInstructionTest(final String target) {
        this.target = target;
    }

    /**
     * Gets the target.
     * @return String
     */
    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "processing-instruction('" + target + "')";
    }
}
