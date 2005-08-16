/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceManager;
import org.eclipse.ui.internal.intro.IIntroRegistry;
import org.eclipse.ui.internal.intro.IntroRegistry;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.operations.WorkbenchOperationSupport;
import org.eclipse.ui.internal.part.components.services.IStatusFactory;
import org.eclipse.ui.internal.part.services.StatusFactory;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.registry.PreferencePageRegistryReader;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.internal.themes.IThemeRegistry;
import org.eclipse.ui.internal.themes.ThemeRegistry;
import org.eclipse.ui.internal.themes.ThemeRegistryReader;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.SWTResourceUtil;
import org.eclipse.ui.internal.wizards.ExportWizardRegistry;
import org.eclipse.ui.internal.wizards.ImportWizardRegistry;
import org.eclipse.ui.internal.wizards.NewWizardRegistry;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

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
	
	private static final String LEFT_TO_RIGHT = "ltr"; //$NON-NLS-1$
	private static final String RIGHT_TO_LEFT = "rtl";//$NON-NLS-1$
	private static final String ORIENTATION_COMMAND_LINE = "-dir";//$NON-NLS-1$
	private static final String ORIENTATION_PROPERTY = "eclipse.orientation";//$NON-NLS-1$
	private static final String NL_USER_PROPERTY = "osgi.nl.user"; //$NON-NLS-1$
    
    // Default instance of the receiver
    private static WorkbenchPlugin inst;
    private static StatusFactory statusFactory;

    // Manager that maps resources to descriptors of editors to use
    private EditorRegistry editorRegistry;

    // Manager for the DecoratorManager
    private DecoratorManager decoratorManager;

    // Theme registry
    private ThemeRegistry themeRegistry;

    // Manager for working sets (IWorkingSet)
    private WorkingSetManager workingSetManager;

    // Working set registry, stores working set dialogs
    private WorkingSetRegistry workingSetRegistry;

    // The context within which this plugin was started.
    private BundleContext bundleContext;

    /**
     * Global workbench ui plugin flag. Only workbench implementation is allowed to use this flag
     * All other plugins, examples, or test cases must *not* use this flag.
     */
    public static boolean DEBUG = false;

    /**
     * The workbench plugin ID.
     * 
     * @issue we should just drop this constant and use PlatformUI.PLUGIN_ID instead
     */
    public static String PI_WORKBENCH = PlatformUI.PLUGIN_ID;

    /**
     * The character used to separate preference page category ids
     */
    public static char PREFERENCE_PAGE_CATEGORY_SEPARATOR = '/';

    // Other data.
    private WorkbenchPreferenceManager preferenceManager;

    private ViewRegistry viewRegistry;

    private PerspectiveRegistry perspRegistry;

    private ActionSetRegistry actionSetRegistry;

    private SharedImages sharedImages;

    /**
     * Information describing the product (formerly called "primary plugin"); lazily
     * initialized.
     * @since 3.0
     */
    private ProductInfo productInfo = null;

    private IntroRegistry introRegistry;
    
    private WorkbenchOperationSupport operationSupport;
        
    
    /**
     * Create an instance of the WorkbenchPlugin. The workbench plugin is
     * effectively the "application" for the workbench UI. The entire UI
     * operates as a good plugin citizen.
     */
    public WorkbenchPlugin() {
        super();
        inst = this;
    }

    /**
     * Unload all members.  This can be used to run a second instance of a workbench.
     * @since 3.0 
     */
    void reset() {
        editorRegistry = null;

        if (decoratorManager != null) {
            decoratorManager.dispose();
            decoratorManager = null;
        }

        ProgressManager.shutdownProgressManager();

        themeRegistry = null;
        if (workingSetManager != null) {
        	workingSetManager.dispose();
        	workingSetManager = null;
        }
        workingSetRegistry = null;

        preferenceManager = null;
        if (viewRegistry != null) {
            viewRegistry.dispose();
            viewRegistry = null;
        }
        if (perspRegistry != null) {
            perspRegistry.dispose();
            perspRegistry = null;
        }
        actionSetRegistry = null;
        sharedImages = null;

        productInfo = null;
        introRegistry = null;
        
        if (operationSupport != null) {
        	operationSupport.dispose();
        	operationSupport = null;
        }

        DEBUG = false;
         
    }

    /**
     * Creates an extension.  If the extension plugin has not
     * been loaded a busy cursor will be activated during the duration of
     * the load.
     *
     * @param element the config element defining the extension
     * @param classAttribute the name of the attribute carrying the class
     * @return the extension object
     * @throws CoreException if the extension cannot be created
     */
    public static Object createExtension(final IConfigurationElement element,
            final String classAttribute) throws CoreException {
        try {
            // If plugin has been loaded create extension.
            // Otherwise, show busy cursor then create extension.
            if (BundleUtility.isActivated(element.getDeclaringExtension()
                    .getNamespace())) {
                return element.createExecutableExtension(classAttribute);
            }
            final Object[] ret = new Object[1];
            final CoreException[] exc = new CoreException[1];
            BusyIndicator.showWhile(null, new Runnable() {
                public void run() {
                    try {
                        ret[0] = element
                                .createExecutableExtension(classAttribute);
                    } catch (CoreException e) {
                        exc[0] = e;
                    }
                }
            });
            if (exc[0] != null)
                throw exc[0];
            return ret[0];

        } catch (CoreException core) {
            throw core;
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, PI_WORKBENCH,
                    IStatus.ERROR, WorkbenchMessages.WorkbenchPlugin_extension,e));
        }
    }

    /**
     * Returns the image registry for this plugin.
     *
     * Where are the images?  The images (typically gifs) are found in the 
     * same plugins directory.
     *
     * @see ImageRegistry
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
     * Return the default instance of the receiver. This represents the runtime plugin.
     * @return WorkbenchPlugin
     * @see AbstractUIPlugin for the typical implementation pattern for plugin classes.
     */
    public static WorkbenchPlugin getDefault() {
        return inst;
    }

    /**
     * Answer the manager that maps resource types to a the 
     * description of the editor to use
     * @return IEditorRegistry the editor registry used
     * by this plug-in.
     */

    public IEditorRegistry getEditorRegistry() {
        if (editorRegistry == null) {
            editorRegistry = new EditorRegistry();
        }
        return editorRegistry;
    }

    /**
     * Answer the element factory for an id, or <code>null</code. if not found.
     * @param targetID
     * @return IElementFactory
     */
    public IElementFactory getElementFactory(String targetID) {

        // Get the extension point registry.
        IExtensionPoint extensionPoint;
        extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(
                PI_WORKBENCH, IWorkbenchConstants.PL_ELEMENT_FACTORY);

        if (extensionPoint == null) {
            WorkbenchPlugin
                    .log("Unable to find element factory. Extension point: " + IWorkbenchConstants.PL_ELEMENT_FACTORY + " not found"); //$NON-NLS-2$ //$NON-NLS-1$
            return null;
        }

        // Loop through the config elements.
        IConfigurationElement targetElement = null;
        IConfigurationElement[] configElements = extensionPoint
                .getConfigurationElements();
        for (int j = 0; j < configElements.length; j++) {
            String strID = configElements[j].getAttribute("id"); //$NON-NLS-1$
            if (targetID.equals(strID)) {
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
            WorkbenchPlugin.log(
                    "Unable to create element factory.", e.getStatus()); //$NON-NLS-1$
            factory = null;
        }
        return factory;
    }

    /**
     * Returns the presentation factory with the given id, or <code>null</code> if not found.
     * @param targetID The id of the presentation factory to use.
     * @return AbstractPresentationFactory or <code>null</code>
     * if not factory matches that id.
     */
    public AbstractPresentationFactory getPresentationFactory(String targetID) {
        Object o = createExtension(
                IWorkbenchConstants.PL_PRESENTATION_FACTORIES,
                "factory", targetID); //$NON-NLS-1$
        if (o instanceof AbstractPresentationFactory) {
            return (AbstractPresentationFactory) o;
        }
        WorkbenchPlugin
                .log("Error creating presentation factory: " + targetID + " -- class is not an AbstractPresentationFactory"); //$NON-NLS-1$ //$NON-NLS-2$
        return null;
    }

    /**
     * Looks up the configuration element with the given id on the given extension point
     * and instantiates the class specified by the class attributes.
     * 
     * @param extensionPointId the extension point id (simple id)
     * @param elementName the name of the configuration element, or <code>null</code>
     *   to match any element
     * @param targetID the target id
     * @return the instantiated extension object, or <code>null</code> if not found
     */
    private Object createExtension(String extensionPointId, String elementName,
            String targetID) {
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
                .getExtensionPoint(PI_WORKBENCH, extensionPointId);
        if (extensionPoint == null) {
            WorkbenchPlugin
                    .log("Unable to find extension. Extension point: " + extensionPointId + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

        // Loop through the config elements.
        IConfigurationElement targetElement = null;
        IConfigurationElement[] elements = extensionPoint
                .getConfigurationElements();
        for (int j = 0; j < elements.length; j++) {
            IConfigurationElement element = elements[j];
            if (elementName == null || elementName.equals(element.getName())) {
                String strID = element.getAttribute("id"); //$NON-NLS-1$
                if (targetID.equals(strID)) {
                    targetElement = element;
                    break;
                }
            }
        }
        if (targetElement == null) {
            // log it since we cannot safely display a dialog.
            WorkbenchPlugin.log("Unable to find extension: " + targetID //$NON-NLS-1$
                    + " in extension point: " + extensionPointId); //$NON-NLS-1$ 
            return null;
        }

        // Create the extension.
        try {
            return createExtension(targetElement, "class"); //$NON-NLS-1$
        } catch (CoreException e) {
            // log it since we cannot safely display a dialog.
            WorkbenchPlugin.log("Unable to create extension: " + targetID //$NON-NLS-1$
                    + " in extension point: " + extensionPointId //$NON-NLS-1$
                    + ", status: ", e.getStatus()); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Return the perspective registry.
     * @return IPerspectiveRegistry. The registry for the receiver.
     */
    public IPerspectiveRegistry getPerspectiveRegistry() {
        if (perspRegistry == null) {
            perspRegistry = new PerspectiveRegistry();
            perspRegistry.load();
        }
        return perspRegistry;
    }

    /**
     * Returns the working set manager
     * 
     * @return the working set manager
     * @since 2.0
     */
    public IWorkingSetManager getWorkingSetManager() {
        if (workingSetManager == null) {
            workingSetManager = new WorkingSetManager(bundleContext);
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

    /**
     * Returns the introduction registry.
     *
     * @return the introduction registry.
     * @since 3.0
     */
    public IIntroRegistry getIntroRegistry() {
        if (introRegistry == null) {
            introRegistry = new IntroRegistry();
        }
        return introRegistry;
    }
    
    /**
	 * Returns the operation support.
	 * 
	 * @return the workbench operation support.
	 * @since 3.1
	 */
    public IWorkbenchOperationSupport getOperationSupport() {
        if (operationSupport == null) {
        	operationSupport = new WorkbenchOperationSupport();
        }
        return operationSupport;
    }
    

    /**
     * Get the preference manager.
     * @return PreferenceManager the preference manager for
     * the receiver.
     */
    public PreferenceManager getPreferenceManager() {
        if (preferenceManager == null) {
            preferenceManager = new WorkbenchPreferenceManager(
                    PREFERENCE_PAGE_CATEGORY_SEPARATOR);

            //Get the pages from the registry
            PreferencePageRegistryReader registryReader = new PreferencePageRegistryReader(
                    getWorkbench());
            registryReader
                    .loadFromRegistry(Platform.getExtensionRegistry());
            preferenceManager.addPages(registryReader.getTopLevelNodes());
           
        }
        return preferenceManager;
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
     * Returns the theme registry for the workbench.
     * 
     * @return the theme registry
     */
    public IThemeRegistry getThemeRegistry() {
        if (themeRegistry == null) {
            themeRegistry = new ThemeRegistry();
            ThemeRegistryReader reader = new ThemeRegistryReader();
            reader.readThemes(Platform.getExtensionRegistry(),
                    themeRegistry);
        }
        return themeRegistry;
    }

    /**
     * Answer the view registry.
     * @return IViewRegistry the view registry for the
     * receiver.
     */
    public IViewRegistry getViewRegistry() {
        if (viewRegistry == null) {
            viewRegistry = new ViewRegistry();
        }
        return viewRegistry;
    }

    /**
     * Answer the workbench.
     * @deprecated Use <code>PlatformUI.getWorkbench()</code> instead.
     */
    public IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    /** 
     * Set default preference values.
     * This method must be called whenever the preference store is initially loaded
     * because the default values are not stored in the preference store.
     */
    protected void initializeDefaultPreferences(IPreferenceStore store) {
        // Do nothing.  This should not be called.
        // Prefs are initialized in WorkbenchPreferenceInitializer.
    }

    /**
     * Logs the given message to the platform log.
     * 
     * If you have an exception in hand, call log(String, Throwable) instead.
     * 
     * If you have a status object in hand call log(String, IStatus) instead.
     * 
     * This convenience method is for internal use by the Workbench only and
     * must not be called outside the Workbench.
     * 
     * @param message
     *            A high level UI message describing when the problem happened.
     */
    public static void log(String message) {
        getDefault().getLog().log(
                StatusUtil.newStatus(IStatus.ERROR, message, null));    
    }

    public static IStatusFactory getStatusFactory() {
        return getDefault().statusFactory();
    }
    
    public static IStatus getStatus(Throwable t) {
        return getStatusFactory().newError(t);
    }
    
    public static void log(Throwable t) {
        getDefault().getLog().log(getStatus(t));
    }
    
    public static IStatus getStatus(String message, Throwable t) {
        return getStatusFactory().newError(message, t);
    }
    
    /**
     * Logs the given message and throwable to the platform log.
     * 
     * If you have a status object in hand call log(String, IStatus) instead.
     * 
     * This convenience method is for internal use by the Workbench only and
     * must not be called outside the Workbench.
     * 
     * @param message
     *            A high level UI message describing when the problem happened.
     * @param t
     *            The throwable from where the problem actually occurred.
     */
    public static void log(String message, Throwable t) {
        IStatus status = StatusUtil.newStatus(IStatus.ERROR, message, t);
        log(message, status);
    }
    
    /**
     * Logs the given throwable to the platform log, indicating the class and
     * method from where it is being logged (this is not necessarily where it
     * occurred).
     * 
     * This convenience method is for internal use by the Workbench only and
     * must not be called outside the Workbench.
     * 
     * @param clazz
     *            The calling class.
     * @param methodName
     *            The calling method name.
     * @param t
     *            The throwable from where the problem actually occurred.
     */
    public static void log(Class clazz, String methodName, Throwable t) {
        String msg = MessageFormat.format("Exception in {0}.{1}: {2}", //$NON-NLS-1$
                new Object[] { clazz.getName(), methodName, t });
        log(msg, t);
    }
    
    /**
     * Logs the given message and status to the platform log.
     * 
     * This convenience method is for internal use by the Workbench only and
     * must not be called outside the Workbench.
     * 
     * @param message
     *            A high level UI message describing when the problem happened.
     *            May be <code>null</code>.
     * @param status
     *            The status describing the problem. Must not be null.
     */
    public static void log(String message, IStatus status) {

        //1FTUHE0: ITPCORE:ALL - API - Status & logging - loss of semantic info

        if (message != null) {
            getDefault().getLog().log(
                    StatusUtil.newStatus(IStatus.ERROR, message, null));
        }

        getDefault().getLog().log(status);
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }
    
    /**
     * Get the decorator manager for the receiver
     * @return DecoratorManager the decorator manager
     * for the receiver.
     */
    public DecoratorManager getDecoratorManager() {
        if (this.decoratorManager == null) {
            this.decoratorManager = new DecoratorManager();
        }
        return decoratorManager;
    }

    /*
     *  (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        bundleContext = context;
        
        JFaceUtil.initializeJFace();
		
		 Window.setDefaultOrientation(getDefaultOrientation());

        // Start the UI plugin so that it can install the callback in PrefUtil,
        // which needs to be done as early as possible, before the workbench
        // accesses any API preferences.
        Bundle uiBundle = Platform.getBundle(PlatformUI.PLUGIN_ID); 
        uiBundle.start();
		/*
		 * DO NOT RUN ANY OTHER CODE AFTER THIS LINE.  If you do, then you are
		 * likely to cause a deadlock in class loader code.  Please see Bug 86450
		 * for more information.
		 */

    }
	/**
     * Get the default orientation from the command line
     * arguments. If there are no arguments imply the 
     * orientation.
	 * @return int
	 * @see SWT#NONE
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 */
    private int getDefaultOrientation() {
		
		String[] commandLineArgs = Platform.getCommandLineArgs();
		
		int orientation = getCommandLineOrientation(commandLineArgs);
		
		if(orientation != SWT.NONE)
			return orientation;
		
		orientation = getSystemPropertyOrientation();
		
		if(orientation != SWT.NONE)
			return orientation;

		return checkCommandLineLocale(); //Use the default value if there is nothing specified
	}
	
	/**
	 * Check to see if the command line parameter for -nl
	 * has been set. If so imply the orientation from this 
	 * specified Locale. If it is a bidirectional Locale
	 * return SWT#RIGHT_TO_LEFT.
	 * If it has not been set or has been set to 
	 * a unidirectional Locale then return SWT#NONE.
	 * 
	 * Locale is determined differently by different JDKs 
	 * and may not be consistent with the users expectations.
	 * 

	 * @return int
	 * @see SWT#NONE
	 * @see SWT#RIGHT_TO_LEFT
	 */
	private int checkCommandLineLocale() {
		
		//Check if the user property is set. If not do not
		//rely on the vm.
		if(System.getProperty(NL_USER_PROPERTY) == null)
			return SWT.NONE;
		
		Locale locale = Locale.getDefault();
		String lang = locale.getLanguage();

		if ("iw".equals(lang) || "ar".equals(lang) || "fa".equals(lang) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			|| "ur".equals(lang)) //$NON-NLS-1$
			return SWT.RIGHT_TO_LEFT;
			
		return SWT.NONE;
	}

	/**
	 * Check to see if the orientation was set in the
	 * system properties. If there is no orientation 
	 * specified return SWT#NONE.
	 * @return int
	 * @see SWT#NONE
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 */
	private int getSystemPropertyOrientation() {
		String orientation = System.getProperty(ORIENTATION_PROPERTY);
		if(RIGHT_TO_LEFT.equals(orientation))
			return SWT.RIGHT_TO_LEFT;
		if(LEFT_TO_RIGHT.equals(orientation))
			return SWT.LEFT_TO_RIGHT;
		return SWT.NONE;
	}

	/**
	 * Find the orientation in the commandLineArgs. If there
	 * is no orientation specified return SWT#NONE.
	 * @param commandLineArgs
	 * @return int
	 * @see SWT#NONE
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 */
	private int getCommandLineOrientation(String[] commandLineArgs) {
		//Do not process the last one as it will never have a parameter
		for (int i = 0; i < commandLineArgs.length - 1; i++) {
			if(commandLineArgs[i].equalsIgnoreCase(ORIENTATION_COMMAND_LINE)){
				String orientation = commandLineArgs[i+1];
				if(orientation.equals(RIGHT_TO_LEFT)){
					System.setProperty(ORIENTATION_PROPERTY,RIGHT_TO_LEFT);
					return SWT.RIGHT_TO_LEFT;
				}
				if(orientation.equals(LEFT_TO_RIGHT)){
					System.setProperty(ORIENTATION_PROPERTY,LEFT_TO_RIGHT);
					return SWT.LEFT_TO_RIGHT;
				}
			}
		}
		
		return SWT.NONE;
	}

	/**
     * Return an array of all bundles contained in this workbench.
     * 
     * @return an array of bundles in the workbench or an empty array if none
     * @since 3.0
     */
    public Bundle[] getBundles() {
        return bundleContext == null ? new Bundle[0] : bundleContext
                .getBundles();
    }
    
    /**
     * Returns the bundle context associated with the workbench plug-in.
     * 
     * @return the bundle context
     * @since 3.1
     */
    public BundleContext getBundleContext() {
    	return bundleContext;
    }

    /**
     * Returns the application name.
     * <p>
     * Note this is never shown to the user.
     * It is used to initialize the SWT Display.
     * On Motif, for example, this can be used
     * to set the name used for resource lookup.
     * </p>
     *
     * @return the application name, or <code>null</code>
     * @see org.eclipse.swt.widgets.Display#setAppName
     * @since 3.0
     */
    public String getAppName() {
        return getProductInfo().getAppName();
    }

    /**
     * Returns the name of the product.
     * 
     * @return the product name, or <code>null</code> if none
     * @since 3.0
     */
    public String getProductName() {
        return getProductInfo().getProductName();
    }

    /**
     * Returns the image descriptors for the window image to use for this product.
     * 
     * @return an array of the image descriptors for the window image, or
     *         <code>null</code> if none
     * @since 3.0
     */
    public ImageDescriptor[] getWindowImages() {
        return getProductInfo().getWindowImages();
    }

    /**
     * Returns an instance that describes this plugin's product (formerly "primary
     * plugin").
     * @return ProductInfo the product info for the receiver
     */
    private ProductInfo getProductInfo() {
        if (productInfo == null)
            productInfo = new ProductInfo(Platform.getProduct());
        return productInfo;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        if (workingSetManager != null) {
        	workingSetManager.dispose();
        	workingSetManager= null;
        }       
        SWTResourceUtil.shutdown();
    } 
    
    /**
     * Return the new wizard registry.
     * 
     * @return the new wizard registry
     * @since 3.1
     */
    public IWizardRegistry getNewWizardRegistry() {
    	return NewWizardRegistry.getInstance();
    }
    
    /**
     * Return the import wizard registry.
     * 
     * @return the import wizard registry
     * @since 3.1
     */
    public IWizardRegistry getImportWizardRegistry() {
    	return ImportWizardRegistry.getInstance();
    }
    
    /**
     * Return the export wizard registry.
     * 
     * @return the export wizard registry
     * @since 3.1
     */
    public IWizardRegistry getExportWizardRegistry() {
    	return ExportWizardRegistry.getInstance();
    }

    IStatusFactory statusFactory() {
        if (statusFactory == null) {
            statusFactory = new StatusFactory(getBundle());
        }
        
        return statusFactory;
    }
    
    /**
     * FOR INTERNAL WORKBENCH USE ONLY. 
     * 
     * Returns the path to a location in the file system that can be used 
     * to persist/restore state between workbench invocations.
     * If the location did not exist prior to this call it will  be created.
     * Returns <code>null</code> if no such location is available.
     * 
     * @return path to a location in the file system where this plug-in can
     * persist data between sessions, or <code>null</code> if no such
     * location is available.
     * @since 3.1
     */
    public IPath getDataLocation() {
        try {
            return getStateLocation();
        } catch (IllegalStateException e) {
            // This occurs if -data=@none is explicitly specified, so ignore this silently.
            // Is this OK? See bug 85071.
            return null;
        }
    }

	/* package */ void addBundleListener(BundleListener bundleListener) {
		bundleContext.addBundleListener(bundleListener);
	}    

	/* package */ void removeBundleListener(BundleListener bundleListener) {
		bundleContext.removeBundleListener(bundleListener);
	}    
	
	/* package */ int getBundleCount() {
		return bundleContext.getBundles().length;
	}
	
	/* package */ OutputStream getSplashStream() {
		// assumes the output stream is available as a service
		// see EclipseStarter.publishSplashScreen
		ServiceReference[] ref;
		try {
			ref = bundleContext.getServiceReferences(OutputStream.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			return null;
		}
		if(ref==null)
			return null;
		for (int i = 0; i < ref.length; i++) {
			String name = (String) ref[i].getProperty("name"); //$NON-NLS-1$
			if (name != null && name.equals("splashstream")) {  //$NON-NLS-1$
				Object result = bundleContext.getService(ref[i]);
				bundleContext.ungetService(ref[i]);
				return (OutputStream) result;
			}
		}
		return null;
	}
	
}
