/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.commands;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.expressions.CountExpression;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.handlers.E4HandlerProxy;
import org.eclipse.ui.internal.handlers.HandlerProxy;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.WorkbenchSourceProvider;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.ISourceProviderService;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

/**
 * @since 3.3
 */
@Ignore("broke during e4 transition and still need adjustments")
public class CommandEnablementTest {

	@Rule
	public CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	private static final String CONTEXT_TEST2 = "org.eclipse.ui.command.contexts.enablement_test2";
	private static final String CONTEXT_TEST1 = "org.eclipse.ui.command.contexts.enablement_test1";
	private static final String PREFIX = "tests.commands.CCT.";
	private static final String CMD1_ID = PREFIX + "cmd1";
	private static final String CMD3_ID = PREFIX + "cmd3";

	private ICommandService commandService;
	private IHandlerService handlerService;
	private IContextService contextService;

	private Command cmd1;
	private Command cmd3;
	private DefaultHandler normalHandler1;
	private IHandlerActivation activation1;
	private DefaultHandler normalHandler2;
	private IHandlerActivation activation2;
	private DisabledHandler disabledHandler1;
	private DisabledHandler disabledHandler2;
	private EnableEventHandler eventHandler1;
	private EnableEventHandler eventHandler2;
	private IEvaluationService evalService;
	private CheckContextHandler contextHandler;
	private IContextActivation contextActivation1;
	private IContextActivation contextActivation2;
	private IWorkbench fWorkbench;

	@Before
	public void doSetUp() throws Exception {
		fWorkbench = PlatformUI.getWorkbench();
		commandService = fWorkbench.getService(ICommandService.class);
		handlerService = fWorkbench.getService(IHandlerService.class);
		contextService = fWorkbench.getService(IContextService.class);
		evalService = fWorkbench.getService(IEvaluationService.class);
		cmd1 = commandService.getCommand(CMD1_ID);
		cmd3 = commandService.getCommand(CMD3_ID);
		normalHandler1 = new DefaultHandler();
		normalHandler2 = new DefaultHandler();
		disabledHandler1 = new DisabledHandler();
		disabledHandler2 = new DisabledHandler();
		eventHandler1 = new EnableEventHandler();
		eventHandler2 = new EnableEventHandler();
		contextHandler = new CheckContextHandler();
	}

	@After
	public void doTearDown() throws Exception {
		if (activation1 != null) {
			handlerService.deactivateHandler(activation1);
			activation1 = null;
		}
		if (activation2 != null) {
			handlerService.deactivateHandler(activation2);
			activation2 = null;
		}
		if (contextActivation1 != null) {
			contextService.deactivateContext(contextActivation1);
			contextActivation1 = null;
		}
		if (contextActivation2 != null) {
			contextService.deactivateContext(contextActivation2);
			contextActivation2 = null;
		}
	}

