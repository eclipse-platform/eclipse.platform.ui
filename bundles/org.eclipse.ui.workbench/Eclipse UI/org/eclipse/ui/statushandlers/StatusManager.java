/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.statushandlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchErrorHandlerProxy;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.statushandlers.StatusHandlerRegistry;

/**
 * <p>
 * Status manager is responsible for handling statuses.
 * </p>
 * 
 * <p>
 * Handlers shoudn't be used directly but through the StatusManager singleton
 * which keeps the status handling policy and chooses handlers due to it.
 * <code>StatusManager.getManager().handle(IStatus)</code> and
 * <code>handle(IStatus status, int style)</code> methods are used for passing
 * all problems to the facility.
 * </p>
 * 
 * <p>
 * Styles
 * <ul>
 * <li>NONE - nothing should be done with the status</li>
 * <li>LOG - the status should be logged</li>
 * <li>SHOW - the status should be shown to an user</li>
 * <li>BLOCK - the status handling should block until is finished</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Default policy (steps):
 * <ul>
 * <li>manager tries to handle the status with a default handler</li>
 * <li>manager tries to find a right handler for the status</li>
 * <li>manager delegates the status to workbench handler</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Each status handler defined in "statusHandlers" extension can have package
 * prefix assigned to it. During step 2 status manager is looking for the most
 * specific handler for given status checking status pluginId against these
 * prefixes. The default handler is not used in this step.
 * </p>
 * 
 * <p>
 * The default handler can be set for product using
 * "statusHandlerProductBinding" element in "statusHandlers" extension.
 * </p>
 * 
 * <p>
 * Workbench handler passes handling to handler assigned to the workbench
 * advisor. This handler doesn't have to be added as "statusHandlers" extension.
 * </p>
 * 
 * @since 3.3
 */
public class StatusManager {
	/**
	 * A style indicating that the status should not be acted on. This is used
	 * by objects such as log listeners that do not want to report a status twice.
	 */
	public static final int NONE = 0;

	/**
	 * A style indicating that the status should be logged only.
	 */
	public static final int LOG = 0x01;

	/**
	 * A style indicating that handlers should show a problem to an user without
	 * blocking the calling method while awaiting user response. This is generally 
	 * done using a non modal {@link Dialog}.
	 */
	public static final int SHOW = 0x02;
	
	/**
	 * A style indicating that the handling should block the calling method until the
	 * user has responded. This is generally done using a modal window such as a 
	 * {@link Dialog}.
	 */
	public static final int BLOCK = 0x04;

	private static StatusManager MANAGER;

	private AbstractStatusHandler workbenchHandler;

	private List loggedStatuses = new ArrayList();

	/**
	 * Returns StatusManager singleton instance.
	 * 
	 * @return StatusManager instance
	 */
	public static StatusManager getManager() {
		if (MANAGER == null) {
			MANAGER = new StatusManager();
		}
		return MANAGER;
	}

	private StatusManager() {
		Platform.addLogListener(new StatusManagerLogListener());
	}

	/**
	 * @return the workbench status handler
	 */
	private AbstractStatusHandler getWorkbenchHandler() {
		if (workbenchHandler == null) {
			workbenchHandler = new WorkbenchErrorHandlerProxy();
		}

		return workbenchHandler;
	}

	/**
	 * Handles the given status adapter due to the style. Because the facility
	 * depends on Workbench, this method will log the status, if Workbench isn't
	 * initialized and the style isn't NONE. If Workbench isn't initialized and
	 * the style is NONE, the manager will do nothing.
	 * 
	 * @param statusAdapter
	 * @param style
	 *            style, values are defined in {@link StatusManager} and can be
	 *            combined with logical OR
	 */
	public void handle(StatusAdapter statusAdapter, int style) {
		try {
			// The manager will only log the status, if Workbench isn't
			// initialized and the style isn't NONE. If Workbench isn't
			// initialized and the style is NONE, the manager will do nothing.
			if (!PlatformUI.isWorkbenchRunning()) {
				if (style != StatusManager.NONE) {
					logError(statusAdapter.getStatus());
				}
				return;
			}
			
			// tries to handle the problem with default (product) handler
			if (StatusHandlerRegistry.getDefault()
					.getDefaultHandlerDescriptor() != null) {
				try {
					StatusHandlerRegistry.getDefault()
							.getDefaultHandlerDescriptor().getStatusHandler()
							.handle(statusAdapter, style);

					return;
				} catch (CoreException ex) {
					logError("Errors during the default handler creating", ex); //$NON-NLS-1$
				}
			}

			// delegates the problem to workbench handler
			getWorkbenchHandler().handle(statusAdapter, style);
		} catch (Throwable ex) {
			logError("Errors during status handling", ex); //$NON-NLS-1$
		}
	}

	/**
	 * Handles the given status adapter. The log style is used when this method
	 * is called.
	 * 
	 * @param statusAdapter
	 */
	public void handle(StatusAdapter statusAdapter) {
		handle(statusAdapter, StatusManager.LOG);
	}

	/**
	 * Handles the given status due to the style. Because the facility depends
	 * on Workbench, this method will log the status, if Workbench isn't
	 * initialized and the style isn't NONE. If Workbench isn't initialized and
	 * the style is NONE, the manager will do nothing.
	 * 
	 * @param status
	 *            status to handle
	 * @param style
	 *            style, values are defined in {@link StatusManager} and can be
	 *            combined with logical OR
	 */
	public void handle(IStatus status, int style) {
		StatusAdapter statusAdapter = new StatusAdapter(status);
		handle(statusAdapter, style);
	}

	/**
	 * Handles the given status. The log style is used when this method is
	 * called.
	 * 
	 * @param status
	 *            status to handle
	 */
	public void handle(IStatus status) {
		handle(status, StatusManager.LOG);
	}

	/**
	 * This method informs the StatusManager that this IStatus is being handled
	 * by the handler and to ignore it when it shows up in our ILogListener.
	 * 
	 * @param status
	 *            already handled and logged status
	 */
	public void addLoggedStatus(IStatus status) {
		loggedStatuses.add(status);
	}

	private void logError(String message, Throwable ex) {
		IStatus status = StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH,
				message, ex);
		addLoggedStatus(status);
		WorkbenchPlugin.log(status);
	}
	
	private void logError(IStatus status) {
		addLoggedStatus(status);
		WorkbenchPlugin.log(status);
	}

	/**
	 * This log listener handles statuses added to a plug-in's log. If our own
	 * WorkbenchErrorHandler inserts it into the log, then ignore it.
	 * 
	 * @see #addLoggedStatus(IStatus)
	 * @since 3.3
	 */
	private class StatusManagerLogListener implements ILogListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus,
		 *      java.lang.String)
		 */
		public void logging(IStatus status, String plugin) {
			if (!loggedStatuses.contains(status)) {
				handle(status, StatusManager.NONE);
			} else {
				loggedStatuses.remove(status);
			}
		}
	}
}
