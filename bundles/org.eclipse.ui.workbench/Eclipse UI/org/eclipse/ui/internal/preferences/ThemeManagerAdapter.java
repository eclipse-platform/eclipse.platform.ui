/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.Set;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.1
 */
public class ThemeManagerAdapter extends PropertyMapAdapter {

    private IThemeManager manager;

    private IPropertyChangeListener listener = new IPropertyChangeListener() {
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            firePropertyChange(event.getProperty());
        }
    };

    public ThemeManagerAdapter(IThemeManager manager) {
        this.manager = manager;
    }

    @Override
	protected void attachListener() {
        manager.addPropertyChangeListener(listener);
    }

    @Override
	protected void detachListener() {
        manager.removePropertyChangeListener(listener);
    }

    @Override
	public Set keySet() {
        Set result = ThemeAdapter.getKeySet(manager.getCurrentTheme());

        return result;
    }

    @Override
	public Object getValue(String propertyId, Class propertyType) {
        return ThemeAdapter.getValue(manager.getCurrentTheme(), propertyId, propertyType);
    }

    @Override
	public boolean propertyExists(String propertyId) {
        return keySet().contains(propertyId);
    }

    @Override
	public void setValue(String propertyId, Object newValue) {
        throw new UnsupportedOperationException();
    }

}
