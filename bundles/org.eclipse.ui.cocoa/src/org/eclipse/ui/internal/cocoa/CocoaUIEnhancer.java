/*******************************************************************************
 * Copyright (c) 2008 Adobe Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adobe Systems, Inc. - initial API and implementation
 *     IBM Corporation - cleanup
 *******************************************************************************/
package org.eclipse.ui.internal.cocoa;

import java.text.*;
import java.util.*;

import org.eclipse.core.commands.*;
import org.eclipse.core.commands.common.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.swt.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.cocoa.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.handlers.*;
import org.eclipse.ui.internal.*;

/**
 * The CocoaUIEnhancer provides the standard "About" and "Preference" menu items
 * and links them to the corresponding workbench commands. 
 * This must be done in a MacOS X fragment because SWT doesn't provide an abstraction
 * for the (MacOS X only) application menu and we have to use MacOS specific natives.
 * The fragment is for the org.eclipse.ui plugin because we need access to the
 * Workbench "About" and "Preference" actions.
 * 
 * @noreference this class is not intended to be referenced by any client.
 * @since 1.0
 */
public class CocoaUIEnhancer implements IStartup {

	private static final int kAboutMenuItem = 0;
	private static final int kPreferencesMenuItem = 2;
	private static final int kServicesMenuItem = 4;
	private static final int kHideApplicationMenuItem = 6;
	private static final int kQuitMenuItem = 10;
	
	static Callback proc3Args;
	static final String SWT_OBJECT = "SWT_OBJECT"; //$NON-NLS-1$
	static final int /*long*/ sel_toolbarButtonClicked_ = OS.sel_registerName("toolbarButtonClicked:"); //$NON-NLS-1$
	static final int /*long*/ sel_preferencesMenuItemSelected_ = OS.sel_registerName("preferencesMenuItemSelected:"); //$NON-NLS-1$
	static final int /*long*/ sel_aboutMenuItemSelected_ = OS.sel_registerName("aboutMenuItemSelected:"); //$NON-NLS-1$

	static {
		String className = "SWTCocoaEnhancerDelegate"; //$NON-NLS-1$

		// TODO: These should either move out of Display or be accessible to this class.
		String types = "*"; //$NON-NLS-1$
		int size = C.PTR_SIZEOF, align = C.PTR_SIZEOF == 4 ? 2 : 3;

		Class clazz = CocoaUIEnhancer.class;

		proc3Args = new Callback(clazz, "actionProc", 3); //$NON-NLS-1$
		int /*long*/ proc3 = proc3Args.getAddress();
		if (proc3 == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);

		int /*long*/ cls = OS.objc_allocateClassPair(OS.class_NSObject, className, 0);
		OS.class_addIvar(cls, SWT_OBJECT, size, (byte)align, types);

		// Add the action callback
		OS.class_addMethod(cls, sel_toolbarButtonClicked_, proc3, "@:@"); //$NON-NLS-1$
		OS.class_addMethod(cls, sel_preferencesMenuItemSelected_, proc3, "@:@"); //$NON-NLS-1$
		OS.class_addMethod(cls, sel_aboutMenuItemSelected_, proc3, "@:@"); //$NON-NLS-1$

		OS.objc_registerClassPair(cls);
	}	

	SWTCocoaEnhancerDelegate delegate;
	private int /*long*/ delegateJniRef;

	/**
	 * Class that is able to intercept and handle OS events from the toolbar and menu.
	 * 
	 * @since 3.1
	 */

    private static final String RESOURCE_BUNDLE = CocoaUIEnhancer.class.getPackage().getName() + ".Messages"; //$NON-NLS-1$
	
    private String fAboutActionName;
    private String fQuitActionName;
    private String fHideActionName;

