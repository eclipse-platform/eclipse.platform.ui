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
