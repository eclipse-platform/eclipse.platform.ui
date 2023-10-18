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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.text.IRegion;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.ChangePreviewViewerInput;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;

public final class TextEditGroupNode extends TextEditChangeNode.ChildNode {

	private TextEditBasedChangeGroup fChangeGroup;

	public TextEditGroupNode(PreviewNode parent, TextEditBasedChangeGroup changeGroup) {
		super(parent);
		fChangeGroup= changeGroup;
		Assert.isNotNull(fChangeGroup);
	}

	/**
	 * Returns the <code>TextEditBasedChangeGroup</code> managed by this node.
	 *
	 * @return the <code>TextEditBasedChangeGroup</code>
	 */
	TextEditBasedChangeGroup getChangeGroup() {
		return fChangeGroup;
	}

	@Override
	public String getText() {
		return fChangeGroup.getName();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return RefactoringPluginImages.DESC_OBJS_TEXT_EDIT;
	}

	@Override
	ChangePreviewViewerDescriptor getChangePreviewViewerDescriptor() throws CoreException {
		InternalTextEditChangeNode element= getTextEditChangeNode();
		if (element == null)
			return null;
		return element.getChangePreviewViewerDescriptor();
	}

	@Override
	void feedInput(IChangePreviewViewer viewer, List<GroupCategory> categories) throws CoreException {
		InternalTextEditChangeNode element= getTextEditChangeNode();
		if (element != null) {
			Change change= element.getChange();
			if (change instanceof TextEditBasedChange) {
				IRegion range= getTextRange(this);
				ChangePreviewViewerInput input= null;
				if (range != null) {
					input= TextEditChangePreviewViewer.createInput(change, new TextEditBasedChangeGroup[] {fChangeGroup}, range);
				} else {
					input= TextEditChangePreviewViewer.createInput(change, fChangeGroup, 2);
				}
				viewer.setInput(input);
			}
		} else {
			viewer.setInput(null);
		}
	}

	@Override
	void setEnabled(boolean enabled) {
		fChangeGroup.setEnabled(enabled);
	}

	@Override
	void setEnabledShallow(boolean enabled) {
		fChangeGroup.setEnabled(enabled);
	}

	@Override
	int getActive() {
		return fChangeGroup.isEnabled() ? PreviewNode.ACTIVE : PreviewNode.INACTIVE;
	}

	@Override
	PreviewNode[] getChildren() {
		return PreviewNode.EMPTY_CHILDREN;
	}

	@Override
	boolean hasOneGroupCategory(List<GroupCategory> categories) {
		return fChangeGroup.getGroupCategorySet().containsOneCategory(categories);
	}

	@Override
	boolean hasDerived() {
		return false;
	}

	GroupCategorySet getGroupCategorySet() {
		return fChangeGroup.getGroupCategorySet();
	}

	private static IRegion getTextRange(PreviewNode element) throws CoreException {
		if (element == null)
			return null;
		if (element instanceof InternalLanguageElementNode) {
			return ((InternalLanguageElementNode)element).getTextRange();
		} else if (element instanceof TextEditChangeNode) {
			return null;
		}
		return getTextRange(element.getParent());
	}
}
