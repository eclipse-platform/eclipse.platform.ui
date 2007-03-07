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
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.IServiceScopes;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.3
 * 
 */
public class CommandCallbackTest extends UITestCase {

	/**
	 * 
	 */
	private static final String HOST_PARAM_ID = "host";
	/**
	 * 
	 */
	private static final String PROT_PARAM_ID = "protocol";
	/**
	 * 
	 */
	private static final String PREFIX = "tests.commands.CCT.";
	private static final String CMD1_ID = PREFIX + "cmd1";
	private static final String CMD2_ID = PREFIX + "cmd2";

	private ICommandService commandService;
	private Command cmd1;
	private Command cmd2;
	private IHandlerService handlerService;
	private IHandlerActivation cmd1Activation;
	private IHandlerActivation cmd2Activation;
	private CallbackHandler cmd1Handler;
	private CallbackHandler cmd2Handler;

	/**
	 * @param testName
	 */
	public CommandCallbackTest(String testName) {
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
		cmd1 = commandService.getCommand(CMD1_ID);
		cmd2 = commandService.getCommand(CMD2_ID);
		handlerService = (IHandlerService) fWorkbench
				.getService(IHandlerService.class);
		cmd1Handler = new CallbackHandler();
		cmd1Activation = handlerService.activateHandler(CMD1_ID, cmd1Handler);
		cmd2Handler = new CallbackHandler();
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

	private static class CallbackHandler extends AbstractHandler implements
			IElementUpdater {
		public int callbacks = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.commands.ICallbackUpdater#updateCallback(org.eclipse.core.runtime.IAdaptable,
		 *      java.util.Map)
		 */
		public void updateElement(UIElement callback, Map parameters) {
			callbacks++;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
		 */
		public Object execute(ExecutionEvent event) throws ExecutionException {
			return null;
		}
	}

	private static class MyElement extends UIElement {
		
		/**
		 * 
		 */
		public MyElement(IServiceLocator locator) {
			super(locator);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.menus.UIElement#setChecked(boolean)
		 */
		public void setChecked(boolean checked) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.menus.UIElement#setDisabledIcon(org.eclipse.jface.resource.ImageDescriptor)
		 */
		public void setDisabledIcon(ImageDescriptor desc) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.menus.UIElement#setHoverIcon(org.eclipse.jface.resource.ImageDescriptor)
		 */
		public void setHoverIcon(ImageDescriptor desc) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.menus.UIElement#setIcon(org.eclipse.jface.resource.ImageDescriptor)
		 */
		public void setIcon(ImageDescriptor desc) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.menus.UIElement#setText(java.lang.String)
		 */
		public void setText(String text) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.menus.UIElement#setTooltip(java.lang.String)
		 */
		public void setTooltip(String text) {
			// TODO Auto-generated method stub

		}

	}

	public void testNoParametersNoCallbacks() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null);
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null);
		try {
			commandService.registerElementForCommand(pc1, null);
			fail("Callback should not register");
		} catch (NotDefinedException e) {
		}
		try {
			commandService.registerElementForCommand(pc2, null);
			fail("Callback 2 should not register");
		} catch (NotDefinedException e) {
		}

		commandService.refreshElements(CMD1_ID + ".1", null);
		assertEquals(0, cmd1Handler.callbacks);

		commandService.refreshElements(CMD1_ID, null);
		assertEquals(0, cmd1Handler.callbacks);
	}

	public void testNoParametersWithCallbacks() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null);
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null);

		IElementReference cr1 = commandService.registerElementForCommand(pc1,
				new MyElement(fWorkbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(fWorkbench));

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
				new MyElement(fWorkbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(fWorkbench));
		try {

			assertEquals(2, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			Map filter = new HashMap();
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
				new MyElement(fWorkbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(fWorkbench));
		IElementReference cr3 = commandService.registerElementForCommand(pc3,
				new MyElement(fWorkbench));
		try {

			assertEquals(3, cmd2Handler.callbacks);

			cmd2Handler.callbacks = 0;
			Map filter = new HashMap();
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
				new MyElement(fWorkbench));
		IElementReference cr2 = commandService.registerElementForCommand(pc2,
				new MyElement(fWorkbench));
		IElementReference cr3 = commandService.registerElementForCommand(pc3,
				new MyElement(fWorkbench));
		IElementReference cr4 = commandService.registerElementForCommand(pc4,
				new MyElement(fWorkbench));
		IElementReference cr5 = commandService.registerElementForCommand(pc5,
				new MyElement(fWorkbench));
		try {
			assertEquals(5, cmd2Handler.callbacks);
			Map filter = new HashMap();

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

	public void testCommandThroughWindow() throws Exception {
		IWorkbenchWindow window = openTestWindow();
		ICommandService cs = (ICommandService) window
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

			Map filter = new HashMap();
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

	public void testCallbackCleanup() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null);
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null);

		IWorkbenchWindow window = openTestWindow();
		ICommandService cs = (ICommandService) window
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
