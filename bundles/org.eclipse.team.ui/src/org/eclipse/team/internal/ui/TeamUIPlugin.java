/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.internal.ui.jobs.RefreshSubscriberInputJob;
import org.eclipse.team.internal.ui.jobs.RefreshSubscriberJob;
import org.eclipse.team.internal.ui.sync.actions.SyncViewerDirectionFilters;
import org.eclipse.team.internal.ui.sync.views.SyncViewerTableSorter;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.sync.SubscriberAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * TeamUIPlugin is the plugin for generic, non-provider specific,
 * team UI functionality in the workbench.
 */
public class TeamUIPlugin extends AbstractUIPlugin implements IPropertyChangeListener {

	private static TeamUIPlugin instance;
	
	public static final String ID = "org.eclipse.team.ui"; //$NON-NLS-1$
	public static final String PT_SUBSCRIBER_MENUS = "subscriberMenus"; //$NON-NLS-1$
	
	//	plugin id
	public static final String PLUGIN_ID = "org.eclipse.team.ui"; //$NON-NLS-1$
	
	 // extension points
	 public static final String PT_CONFIGURATION ="configurationWizards"; //$NON-NLS-1$
	 public static final String PT_TARGETCONFIG ="targetConfigWizards"; //$NON-NLS-1$
	 public static final String PT_DECORATORS = "decorators"; //$NON-NLS-1$	
	
	private static List propertyChangeListeners = new ArrayList(5);
	
	private static Hashtable imageDescriptors = new Hashtable(20);
	private static List disposeOnShutdownImages= new ArrayList();
	
	private RefreshSubscriberInputJob refreshJob;

	/**
	 * Returns the job that refreshes the active subscribers in the background.
	 */
	public RefreshSubscriberInputJob getRefreshJob() {
		return refreshJob;
	}

