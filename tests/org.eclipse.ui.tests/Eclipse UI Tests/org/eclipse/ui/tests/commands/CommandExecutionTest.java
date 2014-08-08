/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import java.util.ArrayList;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.IExecutionListenerWithChecks;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.103
 * 
 */
public class CommandExecutionTest extends UITestCase {
	static class Pair {
		public Pair(String a, Object b) {
			key = a;
			result = b;
		}

		String key;
		Object result;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "(" + key + ",\n\t" + result + ")";
		}
	}

	private static class EL implements IExecutionListenerWithChecks {
		ArrayList<Pair> methods = new ArrayList<Pair>();
		IWorkbenchWindow wbw;

		public void preExecute(String commandId, ExecutionEvent event) {
			methods.add(new Pair("preExecute", event));
			// ensure HandlerUtil has proper access. See bug 412681.
			wbw = HandlerUtil.getActiveWorkbenchWindow(event);
		}

		public void postExecuteSuccess(String commandId, Object returnValue) {
			methods.add(new Pair("postExecuteSuccess", returnValue));
		}

		public void postExecuteFailure(String commandId,
				ExecutionException exception) {
			methods.add(new Pair("postExecuteFailure", exception));
		}

		public void notHandled(String commandId, NotHandledException exception) {
			methods.add(new Pair("notHandled", exception));
		}

		public void notEnabled(String commandId, NotEnabledException exception) {
			methods.add(new Pair("notEnabled", exception));
		}

		public void notDefined(String commandId, NotDefinedException exception) {
			methods.add(new Pair("notDefined", exception));
		}
	}

	/**
	 * @param testName
	 */
	public CommandExecutionTest(String testName) {
		super(testName);
	}

	private void compare(String[] calls, ArrayList<Pair> methods) {
		for (int i = 0; i < calls.length && i < methods.size(); i++) {
			assertEquals("call " + i, calls[i], methods.get(i).key);
		}
		assertEquals(calls.length, methods.size());
	}

	public void testCommandServiceExecute() throws Exception {
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		cmdService.addExecutionListener(listener);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		try {
			handlerService.executeCommand(
					IWorkbenchCommandConstants.FILE_CLOSE_OTHERS, null);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "notEnabled" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}

	public void testCommandExecute() throws Exception {
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		cmdService.addExecutionListener(listener);
		final Command cmd = cmdService
				.getCommand(IWorkbenchCommandConstants.FILE_CLOSE_OTHERS);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd,
				null);
		try {
			cmd.executeWithChecks(event);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "notEnabled" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}

	/**
	 * Verify that {@link IExecutionListener#preExecute(String, ExecutionEvent)} has
	 * received an event compatible with {@link HandlerUtil} methods.
	 * @param listener
	 */
	private void verifyHandlerUtilAccessDuringPreExecute(EL listener) {
		assertNotNull(
				"HandlerUtil.getActiveWorkbenchWindow() returned null during ICommandListener.preExecute().",
				listener.wbw);
	}
	
	public void testCommandListenerExecute() throws Exception {
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		final Command cmd = cmdService
				.getCommand(IWorkbenchCommandConstants.FILE_CLOSE_OTHERS);
		cmd.addExecutionListener(listener);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd,
				null);
		try {
			cmd.executeWithChecks(event);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "notEnabled" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}

	public void testCommandServiceExecuteRefresh() throws Exception {
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		cmdService.addExecutionListener(listener);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		try {
			handlerService.executeCommand(
					IWorkbenchCommandConstants.FILE_REFRESH, null);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "notHandled" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}

	public void testCommandExecuteRefresh() throws Exception {
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		cmdService.addExecutionListener(listener);
		final Command cmd = cmdService
				.getCommand(IWorkbenchCommandConstants.FILE_REFRESH);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd,
				null);
		try {
			cmd.executeWithChecks(event);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "notHandled" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}
	
	public void testCommandListenerExecuteRefresh() throws Exception {
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		final Command cmd = cmdService
				.getCommand(IWorkbenchCommandConstants.FILE_REFRESH);
		cmd.addExecutionListener(listener);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd,
				null);
		try {
			cmd.executeWithChecks(event);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "notHandled" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}

	public void testCommandServiceExecuteClosePart() throws Exception {
		getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(IPageLayout.ID_PROGRESS_VIEW);
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		cmdService.addExecutionListener(listener);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		try {
			handlerService.executeCommand(
					IWorkbenchCommandConstants.WINDOW_CLOSE_PART, null);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "postExecuteSuccess" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}

	public void testCommandExecuteClosePart() throws Exception {
		getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(IPageLayout.ID_PROGRESS_VIEW);
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		cmdService.addExecutionListener(listener);
		final Command cmd = cmdService
				.getCommand(IWorkbenchCommandConstants.WINDOW_CLOSE_PART);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd,
				null);
		try {
			cmd.executeWithChecks(event);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "postExecuteSuccess" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}
	
	public void testCommandListenerExecuteClosePart() throws Exception {
		getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(IPageLayout.ID_PROGRESS_VIEW);
		EL listener = new EL();
		ICommandService cmdService = getWorkbench()
				.getService(ICommandService.class);
		final Command cmd = cmdService
				.getCommand(IWorkbenchCommandConstants.WINDOW_CLOSE_PART);
		cmd.addExecutionListener(listener);
		IHandlerService handlerService = getWorkbench()
				.getService(IHandlerService.class);
		final ExecutionEvent event = handlerService.createExecutionEvent(cmd,
				null);
		try {
			cmd.executeWithChecks(event);
		} catch (Exception e) {
			// do nothing
		}
		System.out.println(listener.methods);
		String[] calls = { "preExecute", "postExecuteSuccess" };
		compare(calls, listener.methods);
		verifyHandlerUtilAccessDuringPreExecute(listener);
	}
}
