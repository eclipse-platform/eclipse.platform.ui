/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @version 	1.0
 * @author
 */
public class WWinKeyBindingService {
	
	//Increased whenever the action mapping changes.
	//E.g. adding/removing action sets.
	private long updateNumber = 0;
	private HashMap globalActionDefIdToAction = new HashMap();
	private HashMap actionSetDefIdToAction = new HashMap();
		
	public WWinKeyBindingService(WorkbenchWindow window) {
		IWorkbenchPage[] pages = window.getPages();
		final PartListener partListener = new PartListener();
		for(int i=0; i<pages.length;i++) {
			pages[i].addPartListener(partListener);
		}
		window.addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page){}
			public void pageClosed(IWorkbenchPage page){}
			public void pageOpened(IWorkbenchPage page){
				page.addPartListener(partListener);
				partListener.partActivated(page.getActivePart());
			}
		});		
	}
	public void registerGlobalAction(IAction action) {
		updateNumber++;
		globalActionDefIdToAction.put(action.getActionDefinitionId(),action);
	}
	public void registerActionSet(IAction action) {
		updateNumber++;
		actionSetDefIdToAction.put(action.getActionDefinitionId(),action);
	}
	public long getUpdateNumber() {
		return updateNumber;
	}
	public HashMap getMapping() {
		HashMap result = (HashMap)globalActionDefIdToAction.clone();
		result.putAll(actionSetDefIdToAction);
		return result;
	}
    private static class PartListener implements IPartListener {
		// keys: menu items (MenuItems)
		// values: accelerators (Accelerators)
		private HashMap menuItemAccelerators =  new HashMap();
    	
	    public void partActivated(IWorkbenchPart part) {
	    	if(part instanceof IViewPart) {
				restoreAccelerators();
	    	} else if(part instanceof IEditorPart) {
	    		IEditorSite site = ((IEditorPart)part).getEditorSite();
	    		KeyBindingService service = (KeyBindingService)site.getKeyBindingService();
	    		if(service.isParticipating()) {
	    			// remove accelerators from menu
	    			WorkbenchWindow w = (WorkbenchWindow)site.getPage().getWorkbenchWindow();
	    			Menu menu = w.getMenuManager().getMenu();
//	    			menu.updateAll(true);
					updateAccelerators(menu);
	    		}
	    		else {
	    			// if the editor being activated is not a "participating" editor
	    			restoreAccelerators();
	    		}
	    	}
	    }
	    public void partBroughtToTop(IWorkbenchPart part) {}
	    public void partClosed(IWorkbenchPart part) {}
	    public void partDeactivated(IWorkbenchPart part) {}  
	    public void partOpened(IWorkbenchPart part) {}
	    /*
	     * Temporarily clears accelerators for all menu items in this menu,
	     * and all it's submenus (and their submenus, etc.). Cleared accelerators may be restored by 
	     * restoreAccelerators().
	     */
		private void updateAccelerators(Menu menu) {
	    	for(int j=0;j<menu.getItemCount();j++) {
	    		updateAccelerators(menu.getItem(j));
	    	}    	
	    }
	    /*
	     * Temporarily clears the accelerator for this menu item. If the menu item
	     * is a menu, clears all accelerators of menu items of the menu and all its
	     * submenus and their submenus, etc.).
	     */
	    private void updateAccelerators(MenuItem item) {
	    	if(item.getMenu()!=null) {
	    		updateAccelerators(item.getMenu());	
	    	}
	    	else {
	    		//store the accelerator so it can be restored later
	    		menuItemAccelerators.put(item, new Integer(item.getAccelerator()));
	    		//clear the accelerator
	    		item.setAccelerator(0);
	    	}
	    }
	    /*
	     * Restores menu item accelerators which were temporarily cleared
	     * using clearAccelerators(Menu) or clearAccelerators(MenuItem).
	     */
	    private void restoreAccelerators() {
			if (!menuItemAccelerators.isEmpty()) {
	        	Set keySet = menuItemAccelerators.keySet();
	    		Iterator items = keySet.iterator();
	    		while(items.hasNext()) {
	    			MenuItem item = (MenuItem)(items.next());
	    			if (!item.isDisposed()) {
	    				int i = ((Integer)(menuItemAccelerators.get(item))).intValue();
	    				item.setAccelerator(i);	
	    			}
		  		}
			}	
	    }
    }
}
