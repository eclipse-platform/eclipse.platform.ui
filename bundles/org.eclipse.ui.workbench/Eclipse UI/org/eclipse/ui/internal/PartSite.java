package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.SubActionBars;

/**
 * <code>PartSite</code> is the general implementation for an
 * <code>IWorkbenchPartSite</code>.  A site maintains the context for a part,
 * including the part, its pane, active contributions, selection provider, etc.
 * Together, these components make up the complete behavior for a
 * part as if it was implemented by one person.  
 *
 * The <code>PartSite</code> lifecycle is as follows ..
 *
 * <ol>
 * <li>a site is constructed </li>
 * <li>a part is constructed and stored in the part </li>
 * <li>the site calls part.init() </li>
 * <li>a pane is constructed and stored in the site </li>
 * <li>the action bars for a part are constructed and stored in the site </li>
 * <li>the pane is added to a presentation </li>
 * <li>the SWT widgets for the pane and part are created </li>
 * <li>the site is activated, causing the actions to become visible </li>
 * </ol>
 */
public class PartSite implements IWorkbenchPartSite {
	private IWorkbenchPart part;
	private IWorkbenchPage page;
	private PartPane pane;
	private IConfigurationElement configElement;
	private String extensionID;
	private String pluginID;
	private String extensionName;
	private ISelectionProvider selectionProvider;
	private SubActionBars actionBars;
	private KeyBindingService keyBindingService;	
/**
 * EditorContainer constructor comment.
 */
public PartSite(IWorkbenchPart part, IWorkbenchPage page) 
{
	this.part = part;
	this.page = page;
	extensionID = "org.eclipse.ui.UnknownID";//$NON-NLS-1$
	extensionName = "Unknown Name";//$NON-NLS-1$
}
/**
 * Dispose the contributions.
 */
public void dispose() {
}
/**
 * Returns the action bars for the part.
 * If this part is a view then it has exclusive use of the action bars.
 * If this part is an editor then the action bars are shared among this editor and other editors of
 * the same type.
 */
public IActionBars getActionBars() {
	return actionBars;
}
/**
 * Returns the configuration element for a part.
 */
public IConfigurationElement getConfigurationElement() {
	return configElement;
}


/**
 * Returns the part registry extension ID.
 *
 * @return the registry extension ID
 */
public String getId() {
	return extensionID;
}
/**
 * Returns the page containing this workbench site's part.
 *
 * @return the page containing this part
 */
public IWorkbenchPage getPage() {
	return page;
}
/**
 * Gets the part pane.
 */
public PartPane getPane() {
	return pane;
}
/**
 * Returns the part.
 */
public IWorkbenchPart getPart() {
	return part;
}
/**
 * Returns the part registry plugin ID.  It cannot be <code>null</code>.
 *
 * @return the registry plugin ID
 */
public String getPluginId() {
	return pluginID;
}
/**
 * Returns the registered name for this part.
 */
public String getRegisteredName() {
	return extensionName;
}
/**
 * Returns the selection provider for a part.
 */
public ISelectionProvider getSelectionProvider() {
	return selectionProvider;
}
/**
 * Returns the shell containing this part.
 *
 * @return the shell containing this part
 */
public Shell getShell() {
	return page.getWorkbenchWindow().getShell();
}
/**
 * Returns the workbench window containing this part.
 *
 * @return the workbench window containing this part
 */
public IWorkbenchWindow getWorkbenchWindow() {
	return page.getWorkbenchWindow();
}
/**
 * Register a popup menu for extension.
 */
public void registerContextMenu(String menuID, MenuManager menuMgr, ISelectionProvider selProvider) {
	new PopupMenuExtender(menuID, menuMgr, selProvider, part);
}
/**
 * Register a popup menu with the default id for extension.
 */
public void registerContextMenu(MenuManager menuMgr, ISelectionProvider selProvider) {
	registerContextMenu(getId(), menuMgr, selProvider);
}
/**
 * Sets the action bars for the part.
 */
public void setActionBars(SubActionBars bars) {
	actionBars = bars;
}
/**
 * Sets the configuration element for a part.
 */
public void setConfigurationElement(IConfigurationElement configElement) {
	// Save for external use.
	this.configElement = configElement;
	
	// Get extension ID.
	extensionID = configElement.getAttribute("id");//$NON-NLS-1$

	// Get plugin ID.
	IPluginDescriptor pd = configElement.getDeclaringExtension().getDeclaringPluginDescriptor();
	pluginID = pd.getUniqueIdentifier();

	// Get extension name.
	String name = configElement.getAttribute("name");//$NON-NLS-1$
	if (name != null)
		extensionName = name;
}
/**
 * Sets the part pane.
 */
public void setPane(PartPane pane) {
	this.pane = pane;
}
/**
 * Sets the part.
 */
public void setPart(IWorkbenchPart newPart) {
	part = newPart;
}
/**
 * Set the selection provider for a part.
 */
public void setSelectionProvider(ISelectionProvider provider) {
	selectionProvider = provider;
}

/* (non-Javadoc)
 * Method declared on IEditorSite.
 */
public IKeyBindingService getKeyBindingService() {
	if(keyBindingService == null) {
		keyBindingService = new KeyBindingService(((WorkbenchWindow)getWorkbenchWindow()).getKeyBindingService(),this);
		keyBindingService.setActiveAcceleratorScopeId(getInitialScopeId()); //$NON-NLS-1$
	}	
	return keyBindingService;
}
protected String getInitialScopeId() {
	return IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID;
}

}
