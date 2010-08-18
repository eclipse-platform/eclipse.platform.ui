/*******************************************************************************
 * Copyright (c) 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 280157)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.ScrollBar;

/**
 * 
 */
public class ScrollBarEnabledProperty extends WidgetBooleanValueProperty {
	public boolean doGetBooleanValue(Object source) {
		return ((ScrollBar) source).getEnabled();
	}

	void doSetBooleanValue(Object source, boolean value) {
		((ScrollBar) source).setEnabled(value);
	}

	public String toString() {
		return "ScrollBar.enabled <boolean>"; //$NON-NLS-1$
	}
}
