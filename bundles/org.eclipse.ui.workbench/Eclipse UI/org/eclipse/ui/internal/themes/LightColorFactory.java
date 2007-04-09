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
 * LightColorFactory returns tab begin and end colours based on taking
 * a system color as input, analyzing it, and lightening it appropriately.
 * 
 * @since 3.3
 * 
 */
public class LightColorFactory implements IColorFactory,
		IExecutableExtension {

	protected static final RGB white = new RGB(255, 255, 255);
	
	String baseColorName;
	String definitionId; 

	/**
	 * This executable extension requires parameters to be explicitly declared
	 * via the second method described in the <code>IExecutableExtension</code>
	 * documentation. The following parameters are parsed:
	 * <code>base</code>, describes the base color to produce all other colours from.
	 * This value may either be an RGB triple or an SWT constant.
	 * <code>definitionId</code>, describes the id of color we are looking for, one of
	 * "org.eclipse.ui.workbench.ACTIVE_TAB_BG_START"
	 * "org.eclipse.ui.workbench.ACTIVE_TAB_BG_END"
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {

		if (data instanceof Hashtable) {
			Hashtable table = (Hashtable) data;
			baseColorName = (String) table.get("base"); //$NON-NLS-1$
			definitionId = (String) table.get("definitionId"); //$NON-NLS-1$
		}
	}

	/* 
	 * Return the number of RGB values in test that are
	 * equal to or between lower and upper.
	 */
	protected int valuesInRange(RGB test, int lower, int upper) {
		int hits = 0;
		if(test.red >= lower && test.red <= upper) hits++;
		if(test.blue >= lower && test.blue <= upper) hits++;
		if(test.green >= lower && test.green <= upper) hits++;

		return hits;
	}
	
	/*
	 * Return the RGB value for the bottom tab color
	 * based on a blend of white and sample color
	 */
	private RGB getLightenedColor(RGB sample) {
		//Group 1
		if(valuesInRange(sample, 180, 255) >= 2)
			return sample;
		
		//Group 2
		if(valuesInRange(sample, 100, 179) >= 2)
			return ColorUtil.blend(white, sample, 30);
		
		//Group 3
		if(valuesInRange(sample, 0, 99) >= 2)
			return ColorUtil.blend(white, sample, 60);
		
		//Group 4
		return ColorUtil.blend(white, sample, 30);
	}
	
	/*
	 * Return the Start (top of tab) color as an RGB
	 */
	private RGB createStartColor() {
		return ColorUtil.blend(white, createEndColor(), 75);
	}

	/*
	 * Return the End (top of tab) color as an RGB
	 */
	private RGB createEndColor() {
		return getLightenedColor(
				ColorUtil.getColorValue(baseColorName));
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.themes.IColorFactory#createColor()
	 */
	public RGB createColor() {
		//should have base, otherwise error in the xml
		if (baseColorName == null || definitionId == null) 
			return white;
		
		if (definitionId.equals("org.eclipse.ui.workbench.ACTIVE_TAB_BG_START")) //$NON-NLS-1$
			return createStartColor();
		if (definitionId.equals("org.eclipse.ui.workbench.ACTIVE_TAB_BG_END")) //$NON-NLS-1$
			return createEndColor();
		
		//should be one of start or end, otherwise error in the xml
		return white;
	}
}
