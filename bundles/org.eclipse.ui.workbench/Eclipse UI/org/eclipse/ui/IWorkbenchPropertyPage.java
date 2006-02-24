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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * Interface for workbench property pages. Property pages generally show up in
 * the workbench's Property Pages dialog.
 * <p>
 * Clients should implement this interface and include the name of their class
 * in an extension contributed to the workbench's property page extension point 
 * (named <code>"org.eclipse.ui.propertyPages"</code>).
 * For example, the plug-in's XML markup might contain:
 * <pre>
 * &LT;extension point="org.eclipse.ui.propertyPages"&GT;
 *      &LT;page id="com.example.myplugin.props"
 *         name="Knobs"
 *         objectClass="org.eclipse.core.resources.IResource"
 *         class="com.example.myplugin.MyPropertyPage" /&GT;
 * &LT;/extension&GT;
 * </pre>
 * </p>
 */
public interface IWorkbenchPropertyPage extends IPreferencePage {
    /**
     * Returns the object that owns the properties shown in this page.
     *
     * @return the object that owns the properties shown in this page
     */
    public IAdaptable getElement();

    /**
     * Sets the object that owns the properties shown in this page.
     * The page is expected to store this object and provide it if
     * <code>getElement</code> is called.
     * <p> As of Eclipse 3.2 the org.eclipse.ui.propertyPages extension
     * point now supports non IAdaptable inputs. An input
     * that is not an IAdaptable will be wrapped in an 
     * IAdaptable by the workbench before it is forwarded 
     * to this method.
     * </p>
     * @see PropertyDialogAction
     *
     * @param element the object that owns the properties shown in this page
     */
    public void setElement(IAdaptable element);
}
