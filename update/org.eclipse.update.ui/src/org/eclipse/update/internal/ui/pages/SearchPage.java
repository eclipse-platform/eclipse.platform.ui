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
package org.eclipse.update.internal.ui.pages;

import org.eclipse.update.internal.ui.forms.SearchForm;
import org.eclipse.update.internal.ui.views.DetailsView;
import org.eclipse.update.ui.forms.internal.*;


public class SearchPage extends UpdateFormPage {
	
	public SearchPage(DetailsView view, String title) {
		super(view, title);
	}
	
	public IForm createForm() {
		return new SearchForm(this);
	}
}
