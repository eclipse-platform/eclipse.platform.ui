/*******************************************************************************
 * Copyright (c) 2008, 2018 Adobe Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Adobe Systems, Inc. - initial API and implementation
 *     IBM Corporation - cleanup
 *     Brian de Alwis - adapted to e4
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 462407
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt.cocoa;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
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

	/** Initialize the handler */
	@PostConstruct
	public void init() {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
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
	 * Listeners to tweak newly-opened workbench window shells with the proper OS
	 * flags.
	 */

	/** Watch for a window's "widget" attribute being flipped to a shell */
	@Inject
	@Optional
	private void monitorShellTopicChanges(@UIEventTopic(UIEvents.UIElement.TOPIC_WIDGET) Event event) {
		if (event.getProperty(UIEvents.EventTags.ELEMENT) instanceof MWindow
				&& event.getProperty(UIEvents.EventTags.NEW_VALUE) != null) {
			MWindow window = (MWindow) event.getProperty(UIEvents.EventTags.ELEMENT);
			modifyWindowShell(window);
		}
	}

	/**
	 * Handle the Eclipse 4.0 compatibility case, where the window is created
	 * without a main menu or trim first, and then later when the main menu is being
	 * set it is time for us to do our work. It also handles dynamically created
	 * windows too.
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

	/**
	 * Modify the given workbench window shell bits to show the tool bar toggle
	 * button.
	 *
	 * @param window the window to modify
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
	 * @param menu       the containing menu
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
	 * @param actionId the action to find
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
				? Collections.singletonMap(COMMAND_PARAMETER_ID_MAY_PROMPT, (Object) "true") //$NON-NLS-1$
				: null;
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
	 * Find the action with the given ID by recursively crawling the provided menu
	 * manager. If the action cannot be found <code>null</code> is returned.
	 *
	 * @param actionId the id to search for
	 * @param menu     the menu to search
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

	private ParameterizedCommand generateParameterizedCommand(final MHandledItem item) {
		Map<String, Object> parameters = null;
		List<MParameter> modelParms = item.getParameters();
		if (modelParms != null && !modelParms.isEmpty()) {
			parameters = new HashMap<>();
			for (MParameter mParm : modelParms) {
				parameters.put(mParm.getName(), mParm.getValue());
			}
		}
		ParameterizedCommand cmd = commandService.createCommand(item.getCommand().getElementId(), parameters);
		item.setWbCommand(cmd);
		return cmd;
	}
}
