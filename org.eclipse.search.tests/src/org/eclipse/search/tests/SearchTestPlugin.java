/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests;

import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search2.internal.ui.SearchView;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Plugin class for search tests.
 */
public class SearchTestPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static SearchTestPlugin fgPlugin;
	
	public SearchTestPlugin() {
		fgPlugin = this;
	}

	public static SearchTestPlugin getDefault() {
		return fgPlugin;
	}
		
	public SearchView getSearchView() {
		return (SearchView) NewSearchUI.activateSearchResultView();
	}
	
}
