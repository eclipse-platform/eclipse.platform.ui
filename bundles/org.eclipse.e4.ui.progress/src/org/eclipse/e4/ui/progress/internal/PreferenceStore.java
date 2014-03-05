/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.progress.internal;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

public class PreferenceStore implements IPreferenceStore {

	@Override
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
    }

	@Override
    public boolean contains(String name) {
		return false;
    }

	@Override
    public void firePropertyChangeEvent(String name, Object oldValue,
            Object newValue) {
    }

	@Override
    public boolean getBoolean(String name) {
		return Preferences.getBoolean(name);
    }

	@Override
    public boolean getDefaultBoolean(String name) {
	    return false;
    }

	@Override
    public double getDefaultDouble(String name) {
		return 0;
    }

	@Override
    public float getDefaultFloat(String name) {
		return 0;
    }

	@Override
    public int getDefaultInt(String name) {
		return 0;
    }

	@Override
    public long getDefaultLong(String name) {
		return 0;
    }

	@Override
    public String getDefaultString(String name) {
		return ""; //$NON-NLS-1$
    }

	@Override
    public double getDouble(String name) {
		return 0;
    }

	@Override
    public float getFloat(String name) {
		return 0;
    }

	@Override
    public int getInt(String name) {
		return 0;
    }

	@Override
    public long getLong(String name) {
		return 0;
    }

	@Override
    public String getString(String name) {
		return ""; //$NON-NLS-1$
    }

	@Override
    public boolean isDefault(String name) {
		return false;
    }

	@Override
    public boolean needsSaving() {
		return false;
    }

	@Override
    public void putValue(String name, String value) {
		Preferences.set(name, value);
    }

	@Override
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
    }

	@Override
    public void setDefault(String name, double value) {
    }

	@Override
    public void setDefault(String name, float value) {
    }

	@Override
    public void setDefault(String name, int value) {
    }

	@Override
    public void setDefault(String name, long value) {
    }

	@Override
    public void setDefault(String name, String defaultObject) {
    }

	@Override
    public void setDefault(String name, boolean value) {
    }

	@Override
    public void setToDefault(String name) {
    	Preferences.set(name, false);
    }

	@Override
    public void setValue(String name, double value) {
    }

	@Override
    public void setValue(String name, float value) {
    }

	@Override
    public void setValue(String name, int value) {
    }

	@Override
    public void setValue(String name, long value) {
    }

	@Override
    public void setValue(String name, String value) {
    }

	@Override
    public void setValue(String name, boolean value) {
	    Preferences.set(name, value);
    }

}
