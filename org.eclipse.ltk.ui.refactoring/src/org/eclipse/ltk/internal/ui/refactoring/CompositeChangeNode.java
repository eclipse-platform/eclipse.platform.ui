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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

public class CompositeChangeNode extends AbstractChangeNode {

	private final RefactoringPreviewChangeFilter fFilter;

	public CompositeChangeNode(PreviewNode parent, RefactoringPreviewChangeFilter filter, CompositeChange change) {
		super(parent, change);
		fFilter= filter;
	}

	@Override
	int getActive() {
		return getCompositeChangeActive();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return RefactoringPluginImages.DESC_OBJS_COMPOSITE_CHANGE;
	}

	@Override
	PreviewNode[] doCreateChildren() {
		List<PreviewNode> children= new ArrayList<>();
		getFlattendedChildren(children, this, (CompositeChange)getChange());
		return children.toArray(new PreviewNode[children.size()]);
	}

	private void getFlattendedChildren(List<PreviewNode> result, CompositeChangeNode parent, CompositeChange focus) {
		for (Change change : focus.getChildren()) {
			if (fFilter == null || fFilter.select(change)) {
				if (change instanceof CompositeChange && ((CompositeChange) change).isSynthetic()) {
					getFlattendedChildren(result, parent, (CompositeChange) change);
				} else {
					result.add(createNode(parent, change));
				}
			}
		}
	}
}