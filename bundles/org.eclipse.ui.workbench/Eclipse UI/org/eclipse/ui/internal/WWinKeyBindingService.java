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
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.commands.KeyBindingMatch;
import org.eclipse.ui.internal.commands.Machine;
import org.eclipse.ui.internal.commands.Manager;
import org.eclipse.ui.internal.commands.KeySequence;
import org.eclipse.ui.internal.commands.KeyStroke;
import org.eclipse.ui.internal.commands.Util;
import org.eclipse.ui.internal.registry.IActionSet;

/**
 * @version 	2.0
 * @author
 */
public class WWinKeyBindingService {

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
	private final KeyModeContributionItem statusItem = new KeyModeContributionItem("KeyModeContribution"); //$NON-NLS-1$

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
				int accelerator = keyStroke.getValue();
				stringBuffer.append(
					org.eclipse.jface.action.Action.convertAccelerator(
					accelerator));					
				i++;
			}		
		}

		statusItem.setText(stringBuffer.toString());	
	}

	public void clear() {		
		Manager keyManager = Manager.getInstance();
		Machine keyMachine = keyManager.getKeyMachine();
		
		/*if (*/keyMachine.setMode(KeySequence.create());//) {
			setStatusLineMessage(null);	
			updateAccelerators();
		//}
	}
	
	public void pressed(KeyStroke keyStroke, Event event) { 
		//System.out.println("pressed(" + keyStroke.getAccelerator() + ")");
		Manager keyManager = Manager.getInstance();
		Machine keyMachine = keyManager.getKeyMachine();
		Map keySequenceMapForMode = keyMachine.getKeySequenceMapForMode();
		KeySequence mode = keyMachine.getMode();
		List keyStrokes = new ArrayList(mode.getKeyStrokes());
		keyStrokes.add(keyStroke);
		KeySequence keySequence = KeySequence.create(keyStrokes);
		SortedSet matchSet = (SortedSet) keySequenceMapForMode.get(keySequence);
		
		if (matchSet != null && matchSet.size() == 1) {
			clear();
			KeyBindingMatch match = (KeyBindingMatch) matchSet.iterator().next();				
			invoke(match.getKeyBinding().getCommand(), event);					
		} else {
			keyMachine.setMode(keySequence);
			setStatusLineMessage(keySequence);
			keySequenceMapForMode = keyMachine.getKeySequenceMapForMode();
			
			if (keySequenceMapForMode.isEmpty())
				clear();	
			else
				updateAccelerators();
		}
	}

	public void invoke(String action, Event event) {		
		//System.out.println("invoke(" + action + ")");
		if (activeService != null) {
			IAction a = activeService.getAction(action);
			
			if (a != null && a.isEnabled())
				a.runWithEvent(event);
		}
	}

	/**
	 * Create an instance of WWinKeyBindingService and initializes it.
	 */			
	public WWinKeyBindingService(WorkbenchWindow workbenchWindow) {
		this.window = workbenchWindow;
		window.getStatusLineManager().add(statusItem);
		
		IWorkbenchPage[] pages = window.getPages();
		final IPartListener partListener = new IPartListener() {
			public void partActivated(IWorkbenchPart part) {
				update(part,false);
			}
			public void partBroughtToTop(IWorkbenchPart part) {}
			public void partClosed(IWorkbenchPart part) {}
			public void partDeactivated(IWorkbenchPart part) {
				clear();
			}
			public void partOpened(IWorkbenchPart part) {}
		};
		final ShellListener shellListener = new ShellAdapter() {
			public void shellDeactivated(ShellEvent e) {
				clear();
			}
		};
		// TODO: Just use getPartService to add listener.		
		for(int i=0; i<pages.length;i++) {
			pages[i].addPartListener(partListener);
		}
		window.addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page){}
			public void pageClosed(IWorkbenchPage page){}
			public void pageOpened(IWorkbenchPage page){
				page.addPartListener(partListener);
				partListener.partActivated(page.getActivePart());
				window.getShell().removeShellListener(shellListener);				
				window.getShell().addShellListener(shellListener);				
			}
		});
		propertyListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals("org.eclipse.ui.commands")) { //$NON-NLS-1$
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
		globalActionDefIdToAction.put(action.getActionDefinitionId(),action);
	}
	/**
	 * Register all action from the specifed action set.
	 */	
	public void registerActionSets(IActionSet sets[]) {
		actionSetDefIdToAction.clear();
		
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
	 * Returns a Map with all action registered in this service.
	 */
	public HashMap getMapping() {
		// TBD: this could be a performance problem.
		HashMap result = (HashMap) globalActionDefIdToAction.clone();
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

    	if (force || Util.compare(oldScopeIds, newScopeIds) != 0) {
			Manager keyManager = Manager.getInstance();
			Machine keyMachine = keyManager.getKeyMachine();
	    	
	    	// TBD: remove this later
	    	if (newScopeIds == null || newScopeIds.length == 0)
	    		newScopeIds = new String[] { "org.eclipse.ui.globalScope" }; //$NON-NLS-1$
	    	
	    	try {
	    		keyMachine.setScopes(newScopeIds);
	    	} catch (IllegalArgumentException eIllegalArgument) {
	    		System.err.println(eIllegalArgument);
	    	}
	    			    	   	
	    	WorkbenchWindow w = (WorkbenchWindow) getWindow();
   	 		MenuManager menuManager = w.getMenuManager();
 			menuManager.update(IAction.TEXT);
    	}
    }
    /**
     * Returns the definition id for <code>accelerator</code>
     */
    public String getDefinitionId(int accelerator) {
    	if (activeService == null) 
    		return null;

		Manager keyManager = Manager.getInstance();
		Machine keyMachine = keyManager.getKeyMachine();        
		KeySequence mode = keyMachine.getMode();
		List keyStrokes = new ArrayList(mode.getKeyStrokes());
		keyStrokes.add(KeyStroke.create(accelerator));
    	KeySequence keySequence = KeySequence.create(keyStrokes);    		
		Map keySequenceMapForMode = keyMachine.getKeySequenceMapForMode();
		SortedSet matchSet = (SortedSet) keySequenceMapForMode.get(keySequence);

		if (matchSet != null && matchSet.size() == 1) {
			KeyBindingMatch match = (KeyBindingMatch) matchSet.iterator().next();				
			return match.getKeyBinding().getCommand();
		}
		
		return null;
    }

	/**
	 * Update the KeyBindingMenu with the current set of accelerators.
	 */
	public void updateAccelerators() {
		Manager keyManager = Manager.getInstance();
		Machine keyMachine = keyManager.getKeyMachine();      		
		KeySequence mode = keyMachine.getMode();
		List keyStrokes = mode.getKeyStrokes();
		int size = keyStrokes.size();
		
		Map keySequenceMapForMode = keyMachine.getKeySequenceMapForMode();
		SortedSet keyStrokeSetForMode = new TreeSet();
		Iterator iterator = keySequenceMapForMode.keySet().iterator();

		while (iterator.hasNext()) {
			KeySequence keySequence = (KeySequence) iterator.next();
			
			if (keySequence.isChildOf(mode, false))
				keyStrokeSetForMode.add(keySequence.getKeyStrokes().get(size));	
		}

	   	iterator = keyStrokeSetForMode.iterator();
	   	int[] accelerators = new int[keyStrokeSetForMode.size()];
		int i = 0;
			   	
	   	while (iterator.hasNext()) {
	   		KeyStroke keyStroke = (KeyStroke) iterator.next();
	   		accelerators[i++] = keyStroke.getValue();	   		
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
			public void widgetSelected(SelectionEvent selectionEvent) {
				Event event = new Event();
				event.item = selectionEvent.item;
				event.detail = selectionEvent.detail;
				event.x = selectionEvent.x;
				event.y = selectionEvent.y;
				event.width = selectionEvent.width;
				event.height = selectionEvent.height;
				event.stateMask = selectionEvent.stateMask;
				event.doit = selectionEvent.doit;
				event.data = selectionEvent.data;
				event.display = selectionEvent.display;
				event.time = selectionEvent.time;
				event.widget = selectionEvent.widget;
				pressed(KeyStroke.create(selectionEvent.detail), event);
			}
		});

		if (mode.getKeyStrokes().size() == 0)
			accMenu.removeVerifyListener(verifyListener);
		else
			accMenu.addVerifyListener(verifyListener);
	}


	/**
	 * Contribution item for the status line.
	 */
	private static class KeyModeContributionItem extends ContributionItem {
		/**
		 * Precomputed label width hint
		 */
		private int fixedWidth = -1;
		/**
		 * Current message to display
		 */
		private String text;
		/** 
		 * The status line label widget
		 */
		private CLabel label;
	
		/**
		 * Creates a new item with the given id.
		 * 
		 * @param id the item's id
		 */
		public KeyModeContributionItem(String id) {
			super(id);
		}
	
		/**
		 * Sets the message for this contribution. If the message
		 * is valid, then the contribution will be made visible,
		 * otherwise the contribution will be hidden.
		 *  
		 * @param msg the message to show, or <code>null</code>
		 */
		public void setText(String msg) {
			text = msg;
			if (label != null && !label.isDisposed()) {
				label.setText(text);
			}
			if (text == null || text.length() < 1) {
				if (isVisible()) {
					setVisible(false);
					getParent().update(true);
				}
			} else {
				if (!isVisible()) {
					setVisible(true);
					getParent().update(true);
				}
			}
		}
	
		/*
		 * @see IContributionItem#fill(Composite)
		 */
		public void fill(Composite parent) {
			label= new CLabel(parent, SWT.SHADOW_IN);
			StatusLineLayoutData data = new StatusLineLayoutData();
			if (fixedWidth < 0) {
				GC gc = new GC(parent);
				gc.setFont(parent.getFont());
				fixedWidth = gc.getFontMetrics().getAverageCharWidth() * 40;
				gc.dispose();
			}
			data.widthHint = fixedWidth;
			label.setLayoutData(data);
		
			if (text != null)
				label.setText(text);
		}
	}
}
