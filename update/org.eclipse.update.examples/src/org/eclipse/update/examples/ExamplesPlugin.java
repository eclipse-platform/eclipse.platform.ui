package org.eclipse.update.examples;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.plugin.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 */
public class ExamplesPlugin extends AbstractUIPlugin {
   //The shared instance.
   private static ExamplesPlugin plugin;
   //Resource bundle.
   private ResourceBundle resourceBundle;
   
  /**
   * The constructor.
   */
   public ExamplesPlugin(IPluginDescriptor descriptor) {
      super(descriptor);
      plugin = this;
      try {
         resourceBundle= ResourceBundle.getBundle("org.eclipse.update.examples.ExamplesPluginResources");
      } catch (MissingResourceException x) {
         resourceBundle = null;
      }
   }

  /**
   * Returns the shared instance.
   */
   public static ExamplesPlugin getDefault() {
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
      ResourceBundle bundle= ExamplesPlugin.getDefault().getResourceBundle();
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
}
