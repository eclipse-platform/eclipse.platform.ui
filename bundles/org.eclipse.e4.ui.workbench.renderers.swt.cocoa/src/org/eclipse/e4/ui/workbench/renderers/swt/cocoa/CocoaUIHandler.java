/*******************************************************************************
 * Copyright (c) 2008, 2010 Adobe Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adobe Systems, Inc. - initial API and implementation
 *     IBM Corporation - cleanup
 *     Brian de Alwis - adapted to e4
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Provider;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.renderers.swt.HandledMenuItemRenderer;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.C;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.gtk.OS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.EventHandler;

/**
 * The {@link CocoaUIHandler} is a port of the Eclipse 3.x
 * org.eclipse.ui.cocoa's CocoaUIEnhancer for native e4 apps. This class
 * redirects the standard MacOS X "About", "Preferences...", and "Quit" menu
 * items to link them to the corresponding workbench commands, as well as
 * hooking in Close-Dialog behaviour.
 * 
 * This functionality uses Cocoa-specific natives as SWT doesn't provide an
 * abstraction for the application menu.
 * 
 * @noreference this class is not intended to be referenced by any client.
 * @since 1.0
 */
public class CocoaUIHandler {
	/**
	 * 
	 */
	private static final String DEFAULT_ACCELERATOR_CONFIGURATION = "org.eclipse.ui.defaultAcceleratorConfiguration"; //$NON-NLS-1$
	protected static final String FRAGMENT_ID = "org.eclipse.e4.ui.workbench.renderers.swt.cocoa"; //$NON-NLS-1$
	protected static final String HOST_ID = "org.eclipse.e4.ui.workbench.renderers.swt"; //$NON-NLS-1$
	public static final String CLASS_URI = "platform:/plugin/" //$NON-NLS-1$  
			+ HOST_ID + "/" + CocoaUIHandler.class.getName(); //$NON-NLS-1$

	// these constants are defined in IWorkbenchCommandConstants
	// but reproduced here to support pure-e4 apps
	private static final String COMMAND_ID_ABOUT = "org.eclipse.ui.help.aboutAction"; //$NON-NLS-1$
	private static final String COMMAND_ID_PREFERENCES = "org.eclipse.ui.window.preferences"; //$NON-NLS-1$
	private static final String COMMAND_ID_QUIT = "org.eclipse.ui.file.exit"; //$NON-NLS-1$
	// toggle coolbar isn't actually defined anywhere
	private static final String COMMAND_ID_TOGGLE_COOLBAR = "org.eclipse.ui.ToggleCoolbarAction"; //$NON-NLS-1$

	private static final String COMMAND_ID_CLOSE_DIALOG = "org.eclipse.ui.cocoa.closeDialog"; //$NON-NLS-1$
	private static final String CLOSE_DIALOG_KEYSEQUENCE = "M1+W"; //$NON-NLS-1$

	private static final int kAboutMenuItem = 0;
	private static final int kPreferencesMenuItem = 2;
	private static final int kHideApplicationMenuItem = 6;
	private static final int kQuitMenuItem = 10;

	static long sel_toolbarButtonClicked_;
	static long sel_preferencesMenuItemSelected_;
	static long sel_aboutMenuItemSelected_;
	static long sel_quitMenuItemSelected_;

	private static final long NSWindowToolbarButton = 3;

	/* This callback is not freed */
	static Callback proc3Args;
	static final byte[] SWT_OBJECT = { 'S', 'W', 'T', '_', 'O', 'B', 'J', 'E',
			'C', 'T', '\0' };

	SWTCocoaEnhancerDelegate delegate;
	private long delegateJniRef;

	private static final String RESOURCE_BUNDLE = CocoaUIHandler.class
			.getPackage().getName() + ".Messages"; //$NON-NLS-1$

	private String fAboutActionName;
	private String fQuitActionName;
	private String fHideActionName;

	protected MCommand closeDialogCommand;

