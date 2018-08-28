/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
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
package org.eclipse.ui.navigator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;

/**
 * <p>
 * Provides information to the Common Navigator on how to link selections with
 * active editors and vice versa.
 * </p>
 * <p>
 * The Common Navigator allows clients to plug-in their own custom logic for
 * linking selections from the Viewer to active editors. This interface is used
 * by the <b>org.eclipse.ui.navigator.linkHelper </b> extension
 * point to gather information and trigger editor activations.
 * </p>
 *
 * @since 3.2
 */
public interface ILinkHelper {

	/**
	 * <p>
	 * Determine the correct structured selection for the Common Navigator given
	 * anInput.
	 * </p>
	 *
	 * @param anInput
	 *            An Editor input
	 * @return A selection to be set against the {@link CommonViewer}
	 */
	IStructuredSelection findSelection(IEditorInput anInput);

	/**
	 * <p>
	 * Activate the correct editor for aSelection.
	 * </p>
	 *
	 * @param aPage
	 *            A WorkbenchPage to use for editor location and activation
	 * @param aSelection
	 *            The current selection from the {@link CommonViewer}
	 */
	void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection);
}
