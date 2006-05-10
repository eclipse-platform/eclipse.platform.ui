/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.ui.dialogs.PropertyPage;

public abstract class CVSPropertiesPage extends PropertyPage {

	/**
	 * Return the appropriate Tag label for properties pages
	 * based on the tag type.
	 * @param tag
	 * @return String
	 */
	
	public static String getTagLabel(CVSTag tag) {
	
	if (tag == null) {
		return CVSUIMessages.CVSFilePropertiesPage_none; 
	}
	
	switch (tag.getType()) {
		case CVSTag.HEAD:
			return tag.getName();
		case CVSTag.VERSION:
			return NLS.bind(CVSUIMessages.CVSFilePropertiesPage_version, new String[] { tag.getName() }); 
		case CVSTag.BRANCH:
			return NLS.bind(CVSUIMessages.CVSFilePropertiesPage_branch, new String[] { tag.getName() }); 
		case CVSTag.DATE:
			return NLS.bind(CVSUIMessages.CVSFilePropertiesPage_date, new String[] { tag.getName() }); 
		default :
			return tag.getName();
		}
	}
}
