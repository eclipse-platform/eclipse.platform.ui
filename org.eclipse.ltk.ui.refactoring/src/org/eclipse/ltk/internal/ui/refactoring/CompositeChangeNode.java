/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;

import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringPreviewChangeRequestor;

import org.eclipse.jface.resource.ImageDescriptor;

public class CompositeChangeNode extends AbstractChangeNode {
	
	private final RefactoringPreviewChangeRequestor fRequestor;

	public CompositeChangeNode(PreviewNode parent, RefactoringPreviewChangeRequestor requestor, CompositeChange change) {
		super(parent, change);
		fRequestor= requestor;
	}
	
	int getActive() {
		return getCompositeChangeActive();
	}

	public ImageDescriptor getImageDescriptor() {
		return RefactoringPluginImages.DESC_OBJS_COMPOSITE_CHANGE;
	}
	
	PreviewNode[] doCreateChildren() {
		List children= new ArrayList();
		getFlattendedChildren(children, this, (CompositeChange)getChange());
		return (PreviewNode[])children.toArray(new PreviewNode[children.size()]);
	}
	
	private void getFlattendedChildren(List result, CompositeChangeNode parent, CompositeChange focus) {
		Change[] changes= focus.getChildren();
		for (int i= 0; i < changes.length; i++) {
			Change change= changes[i];
			if (fRequestor == null || fRequestor.accept(change)) {
				if (change instanceof CompositeChange && ((CompositeChange) change).isSynthetic()) {
					getFlattendedChildren(result, parent, (CompositeChange) change);
				} else {
					result.add(createNode(parent, change));
				}
			}
		}
	}
}