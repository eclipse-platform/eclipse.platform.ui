/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro.impl;

import java.text.*;
import java.util.*;

import org.eclipse.ui.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.model.loader.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.eclipse.ui.intro.*;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.*;

/**
 * Intro main plugin.
 */
public class IntroPlugin extends AbstractUIPlugin {

    // The static shared instance.
    private static IntroPlugin inst;

    // There should always be a single instance of all these classes.
    private ResourceBundle resourceBundle;

    // The intro resource bundle.
    private static String INTRO_RESOURCE_BUNDLE = "org.eclipse.ui.internal.intro.impl.IntroPluginResources"; //$NON-NLS-1$

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

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getString(String key) {
        try {
            ResourceBundle bundle = IntroPlugin.getDefault()
                    .getResourceBundle();
            return (bundle != null ? bundle.getString(key) : key);
        } catch (MissingResourceException e) {
            Log.warning("IntroPlugin - unable to load resource bundle"); //$NON-NLS-1$
            // ok to return Key.
            return key;
        }
    }

    /**
     * Utility method to get a resource from the given key, then format it with
     * the given substitutions. <br>
     */
    public static String getFormattedString(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }

    /**
     * Returns the plugin's resource bundle.
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
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
     *  
     */
    public static IIntroPart getIntro() {
        IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager()
                .getIntro();
        return introPart;
    }

    /**
     * Returns the Intro Part after forcing an open on it.
     *  
     */
    public static IIntroPart showIntro(boolean standby) {
        IIntroPart introPart = PlatformUI.getWorkbench().getIntroManager()
                .showIntro(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                        standby);
        return introPart;
    }

    /**
     * Returns the standby state of the Intro Part. If the intro is closed,
     * retruns false.
     *  
     */
    public static boolean isIntroStandby() {
        return PlatformUI.getWorkbench().getIntroManager().isIntroStandby(
                getIntro());
    }

    /**
     * Sets the standby state of the Intro Part. If the intro is closed, retruns
     * false.
     *  
     */
    public static void setIntroStandby(boolean standby) {
        PlatformUI.getWorkbench().getIntroManager().setIntroStandby(getIntro(),
                standby);
    }


    /**
     * Returns the standby state of the Intro Part. If the intro is closed,
     * retruns false.
     *  
     */
    public static boolean closeIntro() {
        //      Relies on Workbench.
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
        try {
            resourceBundle = ResourceBundle.getBundle(INTRO_RESOURCE_BUNDLE);
        } catch (MissingResourceException x) {
            resourceBundle = null;
            Log.warning("IntroPlugin - unable to load resource bundle"); //$NON-NLS-1$
        }


    }

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }


}