/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.jsch.internal.ui;

import java.net.URL;
import java.util.Hashtable;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class JSchUIPlugin extends AbstractUIPlugin{
  public static final String ID="org.eclipse.jsch.ui"; //$NON-NLS-1$
  public static final String DECORATOR_ID="org.eclipse.jsch.ui.decorator"; //$NON-NLS-1$

  private static Hashtable imageDescriptors=new Hashtable(20);
  /**
   * The singleton plug-in instance
   */
  private static JSchUIPlugin plugin;

  public JSchUIPlugin(){
    super();
    plugin=this;
  }

  /**
   * Returns the singleton plug-in instance.
   * 
   * @return the plugin instance
   */
  public static JSchUIPlugin getPlugin(){
    return plugin;
  }

  /**
   * Returns the image descriptor for the given image ID.
   * Returns null if there is no such image.
   * @param id the id of the image descriptor
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String id){
    return (ImageDescriptor)imageDescriptors.get(id);
  }

  /**
   * Creates an image and places it in the image registry.
   */
  protected void createImageDescriptor(String id){
    URL url=FileLocator.find(JSchUIPlugin.getPlugin().getBundle(), new Path(
        IUIConstants.ICON_PATH+id), null);
    ImageDescriptor desc=ImageDescriptor.createFromURL(url);
    imageDescriptors.put(id, desc);
  }

  /**
   * Convenience method to get an image descriptor for an extension
   * 
   * @param extension  the extension declaring the image
   * @param subdirectoryAndFilename  the path to the image
   * @return the image
   */
  public static ImageDescriptor getImageDescriptorFromExtension(IExtension extension, String subdirectoryAndFilename) {
    URL fullPathString = FileLocator.find(Platform.getBundle(extension.getNamespaceIdentifier()), new Path(subdirectoryAndFilename), null);
    return ImageDescriptor.createFromURL(fullPathString);
  }

  /**
   * Initializes the table of images used in this plugin.
   */
  private void initializeImages() {
    /*
    // objects
    createImageDescriptor(IUIConstants.IMG_REPOSITORY); 
    createImageDescriptor(IUIConstants.IMG_REFRESH);
    createImageDescriptor(IUIConstants.IMG_REFRESH_ENABLED);
    createImageDescriptor(IUIConstants.IMG_REFRESH_DISABLED);
    createImageDescriptor(IUIConstants.IMG_LINK_WITH_EDITOR);
    createImageDescriptor(IUIConstants.IMG_LINK_WITH_EDITOR_ENABLED);
    createImageDescriptor(IUIConstants.IMG_COLLAPSE_ALL);
    createImageDescriptor(IUIConstants.IMG_COLLAPSE_ALL_ENABLED);
    createImageDescriptor(IUIConstants.IMG_NEWLOCATION);
    createImageDescriptor(IUIConstants.IMG_CVSLOGO);
    createImageDescriptor(IUIConstants.IMG_TAG);
    createImageDescriptor(IUIConstants.IMG_MODULE);
    createImageDescriptor(IUIConstants.IMG_CLEAR);
    createImageDescriptor(IUIConstants.IMG_CLEAR_ENABLED);
    createImageDescriptor(IUIConstants.IMG_CLEAR_DISABLED);
    createImageDescriptor(IUIConstants.IMG_BRANCHES_CATEGORY);
    createImageDescriptor(IUIConstants.IMG_VERSIONS_CATEGORY);
    createImageDescriptor(IUIConstants.IMG_DATES_CATEGORY);
    createImageDescriptor(IUIConstants.IMG_PROJECT_VERSION);
    createImageDescriptor(IUIConstants.IMG_WIZBAN_MERGE);
    createImageDescriptor(IUIConstants.IMG_WIZBAN_SHARE);
    createImageDescriptor(IUIConstants.IMG_WIZBAN_DIFF);
    createImageDescriptor(IUIConstants.IMG_WIZBAN_KEYWORD);
    createImageDescriptor(IUIConstants.IMG_WIZBAN_NEW_LOCATION);
    createImageDescriptor(IUIConstants.IMG_WIZBAN_IMPORT);
    createImageDescriptor(IUIConstants.IMG_MERGEABLE_CONFLICT);
    createImageDescriptor(IUIConstants.IMG_QUESTIONABLE);
    createImageDescriptor(IUIConstants.IMG_MERGED);
    createImageDescriptor(IUIConstants.IMG_EDITED);
    createImageDescriptor(IUIConstants.IMG_NO_REMOTEDIR);
    createImageDescriptor(IUIConstants.IMG_CVS_CONSOLE);
    createImageDescriptor(IUIConstants.IMG_DATE);
    createImageDescriptor(IUIConstants.IMG_CHANGELOG);
    createImageDescriptor(IUIConstants.IMG_FILTER_HISTORY);
    createImageDescriptor(IUIConstants.IMG_LOCALMODE);
    createImageDescriptor(IUIConstants.IMG_LOCALREMOTE_MODE);
    createImageDescriptor(IUIConstants.IMG_REMOTEMODE);
    createImageDescriptor(IUIConstants.IMG_LOCALMODE_DISABLED);
    createImageDescriptor(IUIConstants.IMG_LOCALREMOTE_MODE_DISABLED);
    createImageDescriptor(IUIConstants.IMG_REMOTEMODE_DISABLED);
    createImageDescriptor(IUIConstants.IMG_LOCALREVISION_TABLE);
    createImageDescriptor(IUIConstants.IMG_REMOTEREVISION_TABLE);
    createImageDescriptor(IUIConstants.IMG_COMPARE_VIEW);
    */
    
    createImageDescriptor(IUIConstants.IMG_KEY_LOCK);

    /*
    // special
    createImageDescriptor("glyphs/glyph1.gif");  //$NON-NLS-1$
    createImageDescriptor("glyphs/glyph2.gif");  //$NON-NLS-1$
    createImageDescriptor("glyphs/glyph3.gif");  //$NON-NLS-1$
    createImageDescriptor("glyphs/glyph4.gif");  //$NON-NLS-1$
    createImageDescriptor("glyphs/glyph5.gif");  //$NON-NLS-1$
    createImageDescriptor("glyphs/glyph6.gif");  //$NON-NLS-1$
    createImageDescriptor("glyphs/glyph7.gif");  //$NON-NLS-1$
    createImageDescriptor("glyphs/glyph8.gif");  //$NON-NLS-1$
    */
  }
  
  /**
   * @see Plugin#start(BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    
    initializeImages();

    IPreferenceStore store = getPreferenceStore();
    if (store.getBoolean(IUIConstants.PREF_FIRST_STARTUP)) {
      store.setValue(IUIConstants.PREF_FIRST_STARTUP, false);
    }

  }

}
