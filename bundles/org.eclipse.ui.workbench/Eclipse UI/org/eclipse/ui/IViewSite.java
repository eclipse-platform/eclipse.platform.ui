/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui;

/**
 * The primary interface between a view part and the workbench.
 * <p>
 * The workbench exposes its implemention of view part sites via this interface,
 * which is not intended to be implemented or extended by clients.
 * </p>
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IViewSite extends IWorkbenchPartSite {

	/**
	 * Returns the action bars for this part site. Views have exclusive use of their
	 * site's action bars.
	 *
	 * @return the action bars
	 */
	IActionBars getActionBars();

	/**
	 * Returns the secondary id for this part site's part, or <code>null</code> if
	 * it has none.
	 *
	 * @return the secondary id for this part site's part or <code>null</code> if it
	 *         has none
	 * @see IWorkbenchPage#showView(String, String, int)
	 * @since 3.0
	 */
	String getSecondaryId();
}
