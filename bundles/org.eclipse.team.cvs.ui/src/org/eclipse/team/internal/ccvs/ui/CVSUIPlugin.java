/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import java.util.Hashtable;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.util.AddDeleteMoveListener;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.ui.model.CVSAdapterFactory;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.WorkbenchChainedTextFontFieldEditor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * UI Plugin for CVS provider-specific workbench functionality.
 */
public class CVSUIPlugin extends AbstractUIPlugin implements IPropertyChangeListener {
	/**
	 * The id of the CVS plug-in
	 */
	public static final String ID = "org.eclipse.team.cvs.ui"; //$NON-NLS-1$
	public static final String DECORATOR_ID = "org.eclipse.team.cvs.ui.decorator"; //$NON-NLS-1$
	
	private Hashtable imageDescriptors = new Hashtable(20);

	public final static String ICON_PATH;
	static {
		final String iconPath[] = new String[1];
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			public void run() {
				if (display.getIconDepth() > 4) {
					iconPath[0] = ICVSUIConstants.ICON_PATH_FULL;
				} else {
					iconPath[0] = ICVSUIConstants.ICON_PATH_BASIC;
				}
			}
		});
		ICON_PATH = iconPath[0];
	}

	// timeout in milliseconds before displaying a progress monitor dialog
	// (used for normally short-running interactive operations)
	private static final int TIMEOUT = 2000;

	/**
	 * The singleton plug-in instance
	 */
	private static CVSUIPlugin plugin;
	
	/**
	 * The repository manager
	 */
	private RepositoryManager repositoryManager;
	
	// Property change listener
	IPropertyChangeListener listener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(TeamUI.GLOBAL_IGNORES_CHANGED)) {
				CVSDecorator.refresh();
			}
		}
	};
		
	/**
	 * CVSUIPlugin constructor
	 * 
	 * @param descriptor  the plugin descriptor
	 */
	public CVSUIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		getPreferenceStore().addPropertyChangeListener(this);
	}

	/**
	 * Creates an image and places it in the image registry.
	 */
	protected void createImageDescriptor(String id, URL baseURL) {
		URL url = null;
		try {
			url = new URL(baseURL, ICON_PATH + id);
		} catch (MalformedURLException e) {
		}
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		imageDescriptors.put(id, desc);
	}
	
	/**
	 * Returns the active workbench page.
	 * 
	 * @return the active workbench page
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getPlugin().getWorkbench().getActiveWorkbenchWindow();
		if (window == null) return null;
		return window.getActivePage();
	}
	
	/**
	 * Extract or convert to a TeamException
	 */
	public static TeamException asTeamException(InvocationTargetException e) {
		Throwable exception = e.getTargetException();
		if (exception instanceof TeamException) {
			return (TeamException)exception;
		} else if (exception instanceof CoreException) {
			return new TeamException(((CoreException)exception).getStatus());
		} else {
			return new TeamException(new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("internal"), exception)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Run an operation involving the given resource. If an exception is thrown
	 * and the code on the status is IResourceStatus.OUT_OF_SYNC_LOCAL then
	 * the user will be prompted to refresh and try again. If they agree, then the
	 * supplied operation will be run again.
	 */
	public static void runWithRefresh(Shell parent, IResource[] resources, 
		IRunnableWithProgress runnable, IProgressMonitor monitor) 
		throws InvocationTargetException, InterruptedException {
		boolean firstTime = true;
		while(true) {
			try {
				runnable.run(monitor);
				return;
			} catch (InvocationTargetException e) {
				if (! firstTime) throw e;
				IStatus status = null;
				if (e.getTargetException() instanceof CoreException) {
					status = ((CoreException)e.getTargetException()).getStatus();
				} else if (e.getTargetException() instanceof TeamException) {
					status = ((TeamException)e.getTargetException()).getStatus();
				} else {
					throw e;
				}
				if (status.getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
					if (promptToRefresh(parent, resources, status)) {
						try {
							for (int i = 0; i < resources.length; i++) {
								resources[i].refreshLocal(IResource.DEPTH_INFINITE, null);
							}
						} catch (CoreException coreEx) {
							// Throw the original exception to the caller
							log(coreEx.getStatus());
							throw e;
						}
						firstTime = false;
						// Fall through and the operation will be tried again
					} else {
						// User chose not to continue. Treat it as a cancel.
						throw new InterruptedException();
					}
				} else {
					throw e;
				}
			}
		}
	}
	
	private static boolean promptToRefresh(final Shell shell, final IResource[] resources, final IStatus status) {
		final boolean[] result = new boolean[] { false};
		Runnable runnable = new Runnable() {
			public void run() {
				Shell shellToUse = shell;
				if (shell == null) {
					shellToUse = new Shell(Display.getCurrent());
				}
				String question;
				if (resources.length == 1) {
					question = Policy.bind("CVSUIPlugin.refreshQuestion", status.getMessage(), resources[0].getFullPath().toString()); //$NON-NLS-1$
				} else {
					question = Policy.bind("CVSUIPlugin.refreshMultipleQuestion", status.getMessage()); //$NON-NLS-1$
				}
				result[0] = MessageDialog.openQuestion(shellToUse, Policy.bind("CVSUIPlugin.refreshTitle"), question); //$NON-NLS-1$
			}
		};
		Display.getDefault().syncExec(runnable);
		return result[0];
	}
	
	/**
	 * Creates a busy cursor and runs the specified runnable.
	 * May be called from a non-UI thread.
	 * 
	 * @param parent the parent Shell for the dialog
	 * @param cancelable if true, the dialog will support cancelation
	 * @param runnable the runnable
	 * 
	 * @exception InvocationTargetException when an exception is thrown from the runnable
	 * @exception InterruptedException when the progress monitor is cancelled
	 */
	public static void runWithProgress(Shell parent, boolean cancelable,
		final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		TeamUIPlugin.runWithProgress(parent, cancelable, runnable);
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
	 * Returns the image descriptor for the given image ID.
	 * Returns null if there is no such image.
	 */
	public ImageDescriptor getImageDescriptor(String id) {
		return (ImageDescriptor)imageDescriptors.get(id);
	}
	
	/**
	 * Returns the singleton plug-in instance.
	 * 
	 * @return the plugin instance
	 */
	public static CVSUIPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Returns the repository manager
	 * 
	 * @return the repository manager
	 */
	public RepositoryManager getRepositoryManager() {
		return repositoryManager;
	}
	
	/**
	 * Initializes the table of images used in this plugin.
	 */
	private void initializeImages() {
		URL baseURL = getDescriptor().getInstallURL();
	
		// objects
		createImageDescriptor(ICVSUIConstants.IMG_REPOSITORY, baseURL); 
		createImageDescriptor(ICVSUIConstants.IMG_REFRESH, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_NEWLOCATION, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_TAG, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_CLEAR, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_BRANCHES_CATEGORY, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_VERSIONS_CATEGORY, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_WIZBAN_BRANCH, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_WIZBAN_MERGE, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_WIZBAN_SHARE, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_MERGEABLE_CONFLICT, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_QUESTIONABLE, baseURL);
		createImageDescriptor(ICVSUIConstants.IMG_MERGED, baseURL);
		
		// special
		createImageDescriptor("glyphs/glyph1.gif", baseURL);  //$NON-NLS-1$
		createImageDescriptor("glyphs/glyph2.gif", baseURL);  //$NON-NLS-1$
		createImageDescriptor("glyphs/glyph3.gif", baseURL);  //$NON-NLS-1$
		createImageDescriptor("glyphs/glyph4.gif", baseURL);  //$NON-NLS-1$
		createImageDescriptor("glyphs/glyph5.gif", baseURL);  //$NON-NLS-1$
		createImageDescriptor("glyphs/glyph6.gif", baseURL);  //$NON-NLS-1$
		createImageDescriptor("glyphs/glyph7.gif", baseURL);  //$NON-NLS-1$
		createImageDescriptor("glyphs/glyph8.gif", baseURL);  //$NON-NLS-1$
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
	 * Initializes the preferences for this plugin if necessary.
	 */
	protected void initializePreferences() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(ICVSUIConstants.PREF_SHOW_COMMENTS, true);
		store.setDefault(ICVSUIConstants.PREF_SHOW_TAGS, true);
		store.setDefault(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES, CVSProviderPlugin.DEFAULT_PRUNE);
		store.setDefault(ICVSUIConstants.PREF_TIMEOUT, CVSProviderPlugin.DEFAULT_TIMEOUT);
		store.setDefault(ICVSUIConstants.PREF_SHOW_MODULES, false);
		store.setDefault(ICVSUIConstants.PREF_CONSIDER_CONTENTS, false);
		store.setDefault(ICVSUIConstants.PREF_SHOW_MARKERS, true);
		store.setDefault(ICVSUIConstants.PREF_COMPRESSION_LEVEL, CVSProviderPlugin.DEFAULT_COMPRESSION_LEVEL);
		store.setDefault(ICVSUIConstants.PREF_TEXT_KSUBST, CVSProviderPlugin.DEFAULT_TEXT_KSUBST_OPTION.toMode());
		store.setDefault(ICVSUIConstants.PREF_REPLACE_UNMANAGED, true);
		store.setDefault(ICVSUIConstants.PREF_CVS_RSH, CVSProviderPlugin.DEFAULT_CVS_RSH);
		store.setDefault(ICVSUIConstants.PREF_CVS_SERVER, CVSProviderPlugin.DEFAULT_CVS_SERVER);
		store.setDefault(ICVSUIConstants.PREF_PROMPT_ON_CHANGE_GRANULARITY, true);
		
		PreferenceConverter.setDefault(store, ICVSUIConstants.PREF_CONSOLE_COMMAND_COLOR, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, ICVSUIConstants.PREF_CONSOLE_MESSAGE_COLOR, new RGB(0, 0, 255));
		PreferenceConverter.setDefault(store, ICVSUIConstants.PREF_CONSOLE_ERROR_COLOR, new RGB(255, 0, 0));
		WorkbenchChainedTextFontFieldEditor.startPropagate(store, ICVSUIConstants.PREF_CONSOLE_FONT);
		store.setDefault(ICVSUIConstants.PREF_CONSOLE_AUTO_OPEN, false);
		
		store.setDefault(ICVSUIConstants.PREF_FILETEXT_DECORATION, CVSDecoratorConfiguration.DEFAULT_FILETEXTFORMAT);
		store.setDefault(ICVSUIConstants.PREF_FOLDERTEXT_DECORATION, CVSDecoratorConfiguration.DEFAULT_FOLDERTEXTFORMAT);
		store.setDefault(ICVSUIConstants.PREF_PROJECTTEXT_DECORATION, CVSDecoratorConfiguration.DEFAULT_PROJECTTEXTFORMAT);
		
		store.setDefault(ICVSUIConstants.PREF_ADDED_FLAG, CVSDecoratorConfiguration.DEFAULT_ADDED_FLAG);
		store.setDefault(ICVSUIConstants.PREF_DIRTY_FLAG, CVSDecoratorConfiguration.DEFAULT_DIRTY_FLAG);
				
		store.setDefault(ICVSUIConstants.PREF_SHOW_ADDED_DECORATION, true);
		store.setDefault(ICVSUIConstants.PREF_SHOW_HASREMOTE_DECORATION, true);
		store.setDefault(ICVSUIConstants.PREF_SHOW_DIRTY_DECORATION, false);
		store.setDefault(ICVSUIConstants.PREF_ADDED_FLAG, CVSDecoratorConfiguration.DEFAULT_ADDED_FLAG);
		store.setDefault(ICVSUIConstants.PREF_DIRTY_FLAG, CVSDecoratorConfiguration.DEFAULT_DIRTY_FLAG);
		store.setDefault(ICVSUIConstants.PREF_CALCULATE_DIRTY, true);
		store.setDefault(ICVSUIConstants.PREF_PROMPT_ON_MIXED_TAGS, true);
		store.setDefault(ICVSUIConstants.PREF_PROMPT_ON_SAVING_IN_SYNC, true);
		store.setDefault(ICVSUIConstants.PREF_SAVE_DIRTY_EDITORS, ICVSUIConstants.OPTION_PROMPT);
		
		// Forward the values to the CVS plugin
		CVSProviderPlugin.getPlugin().setPruneEmptyDirectories(store.getBoolean(ICVSUIConstants.PREF_PRUNE_EMPTY_DIRECTORIES));
		CVSProviderPlugin.getPlugin().setTimeout(store.getInt(ICVSUIConstants.PREF_TIMEOUT));
		CVSProviderPlugin.getPlugin().setCvsRshCommand(store.getString(ICVSUIConstants.PREF_CVS_RSH));
		CVSProviderPlugin.getPlugin().setCvsServer(store.getString(ICVSUIConstants.PREF_CVS_SERVER));
		CVSProviderPlugin.getPlugin().setQuietness(CVSPreferencesPage.getQuietnessOptionFor(store.getInt(ICVSUIConstants.PREF_QUIETNESS)));
		CVSProviderPlugin.getPlugin().setShowTasksOnAddAndDelete(store.getBoolean(ICVSUIConstants.PREF_SHOW_MARKERS));
		CVSProviderPlugin.getPlugin().setCompressionLevel(store.getInt(ICVSUIConstants.PREF_COMPRESSION_LEVEL));
		CVSProviderPlugin.getPlugin().setReplaceUnmanaged(store.getBoolean(ICVSUIConstants.PREF_REPLACE_UNMANAGED));
		CVSProviderPlugin.getPlugin().setDefaultTextKSubstOption(KSubstOption.fromMode(store.getString(ICVSUIConstants.PREF_TEXT_KSUBST)));
	}

	/**
	 * @see Plugin#startup()
	 */
	public void startup() throws CoreException {
		super.startup();
		Policy.localize("org.eclipse.team.internal.ccvs.ui.messages"); //$NON-NLS-1$

		CVSAdapterFactory factory = new CVSAdapterFactory();
		Platform.getAdapterManager().registerAdapters(factory, ICVSRemoteFile.class);
		Platform.getAdapterManager().registerAdapters(factory, ICVSRemoteFolder.class);
		Platform.getAdapterManager().registerAdapters(factory, ICVSRepositoryLocation.class);
		
		initializeImages();
		initializePreferences();
		repositoryManager = new RepositoryManager();
		
		// if the global ignores list is changed then update decorators.
		TeamUI.addPropertyChangeListener(listener);
		
		try {
			repositoryManager.startup();
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}
		
		Console.startup();
	}
	
	/**
	 * @see Plugin#shutdown()
	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		TeamUI.removePropertyChangeListener(listener);
		try {
			repositoryManager.shutdown();
		} catch (TeamException e) {
			throw new CoreException(e.getStatus());
		}

		Console.shutdown();
	}
	
	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		try {
			String property = event.getProperty();
			if(property.equals(ICVSUIConstants.PREF_SHOW_MARKERS)) {
				Boolean b = (Boolean)event.getNewValue();
				if(b.booleanValue() == true) {
					AddDeleteMoveListener.refreshAllMarkers();
				} else {
					AddDeleteMoveListener.clearAllCVSMarkers();
				}
			}
		} catch(CoreException e) {
			log(e.getStatus());
		}
	}
}