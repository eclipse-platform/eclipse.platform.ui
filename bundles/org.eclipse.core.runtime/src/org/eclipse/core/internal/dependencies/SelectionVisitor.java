/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.dependencies;

import java.util.Collection;
import java.util.Collections;

/**
 * Clients may override to provide alternative policies. 
 */
public class SelectionVisitor implements IElementSetVisitor {
	private int order;
	private ISelectionPolicy selectionPolicy;
	public SelectionVisitor(int order, ISelectionPolicy selectionPolicy) {
		this.order = order;
		this.selectionPolicy = selectionPolicy;
	}
	public int getOrder() {
		return order;
	}
	public final Collection getAncestors(ElementSet elementSet) {
		return elementSet.getRequiring();
	}
	public final Collection getDescendants(ElementSet elementSet) {
		return elementSet.getRequired();
	}
	public void update(ElementSet elementSet) {

		// no versions satisfied, so no versions selected 
		if (elementSet.getSatisfied().isEmpty()) {
			elementSet.setSelected(Collections.EMPTY_SET);
			return;
		}

		// here starts the selection policy implementation

		// all versions allow concurrency - select only those which are required, or the highest
		if (elementSet.allowsConcurrency()) {
			elementSet.setSelected(this.selectionPolicy.selectMultiple(elementSet));
			return;
		}
		IElement selected = this.selectionPolicy.selectSingle(elementSet);
		elementSet.setSelected(selected == null ? Collections.EMPTY_SET : Collections.singleton(selected));
	}

}
