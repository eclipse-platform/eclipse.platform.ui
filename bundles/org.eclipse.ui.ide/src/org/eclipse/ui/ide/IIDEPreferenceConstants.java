/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide;

/**
 * Preference ids exposed by the Eclipse IDE. These preference settings can be
 * obtained from the IDE plug-in's preference store.
 * <p>
 * <b>Note:</b>This interface should not be implemented or extended.
 * </p>
 * 
 * @since 3.10
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IIDEPreferenceConstants {

	/**
	 * Workspace name, will be displayed in the window title. This preference
	 * must only be changed on the UI thread.
	 */
	public static final String WORKSPACE_NAME = "WORKSPACE_NAME"; //$NON-NLS-1$
}
