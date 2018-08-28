/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Represents a tab descriptor provider for the tabbed property view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public interface ITabDescriptorProvider {

	/**
	 * Returns all tab descriptors.
	 *
	 * @param part
	 *            the workbench part containing the selection
	 * @param selection
	 *            the current selection.
	 * @return all section descriptors.
	 */
	public ITabDescriptor[] getTabDescriptors(IWorkbenchPart part,
			ISelection selection);
}
