package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.FontData;
import java.io.*;
import java.util.*;

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

	// Global workbench ui plugin flag. Only workbench implementation is allowed to use this flag
	// All other plugins, examples, or test cases must *not* use this flag.
	public static boolean DEBUG = false;

	/**
	 * The workbench plugin ID.
	 */
	public static String PI_WORKBENCH = IWorkbenchConstants.PLUGIN_ID;

	/**
	 * The character used to separate preference page category ids
	 */
	private static char PREFERENCE_PAGE_CATEGORY_SEPARATOR = '/';

	// Other data.
	private IWorkbench workbench;
	private PreferenceManager preferenceManager;
	private ViewRegistry viewRegistry;
	private PerspectiveRegistry perspRegistry;
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
	public static Object createExtension(
		final IConfigurationElement element,
		final String classAttribute)
		throws CoreException {
		// If plugin has been loaded create extension.
		// Otherwise, show busy cursor then create extension.
		IPluginDescriptor plugin =
			element.getDeclaringExtension().getDeclaringPluginDescriptor();
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
		extensionPoint =
			Platform.getPluginRegistry().getExtensionPoint(
				PI_WORKBENCH,
				IWorkbenchConstants.PL_ELEMENT_FACTORY);

		if (extensionPoint == null) {
			WorkbenchPlugin.log(
				"Unable to find element factory. Extension point: "
					+ IWorkbenchConstants.PL_ELEMENT_FACTORY
					+ " not found");
			//$NON-NLS-2$ //$NON-NLS-1$
			return null;
		}

		// Loop through the config elements.
		IConfigurationElement targetElement = null;
		IConfigurationElement[] configElements =
			extensionPoint.getConfigurationElements();
		for (int j = 0; j < configElements.length; j++) {
			String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
			if (strID.equals(targetID)) {
				targetElement = configElements[j];
				break;
			}
		}
		if (targetElement == null) {
			// log it since we cannot safely display a dialog.
			WorkbenchPlugin.log("Unable to find element factory: " + targetID);
			//$NON-NLS-1$
			return null;
		}

		// Create the extension.
		IElementFactory factory = null;
		try {
			factory = (IElementFactory) createExtension(targetElement, "class");
			//$NON-NLS-1$
		} catch (CoreException e) {
			// log it since we cannot safely display a dialog.
			WorkbenchPlugin.log("Unable to create element factory.", e.getStatus());
			//$NON-NLS-1$
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
			IPath path = WorkbenchPlugin.getDefault().getStateLocation();
			File folder = path.toFile();
			perspRegistry = new PerspectiveRegistry(folder);
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
	/*
	 * Get the preference manager.
	 */
	public PreferenceManager getPreferenceManager() {
		if (preferenceManager == null) {
			preferenceManager = new PreferenceManager(PREFERENCE_PAGE_CATEGORY_SEPARATOR);

			//Get the pages from the registry
			PreferencePageRegistryReader registryReader =
				new PreferencePageRegistryReader(getWorkbench());
			List pageContributions =
				registryReader.getPreferenceContributions(Platform.getPluginRegistry());

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
				WorkbenchPlugin.log("Unable to read view registry.", e.getStatus());
				//$NON-NLS-1$
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
		store.setDefault(IPreferenceConstants.AUTO_BUILD, true);
		store.setDefault(IPreferenceConstants.SAVE_ALL_BEFORE_BUILD, false);
		store.setDefault(IPreferenceConstants.WELCOME_DIALOG, true);
		store.setDefault(IWorkbenchPreferenceConstants.LINK_NAVIGATOR_TO_EDITOR, true);
		store.setDefault(IPreferenceConstants.VIEW_TAB_POSITION, SWT.BOTTOM);
		store.setDefault(IPreferenceConstants.EDITOR_TAB_POSITION, SWT.TOP);
		store.setDefault(
			IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE,
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE);
		store.setDefault(
			IWorkbenchPreferenceConstants.SHIFT_OPEN_NEW_PERSPECTIVE,
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW);
		store.setDefault(
			IWorkbenchPreferenceConstants.ALTERNATE_OPEN_NEW_PERSPECTIVE,
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE);
		store.setDefault(
			IWorkbenchPreferenceConstants.PROJECT_OPEN_NEW_PERSPECTIVE,
			IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE);
		FontRegistry registry = JFaceResources.getFontRegistry();
		String locale = Locale.getDefault().toString();
		initializeFont(JFaceResources.DEFAULT_FONT,  registry, store);
		initializeFont(JFaceResources.DIALOG_FONT,  registry, store);
		initializeFont(JFaceResources.BANNER_FONT,  registry, store);
		initializeFont(JFaceResources.VIEWER_FONT,  registry, store);
		initializeFont(JFaceResources.TEXT_FONT,  registry, store);
		initializeFont(JFaceResources.WINDOW_FONT, registry, store);
	}

	private void initializeFont(
		String fontKey,
		FontRegistry registry,
		IPreferenceStore store) {
		FontData[] fontData = registry.getFontData(JFaceResources.DEFAULT_FONT);

		PreferenceConverter.setDefault(
			store,
			fontKey,
			fontData[0]);
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
		getDefault().getLog().log(
			StatusUtil.newStatus(Status.ERROR, null, message, null));
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
			getDefault().getLog().log(
				StatusUtil.newStatus(IStatus.ERROR, null, message, null));
			System.err.println(message + "\nReason:"); //$NON-NLS-1$
		}

		getDefault().getLog().log(status);
		System.err.println(status.getMessage());

		//1FTTJKV: ITPCORE:ALL - log(status) does not allow plugin information to be recorded
	}
	public void setWorkbench(IWorkbench aWorkbench) {
		this.workbench = aWorkbench;
	}
}