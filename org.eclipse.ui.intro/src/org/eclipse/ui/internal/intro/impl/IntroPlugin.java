/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.loader.ExtensionPointManager;
import org.eclipse.ui.internal.intro.impl.presentations.IntroLaunchBar;
import org.eclipse.ui.internal.intro.impl.util.Log;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Intro main plugin.
 */
public class IntroPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ui.intro"; //$NON-NLS-1$

    // The static shared instance.
    private static IntroPlugin inst;

    // We must keep track of the launch bar so that we can
    // close it if intro is opened from the menu.
    private IntroLaunchBar launchBar;

    // used for performance logging. Time when the constructor of
    // CustomizableIntroPart is called.
    private long uiCreationStartTime;
    
    // image registry that can be disposed while the
    // plug-in is still active. This is important for
    // switching themes after the plug-in has been loaded.
    private ImageRegistry volatileImageRegistry;
    
    // debug options
	public static boolean DEBUG = false;
	public static boolean DEBUG_NO_BROWSER = false;
	public static boolean DEBUG_TOOLBAR = false;

    /**
     * The constructor.
     */
    public IntroPlugin() {
        super();
    }

    /**
     * Returns the shared plugin instance.
     */
    public static IntroPlugin getDefault() {
        return inst;
    }
    
    public ImageRegistry getVolatileImageRegistry() {
    	if (volatileImageRegistry==null) {
    		volatileImageRegistry = createImageRegistry();
    		initializeImageRegistry(volatileImageRegistry);
    	}
    	return volatileImageRegistry;
    }
    
    public void resetVolatileImageRegistry() {
    	if (volatileImageRegistry!=null) {
    		volatileImageRegistry.dispose();
    		volatileImageRegistry = null;
    	}
    }


    public void closeLaunchBar() {
        if (launchBar != null) {
            launchBar.close();
            launchBar = null;
        }
    }

    public void setLaunchBar(IntroLaunchBar launchBar) {
        this.launchBar = launchBar;
    }


    /**
     * @return Returns the extensionPointManager.
     */
    public ExtensionPointManager getExtensionPointManager() {
        return ExtensionPointManager.getInst();
    }

    /**
     * Returns the model root. Will always guarantee that model is loaded.
     * 
     * @return Returns the introModelRoot.
     */
    public IntroModelRoot getIntroModelRoot() {
        return getExtensionPointManager().getCurrentModel();
    }

    /**
     * Returns the Intro Part.
     */
    public static IIntroPart getIntro() {
        IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager()
            .getIntro();
        return introPart;
    }

    /**
     * Returns the Intro Part after forcing an open on it.
     */
    public static IIntroPart showIntro(boolean standby) {
        IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager()
            .showIntro(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                standby);
        return introPart;
    }

    /**
     * Returns the standby state of the Intro Part. If the intro is closed,
     * retruns false.
     */
    public static boolean isIntroStandby() {
        return PlatformUI.getWorkbench().getIntroManager().isIntroStandby(
            getIntro());
    }

    /**
     * Sets the standby state of the Intro Part. If the intro is closed, retruns
     * false.
     */
    public static void setIntroStandby(boolean standby) {
        PlatformUI.getWorkbench().getIntroManager().setIntroStandby(getIntro(),
            standby);
    }


    /**
     * Returns the standby state of the Intro Part. If the intro is closed,
     * retruns false.
     */
    public static boolean closeIntro() {
        // Relies on Workbench.
        return PlatformUI.getWorkbench().getIntroManager().closeIntro(
            getIntro());
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        inst = this;
        if (Log.logInfo)
            Log.info("IntroPlugin - calling start on Intro bundle"); //$NON-NLS-1$
    	// Setup debugging options
		DEBUG = isDebugging();
		if (DEBUG) {
			DEBUG_NO_BROWSER = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/flags/noBrowser")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (DEBUG) {
			DEBUG_TOOLBAR = "true".equalsIgnoreCase(Platform.getDebugOption(PLUGIN_ID + "/debug/toolbar")); //$NON-NLS-1$ //$NON-NLS-2$
		}

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    	resetVolatileImageRegistry();
        super.stop(context);
    }

    public long gettUICreationStartTime() {
        return uiCreationStartTime;
    }

    public void setUICreationStartTime(long uiCreationStartTime) {
        this.uiCreationStartTime = uiCreationStartTime;
    }


}
