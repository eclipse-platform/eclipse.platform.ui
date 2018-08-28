/*******************************************************************************
 * Copyright (c) 2010, 2015 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	@Override
	int doGetIntValue(Object source) {
		return ((Slider) source).getMinimum();
	}

	@Override
	void doSetIntValue(Object source, int value) {
		((Slider) source).setMinimum(value);
	}

	@Override
	public String toString() {
		return "Slider.minimum <int>"; //$NON-NLS-1$
	}
}
