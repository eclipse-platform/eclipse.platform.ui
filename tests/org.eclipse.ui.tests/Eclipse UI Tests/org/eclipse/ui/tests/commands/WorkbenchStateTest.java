/*******************************************************************************
 * Copyright (c) 2023, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vasili Gulevich - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.commands;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.tests.menus.HandlerWithStateMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class WorkbenchStateTest {
	/**
	 * The object to which the command is set as a test.
	 */
	private static final Object OBJECT_CHANGED = "CHANGED";

	/**
	 * The object to which the command is set before the test starts.
	 */
	private static final Object OBJECT_INITIAL = "INITIAL";

	/**
	 * The identifier of the state storing a simple object.
	 */
	private static final String OBJECT_STATE_ID = "OBJECT";

	private static final String VIEW_ID = "org.eclipse.ui.tests.api.MenuTestHarness";
	private static final String CONTEXT_ID = "org.eclipse.ui.tests.issue925";

	private final IWorkbench fWorkbench = PlatformUI.getWorkbench();
	private final ICommandService commandService = fWorkbench.getService(ICommandService.class);
	private final IContextService contextService = fWorkbench.getService(IContextService.class);

	private IWorkbenchWindow testWindow;

	private final Command command = commandService.getCommand("org.eclipse.ui.tests.commandWithState");

	@Before
	public final void before() {
		testWindow = openTestWindow();
		IViewPart view = testWindow.getActivePage().findView(VIEW_ID);
		Assert.assertNull(view);
		Assert.assertFalse(
				((Collection<String>) contextService.getActiveContextIds()).contains("org.eclipse.ui.tests.issue925"));
		command.getState(OBJECT_STATE_ID).setValue(OBJECT_INITIAL);
		command.getState(RegistryToggleState.STATE_ID).setValue(false);
		Assert.assertFalse(command.isEnabled());
		Assert.assertFalse(command.isHandled());
		Assert.assertNull(getHandlerState());
	}

	@After
	public final void after() {
		testWindow.close();
	}

	@Test
	public final void commandListensHandler() {
		// https://github.com/eclipse-platform/eclipse.platform.ui/issues/925
		// This test forces handler reassociation by explicitly polling command
		// accessors like isHandled()

		IContextActivation contextActivation = contextService.activateContext(CONTEXT_ID);
		try {
			// side effect: handler reassociation
			Assert.assertTrue("Command should be handled once context activates", command.isHandled());
			assertSame("The initial state was not correct", OBJECT_INITIAL,
					command.getState(OBJECT_STATE_ID).getValue());
			assertSame("Handler state should match command state", command.getState(OBJECT_STATE_ID).getValue(),
					getHandlerState());
			HandlerWithStateMock.INSTANCE.getState(OBJECT_STATE_ID).setValue(OBJECT_CHANGED);
			assertSame("Handler state should match command state", command.getState(OBJECT_STATE_ID).getValue(),
					getHandlerState());
		} finally {
			contextService.deactivateContext(contextActivation);
		}
		// side effect: handler reassociation
		Assert.assertFalse("Command should not be handled outside of context", command.isHandled());
		Assert.assertNull("Handler should have states removed when context exits", getHandlerState());
	}

	@Test
	public final void toolbarCheckStateIsUpdated() {
		// https://github.com/eclipse-platform/eclipse.platform.ui/issues/925
		// This test relies on toolbar context monitoring to perform command queries on
		// context switch, command queries then reassociate handlers
		// See org.eclipse.e4.ui.workbench.renderers.swt.ToolItemUpdater

		final IMenuService menus = testWindow.getService(IMenuService.class);
		// The toolbar has to be visible,for its updater to kick in, so we have to use
		// an open window
		ToolBar toolbar = new ToolBar(testWindow.getShell(), 0);
		final ToolBarManager toolbarManager = new ToolBarManager(toolbar);
		menus.populateContributionManager(toolbarManager, "toolbar:org.eclipse.ui.tests.commands.WorkbenchStateTest");
		try {

			BooleanSupplier isChecked = () -> itemByLabel(toolbar, "Command Wtih State").getSelection();

			assertEventually("Item should be unchecked initially", not(isChecked));

			IContextActivation contextActivation = contextService.activateContext(CONTEXT_ID);
			try {
				assertEventually("Handler is associated", () -> getHandlerState() != null);
				assertEventually("Initial state of command is unchecked", not(isChecked));

				HandlerWithStateMock.INSTANCE.getState(RegistryToggleState.STATE_ID).setValue(true);

				assertEventually("Item should be checked on a request from handler", isChecked);

				HandlerWithStateMock.INSTANCE.getState(RegistryToggleState.STATE_ID).setValue(false);

				assertEventually("Item should be unchecked on a request from handler", not(isChecked));

			} finally {
				contextService.deactivateContext(contextActivation);
			}
		} finally {
			menus.releaseContributions(toolbarManager);
		}

	}

	@Test
	public final void viewToolbarReassociatesCommandEventually() throws PartInitException {
		// https://github.com/eclipse-platform/eclipse.platform.ui/issues/925
		// This test relies on toolbar context monitoring to perform command queries on
		// context switch, command queries then reassociate handlers
		// The toolbar is automatically populated with test view, making this a full
		// integration test

		testWindow.getActivePage().showView(VIEW_ID);

		Assert.assertNull("Handler should not be initialized with states, until context is activated",
				getHandlerState());

		IContextActivation contextActivation = contextService.activateContext(CONTEXT_ID);
		try {
			// View shows toolbar that should eventually poll the command causing
			// state reassociation
			// As toolbar updates are throttled, some wait might be necessary
			assertEventually("Handler should be initialized when context is active",
					() -> OBJECT_INITIAL == getHandlerState());

			HandlerWithStateMock.INSTANCE.getState(OBJECT_STATE_ID).setValue(OBJECT_CHANGED);

			assertSame("Command state should match handler state", OBJECT_CHANGED, getCommandState());

		} finally {
			contextService.deactivateContext(contextActivation);
		}
		assertEventually("Handler should have states removed when context exits", () -> getHandlerState() == null);
	}

	@Test
	public final void toolbarReassociatesCommandEventually() {
		// https://github.com/eclipse-platform/eclipse.platform.ui/issues/925
		// Here we test states the are not related to selection or enablement, as those
		// have special handling in workbench

		final IMenuService menus = testWindow.getService(IMenuService.class);
		final ToolBarManager toolbarManager = new ToolBarManager(new ToolBar(testWindow.getShell(), 0));
		menus.populateContributionManager(toolbarManager, "toolbar:org.eclipse.ui.tests.commands.WorkbenchStateTest");
		try {
			Assert.assertNull("Handler should not be initialized with states, until context is activated",
					getHandlerState());

			IContextActivation contextActivation = contextService.activateContext(CONTEXT_ID);
			try {
				// View shows toolbar that should eventually poll the command causing
				// state reassociation
				// As toolbar updates are throttled, some wait might be necessary
				assertEventually("Handler should be initialized when context is active",
						() -> OBJECT_INITIAL == getHandlerState());

				HandlerWithStateMock.INSTANCE.getState(OBJECT_STATE_ID).setValue(OBJECT_CHANGED);

				assertSame("Command state should match handler state", OBJECT_CHANGED, getCommandState());

			} finally {
				contextService.deactivateContext(contextActivation);
			}
			assertEventually("Handler should have states removed when context exits", () -> getHandlerState() == null);
		} finally {
			menus.releaseContributions(toolbarManager);
		}
	}

	private Object getHandlerState() {
		if (HandlerWithStateMock.INSTANCE == null) {
			return null;
		}
		State state = HandlerWithStateMock.INSTANCE.getState(OBJECT_STATE_ID);
		if (state == null) {
			return null;
		}
		return state.getValue();
	}

	private Object getCommandState() {
		State state = command.getState(OBJECT_STATE_ID);
		if (state == null) {
			return null;
		}
		return state.getValue();
	}

	private static void assertEventually(String message, BooleanSupplier predicate) {
		Display display = PlatformUI.getWorkbench().getDisplay();
		// Run defered operations
		for (int i = 0; i < 10; i++) {
			if (display != null && !display.isDisposed()) {
				while (!display.isDisposed() && display.readAndDispatch()) {
				}
				Thread.yield();
			}
		}

		long stop = System.currentTimeMillis() + 1000;
		while (System.currentTimeMillis() < stop && !display.isDisposed()) {
			if (predicate.getAsBoolean()) {
				return;
			}
			while (!display.isDisposed() && display.readAndDispatch()) {
			}
			Thread.yield();
		}
		Assert.fail(message);
	}

	private static BooleanSupplier not(BooleanSupplier isChecked) {
		Objects.requireNonNull(isChecked);
		return () -> !isChecked.getAsBoolean();
	}

	private ToolItem itemByLabel(ToolBar toolbar, String label) {
		for (ToolItem item : toolbar.getItems()) {
			if (label.equals(item.getText())) {
				return item;
			}
		}
		return null;
	}

}