	private static class DefaultHandler extends AbstractHandler {

		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}
	}

	private static class DisabledHandler extends AbstractHandler {

		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}

		@Override
		public boolean isEnabled() {
			return false;
		}
	}

	private static class EnableEventHandler extends AbstractHandler {

		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}

		private boolean fEnabled = true;

		@Override
		public boolean isEnabled() {
			return fEnabled;
		}

		public void setEnabled(boolean enabled) {
			if (fEnabled != enabled) {
				fEnabled = enabled;
				fireHandlerChanged(new HandlerEvent(this, true, false));
			}
		}
	}

	private static class CheckContextHandler extends AbstractHandler {

		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActivePartChecked(event);
			return null;
		}

		@Override
		public void setEnabled(Object applicationContext) {
			Object o = HandlerUtil.getVariable(applicationContext,
					ISources.ACTIVE_PART_NAME);
			setBaseEnabled(o instanceof IWorkbenchPart);
		}
	}

	private static class EnablementListener implements ICommandListener {
		public int enabledChanged = 0;

		@Override
		public void commandChanged(CommandEvent commandEvent) {
			if (commandEvent.isEnabledChanged()) {
				enabledChanged++;
			}
		}
	}

	static class UpdatingHandler extends AbstractHandler implements IElementUpdater {

		private final String text;

		public UpdatingHandler(String text) {
			this.text = text;
		}

		@Override
		public void updateElement(UIElement element, Map parameters) {
			element.setText(text);
		}

		@Override
		public Object execute(ExecutionEvent event) {
			return null;
		}

	}

	// Test is currently failing for numerous reason:
	// - Items are not CommandContributionItem but HandledContributionItem
	// - Handlers are not instances of HandlerProxy but
	// WorkbenchHandlerServiceHandler
	// - The actual changes only happen in the MenuManager, if a real Menu is
	// attached
	// Even when changing test to address this via
	// a) changing assertions to deal with HandledContributionItem
	// b) manually inserting a CommandContributionItem to the MenuManager
	// the actual restoring of the default text does not seem to work anymore.
	// Bug 275126 might again be seen.
	// Related bugs possibly breaking this: 382839, 394336
	@Test
	public void testRestoreContributedUI() throws Exception {

		Field iconField = CommandContributionItem.class.getDeclaredField("icon");
		iconField.setAccessible(true);

		Field labelField = CommandContributionItem.class.getDeclaredField("label");
		labelField.setAccessible(true);

		String menuId = "org.eclipse.ui.tests.Bug275126";
		MenuManager manager = new MenuManager(null, menuId);
		IMenuService menuService = fWorkbench.getService(IMenuService.class);
		menuService.populateContributionManager(manager, MenuUtil.menuUri(menuId));
		IContributionItem[] items = manager.getItems();
		assertEquals(1, items.length);
		assertTrue(items[0] instanceof CommandContributionItem);
		CommandContributionItem item = (CommandContributionItem) items[0];

		String text1 = "text1";
		String text2 = "text2";

		// contributed from plugin.xml
		String contributedLabel = "Contributed Label";

		// default handler
		assertTrue(cmd3.getHandler() instanceof HandlerProxy);
		assertEquals(contributedLabel, labelField.get(item));
		assertNotNull(iconField.get(item));

		UpdatingHandler handler1 = new UpdatingHandler(text1);
		activation1 = handlerService.activateHandler(CMD3_ID, handler1, new ActiveContextExpression(CONTEXT_TEST1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		UpdatingHandler handler2 = new UpdatingHandler(text2);
		activation2 = handlerService.activateHandler(CMD3_ID, handler2, new ActiveContextExpression(CONTEXT_TEST2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
		assertEquals(handler1, cmd3.getHandler());
		assertEquals(text1, labelField.get(item));
		assertNotNull(iconField.get(item));

		contextService.deactivateContext(contextActivation1);
		// back to default handler state
		assertTrue(cmd3.getHandler() instanceof HandlerProxy);
		assertEquals(contributedLabel, labelField.get(item));
		assertNotNull(iconField.get(item));

		contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
		assertEquals(handler2, cmd3.getHandler());
		assertEquals(text2, labelField.get(item));
		assertNotNull(iconField.get(item));

		// activate both context
		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);

		// both handler activations eval to true, no handler set
		assertNull(cmd3.getHandler());
		assertEquals(contributedLabel, labelField.get(item));
		assertNotNull(iconField.get(item));

		contextService.deactivateContext(contextActivation1);
		contextService.deactivateContext(contextActivation2);

	}


	@Test
	public void testEnablementForNormalHandlers() throws Exception {
		activation1 = handlerService.activateHandler(CMD1_ID, normalHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, normalHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());

		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
		assertTrue(cmd1.isHandled());
		assertTrue(cmd1.isEnabled());
		assertEquals(normalHandler1, getHandler(cmd1));
		contextService.deactivateContext(contextActivation1);
		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());

		contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
		assertTrue(cmd1.isHandled());
		assertTrue(cmd1.isEnabled());
		assertEquals(normalHandler2, getHandler(cmd1));
		contextService.deactivateContext(contextActivation2);
		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
	}

	private IHandler getHandler(Command command) {
		EHandlerService service = fWorkbench.getService(EHandlerService.class);
		if (service == null) {
			return null;
		}
		IEclipseContext ctx = fWorkbench.getService(IEclipseContext.class);
		Object handler = HandlerServiceImpl.lookUpHandler(ctx, command.getId());
		if (handler instanceof E4HandlerProxy e4HandlerProxy) {
			return e4HandlerProxy.getHandler();
		}
		return null;
	}

	@Test
	public void testEventsForNormalHandlers() throws Exception {
		// incremented for every change that should change enablement
		int enabledChangedCount = 0;

		activation1 = handlerService.activateHandler(CMD1_ID, normalHandler1,
				new ActiveContextExpression(CONTEXT_TEST1, new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, normalHandler2,
				new ActiveContextExpression(CONTEXT_TEST2, new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		IEclipseContext ctx = fWorkbench.getService(IEclipseContext.class);
		ctx.processWaiting();

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		EnablementListener listener = new EnablementListener();
		cmd1.addCommandListener(listener);

		try {
			contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
			enabledChangedCount++;
			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(normalHandler1, getHandler(cmd1));
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation1);
			enabledChangedCount++;
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
			enabledChangedCount++;
			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(normalHandler2, getHandler(cmd1));
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation2);
			enabledChangedCount++;
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

	@Test
	public void testEventsForDisabledHandlers() throws Exception {
		// incremented for every change that should change enablement
		int enabledChangedCount = 0;

		activation1 = handlerService.activateHandler(CMD1_ID, disabledHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, disabledHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		EnablementListener listener = new EnablementListener();
		cmd1.addCommandListener(listener);

		try {
			contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
			assertTrue(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(disabledHandler1, getHandler(cmd1));
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation1);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextActivation2 = contextService.activateContext(CONTEXT_TEST2);
			assertTrue(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(disabledHandler2, getHandler(cmd1));
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(contextActivation2);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

	// Failure analysis:
	// The isEnabled() assertions are correct, however, the
	// enabledChanged listener is called more often than expected
	// due to the way HandlerServiceHandler implements
	// isEnabled() and setEnabled().
	@Test
	public void testEventsForEnabledHandlers() throws Exception {
		// incremented for every change that should change enablement
		int enabledChangedCount = 0;

		activation1 = handlerService.activateHandler(CMD1_ID, eventHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, eventHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		EnablementListener listener = new EnablementListener();
		cmd1.addCommandListener(listener);

		try {
			contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
			enabledChangedCount++;

			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(eventHandler1, getHandler(cmd1));
			assertEquals(enabledChangedCount, listener.enabledChanged);

			eventHandler1.setEnabled(true);
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertTrue(cmd1.isEnabled());

			eventHandler1.setEnabled(false);
			enabledChangedCount++;
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			eventHandler1.setEnabled(false);
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			eventHandler1.setEnabled(true);
			enabledChangedCount++;
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertTrue(cmd1.isEnabled());

			eventHandler1.setEnabled(false);
			enabledChangedCount++;
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			eventHandler2.setEnabled(false);
			eventHandler2.setEnabled(true);
			eventHandler2.setEnabled(false);
			eventHandler2.setEnabled(true);
			assertEquals(enabledChangedCount, listener.enabledChanged);
			assertFalse(cmd1.isEnabled());

			contextService.deactivateContext(contextActivation1);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

	@Test
	public void testCommandWithHandlerProxy() throws Exception {
		IConfigurationElement handlerProxyConfig = null;
		String commandId = null;
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui.handlers");
		IExtension[] extensions = point.getExtensions();
		boolean found = false;
		for (int i = 0; i < extensions.length && !found; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length && !found; j++) {
				String attrClass = configElements[j].getAttribute(
						IWorkbenchRegistryConstants.ATT_CLASS);
				if (attrClass != null && attrClass.equals(
						"org.eclipse.ui.tests.menus.HelloEHandler")) {
					handlerProxyConfig = configElements[j];
					commandId = handlerProxyConfig
							.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
					found = true;
				}
			}
		}
		assertNotNull(handlerProxyConfig);
		Expression enabledWhen = new ActiveContextExpression(CONTEXT_TEST1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		HandlerProxy proxy = new HandlerProxy(commandId, handlerProxyConfig,
				"class", enabledWhen, evalService);
		assertFalse(proxy.isEnabled());
		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
		assertTrue(proxy.isEnabled());
		contextService.deactivateContext(contextActivation1);
		assertFalse(proxy.isEnabled());
	}

	private static class Checker implements IHandlerListener {
		boolean lastChange = false;

		@Override
		public void handlerChanged(HandlerEvent handlerEvent) {
			lastChange = handlerEvent.isEnabledChanged();
		}
	}

	// Test failure analysis:
	// - Selections passed to WorkbenchSourceProvider#selectionChanged()
	//   never make it into the IEclipseContext.
	// - When using ESelectionService to modify the selection, the
	//   test passes.
	// Intended behavior? Bug?
	@Test
	public void testEnablementWithHandlerProxy() throws Exception {
		IConfigurationElement handlerProxyConfig = null;
		String commandId = null;
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui.handlers");
		IExtension[] extensions = point.getExtensions();
		boolean found = false;
		for (int i = 0; i < extensions.length && !found; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length && !found; j++) {
				if (configElements[j].getAttribute(
						IWorkbenchRegistryConstants.ATT_COMMAND_ID).equals(
						"org.eclipse.ui.tests.enabledCount")) {
					handlerProxyConfig = configElements[j];
					commandId = handlerProxyConfig
							.getAttribute(IWorkbenchRegistryConstants.ATT_ID);
					found = true;
				}
			}
		}
		assertNotNull(handlerProxyConfig);
		Expression enabledWhen = ExpressionConverter.getDefault()
				.perform(
						handlerProxyConfig.getChildren("enabledWhen")[0]
								.getChildren()[0]);
		assertTrue(enabledWhen instanceof CountExpression);
		HandlerProxy proxy = new HandlerProxy(commandId, handlerProxyConfig, "class",
				enabledWhen, evalService);
		Checker listener = new Checker();
		proxy.addHandlerListener(listener);
		assertFalse(proxy.isEnabled());
		ISourceProviderService providers = fWorkbench
				.getService(ISourceProviderService.class);
		WorkbenchSourceProvider selectionProvider = (WorkbenchSourceProvider) providers
				.getSourceProvider(ISources.ACTIVE_CURRENT_SELECTION_NAME);

		selectionProvider.selectionChanged(null, StructuredSelection.EMPTY);
		assertFalse(proxy.isEnabled());
		assertFalse(listener.lastChange);

		selectionProvider.selectionChanged(null, new StructuredSelection(
				new Object()));
		assertFalse(proxy.isEnabled());
		assertFalse(listener.lastChange);

		selectionProvider.selectionChanged(null, new StructuredSelection(
				new Object[] { new Object(), new Object() }));
		assertTrue(proxy.isEnabled());
		assertTrue(listener.lastChange);

		listener.lastChange = false;
		selectionProvider.selectionChanged(null, new StructuredSelection(
				new Object[] { new Object(), new Object(), new Object() }));
		assertFalse(proxy.isEnabled());
		assertTrue(listener.lastChange);
	}

	// Test failure analysis:
	// - Handlers are instances of WorkbenchHandlerServiceHandler
	// - WorkbenchHandlerServiceHandler#setEnabled(Object) does not correctly
	//   deal with IEvaluationContext instances being passed, i.e. none of the
	//   variables in the snapshot created in the test are actually taken into
	//   account.
	// - Even when doing so by modifying
	//   WorkbenchHandlerServiceHandler#setEnabled(Object), e.g. via
	//   staticContext.set(IEvaluationContext.class, ...);
	//   wont't help:
	//   When WorkbenchHandlerServiceHandler#isEnabled() is called, the previous
	//   call to #setEnabled(Object) is not relevant anymore, since the currently
	//   active IEclipseContext is asked.
	// Related bugs possibly breaking this: 382839, 394336
	@Test
	public void testEnablementForLocalContext() throws Exception {
		openTestWindow("org.eclipse.ui.resourcePerspective");
		activation1 = handlerService.activateHandler(CMD1_ID, contextHandler,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		IEvaluationContext snapshot = handlerService
				.createContextSnapshot(false);
		cmd1.setEnabled(snapshot);
		assertFalse(cmd1.isEnabled());

		contextActivation1 = contextService.activateContext(CONTEXT_TEST1);
		assertTrue(cmd1.isHandled());
		cmd1.setEnabled(snapshot);
		assertTrue(cmd1.isEnabled());
		assertEquals(contextHandler, getHandler(cmd1));

		snapshot.removeVariable(ISources.ACTIVE_PART_NAME);
		assertTrue(cmd1.isHandled());
		cmd1.setEnabled(snapshot);
		assertFalse(cmd1.isEnabled());
		cmd1.setEnabled(handlerService.getCurrentState());
		assertTrue(cmd1.isEnabled());
		assertEquals(contextHandler, getHandler(cmd1));

		snapshot.addVariable(ISources.ACTIVE_PART_NAME, handlerService
				.getCurrentState().getVariable(ISources.ACTIVE_PART_NAME));
		cmd1.setEnabled(snapshot);
		assertTrue(cmd1.isEnabled());
		cmd1.setEnabled(handlerService.getCurrentState());
		assertTrue(cmd1.isEnabled());
		assertEquals(contextHandler, getHandler(cmd1));
	}

}
