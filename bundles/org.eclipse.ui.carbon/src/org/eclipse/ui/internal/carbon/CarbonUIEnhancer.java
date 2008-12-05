/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.carbon;

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
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.carbon.HICommand;
import org.eclipse.swt.internal.carbon.OS;
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
 * The CarbonUIEnhancer provides the standard "About" and "Preference" menu items
 * and links them to the corresponding workbench commands. 
 * This must be done in a MacOS X fragment because SWT doesn't provide an abstraction
 * for the (MacOS X only) application menu and we have to use MacOS specific natives.
 * The fragment is for the org.eclipse.ui plugin because we need access to the
 * Workbench "About" and "Preference" actions.
 * 
 * @noreference this class is not intended to be referenced by any client.
 * @since 4.0
 */
public class CarbonUIEnhancer implements IStartup {

	/**
	 * Class that is able to intercept and handle OS events from the toolbar and menu.
	 * 
	 * @since 3.1
	 */
    class Target {
    	
    	/**
    	 * Process OS toolbar event.
    	 * 
    	 * @param nextHandler unused
    	 * @param theEvent the OS event
    	 * @param userData unused
    	 * @return whether or not the event was handled by this processor
    	 */
		public int toolbarProc (int nextHandler, int theEvent, int userData) {
			int eventKind = OS.GetEventKind (theEvent);
			if (eventKind != OS.kEventWindowToolbarSwitchMode)
				return OS.eventNotHandledErr;
			
			int [] theWindow = new int [1];
			OS.GetEventParameter (theEvent, OS.kEventParamDirectObject, OS.typeWindowRef, null, 4, null, theWindow);
			
			int [] theRoot = new int [1];
			OS.GetRootControl (theWindow [0], theRoot);
			Widget widget = Display.getCurrent().findWidget(theRoot [0]);
			
			if (!(widget instanceof Shell)) {
				return OS.eventNotHandledErr;
			}
			Shell shell = (Shell) widget;
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
					.getWorkbenchWindows();
			for (int i = 0; i < windows.length; i++) {
				if (windows[i].getShell() == shell) {
					return runCommand("org.eclipse.ui.ToggleCoolbarAction"); //$NON-NLS-1$
				}
			}
			return OS.eventNotHandledErr;
		}

		/**
    	 * Process OS menu event.
    	 * 
    	 * @param nextHandler unused
    	 * @param theEvent the OS event
    	 * @param userData unused
    	 * @return whether or not the event was handled by this processor
    	 */
        public int commandProc(int nextHandler, int theEvent, int userData) {
            if (OS.GetEventKind(theEvent) == OS.kEventProcessCommand) {
                HICommand command = new HICommand();
                OS.GetEventParameter(theEvent, OS.kEventParamDirectObject,
                        OS.typeHICommand, null, HICommand.sizeof, null, command);
                switch (command.commandID) {
                case kHICommandPreferences:
                    return runAction("preferences"); //$NON-NLS-1$
                case kHICommandAbout:
                    return runAction("about"); //$NON-NLS-1$
                default:
                    break;
                }
            }
            return OS.eventNotHandledErr;
        }
	}

	private static final int kHICommandPreferences = ('p' << 24) + ('r' << 16) + ('e' << 8) + 'f';
    private static final int kHICommandAbout = ('a' << 24) + ('b' << 16) + ('o' << 8) + 'u';
    private static final int kHICommandServices = ('s' << 24) + ('e' << 16) + ('r' << 8) + 'v';
    private static final int kHICommandHide = ('h' << 24) + ('i' << 16) + ('d' << 8) + 'e';
    private static final int kHICommandQuit = ('q' << 24) + ('u' << 16) + ('i' << 8) + 't';

    private static final String RESOURCE_BUNDLE = CarbonUIEnhancer.class.getPackage().getName() + ".Messages"; //$NON-NLS-1$
	
    private String fAboutActionName;
    private String fQuitActionName;
    private String fHideActionName;
	private int applicationMenuHandle;

