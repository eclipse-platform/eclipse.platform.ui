package org.eclipse.ui.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * 
 * Provides context for extensions including a valid shell, a selection
 * provider, and a unique identifer corresponding to the abstract viewer behind
 * the viewer site.
 * 
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @since 3.2
 */
public interface ICommonViewerSite extends IAdaptable {

	/**
	 * 
	 * @return The unique identifier associated with the defined abstract
	 *         viewer. In general, this will be the id of the
	 *         <b>org.eclipse.ui.views</b> extension that defines the view
	 *         part.
	 */
	String getId();

	/**
	 * 
	 * @return A workbench window corresponding to the container of the
	 *         {@link CommonViewer}
	 */
	IWorkbenchWindow getWorkbenchWindow();

	/**
	 * 
	 * @return A valid shell corresponding to the shell of the
	 *         {@link CommonViewer}
	 */
	Shell getShell();

	/**
	 * 
	 * @return The selection provider that can provide a current, valid
	 *         selection. The default selection provider is the
	 *         {@link CommonViewer}.
	 */
	ISelectionProvider getSelectionProvider();

	/**
	 * Sets the selection provider for this common viewer site.
	 * 
	 * @param provider
	 *            the selection provider, or <code>null</code> to clear it
	 */
	public void setSelectionProvider(ISelectionProvider provider);
	
    /**
     * Returns the page corresponding to this viewer site.
     *
     * @return the page corresponding to this viewer site
     */
    public IWorkbenchPage getPage();

	/**
	 * Registers a pop-up menu with a particular id for extension.
	 * <p>
	 * Within the workbench one plug-in may extend the pop-up menus for a view
	 * or editor within another plug-in. In order to be eligible for extension,
	 * the menu must be registered by calling <code>registerContextMenu</code>.
	 * Once this has been done the workbench will automatically insert any
	 * action extensions which exist.
	 * </p>
	 * <p>
	 * A unique menu id must be provided for each registered menu. This id
	 * should be published in the Javadoc for the page.
	 * </p>
	 * <p>
	 * Any pop-up menu which is registered with the workbench should also define
	 * a <code>GroupMarker</code> in the registered menu with id
	 * <code>IWorkbenchActionConstants.MB_ADDITIONS</code>. Other plug-ins
	 * will use this group as a reference point for insertion. The marker should
	 * be defined at an appropriate location within the menu for insertion.
	 * </p>
	 * 
	 * @param menuId
	 *            the menu id
	 * @param menuManager
	 *            the menu manager
	 * @param selectionProvider
	 *            the selection provider
	 */
	void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider);

	/**
	 * Returns the action bars for this page site. Pages have exclusive use of
	 * their site's action bars.
	 * 
	 * @return the action bars
	 */
	IActionBars getActionBars();
}
