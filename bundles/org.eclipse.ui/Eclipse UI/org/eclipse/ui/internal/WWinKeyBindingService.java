package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.registry.Accelerator;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
import org.eclipse.ui.internal.registry.AcceleratorScope;
import org.eclipse.ui.internal.registry.IActionSet;

/**
 * @version 	2.0
 * @author
 */
public class WWinKeyBindingService {
	/* A number increased whenever the action mapping changes so
	 * its children can keep their mapping in sync with the ones in
	 * the parent.
	 */
	private long updateNumber = 0;
	/* Maps all global actions definition ids to the action */
	private HashMap globalActionDefIdToAction = new HashMap();
	/* Maps all action sets definition ids to the action */
	private HashMap actionSetDefIdToAction = new HashMap();
	/* A listener to property changes so the mappings can
	 * be updated whenever the active configuration changes.
	 */
	private IPropertyChangeListener propertyListener;
	/* The current KeyBindindService */
	private KeyBindingService activeService;
	/* The window this service is managing the accelerators for.*/
	private WorkbenchWindow window;
	/* The contribution item added to a menu. Is does not appear to 
	 * the user. One this menu will have accelerators */
	private KeyBindingMenu acceleratorsMenu;
	/**
	 * Create an instance of WWinKeyBindingService and initializes it.
	 */			
	public WWinKeyBindingService(final WorkbenchWindow window) {
		this.window = window;
		IWorkbenchPage[] pages = window.getPages();
		final IPartListener partListener = new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
				update(part,false);
			}
			public void partBroughtToTop(IWorkbenchPart part) {}
			public void partClosed(IWorkbenchPart part) {}
			public void partDeactivated(IWorkbenchPart part) {}
			public void partOpened(IWorkbenchPart part) {}
		};
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
		propertyListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID)) {
					IWorkbenchPage page = window.getActivePage();
					if(page != null) {
						IWorkbenchPart part = page.getActivePart();
						if(part != null) {
							update(part,true);
							return;
						}
					}
					MenuManager menuManager = window.getMenuManager();
					menuManager.updateAll(true);
				}
			}
		};
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(propertyListener);
	}
	/** 
	 * Remove the propety change listener when the windows is disposed.
	 */
	public void dispose() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.removePropertyChangeListener(propertyListener);
	}
	/**
	 * Register a global action in this service
	 */	
	public void registerGlobalAction(IAction action) {
		updateNumber++;
		globalActionDefIdToAction.put(action.getActionDefinitionId(),action);
	}
	/**
	 * Register all action from the specifed action set.
	 */	
	public void registerActionSets(IActionSet sets[]) {
		updateNumber++;
		actionSetDefIdToAction.clear();
		AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
		registry.clearFakeAccelerators();
		boolean reinitScopes = false;
		for(int i=0; i<sets.length; i++) {
			if(sets[i] instanceof PluginActionSet) {
				PluginActionSet set = (PluginActionSet)sets[i];
				IAction actions[] = set.getPluginActions();
				for (int j = 0; j < actions.length; j++) {
					Action action = (Action)actions[j];
					String defId = action.getActionDefinitionId();
					String fake = "org.eclipse.ui.fakeDefinitionId"; //$NON-NLS-1$
					if(defId != null && !defId.startsWith(fake)) {
						actionSetDefIdToAction.put(action.getActionDefinitionId(),action);
					} else if(action.getAccelerator() != 0) {
						reinitScopes = true;
						fake = fake + action.getId() + action.getAccelerator(); 
						action.setActionDefinitionId(fake);
						actionSetDefIdToAction.put(fake,action);
						registry.addFakeAccelerator(fake,action.getAccelerator());
					}
				}
			}
		}
		if(reinitScopes) {
			Workbench w = (Workbench)PlatformUI.getWorkbench();
			if (w.getActiveAcceleratorConfiguration() != null) {
				w.getActiveAcceleratorConfiguration().initializeScopes();
			}
			if(activeService != null) {
				activeService.getActiveAcceleratorScope().resetMode(activeService);
				updateAccelerators(true);
			}
		}
	}
	/**
	 * Return the update number used to keep children and parent in sync.
	 */
	public long getUpdateNumber() {
		return updateNumber;
	}
	/**
	 * Returns a Map with all action registered in this service.
	 */
	public HashMap getMapping() {
		HashMap result = (HashMap)globalActionDefIdToAction.clone();
		result.putAll(actionSetDefIdToAction);
		return result;
	}
	/**
	 * Returns the workbench window.
	 */
	public IWorkbenchWindow getWindow() {
		return window;	
	}
	/**
	 * Remove or restore the accelerators in the menus.
	 */
   	private void update(IWorkbenchPart part,boolean force) {
   		if(part==null)
   			return;
   	
   		AcceleratorScope oldScope = null;
   		if(activeService != null)
   			oldScope = activeService.getActiveAcceleratorScope();
   			
    	activeService = (KeyBindingService)part.getSite().getKeyBindingService();
    	AcceleratorScope scope = activeService.getActiveAcceleratorScope();
    	scope.resetMode(activeService);
		updateAccelerators(true);

   		AcceleratorScope newScope = null;
   		if(activeService != null)
   			newScope = activeService.getActiveAcceleratorScope();

    	if(force || (oldScope != newScope)) {
	    	WorkbenchWindow w = (WorkbenchWindow) getWindow();
   	 		MenuManager menuManager = w.getMenuManager();
 			menuManager.update(IAction.TEXT);
    	}
    }
    /**
     * Returns the definition id for <code>accelerator</code>
     */
    public String getDefinitionId(int accelerator[]) {
    	if(activeService == null) return null;
    	AcceleratorScope scope = activeService.getActiveAcceleratorScope();
    	if(scope == null) return null;
    	return scope.getDefinitionId(accelerator);
    }
    /**
     * Returns the accelerator text which can be shown in the 
     * menu item's label.
     */
    public String getAcceleratorText(String definitionId) {
    	if(activeService == null) return null;
    	AcceleratorScope scope = activeService.getActiveAcceleratorScope();
    	if(scope == null) return null;
    	Accelerator acc = scope.getAccelerator(definitionId);
		if(acc == null)
			return null;
		String result = acc.getText();
		if(result.length() == 0)
			return null;
    	return result;
    }
    /**
     * Returns the accelerator for the specified action definition id
     */
    public int[][] getAccelerators(String definitionId) {
    	if(activeService == null) return null;
    	AcceleratorScope scope = activeService.getActiveAcceleratorScope();
    	if(scope == null) return null;
    	Accelerator acc = scope.getAccelerator(definitionId);
		if(acc == null)
			return null;
		return acc.getAccelerators();
    }
    /** 
     * Set the <code>acceleratorsMenu</code> which is used to
     * add items for all accelerators in the current mode.
     */
    public void setAcceleratorsMenu(KeyBindingMenu acceleratorsMenu) {
    	this.acceleratorsMenu = acceleratorsMenu;
    }
	/**
	 * Update the KeyBindingMenu with the current set of accelerators.
	 */
	public void updateAccelerators(boolean defaultMode) {
	   	AcceleratorScope scope = activeService.getActiveAcceleratorScope();
	   	int[] accs;
	   	if(defaultMode) {
	   		int[] scopeAccs = scope.getAccelerators();
	   		int[] editorAccs = activeService.getEditorActions();
	   		if(editorAccs.length == 0) {
	   			accs = scopeAccs;
	   		} else if(scopeAccs.length == 0) {
	   			accs = editorAccs;
	   		} else {
		   		accs = new int[scopeAccs.length + editorAccs.length];
		   		System.arraycopy(scopeAccs,0,accs,0,scopeAccs.length);
	   			System.arraycopy(editorAccs,0,accs,scopeAccs.length,editorAccs.length);
		 	}
	   	} else {
	   		accs = scope.getAccelerators();
	   	}
		acceleratorsMenu.setAccelerators(accs,scope,activeService,defaultMode);
	}
    
}
