/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WWinPluginAction;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * Abstract base class for plug-ins that integrate with the Eclipse platform UI.
 * <p>
 * Subclasses obtain the following capabilities:
 * </p>
 * <p>
 * Preferences
 * <ul>
 * <li> The platform core runtime contains general support for plug-in
 *      preferences (<code>org.eclipse.core.runtime.Preferences</code>). 
 *      This class provides appropriate conversion to the older JFace preference 
 *      API (<code>org.eclipse.jface.preference.IPreferenceStore</code>).</li>
 * <li> The method <code>getPreferenceStore</code> returns the JFace preference
 *      store (cf. <code>Plugin.getPluginPreferences</code> which returns
 *      a core runtime preferences object.</li>
 * <li> Subclasses may reimplement <code>initializeDefaultPreferences</code>
 *      to set up any default values for preferences using JFace API. In this
 *      case, <code>initializeDefaultPluginPreferences</code> should not be
 *      overridden.</li>
 * <li> Subclasses may reimplement
 *      <code>initializeDefaultPluginPreferences</code> to set up any default
 *      values for preferences using core runtime API. In this
 *      case, <code>initializeDefaultPreferences</code> should not be
 *      overridden.</li>
 * <li> Preferences are also saved automatically on plug-in shutdown.
 *      However, saving preferences immediately after changing them is
 *      strongly recommended, since that ensures that preference settings
 *      are not lost even in the event of a platform crash.</li>
 * </ul>
 * Dialogs
 * <ul>
 * <li> The dialog store is read the first time <code>getDialogSettings</code> 
 *      is called.</li>
 * <li> The dialog store allows the plug-in to "record" important choices made
 *      by the user in a wizard or dialog, so that the next time the
 *      wizard/dialog is used the widgets can be defaulted to better values. A
 *      wizard could also use it to record the last 5 values a user entered into
 *      an editable combo - to show "recent values". </li>
 * <li> The dialog store is found in the file whose name is given by the
 *      constant <code>FN_DIALOG_STORE</code>. A dialog store file is first
 *      looked for in the plug-in's read/write state area; if not found there,
 *      the plug-in's install directory is checked.
 *      This allows a plug-in to ship with a read-only copy of a dialog store
 *      file containing initial values for certain settings.</li>
 * <li> Plug-in code can call <code>saveDialogSettings</code> to cause settings to
 *      be saved in the plug-in's read/write state area. A plug-in may opt to do
 *      this each time a wizard or dialog is closed to ensure the latest 
 *      information is always safe on disk. </li>
 * <li> Dialog settings are also saved automatically on plug-in shutdown.</li>
 * </ul>
 * Images
 * <ul>
 * <li> A typical UI plug-in will have some images that are used very frequently
 *      and so need to be cached and shared.  The plug-in's image registry 
 *      provides a central place for a plug-in to store its common images. 
 *      Images managed by the registry are created lazily as needed, and will be
 *      automatically disposed of when the plug-in shuts down. Note that the
 *      number of registry images should be kept to a minimum since many OSs
 *      have severe limits on the number of images that can be in memory at once.
 * </ul>
 * <p>
 * For easy access to your plug-in object, use the singleton pattern. Declare a
 * static variable in your plug-in class for the singleton. Store the first
 * (and only) instance of the plug-in class in the singleton when it is created.
 * Then access the singleton when needed through a static <code>getDefault</code>
 * method.
 * </p>
 * <p>
 * See the description on {@link Plugin}.
 * </p>
 */
public abstract class AbstractUIPlugin extends Plugin {

    /**
     * The name of the dialog settings file (value 
     * <code>"dialog_settings.xml"</code>).
     */
    private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; //$NON-NLS-1$

    /**
     * Storage for dialog and wizard data; <code>null</code> if not yet
     * initialized.
     */
    private DialogSettings dialogSettings = null;

    /**
     * Storage for preferences.
     */
    private ScopedPreferenceStore preferenceStore;

    /**
     * The registry for all graphic images; <code>null</code> if not yet
     * initialized.
     */
    private ImageRegistry imageRegistry = null;

    /**
     * The bundle listener used for kicking off refreshPluginActions().
     * 
     * @since 3.0.1
     */
    private BundleListener bundleListener;
    
    /**
     * Creates an abstract UI plug-in runtime object for the given plug-in
     * descriptor.
     * <p>
     * Note that instances of plug-in runtime classes are automatically created
     * by the platform in the course of plug-in activation.
     * <p>
     * 
     * @param descriptor the plug-in descriptor
     * @see Plugin#Plugin(org.eclipse.core.runtime.IPluginDescriptor descriptor)
     * @deprecated
     * In Eclipse 3.0 this constructor has been replaced by
     * {@link #AbstractUIPlugin()}. Implementations of
     * <code>MyPlugin(IPluginDescriptor descriptor)</code> should be changed to 
     * <code>MyPlugin()</code> and call <code>super()</code> instead of
     * <code>super(descriptor)</code>.
     * The <code>MyPlugin(IPluginDescriptor descriptor)</code> constructor is
     * called only for plug-ins which explicitly require the
     * org.eclipse.core.runtime.compatibility plug-in (or, as in this case,
     * subclasses which might).
     */
    public AbstractUIPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * Creates an abstract UI plug-in runtime object.
     * <p>
     * Plug-in runtime classes are <code>BundleActivators</code> and so must
     * have an default constructor.  This method is called by the runtime when 
     * the associated bundle is being activated.  
     * <p>
     * For more details, see <code>Plugin</code>'s default constructor.
     *
     * @see Plugin#Plugin()
     * @since 3.0
     */
    public AbstractUIPlugin() {
        super();
    }

    /** 
     * Returns a new image registry for this plugin-in.  The registry will be
     * used to manage images which are frequently used by the plugin-in.
     * <p>
     * The default implementation of this method creates an empty registry.
     * Subclasses may override this method if needed.
     * </p>
     *
     * @return ImageRegistry the resulting registry.
     * @see #getImageRegistry
     */
    protected ImageRegistry createImageRegistry() {
    	
    	//If we are in the UI Thread use that
    	if(Display.getCurrent() != null) {
			return new ImageRegistry(Display.getCurrent());
		}
    	
    	if(PlatformUI.isWorkbenchRunning()) {
			return new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
		}
    	
    	//Invalid thread access if it is not the UI Thread 
    	//and the workbench is not created.
    	throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
    }
    
    /**
     * Returns the dialog settings for this UI plug-in.
     * The dialog settings is used to hold persistent state data for the various
     * wizards and dialogs of this plug-in in the context of a workbench. 
     * <p>
     * If an error occurs reading the dialog store, an empty one is quietly created
     * and returned.
     * </p>
     * <p>
     * Subclasses may override this method but are not expected to.
     * </p>
     *
     * @return the dialog settings
     */
    public IDialogSettings getDialogSettings() {
        if (dialogSettings == null) {
			loadDialogSettings();
		}
        return dialogSettings;
    }

    /**
     * Returns the image registry for this UI plug-in. 
     * <p>
     * The image registry contains the images used by this plug-in that are very 
     * frequently used and so need to be globally shared within the plug-in. Since 
     * many OSs have a severe limit on the number of images that can be in memory at 
     * any given time, a plug-in should only keep a small number of images in their 
     * registry.
     * <p>
     * Subclasses should reimplement <code>initializeImageRegistry</code> if they have
     * custom graphic images to load.
     * </p>
     * <p>
     * Subclasses may override this method but are not expected to.
     * </p>
     *
     * @return the image registry
     */
    public ImageRegistry getImageRegistry() {
        if (imageRegistry == null) {
            imageRegistry = createImageRegistry();
            initializeImageRegistry(imageRegistry);
        }
        return imageRegistry;
    }

    /**
     * Returns the preference store for this UI plug-in.
     * This preference store is used to hold persistent settings for this plug-in in
     * the context of a workbench. Some of these settings will be user controlled, 
     * whereas others may be internal setting that are never exposed to the user.
     * <p>
     * If an error occurs reading the preference store, an empty preference store is
     * quietly created, initialized with defaults, and returned.
     * </p>
     * <p>
     * <strong>NOTE:</strong> As of Eclipse 3.1 this method is
     * no longer referring to the core runtime compatibility layer and so
     * plug-ins relying on Plugin#initializeDefaultPreferences
     * will have to access the compatibility layer themselves.
     * </p>
     *
     * @return the preference store 
     */
    public IPreferenceStore getPreferenceStore() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            preferenceStore = new ScopedPreferenceStore(new InstanceScope(),getBundle().getSymbolicName());

        }
        return preferenceStore;
    }

    /**
     * Returns the Platform UI workbench.  
     * <p> 
     * This method exists as a convenience for plugin implementors.  The
     * workbench can also be accessed by invoking <code>PlatformUI.getWorkbench()</code>.
     * </p>
     * @return IWorkbench the workbench for this plug-in
     */
    public IWorkbench getWorkbench() {
        return PlatformUI.getWorkbench();
    }

    /** 
     * Initializes a preference store with default preference values 
     * for this plug-in.
     * <p>
     * This method is called after the preference store is initially loaded
     * (default values are never stored in preference stores).
     * </p>
     * <p>
     * The default implementation of this method does nothing.
     * Subclasses should reimplement this method if the plug-in has any preferences.
     * </p>
     * <p>
     * A subclass may reimplement this method to set default values for the 
     * preference store using JFace API. This is the older way of initializing 
     * default values. If this method is reimplemented, do not override
     * <code>initializeDefaultPluginPreferences()</code>.
     * </p>
     * 
     * @param store the preference store to fill
     * 
     * @deprecated this is only called if the runtime compatibility layer is
     *             present. See {@link #initializeDefaultPluginPreferences}.
     */
    protected void initializeDefaultPreferences(IPreferenceStore store) {
        // spec'ed to do nothing
    }

    /**
     * The <code>AbstractUIPlugin</code> implementation of this
     * <code>Plugin</code> method forwards to
     * <code>initializeDefaultPreferences(IPreferenceStore)</code>.
     * <p>
     * A subclass may reimplement this method to set default values for the core
     * runtime preference store in the standard way. This is the recommended way
     * to do this. The older
     * <code>initializeDefaultPreferences(IPreferenceStore)</code> method
     * serves a similar purpose. If this method is reimplemented, do not send
     * super, and do not override
     * <code>initializeDefaultPreferences(IPreferenceStore)</code>.
     * </p>
     * 
     * @deprecated this is only called if the runtime compatibility layer is
     *             present. See the deprecated comment in
     *             {@link Plugin#initializeDefaultPluginPreferences}.
     * 
     * @see #initializeDefaultPreferences
     * @since 2.0
     */
    protected void initializeDefaultPluginPreferences() {
        // N.B. by the time this method is called, the plug-in has a 
        // core runtime preference store (no default values)

        // call loadPreferenceStore (only) for backwards compatibility with Eclipse 1.0
        loadPreferenceStore();
        // call initializeDefaultPreferences (only) for backwards compatibility 
        // with Eclipse 1.0
        initializeDefaultPreferences(getPreferenceStore());
    }

    /** 
     * Initializes an image registry with images which are frequently used by the 
     * plugin.
     * <p>
     * The image registry contains the images used by this plug-in that are very
     * frequently used and so need to be globally shared within the plug-in. Since
     * many OSs have a severe limit on the number of images that can be in memory
     * at any given time, each plug-in should only keep a small number of images in 
     * its registry.
     * </p><p>
     * Implementors should create a JFace image descriptor for each frequently used
     * image.  The descriptors describe how to create/find the image should it be needed. 
     * The image described by the descriptor is not actually allocated until someone 
     * retrieves it.
     * </p><p>
     * Subclasses may override this method to fill the image registry.
     * </p>
     * @param reg the registry to initalize
     *
     * @see #getImageRegistry
     */
    protected void initializeImageRegistry(ImageRegistry reg) {
        // spec'ed to do nothing
    }

    /**
     * Loads the dialog settings for this plug-in.
     * The default implementation first looks for a standard named file in the 
     * plug-in's read/write state area; if no such file exists, the plug-in's
     * install directory is checked to see if one was installed with some default
     * settings; if no file is found in either place, a new empty dialog settings
     * is created. If a problem occurs, an empty settings is silently used.
     * <p>
     * This framework method may be overridden, although this is typically
     * unnecessary.
     * </p>
     */
    protected void loadDialogSettings() {
        dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$

        // bug 69387: The instance area should not be created (in the call to
        // #getStateLocation) if -data @none or -data @noDefault was used
        IPath dataLocation = getStateLocationOrNull();
        if (dataLocation != null) {
	        // try r/w state area in the local file system
	        String readWritePath = dataLocation.append(FN_DIALOG_SETTINGS)
	                .toOSString();
	        File settingsFile = new File(readWritePath);
	        if (settingsFile.exists()) {
	            try {
	                dialogSettings.load(readWritePath);
	            } catch (IOException e) {
	                // load failed so ensure we have an empty settings
	                dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
	            }
	            
	            return;
	        }
        }

        // otherwise look for bundle specific dialog settings
        URL dsURL = BundleUtility.find(getBundle(), FN_DIALOG_SETTINGS);
        if (dsURL == null) {
			return;
		}

        InputStream is = null;
        try {
            is = dsURL.openStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf-8")); //$NON-NLS-1$
            dialogSettings.load(reader);
        } catch (IOException e) {
            // load failed so ensure we have an empty settings
            dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
        } finally {
            try {
                if (is != null) {
					is.close();
				}
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    /**
     * Loads the preference store for this plug-in.
     * The default implementation looks for a standard named file in the 
     * plug-in's read/write state area. If no file is found or a problem
     * occurs, a new empty preference store is silently created. 
     * <p>
     * This framework method may be overridden, although this is typically 
     * unnecessary.
     * </p>
     * 
     * @deprecated As of Eclipse 2.0, a basic preference store exists for all
     * plug-ins. This method now exists only for backwards compatibility.
     * It is called as the plug-in's preference store is being initialized.
     * The plug-ins preferences are loaded from the file regardless of what
     * this method does.
     */
    protected void loadPreferenceStore() {
        // do nothing by default 
    }

    /**
     * Refreshes the actions for the plugin.
     * This method is called from <code>startup</code>.
     * <p>
     * This framework method may be overridden, although this is typically 
     * unnecessary.
     * </p>
     */
    protected void refreshPluginActions() {
        // If the workbench is not started yet, or is no longer running, do nothing.
        if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}

        // startup() is not guaranteed to be called in the UI thread,
        // but refreshPluginActions must run in the UI thread, 
        // so use asyncExec.  See bug 6623 for more details.
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                WWinPluginAction.refreshActionList();
            }
        });
    }

    /**
     * Saves this plug-in's dialog settings.
     * Any problems which arise are silently ignored.
     */
    protected void saveDialogSettings() {
        if (dialogSettings == null) {
            return;
        }

        try {
        	IPath path = getStateLocationOrNull();
        	if(path == null) {
				return;
			}
            String readWritePath = path
                    .append(FN_DIALOG_SETTINGS).toOSString();
            dialogSettings.save(readWritePath);
        } catch (IOException e) {
            // spec'ed to ignore problems
        } catch (IllegalStateException e) {
            // spec'ed to ignore problems
        }
    }

    /**
     * Saves this plug-in's preference store.
     * Any problems which arise are silently ignored.
     * 
     * @see Plugin#savePluginPreferences()
     * @deprecated As of Eclipse 2.0, preferences exist for all plug-ins. The 
     * equivalent of this method is <code>Plugin.savePluginPreferences</code>. 
     * This method now calls <code>savePluginPreferences</code>, and exists only for
     * backwards compatibility.
     */
    protected void savePreferenceStore() {
        savePluginPreferences();
    }

    /**
     * The <code>AbstractUIPlugin</code> implementation of this <code>Plugin</code>
     * method does nothing.  Subclasses may extend this method, but must send
     * super first.
     * <p>
     * WARNING: Plug-ins may not be started in the UI thread.
     * The <code>startup()</code> method should not assume that its code runs in
     * the UI thread, otherwise SWT thread exceptions may occur on startup.'
     * @deprecated 
     * In Eclipse 3.0, <code>startup</code> has been replaced by {@link Plugin#start(BundleContext context)}.
     * Implementations of <code>startup</code> should be changed to extend
     * <code>start(BundleContext context)</code> and call <code>super.start(context)</code>
     * instead of <code>super.startup()</code>. Like <code>super.startup()</code>,
     * <code>super.stop(context)</code> must be called as the very first thing.
     * The <code>startup</code> method is called only for plug-ins which explicitly require the 
     * org.eclipse.core.runtime.compatibility plug-in; in contrast,
     * the <code>start</code> method is always called.
     */
    public void startup() throws CoreException {
        // this method no longer does anything
        // the code that used to be here in 2.1 has moved to start(BundleContext)
        super.startup();
    }

    /**
     * The <code>AbstractUIPlugin</code> implementation of this <code>Plugin</code>
     * method does nothing. Subclasses may extend this method, but must send
     * super first.
     * @deprecated 
     * In Eclipse 3.0, <code>shutdown</code> has been replaced by {@link Plugin#stop(BundleContext context)}.
     * Implementations of <code>shutdown</code> should be changed to extend 
     * <code>stop(BundleContext context)</code> and call <code>super.stop(context)</code> 
     * instead of <code>super.shutdown()</code>. Unlike <code>super.shutdown()</code>, 
     * <code>super.stop(context)</code> must be called as the very <b>last</b> thing rather
     * than as the very first thing. The <code>shutdown</code> method is called
     * only for plug-ins which explicitly require the 
     * org.eclipse.core.runtime.compatibility plug-in; 
     * in contrast, the <code>stop</code> method is always called.
     */
    public void shutdown() throws CoreException {
        // this method no longer does anything interesting
        // the code that used to be here in 2.1 has moved to stop(BundleContext),
        //   which is called regardless of whether the plug-in being instantiated
        //   requires org.eclipse.core.runtime.compatibility
        super.shutdown();
    }

    /**
     * The <code>AbstractUIPlugin</code> implementation of this <code>Plugin</code>
     * method refreshes the plug-in actions.  Subclasses may extend this method,
     * but must send super <b>first</b>.
     * {@inheritDoc}
     * 
     * @since 3.0
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
		final BundleContext fc = context;
        // Should only attempt refreshPluginActions() once the bundle
        // has been fully started.  Otherwise, action delegates
        // can be created while in the process of creating 
        // a triggering action delegate (if UI events are processed during startup).  
        // Also, if the start throws an exception, the bundle will be shut down.  
        // We don't want to have created any delegates if this happens.
        // See bug 63324 for more details.
        bundleListener = new BundleListener() {
            public void bundleChanged(BundleEvent event) {
                if (event.getBundle() == getBundle()) {
                    if (event.getType() == BundleEvent.STARTED) {
                        // We're getting notified that the bundle has been started.
                        // Make sure it's still active.  It may have been shut down between
                        // the time this event was queued and now.
                        if (getBundle().getState() == Bundle.ACTIVE) {
                            refreshPluginActions();
                        }
                        fc.removeBundleListener(this);
                    }
                }
            }
        };
        context.addBundleListener(bundleListener);
        // bundleListener is removed in stop(BundleContext)
    }

    /**
     * The <code>AbstractUIPlugin</code> implementation of this <code>Plugin</code>
     * method saves this plug-in's preference and dialog stores and shuts down 
     * its image registry (if they are in use). Subclasses may extend this
     * method, but must send super <b>last</b>. A try-finally statement should
     * be used where necessary to ensure that <code>super.shutdown()</code> is
     * always done.
     * {@inheritDoc}
     * 
     * @since 3.0
     */
    public void stop(BundleContext context) throws Exception {
        try {
            if (bundleListener != null) {
                context.removeBundleListener(bundleListener);
            }
            saveDialogSettings();
            savePreferenceStore();
            preferenceStore = null;
            if (imageRegistry != null)
            	imageRegistry.dispose();
            imageRegistry = null;
        } finally {
            super.stop(context);
        }
    }

    /**
     * Creates and returns a new image descriptor for an image file located
     * within the specified plug-in.
     * <p>
     * This is a convenience method that simply locates the image file in
     * within the plug-in (no image registries are involved). The path is
     * relative to the root of the plug-in, and takes into account files
     * coming from plug-in fragments. The path may include $arg$ elements.
     * However, the path must not have a leading "." or path separator.
     * Clients should use a path like "icons/mysample.gif" rather than 
     * "./icons/mysample.gif" or "/icons/mysample.gif".
     * </p>
     * 
     * @param pluginId the id of the plug-in containing the image file; 
     * <code>null</code> is returned if the plug-in does not exist
     * @param imageFilePath the relative path of the image file, relative to the
     * root of the plug-in; the path must be legal
     * @return an image descriptor, or <code>null</code> if no image
     * could be found
     * @since 3.0
     */
    public static ImageDescriptor imageDescriptorFromPlugin(String pluginId,
            String imageFilePath) {
        if (pluginId == null || imageFilePath == null) {
            throw new IllegalArgumentException();
        }

        // if the bundle is not ready then there is no image
        Bundle bundle = Platform.getBundle(pluginId);
        if (!BundleUtility.isReady(bundle)) {
			return null;
		}

        // look for the image (this will check both the plugin and fragment folders
        URL fullPathString = BundleUtility.find(bundle, imageFilePath);
        if (fullPathString == null) {
            try {
                fullPathString = new URL(imageFilePath);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        if (fullPathString == null) {
			return null;
		}
        return ImageDescriptor.createFromURL(fullPathString);
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
    private IPath getStateLocationOrNull() {
        try {
            return getStateLocation();
        } catch (IllegalStateException e) {
            // This occurs if -data=@none is explicitly specified, so ignore this silently.
            // Is this OK? See bug 85071.
            return null;
        }
    }    
    
}
