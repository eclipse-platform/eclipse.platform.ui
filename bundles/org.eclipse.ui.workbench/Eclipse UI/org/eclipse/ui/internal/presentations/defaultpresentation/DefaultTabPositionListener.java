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

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.preferences.AbstractIntegerListener;
import org.eclipse.ui.internal.preferences.IDynamicPropertyMap;

/**
 * @since 3.1
 */
public final class DefaultTabPositionListener extends AbstractIntegerListener {

    private DefaultTabFolder folder;
    
    public DefaultTabPositionListener(IDynamicPropertyMap map, String preferenceId, DefaultTabFolder folder) {
        this.folder = folder;
        attach(map, preferenceId, SWT.TOP);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.AbstractBooleanListener#handleValue(boolean)
     */
    protected void handleValue(int position) {
        folder.setTabPosition(position);
    }

}
