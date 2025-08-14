/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
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
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests various aspects of command state.
 *
 * @since 3.2
 */
@RunWith(JUnit4.class)
public class HandlerActivationTest extends UITestCase {
	static class ActTestHandler extends AbstractHandler {
		public String contextId;

		public int executionCount = 0;

		public ActTestHandler(String id) {
			super();
			contextId = id;
		}

		@Override
		public Object execute(ExecutionEvent event) {
			executionCount++;
			return null;
		}

	}

	static class OutlineOnlyHandler extends AbstractHandler {
		@Override
		public Object execute(ExecutionEvent event) throws ExecutionException {
			IWorkbenchPart part = HandlerUtil.getActivePartChecked(event);
			if (!(part instanceof ContentOutline)) {
				throw new ExecutionException("bogus part "
						+ part.getSite().getId());
			}
			return null;
		}

		@Override
		public void setEnabled(Object evaluationContext) {
			IWorkbenchPart part = (IWorkbenchPart) HandlerUtil.getVariable(
					evaluationContext, ISources.ACTIVE_PART_NAME);
			setBaseEnabled(part instanceof ContentOutline);
		}
	}

	public static final String C_PREFIX = "org.eclipse.ui.tests.contexts.";

	public static final String C1_ID = C_PREFIX + ISources.ACTIVE_CONTEXT_NAME;

	public static final String C2_ID = C_PREFIX
			+ ISources.ACTIVE_ACTION_SETS_NAME;

	public static final String C3_ID = C_PREFIX + ISources.ACTIVE_SHELL_NAME;

	public static final String C4_ID = C_PREFIX
			+ ISources.ACTIVE_WORKBENCH_WINDOW_SHELL_NAME;

	private static final String CATEGORY_ID = "org.eclipse.ui.category.window";

	public static final String CMD_ID = C_PREFIX + "command.testHandler";

	private static final String[][] CREATE_CONTEXTS = {
			{ C1_ID, "Active Contexts", null, IContextService.CONTEXT_ID_WINDOW },
			{ C2_ID, "Active Action Sets", null,
					IContextService.CONTEXT_ID_WINDOW },
			{ C3_ID, "Active Shell", null, IContextService.CONTEXT_ID_WINDOW },
			{ C4_ID, "Active Workbench Window Shell", null,
					IContextService.CONTEXT_ID_WINDOW },
			{ (C_PREFIX + ISources.ACTIVE_MENU_NAME),
					"Active Workbench Window Shell", null,
					IContextService.CONTEXT_ID_WINDOW }, };

	public static final String H1 = C_PREFIX + "h1";

	public static final String H2 = C_PREFIX + "h2";

	public static final String H3 = C_PREFIX + "h3";

	private final ICommandService commandService;

	private final IContextService contextService;

	private final IHandlerService handlerService;

	private final IServiceLocator services;

	private final Map<String, IContextActivation> testContextActivations = new HashMap<>();

	private final Map<Object, IHandlerActivation> testHandlerActivations = new HashMap<>();

	private final Map<String, IHandler> testHandlers = new HashMap<>();

	/**
	 * Constructor for <code>HandlerActivationTest</code>.
	 */
	public HandlerActivationTest() {
		super(HandlerActivationTest.class.getSimpleName());
		services = PlatformUI.getWorkbench();
		contextService = services
				.getService(IContextService.class);
		commandService = services
				.getService(ICommandService.class);
		handlerService = services
				.getService(IHandlerService.class);
	}

	private IContextActivation activateContext(String contextId) {
		IContextActivation c = contextService.activateContext(contextId);
		testContextActivations.put(contextId, c);
		return c;
	}

	private void assertHandlerIsExecuted(Command cmd, String handlerId)
			throws ExecutionException, NotDefinedException,
			NotEnabledException, NotHandledException {
		ActTestHandler handler = (ActTestHandler) testHandlers.get(handlerId);
		int count = handler.executionCount;
		cmd.executeWithChecks(handlerService.createExecutionEvent(cmd, null));
		assertEquals("The handler count should be incremented", count + 1,
				handler.executionCount);
	}

