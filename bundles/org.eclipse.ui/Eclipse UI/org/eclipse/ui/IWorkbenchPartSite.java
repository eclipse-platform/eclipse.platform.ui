package org.eclipse.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;

/**
 * The primary interface between a workbench part and the outside world.
 * <p>
 * This interface is not intended to be implemented or extended by clients.
 * </p>
 */
public interface IWorkbenchPartSite {
	
/**
 * Returns the decorator manager.
 * This site's part can ask the decorator manager to apply decorations 
 * (text and image modifications) to elements it displays.
 * <p>
 * The part should come up with the text and image for the element (including
 * any of the part's own decorations) before calling the decorator manager.
 * It should also add a listener to be notified when decorations change.
 * </p>
 * <p>
 * Note that if the element implements <code>IAdaptable</code>, decorators may use this
 * mechanism to obtain an adapter (for example an <code>IResource</code>), and derive the
 * decoration from the adapter rather than the element.
 * Since the adapter may differ from the part's element, the part should be prepared 
 * to handle notification that the decoration for the adapter has changed, in addition to 
 * handling notification that the decoration for the element has changed.
 * That is, it needs to be able to map back from the adapter to the element.
 * </p>
 * 
 * @return the decorator manager
 * @deprecated use IWorkbench.getDecoratorManager() instead
 * <p>
 * NOTE: This is experimental API, which may be changed or removed at any point in time.
 * This API should not be called, overridden or otherwise used in production code.
 * </p>
 */
public ILabelDecorator getDecoratorManager();

/**
 * Returns the part registry extension id for this workbench site's part.
 * <p>
 * The name comes from the <code>id</code> attribute in the configuration
 * element.
 * </p>
 *
 * @return the registry extension id
 * @see #getConfigurationElement
 */
public String getId();
/**
 * Returns the page containing this workbench site's part.
 *
 * @return the page containing this part
 */
public IWorkbenchPage getPage();
/**
 * Returns the unique identifier of the plug-in that defines this workbench
 * site's part.
 *
 * @return the unique identifier of the declaring plug-in
 * @see org.eclipse.core.runtime.IPluginDescriptor#getUniqueIdentifier
 */
public String getPluginId();
/**
 * Returns the registered name for this workbench site's part.
 * <p>
 * The name comes from the <code>name</code> attribute in the configuration
 * element.
 * </p>
 *
 * @return the part name
 */
public String getRegisteredName();
/**
 * Returns the selection provider for this workbench site's part.
 *
 * @return the selection provider, or <code>null</code> if none
 */
public ISelectionProvider getSelectionProvider();
/**
 * Returns the shell containing this workbench site's part.
 *
 * @return the shell containing the part's controls
 */
public Shell getShell();
/**
 * Returns the workbench window containing this workbench site's part.
 *
 * @return the workbench window containing this part
 */
public IWorkbenchWindow getWorkbenchWindow();
/**
 * Registers a pop-up menu with a particular id for extension.
 * This method should only be called if the target part has more
 * than one context menu to register.
 * <p>
 * For a detailed description of context menu registration see 
 * <code>registerContextMenu(MenuManager, ISelectionProvider);
 * </p>
 *
 * @param menuId the menu id
 * @param menuManager the menu manager
 * @param selectionProvider the selection provider
 */
public void registerContextMenu(String menuId, MenuManager menuManager,
	ISelectionProvider selectionProvider);
/**
 * Registers a pop-up menu with the default id for extension.  
 * The default id is defined as the part id.
 * <p>
 * Within the workbench one plug-in may extend the pop-up menus for a view
 * or editor within another plug-in.  In order to be eligible for extension,
 * the target part must publish each menu by calling <code>registerContextMenu</code>.
 * Once this has been done the workbench will automatically insert any action 
 * extensions which exist.
 * </p>
 * <p>
 * A menu id must be provided for each registered menu.  For consistency across
 * parts the following strategy should be adopted by all part implementors.
 * </p>
 * <ol>
 *		<li>If the target part has only one context menu it should be registered
 *			with <code>id == part id</code>.  This can be done easily by calling
 *			<code>registerContextMenu(MenuManager, ISelectionProvider).  
 *		<li>If the target part has more than one context menu a unique id should be
 *			defined for each.  Prefix each menu id with the part id and publish these
 *			ids within the javadoc for the target part.  Register each menu at 
 *			runtime by calling <code>registerContextMenu(String, MenuManager, 
 *			ISelectionProvider)</code>.  </li>
 * </ol>
 * <p>
 * Any pop-up menu which is registered with the workbench should also define a  
 * <code>GroupMarker</code> in the registered menu with id 
 * <code>IWorkbenchActionConstants.MB_ADDITIONS</code>.  Other plug-ins will use this 
 * group as a reference point for insertion.  The marker should be defined at an 
 * appropriate location within the menu for insertion.  
 * </p>
 *
 * @param menuManager the menu manager
 * @param selectionProvider the selection provider
 */
public void registerContextMenu(MenuManager menuManager,
	ISelectionProvider selectionProvider);
/**
 * Sets the selection provider for this workbench site's part.
 *
 * @param provider the selection provider, or <code>null</code> to clear it
 */
public void setSelectionProvider(ISelectionProvider provider);
}
