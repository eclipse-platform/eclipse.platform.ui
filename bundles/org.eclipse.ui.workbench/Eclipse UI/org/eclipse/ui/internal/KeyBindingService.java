package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import java.util.HashMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.registry.*;

/** 
 * Implementation of an IKeyBindingService.
 * Notes:
 * <ul>
 * <li>One instance is created for each editor site</li>
 * <li>Each editor has to register all its actions by calling registerAction()</li>
 * <li>The editor should call setActiveAcceleratorScopeId() once</li>
 * </ul>
 */
public class KeyBindingService implements IKeyBindingService {
	private IPartListener partListener;
	private ShellListener shellListener;
	
	/* Maps action definition id to action. */
	private HashMap defIdToAction = new HashMap();
	/* Maps action definition id to action. Includes the actions 
	 * registered in this service and its parent so that only one 
	 * lookup is needed.
	 */
	private HashMap allDefIdToAction = new HashMap();
	/* The active accelerator scope which is set by the editor */
	private AcceleratorScope scope;
	/* The Workbench window key binding service which manages the 
	 * global actions and the action sets 
	 */
	private WWinKeyBindingService parent;
	/* A number increased by the parent whenever a new action 
	 * is registered so that this instance can update its mapping
	 * when it is out of sync.
	 */
	private long parentUpdateNumber;
	/* Maps acc to action. 
	 */
	private HashMap editorActions = new HashMap();
	
	/**
	 * Create an instance of KeyBindingService and initializes 
	 * it with its parent.
	 */		
	public KeyBindingService(WWinKeyBindingService service,PartSite site) {
		partListener = new IPartListener() {
			public void partActivated(IWorkbenchPart part) {}
			public void partBroughtToTop(IWorkbenchPart part) {}
			public void partClosed(IWorkbenchPart part) {}
			public void partDeactivated(IWorkbenchPart part) {
				AcceleratorScope.resetMode(KeyBindingService.this);
			}
			public void partOpened(IWorkbenchPart part) {}
		};
		shellListener = new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				AcceleratorScope.resetMode(KeyBindingService.this);	
			}
		};
		parent = service;
		parentUpdateNumber = parent.getUpdateNumber() - 1;
		service.getWindow().getPartService().addPartListener(partListener);
		service.getWindow().getShell().addShellListener(shellListener);
		if(site instanceof EditorSite)
			initEditorActions((EditorSite)site);
	}
	/*
	 * Merge the actions from its parents with its registered actions
	 * in one HashMap
	 */
	private void initializeMapping() {
		parentUpdateNumber = parent.getUpdateNumber();
		allDefIdToAction = parent.getMapping();
		allDefIdToAction.putAll(defIdToAction);
	}
	/*
	 * Initialize a hash map with all editor actions. Used
	 * for backward compatibility.
	 */
	private void initEditorActions(EditorSite site) {
		EditorMenuManager nenuMgr = (EditorMenuManager)site.getActionBars().getMenuManager();
		IAction actions[] = nenuMgr.getAllContributedActions();
		for (int i = 0; i < actions.length; i++) {
			int acc = actions[i].getAccelerator();
			if(acc != 0)
				editorActions.put(new Integer(acc),actions[i]);
		}
	}
	/*
	 * HACK: Should be deleted once we find a solution
	 * for editor actions which is not supported by this key 
	 * binding implementation.
	 */
	public boolean processEditorAction(Event e, int acc) {
		IAction action = (IAction)editorActions.get(new Integer(acc));
		if(action == null)
			return false;
		action.runWithEvent(e);
		return true;
	}
	/*
	 * HACK: Should be deleted once we find a solution
	 * for editor actions which is not supported by this key 
	 * binding implementation.
	 */
	public int[] getEditorActions() {
		int result[] = new int[editorActions.size()];
		int i = 0;
		for (Iterator iter = editorActions.keySet().iterator(); iter.hasNext();i++) {
			result[i] = ((Integer)iter.next()).intValue();
		}
		return result;
	}
	/** 
	 * Remove the part listener when the editor site is disposed.
	 */
	public void dispose() {
		getWindow().getPartService().removePartListener(partListener);
		getWindow().getShell().removeShellListener(shellListener);
	}
	/*
	 * @see IKeyBindingService#getActiveAcceleratorConfigurationId()
	 */
    public String getActiveAcceleratorConfigurationId() {
    	return ((Workbench)PlatformUI.getWorkbench()).getActiveAcceleratorConfiguration().getId();
    }
	/*
	 * @see IKeyBindingService#processKey(Event)
	 */
	public boolean processKey(KeyEvent event) {
		return false;
    }
	/*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void registerAction(IAction action) {
    	if(parentUpdateNumber != parent.getUpdateNumber())
    		initializeMapping();
    	String defId = action.getActionDefinitionId();
    	Assert.isNotNull(defId,"All registered action must have a definition id"); //$NON-NLS-1$
		defIdToAction.put(defId,action);
		allDefIdToAction.put(defId,action);
		if(scope != null)
			scope.registerAction(action.getAccelerator(),defId);
    }
    /*
	 * @see IKeyBindingService#registerAction(IAction)
	 */
	public void enable(boolean enable) {
	}
    /**
     * Returns the action mapped with the specified <code>definitionId</code>
     */
    public IAction getAction(String definitionId) {
    	//Chech if parent has changed. E.g. added action sets.
    	if(parentUpdateNumber != parent.getUpdateNumber())
    		initializeMapping();
    	return (IAction)allDefIdToAction.get(definitionId);
    }
	/*
	 * @see IKeyBindingService#getActiveAcceleratorScopeId()
	 */
	public String getActiveAcceleratorScopeId() {
    	return scope.getId();
    }
    /**
     * Returns the active scope.
     */
	public AcceleratorScope getActiveAcceleratorScope() {
    	return scope;
    }
    /**
     * Returns the workbench window.
     */
    public IWorkbenchWindow getWindow() {
    	return parent.getWindow();	
    } 
	/*
	 * @see IKeyBindingService#setActiveAcceleratorScopeId(String)
	 */ 
    public void setActiveAcceleratorScopeId(String scopeId) {
    	AcceleratorRegistry registry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();
    	scope = registry.getScope(scopeId);
    }
	/**
	 * Update the KeyBindingMenu with the current set of accelerators.
	 */
	public void updateAccelerators(boolean defaultMode) {
		parent.updateAccelerators(defaultMode);
	}
}
