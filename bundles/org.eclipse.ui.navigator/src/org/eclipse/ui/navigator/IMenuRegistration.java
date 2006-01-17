package org.eclipse.ui.navigator;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;

/**
 * Delegate for clients to determine how their menu should be registered for
 * platform contributions.
 * 
 * <p>
 * Clients are expected to supply their implementation of this interface to {@link CommonViewerSiteFactory}
 * for the {@link CommonViewerSiteFactory#createCommonViewerSite(String, org.eclipse.ui.IWorkbenchPage, IMenuRegistration, ISelectionProvider, org.eclipse.ui.IActionBars)}
 * factory method. 
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 * @since 3.2
 * 
 */
public interface IMenuRegistration {

	/**
	 * Will delegate to an IViewSite, IEditorSite, or some other custom
	 * implementation for menu registration.
	 * 
	 * @param menuId
	 * @param menuManager
	 * @param selectionProvider
	 */
	void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider);

}
