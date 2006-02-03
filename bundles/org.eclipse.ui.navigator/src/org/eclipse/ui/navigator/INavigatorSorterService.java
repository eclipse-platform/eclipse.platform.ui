/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.ViewerSorter;

/**
 * 
 * Provides an interface to extensions declared in
 * <b>org.eclipse.ui.navigator.navigatorContent/commonSorter</b>.
 * 
 * <p>
 * Like other extensions to the Common Navigator framework, sorters defined by
 * the above extension point must be bound to the associated
 * {@link INavigatorContentService} through a
 * <b>org.eclipse.ui.navigator.viewer/viewerContentBinding</b> extension.
 * </p>
 * 
 * @since 3.2
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * 
 */
public interface INavigatorSorterService {

	/**
	 * Return a {@link ViewerSorter} from an extension which is visible to the
	 * associated {@link INavigatorContentService} and whose <b>parentExpression</b>
	 * matches the given parent.
	 * 
	 * @param aParent An element from the tree
	 * @return An applicable ViewerSorter or simple {@link ViewerSorter} if no sorter is found.
	 */
	ViewerSorter findSorterForParent(Object aParent);
	
	/**
	 * Return a {@link ViewerSorter} from an extension which is visible to the
	 * associated {@link INavigatorContentService} and whose <b>parentExpression</b>
	 * matches the given parent.
	 * 
	 * @param source The source of the element.
	 * @param parent An element from the tree
	 * @param lvalue An element from the tree
	 * @param rvalue An element from the tree
	 * @return An applicable ViewerSorter or simple {@link ViewerSorter} if no sorter is found.
	 */
	ViewerSorter findSorter(INavigatorContentDescriptor source, Object parent, Object lvalue, Object rvalue);	
}
