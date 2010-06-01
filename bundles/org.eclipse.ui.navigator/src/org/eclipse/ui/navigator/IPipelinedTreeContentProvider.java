/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.navigator;

import java.util.Set;

/**
 * 
 * To correctly implement pipelining you should implement
 * {@link IPipelinedTreeContentProvider2} which provides the
 * additional
 * {@link IPipelinedTreeContentProvider2#hasChildren(Object)} method.
 * This allows the calculation of hasChildren to match what will be provided in
 * calculating the children. If you don't implement the hasChildren, you may get
 * "false positive" hasChildrens which will result in a "+" indication in the
 * tree in the event that the pipelined children calculation.
 * 
 * The only reason these are two separate interfaces is historical.
 * 
 * @since 3.2
 * 
 */
public interface IPipelinedTreeContentProvider extends ICommonContentProvider {

	/**
	 * Intercept the children that would be contributed to the viewer and
	 * determine how to change the shape of those children. The set of children
	 * should be modified to contain the correct children to return to the
	 * viewer.
	 * 
	 * @param aParent
	 *            A parent from the viewer
	 * @param theCurrentChildren
	 *            The set of children contributed thus far from upstream content
	 *            providers.
	 */
	void getPipelinedChildren(Object aParent, Set theCurrentChildren);

	/**
	 * Intercept the elements that would be contributed to the root of the
	 * viewer and determine how to change the shape of those children. The given
	 * set of elements should be modified to contain the correct elements to
	 * return to the viewer.
	 * 
	 * @param anInput
	 *            An input from the viewer
	 * @param theCurrentElements
	 *            The set of children contributed thus far from upstream content
	 *            providers.
	 */
	void getPipelinedElements(Object anInput, Set theCurrentElements);

	/**
	 * Intercept requests for a parent of the given object.
	 * 
	 * @param anObject
	 *            The object being queried for a parent.
	 * @param aSuggestedParent
	 *            The parent already suggested from upstream extensions.
	 * @return The intended parent from this pipelined content provider. If you
	 *         wish to not influence the parent, then return the
	 *         aSuggestedParent value.
	 */
	Object getPipelinedParent(Object anObject, Object aSuggestedParent);

	/**
	 * Intercept attempts to add elements directly to the viewer.
	 * 
	 * <p>
	 * For content extensions that reshape the structure of children in a
	 * viewer, their overridden extensions may sometimes use optimized refreshes
	 * to add elements to the tree. These attempts must be intercepted and
	 * mapped to the correct set of model elements in the overriding extension.
	 * Clients may add, remove, or modify elements in the given set of added
	 * children. Clients should return a set for downstream extensions to
	 * massage further.
	 * </p>
	 * <p>
	 * Clients may change what parent the reshaped elements are added to, so
	 * long as that parent is not the root of the viewer.
	 * </p>
	 * <p>
	 * Clients should never create their own pipeline shape modifications, but
	 * instead return the shape modification that was passed in with appropriate
	 * changes.
	 * </p>
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param anAddModification
	 *            The shape modification which contains the current suggested
	 *            parent and children. Clients may modify this parameter
	 *            directly and return it as the new shape modification.
	 * @return The new shape modification to use. Clients should <b>never</b>
	 *         return <b>null</b> from this method.
	 */
	PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification);

	/**
	 * Intercept attempts to remove elements directly from the viewer.
	 * 
	 * <p>
	 * For content extensions that reshape the structure of children in a
	 * viewer, their overridden extensions may sometimes use optimized refreshes
	 * to remove elements to the tree. These attempts must be intercepted and
	 * mapped to the correct set of model elements in the overriding extension.
	 * Clients may add, remove, or modify elements in the given set of removed
	 * children. Clients should return a set for downstream extensions to
	 * massage further.
	 * </p>
	 * <p>
	 * The parent will be <b>null</b> for remove modifications.
	 * <p>
	 * Clients should never create their own pipeline shape modifications, but
	 * instead return the shape modification that was passed in with appropriate
	 * changes.
	 * </p>
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param aRemoveModification
	 *            The shape modification which contains the current suggested
	 *            parent and children. Clients may modify this parameter
	 *            directly and return it as the new shape modification.
	 * @return The new shape modification to use. Clients should <b>never</b>
	 *         return <b>null</b> from this method.
	 */
	PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification);

	/**
	 * Intercept calls to viewer <code>refresh()</code> methods.
	 * 
	 * <p>
	 * Clients may modify the given update to add or remove the elements to be
	 * refreshed. Clients may return the same instance that was passed in for
	 * the next downstream extension.
	 * </p>
	 * 
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param aRefreshSynchronization
	 *            The (current) refresh update to execute against the viewer.
	 * @return True if the viewer update was modified.
	 */
	boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization);

	/**
	 * Intercept calls to viewer <code>update()</code> methods.
	 * 
	 * <p>
	 * Clients may modify the given update to add or remove the elements to be
	 * updated. Clients may also add or remove properties for the given targets
	 * to optimize the refresh. Clients may return the same instance that was
	 * passed in for the next downstream extension.
	 * </p>
	 * 
	 * <p>
	 * <b>Clients should not call any of the add, remove, refresh, or update
	 * methods on the viewer from this method or any code invoked by the
	 * implementation of this method.</b>
	 * </p>
	 * 
	 * @param anUpdateSynchronization
	 *            The (current) update to execute against the viewer.
	 * @return True if the viewer update was modified.
	 */
	boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization);

}
