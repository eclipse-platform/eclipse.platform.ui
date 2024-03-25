/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
package org.eclipse.ui;

/**
 * Plug-ins that register a startup extension will be activated after the
 * Workbench initializes and have an opportunity to run code that can't be
 * implemented using the normal contribution mechanisms.
 * <p>
 * Users can disable the execution of specific extensions in their workspace's
 * preferences via<br>
 * {@code  General -> Startup and Shutdown -> Plug-ins activated on startup}.
 * </p>
 * <p>
 * Instead of implementing this interface and registering the implementation as
 * Eclipse-Extension, handlers that want to be notified upon application
 * start-up can be registered declaratively as OSGi
 * {@link org.osgi.service.event.EventHandler} service for the
 * {@link org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle#APP_STARTUP_COMPLETE}
 * event.<br>
 * Such an event-handler is always executed and cannot be disabled via a
 * preferences.
 * </p>
 * For example a class
 *
 * <pre>
 * public class MyStartupHandler implements IStartup {
 * 	&#64;Override
 * 	public void earlyStartup() {
 * 		// do handling...
 * 	}
 * }
 * </pre>
 *
 * can be rewritten to
 *
 * <pre>
 * &#64;Component(service = EventHandler.class)
 * &#64;EventTopics(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
 * public class MyStartupHandler implements EventHandler {
 * 	&#64;Override
 * 	public void handleEvent(Event event) {
 * 		// do handling...
 * 	}
 * }
 * </pre>
 *
 * <p>
 * Processing of OSGi declarative services annotations has to be enabled for the
 * containing Plug-in and it has to import the package
 * {@code org.osgi.service.event} and
 * {@code org.osgi.service.event.propertytypes} as well as a dependency to
 * {@code org.eclipse.e4.ui.workbench}. At the same time the registration of the
 * handler as Eclipse Extension for the {@code org.eclipse.ui.startup}
 * extension-point can be removed.
 * </p>
 *
 * @since 2.0
 */
public interface IStartup {
	/**
	 * Will be called in a separate thread after the workbench initializes.
	 * <p>
	 * Note that most workbench methods must be called in the UI thread since they
	 * may access SWT. For example, to obtain the current workbench window, use:
	 * </p>
	 *
	 * <pre>
	 * <code>
	 * IWorkbench workbench = PlatformUI.getWorkbench();
	 * workbench.getDisplay().asyncExec(new Runnable() {
	 *   public void run() {
	 *     IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	 *     if (window != null) {
	 *       // do something
	 *     }
	 *   }
	 * });
	 * </code>
	 * </pre>
	 *
	 * @see org.eclipse.swt.widgets.Display#asyncExec
	 * @see org.eclipse.swt.widgets.Display#syncExec
	 */
	void earlyStartup();
}
