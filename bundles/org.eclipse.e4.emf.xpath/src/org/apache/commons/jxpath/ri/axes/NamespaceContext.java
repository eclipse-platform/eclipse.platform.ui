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
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

/**
 * EvalContext that walks the "namespace::" axis.
 */
public class NamespaceContext extends EvalContext {
    private final NodeTest nodeTest;
    private boolean setStarted = false;
    private NodeIterator iterator;
    private NodePointer currentNodePointer;

    /**
     * @param parentContext represents the previous step on the path
     * @param nodeTest is the name of the namespace we are looking for
     */
    public NamespaceContext(final EvalContext parentContext, final NodeTest nodeTest) {
        super(parentContext);
        this.nodeTest = nodeTest;
    }

    @Override
    public NodePointer getCurrentNodePointer() {
        return currentNodePointer;
    }

    @Override
    public void reset() {
        setStarted = false;
        iterator = null;
        super.reset();
    }

    @Override
    public boolean setPosition(final int position) {
        if (position < getCurrentPosition()) {
            reset();
        }

        while (getCurrentPosition() < position) {
            if (!nextNode()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean nextNode() {
        super.setPosition(getCurrentPosition() + 1);
        if (!setStarted) {
            setStarted = true;
            if (!(nodeTest instanceof NodeNameTest)) {
                return false;
            }

            final NodeNameTest nodeNameTest = (NodeNameTest) nodeTest;
            final QName testName = nodeNameTest.getNodeName();
            if (testName.getPrefix() != null) {
                return false;
            }
            if (nodeNameTest.isWildcard()) {
                iterator =
                    parentContext.getCurrentNodePointer().namespaceIterator();
            }
            else {
                currentNodePointer =
                    parentContext.getCurrentNodePointer().namespacePointer(
                            testName.getName());
                return currentNodePointer != null;
            }
        }

        if (iterator == null) {
            return false;
        }
        if (!iterator.setPosition(iterator.getPosition() + 1)) {
            return false;
        }
        currentNodePointer = iterator.getNodePointer();
        return true;
    }
}
