package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.Accelerator;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
import org.eclipse.ui.internal.registry.AcceleratorSet;

/* (non-Javadoc)
 * @see IKeyBindingService
 */
public class KeyBindingService implements IKeyBindingService {
	/**
	 * Table of key codes (key type: <code>String</code>,
	 * value type: <code>Integer</code>); <code>null</code>
	 * if not yet initialized.
	 * @see #findKeyCode
	 */
	private static Map keyCodes = null;
	
	private PartListener partListener;
	private String activeAccelConfigId;
	private String activeAccelScopeId;
	private List activePartActions;
	private Mode activeMode;
	private AcceleratorRegistry registry;
	private WorkbenchWindow window;
	
	// keys: menu items (MenuItems)
	// values: accelerators (Accelerators)
	private HashMap menuItemAccelerators;
	
	// keys: action definition ids (Strings)
	// values: accelerator keys (Integers)
	private HashMap activeAccelConfig;
	
	// keys: accelerator keys (Integers)
	// values: IActions or Lists of ModeOptions
	private HashMap activeKeyBindings;
	
	public KeyBindingService(WorkbenchWindow window) {
		this.window = window;
		//  use defaults for now
		activeAccelConfigId = IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID;
		activeAccelConfigId = "org.eclipse.ui.emacsAcceleratorConfiguration";
		activeAccelScopeId = IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID;
		activeKeyBindings = new HashMap();
		menuItemAccelerators = new HashMap();
		activePartActions = new ArrayList();
		partListener = new PartListener();
		registry = new AcceleratorRegistry();
		registry.load();
		IWorkbenchPage[] pages = window.getPages();
		for(int i=0; i<pages.length;i++) {
			pages[i].addPartListener(partListener);
		}
		window.addPageListener(new IPageListener() {
			public void pageActivated(IWorkbenchPage page){}
			public void pageClosed(IWorkbenchPage page){}
			public void pageOpened(IWorkbenchPage page){
				page.addPartListener(partListener);
			}
		});
		createActiveAccelConfig();		
	}
	
	/*
	 * Creates the active accelerator configuration
	 */
	private void createActiveAccelConfig() {
		activeAccelConfig = createAccelConfig(activeAccelConfigId);
	}
	
