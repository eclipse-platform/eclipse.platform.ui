package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;

/* (non-Javadoc)
 * @see IKeyBindingService
 */
public class KeyBindingService implements IKeyBindingService {
	private PartListener partListener;
	private String activeAcceleratorConfigurationId;
	private String activeAcceleratorScopeId;
	
	// keys: MenuItems
	// values: accelerators
	private HashMap menuItemAccelerators;
	
	// keys: action definition ids
	// values: accelerator keys
	private HashMap activeAccelConfig;
	
	// keys: accelerator keys
	// values: actions
	private HashMap activeKeyBindings;
	
	public KeyBindingService(Workbench workbench) {
		//  use defaults for now
		activeAcceleratorConfigurationId = IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID;
		activeAcceleratorScopeId = IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID;
		activeKeyBindings = new HashMap();
		menuItemAccelerators = new HashMap();
		partListener = new PartListener();
/**************
 * this line is wrong, we can't just track on the active page
 * it has to be all pages...
 */		
		workbench.getActiveWorkbenchWindow().getActivePage().addPartListener(partListener);
		createActiveAccelConfig();		
	}
	
	/*
	 *  Creates the active accelerator configuration
	 */
	private void createActiveAccelConfig() {
		activeAccelConfig = new HashMap();
		AcceleratorRegistry registry = new AcceleratorRegistry();
		registry.load();
		List acceleratorSets = registry.getAcceleratorSets();
		Iterator i = acceleratorSets.iterator();
		
		while(i.hasNext()) {
			// combine accelerator sets with cofigurationId == activeAcceleratorConfigurationId
			// to form the active accelerator configuration. Resolve conflicts.
				// complete implementation later
		}
	}
	
	/**
	 * @see IKeyBindingService
	 */
    public String getActiveAcceleratorConfigurationId() {
    	return activeAcceleratorConfigurationId;
    }
	/**
	 * @see IKeyBindingService
	 */    
    public String getActiveAcceleratorScopeId() {
    	return activeAcceleratorScopeId;
    }

	/**
	 * @see IKeyBindingService
	 */      
    public boolean processKey(Event event) {
    	boolean consume = false;
    	// complete implementation later
//    	Enumeration e = activeKeyBindings.keys();
//    	while(e.hasMoreElements()) {
//			String key = e.nextElement();
//   		if(key==event.? ) { // how do we get the key from Event?
//  					
//    		}	
//    	}
    	return consume;
    }
    
	/**
	 * @see IKeyBindingService
	 */ 
    public void registerAction(IAction action) {
		// complete implementation later
    }

	/**
	 * @see IKeyBindingService
	 */     
    public void setActiveAcceleratorScopeId(String scopeId) {
    	activeAcceleratorScopeId = scopeId;
    }
    
    /**
     * Sets the active accelerator configuration to be the configuration
     * with the given id.
     */
    public void setActiveAcceleratorConfiguration(String configId) {
    	// set the new active configuration id
    	activeAcceleratorConfigurationId = configId;
    	// create the active accelerator configuration with the given id
    	createActiveAccelConfig();	
    }
    
    private boolean isParticipating(IWorkbenchPart part) {
    	// reimplement correctly later
		return true;
	}
    
    /*
     * Temporarily clears accelerators for all menu items in this menu,
     * and all it's submenus (and their submenus, etc.). Cleared accelerators may be restored by 
     * restoreAccelerators().
     */
	private void clearAccelerators(Menu menu) {
    	for(int j=0;j<menu.getItemCount();j++) {
    		clearAccelerators(menu.getItem(j));
    	}    	
    }
    
    /*
     * Temporarily clears the accelerator for this menu item. If the menu item
     * is a menu, clears all accelerators of menu items of the menu and all its
     * sbmenus and their submenus, etc.).
     */
    private void clearAccelerators(MenuItem item) {
    	if(item.getMenu()!=null) {
    		clearAccelerators(item.getMenu());	
    	}
    	else {
    		//store the accelerator so it can be restored later
    		menuItemAccelerators.put(item, new Integer(item.getAccelerator()));
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

    
    class PartListener implements IPartListener {
    	public PartListener() {
    		super();
    	}
    	
	    /**
	     * @see IPartListener
	     */
	    public void partActivated(IWorkbenchPart part) {
	    	if(part instanceof IViewPart) {
				restoreAccelerators();
	    	}
	    	if(part instanceof IEditorPart) {
	    		if(isParticipating(part)) {
	    			// remove accelerators from menu
	    			Workbench workbench = (Workbench)(part.getSite().getPage().getWorkbenchWindow().getWorkbench());
	    			IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
	    			for(int i=0;i<windows.length;i++) {
	    				Menu menu = ((WorkbenchWindow)windows[i]).getMenuManager().getMenu();
						clearAccelerators(menu);
	    			}
	    			
	    			// calculate active key bindings as per algorithm 
	    			// provided in key bindings proposal
    				// complete implementation later
	    		}
	    		else {
	    			restoreAccelerators();
	    		}
	    	}
	    }
	    
	    /**
	     * @see IPartListener
	     */    
	    public void partBroughtToTop(IWorkbenchPart part) {
	    }
	    
	    /**
	     * @see IPartListener
	     */    
	    public void partClosed(IWorkbenchPart part) {
	    }
	    
	    /**
	     * @see IPartListener
	     */    
	    public void partDeactivated(IWorkbenchPart part) {
	    }
	    
	    /**
	     * @see IPartListener
	     */    
	    public void partOpened(IWorkbenchPart part) {
	    }    
    }
}
