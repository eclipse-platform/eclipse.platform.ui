/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.defaultpresentation;

import org.eclipse.ui.internal.preferences.AbstractBooleanListener;
import org.eclipse.ui.internal.preferences.IDynamicPropertyMap;

/**
 * @since 3.1
 */
public final class DefaultSimpleTabListener extends AbstractBooleanListener {

    private DefaultTabFolder folder;
    
    /**
     * @param map
     * @param propertyId
     * @param defaultValue
     */
    public DefaultSimpleTabListener(IDynamicPropertyMap map, String propertyId, DefaultTabFolder folder) {
        super();
        
        this.folder = folder;
        
        attach(map, propertyId, true);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.AbstractBooleanListener#handleValue(boolean)
     */
    protected void handleValue(boolean b) {
        folder.setSimpleTabs(b);
    }

}
