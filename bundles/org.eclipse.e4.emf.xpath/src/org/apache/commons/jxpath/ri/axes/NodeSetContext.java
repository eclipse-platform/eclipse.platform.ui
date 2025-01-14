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

import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * A simple context that is based on a {@link NodeSet}.
 */
public class NodeSetContext extends EvalContext {
    private boolean startedSet = false;
    private final NodeSet nodeSet;

    /**
     * Create a new NodeSetContext.
     * @param parentContext parent context
     * @param nodeSet associated NodeSet
     */
    public NodeSetContext(final EvalContext parentContext, final NodeSet nodeSet) {
        super(parentContext);
        this.nodeSet = nodeSet;
    }

    @Override
    public NodeSet getNodeSet() {
        return nodeSet;
    }

    @Override
    public NodePointer getCurrentNodePointer() {
        if (position == 0 && !setPosition(1)) {
            return null;
        }
        return (NodePointer) nodeSet.getPointers().get(position - 1);
    }

    @Override
    public boolean setPosition(final int position) {
        super.setPosition(position);
        return position >= 1 && position <= nodeSet.getPointers().size();
    }

    @Override
    public boolean nextSet() {
        if (startedSet) {
            return false;
        }
        startedSet = true;
        return true;
    }

    @Override
    public boolean nextNode() {
        return setPosition(position + 1);
    }
}
