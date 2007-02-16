/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.ISources;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.handlers.HandlerProxy;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.services.IEvaluationService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class CommandEnablementTest extends UITestCase {

	/**
	 * 
	 */
	private static final String CONTEXT_TEST2 = "org.eclipse.ui.command.contexts.enablement_test2";
	/**
	 * 
	 */
	private static final String CONTEXT_TEST1 = "org.eclipse.ui.command.contexts.enablement_test1";
	private static final String PREFIX = "tests.commands.CCT.";
	private static final String CMD1_ID = PREFIX + "cmd1";

	private ICommandService commandService;
	private IHandlerService handlerService;
	private IContextService contextService;

	private Command cmd1;
	private DefaultHandler normalHandler1;
	private IHandlerActivation activation1;
	private DefaultHandler normalHandler2;
	private IHandlerActivation activation2;
	private DisabledHandler disabledHandler1;
	private DisabledHandler disabledHandler2;
	private EnableEventHandler eventHandler1;
	private EnableEventHandler eventHandler2;
	private IEvaluationService evalService;

	/**
	 * @param testName
	 */
	public CommandEnablementTest(String testName) {
		super(testName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		commandService = (ICommandService) fWorkbench
				.getService(ICommandService.class);
		handlerService = (IHandlerService) fWorkbench
				.getService(IHandlerService.class);
		contextService = (IContextService) fWorkbench
				.getService(IContextService.class);
		evalService = (IEvaluationService) fWorkbench
				.getService(IEvaluationService.class);
		cmd1 = commandService.getCommand(CMD1_ID);
		normalHandler1 = new DefaultHandler();
		normalHandler2 = new DefaultHandler();
		disabledHandler1 = new DisabledHandler();
		disabledHandler2 = new DisabledHandler();
		eventHandler1 = new EnableEventHandler();
		eventHandler2 = new EnableEventHandler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		if (activation1 != null) {
			handlerService.deactivateHandler(activation1);
			activation1 = null;
		}
		if (activation2 != null) {
			handlerService.deactivateHandler(activation2);
			activation2 = null;
		}
		super.doTearDown();
	}

	private static class DefaultHandler extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}
	}

	private static class DisabledHandler extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
		 */
		public boolean isEnabled() {
			return false;
		}
	}

	private static class EnableEventHandler extends AbstractHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			HandlerUtil.getActiveContextsChecked(event);
			return null;
		}

		private boolean fEnabled = true;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
		 */
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

	private static class EnablementListener implements ICommandListener {
		public int enabledChanged = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.ICommandListener#commandChanged(org.eclipse.core.commands.CommandEvent)
		 */
		public void commandChanged(CommandEvent commandEvent) {
			if (commandEvent.isEnabledChanged()) {
				enabledChanged++;
			}
		}
	}

	public void testEnablementForNormalHandlers() throws Exception {
		activation1 = handlerService.activateHandler(CMD1_ID, normalHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, normalHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());

		IContextActivation test1 = contextService
				.activateContext(CONTEXT_TEST1);
		assertTrue(cmd1.isHandled());
		assertTrue(cmd1.isEnabled());
		assertEquals(normalHandler1, cmd1.getHandler());
		contextService.deactivateContext(test1);
		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());

		IContextActivation test2 = contextService
				.activateContext(CONTEXT_TEST2);
		assertTrue(cmd1.isHandled());
		assertTrue(cmd1.isEnabled());
		assertEquals(normalHandler2, cmd1.getHandler());
		contextService.deactivateContext(test2);
		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
	}

	public void testEventsForNormalHandlers() throws Exception {
		// incremented for every change that should change enablement
		int enabledChangedCount = 0;

		activation1 = handlerService.activateHandler(CMD1_ID, normalHandler1,
				new ActiveContextExpression(CONTEXT_TEST1,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));
		activation2 = handlerService.activateHandler(CMD1_ID, normalHandler2,
				new ActiveContextExpression(CONTEXT_TEST2,
						new String[] { ISources.ACTIVE_CONTEXT_NAME }));

		assertFalse(cmd1.isHandled());
		assertFalse(cmd1.isEnabled());
		EnablementListener listener = new EnablementListener();
		cmd1.addCommandListener(listener);

		try {
			IContextActivation test1 = contextService
					.activateContext(CONTEXT_TEST1);
			enabledChangedCount++;
			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(normalHandler1, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(test1);
			enabledChangedCount++;
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			IContextActivation test2 = contextService
					.activateContext(CONTEXT_TEST2);
			enabledChangedCount++;
			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(normalHandler2, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(test2);
			enabledChangedCount++;
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

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
			IContextActivation test1 = contextService
					.activateContext(CONTEXT_TEST1);
			assertTrue(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(disabledHandler1, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(test1);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			IContextActivation test2 = contextService
					.activateContext(CONTEXT_TEST2);
			assertTrue(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(disabledHandler2, cmd1.getHandler());
			assertEquals(enabledChangedCount, listener.enabledChanged);

			contextService.deactivateContext(test2);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

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
			IContextActivation test1 = contextService
					.activateContext(CONTEXT_TEST1);
			enabledChangedCount++;

			assertTrue(cmd1.isHandled());
			assertTrue(cmd1.isEnabled());
			assertEquals(eventHandler1, cmd1.getHandler());
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

			contextService.deactivateContext(test1);
			assertFalse(cmd1.isHandled());
			assertFalse(cmd1.isEnabled());
			assertEquals(enabledChangedCount, listener.enabledChanged);
		} finally {
			cmd1.removeCommandListener(listener);
		}
	}

	public void testCommandWithHandlerProxy() throws Exception {
		IConfigurationElement handlerProxyConfig = null;
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint("org.eclipse.ui.handlers");
		IExtension[] extensions = point.getExtensions();
		boolean found = false;
		for (int i = 0; i < extensions.length && !found; i++) {
			IConfigurationElement[] configElements = extensions[i]
					.getConfigurationElements();
			for (int j = 0; j < configElements.length && !found; j++) {
				if (configElements[j].getAttribute(
						IWorkbenchRegistryConstants.ATT_CLASS).equals(
						"org.eclipse.ui.tests.menus.HelloEHandler")) {
					handlerProxyConfig = configElements[j];
					found = true;
				}
			}
		}
		assertNotNull(handlerProxyConfig);
		Expression enabledWhen = new ActiveContextExpression(CONTEXT_TEST1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		HandlerProxy proxy = new HandlerProxy(handlerProxyConfig, "class",
				enabledWhen, evalService);
		assertFalse(proxy.isEnabled());
		IContextActivation test1 = contextService
				.activateContext(CONTEXT_TEST1);
		assertTrue(proxy.isEnabled());
		contextService.deactivateContext(test1);
		assertFalse(proxy.isEnabled());
	}
}
