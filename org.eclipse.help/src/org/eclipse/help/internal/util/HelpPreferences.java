package org.eclipse.help.internal.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.*;
import java.util.Properties;
import org.eclipse.help.internal.HelpPlugin;
/**
 * Properties stored in HelpPlugin work area.
 */
public class HelpPreferences extends HelpProperties {
	// Constants
	public static final String LOG_LEVEL_KEY = "log_level";
	public static final String LOCAL_SERVER_CONFIG = "local_server_config";
	public static final String LOCAL_SERVER_ADDRESS_KEY = "local_server_addr";
	public static final String LOCAL_SERVER_PORT_KEY = "local_server_port";
	public static final String INSTALL_OPTION_KEY = "install";
	public static final String SERVER_PATH_KEY = "server_path";
	public static final String BROWSER_PATH_KEY = "browser_path";
	public static final String INFOCENTER_URL_KEY = "infocenter_url";
	protected URL defaultsUrl = null;
	protected Properties defaults;
	/**
	 * Creates empty Properties.
	 * @param name name of the table;
	 */
	public HelpPreferences() {
		super("pref_store.ini");
		URL installUrl = HelpPlugin.getDefault().getDescriptor().getInstallURL();
		try {
			defaultsUrl = new URL(installUrl, "preferences.ini");
		} catch (MalformedURLException mue) {
		}
		defaults = new Properties();
		initialize();
	}
	public int getDefaultInt(String name) {
		String value = defaults.getProperty(name);
		if (value == null)
			return 0;
		int ival = 0;
		try {
			ival = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return ival;
	}
	public String getDefaultString(String name) {
		String value = defaults.getProperty(name);
		if (value == null)
			return "";
		return value;
	}
	/**
	 * Helper function: gets int for a given name.
	 */
	public int getInt(String name) {
		String value = getProperty(name);
		if (value == null)
			return getDefaultInt(name);
		int ival = 0;
		try {
			ival = Integer.parseInt(value);
		} catch (NumberFormatException e) {
		}
		return ival;
	}
	public String getString(String name) {
		String value = getProperty(name);
		if (value == null)
			return getDefaultString(name);
		return value;
	}
	/**
	 * Restores contents of the Properties from a file.
	 * @return true if persistant data was read in
	 */
	protected boolean initialize() {
		InputStream in = null;
		boolean loaded = false;
		clear();
		// load defaults
		if (defaultsUrl != null)
			try {
				in = defaultsUrl.openStream();
				defaults.load(in);
				loaded = true;
			} catch (IOException ioe00) {
				Logger.logError(
					Resources.getString("File4", defaultsUrl.toExternalForm()),
					null);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException ioe10) {
					}
			}
		// load persisted data
		super.restore();
		return loaded;
	}
	public void setValue(String name, int value) {
		put(name, Integer.toString(value));
	}
	public void setValue(String name, String value) {
		put(name, value);
	}
}