	/*
	 * Creates the accelerator configuration for the given accelerator
	 * configuration id
	 * <p>
	 * Does not implement conflict resolution yet. This requires some thought
	 * and may be a very slow process. If conflicts are not likely, it may be
	 * necessary to leave this ability out for now.
	 * <p>
	 * This method should parse the "||" out of accelerator key strings and
	 * split the text into a list of accelerator key sequences (a list of
	 * lists of accelerator key codes). This information will be used in
	 * partActivated(IWorkbenchPart) to build the key bindings table.
	 */
	private HashMap createAccelConfig(String accelConfigId) {
//		HashMap reverseAccelConfig = new HashMap();
		HashMap accelConfig = new HashMap();
		List acceleratorSets = registry.getSetsOf(accelConfigId);
		
		for(int i=0; i<acceleratorSets.size(); i++) {
			HashSet accelerators = ((AcceleratorSet)(acceleratorSets.get(i))).getAccelerators();
			Iterator accelIterator = accelerators.iterator();
			while(accelIterator.hasNext()) {
				Accelerator accelerator = (Accelerator)(accelIterator.next());
//				Object previousValue = reverseAccelConfig.put(accelerator.getKey(), accelerator.getId());
//				if(previousValue != null) {
//					// resolve conflict
//					// left undone for now
//				}
				accelConfig.put(accelerator.getId(),accelerator.getKey());
			}
		}
		return accelConfig;		
	}
	/**
	 * Parses the given accelerator text, and converts it to a
	 * list (possibly of length 1) of accelerator key codes.
	 *
	 * @param acceleratorText the accelerator text
	 * @return the SWT key code list, or an empty list if there is no accelerator
	 */
	private List convertAccelerator(String acceleratorText) {
		/*
		 * Doesn't deal with || yet
		 */
		List accelerators = new ArrayList();
		
		StringTokenizer sequenceTok = new StringTokenizer(acceleratorText);
		
		while (sequenceTok.hasMoreTokens()) {
			int accelerator = 0;
			StringTokenizer keyTok = new StringTokenizer(sequenceTok.nextToken(), "+");    //$NON-NLS-1$
			int keyCode = -1;
			
			while (keyTok.hasMoreTokens()) {
				String token = keyTok.nextToken();
				// Every token except the last must be one of the modifiers
				// Ctrl, Shift, or Alt.
				if (keyTok.hasMoreTokens()) {
					int modifier = findModifier(token);
					if (modifier != 0) {
						accelerator |= modifier;
					} else {//Leave if there are none
						return new ArrayList();
					}
				} else {
					keyCode = findKeyCode(token);
				}
			}
			if (keyCode != -1) {
				accelerator |= keyCode;
			}
			accelerators.add(new Integer(accelerator));
		}
		return accelerators;
	}
	/**
	 * Maps a standard keyboard key name to an SWT key code.
	 * Key names are converted to upper case before comparison.
	 * If the key name is a single letter, for example "S", its character code is returned.
	 * <p>
	 * The following key names are known (case is ignored):
	 * <ul>
	 * 	<li><code>"BACKSPACE"</code></li>
	 *  <li><code>"TAB"</code></li>
	 *  <li><code>"RETURN"</code></li>
	 *  <li><code>"ENTER"</code></li>
	 *  <li><code>"ESC"</code></li>
	 *  <li><code>"ESCAPE"</code></li>
	 *  <li><code>"DELETE"</code></li>
	 *  <li><code>"SPACE"</code></li>
	 *  <li><code>"ARROW_UP"</code>, <code>"ARROW_DOWN"</code>,
	 *     <code>"ARROW_LEFT"</code>, and <code>"ARROW_RIGHT"</code></li>
	 *  <li><code>"PAGE_UP"</code> and <code>"PAGE_DOWN"</code></li>
	 *  <li><code>"HOME"</code></li>
	 *  <li><code>"END"</code></li>
	 *  <li><code>"INSERT"</code></li>
	 *  <li><code>"F1"</code>, <code>"F2"</code> through <code>"F12"</code></li>
	 * </ul>
	 * </p>
	 *
	 * @param token the key name
	 * @return the SWT key code, <code>-1</code> if no match was found
	 * @see org.eclipse.swt.SWT
	 */
	public static int findKeyCode(String token) {
		if (keyCodes == null)
			initKeyCodes();
		token= token.toUpperCase();
		Integer i= (Integer) keyCodes.get(token);
		if (i != null) 
			return i.intValue();
		if (token.length() == 1)
			return token.charAt(0);
		return -1;
	}
	/**
	 * Maps standard keyboard modifier key names to the corresponding 
	 * SWT modifier bit. The following modifier key names are recognized 
	 * (case is ignored): <code>"CTRL"</code>, <code>"SHIFT"</code>, and
	 * <code>"ALT"</code>.
	 * The given modifier key name is converted to upper case before comparison.
	 *
	 * @param token the modifier key name
	 * @return the SWT modifier bit, or <code>0</code> if no match was found
	 * @see org.eclipse.swt.SWT
	 */
	public static int findModifier(String token) {
		token= token.toUpperCase();
		if (token.equals("CTRL"))//$NON-NLS-1$
			return SWT.CTRL;
		if (token.equals("SHIFT"))//$NON-NLS-1$
			return SWT.SHIFT;
		if (token.equals("ALT"))//$NON-NLS-1$
			return SWT.ALT;
		return 0;
	}
	
