/*******************************************************************************
 * Copyright (c) 2008, 2009 Adobe Systems, Inc. and others.
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * The CocoaUIEnhancer provides the standard "About" and "Preference" menu items
 * and links them to the corresponding workbench commands. 
 * This must be done in a MacOS X fragment because SWT doesn't provide an abstraction
 * for the (MacOS X only) application menu and we have to use MacOS specific natives.
 * The fragment is for the org.eclipse.ui plug-in because we need access to the
 * Workbench "About" and "Preference" actions.
 * 
 * @noreference this class is not intended to be referenced by any client.
 * @since 1.0
 */
public class CocoaUIEnhancer implements IStartup {

	private static final int kAboutMenuItem = 0;
	private static final int kPreferencesMenuItem = 2;
	private static final int kHideApplicationMenuItem = 6;
	private static final int kQuitMenuItem = 10;
	
	static long sel_toolbarButtonClicked_;
	static long sel_preferencesMenuItemSelected_;
	static long sel_aboutMenuItemSelected_;

	private static final long NSWindowToolbarButton = 3;
	
	/* This callback is not freed */
	static Callback proc3Args;
	static final byte[] SWT_OBJECT = {'S', 'W', 'T', '_', 'O', 'B', 'J', 'E', 'C', 'T', '\0'};

	private void init() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		// TODO: These should either move out of Display or be accessible to this class.
		byte[] types = {'*','\0'};
		int size = C.PTR_SIZEOF, align = C.PTR_SIZEOF == 4 ? 2 : 3;

		Class clazz = CocoaUIEnhancer.class;

		proc3Args = new Callback(clazz, "actionProc", 3); //$NON-NLS-1$
		//call getAddress
		Method getAddress = Callback.class.getMethod("getAddress", new Class[0]);
		Object object = getAddress.invoke(proc3Args, null);
		long proc3 = convertToLong(object);
		if (proc3 == 0) SWT.error (SWT.ERROR_NO_MORE_CALLBACKS);

		//call objc_allocateClassPair
		Field field = OS.class.getField("class_NSObject");
		Object fieldObj = field.get(OS.class);
		object = invokeMethod(OS.class, "objc_allocateClassPair", new Object[] { fieldObj, "SWTCocoaEnhancerDelegate", wrapPointer(0) });
		long cls = convertToLong(object);
		
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
		
