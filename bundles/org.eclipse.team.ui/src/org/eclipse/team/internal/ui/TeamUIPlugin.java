package org.eclipse.team.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * TeamUIPlugin is the plugin for generic, non-provider specific,
 * team UI functionality in the workbench.
 */
public class TeamUIPlugin extends AbstractUIPlugin {

	private static TeamUIPlugin instance;
	public static final String ID = "org.eclipse.team.ui"; //$NON-NLS-1$
	
	private static List propertyChangeListeners = new ArrayList(5);

	/**
	 * Creates a new TeamUIPlugin.
	 * 
	 * @param descriptor  the plugin descriptor
	 */
	public TeamUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		instance = this;
	}
	/**
	 * Creates an extension.  If the extension plugin has not
	 * been loaded a busy cursor will be activated during the duration of
	 * the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @return the extension object
	 */
	public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		IPluginDescriptor plugin = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		if (plugin.isPluginActivated()) {
			return element.createExecutableExtension(classAttribute);
		} else {
			final Object [] ret = new Object[1];
			final CoreException [] exc = new CoreException[1];
			BusyIndicator.showWhile(null, new Runnable() {
				public void run() {
					try {
						ret[0] = element.createExecutableExtension(classAttribute);
					} catch (CoreException e) {
						exc[0] = e;
					}
				}
			});
			if (exc[0] != null)
				throw exc[0];
			else
				return ret[0];
		}	
	}
	/**
	 * Convenience method to get the currently active workbench page
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		return getPlugin().getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
	/**
	 * Return the default instance of the receiver. This represents the runtime plugin.
	 * 
	 * @return the singleton plugin instance
	 */
	public static TeamUIPlugin getPlugin() {
		return instance;
	}
	/**
	 * Initializes the preferences for this plugin if necessary.
	 */
	protected void initializePreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(UIConstants.PREF_ALWAYS_IN_INCOMING_OUTGOING, false);
	}
	
	/**
	 * Convenience method for logging statuses to the plugin log
	 * 
	 * @param status  the status to log
	 */
	public static void log(IStatus status) {
		getPlugin().getLog().log(status);
	}
	
	public static void runWithProgress(Shell parent, boolean cancelable,
		final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		boolean createdShell = false;
		try {
			if (parent == null || parent.isDisposed()) {
				Display display = Display.getCurrent();
				if (display == null) {
					// cannot provide progress (not in UI thread)
					runnable.run(new NullProgressMonitor());
					return;
				}
				// get the active shell or a suitable top-level shell
				parent = display.getActiveShell();
				if (parent == null) {
					parent = new Shell(display);
					createdShell = true;
				}
			}
			// pop up progress dialog after a short delay
			final Exception[] holder = new Exception[1];
			BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
				public void run() {
					try {
						runnable.run(new NullProgressMonitor());
					} catch (InvocationTargetException e) {
						holder[0] = e;
					} catch (InterruptedException e) {
						holder[0] = e;
					}
				}
			});
			if (holder[0] != null) {
				if (holder[0] instanceof InvocationTargetException) {
					throw (InvocationTargetException) holder[0];
				} else {
					throw (InterruptedException) holder[0];
				}
			}
			//new TimeoutProgressMonitorDialog(parent, TIMEOUT).run(true /*fork*/, cancelable, runnable);
		} finally {
			if (createdShell) parent.dispose();
		}
	}
	
	/**
	 * Creates a progress monitor and runs the specified runnable.
	 * 
	 * @param parent the parent Shell for the dialog
	 * @param cancelable if true, the dialog will support cancelation
	 * @param runnable the runnable
	 * 
	 * @exception InvocationTargetException when an exception is thrown from the runnable
	 * @exception InterruptedException when the progress monitor is cancelled
	 */
	public static void runWithProgressDialog(Shell parent, boolean cancelable,
		final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
			
		new ProgressMonitorDialog(parent).run(cancelable, cancelable, runnable);
	}
	
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		Policy.localize("org.eclipse.team.internal.ui.messages"); //$NON-NLS-1$
		initializePreferences();
	}
	
	public static void handle(Throwable t) {
		IStatus error = null;
		if (t instanceof CoreException) {
			error = ((CoreException)t).getStatus();
		} else if (t instanceof TeamException) {
			error = ((TeamException)t).getStatus();
		} else {
			error = new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, Policy.bind("simpleInternal"), t); //$NON-NLS-1$
		}
	
		Shell shell = new Shell(Display.getDefault());
	
		if (error.getSeverity() == IStatus.INFO) {
			MessageDialog.openInformation(shell, Policy.bind("information"), error.getMessage()); //$NON-NLS-1$
		} else {
			ErrorDialog.openError(shell, Policy.bind("exception"), null, error); //$NON-NLS-1$
		}
		shell.dispose();
		// Let's log non-team exceptions
		if (!(t instanceof TeamException)) {
			TeamUIPlugin.log(error);
		}
	}

	/**
	 * Register for changes made to Team properties.
	 */
	public static void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}
	
	/**
	 * Deregister as a Team property changes.
	 */
	public static void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}
	
	/**
	 * Broadcast a Team property change.
	 */
	public static void broadcastPropertyChange(PropertyChangeEvent event) {
		for (Iterator it = propertyChangeListeners.iterator(); it.hasNext();) {
			IPropertyChangeListener listener = (IPropertyChangeListener)it.next();			
			listener.propertyChange(event);
		}
	}
}