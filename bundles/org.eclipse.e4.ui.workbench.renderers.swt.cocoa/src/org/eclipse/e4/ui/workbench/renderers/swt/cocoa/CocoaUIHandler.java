/*******************************************************************************
 * Copyright (c) 2008, 2015 Adobe Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Adobe Systems, Inc. - initial API and implementation
 *     IBM Corporation - cleanup
 *     Brian de Alwis - adapted to e4
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 462407
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
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
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MParameter;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledItem;
import org.eclipse.e4.ui.model.application.ui.menu.MHandledMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.internal.C;
import org.eclipse.swt.internal.Callback;
import org.eclipse.swt.internal.cocoa.NSButton;
import org.eclipse.swt.internal.cocoa.NSControl;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.NSToolbar;
import org.eclipse.swt.internal.cocoa.NSWindow;
import org.eclipse.swt.internal.cocoa.OS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.event.Event;

/**
 * The {@link CocoaUIHandler} is a port of the Eclipse 3.x
 * org.eclipse.ui.cocoa's CocoaUIEnhancer for native e4 apps. This class
 * redirects the standard MacOS X "About", "Preferences...", and "Quit" menu
 * items to link them to the corresponding workbench commands, as well as
 * hooking in Close-Dialog behavior.
 *
 * This functionality uses Cocoa-specific natives as SWT doesn't provide an
 * abstraction for the application menu.
 *
 * @since 1.0
 */
public class CocoaUIHandler {
	// these constants are defined in IWorkbenchCommandConstants
	// but reproduced here to support pure-e4 apps
	private static final String COMMAND_ID_ABOUT = "org.eclipse.ui.help.aboutAction"; //$NON-NLS-1$
	private static final String COMMAND_ID_PREFERENCES = "org.eclipse.ui.window.preferences"; //$NON-NLS-1$
	private static final String COMMAND_ID_QUIT = "org.eclipse.ui.file.exit"; //$NON-NLS-1$
	private static final String COMMAND_PARAMETER_ID_MAY_PROMPT = "mayPrompt"; //$NON-NLS-1$
	// toggle coolbar isn't actually defined anywhere
	private static final String COMMAND_ID_TOGGLE_COOLBAR = "org.eclipse.ui.ToggleCoolbarAction"; //$NON-NLS-1$

	static long sel_toolbarButtonClicked_;
	private static final long NSWindowToolbarButton = 3;

	/* This callback is not freed */
	@SuppressWarnings("restriction")
	static Callback proc3Args;
	static final byte[] SWT_OBJECT = { 'S', 'W', 'T', '_', 'O', 'B', 'J', 'E', 'C', 'T', '\0' };

	SWTCocoaEnhancerDelegate delegate;
	private long delegateJniRef;

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
	@Inject
	@Optional
	protected IPresentationEngine engine;

	/**
	 *
	 */
	private void registerSelectors() {
		try {
			if (sel_toolbarButtonClicked_ == 0) {
				sel_toolbarButtonClicked_ = registerName("toolbarButtonClicked:"); //$NON-NLS-1$
				setupDelegateClass();
			}
		} catch (Exception e) {
			// theoretically, one of
			// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
		}
	}

