/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package org.eclipse.ui.internal;

import java.util.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

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
	    public void partActivated(IWorkbenchPart part) {
	    	IWorkbenchPartSite site = part.getSite();
	    	WorkbenchWindow w = (WorkbenchWindow)site.getPage().getWorkbenchWindow();
	    	MenuManager menuManager = w.getMenuManager();
	    	if(part instanceof IViewPart) {
				menuManager.updateAccelerators(true);
	    	} else if(part instanceof IEditorPart) {
	    		KeyBindingService service = (KeyBindingService)((IEditorSite)site).getKeyBindingService();
    			menuManager.updateAccelerators(!service.isParticipating());
	    	}
	    }
	    public void partBroughtToTop(IWorkbenchPart part) {}
	    public void partClosed(IWorkbenchPart part) {}
	    public void partDeactivated(IWorkbenchPart part) {}  
	    public void partOpened(IWorkbenchPart part) {}
    }
}
