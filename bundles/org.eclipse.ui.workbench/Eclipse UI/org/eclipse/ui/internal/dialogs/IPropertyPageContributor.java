/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Oakland Software (Francis Upton) <francisu@ieee.org> - bug 223808 
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.ui.internal.IObjectContributor;

/**
 * Implement this interface in order to register property
 * pages for a given object. During property dialog building
 * sequence, all property page contributors for a given object
 * are given a chance to add their pages.
 */
public interface IPropertyPageContributor extends IObjectContributor {
    /**
     * Implement this method to add an instance of PropertyPage class to the
     * property page manager.
     * @param manager the contributor manager onto which to contribute the
     * property page.
     * @param object the type for which page should be contributed. 
     * @return true the page that was added, <code>null</code> if no page was added.
     */
    public PreferenceNode contributePropertyPage(PropertyPageManager manager,
            Object object);
}