	@Inject
	protected MApplication app;
	@Inject
	protected Provider<StatusReporter> statusReporter;
	@Inject
	protected ECommandService commandService;
	@Inject
	protected EHandlerService handlerService;
	@Inject
	protected EModelService modelService;
	@Inject
	protected EBindingService bindingService;
	@Inject
	protected IEventBroker eventBroker;

	private EventHandler shellListener;
	private EventHandler menuListener;
	private EventHandler menuContributionListener;
	private EventHandler commandListener;

	public void constructActionNames() {
		String productName = Display.getAppName();

		ResourceBundle resourceBundle = ResourceBundle
				.getBundle(RESOURCE_BUNDLE);
		try {
			if (productName != null) {
				String format = resourceBundle.getString("AboutAction.format"); //$NON-NLS-1$
				if (format != null)
					fAboutActionName = MessageFormat.format(format,
							new Object[] { productName });
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
				sel_quitMenuItemSelected_ = registerName("quitMenuItemSelected:"); //$NON-NLS-1$
				setupDelegateClass();
			}
		} catch (Exception e) {
			// theoretically, one of
			// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
		}
	}

	private void setupDelegateClass() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			NoSuchFieldException {
		// TODO: These should either move out of Display or be accessible to
		// this class.
		byte[] types = { '*', '\0' };
		int size = C.PTR_SIZEOF, align = C.PTR_SIZEOF == 4 ? 2 : 3;

		Class clazz = CocoaUIHandler.class;

		proc3Args = new Callback(clazz, "actionProc", 3); //$NON-NLS-1$
		// call getAddress
		Method getAddress = Callback.class
				.getMethod("getAddress", new Class[0]); //$NON-NLS-1$
		Object object = getAddress.invoke(proc3Args, null);
		long proc3 = convertToLong(object);
		if (proc3 == 0)
			SWT.error(SWT.ERROR_NO_MORE_CALLBACKS);

		// call objc_allocateClassPair
		Field field = OS.class.getField("class_NSObject"); //$NON-NLS-1$
		Object fieldObj = field.get(OS.class);
		object = invokeMethod(
				OS.class,
				"objc_allocateClassPair", new Object[] { fieldObj, "SWTCocoaEnhancerDelegate", wrapPointer(0) }); //$NON-NLS-1$ //$NON-NLS-2$
		long cls = convertToLong(object);

		invokeMethod(OS.class, "class_addIvar", new Object[] { //$NON-NLS-1$
				wrapPointer(cls), SWT_OBJECT, wrapPointer(size),
						new Byte((byte) align), types });

		// Add the action callback
		invokeMethod(
				OS.class,
				"class_addMethod", new Object[] { wrapPointer(cls), wrapPointer(sel_toolbarButtonClicked_), wrapPointer(proc3), "@:@" }); //$NON-NLS-1$ //$NON-NLS-2$
		invokeMethod(OS.class, "class_addMethod", new Object[] { //$NON-NLS-1$
				wrapPointer(cls),
						wrapPointer(sel_preferencesMenuItemSelected_),
						wrapPointer(proc3), "@:@" }); //$NON-NLS-1$
		invokeMethod(
				OS.class,
				"class_addMethod", new Object[] { wrapPointer(cls), wrapPointer(sel_aboutMenuItemSelected_), wrapPointer(proc3), "@:@" }); //$NON-NLS-1$ //$NON-NLS-2$
		invokeMethod(
				OS.class,
				"class_addMethod", new Object[] { wrapPointer(cls), wrapPointer(sel_quitMenuItemSelected_), wrapPointer(proc3), "@:@" }); //$NON-NLS-1$ //$NON-NLS-2$

		invokeMethod(OS.class, "objc_registerClassPair", //$NON-NLS-1$
				new Object[] { wrapPointer(cls) });
	}

