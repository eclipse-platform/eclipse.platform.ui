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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.text.WrappedPlainView;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.C;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSButton;
import org.eclipse.swt.internal.cocoa.NSControl;
import org.eclipse.swt.internal.cocoa.NSMenu;
import org.eclipse.swt.internal.cocoa.NSMenuItem;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.NSToolbar;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchWindow;

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
	static final Class PTR_CLASS =  C.PTR_SIZEOF == 8 ? long.class : int.class;
	static final String SWT_OBJECT = "SWT_OBJECT"; //$NON-NLS-1$
	static final long sel_toolbarButtonClicked_ = OS.sel_registerName("toolbarButtonClicked:"); //$NON-NLS-1$
	static final long sel_preferencesMenuItemSelected_ = OS.sel_registerName("preferencesMenuItemSelected:"); //$NON-NLS-1$
	static final long sel_aboutMenuItemSelected_ = OS.sel_registerName("aboutMenuItemSelected:"); //$NON-NLS-1$

	static {
		String className = "SWTCocoaEnhancerDelegate"; //$NON-NLS-1$

		// TODO: These should either move out of Display or be accessible to this class.
		String types = "*"; //$NON-NLS-1$
		int size = C.PTR_SIZEOF, align = C.PTR_SIZEOF == 4 ? 2 : 3;

		Class clazz = CocoaUIEnhancer.class;

		proc3Args = new Callback(clazz, "actionProc", 3); //$NON-NLS-1$
		long proc3 = proc3Args.getAddress();
		if (proc3 == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);

		long cls = OS.objc_allocateClassPair(OS.class_NSObject, className, 0);
		try {
			invokeMethod(OS.class, "class_addIvar", new Object[] {
					wrapPointer(cls), SWT_OBJECT, wrapPointer(size),
					new Byte((byte) align), types });

			// Add the action callback
			invokeMethod(
					OS.class,
					"class_addMethod", new Object[] { wrapPointer(cls), wrapPointer(sel_toolbarButtonClicked_), wrapPointer(proc3), "@:@" }); //$NON-NLS-1$
			invokeMethod(OS.class, "class_addMethod", new Object[] {
					wrapPointer(cls),
					wrapPointer(sel_preferencesMenuItemSelected_),
					wrapPointer(proc3), "@:@" }); //$NON-NLS-1$
			invokeMethod(
					OS.class,
					"class_addMethod", new Object[] { wrapPointer(cls), wrapPointer(sel_aboutMenuItemSelected_), wrapPointer(proc3), "@:@" }); //$NON-NLS-1$

			invokeMethod(OS.class, "objc_registerClassPair",
					new Object[] { wrapPointer(cls) });
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	SWTCocoaEnhancerDelegate delegate;
	private long delegateJniRef;

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
				try {
					invokeMethod(OS.class, "object_setInstanceVariable",
							new Object[] { wrapPointer(delegate.id),
									SWT_OBJECT, wrapPointer(delegateJniRef) });

					hookApplicationMenu();
					hookWorkbenchListener();

					// schedule disposal of callback object
					display.disposeExec(new Runnable() {
						public void run() {
							if (delegateJniRef != 0) {
								try {
									invokeMethod(OS.class, "DeleteGlobalRef", new Object[] {wrapPointer(delegateJniRef)});
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							delegateJniRef = 0;

							if (delegate != null)
								delegate.release();
							delegate = null;

							if (proc3Args != null)
								proc3Args.dispose();
							proc3Args = null;
						}
					});

					// modify all shells opened on startup
					IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
							.getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						modifyWindowShell(windows[i]);
					}
				} catch (Exception e) {
					e.printStackTrace();
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
				try {
					modifyWindowShell(window);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
	protected void modifyWindowShell(IWorkbenchWindow window) throws Exception {
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
			invokeMethod(NSControl.class, toolbarButton, "setAction",
					new Object[] { wrapPointer(sel_toolbarButtonClicked_) });
		}
	}
	
    private void hookApplicationMenu()  throws Exception {
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
    	invokeMethod(NSMenuItem.class, appMenu.itemAtIndex(kPreferencesMenuItem), "setAction", new Object[] {wrapPointer(sel_preferencesMenuItemSelected_)});
    	appMenu.itemAtIndex(kAboutMenuItem).setTarget(delegate);
    	invokeMethod(NSMenuItem.class, appMenu.itemAtIndex(kAboutMenuItem), "setAction", new Object[] {wrapPointer(sel_aboutMenuItemSelected_)});
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

	static int actionProc(int id, int sel, int arg0) throws Exception {
		return (int) actionProc((long)id, (long)sel, (long)arg0);
	}

	static long actionProc(long id, long sel, long arg0) throws Exception {
		long [] jniRef = OS_object_getInstanceVariable(id, SWT_OBJECT);
		if (jniRef[0] == 0) return 0;
		
		CocoaUIEnhancer delegate = (CocoaUIEnhancer) invokeMethod(OS.class,
				"JNIGetObject", new Object[] { wrapPointer(jniRef[0]) });
		
		if (sel == sel_toolbarButtonClicked_) {
			NSControl source = new_NSControl(arg0);
			delegate.toolbarButtonClicked(source);
		} else if (sel == sel_preferencesMenuItemSelected_) {
			delegate.preferencesMenuItemSelected();
		} else if (sel == sel_aboutMenuItemSelected_) {
			delegate.aboutMenuItemSelected();
		}
		
		return 0;
	}
	
	

	// The following methods reflectively call corresponding methods in the OS
	// class, using ints or longs as required based on platform.

	private static NSControl new_NSControl(long arg0)
			throws NoSuchMethodException, InstantiationException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Class clazz = NSControl.class;
		Constructor constructor = clazz
				.getConstructor(new Class[] { PTR_CLASS });
		return (NSControl) constructor.newInstance(new Object[] { wrapPointer(arg0) });
	}
	
	/**
	 * Specialized method.  It's behaviour is isolated and different enough from the usual invocation that custom code is warranted.
	 */
	private static long[] OS_object_getInstanceVariable(long delegateId,
			String name) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		Class clazz = OS.class;
		Method method = null;
		if (PTR_CLASS == long.class) {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] {
					long.class, String.class, long[].class });
			long[] resultPtr = new long[1];
			method.invoke(null, new Object[] { new Long(delegateId), name,
					resultPtr });
			return resultPtr;
		} 
		else {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] {
					int.class, String.class, int[].class });
			int[] resultPtr = new int[1];
			method.invoke(null, new Object[] { new Integer((int) delegateId),
					name, resultPtr });
			return new long[] { resultPtr[0] };
		}
	}
	
	private static Object invokeMethod(Class clazz, String methodName,
			Object[] args) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		return invokeMethod(clazz, null, methodName, args);
	}

	private static Object invokeMethod(Class clazz, Object target,
			String methodName, Object[] args) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		Class[] signature = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			Class thisClass = args[i].getClass();
			if (thisClass == Integer.class)
				signature[i] = int.class;
			else if (thisClass == Long.class)
				signature[i] = long.class;
			else if (thisClass == Byte.class)
				signature[i] = byte.class;
			else
				signature[i] = thisClass;
		}
		Method method = clazz.getMethod(methodName, signature);
		return method.invoke(target, args);
	}
	
	private static Object wrapPointer(long value) {
		if (PTR_CLASS == long.class)
			return new Long(value);
		else 
			return new Integer((int)value);
	}
}
