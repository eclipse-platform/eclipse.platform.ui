package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.*;
import java.util.*;
import java.util.List;
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
	private List menuHandlers;
	private SubActionBars actionBars;
	
	private IMemento memento;
	private Map persistableActions = new HashMap();

/**
 * EditorContainer constructor comment.
 */
public PartSite(IWorkbenchPart part, IWorkbenchPage page, IMemento mem) 
{
	this.part = part;
	this.page = page;
	memento = mem;
	extensionID = "org.eclipse.ui.UnknownID";//$NON-NLS-1$
	extensionName = "Unknown Name";//$NON-NLS-1$
}
/**
 * Dispose the contributions.
 */
public void dispose() {
	if (menuHandlers != null)
		menuHandlers.clear();
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
 * Returns the peage containing this workbench site's part.
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
	if (menuHandlers == null)
		menuHandlers = new ArrayList(1);
	menuHandlers.add(new PopupMenuExtender(menuID, menuMgr, selProvider, part));
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

public IMemento getMemento(String id) {
	if(memento == null)
		return null;
	IMemento children[] = memento.getChildren(IWorkbenchConstants.TAG_CONTRIBUTION);
	for(int i=0;i<children.length;i++) {
		String childID = children[i].getString(IWorkbenchConstants.TAG_ID);
		if(id.equals(childID))
			return children[i].getChild(IWorkbenchConstants.TAG_STATE);
	}
	return null;
}

public void addPersistableAction(String id,IPersistableAction persistable) {
	persistableActions.put(id,persistable);
}

public IPersistableAction getPersistableAction(String id) {
	return (IPersistableAction)persistableActions.get(id);
}

public void saveState(IMemento memento) {
	Iterator iterator = persistableActions.keySet().iterator();
	while(iterator.hasNext()) {
		String id = (String)iterator.next();
		IPersistableAction persistable = (IPersistableAction)persistableActions.get(id);
		IMemento contributionMem = memento.createChild(IWorkbenchConstants.TAG_CONTRIBUTION);
		contributionMem.putString(IWorkbenchConstants.TAG_ID,id);
		persistable.saveState(getPart(),contributionMem.createChild(IWorkbenchConstants.TAG_STATE));
	}
}

}
