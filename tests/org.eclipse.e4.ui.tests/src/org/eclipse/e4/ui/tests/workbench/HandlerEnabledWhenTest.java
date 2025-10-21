/*******************************************************************************
 * Copyright (c) 2025 Eclipse Platform and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eclipse Platform - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.commands.Category;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.CommandServiceAddon;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for handler enabledWhen core expression support.
 *
 * @since 1.4
 */
public class HandlerEnabledWhenTest {
	private static final String TEST_COMMAND_ID = "test.command.enabledwhen";
	private IEclipseContext appContext;

	public static class TestHandler {
		boolean ran = false;
		boolean canRun = true;

		@CanExecute
		public boolean canExecute() {
			return canRun;
		}

		@Execute
		public Object execute() {
			ran = true;
			return "executed";
		}
	}

	/**
	 * A simple expression that always returns true.
	 */
	public static class TrueExpression extends Expression {
		@Override
		public EvaluationResult evaluate(IEvaluationContext context) {
			return EvaluationResult.TRUE;
		}
	}

	/**
	 * A simple expression that always returns false.
	 */
	public static class FalseExpression extends Expression {
		@Override
		public EvaluationResult evaluate(IEvaluationContext context) {
			return EvaluationResult.FALSE;
		}
	}

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		ContextInjectionFactory.make(CommandServiceAddon.class, appContext);
		defineCommand(appContext);
	}

	@After
	public void tearDown() throws Exception {
		appContext.dispose();
	}

	/**
	 * Tests that when no enabledWhen expression is set, the handler's @CanExecute
	 * is used as normal.
	 */
	@Test
	public void testNoEnabledWhen() throws Exception {
		ParameterizedCommand cmd = getCommand(appContext, TEST_COMMAND_ID);
		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler();
		service.activateHandler(TEST_COMMAND_ID, handler);

		// Should be enabled based on @CanExecute
		assertTrue(service.canExecute(cmd));
		assertEquals("executed", service.executeHandler(cmd));
		assertTrue(handler.ran);

		// Now disable via @CanExecute
		handler.canRun = false;
		assertFalse(service.canExecute(cmd));
	}

	/**
	 * Tests that when an enabledWhen expression is set to true, and @CanExecute
	 * returns true, the handler is enabled.
	 */
	@Test
	public void testEnabledWhenTrue() throws Exception {
		ParameterizedCommand cmd = getCommand(appContext, TEST_COMMAND_ID);
		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler();

		// Create handler model with enabledWhen expression
		MHandler handlerModel = createHandlerWithExpression(handler, new TrueExpression());

		// Use the wrapper created by HandlerProcessingAddon simulation
		org.eclipse.e4.ui.internal.workbench.addons.HandlerEnabledWhenWrapper wrapper = 
				new org.eclipse.e4.ui.internal.workbench.addons.HandlerEnabledWhenWrapper(handler, handlerModel);
		service.activateHandler(TEST_COMMAND_ID, wrapper);

		// Expression is true, @CanExecute is true => enabled
		assertTrue(service.canExecute(cmd));
		assertEquals("executed", service.executeHandler(cmd));
		assertTrue(handler.ran);
	}

	/**
	 * Tests that when an enabledWhen expression is set to false, the handler is
	 * disabled regardless of @CanExecute.
	 */
	@Test
	public void testEnabledWhenFalse() throws Exception {
		ParameterizedCommand cmd = getCommand(appContext, TEST_COMMAND_ID);
		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler();

		// Create handler model with enabledWhen expression that returns false
		MHandler handlerModel = createHandlerWithExpression(handler, new FalseExpression());

		// Use the wrapper
		org.eclipse.e4.ui.internal.workbench.addons.HandlerEnabledWhenWrapper wrapper = 
				new org.eclipse.e4.ui.internal.workbench.addons.HandlerEnabledWhenWrapper(handler, handlerModel);
		service.activateHandler(TEST_COMMAND_ID, wrapper);

		// Expression is false => disabled (even though @CanExecute would return true)
		assertFalse(service.canExecute(cmd));
		
		// Handler should not execute when disabled
		assertNull(service.executeHandler(cmd));
		assertFalse(handler.ran);
	}

	/**
	 * Tests that enabledWhen expression takes precedence over @CanExecute.
	 */
	@Test
	public void testEnabledWhenPrecedence() throws Exception {
		ParameterizedCommand cmd = getCommand(appContext, TEST_COMMAND_ID);
		EHandlerService service = appContext.get(EHandlerService.class);
		TestHandler handler = new TestHandler();
		handler.canRun = false; // @CanExecute would return false

		// Create handler model with enabledWhen expression that returns true
		MHandler handlerModel = createHandlerWithExpression(handler, new TrueExpression());

		// Use the wrapper
		org.eclipse.e4.ui.internal.workbench.addons.HandlerEnabledWhenWrapper wrapper = 
				new org.eclipse.e4.ui.internal.workbench.addons.HandlerEnabledWhenWrapper(handler, handlerModel);
		service.activateHandler(TEST_COMMAND_ID, wrapper);

		// Expression is true, but @CanExecute is false
		// The @CanExecute should still be consulted after expression passes
		assertFalse(service.canExecute(cmd));
		
		// Now enable via @CanExecute
		handler.canRun = true;
		assertTrue(service.canExecute(cmd));
	}

	private ParameterizedCommand getCommand(IEclipseContext context, String commandId) {
		ECommandService cs = context.get(ECommandService.class);
		Command cmd = cs.getCommand(commandId);
		return new ParameterizedCommand(cmd, null);
	}

	private void defineCommand(IEclipseContext context) {
		ECommandService cmdService = context.get(ECommandService.class);
		Category category = cmdService.defineCategory("test.category", "Test Category", null);
		cmdService.defineCommand(TEST_COMMAND_ID, "Test Command", null, category, null);
	}

	private MHandler createHandlerWithExpression(Object handler, Expression expression) {
		MHandler handlerModel = CommandsFactoryImpl.eINSTANCE.createHandler();
		MCommand command = CommandsFactoryImpl.eINSTANCE.createCommand();
		command.setElementId(TEST_COMMAND_ID);
		handlerModel.setCommand(command);
		handlerModel.setObject(handler);

		MCoreExpression coreExpression = UiFactoryImpl.eINSTANCE.createCoreExpression();
		coreExpression.setCoreExpression(expression);
		handlerModel.setEnabledWhen(coreExpression);

		return handlerModel;
	}
}
