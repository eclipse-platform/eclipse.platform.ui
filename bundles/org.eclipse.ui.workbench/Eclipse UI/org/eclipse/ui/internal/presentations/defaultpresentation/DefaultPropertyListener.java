/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.internal.preferences.AbstractPropertyListener;
import org.eclipse.ui.internal.preferences.IDynamicPropertyMap;
import org.eclipse.ui.internal.preferences.PropertyUtil;

/**
 * @since 3.1
 */
public class DefaultPropertyListener extends AbstractPropertyListener {

    public DefaultTabFolder tabFolder;
    public IDynamicPropertyMap preferences;
    
    public DefaultPropertyListener(DefaultTabFolder folder, IDynamicPropertyMap prefs) {
        this.tabFolder = folder;
        this.preferences = prefs;
        
        prefs.addListener(new String[]{IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS}, this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.AbstractPropertyListener#update()
     */
    public void update() {
        tabFolder.setSimpleTabs(PropertyUtil.get(preferences, 
                IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, true));
    }

}
