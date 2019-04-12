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
 * Interface for reusable editors.
 *
 * An editors may support changing its input so that the workbench may change
 * its contents instead of opening a new editor.
 */
public interface IReusableEditor extends IEditorPart {
	/**
	 * Sets the input to this editor.
	 *
	 * <p>
	 * <b>Note:</b> Clients must fire the {@link IEditorPart#PROP_INPUT } property
	 * change within their implementation of <code>setInput()</code>.
	 * <p>
	 *
	 * @param input the editor input
	 */
	void setInput(IEditorInput input);
}
