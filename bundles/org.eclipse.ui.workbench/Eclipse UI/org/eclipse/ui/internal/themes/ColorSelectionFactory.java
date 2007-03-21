/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.themes;

import java.util.Hashtable;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IColorFactory;

/**
 * ColorSelectionFactory is the abstract superclass of factories that compare
 * two colors.
 * 
 * @since 3.3
 * 
 */
public abstract class ColorSelectionFactory implements IColorFactory,
		IExecutableExtension {

	String color1;
	String color2;

	/**
	 * This executable extension requires parameters to be explicitly declared
	 * via the second method described in the <code>IExecutableExtension</code>
	 * documentation. This class expects that there will be two parameters,
	 * <code>color1</code> and <code>color2</code>, that describe the two
	 * colors to be compared. These values may either be RGB triples or SWT
	 * constants.
	 * 
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {

		if (data instanceof Hashtable) {
			Hashtable table = (Hashtable) data;
			color1 = (String) table.get("color1"); //$NON-NLS-1$
			color2 = (String) table.get("color2"); //$NON-NLS-1$            
		}
	}

	/**
	 * Return the difference of the two
	 * 
	 * @param rgb1
	 * @param rgb2
	 * @return int if the difference is positive rgb1 is darker, if negative
	 *         rgb2 is darker.
	 */
	protected int difference(RGB rgb1, RGB rgb2) {

		return (rgb1.blue - rgb2.blue) + (rgb1.red - rgb2.red)
				+ (rgb1.green - rgb2.green);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.themes.IColorFactory#createColor()
	 */
	public RGB createColor() {
		if (color1 == null && color2 == null) {
			return new RGB(0, 0, 0);
		} else if (color1 != null && color2 == null) {
			return ColorUtil.getColorValue(color1);
		} else if (color1 == null && color2 != null) {
			return ColorUtil.getColorValue(color2);
		} else {
			RGB rgb1 = ColorUtil.getColorValue(color1);
			RGB rgb2 = ColorUtil.getColorValue(color2);
			return compare(rgb1, rgb2);
		}
	}

	/**
	 * Compare the two {@link RGB} instances and return one of them.
	 * 
	 * @param rgb1
	 * @param rgb2
	 * @return {@link RGB}
	 */
	abstract RGB compare(RGB rgb1, RGB rgb2);

}
