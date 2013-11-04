/*******************************************************************************
 * Copyright (c) 2008, 2010 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *
 */
public class Gradient {

	private final List rgbs = new ArrayList();
	private final List percents = new ArrayList();

	//TODO see bug #278077
	private final List values = new ArrayList();

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

	public List getRGBs() {
		return rgbs;
	}

	public List getValues() {
		return values;
	}

	public List getPercents() {
		return percents;
	}

	public boolean getVerticalGradient() {
		return vertical;
	}
}
