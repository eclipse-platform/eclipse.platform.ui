/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

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
	
	//Increased whenever the action mapping changes.
	//E.g. adding/removing action sets.
	private long updateNumber = 0;
	private HashMap globalActionDefIdToAction = new HashMap();
	private HashMap actionSetDefIdToAction = new HashMap();
	private IPropertyChangeListener propertyListener;
	
	private long fakeDefinitionId = 0;
		
	public WWinKeyBindingService(final WorkbenchWindow window) {
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
				menuManager.updateAccelerators(true);
			}
		};
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(propertyListener);
	}
	public void dispose() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		store.removePropertyChangeListener(propertyListener);
	}
		
	public void registerGlobalAction(IAction action) {
		updateNumber++;
		globalActionDefIdToAction.put(action.getActionDefinitionId(),action);
	}
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
	public long getUpdateNumber() {
		return updateNumber;
	}
	public HashMap getMapping() {
		HashMap result = (HashMap)globalActionDefIdToAction.clone();
		result.putAll(actionSetDefIdToAction);
		return result;
	}
	
   	public static void update(IWorkbenchPart part) {
   		if(part==null)
   			return;
    	IWorkbenchPartSite site = part.getSite();
    	WorkbenchWindow w = (WorkbenchWindow)site.getPage().getWorkbenchWindow();
    	MenuManager menuManager = w.getMenuManager();
    	if(part instanceof IViewPart) {
			menuManager.updateAccelerators(true);
    	} else if(part instanceof IEditorPart) {
    		KeyBindingService service = (KeyBindingService)((IEditorSite)site).getKeyBindingService();
    		AcceleratorConfiguration config = ((Workbench)w.getWorkbench()).getActiveAcceleratorConfiguration();
    		if((config != null) && (!config.getId().equals(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID)))
				menuManager.updateAccelerators(!service.isParticipating());
			else
				menuManager.updateAccelerators(true);
    	}
    }
	    	
    private static class PartListener implements IPartListener {
	    public void partActivated(IWorkbenchPart part) {
	    	update(part);
	    }
	    public void partBroughtToTop(IWorkbenchPart part) {}
	    public void partClosed(IWorkbenchPart part) {}
	    public void partDeactivated(IWorkbenchPart part) {}  
	    public void partOpened(IWorkbenchPart part) {}
    }
}
