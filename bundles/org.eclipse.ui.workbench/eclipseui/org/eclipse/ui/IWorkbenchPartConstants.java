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
 *     Stefan Xenos, IBM; Chris Torrence, ITT Visual Information Solutions - bug 51580
 *******************************************************************************/
package org.eclipse.ui;

/**
 * This interface describes the constants used for
 * {@link org.eclipse.ui.IWorkbenchPart IWorkbenchPart} properties.
 * <p>
 * <b>Note:</b>This interface should not be implemented or extended.
 * </p>
 *
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbenchPartConstants {

	/**
	 * The property id for <code>getTitle</code>, <code>getTitleImage</code> and
	 * <code>getTitleToolTip</code>.
	 */
	int PROP_TITLE = 0x001;

	/**
	 * The property id for <code>ISaveablePart.isDirty()</code>.
	 */
	int PROP_DIRTY = 0x101;

	/**
	 * The property id for <code>IEditorPart.getEditorInput()</code>.
	 */
	int PROP_INPUT = 0x102;

	/**
	 * The property id for <code>IWorkbenchPart2.getPartName</code>
	 */
	int PROP_PART_NAME = 0x104;

	/**
	 * The property id for <code>IWorkbenchPart2.getContentDescription()</code>
	 */
	int PROP_CONTENT_DESCRIPTION = 0x105;

	/**
	 * The property id for any method on the optional <code>ISizeProvider</code>
	 * interface
	 *
	 * @since 3.4
	 */
	int PROP_PREFERRED_SIZE = 0x303;

}
