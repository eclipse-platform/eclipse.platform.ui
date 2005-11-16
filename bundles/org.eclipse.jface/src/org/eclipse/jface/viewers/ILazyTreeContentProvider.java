/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * The ILazyTreeContentProvider is the content provider for tree viewers created
 * using the SWT.VIRTUAL flag that only wish to return their contents as they
 * are queried.
 * 
 * <strong>NOTE:</strong> Changes to the number of items for a given parent
 * require a call to {@link TreeViewer#setItemCount()} on the tree viewer, or
 * calls to the viewer's <code>add()</code> or <code>remove()</code>
 * methods.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will remain
 * unchanged during the 3.2 release cycle. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public interface ILazyTreeContentProvider extends IContentProvider {
	/**
	 * Called when a previously-blank item becomes visible in the TreeViewer. If
	 * the content provider knows the child element for the given parent at this
	 * index, it should respond by calling
	 * {@link TreeViewer#replace(Object, int, Object)}. If the content provider
	 * knows the number of children for the child element, it should also call
	 * {@link TreeViewer#setChildCount(Object, int)}.
	 * 
	 * <strong>NOTE</strong> #updateElement(int index) can be used to determine
	 * selection values. If TableViewer#replace(Object, int) is not called
	 * before returning from this method, selections may have missing or stale
	 * elements. In this situation it is suggested that the selection is asked
	 * for again after replace() has been called.
	 * 
	 * @param parent
	 *            The parent of the element
	 * @param index
	 *            The child index of the parent that is being updated in the
	 *            tree.
	 */
	public void updateElement(Object parent, int index);
}
