package org.eclipse.update.internal.ui;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.net.MalformedURLException;
import org.eclipse.core.runtime.*;
import java.io.File;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import java.net.URL;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.internal.ui.model.IFeatureAdapter;
/**
 * Bundle of all images used by the PDE plugin.
 */
public class UpdateUIPluginImages {

	private static final String NAME_PREFIX= UpdateUIPlugin.getPluginId()+".";
	private static final int    NAME_PREFIX_LENGTH= NAME_PREFIX.length();

	private final static URL BASE_URL = UpdateUIPlugin.getDefault().getDescriptor().getInstallURL();

	private static ImageRegistry PLUGIN_REGISTRY;
	
	public final static String ICONS_PATH = "icons/full/";

	/**
	 * Set of predefined Image Descriptors.
	 */
	
	private static final String PATH_OBJ= ICONS_PATH+"obj16/";
	private static final String PATH_VIEW = ICONS_PATH+"view16/";
	private static final String PATH_LCL= ICONS_PATH+"elcl16/";
	private static final String PATH_LCL_HOVER= ICONS_PATH+"clcl16/";
	private static final String PATH_LCL_DISABLED= ICONS_PATH+"dlcl16/";
	private static final String PATH_TOOL = ICONS_PATH + "etool16/";
	private static final String PATH_TOOL_HOVER = ICONS_PATH + "ctool16/";
	private static final String PATH_TOOL_DISABLED = ICONS_PATH + "dtool16/";
	private static final String PATH_OVR = ICONS_PATH + "ovr16/";
	private static final String PATH_WIZBAN = ICONS_PATH + "wizban/";

	/**
	 * Frequently used images
	 */
	public static final String IMG_FORM_BANNER = NAME_PREFIX+"FORM_BANNER";
	public static final String IMG_FORM_UNDERLINE = NAME_PREFIX + "FORM_UNDERLINE";
	public static final String IMG_UPDATES_OBJ = NAME_PREFIX + "UDATES_OBJ";

	/**
	 * OBJ16
	 */
	public static final ImageDescriptor DESC_ECLIPSE_OBJ = create(PATH_OBJ, "eclipse2_obj.gif");
	public static final ImageDescriptor DESC_COMPUTER_OBJ = create(PATH_OBJ, "computer_obj.gif");
	public static final ImageDescriptor DESC_FEATURE_OBJ = create(PATH_OBJ, "feature_obj.gif");
	public static final ImageDescriptor DESC_UNCONF_FEATURE_OBJ = create(PATH_OBJ, "unconf_feature_obj.gif");
	public static final ImageDescriptor DESC_UNINSTALL_OBJ = create(PATH_OBJ, "uninstall_obj.gif");
	public static final ImageDescriptor DESC_UPDATES_OBJ = create(PATH_OBJ, "updates_obj.gif");
	public static final ImageDescriptor DESC_SITE_OBJ = create(PATH_OBJ, "site_obj.gif");
	public static final ImageDescriptor DESC_CONFIG_OBJ = create(PATH_OBJ, "config_obj.gif");
	
	/**
	 * OVR16
	 */
	public static final ImageDescriptor DESC_LINKED_CO   = create(PATH_OVR, "linked_co.gif");
	public static final ImageDescriptor DESC_INSTALLABLE_CO = create(PATH_OVR, "installable_co.gif");
	public static final ImageDescriptor DESC_CURRENT_CO = create(PATH_OVR, "current_co.gif");
	public static final ImageDescriptor DESC_ERROR_CO = create(PATH_OVR, "error_co.gif");

	/**
	 * TOOL16
	 */
	public static final ImageDescriptor DESC_SEARCH_PROGRESS_0 = create(PATH_TOOL, "search_progress0.gif");
	public static final ImageDescriptor DESC_SEARCH_PROGRESS_1 = create(PATH_TOOL, "search_progress1.gif");
	public static final ImageDescriptor DESC_SEARCH_PROGRESS_2 = create(PATH_TOOL, "search_progress2.gif");
	public static final ImageDescriptor DESC_SEARCH_PROGRESS_3 = create(PATH_TOOL, "search_progress3.gif");
	
	
	/**
	 * VIEW16
	 */
	public static final ImageDescriptor DESC_HISTORY_VIEW = create(PATH_VIEW, "history.gif");
	public static final ImageDescriptor DESC_MY_ECLIPSE_VIEW = create(PATH_VIEW, "myEclipse.gif");
	public static final ImageDescriptor DESC_SITES_VIEW = create(PATH_VIEW, "sites.gif");

