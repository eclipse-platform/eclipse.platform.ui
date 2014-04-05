/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.util.Collection;
import org.eclipse.emf.ecore.EObject;

/**
 * This interface provides a common API for editing trees, lists, and xml
 * editors that display EObjects.
 *
 * @author Steven Spungin
 *
 */
public interface IViewEObjects {

	/**
	 * Highlights the indicated items. Objects that are not managed are ignored.
	 * All other items are un-highlighted.
	 *
	 * @param items
	 *            Collection of objects to highlight.
	 */
	void highlightEObjects(Collection<EObject> items);

	/**
	 * All EObjects that are managed by the view.
	 *
	 * @return All EObjects that are managed by the view's provider
	 */
	Collection<EObject> getAllEObjects();

	/**
	 * All EObjects that are selected by the view
	 *
	 * @return All EObjects that are selected by the view
	 */
	Collection<EObject> getSelectedEObjects();

	/**
	 * Deletes the objects specified.
	 *
	 * @param list
	 *            The list of objects to delete. Objects that are not managed
	 *            are ignored.
	 */
	void deleteEObjects(Collection<EObject> list);
}
