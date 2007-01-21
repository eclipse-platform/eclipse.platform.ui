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

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICallbackReference;
import org.eclipse.ui.commands.ICommandService;
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
	}

	public void testNoParametersNoCallbacks() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null);
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null);
		try {
			commandService.registerCallbackForCommand(pc1);
			fail("Callback should not register");
		} catch (NotDefinedException e) {
		}
		try {
			commandService.registerCallbackForCommand(pc2);
			fail("Callback 2 should not register");
		} catch (NotDefinedException e) {
		}

		IAdaptable[] callbacks = commandService.findCallbacks(CMD1_ID + ".1",
				null);
		assertEquals(0, callbacks.length);

		callbacks = commandService.findCallbacks(CMD1_ID, null);
		assertEquals(0, callbacks.length);
	}

	public void testNoParametersWithCallbacks() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null,
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null,
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});

		ICallbackReference cr1 = commandService.registerCallbackForCommand(pc1);
		ICallbackReference cr2 = commandService.registerCallbackForCommand(pc2);

		try {
			IAdaptable[] callbacks = commandService
					.findCallbacks(CMD1_ID, null);
			assertEquals(2, callbacks.length);
		} finally {
			commandService.unregisterCallback(cr1);
			commandService.unregisterCallback(cr2);
		}

		assertEquals(0, commandService.findCallbacks(CMD1_ID, null).length);
	}

	public void testParametersWithCallbacks() throws Exception {
		IParameter parmProt = cmd2.getParameter(PROT_PARAM_ID);
		IParameter parmHost = cmd2.getParameter(HOST_PARAM_ID);
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "www.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ICallbackReference cr1 = commandService.registerCallbackForCommand(pc1);
		ICallbackReference cr2 = commandService.registerCallbackForCommand(pc2);
		try {

			assertEquals(2, commandService.findCallbacks(CMD2_ID, null).length);
			Map filter = new HashMap();
			filter.put(PROT_PARAM_ID, "http");
			assertEquals(2,
					commandService.findCallbacks(CMD2_ID, filter).length);
			filter.put(HOST_PARAM_ID, "www.eclipse.org");
			assertEquals(1,
					commandService.findCallbacks(CMD2_ID, filter).length);

			filter.remove(PROT_PARAM_ID);
			assertEquals(1,
					commandService.findCallbacks(CMD2_ID, filter).length);
		} finally {
			commandService.unregisterCallback(cr1);
			commandService.unregisterCallback(cr2);
		}
	}

	public void testParmsToSameCommand() throws Exception {
		IParameter parmProt = cmd2.getParameter(PROT_PARAM_ID);
		IParameter parmHost = cmd2.getParameter(HOST_PARAM_ID);
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "www.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc3 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ICallbackReference cr1 = commandService.registerCallbackForCommand(pc1);
		ICallbackReference cr2 = commandService.registerCallbackForCommand(pc2);
		ICallbackReference cr3 = commandService.registerCallbackForCommand(pc3);
		try {

			assertEquals(3, commandService.findCallbacks(CMD2_ID, null).length);
			Map filter = new HashMap();
			filter.put(PROT_PARAM_ID, "http");
			assertEquals(3,
					commandService.findCallbacks(CMD2_ID, filter).length);
			filter.put(HOST_PARAM_ID, "www.eclipse.org");
			assertEquals(1,
					commandService.findCallbacks(CMD2_ID, filter).length);

			filter.remove(PROT_PARAM_ID);
			assertEquals(1,
					commandService.findCallbacks(CMD2_ID, filter).length);

			filter.put(HOST_PARAM_ID, "download.eclipse.org");
			assertEquals(2,
					commandService.findCallbacks(CMD2_ID, filter).length);

		} finally {
			commandService.unregisterCallback(cr1);
			commandService.unregisterCallback(cr2);
			commandService.unregisterCallback(cr3);
		}
	}

	public void testParmsToDifferentProtocol() throws Exception {
		IParameter parmProt = cmd2.getParameter(PROT_PARAM_ID);
		IParameter parmHost = cmd2.getParameter(HOST_PARAM_ID);
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "www.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc3 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc4 = new ParameterizedCommand(cmd2,
				new Parameterization[] { new Parameterization(parmProt, "ftp"),
						new Parameterization(parmHost, "www.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc5 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] { new Parameterization(parmProt, "ftp"),
						new Parameterization(parmHost, "download.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ICallbackReference cr1 = commandService.registerCallbackForCommand(pc1);
		ICallbackReference cr2 = commandService.registerCallbackForCommand(pc2);
		ICallbackReference cr3 = commandService.registerCallbackForCommand(pc3);
		ICallbackReference cr4 = commandService.registerCallbackForCommand(pc4);
		ICallbackReference cr5 = commandService.registerCallbackForCommand(pc5);
		try {

			assertEquals(5, commandService.findCallbacks(CMD2_ID, null).length);
			Map filter = new HashMap();
			filter.put(PROT_PARAM_ID, "http");
			assertEquals(3,
					commandService.findCallbacks(CMD2_ID, filter).length);
			filter.put(PROT_PARAM_ID, "ftp");
			assertEquals(2,
					commandService.findCallbacks(CMD2_ID, filter).length);
			filter.put(HOST_PARAM_ID, "download.eclipse.org");
			assertEquals(1,
					commandService.findCallbacks(CMD2_ID, filter).length);
			filter.remove(PROT_PARAM_ID);
			assertEquals(3,
					commandService.findCallbacks(CMD2_ID, filter).length);

		} finally {
			commandService.unregisterCallback(cr1);
			commandService.unregisterCallback(cr2);
			commandService.unregisterCallback(cr3);
			commandService.unregisterCallback(cr4);
			commandService.unregisterCallback(cr5);
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
						new Parameterization(parmHost, "www.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc2 = new ParameterizedCommand(
				cmd2,
				new Parameterization[] {
						new Parameterization(parmProt, "http"),
						new Parameterization(parmHost, "download.eclipse.org") },
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ICallbackReference cr1 = commandService.registerCallbackForCommand(pc1);
		// should be removed when the window goes away
		cs.registerCallbackForCommand(pc2);
		try {
			Map filter = new HashMap();
			filter.put(PROT_PARAM_ID, "http");
			assertEquals(2,
					commandService.findCallbacks(CMD2_ID, filter).length);
			filter.put(IServiceScopes.WINDOW_SCOPE, window);
			assertEquals(1,
					commandService.findCallbacks(CMD2_ID, filter).length);
		} finally {
			commandService.unregisterCallback(cr1);
		}
	}

	public void testCallbackCleanup() throws Exception {
		ParameterizedCommand pc1 = new ParameterizedCommand(cmd1, null,
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});
		ParameterizedCommand pc2 = new ParameterizedCommand(cmd1, null,
				new IAdaptable() {
					public Object getAdapter(Class adapter) {
						return null;
					}
				});

		IWorkbenchWindow window = openTestWindow();
		ICommandService cs = (ICommandService) window
				.getService(ICommandService.class);

		ICallbackReference cr1 = commandService.registerCallbackForCommand(pc1);
		// should be removed when the window goes away
		cs.registerCallbackForCommand(pc2);

		try {
			assertEquals(2, commandService.findCallbacks(CMD1_ID, null).length);
			closeAllTestWindows();
			assertEquals(1, commandService.findCallbacks(CMD1_ID, null).length);
		} finally {
			commandService.unregisterCallback(cr1);
		}
	}
}