	private void createHandlerActivation(String contextId, String handlerId,
			String[] expressionInfo) {
		ActiveContextExpression expression;
		expression = new ActiveContextExpression(contextId, expressionInfo);
		makeHandler(handlerId, contextId, expression);
	}

	@Override
	protected void doSetUp() throws Exception {
		for (final String[] contextInfo : CREATE_CONTEXTS) {
			final Context context = contextService.getContext(contextInfo[0]);
			if (!context.isDefined()) {
				context.define(contextInfo[1], contextInfo[2], contextInfo[3]);
			}
		}

		Command cmd = commandService.getCommand(CMD_ID);
		if (!cmd.isDefined()) {
			Category cat = commandService.getCategory(CATEGORY_ID);
			cmd.define("Test Handler", "Test handler activation", cat);
			cmd.setHandler(HandlerServiceImpl.getHandler(CMD_ID));
		}

	}

	@Override
	protected void doTearDown() throws Exception {
		handlerService.deactivateHandlers(testHandlerActivations.values());
		testHandlerActivations.clear();
		contextService.deactivateContexts(testContextActivations.values());
		testContextActivations.clear();
		super.doTearDown();
	}

	private void doTestForBreak() throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		Command cmd = commandService.getCommand(CMD_ID);
		assertTrue("Command should be defined", cmd.isDefined());

		assertFalse("Should not be handled yet", cmd.isHandled());

		IContextActivation c1 = activateContext(C1_ID);
		IContextActivation c2 = activateContext(C2_ID);
		IContextActivation c3 = activateContext(C3_ID);

		assertTrue("Should still be handled", cmd.isHandled());

		assertHandlerIsExecuted(cmd, H3);

