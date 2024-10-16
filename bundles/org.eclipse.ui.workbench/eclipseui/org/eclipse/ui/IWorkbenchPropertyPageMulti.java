/*******************************************************************************
 * Copyright (c) 2010, 2015 Broadcom Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Broadcom Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferencePage;

/**
 * This interface is similar to {@link IWorkbenchPropertyPage} with the addition
 * of support for multiple selection.
 *
 * @see IWorkbenchPropertyPage
 * @since 3.7
 */
public interface IWorkbenchPropertyPageMulti extends IPreferencePage {

	/**
	 * Sets the elements that own properties shown on this page. This method will be
	 * called if the property page responds to multiple selection.
	 *
	 * @param elements objects that own the properties shown in this page
	 */
	void setElements(IAdaptable[] elements);

}
