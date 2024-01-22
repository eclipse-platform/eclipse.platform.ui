/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * @since 3.0
 */
public class CascadingFontRegistry extends FontRegistry {

	private FontRegistry parent;

	private IPropertyChangeListener listener = event -> {
		// check to see if we have an override for the given key. If so,
		// then a change in our parent registry shouldn't cause a change in
		// us. Without this check we will propagate a new value
		// (event.getNewValue()) to our listeners despite the fact that this
		// value is NOT our current value.
		if (!hasOverrideFor(event.getProperty()))
			fireMappingChanged(event.getProperty(), event.getOldValue(), event.getNewValue());

	};

	/**
	 * Create a new instance of this class.
	 *
	 * @param parent the parent registry
	 */
	public CascadingFontRegistry(FontRegistry parent) {
		super(Display.getCurrent(), false);
		this.parent = parent;
		parent.addListener(listener);
	}

	@Override
	public Font get(String symbolicName) {
		if (super.hasValueFor(symbolicName)) {
			return super.get(symbolicName);
		}
		return parent.get(symbolicName);
	}

	@Override
	public Set<String> getKeySet() {
		Set<String> keyUnion = new HashSet<>(super.getKeySet());
		keyUnion.addAll(parent.getKeySet());
		return keyUnion;
	}

	@Override
	public FontData[] getFontData(String symbolicName) {
		if (super.hasValueFor(symbolicName)) {
			return super.getFontData(symbolicName);
		}
		return parent.getFontData(symbolicName);
	}

	@Override
	public boolean hasValueFor(String colorKey) {
		return super.hasValueFor(colorKey) || parent.hasValueFor(colorKey);
	}

	/**
	 * Returns whether this cascading registry has an override for the provided
	 * color key.
	 *
	 * @param fontKey the provided color key
	 * @return hether this cascading registry has an override
	 */
	public boolean hasOverrideFor(String fontKey) {
		return super.hasValueFor(fontKey);
	}

	/**
	 * Disposes of all allocated resources.
	 */
	public void dispose() {
		parent.removeListener(listener);
		PlatformUI.getWorkbench().getDisplay().asyncExec(displayRunnable);
	}
}
