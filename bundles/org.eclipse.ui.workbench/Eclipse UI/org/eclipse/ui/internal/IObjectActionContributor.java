package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This interface must be implemented in order to contribute
 * to context (pop-up) menu for an object. Classes
 * that implement this interface must register
 * with the popup menu manager.
 */
public interface IObjectActionContributor extends IObjectContributor {
/**
 * Implement this method to add actions that deal with the currently
 * selected object or objects. Actions should be added to the
 * provided menu object. Current selection can be obtained from
 * the given selection provider.
 * @return True if any contributions were made, and false otherwise.
 */
public boolean contributeObjectActions(IWorkbenchPart part, IMenuManager menu, 
	ISelectionProvider selProv);
/**
 * Implement this method to add menus that deal with the currently
 * selected object or objects. Menus should be added to the
 * provided menu object. Current selection can be obtained from
 * the given selection provider.
 * @return True if any contributions were made, and false otherwise.
 */
public boolean contributeObjectMenus(IMenuManager menu, ISelectionProvider selProv);
}
