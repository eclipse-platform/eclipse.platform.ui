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

import org.eclipse.ltk.core.refactoring.GroupCategory;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

/**
 * Instances of <code>PreviewNode</code> are used to present <code>
 * IChange</code> object as nodes in a tree.
 */
public abstract class PreviewNode {

	/** Flag indicating that the change element isn't active */
	final static int INACTIVE=		0;

	/** Flag indicating that the change element is partly active (some children are inactive) */
	final static int PARTLY_ACTIVE=	1;

	/** Flage indicating that the change element is active */
	final static int ACTIVE=			2;

	final static int[][] ACTIVATION_TABLE= new int[][] {
								/*INACTIVE*/	/*PARTLY_ACTIVE */	/*ACTIVE */
		/* INACTIVE */		{	INACTIVE,		PARTLY_ACTIVE,		PARTLY_ACTIVE },
		/* PARTLY_ACTIVE*/	{	PARTLY_ACTIVE, 	PARTLY_ACTIVE,		PARTLY_ACTIVE },
		/* ACTIVE */		{	PARTLY_ACTIVE, 	PARTLY_ACTIVE,		ACTIVE}
	};

	static final PreviewNode[] EMPTY_CHILDREN= new PreviewNode[0];

	private PreviewNode fParent;

	/**
	 * Creates a new <code>PreviewNode</code> with the
	 * given parent
	 *
	 * @param parent the change element's parent or <code>null
	 * 	</code> if the change element doesn't have a parent
	 */
	protected PreviewNode(PreviewNode parent) {
		fParent= parent;
	}

	/**
	 * Initializes the change node with the given parent. This method
	 * should only be used if the change node has been created via
	 * getAdapter and the parent must be set after creation.
	 *
	 * @param parent the parent or <code>null</code>
	 */
	/* package */ void initialize(PreviewNode parent) {
		Assert.isTrue(fParent == null);
		fParent= parent;
	}

	/**
	 * Returns the change element's parent.
	 *
	 * @return the change element's parent
	 */
	PreviewNode getParent() {
		return fParent;
	}

	/**
	 * Returns the text used to render this node in the
	 * UI.
	 *
	 * @return a human readable representation of this node
	 */
	public abstract String getText();

	/**
	 * Returns the image descriptor used to render this node
	 * in the UI.
	 *
	 * @return the image descriptor representing this node
	 */
	public abstract ImageDescriptor getImageDescriptor();

	/**
	 * Returns the viewer descriptor used to present a preview of this change element
	 *
	 * @return the viewer suitable to present a preview of this change or
	 *  <code>null</code> if no previewer is configured.
	 *
	 * @throws CoreException if an error occurred while creating the descriptor
	 */
	abstract ChangePreviewViewerDescriptor getChangePreviewViewerDescriptor() throws CoreException;

	/**
	 * Feeds the input that corresponds to this change element into the
	 * given viewer.
	 *
	 * @param viewer the viewer to feed the input into
	 * @param categories the group categories to filter for or
	 *  <code>null</code> if no filtering should take place
	 *
	 * @throws CoreException if an error occurred while feeding the input
	 */
	abstract void feedInput(IChangePreviewViewer viewer, List<GroupCategory> categories) throws CoreException;

	/**
	 * Sets the activation status for this <code>PreviewNode</code>. When a
	 * change element is not active, then executing it is expected to do nothing.
	 *
	 * @param enabled the activation status for this change element
	 */
	abstract void setEnabled(boolean enabled);

	/**
	 * Sets the activation status for this <code>PreviewNode</code>. When a
	 * change element is not active, then executing it is expected to do nothing.
	 *
	 * @param enabled the activation status for this change element
	 */
	abstract void setEnabledShallow(boolean enabled);

	/**
	 * Returns the activation status of this <code>PreviewNode</code>.
	 * Returns one of the following values: <code>IChange.ACTIVE</code>
	 * if the node and all its children are active, <code>IChange.INACTIVE</code>
	 * if all children and the node itself is inactive, and <code>IChange.PARTLY_ACTIVE
	 * </code>otherwise.
	 *
	 * @return the change element's activation status.
	 */
	abstract int getActive();

	/**
	 * Returns the change element's children.
	 *
	 * @return the change element's children.
	 */
	abstract PreviewNode[] getChildren();

	/**
	 * Returns <code>true</code> if the change node has
	 * one of the given group categories. Otherwise,
	 * <code>false</code> is returned.
	 *
	 * @param categories the group categories to check
	 *
	 * @return whether the change node has one of the given
	 *  group categories
	 */
	abstract boolean hasOneGroupCategory(List<GroupCategory> categories);

	/**
	 * Returns <code>true</code> if the change node contains
	 * at least one derived resource. Otherwise, <code>false</code> is returned.
	 *
	 * @return whether the change node contains a derived resource
	 */
	abstract boolean hasDerived();
}
