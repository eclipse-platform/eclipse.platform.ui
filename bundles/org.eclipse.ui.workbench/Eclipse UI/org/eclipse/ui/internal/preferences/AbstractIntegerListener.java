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
package org.eclipse.ui.internal.preferences;

/**
 * @since 3.1
 */
public abstract class AbstractIntegerListener extends AbstractPropertyListener {

    private IDynamicPropertyMap map;
    private int defaultValue;
    private String propertyId;
    
    public AbstractIntegerListener() {
    }
    
    public void attach(IDynamicPropertyMap map, String propertyId, int defaultValue) {
        this.defaultValue = defaultValue;
        this.propertyId = propertyId;
        if (this.map != null) {
            this.map.removeListener(this);
        }
        
        this.map = map;
        
        if (this.map != null) {
            this.map.addListener(new String[]{propertyId}, this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.preferences.AbstractPropertyListener#update()
     */
    protected void update() {
        handleValue(PropertyUtil.get(map, propertyId, defaultValue));
    }

    /**
     * @param b
     * @since 3.1
     */
    protected abstract void handleValue(int b);
   
    
}