	/**
	 * @see IKeyBindingService
	 */
    public String getActiveAcceleratorConfigurationId() {
    	return activeAccelConfigId;
    }
	/**
	 * @see IKeyBindingService
	 */    
    public String getActiveAcceleratorScopeId() {
    	return activeAccelScopeId;
    }
	/**
	 * @see IKeyBindingService
	 */      
    public boolean processKey(Event event) {
    	if(activeMode==null) {
    		/*
    		 * No mode is active. Process the current key in context of the
    		 * key binding service's normal key binding service.
    		 */
 			return processKey(event, activeKeyBindings);
    	} else {
    		/* 
    		 * A mode is active. Process the current key in context of the
    		 * active mode.
    		 */
    		return processKey(event, activeMode.getKeyBindings());	
    	}
    }
    private Integer convertEvent(Event event) {
    	if(event.stateMask == SWT.CONTROL) {
    		return new Integer(event.stateMask | Character.toUpperCase(event.character) + 64);
    	}
    	return new Integer(event.stateMask | event.character);
    }
    /*
     * Receives a key event and a key bindings table. Processes the key if
     * it is found in the key bindings table. Returns true if the key is
     * consumed, false otherwise.
     */
    private boolean processKey(Event event, HashMap keyBindings) {
	 	Object o = keyBindings.get(convertEvent(event));  	
		if(o != null) {
    		if(o instanceof IAction) {
				IAction action = (IAction)o;
				action.runWithEvent(event);
				return true;
    		}
    		if(o instanceof List) {
    			/* 
    			 * the key processed is the first key for a multi-key sequence.
    			 * Go into the mode for the first key.
    			 */
    			activeMode = new Mode(new Integer(event.keyCode));
    			activeMode.addOptions((List)o);
    			
    			//display status line message
    			//complete implementation later
   				
    			return true;
    		}
    	}
    	return false;	 	
    }
    
	/**
	 * @see IKeyBindingService
	 */ 
    public void registerAction(IAction action) {
		activePartActions.add(action);
    }
	/**
	 * @see IKeyBindingService
	 */     
    public void setActiveAcceleratorScopeId(String scopeId) {
    	activeAccelScopeId = scopeId;
    }
    
