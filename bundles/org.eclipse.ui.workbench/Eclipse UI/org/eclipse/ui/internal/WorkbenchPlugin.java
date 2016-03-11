/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 400714, 441267, 441184, 445723, 445724, 472654, 481608
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 489250
 *******************************************************************************/

package org.eclipse.ui.internal;

import com.ibm.icu.text.MessageFormat;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.commands.internal.ICommandHelpService;
import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.help.EHelpService;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceManager;
import org.eclipse.ui.internal.help.CommandHelpServiceImpl;
import org.eclipse.ui.internal.help.HelpServiceImpl;
import org.eclipse.ui.internal.intro.IIntroRegistry;
import org.eclipse.ui.internal.intro.IntroRegistry;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.operations.WorkbenchOperationSupport;
import org.eclipse.ui.internal.progress.ProgressManager;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.registry.PreferencePageRegistryReader;
import org.eclipse.ui.internal.registry.ViewRegistry;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;
import org.eclipse.ui.internal.themes.IThemeRegistry;
import org.eclipse.ui.internal.themes.ThemeRegistry;
import org.eclipse.ui.internal.themes.ThemeRegistryReader;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.internal.wizards.ExportWizardRegistry;
import org.eclipse.ui.internal.wizards.ImportWizardRegistry;
import org.eclipse.ui.internal.wizards.NewWizardRegistry;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.testing.TestableObject;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.util.tracker.ServiceTracker;

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

	/**
	 * Splash shell constant.
	 */
	private static final String DATA_SPLASH_SHELL = "org.eclipse.ui.workbench.splashShell"; //$NON-NLS-1$

	/**
	 * The OSGi splash property.
	 *
	 * @sicne 3.4
	 */
	private static final String PROP_SPLASH_HANDLE = "org.eclipse.equinox.launcher.splash.handle"; //$NON-NLS-1$

	private static final String LEFT_TO_RIGHT = "ltr"; //$NON-NLS-1$
	private static final String RIGHT_TO_LEFT = "rtl";//$NON-NLS-1$
	private static final String ORIENTATION_COMMAND_LINE = "-dir";//$NON-NLS-1$
	private static final String ORIENTATION_PROPERTY = "eclipse.orientation";//$NON-NLS-1$
	private static final String NL_USER_PROPERTY = "osgi.nl.user"; //$NON-NLS-1$
	private static final String BIDI_COMMAND_LINE = "-bidi";//$NON-NLS-1$
	private static final String BIDI_SUPPORT_OPTION = "on";//$NON-NLS-1$
	private static final String BIDI_TEXTDIR_OPTION = "textDir";//$NON-NLS-1$


    // Default instance of the receiver
    private static WorkbenchPlugin inst;

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

    // The set of currently starting bundles
	private Collection<Bundle> startingBundles = new HashSet<>();

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
	private BundleListener bundleListener;

	private IEclipseContext e4Context;

	private ServiceTracker debugTracker = null;

	private ServiceTracker testableTracker = null;

	private EHelpService helpService;

	private ICommandHelpService commandHelpService;

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
			decoratorManager.shutdown();
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
			// nothing to dispose for viewRegistry
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

		helpService = null;
		commandHelpService = null;

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
			if (BundleUtility.isActivated(element.getContributor().getName())) {
                return element.createExecutableExtension(classAttribute);
            }
            final Object[] ret = new Object[1];
            final CoreException[] exc = new CoreException[1];
            BusyIndicator.showWhile(null, new Runnable() {
                @Override
				public void run() {
                    try {
                        ret[0] = element
                                .createExecutableExtension(classAttribute);
                    } catch (CoreException e) {
                        exc[0] = e;
                    }
                }
            });
            if (exc[0] != null) {
				throw exc[0];
			}
            return ret[0];

        } catch (CoreException core) {
            throw core;
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, PI_WORKBENCH,
                    IStatus.ERROR, WorkbenchMessages.WorkbenchPlugin_extension,e));
        }
    }

    /**
	 * Answers whether the provided element either has an attribute with the
	 * given name or a child element with the given name with an attribute
	 * called class.
	 *
	 * @param element
	 *            the element to test
	 * @param extensionName
	 *            the name of the extension to test for
	 * @return whether or not the extension is declared
	 * @since 3.3
	 */
	public static boolean hasExecutableExtension(IConfigurationElement element,
			String extensionName) {

		if (element.getAttribute(extensionName) != null)
			return true;
		String elementText = element.getValue();
		if (elementText != null && !elementText.equals("")) //$NON-NLS-1$
			return true;
		IConfigurationElement [] children = element.getChildren(extensionName);
		if (children.length == 1) {
			if (children[0].getAttribute(IWorkbenchRegistryConstants.ATT_CLASS) != null)
				return true;
		}
		return false;
	}

	/**
	 * Checks to see if the provided element has the syntax for an executable
	 * extension with a given name that resides in a bundle that is already
	 * active. Determining the bundle happens in one of two ways:<br/>
	 * <ul>
	 * <li>The element has an attribute with the specified name or element text
	 * in the form <code>bundle.id/class.name[:optional attributes]</code></li>
	 * <li>The element has a child element with the specified name that has a
	 * <code>plugin</code> attribute</li>
	 * </ul>
	 *
	 * @param element
	 *            the element to test
	 * @param extensionName
	 *            the name of the extension to test for
	 * @return whether or not the bundle expressed by the above criteria is
	 *         active. If the bundle cannot be determined then the state of the
	 *         bundle that declared the element is returned.
	 * @since 3.3
	 */
	public static boolean isBundleLoadedForExecutableExtension(
			IConfigurationElement element, String extensionName) {
		Bundle bundle = getBundleForExecutableExtension(element, extensionName);

		if (bundle == null)
			return true;
		return bundle.getState() == Bundle.ACTIVE;
	}

	/**
	 * Returns the bundle that contains the class referenced by an executable
	 * extension. Determining the bundle happens in one of two ways:<br/>
	 * <ul>
	 * <li>The element has an attribute with the specified name or element text
	 * in the form <code>bundle.id/class.name[:optional attributes]</code></li>
	 * <li>The element has a child element with the specified name that has a
	 * <code>plugin</code> attribute</li>
	 * </ul>
	 *
	 * @param element
	 *            the element to test
	 * @param extensionName
	 *            the name of the extension to test for
	 * @return the bundle referenced by the extension. If that bundle cannot be
	 *         determined the bundle that declared the element is returned. Note
	 *         that this may be <code>null</code>.
	 * @since 3.3
	 */
	public static Bundle getBundleForExecutableExtension(IConfigurationElement element, String extensionName) {
		// this code is derived heavily from
		// ConfigurationElement.createExecutableExtension.
		String prop = null;
		String executable;
		String contributorName = null;
		int i;

		if (extensionName != null)
			prop = element.getAttribute(extensionName);
		else {
			// property not specified, try as element value
			prop = element.getValue();
			if (prop != null) {
				prop = prop.trim();
				if (prop.equals("")) //$NON-NLS-1$
					prop = null;
			}
		}

		if (prop == null) {
			// property not defined, try as a child element
			IConfigurationElement[] exec = element.getChildren(extensionName);
			if (exec.length != 0)
				contributorName = exec[0].getAttribute("plugin"); //$NON-NLS-1$
		} else {
			// simple property or element value, parse it into its components
			i = prop.indexOf(':');
			if (i != -1)
				executable = prop.substring(0, i).trim();
			else
				executable = prop;

			i = executable.indexOf('/');
			if (i != -1)
				contributorName = executable.substring(0, i).trim();

		}

		if (contributorName == null)
			contributorName = element.getContributor().getName();

		return Platform.getBundle(contributorName);
	}

    /**
	 * Returns the image registry for this plugin.
	 *
	 * Where are the images? The images (typically gifs) are found in the same
	 * plugins directory.
	 *
	 * @see ImageRegistry
	 *
	 * Note: The workbench uses the standard JFace ImageRegistry to track its
	 * images. In addition the class WorkbenchGraphicResources provides
	 * convenience access to the graphics resources and fast field access for
	 * some of the commonly used graphical images.
	 */
    @Override
	protected ImageRegistry createImageRegistry() {
        return WorkbenchImages.getImageRegistry();
    }

    /**
     * Returns the action set registry for the workbench.
     *
     * @return the workbench action set registry
     */
    public ActionSetRegistry getActionSetRegistry() {
		return e4Context.get(ActionSetRegistry.class);
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
		return e4Context.get(IEditorRegistry.class);
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
                PI_WORKBENCH, IWorkbenchRegistryConstants.PL_ELEMENT_FACTORY);

        if (extensionPoint == null) {
            WorkbenchPlugin
                    .log("Unable to find element factory. Extension point: " + IWorkbenchRegistryConstants.PL_ELEMENT_FACTORY + " not found"); //$NON-NLS-2$ //$NON-NLS-1$
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
     * Return the perspective registry.
     * @return IPerspectiveRegistry. The registry for the receiver.
     */
    public IPerspectiveRegistry getPerspectiveRegistry() {
		return e4Context.get(IPerspectiveRegistry.class);
    }

    /**
     * Returns the working set manager
     *
     * @return the working set manager
     * @since 2.0
     */
    public IWorkingSetManager getWorkingSetManager() {
		return e4Context.get(IWorkingSetManager.class);
    }

    /**
     * Returns the working set registry
     *
     * @return the working set registry
     * @since 2.0
     */
    public WorkingSetRegistry getWorkingSetRegistry() {
		return e4Context.get(WorkingSetRegistry.class);
    }

    /**
     * Returns the introduction registry.
     *
     * @return the introduction registry.
     * @since 3.0
     */
    public IIntroRegistry getIntroRegistry() {
		return e4Context.get(IIntroRegistry.class);
    }

    /**
	 * Returns the operation support.
	 *
	 * @return the workbench operation support.
	 * @since 3.1
	 */
    public IWorkbenchOperationSupport getOperationSupport() {
		IWorkbenchOperationSupport op = e4Context.get(IWorkbenchOperationSupport.class);
		if (op == null) {
			// we're in shutdown, but some plugins get this in their stop()
			// methods. In 3.x we just return a bogus one, so here it is
			op = new WorkbenchOperationSupport();
		}
		return op;
    }


    /**
     * Get the preference manager.
     * @return PreferenceManager the preference manager for
     * the receiver.
     */
    public PreferenceManager getPreferenceManager() {
		return e4Context.get(PreferenceManager.class);
    }

    /**
     * Returns the shared images for the workbench.
     *
     * @return the shared image manager
     */
    public ISharedImages getSharedImages() {
    	if(sharedImages == null) {
    		sharedImages = new SharedImages();
    	}
    	if(e4Context == null) {
    		return sharedImages;
    	}
		return e4Context.get(ISharedImages.class);
    }

    /**
     * Returns the theme registry for the workbench.
     *
     * @return the theme registry
     */
    public IThemeRegistry getThemeRegistry() {
		return e4Context.get(IThemeRegistry.class);
    }

    /**
     * Answer the view registry.
     * @return IViewRegistry the view registry for the
     * receiver.
     */
    public IViewRegistry getViewRegistry() {
		return e4Context.get(IViewRegistry.class);
    }

    /**
     * Answer the workbench.
     * @deprecated Use <code>PlatformUI.getWorkbench()</code> instead.
     */
    @Deprecated
	@Override
	public IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    /**
     * Set default preference values.
     * This method must be called whenever the preference store is initially loaded
     * because the default values are not stored in the preference store.
     */
    @Override
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

    /**
     * Log the throwable.
     * @param t
     */
    public static void log(Throwable t) {
		getDefault().getLog().log(getStatus(t));
	}

	/**
	 * Return the status from throwable
	 * @param t throwable
	 * @return IStatus
	 */
	public static IStatus getStatus(Throwable t) {
		String message = StatusUtil.getLocalizedMessage(t);

		return newError(message, t);
	}

	/**
	 * Create a new error from the message and the
	 * throwable.
	 * @param message
	 * @param t
	 * @return IStatus
	 */
	public static IStatus newError(String message, Throwable t) {
		String pluginId = "org.eclipse.ui.workbench"; //$NON-NLS-1$
		int errorCode = IStatus.OK;

		// If this was a CoreException, keep the original plugin ID and error
		// code
		if (t instanceof CoreException) {
			CoreException ce = (CoreException) t;
			pluginId = ce.getStatus().getPlugin();
			errorCode = ce.getStatus().getCode();
		}

		return new Status(IStatus.ERROR, pluginId, errorCode, message,
				StatusUtil.getCause(t));
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
				clazz.getName(), methodName, t);
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

		// Combine message and status into a MultiStatus to avoid losing
		// context, but avoid creating the MultiStatus unnecessarily if message
		// is the same
		if (message != null && !message.equals(status.getMessage())) {
			status = StatusUtil.newStatus(Collections.singletonList(status), message, null);
        }
		getDefault().getLog().log(status);
    }

    /**
     * Log the status to the default log.
     * @param status
     */
    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    /**
     * Get the decorator manager for the receiver
     * @return DecoratorManager the decorator manager
     * for the receiver.
     */
    public DecoratorManager getDecoratorManager() {
		return (DecoratorManager) e4Context.get(IDecoratorManager.class);
    }

    @Override
	public void start(BundleContext context) throws Exception {
    	context.addBundleListener(getBundleListener());
        super.start(context);
        bundleContext = context;

        JFaceUtil.initializeJFace();

		parseBidiArguments();
		Window.setDefaultOrientation(getDefaultOrientation());

        // The UI plugin needs to be initialized so that it can install the callback in PrefUtil,
        // which needs to be done as early as possible, before the workbench
        // accesses any API preferences.
        Bundle uiBundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
        try {
            // Attempt to load the activator of the ui bundle.  This will force lazy start
            // of the ui bundle.  Using the bundle activator class here because it is a
            // class that needs to be loaded anyway so it should not cause extra classes
            // to be loaded.s
        	if(uiBundle != null)
        		uiBundle.start(Bundle.START_TRANSIENT);
        } catch (BundleException e) {
            WorkbenchPlugin.log("Unable to load UI activator", e); //$NON-NLS-1$
        }
		/*
		 * DO NOT RUN ANY OTHER CODE AFTER THIS LINE.  If you do, then you are
		 * likely to cause a deadlock in class loader code.  Please see Bug 86450
		 * for more information.
		 */

    }

	/**
	 * Read the -bidi option from the command line arguments. The valid values /
	 * syntax is as follows:
	 *
	 * <pre>
	 * -bidi "on=[y/n];textDir=[ltr/rtl/auto]"
	 * </pre>
	 * <p>
	 * Important:
	 * <ul>
	 * <li>The order of parameters under the <code>-bidi</code> switch is
	 * arbitrary.</li>
	 * <li>The presence of any parameter is not mandatory.</li>
	 * <li>If any of the parameters is not specified, the default value is
	 * assumed. Defaults:
	 * <ul>
	 * <li>on: n</li>
	 * <li>textDir: no default value</li>
	 * </ul>
	 * </li>
	 * <li>If no value (or an illegal value) is provided for handling of base
	 * text direction functionality, then bidi support is turned off and no
	 * handling occurs.</li>
	 * </ul>
	 */
	private void parseBidiArguments() {
		String[] commandLineArgs = Platform.getCommandLineArgs();
		String bidiParams = null;
		// Do not process the last one as it will never have a parameter
		for (int i = 0; i < commandLineArgs.length - 1; i++) {
			if (commandLineArgs[i].equals(BIDI_COMMAND_LINE)) {
				bidiParams = commandLineArgs[i + 1];
			}
		}
		if (bidiParams != null) {
			String[] bidiProps = Util.getArrayFromList(bidiParams, ";"); //$NON-NLS-1$
			for (int i = 0; i < bidiProps.length; ++i) {
				int eqPos = bidiProps[i].indexOf("="); //$NON-NLS-1$
				if ((eqPos > 0) && (eqPos < bidiProps[i].length() - 1)) {
					String nameProp = bidiProps[i].substring(0, eqPos);
					String valProp = bidiProps[i].substring(eqPos + 1);
					if (nameProp.equals(BIDI_SUPPORT_OPTION)) {
						BidiUtils.setBidiSupport("y".equals(valProp)); //$NON-NLS-1$
					} else if (nameProp.equalsIgnoreCase(BIDI_TEXTDIR_OPTION)) {
						try {
							BidiUtils.setTextDirection(valProp.intern());
						} catch (IllegalArgumentException e) {
							WorkbenchPlugin.log(e);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the default orientation from the command line arguments. If there are
	 * no arguments imply the orientation.
	 *
	 * @return int
	 * @see SWT#NONE
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 */
    private int getDefaultOrientation() {

		String[] commandLineArgs = Platform.getCommandLineArgs();

		int orientation = getCommandLineOrientation(commandLineArgs);

		if(orientation != SWT.NONE) {
			return orientation;
		}

		orientation = getSystemPropertyOrientation();

		if(orientation != SWT.NONE) {
			return orientation;
		}

		return checkCommandLineLocale(); //Use the default value if there is nothing specified
	}

	/**
	 * Check whether the workbench messages are in a Bidi language. This method
	 * will return <code>null</code> if it is unable to determine message
	 * properties.
	 */
	private Boolean isBidiMessageText() {
		// Check if the user installed the NLS packs for bidi
		String message = WorkbenchMessages.Startup_Loading_Workbench;
		if (message == null)
			return null;

		try {
			// use qualified class name to avoid import statement
			// and premature attempt to resolve class reference
			boolean isBidi = com.ibm.icu.text.Bidi.requiresBidi(message.toCharArray(), 0,
					message.length());
			return Boolean.valueOf(isBidi);
		} catch (NoClassDefFoundError e) {
			// the ICU Base bundle used in place of ICU?
			return null;
		}
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
		// Check if the user property is set. If not, do not rely on the VM.
		if (System.getProperty(NL_USER_PROPERTY) == null) {
			Boolean needRTL = isBidiMessageText();
			if (needRTL != null && needRTL.booleanValue())
				return SWT.RIGHT_TO_LEFT;
		} else {
			String lang = Locale.getDefault().getLanguage();
			boolean bidiLangauage = "iw".equals(lang) || "he".equals(lang) || "ar".equals(lang) || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"fa".equals(lang) || "ur".equals(lang); //$NON-NLS-1$ //$NON-NLS-2$
			if (bidiLangauage) {
				Boolean needRTL = isBidiMessageText();
				if (needRTL == null)
					return SWT.RIGHT_TO_LEFT;
				if (needRTL.booleanValue())
					return SWT.RIGHT_TO_LEFT;
			}
		}
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
		if(RIGHT_TO_LEFT.equals(orientation)) {
			return SWT.RIGHT_TO_LEFT;
		}
		if(LEFT_TO_RIGHT.equals(orientation)) {
			return SWT.LEFT_TO_RIGHT;
		}
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
        if (productInfo == null) {
			productInfo = new ProductInfo(Platform.getProduct());
		}
        return productInfo;
    }

    @Override
	public void stop(BundleContext context) throws Exception {
    	if (bundleListener!=null) {
    		context.removeBundleListener(bundleListener);
    		bundleListener = null;
    	}
		if (debugTracker != null) {
			debugTracker.close();
			debugTracker = null;
		}
		if (testableTracker != null) {
			testableTracker.close();
			testableTracker = null;
		}
        super.stop(context);
    }

    /**
     * Return the new wizard registry.
     *
     * @return the new wizard registry
     * @since 3.1
     */
    public IWizardRegistry getNewWizardRegistry() {
		return e4Context.get(NewWizardRegistry.class);
    }

    /**
     * Return the import wizard registry.
     *
     * @return the import wizard registry
     * @since 3.1
     */
    public IWizardRegistry getImportWizardRegistry() {
		return e4Context.get(ImportWizardRegistry.class);
    }

    /**
     * Return the export wizard registry.
     *
     * @return the export wizard registry
     * @since 3.1
     */
    public IWizardRegistry getExportWizardRegistry() {
		return e4Context.get(ExportWizardRegistry.class);
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

	/**
	 * @return the bundle listener for this plug-in
	 */
	private BundleListener getBundleListener() {
		if (bundleListener == null) {
			bundleListener = new SynchronousBundleListener() {
				@Override
				public void bundleChanged(BundleEvent event) {
					WorkbenchPlugin.this.bundleChanged(event);
				}
			};
		}
		return bundleListener;
	}

	private void bundleChanged(BundleEvent event) {
		int eventType = event.getType();
		// a bundle in the STARTING state generates 2 events, LAZY_ACTIVATION
		// when it enters STARTING and STARTING when it exists STARTING :-)
		synchronized (startingBundles) {
			switch (eventType) {
				case BundleEvent.STARTING :
					startingBundles.add(event.getBundle());
					break;
				case BundleEvent.STARTED :
				case BundleEvent.STOPPED :
					startingBundles.remove(event.getBundle());
					break;
				default :
					break;
			}
		}
	}

	public boolean isStarting(Bundle bundle) {
		synchronized (startingBundles) {
			return startingBundles.contains(bundle);
		}
	}

	/**
	 * Return whether or not the OSGi framework has specified the handle of a splash shell.
	 *
	 * @return whether or not the OSGi framework has specified the handle of a splash shell
	 * @since 3.4
	 */
	public static boolean isSplashHandleSpecified() {
		return System.getProperty(PROP_SPLASH_HANDLE) != null;
	}

	/**
	 * Get the splash shell for this workbench instance, if any. This will find
	 * the splash created by the launcher (native) code and wrap it in a SWT
	 * shell. This may have the side effect of setting data on the provided
	 * {@link Display}.
	 *
	 * @param display
	 *            the display to parent the shell on
	 *
	 * @return the splash shell or <code>null</code>
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws NumberFormatException
	 * @see Display#setData(String, Object)
	 * @since 3.4
	 */
	public static Shell getSplashShell(Display display)
			throws NumberFormatException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		Shell splashShell = (Shell) display.getData(DATA_SPLASH_SHELL);
		if (splashShell != null)
			return splashShell;

		String splashHandle = System.getProperty(PROP_SPLASH_HANDLE);
		if (splashHandle == null) {
			return null;
		}

		// look for the 32 bit internal_new shell method
		try {
			Method method = Shell.class.getMethod(
					"internal_new", new Class[] { Display.class, int.class }); //$NON-NLS-1$
			// we're on a 32 bit platform so invoke it with splash
			// handle as an int
			splashShell = (Shell) method.invoke(null, new Object[] { display,
					Integer.valueOf(splashHandle) });
		} catch (NoSuchMethodException e) {
			// look for the 64 bit internal_new shell method
			try {
				Method method = Shell.class
						.getMethod(
								"internal_new", new Class[] { Display.class, long.class }); //$NON-NLS-1$

				// we're on a 64 bit platform so invoke it with a long
				splashShell = (Shell) method.invoke(null, new Object[] {
						display, new Long(splashHandle) });
			} catch (NoSuchMethodException e2) {
				// cant find either method - don't do anything.
			}
		}

		display.setData(DATA_SPLASH_SHELL, splashShell);
		return splashShell;
	}

	/**
	 * Removes any splash shell data set on the provided display and disposes
	 * the shell if necessary.
	 *
	 * @param display
	 *            the display to parent the shell on
	 * @since 3.4
	 */
	public static void unsetSplashShell(Display display) {
		Shell splashShell = (Shell) display.getData(DATA_SPLASH_SHELL);
		if (splashShell != null) {
			if (!splashShell.isDisposed())
				splashShell.dispose();
			display.setData(DATA_SPLASH_SHELL, null);
		}

	}

	/**
	 * Initialized the workbench plug-in with the e4 context
	 * @param context the e4 context
	 */
	public void initializeContext(IEclipseContext context) {
		e4Context = context;
		e4Context.set(IPerspectiveRegistry.class.getName(), new ContextFunction() {

			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (perspRegistry == null) {
					perspRegistry = ContextInjectionFactory.make(
							PerspectiveRegistry.class, e4Context);
				}
				return perspRegistry;
			}
		});
		e4Context.set(IViewRegistry.class.getName(), new ContextFunction() {

			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (viewRegistry == null) {
					viewRegistry = ContextInjectionFactory.make(ViewRegistry.class,
							e4Context);
				}
				return viewRegistry;
			}
		});
		e4Context.set(ActionSetRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (actionSetRegistry == null) {
					actionSetRegistry = new ActionSetRegistry();
				}
				return actionSetRegistry;
			}
		});
		context.set(IDecoratorManager.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (decoratorManager == null) {
					decoratorManager = new DecoratorManager();
				}
				return decoratorManager;
			}
		});
		context.set(ExportWizardRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				return ExportWizardRegistry.getInstance();
			}
		});
		context.set(ImportWizardRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				return ImportWizardRegistry.getInstance();
			}
		});
		context.set(IIntroRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (introRegistry == null) {
					introRegistry = new IntroRegistry();
				}
				return introRegistry;
			}
		});
		context.set(NewWizardRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				return NewWizardRegistry.getInstance();
			}
		});
		context.set(IWorkbenchOperationSupport.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (operationSupport == null) {
					operationSupport = new WorkbenchOperationSupport();
				}
				return operationSupport;
			}
		});
		context.set(PreferenceManager.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (preferenceManager == null) {
					preferenceManager = new WorkbenchPreferenceManager(
							PREFERENCE_PAGE_CATEGORY_SEPARATOR);

					// Get the pages from the registry
					PreferencePageRegistryReader registryReader = new PreferencePageRegistryReader(
							getWorkbench());
					registryReader.loadFromRegistry(Platform.getExtensionRegistry());
					preferenceManager.addPages(registryReader.getTopLevelNodes());

				}
				return preferenceManager;
			}
		});
		context.set(ISharedImages.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (sharedImages == null) {
					sharedImages = new SharedImages();
				}
				return sharedImages;
			}
		});

		context.set(IThemeRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (themeRegistry == null) {
					themeRegistry = new ThemeRegistry();
					ThemeRegistryReader reader = new ThemeRegistryReader();
					reader.readThemes(Platform.getExtensionRegistry(), themeRegistry);
				}
				return themeRegistry;
			}
		});
		context.set(IWorkingSetManager.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (workingSetManager == null) {
					workingSetManager = new WorkingSetManager(bundleContext);
					workingSetManager.restoreState();
				}
				return workingSetManager;
			}
		});
		context.set(WorkingSetRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (workingSetRegistry == null) {
					workingSetRegistry = new WorkingSetRegistry();
					workingSetRegistry.load();
				}
				return workingSetRegistry;
			}
		});
		context.set(IEditorRegistry.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (editorRegistry == null) {
					editorRegistry = new EditorRegistry();
				}
				return editorRegistry;
			}
		});
		context.set(EHelpService.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (helpService == null) {
					helpService = new HelpServiceImpl();
				}
				return helpService;
			}
		});
		context.set(ICommandHelpService.class.getName(), new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context, String contextKey) {
				if (commandHelpService == null) {
					commandHelpService = ContextInjectionFactory.make(CommandHelpServiceImpl.class,
							e4Context);
				}
				return commandHelpService;
			}
		});
	}

	/*
	 * Return the debug options service, if available.
	 */
	public DebugOptions getDebugOptions() {
		if (bundleContext == null)
			return null;
		if (debugTracker == null) {
			debugTracker = new ServiceTracker(bundleContext, DebugOptions.class.getName(), null);
			debugTracker.open();
		}
		return (DebugOptions) debugTracker.getService();
	}


	/**
	 * Returns a {@link TestableObject} provided by a TestableObject
	 * service or <code>null</code> if a service implementation cannot
	 * be found.  The TestableObject is used to hook tests into the
	 * application lifecycle.
	 * <p>
	 * It is recommended the testable object is obtained via service
	 * over {@link Workbench#getWorkbenchTestable()} to avoid the
	 * tests having a dependency on the Workbench.
	 * </p>
	 * @see PlatformUI#getTestableObject()
	 * @return TestableObject provided via service or <code>null</code>
	 */
	public TestableObject getTestableObject() {
		if (bundleContext == null)
			return null;
		if (testableTracker == null) {
			testableTracker = new ServiceTracker(bundleContext, TestableObject.class.getName(), null);
			testableTracker.open();
		}
		return (TestableObject) testableTracker.getService();
	}
}
