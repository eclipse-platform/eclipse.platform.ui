package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

public interface IManifestAttributes {


	public static final String COMPONENT = "component";
	public static final String COMPONENT_NAME = "label";
	public static final String COMPONENT_ID = "id";
	public static final String COMPONENT_VERSION = "version";
	public static final String PROVIDER = "provider-name";
	public static final String DESCRIPTION = "description";

	public static final String URL = "url";
	public static final String URL_NAME = "label";
	public static final String UPDATE_URL = "update";
	public static final String DISCOVERY_URL = "discovery";
	
	public static final String PLUGIN = "plugin";
	public static final String PLUGIN_NAME = "label";
	public static final String PLUGIN_ID = "id";
	public static final String PLUGIN_VERSION = "version";

	public static final String FRAGMENT = "fragment";
	public static final String FRAGMENT_NAME = "label";
	public static final String FRAGMENT_ID = "id";
	public static final String FRAGMENT_VERSION = "version";

	public static final String CONFIGURATION = "configuration";
	public static final String PRODUCT = "product";
	public static final String PRODUCT_NAME = "label";
	public static final String PRODUCT_ID = "id";
	public static final String PRODUCT_VERSION = "version";
	public static final String APPLICATION = "application";

	public static final String ALLOW_UPGRADE = "allowUpgrade";
	public static final String OPTIONAL = "optional";
	public static final String REQUIRES = "requires";

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	public static final String VERSION_SEPARATOR_OPEN = "(";
	public static final String VERSION_SEPARATOR_CLOSE = ")";
	
	// NL constants
	public static final String DEFAULT_BUNDLE_NAME	= "install";
				
	public static final String MANIFEST_DIR = "META-INF";
	public static final String MANIFEST_FILE = "MANIFEST.MF";
	public static final String INSTALL_MANIFEST = "install.xml";
	public static final String INSTALL_INDEX = "install.index";
	public static final String SETTINGS_FILE = "UMSettings";
	public static final String UM_LOCK = "UM.lock";
}
