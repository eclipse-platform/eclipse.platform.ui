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
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.IWorkbenchWidget;
import org.eclipse.ui.menus.WidgetContributionItem;
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

	private AbstractContributionFactory viewMenuAddition;

	private AbstractContributionFactory viewToolbarAddition;

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
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);
		viewMenuAddition = new AbstractContributionFactory(
				"menu:org.eclipse.ui.tests.api.MenuTestHarness?after=additions") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX20",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X20", null, null);
				additions.add(item);

				MenuManager submenu = new MenuManager("Menu X21",
						"org.eclipse.ui.tests.menus.menuX21");
				item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX22",
						"org.eclipse.ui.tests.menus.updateWorld", null, null,
						null, null, "Item X22", null, null);
				submenu.add(item);
				item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX23",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X23", null, null);
				submenu.add(item);

				additions.add(submenu);

				item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX24",
						"org.eclipse.ui.tests.menus.enabledWorld", null, null,
						null, null, "Item X24", null, null);
				additions.add(item);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
				// for us this is a no-op
			}
		};
		menuService.addContributionFactory(viewMenuAddition);

		viewToolbarAddition = new AbstractContributionFactory(
				"toolbar:org.eclipse.ui.tests.api.MenuTestHarness") {
			public void createContributionItems(IMenuService menuService,
					List additions) {
				CommandContributionItem item = new CommandContributionItem(
						"org.eclipse.ui.tests.menus.itemX25",
						"org.eclipse.ui.tests.menus.updateWorld", null, null,
						null, null, "Item X25", null, null);
				additions.add(item);
				WidgetContributionItem widget = new WidgetContributionItem(
						"org.eclipse.ui.tests.menus.itemX26") {

					public IWorkbenchWidget createWidget() {
						return new TextWidget();
					}
				};
				additions.add(widget);
			}

			public void releaseContributionItems(IMenuService menuService,
					List items) {
				// for us this is a no-op
			}
		};
		menuService.addContributionFactory(viewToolbarAddition);
	}
    
    public void removeMenuContribution() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return;
		}
		IMenuService menuService = (IMenuService) PlatformUI.getWorkbench()
				.getService(IMenuService.class);
		menuService.removeContributionFactory(viewMenuAddition);
		viewMenuAddition = null;
		menuService.removeContributionFactory(viewToolbarAddition);
		viewMenuAddition = null;
	}
}
