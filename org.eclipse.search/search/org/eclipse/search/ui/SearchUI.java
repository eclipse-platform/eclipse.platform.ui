/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.ui;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.ui.dialogs.SelectionDialog;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPluginImages;
import org.eclipse.search.internal.workingsets.WorkingSet;
import org.eclipse.search.internal.workingsets.WorkingSetSelectionDialog;

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
	 * Id of the Search result view
	 * (value <code>"org.eclipse.search.SearchResultView"</code>).
	 */
	public static final String SEARCH_RESULT_VIEW_ID= PLUGIN_ID + ".SearchResultView"; //$NON-NLS-1$

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
	 * Creates a selection dialog that lists all working sets and allows to
	 * add and edit working sets.
	 * The caller is responsible for opening the dialog with <code>Window.open</code>,
	 * and subsequently extracting the selected working sets (of type
	 * <code>IWorkingSet</code>) via <code>SelectionDialog.getResult</code>.
	 * <p>
	 * This method is for internal use only due to issue below. Once
	 * the issues is solved there will be an official API.
	 * </p>
	 * <p>
	 * [Issue: Working set must be provided by platform.]
	 * </p>
	 * 
	 * @param parent the parent shell of the dialog to be created
	 * @return a new selection dialog or <code>null</code> if none available
	 * @since 2.0
	 * @deprecated use org.eclipse.ui.IWorkingSet support - this method will be removed soon
	 */
	public static SelectionDialog createWorkingSetDialog(Shell parent) {
		return new WorkingSetSelectionDialog(parent);
	}

	/**
	 * Returns all working sets for the workspace.
	 *
	 * This method is for internal use only due to issue below. Once
	 * the issues is solved there will be an official API.
	 * </p>
	 * <p>
	 * [Issue: Working set must be provided by platform.]
	 * </p>
	 * 
	 * @return an array of IWorkingSet
	 * @since 2.0
	 * @deprecated use org.eclipse.ui.IWorkingSet support - this method will be removed soon
	 */
	public static IWorkingSet[] getWorkingSets() {
		return WorkingSet.getWorkingSets();
	}

	/**
	 * Returns a working set by name.
	 *
	 * This method is for internal use only due to issue below. Once
	 * the issues is solved there will be an official API.
	 * </p>
	 * <p>
	 * [Issue: Working set must be provided by platform.]
	 * </p>
	 * 
	 * @param name the name the working set
	 * @return the working set with the given name or <code>null</code> if not found
	 * @since 2.0
	 * @deprecated use org.eclipse.ui.IWorkingSet support - this method will be removed soon
	 */
	public static IWorkingSet findWorkingSet(String name) {
		return WorkingSet.find(name);
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
	 * [Issue: Bug is now fixed and method. But because it is not yet clear if
	 *         old style should be supported. Therefore returning <code>false</code>]
	 * </p>
	 * 
	 * @return <code>true</code> if editors should be reused for showing search results
	 * @since 2.0
	 */
	public static boolean reuseEditor() {
		return false;
	}

	/**
	 * Block instantiation.
	 */
	private SearchUI() {
	}
}
