/************************************************************************
Copyright (c) 2000, 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.CapabilityRegistry;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.IViewRegistry;
import org.eclipse.ui.internal.registry.MarkerHelpRegistry;
import org.eclipse.ui.internal.registry.MarkerHelpRegistryReader;
import org.eclipse.ui.internal.registry.MarkerImageProviderRegistry;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.registry.PreferencePageRegistryReader;
import org.eclipse.ui.internal.registry.ProjectImageRegistry;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.internal.registry.ViewRegistryReader;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * This class represents the TOP of the workbench UI world
 * A plugin class is effectively an application wrapper
 * for a plugin & its classes. This class should be thought
 * of as the workbench UI's application class.
 *
 * This class is responsible for tracking various registries
 * font, preference, graphics, dialog store.
 *
 * This class is explicitly referenced by the 
 * workbench plugin's  "plugin.xml" and places it
 * into the UI start extension point of the main
 * overall application harness
 *
 * When is this class started?
 *      When the Application
 *      calls createExecutableExtension to create an executable
 *      instance of our workbench class.
 */
public class WorkbenchPlugin extends AbstractUIPlugin {
	// Default instance of the receiver
	private static WorkbenchPlugin inst;
	// Manager that maps resources to descriptors of editors to use
	private EditorRegistry editorRegistry;
	// Manager that maps project nature ids to images
	private ProjectImageRegistry projectImageRegistry;
	// Manager for the DecoratorManager
	private DecoratorManager decoratorManager;
	// Manager that maps markers to help context ids and resolutions
	private MarkerHelpRegistry markerHelpRegistry;
	// Manager for working sets (IWorkingSet)
	private WorkingSetManager workingSetManager;
	// Working set registry, stores working set dialogs
	private WorkingSetRegistry workingSetRegistry;	
	
	// Global workbench ui plugin flag. Only workbench implementation is allowed to use this flag
	// All other plugins, examples, or test cases must *not* use this flag.
	public static boolean DEBUG = false;

	/**
	 * The workbench plugin ID.
	 */
	public static String PI_WORKBENCH = PlatformUI.PLUGIN_ID;

	/**
	 * The character used to separate preference page category ids
	 */
	private static char PREFERENCE_PAGE_CATEGORY_SEPARATOR = '/';

	// Other data.
	private IWorkbench workbench;
	private PreferenceManager preferenceManager;
	private ViewRegistry viewRegistry;
	private PerspectiveRegistry perspRegistry;
	private CapabilityRegistry capabilityRegistry;
	private ActionSetRegistry actionSetRegistry;
	private SharedImages sharedImages;
	private MarkerImageProviderRegistry markerImageProviderRegistry;
	
