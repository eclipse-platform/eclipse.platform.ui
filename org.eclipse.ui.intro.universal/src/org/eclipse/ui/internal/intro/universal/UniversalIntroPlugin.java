/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.universal;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.universal.util.Log;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Intro main plugin.
 */
public class UniversalIntroPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.ui.intro.universal"; //$NON-NLS-1$

    // The static shared instance.
    private static UniversalIntroPlugin inst;

    // used for performance logging. Time when the constructor of
    // CustomizableIntroPart is called.
    private long uiCreationStartTime;
    
    // image registry that can be disposed while the
    // plug-in is still active. This is important for
    // switching themes after the plug-in has been loaded.
    private ImageRegistry volatileImageRegistry;
    
    /**
     * The constructor.
     */
    public UniversalIntroPlugin() {
        super();
    }

    /**
     * Returns the shared plugin instance.
     */
    public static UniversalIntroPlugin getDefault() {
        return inst;
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