    /**
     * Default constructor
     */
    public CarbonUIEnhancer() {
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
				hookApplicationMenu(display);
				hookToolbarButtonCallback();
				hookWorkbenchListener();
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
			// modify the newly opened window with the correct OS X
			// style bits such that it possesses the toolbar button
			Shell shell = window.getShell();
			int windowHandle = OS.GetControlOwner(shell.handle);
			OS.ChangeWindowAttributes(windowHandle,
					OS.kWindowToolbarButtonAttribute, 0);
		}
	}
	
	/**
	 * Hook the window toolbar button to toggle the visibility of the coolbar
	 * and perspective bar.
	 * 
	 * @since 3.2
	 */
    protected void hookToolbarButtonCallback() {
		Object target = new Target();
		
	    final Callback commandCallback = new Callback(target, "toolbarProc", 3); //$NON-NLS-1$
        int commandProc = commandCallback.getAddress();
        if (commandProc == 0) {
            commandCallback.dispose();
            return; // give up
        }
        
        int[] mask = new int[] { OS.kEventClassWindow, OS.kEventWindowToolbarSwitchMode };
        OS.InstallEventHandler(OS.GetApplicationEventTarget(), commandProc,
                mask.length / 2, mask, 0, null);
		
	}

	/**
     * See Apple Technical Q&A 1079 (http://developer.apple.com/qa/qa2001/qa1079.html)
     */
    private void hookApplicationMenu(Display display) {

        // Callback target
        Object target = new Target();

        final Callback commandCallback = new Callback(target, "commandProc", 3); //$NON-NLS-1$
        int commandProc = commandCallback.getAddress();
        if (commandProc == 0) {
            commandCallback.dispose();
            return; // give up
        }

        // Install event handler for commands
        int[] mask = new int[] { OS.kEventClassCommand, OS.kEventProcessCommand };
        OS.InstallEventHandler(OS.GetApplicationEventTarget(), commandProc,
                mask.length / 2, mask, 0, null);

        // create About Eclipse menu command
        int[] outMenu = new int[1];
        short[] outIndex = new short[1];
        if (OS.GetIndMenuItemWithCommandID(0, kHICommandPreferences, 1, outMenu, outIndex) == OS.noErr
                && outMenu[0] != 0) {
            applicationMenuHandle = outMenu[0];

            // add the about action
            int l = fAboutActionName.length();
            char buffer[] = new char[l];
            fAboutActionName.getChars(0, l, buffer, 0);
            int str = OS.CFStringCreateWithCharacters(OS.kCFAllocatorDefault, buffer, l);
            OS.InsertMenuItemTextWithCFString(applicationMenuHandle, str, (short) 0, 0, kHICommandAbout);
            OS.CFRelease(str);

            // rename the hide action if we have an override string
            if (fHideActionName != null) {
				renameApplicationMenuItem(kHICommandHide, fHideActionName);
			}
            
            // rename the quit action if we have an override string
            if (fQuitActionName != null) {
				renameApplicationMenuItem(kHICommandQuit, fQuitActionName);
			}
            
            // add separator between About & Preferences
            OS.InsertMenuItemTextWithCFString(applicationMenuHandle, 0, (short) 1, OS.kMenuItemAttrSeparator, 0);

            // enable pref menu
            OS.EnableMenuCommand(applicationMenuHandle, kHICommandPreferences);

            // disable services menu
            OS.DisableMenuCommand(applicationMenuHandle, kHICommandServices);
        }

        // schedule disposal of callback object
        display.disposeExec(new Runnable() {
            public void run() {
                commandCallback.dispose();
            }
        });
    }

	/**
     * Rename the given application menu item.
     *
	 * @param itemConstant the kHI* constant for the menu item
	 * @param replacementName the new name
     * @since 3.4
	 */
	private void renameApplicationMenuItem(int itemConstant,
			String replacementName) {
		int l;
		char[] buffer;
		int str;
		int[] itemMenu = new int[1];
		short[] itemIndex = new short[1];

		if (OS.GetIndMenuItemWithCommandID(0, itemConstant, 1,
				itemMenu, itemIndex) == OS.noErr
				&& itemMenu[0] != 0) {

			l = replacementName.length();
			buffer = new char[l];
			replacementName.getChars(0, l, buffer, 0);
			str = OS.CFStringCreateWithCharacters(
					OS.kCFAllocatorDefault, buffer, l);

			OS.SetMenuItemTextWithCFString(itemMenu[0],
					itemIndex[0], str);

			OS.CFRelease(str);
		}
	}

    /**
     * Locate an action with the given id in the current menubar and run it.
     */
    private int runAction(String actionId) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
        	IMenuManager manager = ((WorkbenchWindow)window).getActionBars().getMenuManager();
        	IAction action = findAction(actionId, manager);
        	if (action != null && action.isEnabled()) {
        		try {
        			 // disable About and Pref actions;
                    OS.DisableMenuCommand(applicationMenuHandle, kHICommandPreferences);
                    OS.DisableMenuCommand(applicationMenuHandle, kHICommandAbout);
                    action.run();
        		}
        		finally {
        			 // re-enable About and Pref actions;
        			 OS.EnableMenuCommand(applicationMenuHandle, kHICommandPreferences);
                     OS.EnableMenuCommand(applicationMenuHandle, kHICommandAbout);
        		}
        		return OS.noErr;
        	}
        }
        return OS.eventNotHandledErr;
       
    }
    
    private int runCommand(String commandId) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return OS.eventNotHandledErr;
		
		IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null)
			return OS.eventNotHandledErr;
		
		IHandlerService commandService = (IHandlerService) activeWorkbenchWindow
				.getService(IHandlerService.class);

		if (commandService != null) {
			try {
				commandService.executeCommand(commandId, null);
				return OS.noErr;
			} catch (ExecutionException e) {
			} catch (NotDefinedException e) {
			} catch (NotEnabledException e) {
			} catch (NotHandledException e) {
			}
		}
		return OS.eventNotHandledErr;
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
}
