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
package org.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.IWorkbenchPropertyPageMulti;

/**
 * Abstract base implementation of a workbench property page (
 * <code>IWorkbenchPropertyPage</code>). The implementation is a JFace
 * preference page with an adaptable element.
 * <p>
 * Property pages that support multiple selected objects should implement
 * {@link IWorkbenchPropertyPageMulti} instead.
 * <p>
 * Subclasses must implement the <code>createContents</code> framework method to
 * supply the property page's main control.
 * </p>
 * <p>
 * Subclasses should extend the <code>doComputeSize</code> framework method to
 * compute the size of the page's control.
 * </p>
 * <p>
 * Subclasses may override the <code>performOk</code>,
 * <code>performApply</code>,<code>performDefaults</code>,
 * <code>performCancel</code>, and <code>performHelp</code> framework methods to
 * react to the standard button events.
 * </p>
 * <p>
 * Subclasses may call the <code>noDefaultAndApplyButton</code> framework method
 * before the page's control has been created to suppress the standard Apply and
 * Defaults buttons.
 * </p>
 *
 * @see IWorkbenchPropertyPage
 * @see IWorkbenchPropertyPageMulti
 */
public abstract class PropertyPage extends PreferencePage implements IWorkbenchPropertyPage {
	/**
	 * The element.
	 */
	private IAdaptable element;

	/**
	 * Creates a new property page.
	 */
	public PropertyPage() {
	}

	@Override
	public IAdaptable getElement() {
		return element;
	}

	/**
	 * Sets the element that owns properties shown on this page.
	 *
	 * @param element the element
	 */
	@Override
	public void setElement(IAdaptable element) {
		this.element = element;
	}
}
