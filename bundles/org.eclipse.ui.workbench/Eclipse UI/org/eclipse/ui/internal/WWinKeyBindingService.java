package org.eclipse.ui.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.keybindings.Util;
import org.eclipse.ui.internal.keybindings.KeyBindingManager;
import org.eclipse.ui.internal.keybindings.KeySequence;
import org.eclipse.ui.internal.keybindings.KeyStroke;
import org.eclipse.ui.internal.keybindings.Scope;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
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

	private AcceleratorMenu accMenu;

	private VerifyListener verifyListener = new VerifyListener() {
		public void verifyText(VerifyEvent event) {
			event.doit = false;
			clear();
		}
	};

	private void setStatusLineMessage(KeySequence keySequence) {
		StringBuffer stringBuffer = new StringBuffer();
		
		if (keySequence != null) {
			Iterator iterator = keySequence.getKeyStrokes().iterator();
			int i = 0;
			
			while (iterator.hasNext()) {					
				if (i != 0)
					stringBuffer.append(' ');
	
				KeyStroke keyStroke = (KeyStroke) iterator.next();
				int accelerator = keyStroke.getAccelerator();
				stringBuffer.append(
					org.eclipse.jface.action.Action.convertAccelerator(
					accelerator));					
				i++;
			}		
		}
	
		window.getActionBars().getStatusLineManager().setMessage(stringBuffer.toString());
	}

	public void clear() {
		KeyBindingManager.getInstance().setMode(KeySequence.create());
		setStatusLineMessage(null);			
		updateAccelerators();
	}
	
	public void pressed(KeyStroke stroke) { 
		//System.out.println("pressed(" + stroke.getAccelerator() + ")");
		KeySequence mode = KeyBindingManager.getInstance().getMode();
		SortedMap sequenceActionMapForMode = 
			KeyBindingManager.getInstance().getKeySequenceActionMapForMode();
		KeySequence sequence = KeySequence.create(stroke);
	
		if (sequenceActionMapForMode.containsKey(sequence)) {
			invoke(((org.eclipse.ui.internal.keybindings.Action) 
				sequenceActionMapForMode.get(sequence)).getValue());
			clear();	
		} else {
			List strokes = new ArrayList(mode.getKeyStrokes());
			strokes.add(stroke);
			mode = KeySequence.create(strokes);
			KeyBindingManager keyBindingManager = 
				KeyBindingManager.getInstance();
			
			keyBindingManager.setMode(mode);
			setStatusLineMessage(mode);
			
			if (keyBindingManager.getKeySequenceActionMapForMode().size() == 0)				
				clear();	
			else
				updateAccelerators();
		}
	}

	public void invoke(String action) {		
		//System.out.println("invoke(" + action + ")");
		if (activeService != null) {
			IAction a = activeService.getAction(action);
			
			if (a != null && a.isEnabled())
				a.run();
		}
	}

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
		
		for (int i=0; i<sets.length; i++) {
			if (sets[i] instanceof PluginActionSet) {
				PluginActionSet set = (PluginActionSet)sets[i];
				IAction actions[] = set.getPluginActions();
				
				for (int j = 0; j < actions.length; j++) {
					Action action = (Action)actions[j];
					String defId = action.getActionDefinitionId();
					
					if (defId != null) {
						actionSetDefIdToAction.put(action.getActionDefinitionId(),action);
					}
				}
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
   	public void update(IWorkbenchPart part, boolean force) {
   		if (part == null)
   			return;
   		
		String[] oldScopeIds = new String[0];
   		
   		if (activeService != null)
   			oldScopeIds = activeService.getScopeIds();
   			
    	activeService = (KeyBindingService) part.getSite().getKeyBindingService();
		clear();

   		String[] newScopeIds = new String[0];
   		
   		if (activeService != null)
   			newScopeIds = activeService.getScopeIds();

    	if (force || Util.compare(oldScopeIds, newScopeIds) == 0) {
	    	Scope[] scopes = new Scope[newScopeIds.length];
	    	
	    	for (int i = 0; i < newScopeIds.length; i++)
	    		scopes[i] = KeyBindingManager.getInstance().getScopeForId(newScopeIds[i]);
	    	
	    	KeyBindingManager.getInstance().setScopes(scopes);	    	
	    	WorkbenchWindow w = (WorkbenchWindow) getWindow();
   	 		MenuManager menuManager = w.getMenuManager();
 			menuManager.update(IAction.TEXT);
    	}
    }
    /**
     * Returns the definition id for <code>accelerator</code>
     */
    public String getDefinitionId(int[] accelerators) {
    	if (accelerators == null || activeService == null) 
    		return null;
        
    	KeyStroke[] keyStrokes = KeyStroke.create(accelerators);   
    	KeySequence keySequence = KeySequence.create(keyStrokes);    
		Map sequenceActionMapForMode =
			KeyBindingManager.getInstance().getKeySequenceActionMapForMode();
			
		Object object = sequenceActionMapForMode.get(keySequence);
		
		if (object == null)
			return null;
			
     	return ((org.eclipse.ui.internal.keybindings.Action) object).getValue();
    }

	/**
	 * Update the KeyBindingMenu with the current set of accelerators.
	 */
	public void updateAccelerators() {
	   	SortedSet sortedSet = 
	   		(SortedSet) KeyBindingManager.getInstance().getStrokeSetForMode();
	   	Iterator iterator = sortedSet.iterator();
	   	int[] accelerators = new int[sortedSet.size()];
		int i = 0;
			   	
	   	while (iterator.hasNext()) {
	   		KeyStroke keyStroke = (KeyStroke) iterator.next();
	   		accelerators[i++] = keyStroke.getAccelerator();	   		
	   	}

		if (accMenu == null || accMenu.isDisposed()) {		
			Menu parent = window.getShell().getMenuBar();
			if (parent == null || parent.getItemCount() < 1)
				return;
			MenuItem parentItem = parent.getItem(parent.getItemCount() - 1);
			parent = parentItem.getMenu();
			accMenu = new AcceleratorMenu(parent);
		}
		
		if (accMenu == null)
			return;
		
		accMenu.setAccelerators(accelerators);		
		accMenu.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				pressed(KeyStroke.create(e.detail));
			}
		});

		KeySequence keySequence = KeyBindingManager.getInstance().getMode();

		if (keySequence.getKeyStrokes().size() == 0)
			accMenu.removeVerifyListener(verifyListener);
		else
			accMenu.addVerifyListener(verifyListener);
	}
    
}
