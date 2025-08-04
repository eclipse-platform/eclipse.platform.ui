/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
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
 *     Gorkem Ercan (Red Hat) - Fix for Bug 427142
 *******************************************************************************/

package org.eclipse.ui.statushandlers;

import java.util.List;
import java.util.Vector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.WorkbenchErrorHandlerProxy;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.statushandlers.StatusHandlerDescriptor;
import org.eclipse.ui.internal.statushandlers.StatusHandlerRegistry;
import org.eclipse.ui.progress.IProgressConstants;
import org.osgi.framework.BundleContext;

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
 * </p>
 * <ul>
 * <li>NONE - a style indicating that the status should not be acted on. This is
 * used by objects such as log listeners that do not want to report a status
 * twice</li>
 * <li>LOG - a style indicating that the status should be logged only</li>
 * <li>SHOW - a style indicating that handlers should show a problem to an user
 * without blocking the calling method while awaiting user response. This is
 * generally done using a non modal {@link Dialog}</li>
 * <li>BLOCK - a style indicating that the handling should block the UI until
 * the user has responded. This is generally done using a modal window such as a
 * {@link Dialog}</li>
 * </ul>
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
	 * A style indicating that the status should not be acted on. This is used by
	 * objects such as log listeners that do not want to report a status twice.
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
	 * A style indicating that the handling should block the calling thread until
	 * the status has been handled.
	 * <p>
	 * A typical usage of this would be to ensure that the user's actions are
	 * blocked until they've dealt with the status in some manner. It is therefore
	 * likely but not required that the <code>StatusHandler</code> would achieve
	 * this through the use of a modal dialog.
	 * </p>
	 * <p>
	 * Due to the fact that use of <code>BLOCK</code> will block UI, care should be
	 * taken in this use of this flag.
	 * </p>
	 */
	public static final int BLOCK = 0x04;

	private static volatile StatusManager MANAGER;

	private volatile AbstractStatusHandler statusHandler;

	private List<IStatus> loggedStatuses = new Vector<>();

	private ListenerList<INotificationListener> listeners = new ListenerList<>();

	private StatusManagerLogListener statusManagerLogListener;

	/**
	 * Returns StatusManager singleton instance.
	 *
	 * @return the manager instance
	 */
	public static StatusManager getManager() {
		if (MANAGER != null) {
			return MANAGER;
		}
		synchronized (StatusManager.class) {
			if (MANAGER == null) {
				MANAGER = new StatusManager();
			}
		}
		return MANAGER;
	}

	private StatusManager() {
		statusManagerLogListener = new StatusManagerLogListener();
		if (Platform.isRunning()) {
			Platform.addLogListener(statusManagerLogListener);
		}
	}

	private AbstractStatusHandler getStatusHandler() {
		if (statusHandler != null) {
			return statusHandler;
		}
		BundleContext bundleContext = WorkbenchPlugin.getDefault().getBundle().getBundleContext();
		if (bundleContext == null) {
			// bundle is not in the STARTING, ACTIVE, or STOPPING state: we
			// should not do anything, most likely we are going to shut down
			return null;
		}

		StatusHandlerDescriptor defaultHandlerDescriptor = StatusHandlerRegistry.getDefault()
				.getDefaultHandlerDescriptor();

		synchronized (this) {
			if (statusHandler == null) {
				if (defaultHandlerDescriptor != null) {
					try {
						statusHandler = defaultHandlerDescriptor.getStatusHandler();
					} catch (CoreException ex) {
						logError("Errors during the default handler creating", ex); //$NON-NLS-1$
					}
				}
				if (statusHandler == null) {
					statusHandler = new WorkbenchErrorHandlerProxy();
				}
			}
		}
		return statusHandler;
	}

	/**
	 * Handles the given status adapter due to the style. Because the facility
	 * depends on Workbench, this method will log the status, if Workbench isn't
	 * initialized and the style isn't {@link #NONE}. If Workbench isn't initialized
	 * and the style is {@link #NONE}, the manager will do nothing.
	 *
	 * @param statusAdapter the status adapter
	 * @param style         the style. Value can be combined with logical OR. One of
	 *                      {@link #NONE}, {@link #LOG}, {@link #SHOW} and
	 *                      {@link #BLOCK}.
	 */
	public void handle(StatusAdapter statusAdapter, int style) {
		try {
			// The manager will only log the error when the status adapter or
			// the embedded status is null.
			if (statusAdapter == null) {
				logError("Error occurred during status handling", //$NON-NLS-1$
						new NullPointerException("StatusAdapter object is null")); //$NON-NLS-1$
				return;
			}
			if (statusAdapter.getStatus() == null) {
				logError("Error occurred during status handling", //$NON-NLS-1$
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

			// delegates the problem to workbench handler
			AbstractStatusHandler handler = getStatusHandler();
			if (handler != null) {
				handler.handle(statusAdapter, style);
			} else if (style != StatusManager.NONE) {
				logError(statusAdapter.getStatus());
			}

			// if attached status handler is not able to notify StatusManager
			// about particular event, use the default policy and fake the
			// notification
			if (handler == null || !handler.supportsNotification(INotificationTypes.HANDLED)) {
				generateFakeNotification(statusAdapter, style);
			}
		} catch (Throwable ex) {
			// The used status handler failed
			if (statusAdapter != null) {
				logError(statusAdapter.getStatus());
			}
			logError("Error occurred during status handling", ex); //$NON-NLS-1$
		}
	}

	/**
	 * Handles the given status adapter. The {@link #LOG} style is used when this
	 * method is called.
	 *
	 * @param statusAdapter the status adapter
	 */
	public void handle(StatusAdapter statusAdapter) {
		handle(statusAdapter, StatusManager.LOG);
	}

	/**
	 * Handles the given status due to the style. Because the facility depends on
	 * Workbench, this method will log the status, if Workbench isn't initialized
	 * and the style isn't {@link #NONE}. If Workbench isn't initialized and the
	 * style is {@link #NONE}, the manager will do nothing.
	 *
	 * @param status the status to handle
	 * @param style  the style. Value can be combined with logical OR. One of
	 *               {@link #NONE}, {@link #LOG}, {@link #SHOW} and {@link #BLOCK}.
	 */
	public void handle(IStatus status, int style) {
		StatusAdapter statusAdapter = new StatusAdapter(status);
		handle(statusAdapter, style);
	}

	/**
	 * Handles the given status. The {@link #LOG} style is used when this method is
	 * called.
	 *
	 * @param status the status to handle
	 */
	public void handle(IStatus status) {
		handle(status, StatusManager.LOG);
	}

	/**
	 * Handles given CoreException. This method has been introduced to prevent
	 * anti-pattern:
	 *
	 * <pre>
	 * <code>
	 * StatusManager.getManager().handle(coreException.getStatus());
	 * </code>
	 * </pre>
	 *
	 * that does not print the stack trace to the log.
	 *
	 * @param coreException a CoreException to be handled.
	 * @param pluginId      the unique identifier of the relevant plug-in
	 * @see StatusManager#handle(IStatus)
	 * @since 3.4
	 */
	public void handle(CoreException coreException, String pluginId) {
		IStatus exceptionStatus = coreException.getStatus();
		handle(new Status(exceptionStatus.getSeverity(), pluginId, coreException.getLocalizedMessage(), coreException));
	}

	/**
	 * This method informs the StatusManager that this IStatus is being handled by
	 * the handler and to ignore it when it shows up in our ILogListener.
	 *
	 * @param status already handled and logged status
	 */
	public void addLoggedStatus(IStatus status) {
		loggedStatuses.add(status);
	}

	private void logError(String message, Throwable ex) {
		IStatus status = StatusUtil.newStatus(WorkbenchPlugin.PI_WORKBENCH, message, ex);
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

		@Override
		public void logging(IStatus status, String plugin) {
			if (!loggedStatuses.contains(status)) {
				handle(status, StatusManager.NONE);
			} else {
				loggedStatuses.remove(status);
			}
		}
	}

	/**
	 * This method should be called by custom status handlers when an event occurs.
	 * This method has no effect if statushandler does not support particular event
	 * type.
	 *
	 * @param type     - type of the event.
	 * @param adapters - array of affected {@link StatusAdapter}s.
	 * @see INotificationTypes
	 * @see AbstractStatusHandler#supportsNotification(int)
	 * @since 3.5
	 */
	public void fireNotification(int type, StatusAdapter[] adapters) {
		AbstractStatusHandler handler = getStatusHandler();
		if (handler != null && handler.supportsNotification(type)) {
			doFireNotification(type, adapters);
		}
	}

	private void doFireNotification(int type, StatusAdapter[] adapters) {
		for (INotificationListener listener : listeners) {
			listener.statusManagerNotified(type, adapters);
		}
	}

	private void generateFakeNotification(StatusAdapter statusAdapter, int style) {
		if (((style & StatusManager.SHOW) == StatusManager.SHOW || (style & StatusManager.BLOCK) == StatusManager.BLOCK)
				&& statusAdapter.getProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY) != Boolean.TRUE) {
			doFireNotification(INotificationTypes.HANDLED, new StatusAdapter[] { statusAdapter });
		}
	}

	/**
	 * Adds a listener to the StatusManager.
	 *
	 * @param listener - a listener to be added.
	 * @since 3.5
	 */
	public void addListener(INotificationListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * Removes a listener from StatusManager.
	 *
	 * @param listener - a listener to be removed.
	 * @since 3.5
	 */
	public void removeListener(INotificationListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * This interface allows for listening to status handling framework changes.
	 * Currently it is possible to be notified when:
	 * <ul>
	 * <li>all statuses has been handled</li>
	 * </ul>
	 *
	 * @since 3.5
	 */
	public interface INotificationListener {
		/**
		 *
		 * @param type     - a type of notification.
		 * @param adapters - affected {@link StatusAdapter}s
		 */
		void statusManagerNotified(int type, StatusAdapter[] adapters);
	}

	/**
	 * This interface declares types of notification.
	 *
	 * @since 3.5
	 * @noextend This interface is not intended to be extended by clients.
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public interface INotificationTypes {

		/**
		 * This type notifications are used when a particular {@link StatusAdapter} was
		 * handled.
		 */
		int HANDLED = 0x01;

	}

	/**
	 * Unregisters the StatusManager from the workbench. Called by the framework
	 * during shutdown, should not be called by client code.
	 *
	 * @since 3.117
	 */
	public void unregister() {
		Platform.removeLogListener(statusManagerLogListener);
	}
}
