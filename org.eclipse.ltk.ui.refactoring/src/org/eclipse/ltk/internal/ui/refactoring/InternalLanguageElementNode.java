/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.text.IRegion;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.core.refactoring.GroupCategorySet;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode.ChildNode;

public abstract class InternalLanguageElementNode extends TextEditChangeNode.ChildNode {

	private List<ChildNode> fChildren;
	private GroupCategorySet fGroupCategories;

	protected InternalLanguageElementNode(PreviewNode parent) {
		super(parent);
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
				List<TextEditBasedChangeGroup> groups= collectTextEditBasedChangeGroups(categories);
				viewer.setInput(TextEditChangePreviewViewer.createInput(change,
					groups.toArray(new TextEditBasedChangeGroup[groups.size()]),
					getTextRange()));
			}
		} else {
			viewer.setInput(null);
		}
	}

	@Override
	void setEnabled(boolean enabled) {
		for (PreviewNode element : fChildren) {
			element.setEnabled(enabled);
		}
	}

	@Override
	void setEnabledShallow(boolean enabled) {
		// do nothing. We don't manage an own enablement state.
	}

	@Override
	int getActive() {
		Assert.isTrue(fChildren.size() > 0);
		int result= ((PreviewNode)fChildren.get(0)).getActive();
		for (int i= 1; i < fChildren.size(); i++) {
			PreviewNode element= fChildren.get(i);
			result= PreviewNode.ACTIVATION_TABLE[element.getActive()][result];
			if (result == PreviewNode.PARTLY_ACTIVE)
				break;
		}
		return result;
	}

	@Override
	PreviewNode[] getChildren() {
		if (fChildren == null)
			return PreviewNode.EMPTY_CHILDREN;
		return fChildren.toArray(new PreviewNode[fChildren.size()]);
	}

	@Override
	boolean hasOneGroupCategory(List<GroupCategory> categories) {
		if (fChildren == null)
			return false;
		return getGroupCategorySet().containsOneCategory(categories);
	}

	@Override
	boolean hasDerived() {
		if (fChildren == null)
			return false;
		for (PreviewNode node : fChildren) {
			if (node.hasDerived())
				return true;
		}
		return false;
	}

	private GroupCategorySet getGroupCategorySet() {
		if (fGroupCategories == null) {
			fGroupCategories= GroupCategorySet.NONE;
			for (PreviewNode node : fChildren) {
				GroupCategorySet other= null;
				if (node instanceof TextEditGroupNode) {
					other= ((TextEditGroupNode)node).getGroupCategorySet();
				} else if (node instanceof InternalLanguageElementNode) {
					other= ((InternalLanguageElementNode)node).getGroupCategorySet();
				} else {
					Assert.isTrue(false, "Shouldn't happen"); //$NON-NLS-1$
				}
				fGroupCategories= GroupCategorySet.union(fGroupCategories, other);
			}
		}
		return fGroupCategories;
	}

	protected void internalAddChild(ChildNode child) {
		if (fChildren == null)
			fChildren= new ArrayList<>(2);
		fChildren.add(child);
	}

	private List<TextEditBasedChangeGroup> collectTextEditBasedChangeGroups(List<GroupCategory> categories) {
		List<TextEditBasedChangeGroup> result= new ArrayList<>(10);
		for (PreviewNode child : getChildren()) {
			if (child instanceof TextEditGroupNode) {
				TextEditBasedChangeGroup changeGroup= ((TextEditGroupNode)child).getChangeGroup();
				if (categories == null || changeGroup.getGroupCategorySet().containsOneCategory(categories))
					result.add(changeGroup);
			} else if (child instanceof InternalLanguageElementNode) {
				result.addAll(((InternalLanguageElementNode)child).collectTextEditBasedChangeGroups(categories));
			}
		}
		return result;
	}

	/**
	 * Returns the text region the of this language element node.
	 *
	 * @return the text region of this language element node
	 * @throws CoreException if the source region can't be obtained
	 */
	public abstract IRegion getTextRange() throws CoreException;
}
