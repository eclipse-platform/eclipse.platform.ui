/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     Kai Toedter - added radial gradient support
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Generic class to store informations to manage Gradient color.
 */
public class Gradient {

	private final List<Object> rgbs = new ArrayList<>(2);
	private final List<Integer> percents = new ArrayList<>(2);

	//TODO see bug #278077
	private final List<CSSPrimitiveValue> values = new ArrayList<>(2);

	private boolean isLinear = true;

	private boolean vertical = true;

	/* TODO: enhance Gradient with focus points */

	public void setLinear(boolean linear) {
		isLinear = linear;
	}

	public boolean isLinear() {
		return isLinear;
	}

	public boolean isRadial() {
		return !isLinear;
	}

	//TODO see bug #278077
	public void addRGB(Object rgb, CSSPrimitiveValue value) {
		rgbs.add(rgb);
		values.add(value);
	}

	public void addPercent(Integer percent) {
		percents.add(percent);
	}

	public void setVertical(boolean vertical){
		this.vertical = vertical;
	}

	public List<Object> getRGBs() {
		return rgbs;
	}

	public List<CSSPrimitiveValue> getValues() {
		return values;
	}

	public List<Integer> getPercents() {
		return percents;
	}

	public boolean getVerticalGradient() {
		return vertical;
	}
}
