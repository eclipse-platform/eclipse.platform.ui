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

import org.eclipse.jface.preference.IPreferencePage;

/**
 * Interface for workbench preference pages.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in an extension contributed to the workbench's preference extension point
 * (named <code>"org.eclipse.ui.preferencePages"</code>). For example, the
 * plug-in's XML markup might contain:
 * </p>
 *
 * <pre>
 * &lt;extension point="org.eclipse.ui.preferencePages"&gt;
 *      &lt;page id="com.example.myplugin.prefs"
 *         name="Knobs"
 *         class="com.example.myplugin.MyPreferencePage" /&gt;
 * &lt;/extension&gt;
 * </pre>
 */
public interface IWorkbenchPreferencePage extends IPreferencePage {
	/**
	 * Initializes this preference page for the given workbench.
	 * <p>
	 * This method is called automatically as the preference page is being created
	 * and initialized. Clients must not call this method.
	 * </p>
	 *
	 * @param workbench the workbench
	 */
	void init(IWorkbench workbench);
}