		try {
			if (sel_toolbarButtonClicked_ == 0) {
				sel_toolbarButtonClicked_ = registerName("toolbarButtonClicked:"); //$NON-NLS-1$
				sel_preferencesMenuItemSelected_ = registerName("preferencesMenuItemSelected:"); //$NON-NLS-1$
				sel_aboutMenuItemSelected_ = registerName("aboutMenuItemSelected:"); //$NON-NLS-1$
				init();
			}
		} catch (Exception e) {
			// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
		}
    }

    
    private long registerName(String name) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	Class clazz = OS.class;
    	Object object = invokeMethod(clazz, "sel_registerName", new Object[] {name});
    	return convertToLong(object);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    public void earlyStartup() {
        final Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            public void run() {
            	try {
            		delegate = new SWTCocoaEnhancerDelegate();
            		delegate.alloc().init();
            		//call OS.NewGlobalRef
					Method method = OS.class.getMethod("NewGlobalRef", new Class[] { Object.class });
					Object object = method.invoke(OS.class, new Object[] {CocoaUIEnhancer.this});
					delegateJniRef = convertToLong(object);
            	} catch (Exception e) {
					// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
					// not expected to happen at all.
					log(e);
				}
        		if (delegateJniRef == 0) SWT.error(SWT.ERROR_NO_HANDLES);
				try {
					Field idField = SWTCocoaEnhancerDelegate.class.getField("id");
					Object idValue = idField.get(delegate);
					invokeMethod(OS.class, "object_setInstanceVariable",
							new Object[] { idValue,
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
									// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
									// not expected to happen at all.
									log(e);
								}
							}
							delegateJniRef = 0;

							if (delegate != null)
								delegate.release();
							delegate = null;

						}
					});

					// modify all shells opened on startup
					IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
							.getWorkbenchWindows();
					for (int i = 0; i < windows.length; i++) {
						modifyWindowShell(windows[i]);
					}
				} catch (Exception e) {
					// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
					// not expected to happen at all.
					log(e);
				}
			}

        });
    }

	void log(Exception e) {
		StatusUtil.handleStatus(e, StatusManager.LOG);
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
	 * Modify the given workbench window shell bits to show the tool bar toggle
	 * button.
	 * 
	 * @param window
	 *            the window to modify
	 * @since 3.2
	 */
	protected void modifyWindowShell(IWorkbenchWindow window) {
		// only add the button when either the cool bar or perspective bar
		// is initially visible. This is so that RCP applications can choose to use
		// this fragment without fear that their explicitly invisible bars
		// can't be shown.
		boolean coolBarInitiallyVsible = ((WorkbenchWindow) window)
				.getCoolBarVisible();
		boolean perspectiveBarInitiallyVsible = ((WorkbenchWindow) window)
				.getPerspectiveBarVisible();

		if (coolBarInitiallyVsible || perspectiveBarInitiallyVsible) {
			// Add an empty, hidden tool bar to the window.  Without this the
			// tool bar button at the top right of the window will not appear
			// even when setShowsToolbarButton(true) is called.
			NSToolbar dummyBar = new NSToolbar();
			dummyBar.alloc();
			dummyBar.initWithIdentifier(NSString.stringWith("SWTToolbar")); //$NON-NLS-1$
			dummyBar.setVisible(false);
			
			Shell shell = window.getShell();
			NSWindow nsWindow = shell.view.window();
			nsWindow.setToolbar(dummyBar);
			dummyBar.release();
			nsWindow.setShowsToolbarButton(true);
			
			// Override the target and action of the toolbar button so we can control it.
			try {
				Object fieldValue = wrapPointer(NSWindowToolbarButton);
				NSButton toolbarButton = (NSButton) invokeMethod(NSWindow.class, nsWindow, "standardWindowButton", new Object[] {fieldValue});
				if (toolbarButton != null) {
					toolbarButton.setTarget(delegate);
					invokeMethod(NSControl.class, toolbarButton, "setAction",
							new Object[] { wrapPointer(sel_toolbarButtonClicked_) });
				}
			} catch (Exception e) {
				// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
				// not expected to happen at all.
				log(e);
			}
		}
	}
	
    private void hookApplicationMenu() {
    	try {
    		// create About Eclipse menu command
    		NSMenu mainMenu = NSApplication.sharedApplication().mainMenu();
    		NSMenuItem mainMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, mainMenu, "itemAtIndex", new Object[] {wrapPointer(0)});
    		NSMenu appMenu = mainMenuItem.submenu();

    		// add the about action
    		NSMenuItem aboutMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, appMenu, "itemAtIndex", new Object[] {wrapPointer(kAboutMenuItem)});
    		aboutMenuItem.setTitle(NSString.stringWith(fAboutActionName));

    		// rename the hide action if we have an override string
    		if (fHideActionName != null) {
    			NSMenuItem hideMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, appMenu, "itemAtIndex", new Object[] {wrapPointer(kHideApplicationMenuItem)});
    			hideMenuItem.setTitle(NSString.stringWith(fHideActionName));
    		}

    		// rename the quit action if we have an override string
    		if (fQuitActionName != null) {
    			NSMenuItem quitMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, appMenu, "itemAtIndex", new Object[] {wrapPointer(kQuitMenuItem)});
    			quitMenuItem.setTitle(NSString.stringWith(fQuitActionName));
    		}

    		// enable pref menu
    		NSMenuItem prefMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, appMenu, "itemAtIndex", new Object[] {wrapPointer(kPreferencesMenuItem)});
    		prefMenuItem.setEnabled(true);

    		// Register as a target on the prefs and quit items.
    		prefMenuItem.setTarget(delegate);
    		invokeMethod(NSMenuItem.class, prefMenuItem, "setAction", new Object[] {wrapPointer(sel_preferencesMenuItemSelected_)});
    		aboutMenuItem.setTarget(delegate);
    		invokeMethod(NSMenuItem.class, aboutMenuItem, "setAction", new Object[] {wrapPointer(sel_aboutMenuItemSelected_)});
    	} catch (Exception e) {
			// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
		}
    }

	/**
     * Locate an action with the given id in the current menu bar and run it.
     */
    private void runAction(String actionId) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
        	IMenuManager manager = ((WorkbenchWindow)window).getActionBars().getMenuManager();
        	IAction action = findAction(actionId, manager);
        	if (action != null && action.isEnabled()) {
        		try {
        			NSMenu mainMenu = NSApplication.sharedApplication().mainMenu();
        			NSMenuItem mainMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, mainMenu, "itemAtIndex", new Object[] {wrapPointer(0)});
        			NSMenu appMenu = mainMenuItem.submenu();
        			NSMenuItem aboutMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, appMenu, "itemAtIndex", new Object[] {wrapPointer(kAboutMenuItem)});
        			NSMenuItem prefMenuItem = (NSMenuItem) invokeMethod(NSMenu.class, appMenu, "itemAtIndex", new Object[] {wrapPointer(kPreferencesMenuItem)});
        			try {
        				prefMenuItem.setEnabled(false);
        				aboutMenuItem.setEnabled(false);
        				action.run();
        			}
        			finally {
        				prefMenuItem.setEnabled(true);
        				aboutMenuItem.setEnabled(true);
        			}
        		} catch (Exception e) {
					// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
					// not expected to happen at all.
					log(e);
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
	 * Find the action with the given ID by recursively crawling the provided
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
		try {
			NSWindow window = source.window();
			Field idField = NSWindow.class.getField("id");
			Object idValue = idField.get(window);
			
			Display display = Display.getCurrent();
			Widget widget = (Widget) invokeMethod(Display.class, display, "findWidget", new Object[] { idValue });

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
		} catch (Exception e) {
			// theoretically, one of SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
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
		Class PTR_CLASS =  C.PTR_SIZEOF == 8 ? long.class : int.class;
		Constructor constructor = clazz
				.getConstructor(new Class[] { PTR_CLASS });
		return (NSControl) constructor.newInstance(new Object[] { wrapPointer(arg0) });
	}
	
	/**
	 * Specialized method.  It's behavior is isolated and different enough from the usual invocation that custom code is warranted.
	 */
	private static long[] OS_object_getInstanceVariable(long delegateId,
			byte[] name) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		Class clazz = OS.class;
		Method method = null;
		Class PTR_CLASS =  C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class) {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] {
					long.class, byte[].class, long[].class });
			long[] resultPtr = new long[1];
			method.invoke(null, new Object[] { new Long(delegateId), name,
					resultPtr });
			return resultPtr;
		} 
		else {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] {
					int.class, byte[].class, int[].class });
			int[] resultPtr = new int[1];
			method.invoke(null, new Object[] { new Integer((int) delegateId),
					name, resultPtr });
			return new long[] { resultPtr[0] };
		}
	}
	
	private long convertToLong(Object object) {
		if (object instanceof Integer) {
			Integer i = (Integer) object;
			return i.longValue();
		}
		if (object instanceof Long) {
			Long l = (Long) object;
			return l.longValue();
		}
		return 0;
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
		Class PTR_CLASS =  C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class)
			return new Long(value);
		else 
			return new Integer((int)value);
	}
}
