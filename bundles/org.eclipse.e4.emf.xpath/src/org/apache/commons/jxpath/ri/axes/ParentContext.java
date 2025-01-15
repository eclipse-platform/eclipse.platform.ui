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
package org.apache.commons.jxpath.ri.axes;

import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * EvalContext that walks the "parent::" axis.
 */
public class ParentContext extends EvalContext {
    private final NodeTest nodeTest;
    private boolean setStarted = false;
    private NodePointer currentNodePointer;

    /**
     * Create a new ParentContext.
     * @param parentContext parent context
     * @param nodeTest test
     */
    public ParentContext(final EvalContext parentContext, final NodeTest nodeTest) {
        super(parentContext);
        this.nodeTest = nodeTest;
    }

    @Override
    public NodePointer getCurrentNodePointer() {
        return currentNodePointer;
    }

    @Override
    public int getCurrentPosition() {
        return 1;
    }

    @Override
    public int getDocumentOrder() {
        return -1;
    }

    @Override
    public void reset() {
        super.reset();
        setStarted = false;
    }

    @Override
    public boolean setPosition(final int position) {
        super.setPosition(position);
        return position == 1;
    }

    @Override
    public boolean nextNode() {
        // Each set contains exactly one node: the parent
        if (setStarted) {
            return false;
        }
        setStarted = true;
        final NodePointer thisLocation = parentContext.getCurrentNodePointer();
        currentNodePointer = thisLocation.getImmediateParentPointer();
        while (currentNodePointer != null
            && currentNodePointer.isContainer()) {
            currentNodePointer = currentNodePointer.getImmediateParentPointer();
        }
        if (currentNodePointer != null
            && currentNodePointer.testNode(nodeTest)) {
            position++;
            return true;
        }
        return false;
    }
}
