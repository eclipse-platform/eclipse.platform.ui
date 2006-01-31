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
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ShotKind;

/**
 * A section for the shot property of a selected player Object.
 * 
 * @author Anthony Hunter
 */
public class ShotPropertySection
	extends AbstractEnumerationPropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getFeature()
	 */
	protected EAttribute getFeature() {
		return HockeyleaguePackage.eINSTANCE.getPlayer_Shot();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getFeatureAsText()
	 */
	protected String getFeatureAsText() {
		return ((Player) eObject).getShot().getName();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getFeatureValue(int)
	 */
	protected Object getFeatureValue(int index) {
		return ShotKind.VALUES.get(index);
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getLabelText()
	 */
	protected String getLabelText() {
		return "Shot:";//$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#isEqual(int)
	 */
	protected boolean isEqual(int index) {
		return ShotKind.VALUES.get(index).equals(((Player) eObject).getShot());
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getEnumerationFeatureValues()
	 */
	protected String[] getEnumerationFeatureValues() {
		List values = ShotKind.VALUES;
		String[] ret = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			ret[i] = ((ShotKind) values.get(i)).getName();
		}
		return ret;
	}
}