/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.ui;

import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.search.internal.ui.OpenSearchDialogAction;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.internal.ui.SearchPreferencePage;

/**
 * The central class for access to the Search Plug-in's User Interface. 
 * This class cannot be instantiated; all functionality is provided by 
 * static methods.
 * 
 * Features provided:
 * <ul>
 * <li>convenient access to the search result view of the active workbench
 *   window.</li>
 * </ul>
 *
 * @see ISearchResultView
 */
public final class SearchUI {

	/**
	 * Search Plug-in Id (value <code>"org.eclipse.search"</code>).
	 */
	public static final String PLUGIN_ID= "org.eclipse.search"; //$NON-NLS-1$

	/** 
	 * Search marker type (value <code>"org.eclipse.search.searchmarker"</code>).
	 *
	 * @see org.eclipse.core.resources.IMarker
	 */ 
	public static final String SEARCH_MARKER=  PLUGIN_ID + ".searchmarker"; //$NON-NLS-1$

	/** 
	 * Line marker attribute (value <code>"line"</code>)
	 * The value of the marker attribute is the line which contains the text search match.
	 *
	 * @see org.eclipse.core.resources.IMarker#getAttribute
	 */
	public static final String LINE= "line"; //$NON-NLS-1$

	/** 
	 * Potential match marker attribute
	 * (value <code>"potentialMatch"</code>).
	 *  <p>
	 * This optional marker attribute tells whether a marker is
	 * a potential or an exact match.
	 * The marker is considered an exact match if the attribute is missing.
	 * </p>
	 * <p>
	 * Potential matches are shown with a different background color in
	 * the Search view. The color can be changed in the Search preferences.
	 * </p>
	 *
	 * @see org.eclipse.core.resources.IMarker#getAttribute
	 * @since 2.0
	 */
	public static final String POTENTIAL_MATCH= "potentialMatch"; //$NON-NLS-1$

	/** 
	 * Id of the Search result view
	 * (value <code>"org.eclipse.search.SearchResultView"</code>).
	 */
	public static final String SEARCH_RESULT_VIEW_ID= "org.eclipse.search.SearchResultView"; //$NON-NLS-1$

	/**
	 * Id of the Search action set
	 * (value <code>"org.eclipse.search.searchActionSet"</code>).
	 *
	 * @since 2.0
	 */
	public static final String ACTION_SET_ID= PLUGIN_ID + ".searchActionSet"; //$NON-NLS-1$

	/**
	 * Activates the search result view in the active page of the
	 * active workbench window. This call has no effect (but returns <code>true</code>
	 * if the search result view is already activated.
	 *
	 * @return <code>true</code> if the search result view could be activated
	 */
	public static boolean activateSearchResultView() {
		return SearchPlugin.activateSearchResultView();	
	}		

	/**
	 * Opens the search dialog.
	 * If <code>pageId</code> is specified and a corresponding page
	 * is found then it is brought to top.
	 *
	 * @param pageId	the page to select or <code>null</code>
	 * 					if the best fitting page should be selected
	 * @since 2.0
	 */
	public static void openSearchDialog(IWorkbenchWindow window, String pageId) {
		new OpenSearchDialogAction(window, pageId).run();
	}		

	/**
	 * Returns the search result view of the active page of the
	 * active workbench window.
	 *
	 * @return	the search result view or <code>null</code>
	 * 		if there is no active search result view
	 */
	public static ISearchResultView getSearchResultView() {
		return SearchPlugin.getSearchResultView();	
	}

	/**
	 * Returns the shared search marker image.
	 * Normally, editors show this icon in their vertical ruler.
	 * This image is owned by the search UI plug-in and must not be disposed
	 * by clients.
	 *
	 * @return the shared image
	 */
	public static Image getSearchMarkerImage() {
		return SearchPluginImages.get(SearchPluginImages.IMG_OBJ_SEARCHMARKER);
	}

	/**
	 * Returns the preference whether editors should be reused
	 * when showing search results.
	 * 
	 * The goto action can decide to use or ignore this preference.
	 *
	 * <p>
	 * [Issue: Work in progress - not yet stable.]
	 * </p>
	 * <p>
	 * [Issue: Always returns <code>true</code> yet due to bug 6784.]
	 * </p>
	 * <p>
	 * [Issue: Bug is now fixed. But because it is not yet clear if
	 *         old style should be supported. Therefore returning <code>false</code>]
	 * </p>
	 * 
	 * @return <code>true</code> if editors should be reused for showing search results
	 * @since 2.0
	 */
	public static boolean reuseEditor() {
		return SearchPreferencePage.isEditorReused();
	}

	/**
	 * Block instantiation.
	 */
	private SearchUI() {
	}
}
