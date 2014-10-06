package org.eclipse.e4.internal.tools;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.ResourceLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class ToolsPlugin extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.e4.tools"; //$NON-NLS-1$

	private static ToolsPlugin plugin;

	private ResourceLocator resourceLocator;

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ToolsPlugin getDefault() {
		return plugin;
	}

	public ResourceLocator getResourceLocator() {
		if (resourceLocator == null) {
			resourceLocator = new ResourceLocator() {

				@Override
				public String getString(String key, Object[] substitutions,
					boolean translate) {
					return null;
				}

				@Override
				public String getString(String key, Object[] substitutions) {
					return null;
				}

				@Override
				public String getString(String key, boolean translate) {
					return null;
				}

				@Override
				public String getString(String key) {
					return null;
				}

				@Override
				public Object getImage(String key) {
					return null;
				}

				@Override
				public URL getBaseURL() {
					return null;
				}
			};
		}
		return resourceLocator;
	}

	public static void log(IStatus status, int nesting, boolean appendLogger) {
		getDefault().getLog().log(status);
	}

	/**
	 * Log Throwable Error
	 */
	public static void logError(Throwable t) {
		logError(t, 0, true);
	}

	public static void logError(Throwable t, boolean appendLogger) {
		logError(t, 0, appendLogger);
	}

	public static void logError(Throwable t, int nesting) {
		logError(t, nesting, true);
	}

	public static void logError(Throwable t, int nesting, boolean appendLogger) {
		log(newStatus(IStatus.ERROR, t.getMessage(), t), nesting, appendLogger);
	}

	/**
	 * Create an IStatus
	 *
	 * @return a new IStatus
	 */
	public static IStatus newStatus(int severity, String message,
		Throwable exception) {
		return new Status(severity, PLUGIN_ID, 0, message, exception);
	}
}
