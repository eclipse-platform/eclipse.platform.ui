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
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleagueFactory;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.League;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Team;

/**
 * The teams section in the teams tab for a league.
 * 
 * @author Anthony Hunter
 */
public class TeamsPropertySection
	extends AbstractTablePropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getColumnLabelText()
	 */
	protected List getColumnLabelText() {
		ArrayList ret = new ArrayList();
		ret.add("Name");//$NON-NLS-1$
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getKeyForRow(java.lang.Object)
	 */
	protected String getKeyForRow(Object object) {
		return ((Team) object).getName();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getValuesForRow(java.lang.Object)
	 */
	protected List getValuesForRow(Object object) {
		ArrayList ret = new ArrayList();
		ret.add(((Team) object).getName());
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getButtonLabelText()
	 */
	protected String getButtonLabelText() {
		return "Team";//$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getOwnedRows()
	 */
	protected List getOwnedRows() {
		ArrayList ret = new ArrayList();
		for (Iterator i = ((League) eObject).getTeams().iterator(); i.hasNext();) {
			ret.add(i.next());
		}
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getFeature()
	 */
	protected EReference getFeature() {
		return HockeyleaguePackage.eINSTANCE.getLeague_Teams();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getNewChild()
	 */
	protected Object getNewChild() {
		return HockeyleagueFactory.eINSTANCE.createTeam();
	}
}