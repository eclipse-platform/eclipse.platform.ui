/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * (named <code>"org.eclipse.ui.preferencePages"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.preferencePages"&GT;
 *      &LT;page id="com.example.myplugin.prefs"
 *         name="Knobs"
 *         class="com.example.myplugin.MyPreferencePage" /&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p>
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
