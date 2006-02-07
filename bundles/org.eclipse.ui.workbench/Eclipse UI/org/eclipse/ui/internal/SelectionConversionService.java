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

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * The SelectionConversionService is the service that converts the selection
 * to IResources.
 * @since 3.2
 * 
 */
public class SelectionConversionService {
	
	/**
	 * The SERVICE_KEY is the Object used to reference the service in the 
	 * workbench.
	 */
	public static Object SERVICE_KEY = new Object();

	/**
	 * Create a new instance of the receiver.
	 */
	public SelectionConversionService() {
		super();
	}

	/**
	 * Attempt to convert the elements in the passed selection into resources by
	 * asking each for its IResource property (iff it isn't already a resource).
	 * If all elements in the initial selection can be converted to resources
	 * then answer a new selection containing these resources; otherwise answer
	 * an empty selection.
	 * 
	 * @param originalSelection
	 *            the original selection
	 * @return the converted selection or an empty selection.
	 */
	public IStructuredSelection convertToResources(
			IStructuredSelection originalSelection) {
		// @issue resource-specific code should be pushed into IDE
		Class resourceClass = LegacyResourceSupport.getResourceClass();
		if (resourceClass == null) {
			return originalSelection;
		}

		List result = new ArrayList();
		Iterator elements = originalSelection.iterator();

		while (elements.hasNext()) {
			Object currentElement = elements.next();
			if (resourceClass.isInstance(currentElement)) { // already a
				// resource
				result.add(currentElement);
			} else if (currentElement instanceof IAdaptable) {
				Object adapter = ((IAdaptable) currentElement)
						.getAdapter(resourceClass);
				if (resourceClass.isInstance(adapter))
					result.add(adapter); // add the converted resource

			}
		}

		// all that can be converted are done, answer new selection
		if (result.isEmpty())
			return StructuredSelection.EMPTY;
		return new StructuredSelection(result.toArray());
	}

}
