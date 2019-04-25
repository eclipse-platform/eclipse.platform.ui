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
public class SliderMaximumProperty extends WidgetIntValueProperty<Slider> {
	@Override
	int doGetIntValue(Slider source) {
		return source.getMaximum();
	}

	@Override
	void doSetIntValue(Slider source, int value) {
		source.setMaximum(value);
	}

	@Override
	public String toString() {
		return "Slider.maximum <int>"; //$NON-NLS-1$
	}
}
