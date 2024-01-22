/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * <p>
 * The SelectionConversionService is the service that converts the selection to
 * IResources.
 * </p>
 * <p>
 * This interface is only intended for use within the
 * <code>org.eclipse.ui.workbench</code> and <code>org.eclipse.ui.ide</code>
 * plug-ins.
 * </p>
 *
 * @since 3.2
 */
public class SelectionConversionService implements ISelectionConversionService {

	/**
	 * Attempt to convert the elements in the passed selection into resources by
	 * asking each for its IResource property (iff it isn't already a resource). If
	 * all elements in the initial selection can be converted to resources then
	 * answer a new selection containing these resources; otherwise answer an empty
	 * selection.
	 *
	 * @param originalSelection the original selection
	 * @return the converted selection or an empty selection.
	 */
	@Override
	public IStructuredSelection convertToResources(IStructuredSelection originalSelection) {
		// @issue resource-specific code should be pushed into IDE
		Class<?> resourceClass = LegacyResourceSupport.getResourceClass();
		if (resourceClass == null) {
			return originalSelection;
		}

		List result = new ArrayList();

		for (Object currentElement : originalSelection) {
			Object resource = Adapters.adapt(currentElement, resourceClass);
			if (resource != null) {
				result.add(resource);
			}
		}

		// all that can be converted are done, answer new selection
		if (result.isEmpty()) {
			return StructuredSelection.EMPTY;
		}
		return new StructuredSelection(result.toArray());
	}

}
