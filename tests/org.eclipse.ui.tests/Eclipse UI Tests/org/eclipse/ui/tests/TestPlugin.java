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
package org.eclipse.ui.tests;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.menus.AbstractWorkbenchWidget;
import org.eclipse.ui.internal.menus.IMenuService;
import org.eclipse.ui.internal.menus.ItemData;
import org.eclipse.ui.internal.menus.MenuData;
import org.eclipse.ui.internal.menus.MenuDataCacheEntry;
import org.eclipse.ui.internal.menus.WidgetData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.tests.api.workbenchpart.TextWidget;
import org.eclipse.ui.tests.decorators.BackgroundColorDecorator;
import org.eclipse.ui.tests.dynamicplugins.TestInstallUtil;
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

    /**
     * The constructor.
     */
    public TestPlugin(IPluginDescriptor descriptor) {
        super(descriptor);
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
        addMenuContribution();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    	removeMenuContribution();
        TestInstallUtil.setContext(null);
        super.stop(context);
        BackgroundColorDecorator.color = null;
    }
    
    public void addMenuContribution() {
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);
		MenuDataCacheEntry cache = new MenuDataCacheEntry(menuService,
				"menu:org.eclipse.ui.tests.api.MenuTestHarness?after=additions");

		ItemData item = new ItemData("org.eclipse.ui.tests.menus.itemX20",
				"org.eclipse.ui.tests.menus.enabledWorld", null, null,
				"Item X20", null, null);
		cache.add(item);
		MenuData submenu = new MenuData("org.eclipse.ui.tests.menus.menuX21",
				null, "Menu X21", null, null);
		cache.add(submenu);
		item = new ItemData("org.eclipse.ui.tests.menus.itemX22",
				"org.eclipse.ui.tests.menus.updateWorld", null, null,
				"Item X22", null, null);
		submenu.add(item);
		item = new ItemData("org.eclipse.ui.tests.menus.itemX23",
				"org.eclipse.ui.tests.menus.enabledWorld", null, null,
				"Item X23", null, null);
		submenu.add(item);

		item = new ItemData("org.eclipse.ui.tests.menus.itemX24",
				"org.eclipse.ui.tests.menus.enabledWorld", null, null,
				"Item X24", null, null);
		cache.add(item);

		menuService.addCacheForURI(cache);

		cache = new MenuDataCacheEntry(menuService,
				"toolbar:org.eclipse.ui.tests.api.MenuTestHarness");
		item = new ItemData("org.eclipse.ui.tests.menus.itemX25",
				"org.eclipse.ui.tests.menus.updateWorld", null, null,
				"Item X25", null, null);
		cache.add(item);
		WidgetData widget = new WidgetData(
				"org.eclipse.ui.tests.menus.itemX26", null) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.menus.WidgetData#createWidget()
			 */
			public AbstractWorkbenchWidget createWidget() {
				return new TextWidget();
			}
		};
		cache.add(widget);

		menuService.addCacheForURI(cache);
	}
    
    public void removeMenuContribution() {
    	// still figuring this out
    }
}
