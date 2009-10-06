/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

public interface IMementoConstants {
	// These persistence constant is stored in XML. Do not
	// change it.
	public static final String TAG_FACTORY_ID = "factoryID"; //$NON-NLS-1$
	
	public static final String TAG_EDITOR_STATE = "editorState"; //$NON-NLS-1$

	public static final String MEMENTO_ITEM = "item"; //$NON-NLS-1$

	public static final String TAG_EDIT_PAGE_ID = "editPageId"; //$NON-NLS-1$

	public static final String TAG_NAME = "name"; //$NON-NLS-1$

	public static final String TAG_LABEL = "label"; //$NON-NLS-1$

	public static final String TAG_ID = "id"; //$NON-NLS-1$

	public static final String TAG_LAUNCH_CONFIGURATION_WORKING_SET = "launchConfigurationWorkingSet"; //$NON-NLS-1$

}