	@SuppressWarnings("restriction")
	private void setupDelegateClass() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, NoSuchFieldException {
		// TODO: These should either move out of Display or be accessible to
		// this class.
		byte[] types = { '*', '\0' };
		int size = C.PTR_SIZEOF, align = C.PTR_SIZEOF == 4 ? 2 : 3;

		Class<?> clazz = CocoaUIHandler.class;

		proc3Args = new Callback(clazz, "actionProc", 3); //$NON-NLS-1$
		// call getAddress
		Method getAddress = Callback.class.getMethod("getAddress", new Class[0]); //$NON-NLS-1$
		Object object = getAddress.invoke(proc3Args);
		long proc3 = convertToLong(object);
		if (proc3 == 0)
			SWT.error(SWT.ERROR_NO_MORE_CALLBACKS);

		// call objc_allocateClassPair
		Field field = OS.class.getField("class_NSObject"); //$NON-NLS-1$
		Object fieldObj = field.get(OS.class);
		object = invokeMethod(OS.class, "objc_allocateClassPair", //$NON-NLS-1$
				new Object[] { fieldObj, "SWTCocoaEnhancerDelegate", wrapPointer(0) }); //$NON-NLS-1$
		long cls = convertToLong(object);

		invokeMethod(OS.class, "class_addIvar", new Object[] { //$NON-NLS-1$
				wrapPointer(cls), SWT_OBJECT, wrapPointer(size), new Byte((byte) align), types });

		// Add the action callback
		invokeMethod(OS.class, "class_addMethod", //$NON-NLS-1$
				new Object[] { wrapPointer(cls), wrapPointer(sel_toolbarButtonClicked_), wrapPointer(proc3), "@:@" }); //$NON-NLS-1$
		invokeMethod(OS.class, "objc_registerClassPair", //$NON-NLS-1$
				new Object[] { wrapPointer(cls) });
	}

