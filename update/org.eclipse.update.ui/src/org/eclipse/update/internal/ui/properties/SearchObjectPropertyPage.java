/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.properties;

import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.search.SearchObject;

public class SearchObjectPropertyPage extends NamedObjectPropertyPage {

	/**
	 * Constructor for SearchObjectPropertyPage.
	 */
	public SearchObjectPropertyPage() {
		super();
	}
	
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		SearchObject searchObject = (SearchObject)getElement();
		objectName.setEditable(!searchObject.isCategoryFixed());
		return control;
	}
}
