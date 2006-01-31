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
import java.util.List;

import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.Player;

/**
 * The abstract implementation for a players table section.
 * 
 * @author Anthony Hunter
 */
public abstract class AbstractPlayersPropertySection
	extends AbstractTablePropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getColumnLabelText()
	 */
	protected List getColumnLabelText() {
		ArrayList ret = new ArrayList();
		ret.add("Name");//$NON-NLS-1$
		ret.add("Jersey Number");//$NON-NLS-1$
		ret.add("Shot");//$NON-NLS-1$
		ret.addAll(getAdditionalColumnLabelText());
		return ret;
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getKeyForRow(java.lang.Object)
	 */
	protected String getKeyForRow(Object object) {
		return ((Player) object).getName();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection#getValuesForRow(java.lang.Object)
	 */
	protected List getValuesForRow(Object object) {
		ArrayList ret = new ArrayList();
		ret.add(((Player) object).getName());
		ret.add(Integer.toString(((Player) object).getNumber()));
		ret.add(((Player) object).getShot().getName());
		ret.addAll(getAdditionalValuesForRow(object));
		return ret;
	}

	/**
	 * Get the labels for the additional columns for the table.
	 * 
	 * @return the labels for the columns.
	 */
	protected abstract List getAdditionalColumnLabelText();
	
	/**
	 * Get the additional values for the row in the table.
	 * 
	 * @param object
	 *            an object in the row of the table.
	 * @return the list of string values for the row.
	 */
	protected abstract List getAdditionalValuesForRow(Object object);

}