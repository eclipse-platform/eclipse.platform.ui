/*******************************************************************************
 * Copyright (c) 2014, 2017 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433603
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import static org.eclipse.ui.tests.harness.util.UITestUtil.openTestWindow;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.tests.harness.util.UITestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @since 3.3
 */
@RunWith(JUnit4.class)
public class CommandCallbackTest extends UITestCase {

	private static final String HOST_PARAM_ID = "host";
	private static final String PROT_PARAM_ID = "protocol";
	private static final String PREFIX = "tests.commands.CCT.";
	private static final String CMD1_ID = PREFIX + "cmd1";
	private static final String CMD2_ID = PREFIX + "cmd2";

	private IWorkbench workbench;
	private ICommandService commandService;
	private Command cmd1;
	private Command cmd2;
	private IHandlerService handlerService;
	private IHandlerActivation cmd1Activation;
	private IHandlerActivation cmd2Activation;
	private CallbackHandler cmd1Handler;
	private CallbackHandler cmd2Handler;

	public CommandCallbackTest() {
		super(CommandCallbackTest.class.getSimpleName());
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		workbench = PlatformUI.getWorkbench();
		commandService = workbench.getService(ICommandService.class);
		cmd1 = commandService.getCommand(CMD1_ID);
		cmd2 = commandService.getCommand(CMD2_ID);
		handlerService = workbench.getService(IHandlerService.class);
		cmd1Handler = new CallbackHandler();
		cmd1Activation = handlerService.activateHandler(CMD1_ID, cmd1Handler);
		cmd2Handler = new CallbackHandler();
		cmd2Activation = handlerService.activateHandler(CMD2_ID, cmd2Handler);
	}

	@Override
	protected void doTearDown() throws Exception {
		if (cmd1Activation != null) {
			handlerService.deactivateHandler(cmd1Activation);
			cmd1Activation = null;
		}
		if (cmd2Activation != null) {
			handlerService.deactivateHandler(cmd2Activation);
			cmd2Activation = null;
		}
		workbench = null;
		super.doTearDown();
	}

	private static class CallbackHandler extends AbstractHandler implements
			IElementUpdater {
		public int callbacks = 0;

		@Override
		public void updateElement(UIElement callback, Map parameters) {
			callbacks++;
		}

		@Override
		public Object execute(ExecutionEvent event) {
			return null;
		}
	}

	private static class MyElement extends UIElement {

		public MyElement(IServiceLocator locator) {
			super(locator);
		}

		@Override
		public void setChecked(boolean checked) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setDisabledIcon(ImageDescriptor desc) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setHoverIcon(ImageDescriptor desc) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setIcon(ImageDescriptor desc) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setText(String text) {
			// TODO Auto-generated method stub

		}

		@Override
		public void setTooltip(String text) {
			// TODO Auto-generated method stub

		}

	}