	private long registerName(String name) throws IllegalArgumentException,
			SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Class clazz = OS.class;
		Object object = invokeMethod(clazz,
				"sel_registerName", new Object[] { name }); //$NON-NLS-1$
		return convertToLong(object);
	}

	@PostConstruct
	public void init() {
		constructActionNames();

		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				try {
					delegate = new SWTCocoaEnhancerDelegate();
					delegate.alloc().init();
					// call OS.NewGlobalRef
					Method method = OS.class.getMethod(
							"NewGlobalRef", new Class[] { Object.class }); //$NON-NLS-1$
					Object object = method.invoke(OS.class,
							new Object[] { CocoaUIHandler.this });
					delegateJniRef = convertToLong(object);
				} catch (Exception e) {
					// theoretically, one of
					// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
					// not expected to happen at all.
					log(e);
				}
				if (delegateJniRef == 0)
					SWT.error(SWT.ERROR_NO_HANDLES);
				try {
					Field idField = SWTCocoaEnhancerDelegate.class
							.getField("id"); //$NON-NLS-1$
					Object idValue = idField.get(delegate);
					invokeMethod(OS.class, "object_setInstanceVariable", //$NON-NLS-1$
							new Object[] { idValue, SWT_OBJECT,
									wrapPointer(delegateJniRef) });

					hookApplicationMenu();
					hookWorkbenchListeners();
					processModelMenus();

					// Now add the special Cmd-W dialog helper
					addCloseDialogCommand();
					addCloseDialogHandler();
					addCloseDialogBinding();

					// modify all shells opened on startup
					for (MWindow window : app.getChildren()) {
						modifyWindowShell(window);
					}

					// schedule disposal of callback object
					display.disposeExec(new Runnable() {
						public void run() {
							if (delegateJniRef != 0) {
								try {
									invokeMethod(
											OS.class,
											"DeleteGlobalRef", new Object[] { wrapPointer(delegateJniRef) }); //$NON-NLS-1$
								} catch (Exception e) {
									// theoretically, one of
									// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
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
				} catch (Exception e) {
					// theoretically, one of
					// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
					// not expected to happen at all.
					log(e);
				}
			}

		});
	}

	@PreDestroy
	public void dispose() {
		if (shellListener != null) {
			eventBroker.unsubscribe(shellListener);
		}
		if (menuListener != null) {
			eventBroker.unsubscribe(menuListener);
		}
		if (menuContributionListener != null) {
			eventBroker.unsubscribe(menuContributionListener);
		}
		if (commandListener != null) {
			eventBroker.unsubscribe(commandListener);
		}
	}

	/**
	 * Process defined windows and menu contributions
	 */
	protected void processModelMenus() {
		for (MWindow window : app.getChildren()) {
			redirectHandledMenuItems(window.getMainMenu());
		}
		for (MMenuContribution contribution : app.getMenuContributions()) {
			processMenuContribution(contribution);
		}
	}

	/**
	 * @param contribution
	 */
	private void processMenuContribution(MMenuContribution contribution) {
		for (MMenuElement elmt : contribution.getChildren()) {
			if (elmt instanceof MMenu) {
				redirectHandledMenuItems((MMenu) elmt);
			} else if (elmt instanceof MMenuItem) {
				redirectHandledMenuItem((MMenuItem) elmt);
			}
		}
	}

	private void addCloseDialogCommand() {
		closeDialogCommand = findCommand(COMMAND_ID_CLOSE_DIALOG);
		if (closeDialogCommand != null) {
			return;
		}
		closeDialogCommand = CommandsFactoryImpl.eINSTANCE.createCommand();
		closeDialogCommand.setElementId(COMMAND_ID_CLOSE_DIALOG);
		closeDialogCommand.setCommandName(COMMAND_ID_CLOSE_DIALOG);
		app.getCommands().add(closeDialogCommand);
	}

	private void addCloseDialogHandler() {
		MHandler handler = findHandler(closeDialogCommand);
		if (handler != null) {
			return;
		}
		handler = CommandsFactoryImpl.eINSTANCE.createHandler();
		handler.setCommand(closeDialogCommand);
		handler.setContributionURI("platform:/plugin/" + HOST_ID + "/" //$NON-NLS-1$ //$NON-NLS-2$
				+ CloseDialogHandler.class.getName());
		app.getHandlers().add(handler);
	}

	private void addCloseDialogBinding() {
		TriggerSequence sequence = bindingService
				.createSequence(CLOSE_DIALOG_KEYSEQUENCE);
		ParameterizedCommand cmd = commandService.createCommand(
				COMMAND_ID_CLOSE_DIALOG, null);
		Binding binding = bindingService.createBinding(sequence, cmd,
				DEFAULT_ACCELERATOR_CONFIGURATION,
				EBindingService.DIALOG_CONTEXT_ID, null, null, Binding.SYSTEM);
		bindingService.deactivateBinding(binding);
		bindingService.activateBinding(binding);
	}

	void log(Exception e) {
		// StatusUtil.handleStatus(e, StatusManager.LOG);
		statusReporter
				.get()
				.report(new Status(IStatus.WARNING, FRAGMENT_ID,
						"Exception occurred during CocoaUI processing", e), StatusReporter.LOG); //$NON-NLS-1$
	}

	/**
	 * Hooks a listener that tweaks newly opened workbench window shells with
	 * the proper OS flags.
	 */
	protected void hookWorkbenchListeners() {
		// watch for a window's "widget" attribute being flipped to a shell
		shellListener = new EventHandler() {
			public void handleEvent(org.osgi.service.event.Event event) {
				if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow
						&& event.getProperty(UIEvents.EventTags.NEW_VALUE) != null) {
					MWindow window = (MWindow) event
							.getProperty(UIEvents.EventTags.ELEMENT);
					modifyWindowShell(window);
				}
			}
		};
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
				UIEvents.UIElement.WIDGET), shellListener);

		// this listener is handling the Eclipse 4.0 compatibility case,
		// where the window is created without a main menu or trim first,
		// and then later when the main menu is being set it is time
		// for us to do our work. It also handles dynamically created
		// windows too.
		menuListener = new EventHandler() {
			public void handleEvent(org.osgi.service.event.Event event) {
				Object newValue = event
						.getProperty(UIEvents.EventTags.NEW_VALUE);
				Object oldValue = event
						.getProperty(UIEvents.EventTags.OLD_VALUE);
				Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (element instanceof MWindow && oldValue == null
						&& newValue instanceof MMenu) {
					modifyWindowShell((MWindow) element);
				}
			}
		};
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.Window.TOPIC,
				UIEvents.Window.MAINMENU), menuListener);

		// watch for new menu contributions
		menuContributionListener = new EventHandler() {
			public void handleEvent(org.osgi.service.event.Event event) {
				if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuContribution
						&& event.getProperty(UIEvents.EventTags.NEW_VALUE) != null) {
					MMenuContribution contribution = (MMenuContribution) event
							.getProperty(UIEvents.EventTags.ELEMENT);
					processMenuContribution(contribution);
				}
			}
		};
		eventBroker.subscribe(UIEvents.buildTopic(
				UIEvents.MenuContributions.TOPIC,
				UIEvents.MenuContributions.MENUCONTRIBUTIONS),
				menuContributionListener);

		// watch for command changes
		commandListener = new EventHandler() {
			public void handleEvent(org.osgi.service.event.Event event) {
				if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MCommand) {
					MCommand cmd = (MCommand) event
							.getProperty(UIEvents.EventTags.ELEMENT);
					String id = cmd.getElementId();
					if (COMMAND_ID_ABOUT.equals(id)
							|| COMMAND_ID_PREFERENCES.equals(id)
							|| COMMAND_ID_QUIT.equals(id)) {
						hookApplicationMenu();
					}
				}
			}
		};
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.Application.TOPIC,
				UIEvents.Application.COMMANDS), commandListener);

	}

	/**
	 * Modify the given workbench window shell bits to show the tool bar toggle
	 * button.
	 * 
	 * @param window
	 *            the window to modify
	 * @since 3.2
	 */
	protected void modifyWindowShell(MWindow window) {
		if (window.getWidget() == null) {
			return;
		}
		if (window.getMainMenu() == null) {
			return;
		}
		redirectHandledMenuItems(window.getMainMenu());
		// only add the button when either the cool bar or perspective bar
		// is initially visible. This is so that RCP applications can choose to
		// use this fragment without fear that their explicitly invisible bars
		// can't be shown.
		boolean trimInitiallyVisible = false;
		if (window instanceof MTrimmedWindow
				&& !((MTrimmedWindow) window).getTrimBars().isEmpty()) {
			for (MTrimBar tb : ((MTrimmedWindow) window).getTrimBars()) {
				if (tb.isVisible()) {
					trimInitiallyVisible = true;
				}
			}
		}

		if (trimInitiallyVisible) {
			// Add an empty, hidden tool bar to the window. Without this the
			// tool bar button at the top right of the window will not appear
			// even when setShowsToolbarButton(true) is called.
			NSToolbar dummyBar = new NSToolbar();
			dummyBar.alloc();
			dummyBar.initWithIdentifier(NSString.stringWith("SWTToolbar")); //$NON-NLS-1$
			dummyBar.setVisible(false);

			Shell shell = ((Control) window.getWidget()).getShell();
			NSWindow nsWindow = shell.view.window();
			nsWindow.setToolbar(dummyBar);
			dummyBar.release();
			nsWindow.setShowsToolbarButton(true);

			// Override the target and action of the toolbar button so we can
			// control it.
			try {
				Object fieldValue = wrapPointer(NSWindowToolbarButton);
				NSButton toolbarButton = (NSButton) invokeMethod(
						NSWindow.class, nsWindow,
						"standardWindowButton", new Object[] { fieldValue }); //$NON-NLS-1$
				if (toolbarButton != null) {
					toolbarButton.setTarget(delegate);
					invokeMethod(
							NSControl.class,
							toolbarButton,
							"setAction", //$NON-NLS-1$
							new Object[] { wrapPointer(sel_toolbarButtonClicked_) });
				}
			} catch (Exception e) {
				// theoretically, one of
				// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
				// not expected to happen at all.
				log(e);
			}
		}
	}

	private void redirectHandledMenuItems(MMenu menu) {
		if (menu == null) {
			return;
		}
		for (MMenuElement elmt : menu.getChildren()) {
			if (elmt instanceof MMenu) {
				redirectHandledMenuItems((MMenu) elmt);
			} else if (elmt instanceof MMenuItem) {
				redirectHandledMenuItem((MMenuItem) elmt);
			}
		}
	}

	private void redirectHandledMenuItem(MMenuItem item) {
		String elmtId = item.getElementId();
		if (elmtId != null
				&& (elmtId.equals(COMMAND_ID_ABOUT)
						|| elmtId.equals(COMMAND_ID_PREFERENCES) || elmtId
							.equals(COMMAND_ID_QUIT))) {
			item.setVisible(false);
		} else if (item instanceof MHandledMenuItem) {
			MHandledMenuItem mhmi = (MHandledMenuItem) item;
			elmtId = mhmi.getCommand() == null ? null : mhmi.getCommand()
					.getElementId();
			if (elmtId != null
					&& (elmtId.equals(COMMAND_ID_ABOUT)
							|| elmtId.equals(COMMAND_ID_PREFERENCES) || elmtId
								.equals(COMMAND_ID_QUIT))) {
				item.setVisible(false);
			}
		}
	}

	private void hookApplicationMenu() {
		try {
			// create About Eclipse menu command
			NSMenu mainMenu = NSApplication.sharedApplication().mainMenu();
			NSMenuItem mainMenuItem = (NSMenuItem) invokeMethod(NSMenu.class,
					mainMenu, "itemAtIndex", new Object[] { wrapPointer(0) }); //$NON-NLS-1$
			NSMenu appMenu = mainMenuItem.submenu();

			// add the about action
			NSMenuItem aboutMenuItem = (NSMenuItem) invokeMethod(NSMenu.class,
					appMenu,
					"itemAtIndex", new Object[] { wrapPointer(kAboutMenuItem) }); //$NON-NLS-1$
			aboutMenuItem.setTitle(NSString.stringWith(fAboutActionName));

			// rename the hide action if we have an override string
			if (fHideActionName != null) {
				NSMenuItem hideMenuItem = (NSMenuItem) invokeMethod(
						NSMenu.class,
						appMenu,
						"itemAtIndex", new Object[] { wrapPointer(kHideApplicationMenuItem) }); //$NON-NLS-1$
				hideMenuItem.setTitle(NSString.stringWith(fHideActionName));
			}

			// rename the quit action if we have an override string
			NSMenuItem quitMenuItem = (NSMenuItem) invokeMethod(NSMenu.class,
					appMenu,
					"itemAtIndex", new Object[] { wrapPointer(kQuitMenuItem) }); //$NON-NLS-1$
			if (fQuitActionName != null) {
				quitMenuItem.setTitle(NSString.stringWith(fQuitActionName));
			}

			// enable pref menu
			NSMenuItem prefMenuItem = (NSMenuItem) invokeMethod(
					NSMenu.class,
					appMenu,
					"itemAtIndex", new Object[] { wrapPointer(kPreferencesMenuItem) }); //$NON-NLS-1$
			prefMenuItem.setEnabled(true);

			// Register as a target on the about, prefs and quit items.
			setMenuDelegate(COMMAND_ID_ABOUT, aboutMenuItem,
					sel_aboutMenuItemSelected_);
			setMenuDelegate(COMMAND_ID_PREFERENCES, prefMenuItem,
					sel_preferencesMenuItemSelected_);
			setMenuDelegate(COMMAND_ID_QUIT, quitMenuItem,
					sel_quitMenuItemSelected_);
		} catch (Exception e) {
			// theoretically, one of
			// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
		}
	}

	/**
	 * @param commandId
	 *            the command id to be invoked
	 * @param menuItem
	 *            the menu item to be modified
	 * @param selector
	 *            the selector
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	private void setMenuDelegate(String commandId, NSMenuItem menuItem,
			long selector) throws IllegalArgumentException, SecurityException,
			IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		if (!isDefined(commandId)) {
			menuItem.setTarget(new id());
		} else {
			menuItem.setTarget(delegate);
			invokeMethod(NSMenuItem.class, menuItem,
					"setAction", new Object[] { wrapPointer(selector) }); //$NON-NLS-1$
		}
	}

	/**
	 * Check if there is a corresponding MCommand
	 * 
	 * @param commandId
	 *            the command id
	 * @return true if found or false otherwise
	 */
	private boolean isDefined(String commandId) {
		// cannot use "commandService.getCommand(commandId).isDefined()" as
		// it may not yet have processed the MCommand definitions
		return findCommand(commandId) != null;
	}

	/**
	 * Find the command definition
	 * 
	 * @param commandId
	 * @return the command definition or null if not found
	 */
	private MCommand findCommand(String commandId) {
		for (MCommand cmd : app.getCommands()) {
			if (commandId.equals(cmd.getElementId())) {
				return cmd;
			}
		}
		return null;
	}

	/**
	 * Find a handler defined for the provided command. This command returns on
	 * the first command found.
	 * 
	 * @param cmd
	 * @return the first handler found, or <code>null</code> if none
	 */
	private MHandler findHandler(MCommand cmd) {
		if (cmd == null) {
			return null;
		}
		for (MHandler handler : app.getHandlers()) {
			if (handler.getCommand() == cmd) {
				return handler;
			}
		}
		return null;
	}

	/**
	 * Locate an action (a menu item, actually) with the given id in the current
	 * menu bar and run it.
	 */
	private void runAction(String actionId) {
		MWindow window = app.getSelectedElement();
		if (window != null) {
			MMenu topMenu = window.getMainMenu();
			MMenuItem item = findAction(actionId, topMenu);
			if (item != null && item.isEnabled()) {
				try {
					// disable the about and prefs items -- they shouldn't be
					// able to be run when another item is being triggered
					NSMenu mainMenu = NSApplication.sharedApplication()
							.mainMenu();
					NSMenuItem mainMenuItem = (NSMenuItem) invokeMethod(
							NSMenu.class, mainMenu,
							"itemAtIndex", new Object[] { wrapPointer(0) }); //$NON-NLS-1$
					NSMenu appMenu = mainMenuItem.submenu();
					NSMenuItem aboutMenuItem = (NSMenuItem) invokeMethod(
							NSMenu.class,
							appMenu,
							"itemAtIndex", new Object[] { wrapPointer(kAboutMenuItem) }); //$NON-NLS-1$
					NSMenuItem prefMenuItem = (NSMenuItem) invokeMethod(
							NSMenu.class,
							appMenu,
							"itemAtIndex", new Object[] { wrapPointer(kPreferencesMenuItem) }); //$NON-NLS-1$
					try {
						prefMenuItem.setEnabled(false);
						aboutMenuItem.setEnabled(false);
						simulateMenuSelection(item);
					} finally {
						prefMenuItem.setEnabled(true);
						aboutMenuItem.setEnabled(true);
					}
				} catch (Exception e) {
					// theoretically, one of
					// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
					// not expected to happen at all.
					log(e);
				}
			}
		}

	}

	private void simulateMenuSelection(MMenuItem item) {
		// FIXME: pity this code isn't available through the MMenuItem instance
		// somehow
		IEclipseContext lclContext = getContext(item);
		if (item instanceof MDirectMenuItem) {
			MDirectMenuItem dmi = (MDirectMenuItem) item;
			if (dmi.getObject() == null) {
				IContributionFactory cf = (IContributionFactory) lclContext
						.get(IContributionFactory.class.getName());
				dmi.setObject(cf.create(dmi.getContributionURI(), lclContext));
			}
			lclContext.set(MItem.class.getName(), item);
			ContextInjectionFactory.invoke(dmi.getObject(), Execute.class,
					lclContext);
			lclContext.remove(MItem.class.getName());
		} else if (item instanceof MHandledMenuItem) {
			MHandledMenuItem hmi = (MHandledMenuItem) item;
			EHandlerService service = (EHandlerService) lclContext
					.get(EHandlerService.class.getName());
			ParameterizedCommand cmd = hmi.getWbCommand();
			if (cmd == null) {
				cmd = HandledMenuItemRenderer.generateParameterizedCommand(hmi,
						lclContext);
			}
			lclContext.set(MItem.class.getName(), item);
			service.executeHandler(cmd);
			lclContext.remove(MItem.class.getName());
		} else {
			statusReporter
					.get()
					.report(new Status(
							IStatus.WARNING,
							FRAGMENT_ID,
							"Unhandled menu type: " + item.getClass() + ": " + item), //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
					StatusReporter.LOG);
		}
	}

	private IEclipseContext getContext(MUIElement element) {
		if (element instanceof MContext) {
			return ((MContext) element).getContext();
		}
		return modelService.getContainingContext(element);
	}

	/**
	 * Delegate to the handler for the provided command id.
	 * 
	 * @param commandId
	 * @return true if the command was found, false otherwise
	 */
	private boolean runCommand(String commandId) {
		if (handlerService != null) {
			ParameterizedCommand cmd = commandService.createCommand(commandId,
					Collections.emptyMap());
			if (cmd != null) {
				handlerService.executeHandler(cmd);
				return true;
			}
		}
		return false;
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
	private MMenuItem findAction(String actionId, MMenu menu) {
		if (menu == null) {
			return null;
		}
		for (MMenuElement item : menu.getChildren()) {
			if (item instanceof MMenuItem) {
				MMenuItem mmi = (MMenuItem) item;
				if (mmi.getElementId() != null
						&& mmi.getElementId().equals(actionId))
					return mmi;
				if (mmi instanceof MHandledMenuItem) {
					MHandledMenuItem mhmi = (MHandledMenuItem) mmi;
					if (mhmi.getCommand() != null
							&& actionId
									.equals(mhmi.getCommand().getElementId())) {
						return mmi;
					}
				}
			} else if (item instanceof MMenu) {
				MMenuItem found = findAction(actionId, (MMenu) item);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	/*
	 * Action implementations for the toolbar button and preferences and about
	 * menu items
	 */
	void toolbarButtonClicked(NSControl source) {
		try {
			NSWindow window = source.window();
			Field idField = NSWindow.class.getField("id"); //$NON-NLS-1$
			Object idValue = idField.get(window);

			Display display = Display.getCurrent();
			Widget widget = (Widget) invokeMethod(Display.class, display,
					"findWidget", new Object[] { idValue }); //$NON-NLS-1$

			if (!(widget instanceof Shell)) {
				return;
			}
			Shell shell = (Shell) widget;
			for (MWindow mwin : app.getChildren()) {
				if (mwin.getWidget() == shell) {
					if (!runCommand(COMMAND_ID_TOGGLE_COOLBAR)) {
						// there may be a menu item to do the toggle...
						runAction(COMMAND_ID_TOGGLE_COOLBAR);
					}
				}
			}
		} catch (Exception e) {
			// theoretically, one of
			// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
		}
	}

	void preferencesMenuItemSelected() {
		if (!runCommand(COMMAND_ID_PREFERENCES)) {
			runAction(COMMAND_ID_PREFERENCES);
		}
	}

	void aboutMenuItemSelected() {
		if (!runCommand(COMMAND_ID_ABOUT)) {
			runAction(COMMAND_ID_ABOUT);
		}
	}

	void quitMenuItemSelected() {
		if (!runCommand(COMMAND_ID_QUIT)) {
			runAction(COMMAND_ID_QUIT);
		}
	}

	static int actionProc(int id, int sel, int arg0) throws Exception {
		return (int) actionProc((long) id, (long) sel, (long) arg0);
	}

	static long actionProc(long id, long sel, long arg0) throws Exception {
		long[] jniRef = OS_object_getInstanceVariable(id, SWT_OBJECT);
		if (jniRef[0] == 0)
			return 0;

		CocoaUIHandler delegate = (CocoaUIHandler) invokeMethod(OS.class,
				"JNIGetObject", new Object[] { wrapPointer(jniRef[0]) }); //$NON-NLS-1$

		if (sel == sel_toolbarButtonClicked_) {
			NSControl source = new_NSControl(arg0);
			delegate.toolbarButtonClicked(source);
		} else if (sel == sel_preferencesMenuItemSelected_) {
			delegate.preferencesMenuItemSelected();
		} else if (sel == sel_aboutMenuItemSelected_) {
			delegate.aboutMenuItemSelected();
		} else if (sel == sel_quitMenuItemSelected_) {
			delegate.quitMenuItemSelected();
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
		Class PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		Constructor constructor = clazz
				.getConstructor(new Class[] { PTR_CLASS });
		return (NSControl) constructor
				.newInstance(new Object[] { wrapPointer(arg0) });
	}

	/**
	 * Specialized method. It's behavior is isolated and different enough from
	 * the usual invocation that custom code is warranted.
	 */
	private static long[] OS_object_getInstanceVariable(long delegateId,
			byte[] name) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		Class clazz = OS.class;
		Method method = null;
		Class PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class) {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] { //$NON-NLS-1$
					long.class, byte[].class, long[].class });
			long[] resultPtr = new long[1];
			method.invoke(null, new Object[] { new Long(delegateId), name,
					resultPtr });
			return resultPtr;
		} else {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] { //$NON-NLS-1$
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
		Class PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class)
			return new Long(value);
		else
			return new Integer((int) value);
	}
}