	/**
	 * LCL
	 */
	public static final ImageDescriptor DESC_BACKWARD_NAV = create(PATH_LCL, "backward_nav.gif");
	public static final ImageDescriptor DESC_BACKWARD_NAV_H = create(PATH_LCL_HOVER, "backward_nav.gif");
	public static final ImageDescriptor DESC_BACKWARD_NAV_D = create(PATH_LCL_DISABLED, "backward_nav.gif");
	public static final ImageDescriptor DESC_FORWARD_NAV = create(PATH_LCL, "forward_nav.gif");
	public static final ImageDescriptor DESC_FORWARD_NAV_H = create(PATH_LCL_HOVER, "forward_nav.gif");
	public static final ImageDescriptor DESC_FORWARD_NAV_D = create(PATH_LCL_DISABLED, "forward_nav.gif");
	public static final ImageDescriptor DESC_HOME_NAV = create(PATH_LCL, "home_nav.gif");
	public static final ImageDescriptor DESC_HOME_NAV_H = create(PATH_LCL_HOVER, "home_nav.gif");
	public static final ImageDescriptor DESC_HOME_NAV_D = create(PATH_LCL_DISABLED, "home_nav.gif");
	public static final ImageDescriptor DESC_REFRESH_NAV = create(PATH_LCL, "refresh_nav.gif");
	public static final ImageDescriptor DESC_REFRESH_NAV_H = create(PATH_LCL_HOVER, "refresh_nav.gif");
	public static final ImageDescriptor DESC_REFRESH_NAV_D = create(PATH_LCL_DISABLED, "refresh_nav.gif");
	public static final ImageDescriptor DESC_STOP_NAV = create(PATH_LCL, "stop_nav.gif");
	public static final ImageDescriptor DESC_STOP_NAV_H = create(PATH_LCL_HOVER, "stop_nav.gif");
	public static final ImageDescriptor DESC_STOP_NAV_D = create(PATH_LCL_DISABLED, "stop_nav.gif");
	public static final ImageDescriptor DESC_GO_NAV = create(PATH_LCL, "go_nav.gif");
	public static final ImageDescriptor DESC_GO_NAV_H = create(PATH_LCL_HOVER, "go_nav.gif");
	public static final ImageDescriptor DESC_GO_NAV_D = create(PATH_LCL_DISABLED, "go_nav.gif");

	/**
	 * WIZ
	 */
	public static final ImageDescriptor DESC_INSTALL_WIZ  = create(PATH_WIZBAN, "install_wiz.gif");
	public static final ImageDescriptor DESC_INSTALL_BANNER  = create(PATH_WIZBAN, "def_wizban.jpg");
	public static final ImageDescriptor DESC_FORM_BANNER  = create(PATH_WIZBAN, "form_banner.gif");
	public static final ImageDescriptor DESC_FORM_UNDERLINE  = create(PATH_WIZBAN, "form_underline.gif");
	public static final ImageDescriptor DESC_PROVIDER = create(PATH_WIZBAN, "provider.gif");
	public static final ImageDescriptor DESC_ITEM = create(PATH_WIZBAN, "topic.gif");

	private static ImageDescriptor create(String prefix, String name) {
		return ImageDescriptor.createFromURL(makeImageURL(prefix, name));
	}

	public static Image get(String key) {
		if (PLUGIN_REGISTRY==null) initialize();
		return PLUGIN_REGISTRY.get(key);
	}

public static ImageDescriptor getImageDescriptorFromPlugin(
	IPluginDescriptor pluginDescriptor, 
	String subdirectoryAndFilename) {
	URL installURL = pluginDescriptor.getInstallURL();
	try {
		URL newURL = new URL(installURL, subdirectoryAndFilename);
		return ImageDescriptor.createFromURL(newURL);
	}
	catch (MalformedURLException e) {
	}
	return null;
}

public static Image getImageFromPlugin(
	IPluginDescriptor pluginDescriptor,
	String subdirectoryAndFilename) {
	URL installURL = pluginDescriptor.getInstallURL();
	Image image = null;
	try {
		URL newURL = new URL(installURL, subdirectoryAndFilename);
		String key = newURL.toString();
		if (PLUGIN_REGISTRY==null) initialize();
		image = PLUGIN_REGISTRY.get(key);
		if (image==null) {
			ImageDescriptor desc = ImageDescriptor.createFromURL(newURL);
			image = desc.createImage();
			PLUGIN_REGISTRY.put(key, image);
		}
	}
	catch (MalformedURLException e) {
	}
	return image;
}
/* package */
private static final void initialize() {
	PLUGIN_REGISTRY = new ImageRegistry();
	manage(IMG_FORM_BANNER, DESC_FORM_BANNER);
	manage(IMG_FORM_UNDERLINE, DESC_FORM_UNDERLINE);
	manage(IMG_UPDATES_OBJ, DESC_UPDATES_OBJ);
}

private static URL makeImageURL(String prefix, String name) {
	String path = prefix + name;
	URL url = null;
	try {
		url = new URL(BASE_URL, path);
	}
	catch (MalformedURLException e) {
		return null;
	}
	return url;
}
public static Image manage(String key, ImageDescriptor desc) {
	Image image = desc.createImage();
	PLUGIN_REGISTRY.put(key, image);
	return image;
}

}
