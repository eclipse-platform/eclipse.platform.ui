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

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Description of an editor in the workbench editor registry. The editor
 * descriptor contains the information needed to create editor instances.
 * <p>
 * An editor descriptor typically represents one of three types of editors:
 * </p>
 * <ul>
 * <li>a file editor extension for a specific file extension.</li>
 * <li>a file editor added by the user (via the workbench preference page)</li>
 * <li>a general editor extension which works on objects other than files.</li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 *
 * @see IEditorRegistry
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IEditorDescriptor extends IWorkbenchPartDescriptor {
	/**
	 * Returns the editor id.
	 * <p>
	 * For internal editors, this is the extension id as defined in the workbench
	 * registry; for external editors, it is path and file name of the external
	 * program.
	 * </p>
	 *
	 * @return the id of the editor
	 */
	@Override
	String getId();

	/**
	 * Returns the descriptor of the image for this editor.
	 *
	 * @return the descriptor of the image to display next to this editor
	 */
	@Override
	ImageDescriptor getImageDescriptor();

	/**
	 * Returns the label to show for this editor.
	 *
	 * @return the editor label
	 */
	@Override
	String getLabel();

	/**
	 * Returns whether this editor descriptor will open a regular editor part inside
	 * the editor area.
	 *
	 * @return <code>true</code> if editor is inside editor area, and
	 *         <code>false</code> otherwise
	 * @since 3.0
	 */
	boolean isInternal();

	/**
	 * Returns whether this editor descriptor will open an external editor in-place
	 * inside the editor area.
	 *
	 * @return <code>true</code> if editor is in-place, and <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	boolean isOpenInPlace();

	/**
	 * Returns whether this editor descriptor will open an external editor in a new
	 * window outside the workbench.
	 *
	 * @return <code>true</code> if editor is external, and <code>false</code>
	 *         otherwise
	 * @since 3.0
	 */
	boolean isOpenExternal();

	/**
	 * Returns the editor matching strategy object for editors represented by this
	 * editor descriptor, or <code>null</code> if there is no explicit matching
	 * strategy specified.
	 *
	 * @return the editor matching strategy, or <code>null</code> if none
	 * @since 3.1
	 */
	IEditorMatchingStrategy getEditorMatchingStrategy();
}
