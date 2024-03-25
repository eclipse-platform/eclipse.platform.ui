/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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

package org.eclipse.jface.viewers;

import org.eclipse.pde.api.tools.annotations.NoExtend;
import org.eclipse.pde.api.tools.annotations.NoImplement;
import org.eclipse.pde.api.tools.annotations.NoInstantiate;
import org.eclipse.pde.api.tools.annotations.NoReference;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;

/**
 * This class is not part of the public API of JFace. See bug 267722.
 *
 * @since 3.5
 */
@NoInstantiate
@NoExtend
public class StructuredViewerInternals {

	/**
	 * Nothing to see here.
	 *
	 * @since 3.5
	 */
	@NoExtend
	@NoImplement
	protected static interface AssociateListener {

		/**
		 * Call when an element is associated with an Item
		 *
		 * @param element the element
		 * @param item    the item
		 */
		void associate(Object element, Item item);

		/**
		 * Called when an Item is no longer associated
		 *
		 * @param item the item
		 */
		void disassociate(Item item);

		/**
		 * Called when an element has been filtered out.
		 *
		 * @since 3.6
		 * @param element the filtered element
		 */
		void filteredOut(Object element);
	}

	/**
	 * Nothing to see here. Sets or resets the AssociateListener for the given
	 * Viewer.
	 *
	 * @param viewer
	 *            the viewer
	 * @param listener
	 *            the {@link AssociateListener}
	 */
	@NoReference
	protected static void setAssociateListener(StructuredViewer viewer,
			AssociateListener listener) {
		viewer.setAssociateListener(listener);
	}

	/**
	 * Nothing to see here. Returns the items for the given element.
	 *
	 * @param viewer
	 *            the viewer
	 * @param element
	 *            the element
	 * @return the Widgets corresponding to the element
	 */
	@NoReference
	protected static Widget[] getItems(StructuredViewer viewer, Object element) {
		return viewer.findItems(element);
	}

}
