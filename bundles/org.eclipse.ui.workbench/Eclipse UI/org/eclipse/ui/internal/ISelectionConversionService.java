/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * <p>
 * A service that is capable of converting a selection into resources.
 * </p>
 * <p>
 * This interface is only intended for use within the
 * <code>org.eclipse.ui.workbench</code> and <code>org.eclipse.ui.ide</code>
 * plug-ins.
 * </p>
 * 
 * @since 3.2
 */
public interface ISelectionConversionService {

	/**
	 * Attempt to convert the elements in the passed selection into resources by
	 * asking each for its IResource property (iff it isn't already a resource).
	 * If all elements in the initial selection can be converted to resources
	 * then answer a new selection containing these resources; otherwise answer
	 * an empty selection.
	 * 
	 * @param originalSelection
	 *            the original selection; must not be <code>null</code>.
	 * @return the converted selection or an empty selection; never
	 *         <code>null</code>.
	 */
	public IStructuredSelection convertToResources(
			IStructuredSelection originalSelection);

}

