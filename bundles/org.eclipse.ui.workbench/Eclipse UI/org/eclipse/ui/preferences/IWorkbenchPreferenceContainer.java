/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.preferences;

/**
 * IWorkbenchPreferenceContainer is the class that specifies
 * the workbench specific preferences support.
 *
 */
public interface IWorkbenchPreferenceContainer {
	
	/**
	 * Open the page specified in the org.eclipse.ui.preferencePage
	 * extension point with id pageId. Apply data to it 
	 * when it is opened.
	 * @param preferencePageId String the id specified for a page in
	 *    the plugin.xml of its defining plug-in.
	 * @param data The data to be applied to the page when it 
	 * 		opens.
	 * @return boolean <code>true</code> if the page was
	 * opened successfully and data was applied.
	 */
	public boolean openPage(String preferencePageId, Object data);

}
