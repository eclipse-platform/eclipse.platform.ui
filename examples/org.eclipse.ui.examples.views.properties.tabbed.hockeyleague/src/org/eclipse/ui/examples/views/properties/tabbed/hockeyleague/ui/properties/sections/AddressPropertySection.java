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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.HockeyleaguePackage;

/**
 * A section for the address property of a selected Arena Object.
 * 
 * @author Anthony Hunter
 */
public class AddressPropertySection
	extends AbstractStringPropertySection {

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractStringPropertySection#getFeature()
	 */
	protected EAttribute getFeature() {
		return HockeyleaguePackage.eINSTANCE.getArena_Address();
	}

	/**
	 * @see org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractStringPropertySection#getLabelText()
	 */
	protected String getLabelText() {
		return "Address:"; //$NON-NLS-1$
	}

}
