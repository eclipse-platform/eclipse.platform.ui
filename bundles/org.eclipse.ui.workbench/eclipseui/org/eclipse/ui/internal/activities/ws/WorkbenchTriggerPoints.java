/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.activities.ws;

/**
 * @since 3.1
 */
public interface WorkbenchTriggerPoints {

	/**
	 * New wizard trigger point identifier. Value
	 * <code>org.eclipse.ui.newWizards</code>.
	 */
	String NEW_WIZARDS = "org.eclipse.ui.newWizards"; //$NON-NLS-1$

	/**
	 * Perspective opening trigger point identifier. Value
	 * <code>org.eclipse.ui.openPerspectiveDialog</code>.
	 */
	String OPEN_PERSPECITVE_DIALOG = "org.eclipse.ui.openPerspectiveDialog"; //$NON-NLS-1$

	/**
	 * Import wizards trigger point identifier. Value
	 * <code>org.eclipse.ui.importWizards</code>.
	 */
	String IMPORT_WIZARDS = "org.eclipse.ui.importWizards"; //$NON-NLS-1$

	/**
	 * Export wizards trigger point identifier. Value
	 * <code>org.eclipse.ui.exportWizards</code>.
	 */
	String EXPORT_WIZARDS = "org.eclipse.ui.exportWizards"; //$NON-NLS-1$

}