		contextService.deactivateContext(c3);
		contextService.deactivateContext(c2);
		contextService.deactivateContext(c1);
	}

	private void makeHandler(String handler, String context,
			ActiveContextExpression expression) {
		IHandler currentHandler = null;
		if (!testHandlers.containsKey(handler)) {
			currentHandler = new ActTestHandler(context);
			testHandlers.put(handler, currentHandler);
		} else {
			currentHandler = testHandlers.get(handler);
		}

		testHandlerActivations.put(handler, handlerService.activateHandler(
				CMD_ID, currentHandler, expression));
	}


	@Test
	public void testExceptionThrowingHandler(){
		assertThrows(ExecutionException.class,
				() -> handlerService.executeCommand("org.eclipse.ui.tests.command.handlerException", null));
	}


	@Test
	public void testBasicHandler() throws Exception {

		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });

		Command cmd = commandService.getCommand(CMD_ID);
		assertTrue("Command should be defined", cmd.isDefined());

		assertFalse("Should not be handled yet", cmd.isHandled());

		IContextActivation activationC1 = activateContext(C1_ID);
		assertTrue("Should definitely be handled", cmd.isHandled());

		ActTestHandler handler1 = (ActTestHandler) testHandlers.get(H1);
		int count = handler1.executionCount;
		cmd.executeWithChecks(handlerService.createExecutionEvent(cmd, null));
		assertEquals("The handler count should be correct", count + 1,
				handler1.executionCount);

		contextService.deactivateContext(activationC1);

		assertFalse("Should not be handled", cmd.isHandled());
	}

	@Test
	public void testForBreak123() throws Exception {
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		createHandlerActivation(C2_ID, H2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME,
						ISources.ACTIVE_ACTION_SETS_NAME });
		createHandlerActivation(C3_ID, H3, new String[] {
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_SHELL_NAME });

		doTestForBreak();
	}

	@Test
	public void testForBreak132() throws Exception {
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		createHandlerActivation(C3_ID, H3, new String[] {
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_SHELL_NAME });
		createHandlerActivation(C2_ID, H2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME,
						ISources.ACTIVE_ACTION_SETS_NAME });

		doTestForBreak();
	}

	@Test
	public void testForBreak213() throws Exception {
		createHandlerActivation(C2_ID, H2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME,
						ISources.ACTIVE_ACTION_SETS_NAME });
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		createHandlerActivation(C3_ID, H3, new String[] {
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_SHELL_NAME });

		doTestForBreak();
	}

	@Test
	public void testForBreak231() throws Exception {
		createHandlerActivation(C2_ID, H2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME,
						ISources.ACTIVE_ACTION_SETS_NAME });
		createHandlerActivation(C3_ID, H3, new String[] {
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_SHELL_NAME });
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });

		doTestForBreak();
	}

	@Test
	public void testForBreak312() throws Exception {
		createHandlerActivation(C3_ID, H3, new String[] {
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_SHELL_NAME });
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		createHandlerActivation(C2_ID, H2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME,
						ISources.ACTIVE_ACTION_SETS_NAME });

		doTestForBreak();
	}

	@Test
	public void testForBreak321() throws Exception {
		createHandlerActivation(C3_ID, H3, new String[] {
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_SHELL_NAME });
		createHandlerActivation(C2_ID, H2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME,
						ISources.ACTIVE_ACTION_SETS_NAME });
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });

		doTestForBreak();
	}

	@Test
	public void testForNegativeNumber() throws Exception {
		String c5_id = C_PREFIX + ISources.ACTIVE_MENU_NAME;
		String h5 = C_PREFIX + "h5";
		createHandlerActivation(c5_id, h5, new String[] {
				ISources.ACTIVE_CONTEXT_NAME, ISources.ACTIVE_MENU_NAME });
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });

		Command cmd = commandService.getCommand(CMD_ID);

		activateContext(C1_ID);
		activateContext(c5_id);

		assertHandlerIsExecuted(cmd, h5);
	}

	@Test
	public void testTwoHandlers() throws Exception {
		createHandlerActivation(C1_ID, H1,
				new String[] { ISources.ACTIVE_CONTEXT_NAME });
		createHandlerActivation(C2_ID, H2,
				new String[] { ISources.ACTIVE_CONTEXT_NAME,
						ISources.ACTIVE_ACTION_SETS_NAME });

		Command cmd = commandService.getCommand(CMD_ID);
		assertTrue("Command should be defined", cmd.isDefined());

		assertFalse("Should not be handled yet", cmd.isHandled());
		IContextActivation activationC1 = activateContext(C1_ID);
		assertTrue("Should definitely be handled", cmd.isHandled());

		ActTestHandler handler1 = (ActTestHandler) testHandlers.get(H1);
		int count1 = handler1.executionCount;
		cmd.executeWithChecks(new ExecutionEvent());
		assertEquals("The handler count should be incremented", count1 + 1,
				handler1.executionCount);

		activateContext(C2_ID);
		assertTrue("Should still be handled", cmd.isHandled());

		ActTestHandler handler2 = (ActTestHandler) testHandlers.get(H2);
		int count2 = handler2.executionCount;
		count1 = handler1.executionCount;
		cmd.executeWithChecks(new ExecutionEvent());
		assertEquals("The handler1 count should not be incremented", count1,
				handler1.executionCount);
		assertEquals("The handler2 count should be incremented", count2 + 1,
				handler2.executionCount);

		contextService.deactivateContext(activationC1);
		assertTrue("Will still be handled", cmd.isHandled());
	}

	@Test
	public void testLocalContext() throws Exception {
		IWorkbenchWindow window = openTestWindow("org.eclipse.ui.resourcePerspective");
		OutlineOnlyHandler handler = new OutlineOnlyHandler();
		IEvaluationContext oldContext = handlerService
				.createContextSnapshot(false);
		testHandlerActivations.put(handler, handlerService.activateHandler(
				CMD_ID, handler));
		Command cmd = commandService.getCommand(CMD_ID);
		ParameterizedCommand pcmd = new ParameterizedCommand(cmd, null);
		assertThrows(NotEnabledException.class, () -> handlerService.executeCommand(pcmd, null));
		assertFalse(cmd.isEnabled());
		window.getActivePage().showView(IPageLayout.ID_OUTLINE);
		IEvaluationContext outlineContext = handlerService.createContextSnapshot(false);
		handlerService.executeCommand(pcmd, null);
		assertTrue(cmd.isEnabled());

		assertThrows(NotEnabledException.class, () -> handlerService.executeCommandInContext(pcmd, null, oldContext));

		assertTrue(cmd.isEnabled());
		handlerService.executeCommandInContext(pcmd, null, outlineContext);
	}
}
