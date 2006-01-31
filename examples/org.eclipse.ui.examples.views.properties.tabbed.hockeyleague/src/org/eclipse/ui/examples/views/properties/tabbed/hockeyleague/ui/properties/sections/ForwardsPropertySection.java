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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Forward;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueFactory;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team;


/**
 * The forwards section.
 * 
 * @author Anthony Hunter
 */
public class ForwardsPropertySection
	extends AbstractPlayersPropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getButtonLabelText()
	 */
	protected String getButtonLabelText() {
		return "Forward";//$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getOwnedRows()
	 */
	protected List getOwnedRows() {
		ArrayList ret = new ArrayList();
		for (Iterator i = ((Team) eObject).getForwards().iterator(); i
			.hasNext();) {
			ret.add(i.next());
		}
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractPlayersPropertySection#getAdditionalColumnLabelText()
	 */
	protected List getAdditionalColumnLabelText() {
		ArrayList ret = new ArrayList();
		ret.add("Position");//$NON-NLS-1$
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractPlayersPropertySection#getAdditionalValuesForRow(java.lang.Object)
	 */
	protected List getAdditionalValuesForRow(Object object) {
		ArrayList ret = new ArrayList();
		ret.add(((Forward) object).getPosition().getName());
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getFeature()
	 */
	protected EReference getFeature() {
		return HockeyleaguePackage.eINSTANCE.getTeam_Forwards();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getNewChild()
	 */
	protected Object getNewChild() {
		return HockeyleagueFactory.eINSTANCE.createForward();
	}
}