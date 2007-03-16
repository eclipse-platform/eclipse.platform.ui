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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.actions.CommandAction;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class CommandActionTest extends UITestCase {
	// you can find these commands in org.eclipse.ui.tests/plugin.xml
	private static final String PREFIX = "tests.commands.CCT.";
	private static final String CMD1_ID = PREFIX + "cmd1";
	private static final String CMD2_ID = PREFIX + "cmd2";

	private IHandlerService handlerService;
	private VerifyHandler cmd1Handler;
	private IHandlerActivation cmd1Activation;
	private VerifyHandler2 cmd2Handler;
	private IHandlerActivation cmd2Activation;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		handlerService = (IHandlerService) fWorkbench
				.getService(IHandlerService.class);

		cmd1Handler = new VerifyHandler();
		cmd1Activation = handlerService.activateHandler(CMD1_ID, cmd1Handler);
		cmd2Handler = new VerifyHandler2();
		cmd2Activation = handlerService.activateHandler(CMD2_ID, cmd2Handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.harness.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		if (cmd1Activation != null) {
			handlerService.deactivateHandler(cmd1Activation);
			cmd1Activation = null;
		}
		if (cmd2Activation != null) {
			handlerService.deactivateHandler(cmd2Activation);
			cmd2Activation = null;
		}
		super.doTearDown();
	}

	private static class VerifyHandler2 extends AbstractHandler {
		public int count = 0;
		public String paramValue1 = null;
		public String paramValue2 = null;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */

		public Object execute(ExecutionEvent event) throws ExecutionException {

			paramValue1 = event.getParameter("protocol");
			paramValue2 = event.getParameter("host");
			count++;
			return null;
		}
	}

	private static class VerifyHandler extends AbstractHandler {
		public int count = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {

			count++;
			return null;
		}
	}

	/**
	 * @param testName
	 */
	public CommandActionTest(String testName) {
		super(testName);
	}

	public void testCommandId() throws Exception {

		// create a command action for CMD1_ID, which takes no parameters.
		CommandAction action1 = new CommandAction(PlatformUI
				.getWorkbench(), CMD1_ID);
		assertEquals(0, cmd1Handler.count);
		action1.run();
		assertEquals(1, cmd1Handler.count);
	}

	public void testParameterizedCommand() throws Exception {

		// create a command action for CMD2_id which takes parameters.
		// make sure the handler gets called with the correct parameters.
		assertEquals(0, cmd2Handler.count);
		assertNull(cmd2Handler.paramValue1);
		assertNull(cmd2Handler.paramValue2);

		Map map = new HashMap();
		map.put("protocol", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("host", "true");

		CommandAction action2 = new CommandAction(PlatformUI
				.getWorkbench(), CMD2_ID, map);//$NON-NLS-1$
		action2.run();
		assertEquals(1, cmd2Handler.count);
		assertNotNull(cmd2Handler.paramValue1);
		assertEquals("true", cmd2Handler.paramValue1);
		assertNotNull(cmd2Handler.paramValue2);
		assertEquals("true", cmd2Handler.paramValue2);

		map = new HashMap();
		map.put("protocol", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("host", "false");

		action2 = new CommandAction(PlatformUI.getWorkbench(), CMD2_ID, map);//$NON-NLS-1$
		action2.run();
		assertEquals(2, cmd2Handler.count);
		assertNotNull(cmd2Handler.paramValue1);
		assertEquals("false", cmd2Handler.paramValue1);
		assertNotNull(cmd2Handler.paramValue2);
		assertEquals("false", cmd2Handler.paramValue2);
	}
}