    /**
     * Default constructor
     */
    public CocoaUIEnhancer() {
        IProduct product = Platform.getProduct();
        String productName = null;
        if (product != null)
            productName = product.getName();
        
		ResourceBundle resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
		try {
			if (productName != null) {
				String format = resourceBundle.getString("AboutAction.format"); //$NON-NLS-1$
				if (format != null)
					fAboutActionName= MessageFormat.format(format, new Object[] { productName } );
			}
			if (fAboutActionName == null)
				fAboutActionName = resourceBundle.getString("AboutAction.name"); //$NON-NLS-1$
		} catch (MissingResourceException e) {
		}
		
		if (fAboutActionName == null)
			fAboutActionName = "About"; //$NON-NLS-1$
		
		if (productName != null) {	
			try {
				// prime the format Hide <app name>
				String format = resourceBundle.getString("HideAction.format"); //$NON-NLS-1$
				if (format != null)
					fHideActionName = MessageFormat.format(format,
							new Object[] { productName });

			} catch (MissingResourceException e) {
			}

			try {
				// prime the format Quit <app name>
				String format = resourceBundle.getString("QuitAction.format"); //$NON-NLS-1$
				if (format != null)
					fQuitActionName = MessageFormat.format(format,
							new Object[] { productName });

			} catch (MissingResourceException e) {
			}
		}
		
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    public void earlyStartup() {
        final Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            public void run() {
        		delegate = new SWTCocoaEnhancerDelegate();
        		delegate.alloc().init();
        		delegateJniRef = OS.NewGlobalRef(CocoaUIEnhancer.this);
        		if (delegateJniRef == 0) SWT.error(SWT.ERROR_NO_HANDLES);
        		OS.object_setInstanceVariable(delegate.id, SWT_OBJECT, delegateJniRef);		

        		hookApplicationMenu();
				hookWorkbenchListener();
				
		        // schedule disposal of callback object
		        display.disposeExec(new Runnable() {
		            public void run() {
		            	if (delegateJniRef != 0) OS.DeleteGlobalRef(delegateJniRef);
		            	delegateJniRef = 0;
		            	
		            	if (delegate != null) delegate.release();
		            	delegate = null;
		                
		            	if (proc3Args != null) proc3Args.dispose();
		                proc3Args = null;
		            }
		        });

				// modify all shells opened on startup
				IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
						.getWorkbenchWindows();
				for (int i = 0; i < windows.length; i++) {
					modifyWindowShell(windows[i]);
				}
			}
        });
    }

    /**
	 * Hooks a listener that tweaks newly opened workbench window shells with
	 * the proper OS flags.
	 * 
	 * @since 3.2
	 */
    protected void hookWorkbenchListener() {
		PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

			public void windowActivated(IWorkbenchWindow window) {
				// no-op
			}

			public void windowDeactivated(IWorkbenchWindow window) {
				// no-op
			}

			public void windowClosed(IWorkbenchWindow window) {
				// no-op
			}

			public void windowOpened(IWorkbenchWindow window) {
				modifyWindowShell(window);
			}});
	}

    /**
	 * Modify the given workbench window shell bits to show the toolbar toggle
	 * button.
	 * 
	 * @param window
	 *            the window to modify
	 * @since 3.2
	 */
	protected void modifyWindowShell(IWorkbenchWindow window) {
		// only add the button when either the coolbar or perspectivebar
		// is initially visible. This is so that RCP apps can choose to use
		// this fragment without fear that their explicitly invisble bars
		// can't be shown.
		boolean coolBarInitiallyVsible = ((WorkbenchWindow) window)
				.getCoolBarVisible();
		boolean perspectiveBarInitiallyVsible = ((WorkbenchWindow) window)
				.getPerspectiveBarVisible();

		if (coolBarInitiallyVsible || perspectiveBarInitiallyVsible) {
			// Add an empty, hidden toolbar to the window.  Without this the
			// toolbar button at the top right of the window will not appear
			// even when setShowsToolbarButton(true) is called.
			NSToolbar dummyBar = new NSToolbar();
			dummyBar.alloc();
			dummyBar.initWithIdentifier(NSString.stringWith("SWTToolbar")); //$NON-NLS-1$
			dummyBar.setVisible(false);
			
			Shell shell = window.getShell();
			NSWindow nsWindow = shell.view.window();
			nsWindow.setToolbar(dummyBar);
			nsWindow.setShowsToolbarButton(true);
			
			// Override the target and action of the toolbar button so we can control it.
			NSButton toolbarButton = nsWindow.standardWindowButton(OS.NSWindowToolbarButton);
			toolbarButton.setTarget(delegate);
			toolbarButton.setAction(sel_toolbarButtonClicked_);
		}
	}
	
    private void hookApplicationMenu() {
        // create About Eclipse menu command
    	NSMenu mainMenu = NSApplication.sharedApplication().mainMenu();
    	NSMenu appMenu = mainMenu.itemAtIndex(0).submenu();
    	
        // add the about action
    	NSMenuItem aboutMenuItem = appMenu.itemAtIndex(kAboutMenuItem);
    	aboutMenuItem.setTitle(NSString.stringWith(fAboutActionName));

    	// rename the hide action if we have an override string
    	if (fHideActionName != null) {
        	NSMenuItem hideMenuItem = appMenu.itemAtIndex(kHideApplicationMenuItem);
        	hideMenuItem.setTitle(NSString.stringWith(fHideActionName));
    	}
    	
    	// rename the quit action if we have an override string
    	if (fQuitActionName != null) {
        	NSMenuItem quitMenuItem = appMenu.itemAtIndex(kQuitMenuItem);
        	quitMenuItem.setTitle(NSString.stringWith(fQuitActionName));
    	}

    	// enable pref menu
    	appMenu.itemAtIndex(kPreferencesMenuItem).setEnabled(true);

    	// disable services menu
    	appMenu.itemAtIndex(kServicesMenuItem).setEnabled(false);

    	// Register as a target on the prefs and quit items.
    	appMenu.itemAtIndex(kPreferencesMenuItem).setTarget(delegate);
    	appMenu.itemAtIndex(kPreferencesMenuItem).setAction(sel_preferencesMenuItemSelected_);
    	appMenu.itemAtIndex(kAboutMenuItem).setTarget(delegate);
    	appMenu.itemAtIndex(kAboutMenuItem).setAction(sel_aboutMenuItemSelected_);
    }

    /**
     * Locate an action with the given id in the current menubar and run it.
     */
    private void runAction(String actionId) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
        	IMenuManager manager = ((WorkbenchWindow)window).getActionBars().getMenuManager();
        	IAction action = findAction(actionId, manager);
        	if (action != null && action.isEnabled()) {
        		NSMenu mainMenu = NSApplication.sharedApplication().mainMenu();
        		NSMenu appMenu = mainMenu.itemAtIndex(0).submenu();
        		try {
        			appMenu.itemAtIndex(kPreferencesMenuItem).setEnabled(false);
        	    	appMenu.itemAtIndex(kAboutMenuItem).setEnabled(false);
        	    	action.run();
        		}
        		finally {
        			appMenu.itemAtIndex(kPreferencesMenuItem).setEnabled(true);
        	    	appMenu.itemAtIndex(kAboutMenuItem).setEnabled(true);
        		}
        	}
        }
       
    }
    
    private void runCommand(String commandId) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		
		IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return;
		
		IHandlerService commandService = (IHandlerService) activeWorkbenchWindow
				.getService(IHandlerService.class);

		if (commandService != null) {
			try {
				commandService.executeCommand(commandId, null);
			} catch (ExecutionException e) {
			} catch (NotDefinedException e) {
			} catch (NotEnabledException e) {
			} catch (NotHandledException e) {
			}
		}
	}

    /**
	 * Find the action with the given ID by recursivly crawling the provided
	 * menu manager. If the action cannot be found <code>null</code> is
	 * returned.
	 * 
	 * @param actionId
	 *            the id to search for
	 * @param manager
	 *            the manager to search
	 * @return the action or <code>null</code>
	 */
	private IAction findAction(String actionId, IMenuManager manager) {
		IContributionItem[] items = manager.getItems();
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if (item instanceof ActionContributionItem) {
				ActionContributionItem aci = (ActionContributionItem) item;
				String id = aci.getId();
				if (id != null && id.equals(actionId))
					return aci.getAction();
			} else if (item instanceof IMenuManager) {
				IAction found = findAction(actionId, (IMenuManager) item);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	/*
	 * Action implementations for the toolbar button and preferences and about menu items
	 */
	void toolbarButtonClicked (NSControl source) {
		NSWindow window = source.window();
		Widget widget = Display.getCurrent().findWidget(window.id);
		
		if (!(widget instanceof Shell)) {
			return;
		}
		Shell shell = (Shell) widget;
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
		.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			if (windows[i].getShell() == shell) {
				runCommand("org.eclipse.ui.ToggleCoolbarAction"); //$NON-NLS-1$
			}
		}
	}
	
	void preferencesMenuItemSelected() {
		runAction("preferences"); //$NON-NLS-1$
	}

	void aboutMenuItemSelected() {
		runAction("about"); //$NON-NLS-1$
	}


	static int /*long*/ actionProc(int /*long*/ id, int /*long*/ sel, int /*long*/ arg0) {
		int /*long*/ [] jniRef = new int /*long*/ [1];
		OS.object_getInstanceVariable(id, SWT_OBJECT, jniRef);
		if (jniRef[0] == 0) return 0;
		
		CocoaUIEnhancer delegate = (CocoaUIEnhancer) OS.JNIGetObject(jniRef[0]); 
		
		if (sel == sel_toolbarButtonClicked_) {
			NSControl source = new NSControl(arg0);
			delegate.toolbarButtonClicked(source);
		} else if (sel == sel_preferencesMenuItemSelected_) {
			delegate.preferencesMenuItemSelected();
		} else if (sel == sel_aboutMenuItemSelected_) {
			delegate.aboutMenuItemSelected();
		}
		
		return 0;
	}

}
