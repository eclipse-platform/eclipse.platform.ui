/*******************************************************************************
 * Copyright (c) 2010 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 299123)
 ******************************************************************************/

package org.eclipse.jface.internal.databinding.swt;

import org.eclipse.swt.widgets.Slider;

/**
 * 
 */
public class SliderMinimumProperty extends WidgetIntValueProperty {
	int doGetIntValue(Object source) {
		return ((Slider) source).getMinimum();
	}

	void doSetIntValue(Object source, int value) {
		((Slider) source).setMinimum(value);
	}

	public String toString() {
		return "Slider.minimum <int>"; //$NON-NLS-1$
	}
}
