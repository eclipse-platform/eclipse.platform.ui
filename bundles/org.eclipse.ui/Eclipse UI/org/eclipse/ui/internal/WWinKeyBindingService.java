package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
import org.eclipse.ui.internal.registry.IActionSet;

/**
 * @version 	1.0
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
	/* A number used to generate definition id for the action
	 * without one 
	 */
	private long fakeDefinitionId = 0;
	/* The window this service is managing the accelerators for.*/
	private WorkbenchWindow window;
	/**
	 * Create an instance of WWinKeyBindingService and initializes it.
	 */			
	public WWinKeyBindingService(final WorkbenchWindow window) {
		this.window = window;
		IWorkbenchPage[] pages = window.getPages();
		final IPartListener partListener = new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
				update(part);
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
				IWorkbenchPage page = window.getActivePage();
				if(page != null) {
					IWorkbenchPart part = page.getActivePart();
					if(part != null) {
						update(part);
						return;
					}
				}
				MenuManager menuManager = window.getMenuManager();
				menuManager.updateAll(true);
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
		boolean reinitScopes = false;
		for(int i=0; i<sets.length; i++) {
			if(sets[i] instanceof PluginActionSet) {
				PluginActionSet set = (PluginActionSet)sets[i];
				IAction actions[] = set.getPluginActions();
				for (int j = 0; j < actions.length; j++) {
					Action action = (Action)actions[j];
					String defId = action.getActionDefinitionId();
					if(defId != null) {
						actionSetDefIdToAction.put(action.getActionDefinitionId(),action);
					} else if(action.getAccelerator() != 0) {
						reinitScopes = true;
						String fake = "org.eclipse.ui.fakeDefinitionId" + fakeDefinitionId;
						fakeDefinitionId++;
						action.setActionDefinitionId(fake);
						actionSetDefIdToAction.put(fake,action);
						AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
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
	 * Remove or restore the accelerators in the menus.
	 * If the service is the active part's service.
	 */	
   	public void update(KeyBindingService service) {
   		IWorkbenchPart part = window.getActivePage().getActivePart();
   		if(part instanceof IEditorPart) {
   			KeyBindingService currServ = (KeyBindingService)((IEditorPart)part).getEditorSite().getKeyBindingService();
   			if(currServ == service)
   				update(part);
   		}
   	}
	/**
	 * Remove or restore the accelerators in the menus.
	 */
   	private static void update(IWorkbenchPart part) {
   		if(part==null)
   			return;
    	IWorkbenchPartSite site = part.getSite();
    	WorkbenchWindow w = (WorkbenchWindow)site.getPage().getWorkbenchWindow();
    	MenuManager menuManager = w.getMenuManager();
    	if(part instanceof IViewPart) {
    		menuManager.setAcceleratorsAllowed(menuManager.getMenu(),true);
    	} else if(part instanceof IEditorPart) {
    		KeyBindingService service = (KeyBindingService)((IEditorSite)site).getKeyBindingService();
    		AcceleratorConfiguration config = ((Workbench)w.getWorkbench()).getActiveAcceleratorConfiguration();
    		if((config != null) && (!config.getId().equals(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID))) {
    			boolean useAcc = !service.isParticipating();
				menuManager.setAcceleratorsAllowed(menuManager.getMenu(),useAcc);
    		} else {
				menuManager.setAcceleratorsAllowed(menuManager.getMenu(),true);
    		}
    	}
		menuManager.updateAll(true);
    }
}