	@Test
	public void testNoParametersNoCallbacks() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null);
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null);
		assertThrows(NotDefinedException.class, () -> commandService.registerElementForCommand(pc1, null));
		assertThrows(NotDefinedException.class, () -> commandService.registerElementForCommand(pc2, null));

		commandService.refreshElements(CMD1_ID + ".1", null);
		assertEquals(0, cmd1Handler.callbacks);

		commandService.refreshElements(CMD1_ID, null);
		assertEquals(0, cmd1Handler.callbacks);
	}

	@Test
	public void testNoParametersWithCallbacks() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null);
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null);

		IElementReference cr1 = commandService.registerElementForCommand(pc1,
				new MyElement(workbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(workbench));

		try {
			assertEquals(2, cmd1Handler.callbacks);
			cmd1Handler.callbacks = 0;
			commandService.refreshElements(CMD1_ID, null);
			assertEquals(2, cmd1Handler.callbacks);
		} finally {
			commandService.unregisterElement(cr1);
			commandService.unregisterElement(cr2);
		}

		cmd1Handler.callbacks = 0;
		commandService.refreshElements(CMD1_ID, null);
		assertEquals(0, cmd1Handler.callbacks);
	}

	@Test
	public void testParametersWithCallbacks() throws Exception {
		IParameter parmProt = cmd2.getParameter(PROT_PARAM_ID);
		IParameter parmHost = cmd2.getParameter(HOST_PARAM_ID);
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "www.eclipse.org") });
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") });
		IElementReference cr1 = commandService.registerElementForCommand(pc1,
				new MyElement(workbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(workbench));
		try {

			assertEquals(2, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			Map<String, String> filter = new HashMap<>();
			filter.put(PROT_PARAM_ID, "http");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(2, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.put(HOST_PARAM_ID, "www.eclipse.org");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(1, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.remove(PROT_PARAM_ID);
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(1, cmd2Handler.callbacks);

		} finally {
			commandService.unregisterElement(cr1);
			commandService.unregisterElement(cr2);
		}
	}

	@Test
	public void testParmsToSameCommand() throws Exception {
		IParameter parmProt = cmd2.getParameter(PROT_PARAM_ID);
		IParameter parmHost = cmd2.getParameter(HOST_PARAM_ID);
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "www.eclipse.org") });
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") });
		ParameterizedCommand pc3 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") });
		IElementReference cr1 = commandService.registerElementForCommand(pc1,
				new MyElement(workbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(workbench));
		IElementReference cr3 = commandService.registerElementForCommand(pc3,
				new MyElement(workbench));
		try {

			assertEquals(3, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			Map<String, String> filter = new HashMap<>();
			filter.put(PROT_PARAM_ID, "http");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(3, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.put(HOST_PARAM_ID, "www.eclipse.org");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(1, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.remove(PROT_PARAM_ID);
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(1, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.put(HOST_PARAM_ID, "download.eclipse.org");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(2, cmd2Handler.callbacks);
		} finally {
			commandService.unregisterElement(cr1);
			commandService.unregisterElement(cr2);
			commandService.unregisterElement(cr3);
		}
	}

	@Test
	public void testParmsToDifferentProtocol() throws Exception {
		IParameter parmProt = cmd2.getParameter(PROT_PARAM_ID);
		IParameter parmHost = cmd2.getParameter(HOST_PARAM_ID);
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "www.eclipse.org") });
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") });
		ParameterizedCommand pc3 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") });
		ParameterizedCommand pc4 = new ParameterizedCommand(cmd2,
				new Parameterization[] { new Parameterization(parmProt, "ftp"),
						new Parameterization(parmHost, "www.eclipse.org") });
		ParameterizedCommand pc5 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] { new Parameterization(parmProt, "ftp"),
						new Parameterization(parmHost, "download.eclipse.org") });
		IElementReference cr1 = commandService.registerElementForCommand(pc1,
				new MyElement(workbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(workbench));
		IElementReference cr3 = commandService.registerElementForCommand(pc3,
				new MyElement(workbench));
		IElementReference cr4 = commandService.registerElementForCommand(pc4,
				new MyElement(workbench));
		IElementReference cr5 = commandService.registerElementForCommand(pc5,
				new MyElement(workbench));
		try {
			assertEquals(5, cmd2Handler.callbacks);
			Map<String, String> filter = new HashMap<>();

			cmd2Handler.callbacks = 0;
			filter.put(PROT_PARAM_ID, "http");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(3, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.put(PROT_PARAM_ID, "ftp");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(2, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.put(HOST_PARAM_ID, "download.eclipse.org");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(1, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.remove(PROT_PARAM_ID);
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(3, cmd2Handler.callbacks);

		} finally {
			commandService.unregisterElement(cr1);
			commandService.unregisterElement(cr2);
			commandService.unregisterElement(cr3);
			commandService.unregisterElement(cr4);
			commandService.unregisterElement(cr5);
		}
	}

	@Test
	public void testCommandThroughWindow() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		ICommandService cs = window
				.getService(ICommandService.class);
		IParameter parmProt = cmd2.getParameter(PROT_PARAM_ID);
		IParameter parmHost = cmd2.getParameter(HOST_PARAM_ID);
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "www.eclipse.org") });
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") });
		IElementReference cr1 = commandService.registerElementForCommand(pc1,
				new MyElement(window));
		// should be removed when the window goes away
		cs.registerElementForCommand(pc2, new MyElement(window));
		try {
			assertEquals(2, cmd2Handler.callbacks);

			Map<String, Object> filter = new HashMap<>();
			cmd2Handler.callbacks = 0;
			filter.put(PROT_PARAM_ID, "http");
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(2, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			filter.put(IServiceScopes.WINDOW_SCOPE, window);
			commandService.refreshElements(CMD2_ID, filter);
			assertEquals(1, cmd2Handler.callbacks);
		} finally {
			commandService.unregisterElement(cr1);
		}
	}

	@Test
	public void testCallbackCleanup() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null);
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null);

		IWorkbenchWindow window = openTestWindow();
		ICommandService cs = window
				.getService(ICommandService.class);

		IElementReference cr1 = commandService.registerElementForCommand(pc1,
				new MyElement(window));
		// should be removed when the window goes away
		cs.registerElementForCommand(pc2, new MyElement(window));

		try {
			assertEquals(2, cmd1Handler.callbacks);
			cmd1Handler.callbacks = 0;
			closeAllTestWindows();
			commandService.refreshElements(CMD1_ID, null);
			assertEquals(1, cmd1Handler.callbacks);
		} finally {
			commandService.unregisterElement(cr1);
		}
	}
}