	@SuppressWarnings("restriction")
	private long registerName(String name) throws IllegalArgumentException, SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Class<OS> clazz = OS.class;
		Object object = invokeMethod(clazz, "sel_registerName", new Object[] { name }); //$NON-NLS-1$
		return convertToLong(object);
	}

	/** Initialize the handler */
	@PostConstruct
	public void init() {
		registerSelectors();

		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				allocateDelegate(display);

				hookApplicationMenu();
				processModelMenus();

				// modify all shells opened on startup
				for (MWindow window : app.getChildren()) {
					modifyWindowShell(window);
				}
			}
		});
	}

	/**
	 * @param display
	 */
	protected void allocateDelegate(Display display) {
		try {
			delegate = new SWTCocoaEnhancerDelegate();
			delegate.alloc().init();
			// call OS.NewGlobalRef
			Method method = OS.class.getMethod("NewGlobalRef", new Class[] { Object.class }); //$NON-NLS-1$
			Object object = method.invoke(OS.class, new Object[] { CocoaUIHandler.this });
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
			Field idField = SWTCocoaEnhancerDelegate.class.getField("id"); //$NON-NLS-1$
			Object idValue = idField.get(delegate);
			invokeMethod(OS.class, "object_setInstanceVariable", //$NON-NLS-1$
					new Object[] { idValue, SWT_OBJECT, wrapPointer(delegateJniRef) });
			display.disposeExec(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if (delegateJniRef != 0) {
						try {
							invokeMethod(OS.class, "DeleteGlobalRef", new Object[] { wrapPointer(delegateJniRef) }); //$NON-NLS-1$
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

	void log(Exception e) {
		// StatusUtil.handleStatus(e, StatusManager.LOG);
		statusReporter.get().report(new Status(IStatus.WARNING, CocoaUIProcessor.FRAGMENT_ID,
				"Exception occurred during CocoaUI processing", e), StatusReporter.LOG); //$NON-NLS-1$
	}

	/*
	 * Listeners to tweak newly-opened workbench window shells with the proper
	 * OS flags.
	 */

	/** Watch for a window's "widget" attribute being flipped to a shell */
	@Inject
	@Optional
	private void monitorShellTopicChanges(@UIEventTopic(UIEvents.UIElement.TOPIC_WIDGET) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow
				&& event.getProperty(UIEvents.EventTags.NEW_VALUE) != null) {
			MWindow window = (MWindow) event.getProperty(UIEvents.EventTags.ELEMENT);
			modifyWindowShell(window);
			updateFullScreenStatus(window);
		}
	}

	/**
	 * Handle the Eclipse 4.0 compatibility case, where the window is created
	 * without a main menu or trim first, and then later when the main menu is
	 * being set it is time for us to do our work. It also handles dynamically
	 * created windows too.
	 */
	@Inject
	@Optional
	private void monitorMainMenuTopicChanges(@UIEventTopic(UIEvents.Window.TOPIC_MAINMENU) Event event) {
		Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);
		Object oldValue = event.getProperty(UIEvents.EventTags.OLD_VALUE);
		Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
		if (element instanceof MWindow && oldValue == null && newValue instanceof MMenu) {
			modifyWindowShell((MWindow) element);
		}
	}

	/** Watch for new menu contributions */
	@Inject
	@Optional
	private void monitorMenuContributionsChanges(
			@UIEventTopic(UIEvents.MenuContributions.TOPIC_MENUCONTRIBUTIONS) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MMenuContribution
				&& event.getProperty(UIEvents.EventTags.NEW_VALUE) != null) {
			MMenuContribution contribution = (MMenuContribution) event.getProperty(UIEvents.EventTags.ELEMENT);
			processMenuContribution(contribution);
		}
	}

	/** Watch for command changes */
	@Inject
	@Optional
	private void monitorCommandChanges(@UIEventTopic(UIEvents.Application.TOPIC_COMMANDS) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MCommand) {
			MCommand cmd = (MCommand) event.getProperty(UIEvents.EventTags.ELEMENT);
			String id = cmd.getElementId();
			if (COMMAND_ID_ABOUT.equals(id) || COMMAND_ID_PREFERENCES.equals(id) || COMMAND_ID_QUIT.equals(id)) {
				hookApplicationMenu();
			}
		}
	}

	/** Watch for a window's full-screen tag being flipped */
	@Inject
	@Optional
	private void monitorApplicationTagChanges(@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow) {
			MWindow window = (MWindow) event.getProperty(UIEvents.EventTags.ELEMENT);
			updateFullScreenStatus(window);
		}
	}

	/**
	 * @param window
	 */
	protected void updateFullScreenStatus(MWindow window) {
		// toggle full-screen is only available since MacOS X 10.7
		if (OS.VERSION < 0x1070 || !(window.getWidget() instanceof Shell)) {
			return;
		}
	}

	/**
	 * Modify the given workbench window shell bits to show the tool bar toggle
	 * button.
	 *
	 * @param window
	 *            the window to modify
	 * @since 3.2
	 */
	@SuppressWarnings("restriction")
	protected void modifyWindowShell(MWindow window) {
		if (window.getWidget() == null) {
			return;
		}
		if (window.getMainMenu() == null) {
			return;
		}
		redirectHandledMenuItems(window.getMainMenu());

		// the toolbar button is not available since MacOS X 10.7
		if (OS.VERSION >= 0x1070) {
			return;
		}
		// only add the button when either the cool bar or perspective bar
		// is initially visible. This is so that RCP applications can choose to
		// use this fragment without fear that their explicitly invisible bars
		// can't be shown.
		boolean trimInitiallyVisible = false;
		if (window instanceof MTrimmedWindow && !((MTrimmedWindow) window).getTrimBars().isEmpty()) {
			for (MTrimBar tb : ((MTrimmedWindow) window).getTrimBars()) {
				if (tb.isVisible()) {
					trimInitiallyVisible = true;
				}
			}
		}

		// It would also be worth checking if there's a command defined
		// for COMMAND_ID_TOGGLE_COOLBAR
		if (trimInitiallyVisible) {
			Shell shell = ((Control) window.getWidget()).getShell();
			NSWindow nsWindow = shell.view.window();
			// Add an empty, hidden tool bar to the window. Without this the
			// tool bar button at the top right of the window will not
			// appear even when setShowsToolbarButton(true) is called.
			// Unfortunately cannot just call shell.getToolBar() as it
			// allocates a properly-sized toolbar
			NSToolbar dummyBar = new NSToolbar();
			dummyBar.alloc();
			dummyBar.initWithIdentifier(NSString.stringWith("SWTToolbar")); //$NON-NLS-1$
			dummyBar.setVisible(false);
			nsWindow.setToolbar(dummyBar);
			dummyBar.release();
			nsWindow.setShowsToolbarButton(true);

			// Override the target and action of the toolbar button so we can
			// control it.
			try {
				Object fieldValue = wrapPointer(NSWindowToolbarButton);
				NSButton toolbarButton = (NSButton) invokeMethod(NSWindow.class, nsWindow, "standardWindowButton", //$NON-NLS-1$
						new Object[] { fieldValue });
				if (toolbarButton != null) {
					toolbarButton.setTarget(delegate);
					invokeMethod(NSControl.class, toolbarButton, "setAction", //$NON-NLS-1$
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
		if (elmtId != null && (elmtId.equals(COMMAND_ID_ABOUT) || elmtId.equals(COMMAND_ID_PREFERENCES)
				|| elmtId.equals(COMMAND_ID_QUIT))) {
			item.setVisible(false);
			item.setToBeRendered(false);
			if (engine != null) {
				engine.removeGui(item);
			}
		} else if (item instanceof MHandledMenuItem) {
			MHandledMenuItem mhmi = (MHandledMenuItem) item;
			elmtId = mhmi.getCommand() == null ? null : mhmi.getCommand().getElementId();
			if (elmtId != null && (elmtId.equals(COMMAND_ID_ABOUT) || elmtId.equals(COMMAND_ID_PREFERENCES)
					|| elmtId.equals(COMMAND_ID_QUIT))) {
				item.setVisible(false);
				item.setToBeRendered(false);
				if (engine != null) {
					engine.removeGui(item);
				}
			}
		}
	}

	// cribbed from SWT Snippet347
	private void hookApplicationMenu() {
		hookAppMenuItem(SWT.ID_QUIT, COMMAND_ID_QUIT);
		hookAppMenuItem(SWT.ID_PREFERENCES, COMMAND_ID_PREFERENCES);
		hookAppMenuItem(SWT.ID_ABOUT, COMMAND_ID_ABOUT);
	}

	private void hookAppMenuItem(int menuItemId, final String commandId) {
		final Display display = Display.getDefault();
		Menu[] menusToCheck = new Menu[] { display.getMenuBar(), display.getSystemMenu() };
		for (Menu topLevelMenu : menusToCheck) {
			if (topLevelMenu == null) {
				continue;
			}
			MenuItem item = findMenuItemById(topLevelMenu, menuItemId);
			if (item != null) {
				item.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (runCommand(commandId) || runAction(commandId)) {
							e.doit = false;
						}
					}
				});
			}
		}
	}

	/**
	 * @param menu
	 *            the containing menu
	 * @param menuItemId
	 * @return the menu item with {@code menuItemId} or null if not found
	 */
	private MenuItem findMenuItemById(Menu menu, int menuItemId) {
		for (MenuItem mi : menu.getItems()) {
			if (mi.getID() == menuItemId) {
				return mi;
			}
		}
		return null;
	}

	/**
	 * Locate an action (a menu item, actually) with the given id in the current
	 * menu bar and run it.
	 *
	 * @param actionId
	 *            the action to find
	 * @return true if an action was found, false otherwise
	 */
	private boolean runAction(String actionId) {
		MWindow window = app.getSelectedElement();
		if (window == null) {
			return false;
		}
		MMenu topMenu = window.getMainMenu();
		MMenuItem item = findAction(actionId, topMenu);
		if (item == null || !item.isEnabled()) {
			return false;
		}
		try {
			// disable the about and prefs items -- they shouldn't be
			// able to be run when another item is being triggered
			final Display display = Display.getDefault();
			MenuItem aboutItem = null;
			boolean aboutEnabled = true;
			MenuItem prefsItem = null;
			boolean prefsEnabled = true;

			Menu appMenuBar = display.getMenuBar();
			if (appMenuBar != null) {
				aboutItem = findMenuItemById(appMenuBar, SWT.ID_ABOUT);
				if (aboutItem != null) {
					aboutEnabled = aboutItem.getEnabled();
					aboutItem.setEnabled(false);
				}
				prefsItem = findMenuItemById(appMenuBar, SWT.ID_PREFERENCES);
				if (prefsItem != null) {
					prefsEnabled = prefsItem.getEnabled();
					prefsItem.setEnabled(false);
				}
			}
			try {
				simulateMenuSelection(item);
			} finally {
				if (prefsItem != null) {
					prefsItem.setEnabled(prefsEnabled);
				}
				if (aboutItem != null) {
					aboutItem.setEnabled(aboutEnabled);
				}
			}
		} catch (Exception e) {
			// theoretically, one of
			// SecurityException,Illegal*Exception,InvocationTargetException,NoSuch*Exception
			// not expected to happen at all.
			log(e);
			// return false?
		}
		return true;
	}

	private void simulateMenuSelection(MMenuItem item) {
		// FIXME: pity this code isn't available through the MMenuItem instance
		// somehow
		IEclipseContext lclContext = getContext(item);
		if (item instanceof MDirectMenuItem) {
			MDirectMenuItem dmi = (MDirectMenuItem) item;
			if (dmi.getObject() == null) {
				IContributionFactory cf = (IContributionFactory) lclContext.get(IContributionFactory.class.getName());
				dmi.setObject(cf.create(dmi.getContributionURI(), lclContext));
			}
			lclContext.set(MItem.class.getName(), item);
			ContextInjectionFactory.invoke(dmi.getObject(), Execute.class, lclContext);
			lclContext.remove(MItem.class.getName());
		} else if (item instanceof MHandledMenuItem) {
			MHandledMenuItem hmi = (MHandledMenuItem) item;
			EHandlerService service = (EHandlerService) lclContext.get(EHandlerService.class.getName());
			ParameterizedCommand cmd = hmi.getWbCommand();
			if (cmd == null) {
				cmd = generateParameterizedCommand(hmi);
			}
			lclContext.set(MItem.class.getName(), item);
			service.executeHandler(cmd);
			lclContext.remove(MItem.class.getName());
		} else {
			statusReporter.get()
					.report(new Status(IStatus.WARNING, CocoaUIProcessor.FRAGMENT_ID,
							"Unhandled menu type: " + item.getClass() + ": " + item), //$NON-NLS-1$ //$NON-NLS-2$
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
		if (commandService == null || handlerService == null) {
			return false;
		}
		Map<String, Object> params = COMMAND_ID_QUIT.equals(commandId)
				? Collections.singletonMap(COMMAND_PARAMETER_ID_MAY_PROMPT, (Object) "true") : null; //$NON-NLS-1$
		ParameterizedCommand cmd = commandService.createCommand(commandId, params);
		if (cmd == null) {
			return false;
		}
		// Unfortunately there's no way to check if a handler was available...
		// EHandlerService#executeHandler() returns null if a handler cannot be
		// found, but the handler itself could also return null too.
		handlerService.executeHandler(cmd);
		return true;
	}

	/**
	 * Find the action with the given ID by recursively crawling the provided
	 * menu manager. If the action cannot be found <code>null</code> is
	 * returned.
	 *
	 * @param actionId
	 *            the id to search for
	 * @param menu
	 *            the menu to search
	 * @return the action or <code>null</code>
	 */
	private MMenuItem findAction(String actionId, MMenu menu) {
		if (menu == null) {
			return null;
		}
		for (MMenuElement item : menu.getChildren()) {
			if (item instanceof MMenuItem) {
				MMenuItem mmi = (MMenuItem) item;
				if (mmi.getElementId() != null && mmi.getElementId().equals(actionId))
					return mmi;
				if (mmi instanceof MHandledMenuItem) {
					MHandledMenuItem mhmi = (MHandledMenuItem) mmi;
					if (mhmi.getCommand() != null && actionId.equals(mhmi.getCommand().getElementId())) {
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
	 * Action implementation for the toolbar button
	 */
	@SuppressWarnings("restriction")
	void toolbarButtonClicked(NSControl source) {
		try {
			NSWindow window = source.window();
			Field idField = NSWindow.class.getField("id"); //$NON-NLS-1$
			Object idValue = idField.get(window);

			Display display = Display.getCurrent();
			Widget widget = (Widget) invokeMethod(Display.class, display, "findWidget", new Object[] { idValue }); //$NON-NLS-1$

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

	static int actionProc(int id, int sel, int arg0) throws Exception {
		return (int) actionProc((long) id, (long) sel, (long) arg0);
	}

	@SuppressWarnings("restriction")
	static long actionProc(long id, long sel, long arg0) throws Exception {
		long[] jniRef = OS_object_getInstanceVariable(id, SWT_OBJECT);
		if (jniRef[0] == 0)
			return 0;

		CocoaUIHandler delegate = (CocoaUIHandler) invokeMethod(OS.class, "JNIGetObject", //$NON-NLS-1$
				new Object[] { wrapPointer(jniRef[0]) });

		if (sel == sel_toolbarButtonClicked_) {
			NSControl source = new_NSControl(arg0);
			delegate.toolbarButtonClicked(source);
		}

		return 0;
	}

	// The following methods reflectively call corresponding methods in the OS
	// class, using ints or longs as required based on platform.

	@SuppressWarnings("restriction")
	private static NSControl new_NSControl(long arg0) throws NoSuchMethodException, InstantiationException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<NSControl> clazz = NSControl.class;
		Class<?> PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		Constructor<NSControl> constructor = clazz.getConstructor(new Class[] { PTR_CLASS });
		return constructor.newInstance(new Object[] { wrapPointer(arg0) });
	}

	/**
	 * Specialized method. It's behavior is isolated and different enough from
	 * the usual invocation that custom code is warranted.
	 */
	@SuppressWarnings("restriction")
	private static long[] OS_object_getInstanceVariable(long delegateId, byte[] name) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		Class<OS> clazz = OS.class;
		Method method = null;
		Class<?> PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class) {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] { //$NON-NLS-1$
					long.class, byte[].class, long[].class });
			long[] resultPtr = new long[1];
			method.invoke(null, new Object[] { new Long(delegateId), name, resultPtr });
			return resultPtr;
		} else {
			method = clazz.getMethod("object_getInstanceVariable", new Class[] { //$NON-NLS-1$
					int.class, byte[].class, int[].class });
			int[] resultPtr = new int[1];
			method.invoke(null, new Object[] { new Integer((int) delegateId), name, resultPtr });
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

	private static Object invokeMethod(Class<?> clazz, String methodName, Object[] args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
			NoSuchMethodException {
		return invokeMethod(clazz, null, methodName, args);
	}

	private static Object invokeMethod(Class<?> clazz, Object target, String methodName, Object[] args)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, SecurityException,
			NoSuchMethodException {
		Class<?>[] signature = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			Class<?> thisClass = args[i].getClass();
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

	@SuppressWarnings("restriction")
	private static Object wrapPointer(long value) {
		Class<?> PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class)
			return new Long(value);
		else
			return new Integer((int) value);
	}

	private ParameterizedCommand generateParameterizedCommand(final MHandledItem item) {
		Map<String, Object> parameters = null;
		List<MParameter> modelParms = item.getParameters();
		if (modelParms != null && !modelParms.isEmpty()) {
			parameters = new HashMap<String, Object>();
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
		}
		ParameterizedCommand cmd = commandService.createCommand(item.getCommand().getElementId(), parameters);
		item.setWbCommand(cmd);
		return cmd;
	}
}
