/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.search;

import org.eclipse.help.ui.internal.search.HelpCriteriaContentProvider.CriterionName;
import org.eclipse.help.ui.internal.search.HelpCriteriaContentProvider.CriterionValue;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class HelpCriteriaLabelProvider extends LabelProvider {

	/**
	 * Constructor for HelpWorkingSetElementLabelProvider.
	 */
	public HelpCriteriaLabelProvider() {
		super();
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return null;
	}

	/**
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof HelpCriteriaContentProvider.CriterionName) {
			CriterionName criterionName = (HelpCriteriaContentProvider.CriterionName)element;
			return criterionName.getName();
		} else if (element instanceof HelpCriteriaContentProvider.CriterionValue) {
			CriterionValue criterionValue = (HelpCriteriaContentProvider.CriterionValue)element;
			return criterionValue.getName();
		} 
		return null;
	}

}
