/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.ui.refactoring;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Viewer to present the preview for a {@link org.eclipse.ltk.core.refactoring.Change}.
 * <p>
 * Viewers are associated with a change object via the extension point <code>
 * org.eclipse.ltk.ui.refactoring.changePreviewViewers</code>. Implementors of this
 * extension point must therefore implement this interface.
 * </p>
 * <p>
 * To ensure visual consistency across all provided preview viewers the widget
 * hierarchy provided through the method {@link #createControl(Composite)} has to
 * use a {@link org.eclipse.swt.custom.ViewForm} as its root widget.
 * </p>
 * <p>
 * Clients of this interface should call <code>createControl</code> before calling
 * <code>setInput</code>.
 * </p>
 *
 * @since 3.0
 */
public interface IChangePreviewViewer {

	/**
	 * Creates the preview viewer's widget hierarchy. This method
	 * is only called once. Method <code>getControl()</code>
	 * should be used to retrieve the widget hierarchy.
	 *
	 * @param parent the parent for the widget hierarchy
	 *
	 * @see #getControl()
	 */
	void createControl(Composite parent);

	/**
	 * Returns the preview viewer's SWT control.
	 *
	 * @return the preview viewer's SWT control or <code>null</code>
	 *  is the widget hierarchy hasn't been created yet
	 */
	Control getControl();

	/**
	 * Sets the preview viewer's input element.
	 *
	 * @param input the input element
	 */
	void setInput(ChangePreviewViewerInput input);
}

