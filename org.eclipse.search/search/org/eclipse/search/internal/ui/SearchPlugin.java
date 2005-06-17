/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.internal.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.search.ui.IContextMenuConstants;
import org.eclipse.search.ui.ISearchResultView;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.util.ExceptionHandler;

import org.eclipse.search2.internal.ui.InternalSearchUI;

/**
 * The plug-in runtime class for Search plug-in
 */
public class SearchPlugin extends AbstractUIPlugin {
	
	public static final String SEARCH_PAGE_EXTENSION_POINT= "searchPages"; //$NON-NLS-1$
	public static final String SORTER_EXTENSION_POINT= "searchResultSorters"; //$NON-NLS-1$

	/** 
	 * Filtered search marker type (value <code>"org.eclipse.search.filteredsearchmarker"</code>).
	 *
	 * @see org.eclipse.core.resources.IMarker
	 */ 
	public static final String FILTERED_SEARCH_MARKER=  NewSearchUI.PLUGIN_ID + ".filteredsearchmarker"; //$NON-NLS-1$

	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR= 1;
	
	private static SearchPlugin fgSearchPlugin;
			
	private List fPageDescriptors;
	private List fSorterDescriptors;
	

	public SearchPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		Assert.isTrue(fgSearchPlugin == null);
		fgSearchPlugin= this;
	}

	/**
	 * Returns the search plugin instance.
	 */
	public static SearchPlugin getDefault() {
		return fgSearchPlugin;
	}

	/*
	 * Overrides AbstractUIPlugin#initializeDefaultPreferences
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		SearchPreferencePage.initDefaults(store);
	}
	
	/**
	 * Returns the active workbench window.
	 * <code>null</code> if the active window is not a workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbenchWindow window= fgSearchPlugin.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			final WindowRef windowRef= new WindowRef();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					setActiveWorkbenchWindow(windowRef);
				}
			});
			return windowRef.window;
		}
		else
			return window;
	}

	private static class WindowRef {
		public IWorkbenchWindow window;
	}

	private static void setActiveWorkbenchWindow(WindowRef windowRef) {
		windowRef.window= null;
		Display display= Display.getCurrent();
		if (display == null)
			return;
		Control shell= display.getActiveShell();
		while (shell != null) {
			Object data= shell.getData();
			if (data instanceof IWorkbenchWindow) {
				windowRef.window= (IWorkbenchWindow)data;
				return;
			}
			shell= shell.getParent();
		}
		Shell shells[]= display.getShells();
		for (int i= 0; i < shells.length; i++) {
			Object data= shells[i].getData();
			if (data instanceof IWorkbenchWindow) {
				windowRef.window= (IWorkbenchWindow)data;
				return;
			}
		}
	}

	/**
	 * Returns the shell of the active workbench window.
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null)
			return window.getShell();
		return null;
	}

	/**
	 * Beeps using the display of the active workbench window.
	 */
	public static void beep() {
		getActiveWorkbenchShell().getDisplay().beep();
	}

	/**
	 * Returns the active workbench window's currrent page.
	 */
	public static IWorkbenchPage getActivePage() {
		return getActiveWorkbenchWindow().getActivePage();
	} 

	/**
	 * Returns the workbench from which this plugin has been loaded.
	 */	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Activates the search result view in the active page.
	 * This call has no effect, if the search result view is
	 * already activated.
	 *
	 * @return <code>true</code> if the search result view could be activated
	 */
	public static boolean activateSearchResultView() {

		String defaultPerspectiveId= SearchUI.getDefaultPerspectiveId();
		if (defaultPerspectiveId != null) {
			IWorkbenchWindow window= window= getActiveWorkbenchWindow();
			if (window != null && window.getShell() != null && !window.getShell().isDisposed()) {
				try {
					PlatformUI.getWorkbench().showPerspective(defaultPerspectiveId, window);
				} catch (WorkbenchException ex) {
					// show view in current perspective
				}
			}
		}

		try {
			IViewPart viewPart= getActivePage().findView(SearchUI.SEARCH_RESULT_VIEW_ID);
			if (viewPart == null || SearchPreferencePage.isViewBroughtToFront()) {
				return (getActivePage().showView(SearchUI.SEARCH_RESULT_VIEW_ID) != null);
			}
			return true;
		} catch (PartInitException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_openResultView_title, SearchMessages.Search_Error_openResultView_message); 
			return false;
		}	
	}

	/**
	 * Returns the search result view of the active workbench window. Returns <code>
	 * null</code> if the active workbench window doesn't have any search result
	 * view.
	 */
	public static ISearchResultView getSearchResultView() {
		IViewPart part= getActivePage().findView(SearchUI.SEARCH_RESULT_VIEW_ID);
		if (part instanceof ISearchResultView)
			return (ISearchResultView) part;
		return null;	
	}

	static boolean setAutoBuilding(boolean state) {
		IWorkspaceDescription workspaceDesc= getWorkspace().getDescription();
		boolean isAutobuilding= workspaceDesc.isAutoBuilding();
		
		if (isAutobuilding != state) {
			workspaceDesc.setAutoBuilding(state);
			try {
				getWorkspace().setDescription(workspaceDesc);
			}
			catch (CoreException ex) {
				ExceptionHandler.handle(ex, SearchMessages.Search_Error_setDescription_title, SearchMessages.Search_Error_setDescription_message); 
			}
		}
		return isAutobuilding;
	}

	/**
	 * Shuts down this plug-in.
	 */
	public void shutdown() throws CoreException {
		InternalSearchUI.shutdown();
		getWorkspace().removeResourceChangeListener(SearchManager.getDefault());
		super.shutdown();
		fgSearchPlugin = null;
	}

	/**
	 * Returns all search pages contributed to the workbench.
	 */
	public List getSearchPageDescriptors() {
		if (fPageDescriptors == null) {
			IPluginRegistry registry= Platform.getPluginRegistry();
			IConfigurationElement[] elements= registry.getConfigurationElementsFor(NewSearchUI.PLUGIN_ID, SEARCH_PAGE_EXTENSION_POINT);
			fPageDescriptors= createSearchPageDescriptors(elements);
		}	
		return fPageDescriptors;
	} 

	/**
	 * Returns all search pages contributed to the workbench.
	 */
	public List getEnabledSearchPageDescriptors(String pageId) {
		Iterator iter= getSearchPageDescriptors().iterator();
		List enabledDescriptors= new ArrayList(5);
		while (iter.hasNext()) {
			SearchPageDescriptor desc= (SearchPageDescriptor)iter.next();
			if (desc.isEnabled() || desc.getId().equals(pageId))
				enabledDescriptors.add(desc);
		}
		return enabledDescriptors;
	} 

	/**
	 * Returns the help context ID for the Search view
	 * as provided by the current search page extension.
	 * 
	 * @since 3.0
	 */
	public String getSearchViewHelpContextId() {
		Search currentSearch= SearchManager.getDefault().getCurrentSearch();
		if (currentSearch != null) {
			String pageId= currentSearch.getPageId();
			Iterator iter= getSearchPageDescriptors().iterator();
			while (iter.hasNext()) {
				SearchPageDescriptor desc= (SearchPageDescriptor)iter.next();
				if (desc.getId().equals(pageId)) {
					String helpId= desc.getSearchViewHelpContextId();
					if (helpId == null)
						return ISearchHelpContextIds.SEARCH_VIEW;
					else
						return desc.getSearchViewHelpContextId();
				}
			}
		}
		return ISearchHelpContextIds.SEARCH_VIEW;
	} 

	/**
	 * Creates all necessary search page nodes.
	 */
	private List createSearchPageDescriptors(IConfigurationElement[] elements) {
		List result= new ArrayList(5);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (SearchPageDescriptor.PAGE_TAG.equals(element.getName())) {
				SearchPageDescriptor desc= new SearchPageDescriptor(element);
				result.add(desc);
			}
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Returns all sorters contributed to the workbench.
	 */
	public List getSorterDescriptors() {
		if (fSorterDescriptors == null) {
			IPluginRegistry registry= Platform.getPluginRegistry();
			IConfigurationElement[] elements= registry.getConfigurationElementsFor(SearchUI.PLUGIN_ID, SORTER_EXTENSION_POINT);
			fSorterDescriptors= createSorterDescriptors(elements);
		}	
		return fSorterDescriptors;
	} 

	/**
	 * Creates all necessary sorter description nodes.
	 */
	private List createSorterDescriptors(IConfigurationElement[] elements) {
		List result= new ArrayList(5);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (SorterDescriptor.SORTER_TAG.equals(element.getName()))
				result.add(new SorterDescriptor(element));
		}
		return result;
	}

	/**
	 * Log status to platform log
	 */	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, INTERNAL_ERROR, SearchMessages.SearchPlugin_internal_error, e)); 
	}
	
	public static String getID() {
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	/**
	 * Creates the Search plugin standard groups in a context menu.
	 */
	public static void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty())
			return;
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}
}
