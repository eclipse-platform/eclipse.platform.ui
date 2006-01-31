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
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Defence;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.DefencePositionKind;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;

/**
 * A section for the position property of a selected defence player Object.
 * 
 * @author Anthony Hunter
 */
public class DefencePositionPropertySection
	extends AbstractEnumerationPropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getFeature()
	 */
	protected EAttribute getFeature() {
		return HockeyleaguePackage.eINSTANCE.getDefence_Position();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getFeatureAsText()
	 */
	protected String getFeatureAsText() {
		return ((Defence) eObject).getPosition().getName();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getFeatureValue(int)
	 */
	protected Object getFeatureValue(int index) {
		return DefencePositionKind.VALUES.get(index);
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getLabelText()
	 */
	protected String getLabelText() {
		return "Position:";//$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#isEqual(int)
	 */
	protected boolean isEqual(int index) {
		return DefencePositionKind.VALUES.get(index).equals(
			((Defence) eObject).getPosition());
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection#getEnumerationFeatureValues()
	 */
	protected String[] getEnumerationFeatureValues() {
		List values = DefencePositionKind.VALUES;
		String[] ret = new String[values.size()];
		for (int i = 0; i < values.size(); i++) {
			ret[i] = ((DefencePositionKind) values.get(i)).getName();
		}
		return ret;
	}
}