    /**
     * Sets the active accelerator configuration to be the configuration
     * with the given id.
     */
    public void setActiveAcceleratorConfiguration(String configId) {
    	// set the new active configuration id
    	activeAccelConfigId = configId;
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
     * submenus and their submenus, etc.).
     */
    private void clearAccelerators(MenuItem item) {
    	if(item.getMenu()!=null) {
    		clearAccelerators(item.getMenu());	
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
    
    /* 
     * Given a list of available actions, creates a key bindings table.
     */   
    private void createKeyBindingsTable(List availableActions) {
    	Iterator iterator = availableActions.iterator();
		//accelerators in activeAccelConfig which are applicable in the current scope
		HashMap activeAcceleratorsForScope = registry.getAcceleratorsOf(activeAccelConfigId, activeAccelScopeId);
		//accelerators in defaultAccelConfig which are applicable in the current scope
		HashMap defaultAcceleratorsForScope = registry.getAcceleratorsOf(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID, activeAccelScopeId);	
		while(iterator.hasNext()) {
			Object o = iterator.next();
			IAction action = (IAction)o;
			String actionDefID = action.getActionDefinitionId();
			String keyText;
			List keyCodes = null;
			
			//try to get the accelerator key text from the active accelerators
			keyText = (String)(activeAcceleratorsForScope.get(actionDefID));
			if(keyText==null) {
				//try to get the accelerator key text from the default accelerators
				keyText = (String)(defaultAcceleratorsForScope.get(actionDefID));
			}
					
			if(keyText!=null) {
				// get the sequence of accelerator key codes
				keyCodes = convertAccelerator(keyText);
			} else {
				// actionDefID not found in active or default accelerators
				/*
				 *  set the sequence of accelerator key codes to be a list
				 * with one element: the accelerator defined by the action
				 */
				List list = new ArrayList();
				if(action.getAccelerator() != 0) {
					list.add(new Integer(action.getAccelerator()));
					keyCodes = list;
				}
			}
					
			// if the action has an accelerator
			if(keyCodes!=null) {
				createKeyBinding(keyCodes, action);
			}
		}
    }
    
    /* 
     * Given a sequence of accelerator key codes and an action,
     * creates a key binding for the first key code in the sequence and
     * puts the key binding in the key bindings table.
     */
    private void createKeyBinding(List keyCodes, IAction action) {
    	// if the sequence of keys is of length one
		if(keyCodes.size()==1) {
			// put the mapping of the key code to the action into the map
			activeKeyBindings.put(keyCodes.get(0), action);
		} else {
			Object previousValue = activeKeyBindings.put(keyCodes.get(0), new ModeOption(action, keyCodes.subList(1,keyCodes.size())));
			// if there was already a mapping for the first key in the sequence
			if(previousValue!=null) {
				// if there is only one option previosly listed for this key code
				if(previousValue instanceof ModeOption) {
					//put a list of both options into the map
					List list = new ArrayList();
					list.add(new ModeOption(action, keyCodes.subList(1,keyCodes.size())));
					list.add(previousValue);
					activeKeyBindings.put(keyCodes.get(0), list);
				}
				// if several options were previously listed for this key code	
				if(previousValue instanceof List) {
					// add the new option to the list in the map
					List list = (List)previousValue;
					list.add(new ModeOption(action, keyCodes.subList(1,keyCodes.size())));
					activeKeyBindings.put(keyCodes.get(0), list);	
				}
			}
		}	
    }
    
	/** 
	 * Initializes the internal key code table.
	 */
	private static void initKeyCodes() {
		
		keyCodes = new HashMap(40);
	
		keyCodes.put("BACKSPACE", new Integer(8));//$NON-NLS-1$
		keyCodes.put("TAB", new Integer(9));//$NON-NLS-1$
		keyCodes.put("RETURN", new Integer(13));//$NON-NLS-1$
		keyCodes.put("ENTER", new Integer(13));//$NON-NLS-1$
		keyCodes.put("ESCAPE", new Integer(27));//$NON-NLS-1$
		keyCodes.put("ESC", new Integer(27));//$NON-NLS-1$
		keyCodes.put("DELETE", new Integer(127));//$NON-NLS-1$
	
		keyCodes.put("SPACE", new Integer(' '));//$NON-NLS-1$
		keyCodes.put("ARROW_UP", new Integer(SWT.ARROW_UP));//$NON-NLS-1$
		keyCodes.put("ARROW_DOWN", new Integer(SWT.ARROW_DOWN));//$NON-NLS-1$
		keyCodes.put("ARROW_LEFT", new Integer(SWT.ARROW_LEFT));//$NON-NLS-1$
		keyCodes.put("ARROW_RIGHT", new Integer(SWT.ARROW_RIGHT));//$NON-NLS-1$
		keyCodes.put("PAGE_UP", new Integer(SWT.PAGE_UP));//$NON-NLS-1$
		keyCodes.put("PAGE_DOWN", new Integer(SWT.PAGE_DOWN));//$NON-NLS-1$
		keyCodes.put("HOME", new Integer(SWT.HOME));//$NON-NLS-1$
		keyCodes.put("END", new Integer(SWT.END));//$NON-NLS-1$
		keyCodes.put("INSERT", new Integer(SWT.INSERT));//$NON-NLS-1$
		keyCodes.put("F1", new Integer(SWT.F1));//$NON-NLS-1$
		keyCodes.put("F2", new Integer(SWT.F2));//$NON-NLS-1$
		keyCodes.put("F3", new Integer(SWT.F3));//$NON-NLS-1$
		keyCodes.put("F4", new Integer(SWT.F4));//$NON-NLS-1$
		keyCodes.put("F5", new Integer(SWT.F5));//$NON-NLS-1$
		keyCodes.put("F6", new Integer(SWT.F6));//$NON-NLS-1$
		keyCodes.put("F7", new Integer(SWT.F7));//$NON-NLS-1$
		keyCodes.put("F8", new Integer(SWT.F8));//$NON-NLS-1$
		keyCodes.put("F9", new Integer(SWT.F9));//$NON-NLS-1$
		keyCodes.put("F10", new Integer(SWT.F10));//$NON-NLS-1$
		keyCodes.put("F11", new Integer(SWT.F11));//$NON-NLS-1$
		keyCodes.put("F12", new Integer(SWT.F12));//$NON-NLS-1$
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
	    	} else if(part instanceof IEditorPart) {
	    		if(isParticipating(part)) {
	    			// remove accelerators from menu
	    			Menu menu = window.getMenuManager().getMenu();
					clearAccelerators(menu);
	    			/* 
	    			 * calculate active key bindings as per algorithm 
	    			 * provided in key bindings proposal 
	    			 */
	    			List availableActions = new ArrayList();
	    			availableActions.addAll(activePartActions);
//	    			availableActions.add(activeActionSet);
//	    			availableActions.add(workbenchActions);
					createKeyBindingsTable(availableActions);
	    		}
	    		else {
	    			// if the editor being activated is not a "participating" editor
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
	    	activePartActions.clear();
	    }
	    
	    /**
	     * @see IPartListener
	     */    
	    public void partOpened(IWorkbenchPart part) {
	    }    
    }
}
