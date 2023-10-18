/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode.ChildNode;

public abstract class InternalTextEditChangeNode extends AbstractChangeNode {

	protected InternalTextEditChangeNode(PreviewNode parent, Change change) {
		super(parent, change);
	}

	@Override
	int getActive() {
		return getCompositeChangeActive();
	}

	@Override
	boolean hasOneGroupCategory(List<GroupCategory> categories) {
		return ((TextEditBasedChange)getChange()).hasOneGroupCategory(categories);
	}

	protected TextEditBasedChange getTextEditBasedChange() {
		return (TextEditBasedChange)getChange();
	}

	@Override
	final PreviewNode[] doCreateChildren() {
		return createChildNodes();
	}

	protected static TextEditChangeNode internalGetTextEditChangeNode(PreviewNode node) {
		PreviewNode element= node.getParent();
		while(!(element instanceof TextEditChangeNode) && element != null) {
			element= element.getParent();
		}
		return (TextEditChangeNode)element;
	}

	protected abstract ChildNode[] createChildNodes();
}
