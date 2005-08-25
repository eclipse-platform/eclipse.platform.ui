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

import java.util.List;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.IChangePreviewViewer;

/**
 * Instances of <code>ChangeElement<code> are used to present <code>
 * IChange</code> object as nodes in a tree.
 */
public abstract class ChangeElement {
	
	/** Flag indicating that the change element isn't active */
	public final static int INACTIVE=		0;
	/** Flag indicating that the change element is partly active (some children are inactive) */
	public final static int PARTLY_ACTIVE=	1;	
	/** Flage indicating that the change element is active */
	public final static int ACTIVE=			2;
	
	protected final static int[][] ACTIVATION_TABLE= new int[][] {
								/*INACTIVE*/	/*PARTLY_ACTIVE */	/*ACTIVE */
		/* INACTIVE */		{	INACTIVE,		PARTLY_ACTIVE,		PARTLY_ACTIVE },
		/* PARTLY_ACTIVE*/	{	PARTLY_ACTIVE, 	PARTLY_ACTIVE,		PARTLY_ACTIVE },
		/* ACTIVE */		{	PARTLY_ACTIVE, 	PARTLY_ACTIVE,		ACTIVE}
	};
	
	protected static final ChangeElement[] EMPTY_CHILDREN= new ChangeElement[0];
	
	private ChangeElement fParent;

	/**
	 * Creates a new <code>ChangeElement</code> with the
	 * given parent
	 * 
	 * @param parent the change element's parent or <code>null
	 * 	</code> if the change element doesn't have a parent
	 */
	public ChangeElement(ChangeElement parent) {
		fParent= parent;
	}

	/**
	 * Returns the change element's parent.
	 * 
	 * @return the change element's parent
	 */
	public ChangeElement getParent() {
		return fParent;
	}
	
	/**
	 * Returns the viewer descriptor used to present a preview of this change element
	 * 
	 * @return the viewer suitable to present a preview of this change or
	 *  <code>null</code> if no previewer is configured.
	 * 
	 * @throws CoreException if an error occurred while creating the descriptor
	 */
	public abstract ChangePreviewViewerDescriptor getChangePreviewViewerDescriptor() throws CoreException;
	
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
	public abstract void feedInput(IChangePreviewViewer viewer, List categories) throws CoreException;
	
	/**
	 * Returns the change directly associated with this change element or <code
	 * null</code> if the element isn't associated with a change.
	 * 
	 * @return the change or <code>null</code>
	 */
	public abstract Change getChange();
	
	/**
	 * Sets the activation status for this <code>ChangeElement</code>. When a 
	 * change element is not active,  then executing it is expected to do nothing.
	 *
	 * @param enabled the activation status for this change element
	 */
	public abstract void setEnabled(boolean enabled);
	
	/**
	 * Sets the activation status for this <code>ChangeElement</code>. When a 
	 * change element is not active,  then executing it is expected to do nothing.
	 *
	 * @param enabled the activation status for this change element
	 */
	public abstract void setEnabledShallow(boolean enabled);
	
	/**
	 * Returns the activation status of this <code>ChangeElement</code>.
	 * Returns one of the following values: <code>IChange.ACTIVE</code>
	 * if the node and all its children are active, <code>IChange.INACTIVE</code>
	 * if all children and the node itself is inactive, and <code>IChange.PARTLY_ACTIVE
	 * </code>otherwise.
	 *
	 * @return the change element's activation status.
	 */
	public abstract int getActive();
	
	/**
	 * Returns the element the change node represents. The method may return 
	 * <code>null</code> if the change node isn't related to an element.
	 * 
	 * @return the element modified by this change node
	 */
	public abstract Object getModifiedElement();
	
	/**
	 * Returns the change element's children.
	 * 
	 * @return the change element's children.
	 */
	public abstract ChangeElement[] getChildren();
	
	/**
	 * Returns <code>true</code> if the change node has
	 * one of the given group categories. Otherwise
	 * <code>false</code> is returned.
	 *
	 * @param categories the group categories to check
	 * 
	 * @return whether the change node has one of the given 
	 *  group categories
	 */
	public abstract boolean hasOneGroupCategory(List categories);
	
}