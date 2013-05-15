/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.tests.decorators.BackgroundColorDecorator;
import org.eclipse.ui.tests.dynamicplugins.TestInstallUtil;
import org.eclipse.ui.tests.menus.MenuBuilder;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TestPlugin extends AbstractUIPlugin implements IStartup {
    //The shared instance.
    private static TestPlugin plugin;

    //Resource bundle.
    private ResourceBundle resourceBundle;

    // This boolean should only be true if the earlyStartup() method
    // has been called.
    private static boolean earlyStartupCalled = false;
    
    public static final String PLUGIN_ID = "org.eclipse.ui.tests"; 

    /**
     * The constructor.
     */
    public TestPlugin() {
        plugin = this;
        try {
            resourceBundle = ResourceBundle
                    .getBundle("org.eclipse.ui.tests.TestPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /**
     * Returns the shared instance.
     */
    public static TestPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the workspace instance.
     */
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Returns the string from the plugin's resource bundle,
     * or 'key' if not found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = TestPlugin.getDefault().getResourceBundle();
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Returns the image descriptor with the given relative path.
     */
    public ImageDescriptor getImageDescriptor(String relativePath) {
        String iconPath = "icons/";
        try {
            URL installURL = getDescriptor().getInstallURL();
            URL url = new URL(installURL, iconPath + relativePath);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            // should not happen
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStartup#earlyStartup()
     */
    public void earlyStartup() {
        earlyStartupCalled = true;
    }

    public static boolean getEarlyStartupCalled() {
        return earlyStartupCalled;
    }

    public static void clearEarlyStartup() {
        earlyStartupCalled = false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        TestInstallUtil.setContext(context);
        super.start(context);
        earlyStartup();
        MenuBuilder.addMenuContribution();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    	MenuBuilder.removeMenuContribution();
        TestInstallUtil.setContext(null);
        super.stop(context);
        BackgroundColorDecorator.color = null;
    }
}
