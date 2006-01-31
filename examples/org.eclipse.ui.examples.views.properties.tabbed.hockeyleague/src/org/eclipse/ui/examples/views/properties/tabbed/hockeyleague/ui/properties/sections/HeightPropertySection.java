/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HeightKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player;

/**
 * A section for the height property of a selected player Object.
 * 
 * @author Anthony Hunter
 */
public class HeightPropertySection
	extends AbstractMeasurementPropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractMeasurementPropertySection#isEnumerationEqual(int)
	 */
	protected boolean isEnumerationEqual(int index) {
		return HeightKind.VALUES.get(index).equals(
			((Player) eObject).getHeightMesurement());
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractMeasurementPropertySection#getEnumerationFeature()
	 */
	protected EAttribute getEnumerationFeature() {
		return HockeyleaguePackage.eINSTANCE.getPlayer_HeightMesurement();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractMeasurementPropertySection#getEnumerationLabels()
	 */
	protected String[] getEnumerationLabels() {
		List values = HeightKind.VALUES;
		String[] ret = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			ret[i] = ((HeightKind) values.get(i)).getName();
		}
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractMeasurementPropertySection#getEnumerationIndex()
	 */
	protected int getEnumerationIndex() {
		return ((Player) eObject).getHeightMesurement().getValue();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractMeasurementPropertySection#getEnumerationFeatureValue(int)
	 */
	protected Object getEnumerationFeatureValue(int index) {
		return HeightKind.VALUES.get(index);
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractIntegerPropertySection#getFeatureInteger()
	 */
	protected Integer getFeatureInteger() {
		return new Integer(((Player) eObject).getHeightValue());
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTextPropertySection#getFeature()
	 */
	protected EAttribute getFeature() {
		return HockeyleaguePackage.eINSTANCE.getPlayer_HeightValue();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTextPropertySection#getLabelText()
	 */
	protected String getLabelText() {
		return "Height:";//$NON-NLS-1$
	}
}