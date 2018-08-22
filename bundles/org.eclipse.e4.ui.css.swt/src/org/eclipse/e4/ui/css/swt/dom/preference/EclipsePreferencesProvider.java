/*******************************************************************************
 * Copyright (c) 2014, 2016 IBM Corporation and others.
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
package org.eclipse.e4.ui.css.swt.dom.preference;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;

public class EclipsePreferencesProvider implements IElementProvider {
	@Override
	public Element getElement(Object element, CSSEngine engine) {
		if (element instanceof IEclipsePreferences) {
			return new EclipsePreferencesElement((IEclipsePreferences) element, engine);
		}
		return null;
	}

}
