package org.eclipse.ui.examples.jobs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.examples.jobs.views.ProgressExampleAdapterFactory;
import org.eclipse.ui.examples.jobs.views.SlowElement;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class ProgressExamplesPlugin extends AbstractUIPlugin {
	//The shared instance.
	private static ProgressExamplesPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public ProgressExamplesPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static ProgressExamplesPlugin getDefault() {
		return plugin;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException {
		IAdapterManager m = Platform.getAdapterManager();
		IAdapterFactory f = new ProgressExampleAdapterFactory();
		m.registerAdapters(f, SlowElement.class);
	}
}