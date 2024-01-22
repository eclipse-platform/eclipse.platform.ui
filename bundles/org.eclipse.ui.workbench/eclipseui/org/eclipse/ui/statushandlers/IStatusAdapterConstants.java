/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.statushandlers;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.PlatformUI;

/**
 * This interface contains common constants important for
 * <code>StatusAdapter</code>.
 *
 * <p>
 * This interface should not be implemented or extended by clients.
 * </p>
 *
 * @since 3.4
 */
public interface IStatusAdapterConstants {

	/**
	 * Common prefix for properties defined in this interface.
	 */
	String PROPERTY_PREFIX = PlatformUI.PLUGIN_ID + ".workbench.statusHandlers.adapters"; //$NON-NLS-1$

	/**
	 * This property is used to add title to the adapter. If the adapter is shown in
	 * a dialog, this property is used to create title of the dialog.
	 *
	 * <p>
	 * The property must be of type <code>String</code>.
	 * </p>
	 */
	QualifiedName TITLE_PROPERTY = new QualifiedName(PROPERTY_PREFIX, "title"); //$NON-NLS-1$

	/**
	 * This property is used to add a timestamp to the adapter. If the adapter is
	 * shown in the UI, this property can be used for sorting and showing
	 * information about the status creation time.
	 *
	 * <p>
	 * The property must be of type <code>Long</code>.
	 * </p>
	 */
	QualifiedName TIMESTAMP_PROPERTY = new QualifiedName(PROPERTY_PREFIX, "timestamp"); //$NON-NLS-1$

	/**
	 * This property is used to add an explanation to the adapter. If the adapter is
	 * shown in the UI, this property should be used to present additional
	 * explanation for the status.
	 *
	 * <p>
	 * The property must be of type <code>String</code>.
	 * </p>
	 */
	QualifiedName EXPLANATION_PROPERTY = new QualifiedName(PROPERTY_PREFIX, "explanation"); //$NON-NLS-1$

	/**
	 * This property is used to add a hint to the adapter. If the adapter is shown
	 * in the UI, this property should be used to present suggested actions that
	 * have to be performed by the user.
	 *
	 * <p>
	 * The property must be of type <code>String</code>.
	 * </p>
	 */
	QualifiedName HINT_PROPERTY = new QualifiedName(PROPERTY_PREFIX, "suggestedAction"); //$NON-NLS-1$

}