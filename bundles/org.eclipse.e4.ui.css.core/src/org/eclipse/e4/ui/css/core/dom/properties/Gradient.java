/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     Kai Toedter - added radial gradient support
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic class to store informations to manage Gradiant color.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class Gradient {

	private final List rgbs = new ArrayList();
	private final List percents = new ArrayList();

	private boolean isLinear = true;

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

	public void addRGB(Object rgb) {
		rgbs.add(rgb);
	}

	public void addPercent(Integer percent) {
		percents.add(percent);
	}

	public List getRGBs() {
		return rgbs;
	}

	public List getPercents() {
		return percents;
	}
}
