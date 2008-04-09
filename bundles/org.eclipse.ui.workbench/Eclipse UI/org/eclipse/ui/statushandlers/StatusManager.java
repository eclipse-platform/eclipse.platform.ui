/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchErrorHandlerProxy;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.progress.FinishedJobs;
import org.eclipse.ui.internal.progress.StatusAdapterHelper;
import org.eclipse.ui.internal.statushandlers.StatusHandlerRegistry;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * <p>
 * StatusManager is the entry point for all statuses to be reported in the user
 * interface.
 * </p>
 * 
 * <p>
 * Handlers shoudn't be used directly but through the StatusManager singleton
 * which keeps the status handling policy and chooses handlers.
 * <code>StatusManager.getManager().handle(IStatus)</code> and
 * <code>handle(IStatus status, int style)</code> are the methods are the
 * primary access points to the StatusManager.
 * </p>
 * 
 * <p>
 * Acceptable styles (can be combined with logical OR)
 * <ul>
 * <li>NONE - a style indicating that the status should not be acted on. This
 * is used by objects such as log listeners that do not want to report a status
 * twice</li>
 * <li>LOG - a style indicating that the status should be logged only</li>
 * <li>SHOW - a style indicating that handlers should show a problem to an user
 * without blocking the calling method while awaiting user response. This is
 * generally done using a non modal {@link Dialog}</li>
 * <li>BLOCK - a style indicating that the handling should block the calling
 * method until the user has responded. This is generally done using a modal
 * window such as a {@link Dialog}</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Handlers are intended to be accessed via the status manager. The
 * StatusManager chooses which handler should be used for a particular error.
 * There are two ways for adding handlers to the handling flow. First using
 * extension point <code>org.eclipse.ui.statusHandlers</code>, second by the
 * workbench advisor and its method
 * {@link WorkbenchAdvisor#getWorkbenchErrorHandler()}. If a handler is
 * associated with a product, it is used instead of this defined in advisor.
 * </p>
 * 
 * @since 3.3
 * @see AbstractStatusHandler
 */
public class StatusManager {
	/**
	 * A style indicating that the status should not be acted on. This is used
	 * by objects such as log listeners that do not want to report a status
	 * twice.
	 */
	public static final int NONE = 0;

	/**
	 * A style indicating that the status should be logged only.
	 */
	public static final int LOG = 0x01;

	/**
	 * A style indicating that handlers should show a problem to an user without
	 * blocking the calling method while awaiting user response. This is
	 * generally done using a non modal {@link Dialog}.
	 */
	public static final int SHOW = 0x02;

	/**
	 * A style indicating that the handling should block the calling method
	 * until the user has responded. This is generally done using a modal window
	 * such as a {@link Dialog}.
	 */
	public static final int BLOCK = 0x04;

	private static StatusManager MANAGER;

	private AbstractStatusHandler workbenchHandler;

	private List loggedStatuses = new ArrayList();

	/**
	 * Returns StatusManager singleton instance.
	 * 
	 * @return the manager instance
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
	 * initialized and the style isn't {@link #NONE}. If Workbench isn't
	 * initialized and the style is {@link #NONE}, the manager will do nothing.
	 * 
	 * @param statusAdapter
	 *            the status adapter
	 * @param style
	 *            the style. Value can be combined with logical OR. One of
	 *            {@link #NONE}, {@link #LOG}, {@link #SHOW} and
	 *            {@link #BLOCK}.
	 */
	public void handle(StatusAdapter statusAdapter, int style) {
		try {
			// The manager will only log the error when the status adapter or
			// the embedded status is null.
			if (statusAdapter == null) {
				logError(
						"Error occurred during status handling",//$NON-NLS-1$
						new NullPointerException("StatusAdapter object is null")); //$NON-NLS-1$
				return;
			}
			if (statusAdapter.getStatus() == null) {
				logError("Error occurred during status handling",//$NON-NLS-1$
						new NullPointerException("Status object is null")); //$NON-NLS-1$
				return;
			}

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
					// if statuses are shown, all finished jobs with error will
					// be removed,
					// we should remove it from the status manager, when error
					// icon
					// will be part of handlers not ProgressAnimationItem
					if (((style & StatusManager.SHOW) == StatusManager.SHOW || (style & StatusManager.BLOCK) == StatusManager.BLOCK)
							&& statusAdapter
									.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) != Boolean.TRUE) {
						FinishedJobs.getInstance().removeErrorJobs();
						StatusAdapterHelper.getInstance().clear();
					}
					return;
				} catch (CoreException ex) {
					logError("Errors during the default handler creating", ex); //$NON-NLS-1$
				}
			}

			// delegates the problem to workbench handler
			getWorkbenchHandler().handle(statusAdapter, style);

			// if statuses are shown, all finished jobs with error will be
			// removed,
			// we should remove it from the status manager, when error icon
			// will be part of handlers not ProgressAnimationItem
			if (((style & StatusManager.SHOW) == StatusManager.SHOW || (style & StatusManager.BLOCK) == StatusManager.BLOCK)
					&& statusAdapter
							.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) != Boolean.TRUE) {
				FinishedJobs.getInstance().removeErrorJobs();
			}
		} catch (Throwable ex) {
			// The used status handler failed
			logError(statusAdapter.getStatus());
			logError("Error occurred during status handling", ex); //$NON-NLS-1$
		}
	}

	/**
	 * Handles the given status adapter. The {@link #LOG} style is used when
	 * this method is called.
	 * 
	 * @param statusAdapter
	 *            the status adapter
	 */
	public void handle(StatusAdapter statusAdapter) {
		handle(statusAdapter, StatusManager.LOG);
	}

	/**
	 * Handles the given status due to the style. Because the facility depends
	 * on Workbench, this method will log the status, if Workbench isn't
	 * initialized and the style isn't {@link #NONE}. If Workbench isn't
	 * initialized and the style is {@link #NONE}, the manager will do nothing.
	 * 
	 * @param status
	 *            the status to handle
	 * @param style
	 *            the style. Value can be combined with logical OR. One of
	 *            {@link #NONE}, {@link #LOG}, {@link #SHOW} and
	 *            {@link #BLOCK}.
	 */
	public void handle(IStatus status, int style) {
		StatusAdapter statusAdapter = new StatusAdapter(status);
		handle(statusAdapter, style);
	}

	/**
	 * Handles the given status. The {@link #LOG} style is used when this method
	 * is called.
	 * 
	 * @param status
	 *            the status to handle
	 */
	public void handle(IStatus status) {
		handle(status, StatusManager.LOG);
	}

	/**
	 * Handles given CoreException. This method has been introduced to prevent
	 * anti-pattern: <br/><code>
	 * StatusManager.getManager().handle(coreException.getStatus());
	 * </code><br/>
	 * that does not print the stack trace to the log.
	 * 
	 * @param coreException
	 *            a CoreException to be handled.
	 * @param pluginId
	 *            the unique identifier of the relevant plug-in
	 * @see StatusManager#handle(IStatus)
	 * @since 3.4
	 * 
	 */
	public void handle(CoreException coreException,String pluginId) {
		handle(new Status(IStatus.WARNING, pluginId, coreException
				.getLocalizedMessage(), coreException));
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