	/**
	 * Creates a new TeamUIPlugin.
	 * 
	 * @param descriptor  the plugin descriptor
	 */
	public TeamUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		instance = this;
		initializeImages();
		initializePreferences();
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
	 * Convenience method to get the currently active workbench page. Note that
	 * the active page may not be the one that the usr perceives as active in
	 * some situations so this method of obtaining the activae page should only
	 * be used if no other method is available.
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getPlugin().getWorkbench().getActiveWorkbenchWindow();
		if (window == null) return null;
		return window.getActivePage();
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
		store.setDefault(IPreferenceIds.SYNCVIEW_BACKGROUND_SYNC, true);
		store.setDefault(IPreferenceIds.SYNCVIEW_SCHEDULED_SYNC, false);
		store.setDefault(IPreferenceIds.SYNCVIEW_DELAY, 60 /* minutes */);
		store.setDefault(IPreferenceIds.SYNCVIEW_COMPRESS_FOLDERS, true);
		store.setDefault(IPreferenceIds.SYNCVIEW_VIEW_TABLESORT, SyncViewerTableSorter.COL_NAME);
		store.setDefault(IPreferenceIds.SYNCVIEW_VIEW_TABLESORT_REVERSED, false);
		store.setDefault(IPreferenceIds.SYNCVIEW_SELECTED_MODE, SyncViewerDirectionFilters.BOTH_MODE);
		store.setDefault(IPreferenceIds.SYNCVIEW_DEFAULT_PERSPECTIVE, "org.eclipse.team.internal.ui.sync.views.TeamSynchronizingPerspective"); //$NON-NLS-1$
	}
	
	/**
	 * Convenience method for logging statuses to the plugin log
	 * 
	 * @param status  the status to log
	 */
	public static void log(IStatus status) {
		getPlugin().getLog().log(status);
	}
	
	/**
	 * Convenience method for logging a TeamException in such a way that the
	 * stacktrace is logged as well.
	 * @param e
	 */
	public static void log(CoreException e) {
		IStatus status = e.getStatus();
		log (status.getSeverity(), status.getMessage(), e);
	}
	
	/**
	 * Log the given exception along with the provided message and severity indicator
	 */
	public static void log(int severity, String message, Throwable e) {
		log(new Status(severity, ID, 0, message, e));
	}
	
	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		Policy.localize("org.eclipse.team.internal.ui.messages"); //$NON-NLS-1$
		initializePreferences();
		
		getPreferenceStore().addPropertyChangeListener(this);
		
		// startup auto-refresh job if necessary
		refreshJob = new RefreshSubscriberInputJob(Policy.bind("ScheduledSyncViewRefresh.taskName")); //$NON-NLS-1$		
		refreshJob.setRefreshInterval(getPreferenceStore().getInt(IPreferenceIds.SYNCVIEW_DELAY) * 60);
		if(getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_SCHEDULED_SYNC)) {
			refreshJob.setRestartOnCancel(true);
			refreshJob.setReschedule(true);
			// start once the platform has started and stabilized
			SubscriberAction.getJobStatusHandler().schedule(refreshJob, 20000 /* 20 seconds */);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		disposeImages();
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

	/**
	 * Registers the given image for being disposed when this plug-in is shutdown.
	 *
	 * @param image the image to register for disposal
	 */
	public static void disposeOnShutdown(Image image) {
		if (image != null)
			disposeOnShutdownImages.add(image);
	}
	
	/**
	 * Creates an image and places it in the image registry.
	 * 
	 * @param id  the identifier for the image
	 * @param baseURL  the base URL for the image
	 */
	protected static void createImageDescriptor(String id, URL baseURL) {
		URL url = null;
		try {
			url = new URL(baseURL, ISharedImages.ICON_PATH + id);
		} catch (MalformedURLException e) {
		}
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		imageDescriptors.put(id, desc);
	}
	/**
	 * Returns the image descriptor for the given image ID.
	 * Returns null if there is no such image.
	 * 
	 * @param id  the identifier for the image to retrieve
	 * @return the image associated with the given ID
	 */
	public static ImageDescriptor getImageDescriptor(String id) {
		if(! imageDescriptors.containsKey(id)) {
			URL baseURL = TeamUIPlugin.getPlugin().getDescriptor().getInstallURL();
			createImageDescriptor(id, baseURL);
		}
		return (ImageDescriptor)imageDescriptors.get(id);
	}	
	/**
	 * Convenience method to get an image descriptor for an extension
	 * 
	 * @param extension  the extension declaring the image
	 * @param subdirectoryAndFilename  the path to the image
	 * @return the image
	 */
	public static ImageDescriptor getImageDescriptorFromExtension(IExtension extension, String subdirectoryAndFilename) {
		IPluginDescriptor pluginDescriptor = extension.getDeclaringPluginDescriptor();
		URL path = pluginDescriptor.getInstallURL();
		URL fullPathString = null;
		try {
			fullPathString = new URL(path,subdirectoryAndFilename);
			return ImageDescriptor.createFromURL(fullPathString);
		} catch (MalformedURLException e) {
		}
		return null;
	}
	/**
	 * Initializes the table of images used in this plugin.
	 */
	private void initializeImages() {
		URL baseURL = TeamUIPlugin.getPlugin().getDescriptor().getInstallURL();

		// View decoration overlays
		createImageDescriptor(ISharedImages.IMG_DIRTY_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_CONFLICT_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_CHECKEDOUT_OVR, baseURL);
		createImageDescriptor(ISharedImages.IMG_SYNC_VIEW, baseURL);
		
		// Collapse all
		createImageDescriptor(ISharedImages.IMG_COLLAPSE_ALL, baseURL);
		createImageDescriptor(ISharedImages.IMG_COLLAPSE_ALL_ENABLED, baseURL);
		
		// Target Management Icons
		createImageDescriptor(ISharedImages.IMG_SITE_ELEMENT, baseURL);
		
		// Sync View Icons
		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_INCOMING, baseURL);
		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_OUTGOING, baseURL);
		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_CONFLICTING, baseURL);
		createImageDescriptor(ISharedImages.IMG_REFRESH, baseURL);
		createImageDescriptor(ISharedImages.IMG_CHANGE_FILTER, baseURL);
		createImageDescriptor(ISharedImages.IMG_IGNORE_WHITESPACE, baseURL);
		createImageDescriptor(ISharedImages.IMG_CONTENTS, baseURL);

		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_INCOMING_DISABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_OUTGOING_DISABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_CONFLICTING_DISABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_REFRESH_DISABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_IGNORE_WHITESPACE_DISABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_CONTENTS_DISABLED, baseURL);

		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_INCOMING_ENABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_OUTGOING_ENABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_DLG_SYNC_CONFLICTING_ENABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_REFRESH_ENABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_IGNORE_WHITESPACE_ENABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_CONTENTS_ENABLED, baseURL);

		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_CATCHUP, baseURL);
		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_RELEASE, baseURL);
		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_FREE, baseURL);

		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_CATCHUP_DISABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_RELEASE_DISABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_FREE_DISABLED, baseURL);

		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_CATCHUP_ENABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_RELEASE_ENABLED, baseURL);
		createImageDescriptor(ISharedImages.IMG_SYNC_MODE_FREE_ENABLED, baseURL);

		createImageDescriptor(ISharedImages.IMG_WIZBAN_SHARE, baseURL);
		
		// Wizard banners
		createImageDescriptor(ISharedImages.IMG_PROJECTSET_IMPORT_BANNER, baseURL);
		createImageDescriptor(ISharedImages.IMG_PROJECTSET_EXPORT_BANNER, baseURL);	
		
		// Live Sync View icons
		createImageDescriptor(ISharedImages.IMG_COMPRESSED_FOLDER, baseURL);	
	}

	/**
	 * Dispose of images
	 */
	public static void disposeImages() {
		if (disposeOnShutdownImages != null) {
			Iterator i= disposeOnShutdownImages.iterator();
			while (i.hasNext()) {
				Image img= (Image) i.next();
				if (!img.isDisposed())
					img.dispose();
			}
			imageDescriptors= null;
		}
	}

	public void propertyChange(PropertyChangeEvent event) {		
		// update the background sync delay
		if(event.getProperty().equals(IPreferenceIds.SYNCVIEW_DELAY)) {
			RefreshSubscriberJob refreshJob = getRefreshJob();
			refreshJob.setRefreshInterval(getPreferenceStore().getInt(IPreferenceIds.SYNCVIEW_DELAY) * 60);
		}
		
		// enable / disable the background sync job
		if(event.getProperty().equals(IPreferenceIds.SYNCVIEW_SCHEDULED_SYNC)) {
			RefreshSubscriberJob refreshJob = getRefreshJob();
			refreshJob.setRefreshInterval(getPreferenceStore().getInt(IPreferenceIds.SYNCVIEW_DELAY) * 60);
			boolean value = getPreferenceStore().getBoolean(IPreferenceIds.SYNCVIEW_SCHEDULED_SYNC);
			if(value) {
				refreshJob.setRestartOnCancel(true);
				refreshJob.setReschedule(true);
				refreshJob.schedule();				
			} else {				
				refreshJob.setRestartOnCancel(false /* don't restart the job */);
				refreshJob.setReschedule(false);
				refreshJob.cancel();				
			}
		}
	}
}
