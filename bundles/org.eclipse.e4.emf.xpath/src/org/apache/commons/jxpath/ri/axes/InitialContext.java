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

import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A single-set EvalContext that provides access to the current node of
 * the parent context and nothing else.  It does not pass the iteration
 * on to the parent context.
 */
public class InitialContext extends EvalContext {
    private boolean started = false;
    private boolean collection;
    private final NodePointer nodePointer;

    /**
     * Create a new InitialContext.
     * @param parentContext parent context
     */
    public InitialContext(final EvalContext parentContext) {
        super(parentContext);
        nodePointer =
            (NodePointer) parentContext.getCurrentNodePointer().clone();
        if (nodePointer != null) {
            collection =
                nodePointer.getIndex() == NodePointer.WHOLE_COLLECTION;
        }
    }

    @Override
    public Pointer getSingleNodePointer() {
        return nodePointer;
    }

    @Override
    public NodePointer getCurrentNodePointer() {
        return nodePointer;
    }

    @Override
    public Object getValue() {
        return nodePointer.getValue();
    }

    @Override
    public boolean nextNode() {
        return setPosition(position + 1);
    }

    @Override
    public boolean setPosition(final int position) {
        this.position = position;
        if (collection) {
            if (position >= 1 && position <= nodePointer.getLength()) {
                nodePointer.setIndex(position - 1);
                return true;
            }
            return false;
        }
        return position == 1;
    }

    @Override
    public boolean nextSet() {
        if (started) {
            return false;
        }
        started = true;
        return true;
    }
}