	/**
	 * Create an instance of the WorkbenchPlugin.
	 * The workbench plugin is effectively the "application" for the workbench UI.
	 * The entire UI operates as a good plugin citizen.
	 */
	public WorkbenchPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		inst = this;
	}
				
	/**
	 * Creates an extension.  If the extension plugin has not
	 * been loaded a busy cursor will be activated during the duration of
	 * the load.
	 *
	 * @param element the config element defining the extension
	 * @param classAttribute the name of the attribute carrying the class
	 * @returns the extension object
	 */
	public static Object createExtension(final IConfigurationElement element, final String classAttribute) throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		IPluginDescriptor plugin = element.getDeclaringExtension().getDeclaringPluginDescriptor();
		if (plugin.isPluginActivated()) {
			return element.createExecutableExtension(classAttribute);
		} else {
			final Object[] ret = new Object[1];
			final CoreException[] exc = new CoreException[1];
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
	 * Returns the image registry for this plugin.
	 *
	 * Where are the images?  The images (typically gifs) are found in the 
	 * same plugins directory.
	 *
	 * @see JFace's ImageRegistry
	 *
	 * Note: The workbench uses the standard JFace ImageRegistry to track its images. In addition 
	 * the class WorkbenchGraphicResources provides convenience access to the graphics resources 
	 * and fast field access for some of the commonly used graphical images.
	 */
	protected ImageRegistry createImageRegistry() {
		return WorkbenchImages.getImageRegistry();
	}
	
	/**
	 * Returns the action set registry for the workbench.
	 *
	 * @return the workbench action set registry
	 */
	public ActionSetRegistry getActionSetRegistry() {
		if (actionSetRegistry == null) {
			actionSetRegistry = new ActionSetRegistry();
		}
		return actionSetRegistry;
	}
	/**
	 * Returns the capability registry for the workbench.
	 * 
	 * @return the capability registry
	 */
	public CapabilityRegistry getCapabilityRegistry() {
		if (capabilityRegistry == null) {
			capabilityRegistry = new CapabilityRegistry();
			capabilityRegistry.load();
		}
		return capabilityRegistry;
	}
	/**
	 * Returns the marker help registry for the workbench.
	 *
	 * @return the marker help registry
	 */
	public MarkerHelpRegistry getMarkerHelpRegistry() {
		if (markerHelpRegistry == null) {
			markerHelpRegistry = new MarkerHelpRegistry();
			new MarkerHelpRegistryReader().addHelp(markerHelpRegistry);
		}
		return markerHelpRegistry;
	}
	/* Return the default instance of the receiver. This represents the runtime plugin.
	 *
	 * @see AbstractPlugin for the typical implementation pattern for plugin classes.
	 */
	public static WorkbenchPlugin getDefault() {
		return inst;
	}
	/* Answer the manager that maps resource types to a the 
	 * description of the editor to use
	*/

	public IEditorRegistry getEditorRegistry() {
		if (editorRegistry == null) {
			editorRegistry = new EditorRegistry();
		}
		return editorRegistry;
	}
	/**
	 * Answer the element factory for an id.
	 */
	public IElementFactory getElementFactory(String targetID) {

		// Get the extension point registry.
		IExtensionPoint extensionPoint;
		extensionPoint = Platform.getPluginRegistry().getExtensionPoint(PI_WORKBENCH, IWorkbenchConstants.PL_ELEMENT_FACTORY);

		if (extensionPoint == null) {
			WorkbenchPlugin.log("Unable to find element factory. Extension point: " + IWorkbenchConstants.PL_ELEMENT_FACTORY + " not found"); //$NON-NLS-2$ //$NON-NLS-1$
			return null;
		}

		// Loop through the config elements.
		IConfigurationElement targetElement = null;
		IConfigurationElement[] configElements = extensionPoint.getConfigurationElements();
		for (int j = 0; j < configElements.length; j++) {
			String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
			if (strID.equals(targetID)) {
				targetElement = configElements[j];
				break;
			}
		}
		if (targetElement == null) {
			// log it since we cannot safely display a dialog.
			WorkbenchPlugin.log("Unable to find element factory: " + targetID); //$NON-NLS-1$
			return null;
		}

		// Create the extension.
		IElementFactory factory = null;
		try {
			factory = (IElementFactory) createExtension(targetElement, "class"); //$NON-NLS-1$
		} catch (CoreException e) {
			// log it since we cannot safely display a dialog.
			WorkbenchPlugin.log("Unable to create element factory.", e.getStatus()); //$NON-NLS-1$
			factory = null;
		}
		return factory;
	}
	/**
	 * Returns the marker image provider registry for the workbench.
	 *
	 * @return the marker image provider registry
	 */
	public MarkerImageProviderRegistry getMarkerImageProviderRegistry() {
		if (markerImageProviderRegistry == null)
			markerImageProviderRegistry = new MarkerImageProviderRegistry();
		return markerImageProviderRegistry;
	}
	/**
	 * Return the perspective registry.
	 */
	public IPerspectiveRegistry getPerspectiveRegistry() {
		if (perspRegistry == null) {
			perspRegistry = new PerspectiveRegistry();
			perspRegistry.load();
		}
		return perspRegistry;
	}
	/**
	 * Return the workspace used by the workbench
	 *
	 * This method is internal to the workbench and must not be called
	 * by any plugins.
	 */
	public static IWorkspace getPluginWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	/**
	 * Returns the working set manager
	 * 
	 * @return the working set manager
	 * @since 2.0
	 */
	public IWorkingSetManager getWorkingSetManager() {
		if (workingSetManager == null) {
			workingSetManager = new WorkingSetManager();
			workingSetManager.restoreState();			
		}
		return workingSetManager;
	}
	/**
	 * Returns the working set registry
	 * 
	 * @return the working set registry
	 * @since 2.0
	 */
	public WorkingSetRegistry getWorkingSetRegistry() {
		if (workingSetRegistry == null) {
			workingSetRegistry = new WorkingSetRegistry();
			workingSetRegistry.load();
		}
		return workingSetRegistry;
	}
	/*
	 * Get the preference manager.
	 */
	public PreferenceManager getPreferenceManager() {
		if (preferenceManager == null) {
			preferenceManager = new PreferenceManager(PREFERENCE_PAGE_CATEGORY_SEPARATOR);

			//Get the pages from the registry
			PreferencePageRegistryReader registryReader = new PreferencePageRegistryReader(getWorkbench());
			List pageContributions = registryReader.getPreferenceContributions(Platform.getPluginRegistry());

			//Add the contributions to the manager
			Iterator enum = pageContributions.iterator();
			while (enum.hasNext()) {
				preferenceManager.addToRoot((IPreferenceNode) enum.next());
			}
		}
		return preferenceManager;
	}
	/**
	 *Answers the manager that maps project nature ids to images
	 */

	public ProjectImageRegistry getProjectImageRegistry() {
		if (projectImageRegistry == null) {
			projectImageRegistry = new ProjectImageRegistry();
			projectImageRegistry.load();
		}
		return projectImageRegistry;
	}
	/**
	 * Returns the shared images for the workbench.
	 *
	 * @return the shared image manager
	 */
	public ISharedImages getSharedImages() {
		if (sharedImages == null)
			sharedImages = new SharedImages();
		return sharedImages;
	}
	/**
	 * Answer the view registry.
	 */
	public IViewRegistry getViewRegistry() {
		if (viewRegistry == null) {
			viewRegistry = new ViewRegistry();
			try {
				ViewRegistryReader reader = new ViewRegistryReader();
				reader.readViews(Platform.getPluginRegistry(), viewRegistry);
			} catch (CoreException e) {
				// cannot safely show a dialog so log it
				WorkbenchPlugin.log("Unable to read view registry.", e.getStatus()); //$NON-NLS-1$
			}
		}
		return viewRegistry;
	}
	/*
	 * Answer the workbench.
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}
	/** 
	 * Set default preference values.
	 * This method must be called whenever the preference store is initially loaded
	 * because the default values are not stored in the preference store.
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		
		JFacePreferences.setPreferenceStore(store);
		store.setDefault(IPreferenceConstants.AUTO_BUILD, true);
		store.setDefault(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD, false);
		store.setDefault(IPreferenceConstants.SAVE_INTERVAL, 5); //5 minutes
		store.setDefault(IPreferenceConstants.WELCOME_DIALOG, true);
		store.setDefault(IPreferenceConstants.REFRESH_WORKSPACE_ON_STARTUP, false);
		store.setDefault(IPreferenceConstants.EDITOR_LIST_PULLDOWN_ACTIVE, false);
		store.setDefault(IPreferenceConstants.EDITOR_LIST_DISPLAY_FULL_NAME, false);
		store.setDefault(IPreferenceConstants.CLOSE_EDITORS_ON_EXIT, false);
		store.setDefault(IPreferenceConstants.REUSE_EDITORS_BOOLEAN, false);
		store.setDefault(IPreferenceConstants.REUSE_DIRTY_EDITORS, true);
		store.setDefault(IPreferenceConstants.REUSE_EDITORS, 8);
		store.setDefault(IPreferenceConstants.OPEN_ON_SINGLE_CLICK, false);
		store.setDefault(IPreferenceConstants.SELECT_ON_HOVER, false);
		store.setDefault(IPreferenceConstants.OPEN_AFTER_DELAY, false);
		store.setDefault(IPreferenceConstants.RECENT_FILES, 4);
		store.setDefault(IPreferenceConstants.VIEW_TAB_POSITION, SWT.BOTTOM);
		store.setDefault(IPreferenceConstants.EDITOR_TAB_POSITION, SWT.TOP);
		store.setDefault(IPreferenceConstants.EDITOR_TABS_SPAN_MULTIPLE_LINES, false);
		store.setDefault(IPreferenceConstants.EDITOR_TAB_WIDTH_SCALAR, 3); // high
		store.setDefault(IPreferenceConstants.NUMBER_EDITOR_TABS, IPreferenceConstants.NUMBER_EDITOR_TABS_MAXIMUM);
		store.setDefault(IPreferenceConstants.OPEN_VIEW_MODE, IPreferenceConstants.OVM_EMBED);
		store.setDefault(IPreferenceConstants.OPEN_PERSP_MODE, IPreferenceConstants.OPM_ACTIVE_PAGE);
		store.setDefault(IPreferenceConstants.ENABLED_DECORATORS, ""); //$NON-NLS-1$
		store.setDefault(IPreferenceConstants.EDITOR_LIST_SELECTION_SCOPE, IPreferenceConstants.EDITOR_LIST_SET_PAGE_SCOPE); // Current Window
		store.setDefault(IPreferenceConstants.EDITOR_LIST_SORT_CRITERIA, IPreferenceConstants.EDITOR_LIST_NAME_SORT); // Name Sort
		store.setDefault(IPreferenceConstants.COLOR_ICONS, false);
		
		// Set the default behaviour for showing the task list when there are compiles errors in the build
		store.setDefault(IPreferenceConstants.SHOW_TASKS_ON_BUILD, true);

		// Set the default configuration for the key binding service
		store.setDefault(IWorkbenchConstants.ACCELERATOR_CONFIGURATION_ID, IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID);
		
		//Set the default error colour to red
		PreferenceConverter.setDefault(store,JFacePreferences.ERROR_COLOR, new RGB(255, 0, 0));
		//Set the default hyperlink line colour to dark blue
		PreferenceConverter.setDefault(store,JFacePreferences.HYPERLINK_COLOR, new RGB(0, 0, 153));
		//Set the default active hyperlink line colour to blue
		PreferenceConverter.setDefault(store,JFacePreferences.ACTIVE_HYPERLINK_COLOR, new RGB(0, 0, 255));
		
		
		// Temporary option to enable wizard for project capability
		store.setDefault("ENABLE_CONFIGURABLE_PROJECT_WIZARD", false); //$NON-NLS-1$
		// Temporary option to enable single click
		store.setDefault("SINGLE_CLICK_METHOD", OpenStrategy.DOUBLE_CLICK); //$NON-NLS-1$
		// Temporary option to enable cool bars
		store.setDefault("ENABLE_COOL_BARS", true); //$NON-NLS-1$
		// Temporary option to enable new menu organization
		store.setDefault("ENABLE_NEW_MENUS", true); //$NON-NLS-1$	
			

		FontRegistry registry = JFaceResources.getFontRegistry();
		initializeFont(JFaceResources.DIALOG_FONT, registry, store);
		initializeFont(JFaceResources.BANNER_FONT, registry, store);
		initializeFont(JFaceResources.HEADER_FONT, registry, store);
		initializeFont(JFaceResources.TEXT_FONT, registry, store);
			
		store.addPropertyChangeListener(new PlatformUIPreferenceListener());
	}

	private void initializeFont(String fontKey, FontRegistry registry, IPreferenceStore store) {

		FontData[] fontData = registry.getFontData(fontKey);
		PreferenceConverter.setDefault(store, fontKey, fontData);
	}
	/**
	 * Log the given status to the ISV log.
	 *
	 * When to use this:
	 *
	 *		This should be used when a PluginException or a
	 *		ExtensionException occur but for which an error
	 *		dialog cannot be safely shown.
	 *
	 *		If you can show an ErrorDialog then do so, and do
	 *		not call this method.
	 *
	 *		If you have a plugin exception or core exception in hand
	 *		call log(String, IStatus)
	 *
	 * This convenience method is for internal use by the Workbench only
	 * and must not be called outside the workbench.
	 *
	 * This method is supported in the event the log allows plugin related
	 * information to be logged (1FTTJKV). This would be done by this method.
	 *
	 * This method is internal to the workbench and must not be called
	 * by any plugins, or examples.
	 *
	 * @param message 	A high level UI message describing when the problem happened.
	 *
	 */

	public static void log(String message) {
		getDefault().getLog().log(StatusUtil.newStatus(Status.ERROR, message, null));
		System.err.println(message);
		//1FTTJKV: ITPCORE:ALL - log(status) does not allow plugin information to be recorded
	}
	/**
	 * Log the given status to the ISV log.
	 *
	 * When to use this:
	 *
	 *		This should be used when a PluginException or a
	 *		ExtensionException occur but for which an error
	 *		dialog cannot be safely shown.
	 *
	 *		If you can show an ErrorDialog then do so, and do
	 *		not call this method.
	 *
	 * This convenience method is for internal use by the workbench only
	 * and must not be called outside the workbench.
	 *
	 * This method is supported in the event the log allows plugin related
	 * information to be logged (1FTTJKV). This would be done by this method.
	 *
	 * This method is internal to the workbench and must not be called
	 * by any plugins, or examples.
	 *
	 * @param message 	A high level UI message describing when the problem happened.
	 *					May be null.
	 * @param status  	The status describing the problem.
	 *					Must not be null.
	 *
	 */

	public static void log(String message, IStatus status) {

		//1FTUHE0: ITPCORE:ALL - API - Status & logging - loss of semantic info

		if (message != null) {
			getDefault().getLog().log(StatusUtil.newStatus(IStatus.ERROR, message, null));
			System.err.println(message + "\nReason:"); //$NON-NLS-1$
		}

		getDefault().getLog().log(status);
		System.err.println(status.getMessage());

		//1FTTJKV: ITPCORE:ALL - log(status) does not allow plugin information to be recorded
	}
	public void setWorkbench(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
	}

	/**
	 * Get the decorator manager for the receiver
	 */

	public DecoratorManager getDecoratorManager() {
		if (this.decoratorManager == null) {
			this.decoratorManager = new DecoratorManager();
			this.decoratorManager.restoreListeners();
		}
		return decoratorManager;
	}

	public void startup() throws CoreException {
		/* The plugin org.eclipse.ui has being separed in
		   several plugins. Copy the state files from 
		   org.eclipse.ui to org.eclipse.ui.workbench */
		
		IPath locationPath = getStateLocation();
		File newLocation = locationPath.toFile();
		File oldLocation = new File(newLocation,"..//org.eclipse.ui");
		try {
			oldLocation = oldLocation.getCanonicalFile();
		} catch (IOException e) {}		
		String markerFileName = ".copiedStateFiles_Marker";
		File markerFile = new File(oldLocation,markerFileName);
		if(markerFile.exists())
			return;
			
		try {
			String list[] = newLocation.list();
			if(list != null && list.length != 0)
				return;

			String oldList[] = oldLocation.list();
			if(oldList == null || oldList.length == 0)
				return;

			byte b[] = new byte[1024];
			for (int i = 0; i < oldList.length; i++) {
				String string = oldList[i];
				try {
					File oldFile = new File(oldLocation,string);
					FileInputStream in = new FileInputStream(oldFile);
					FileOutputStream out = new FileOutputStream(new File(newLocation,string));
					int read = in.read(b);
					while(read >= 0) {
						out.write(b,0,read);
						read = in.read(b);
					}
					in.close();
					out.close();
					oldFile.delete();
				} catch (IOException e) {
					new File(newLocation,string).delete();
				}
			}
		} finally {
			try { 
				new FileOutputStream(markerFile).close(); 
			} catch (IOException e) {}
		}
	}
	
	/*
 	 * @see Plugin#shutdown() 
 	 */
	public void shutdown() throws CoreException {
		super.shutdown();
		if (this.decoratorManager != null) {
			this.decoratorManager.shutdown();
		}
	}

}