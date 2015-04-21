/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Set;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.themes.ITheme;

/**
 * @since 3.0
 */
public class CascadingTheme extends EventManager implements ITheme {

    private CascadingFontRegistry fontRegistry;

    private CascadingColorRegistry colorRegistry;

    private ITheme currentTheme;

    private IPropertyChangeListener listener = new IPropertyChangeListener() {

        @Override
		public void propertyChange(PropertyChangeEvent event) {
            fire(event);
        }
    };

    /**
     * @param colorRegistry
     * @param fontRegistry
     */
    public CascadingTheme(ITheme currentTheme,
            CascadingColorRegistry colorRegistry,
            CascadingFontRegistry fontRegistry) {
        this.currentTheme = currentTheme;
        this.colorRegistry = colorRegistry;
        this.fontRegistry = fontRegistry;

        fontRegistry.addListener(listener);
        colorRegistry.addListener(listener);
    }

    /**
     * @param event
     */
    protected void fire(PropertyChangeEvent event) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyChangeListener) listeners[i]).propertyChange(event);
        }
    }

    @Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
        addListenerObject(listener);
    }

    @Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
        removeListenerObject(listener);
    }

    @Override
	public String getId() {
        return currentTheme.getId();
    }

    @Override
	public String getLabel() {
        return currentTheme.getLabel();
    }

    @Override
	public ColorRegistry getColorRegistry() {
        return colorRegistry;
    }

    @Override
	public FontRegistry getFontRegistry() {
        return fontRegistry;
    }

    @Override
	public void dispose() {
        colorRegistry.removeListener(listener);
        fontRegistry.removeListener(listener);
    }

    @Override
	public String getString(String key) {
        return currentTheme.getString(key);
    }

    @Override
	public int getInt(String key) {
        return currentTheme.getInt(key);
    }

    @Override
	public boolean getBoolean(String key) {
        return currentTheme.getBoolean(key);
    }

    @Override
	public Set keySet() {
        return currentTheme.keySet();
    }

}
