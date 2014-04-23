/*******************************************************************************
 * Copyright (c) 2003, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Mickael Istria (Red Hat Inc.) - 226046 Add filter for user-spec'd patterns
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import java.util.Collections;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.filters.UserFilter;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.services.IEvaluationService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * The main plugin class for the Navigator.
 *
 * @since 3.2
 */
public class NavigatorPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static NavigatorPlugin plugin;

	private static final int LOG_DELAY = 100;

	/**
	 * The delay before updating the action bars. Must be shorter than the
	 * LINK_HELPER_DELAY to make sure the linking works after the boot.
	 */
	public static final int ACTION_BAR_DELAY = 100;

	/**
	 * The delay before responding to a selection/activation event in processing
	 * for linking with the editor.
	 */
	public static final int LINK_HELPER_DELAY = ACTION_BAR_DELAY + 20;

	private static class LogJob extends Job {


		private ListenerList messages = new ListenerList() {

			@Override
			public synchronized Object[] getListeners() {
				Object[] mesgs = super.getListeners();
				clear();
				return mesgs;
			}
		};


		/**
		 * Creates a Job which offloads the logging work into a non-UI thread.
		 *
		 */
		public LogJob() {
			super(CommonNavigatorMessages.LoggingJob);
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			Object[] mesgs = messages.getListeners();
			ILog pluginLog = getDefault().getLog();
			for (Object mesg : mesgs) {
				pluginLog.log((IStatus)mesg);
			}
			return Status.OK_STATUS;

		}

		/**
		 * @param mesg The message to add to the Plugin's log.
		 */
		public void log(IStatus mesg) {
			messages.add(mesg);

		}

	}

	private static final LogJob logJob = new LogJob();

	/** The id of the orge.eclipse.ui.navigator plugin. */
	public static String PLUGIN_ID = "org.eclipse.ui.navigator"; //$NON-NLS-1$

	private BundleListener bundleListener = new BundleListener() {
		@Override
		public void bundleChanged(BundleEvent event) {
			NavigatorSaveablesService.bundleChanged(event);
		}
	};

	/**
	 * This constant can be used via {@link CommonViewer#setData(String, Object)} to set/get the lisst
	 * of available regexps to filter out  resource from the viewer based on resource name.
	 * The expected type for this data is a Collection of {@link UserFilter}.
	 */
	public static final String RESOURCE_REGEXP_FILTER_DATA = "resourceRegexpFilters"; //$NON-NLS-1$
	/**
	 * The
	 */
	public static final String RESOURCE_REGEXP_FILTER_FILTER_ID = "org.eclipse.ui.navigator.resources.filters.userDefined"; //$NON-NLS-1$

	/**
	 * Creates a new instance of the receiver
	 */
	public NavigatorPlugin() {
		super();
		plugin = this;
	}

	/**
	 * @return the shared instance.
	 */
	public static NavigatorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}


	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 *
	 * @param path
	 *            the path
	 * @return the image
	 */
	public Image getImage(String path) {
		Image image = getImageRegistry().get(path);
		if(image == null) {
			ImageDescriptor descriptor = getImageDescriptor(path);
			if(descriptor != null) {
				getImageRegistry().put(path, image = descriptor.createImage());
			}
		}
		return image;
	}

	/**
	 * Record an error against this plugin's log.
	 *
	 * @param aCode
	 * @param aMessage
	 * @param anException
	 */
	public static void logError(int aCode, String aMessage,
			Throwable anException) {
		getDefault().getLog().log(
				createErrorStatus(aCode, aMessage, anException));
	}

	/**
	 *
	 * Record a message against this plugin's log.
	 *
	 * @param severity
	 * @param aCode
	 * @param aMessage
	 * @param exception
	 */
	public static void log(int severity, int aCode, String aMessage,
			Throwable exception) {
		log(createStatus(severity, aCode, aMessage, exception));
	}

	/**
	 *
	 * Record a status against this plugin's log.
	 *
	 * @param aStatus
	 */
	public static void log(IStatus aStatus) {
		//getDefault().getLog().log(aStatus);
		logJob.log(aStatus);
		logJob.schedule(LOG_DELAY);
	}

	/**
	 * @return an evaluation context
	 */
	public static IEvaluationContext getApplicationContext() {
		IEvaluationService es = PlatformUI.getWorkbench().getService(IEvaluationService.class);
		return es == null ? null : es.getCurrentState();
	}

	/**
	 * @return an evaluation context
	 */
	public static IEvaluationContext getEmptyEvalContext() {
		IEvaluationContext c = new EvaluationContext(getApplicationContext(),
				Collections.EMPTY_LIST);
		c.setAllowPluginActivation(true);
		return c;
	}

	/**
	 * @param selection
	 * @return an evaluation context
	 */
	public static IEvaluationContext getEvalContext(Object selection) {
		IEvaluationContext c = new EvaluationContext(getApplicationContext(), selection);
		c.setAllowPluginActivation(true);
		return c;
	}


	/**
	 * Helper class to evaluate an expression.
	 */
	public static class Evaluator implements ISafeRunnable {
		EvaluationResult result;
		Expression expression;
		IEvaluationContext scope;

		@Override
		public void handleException(Throwable exception) {
			result = EvaluationResult.FALSE;
		}

		@Override
		public void run() throws Exception {
			result = expression.evaluate(scope);
		}
	}

	/**
	 * Safely evaluation an expression, logging appropriately on error
	 *
	 * @param expression
	 * @param scope
	 * @return the EvaluationResult
	 */
	public static EvaluationResult safeEvaluate(Expression expression, IEvaluationContext scope) {
		Evaluator evaluator = new Evaluator();
		evaluator.expression = expression;
		evaluator.scope = scope;
		SafeRunner.run(evaluator);
		return evaluator.result;
	}

	/**
	 * Create a status associated with this plugin.
	 *
	 * @param severity
	 * @param aCode
	 * @param aMessage
	 * @param exception
	 * @return A status configured with this plugin's id and the given parameters.
	 */
	public static IStatus createStatus(int severity, int aCode,
			String aMessage, Throwable exception) {
		return new Status(severity, PLUGIN_ID, aCode,
				aMessage != null ? aMessage : "No message.", exception); //$NON-NLS-1$
	}

	/**
	 *
	 * @param aCode
	 * @param aMessage
	 * @param exception
	 * @return A status configured with this plugin's id and the given parameters.
	 */
	public static IStatus createErrorStatus(int aCode, String aMessage,
			Throwable exception) {
		return createStatus(IStatus.ERROR, aCode, aMessage, exception);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		// System.out.println("Navigator plugin starting"); //$NON-NLS-1$
		super.start(context);
		context.addBundleListener(bundleListener);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		context.removeBundleListener(bundleListener);
		super.stop(context);
		// System.out.println("Navigator plugin stopped"); //$NON-NLS-1$
	}

}
