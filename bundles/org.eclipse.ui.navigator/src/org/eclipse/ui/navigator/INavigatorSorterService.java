/*******************************************************************************
 * Copyright (c) 2006, 2025 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.navigator;

import java.util.Map;

import org.eclipse.jface.viewers.ViewerComparator;
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
 * @see INavigatorContentService#getSorterService()
 * @see ViewerComparator
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 * @since 3.2
 */
public interface INavigatorSorterService {

	/**
	 * Return a {@link ViewerSorter} from an extension which is visible to the
	 * associated {@link INavigatorContentService} and whose <b>parentExpression</b>
	 * matches the given parent.
	 *
	 * @param aParent An element from the tree
	 * @return An applicable ViewerSorter or simple {@link ViewerSorter} if no
	 *         sorter is found.
	 * @deprecated Use {@link #findComparatorForParent(Object)} instead
	 */
	@Deprecated(since = "2025-03", forRemoval = true)
	ViewerSorter findSorterForParent(Object aParent);

	/**
	 * Return a {@link ViewerComparator} from an extension which is visible to the
	 * associated {@link INavigatorContentService} and whose <b>parentExpression</b>
	 * matches the given parent.
	 *
	 * @param aParent An element from the tree
	 * @return An applicable ViewerComparator or simple {@link ViewerComparator} if
	 *         no sorter is found.
	 * @since 3.13
	 */
	ViewerComparator findComparatorForParent(Object aParent);

	/**
	 * Return a {@link ViewerSorter} from an extension which is visible to the
	 * associated {@link INavigatorContentService} and whose <b>parentExpression</b>
	 * matches the given parent.
	 *
	 * @param source The source of the element.
	 * @param parent An element from the tree
	 * @param lvalue An element from the tree
	 * @param rvalue An element from the tree
	 * @return An applicable ViewerSorter or simple {@link ViewerSorter} if no
	 *         sorter is found.
	 * @deprecated Use
	 *             {@link #findComparator(INavigatorContentDescriptor, Object, Object, Object)}
	 *             instead.
	 */
	@Deprecated(since = "2025-03", forRemoval = true)
	ViewerSorter findSorter(INavigatorContentDescriptor source, Object parent,
			Object lvalue, Object rvalue);

	/**
	 * Return a {@link ViewerComparator} from an extension which is visible to the
	 * associated {@link INavigatorContentService} and whose <b>parentExpression</b>
	 * matches the given parent.
	 *
	 * @param source The source of the element.
	 * @param parent An element from the tree
	 * @param lvalue An element from the tree
	 * @param rvalue An element from the tree
	 * @return An applicable ViewerComparator or simple {@link ViewerComparator} if
	 *         no sorter is found.
	 * @since 3.13
	 */
	ViewerComparator findComparator(INavigatorContentDescriptor source, Object parent, Object lvalue,
			Object rvalue);

	/**
	 * Find and return all viewer sorters associated with the given descriptor.
	 *
	 * <p>
	 * The <i>commonSorter</i> element is not required to have an id, so in some
	 * cases, an auto-generated id, using the content extension id as a base, is
	 * generated to ensure the map is properly filled with all available sorters. No
	 * guarantees are given as to the order or consistency of these generated ids
	 * between invocations.
	 * </p>
	 *
	 * @param theSource A descriptor that identifies a particular content extension
	 * @return A Map[String sorterDescriptorId, ViewerComparator instance] where the
	 *         key is the id defined in the extension and the value is the
	 *         instantiated sorter.
	 *
	 * @see INavigatorContentService#getContentDescriptorById(String)
	 * @see INavigatorContentService#getContentExtensionById(String)
	 * @see INavigatorContentExtension#getDescriptor()
	 * @since 3.3
	 */
	public Map<String, ViewerComparator> findAvailableSorters(INavigatorContentDescriptor theSource);

}
