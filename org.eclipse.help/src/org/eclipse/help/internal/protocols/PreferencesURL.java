package org.eclipse.help.internal.protocols;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.*;

import org.eclipse.help.internal.*;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.util.XMLGenerator;


/**
 * URL-like description of the help preferences. This is part of the help:/ protocol (help:/preferences). 
 * For now we only support the preferences for the help plugin. This can be improved by specifying 
 * the plugin for which to get preferences.
 */
public class PreferencesURL extends HelpURL {
	public final static String PREFERENCES = "prefs";
	/**
	 */
	public PreferencesURL(String url, String query) {
		super(url, query);
	}
	/**
	 * Returns the path prefix that identifies the URL. 
	 */
	public static String getPrefix() {
		return PREFERENCES;
	}
	/**
	 * Opens a stream for reading.
	 * 
	 * @return java.io.InputStream
	 */
	public InputStream openStream() {
		Preferences pref = HelpPlugin.getDefault().getPluginPreferences();
		StringWriter stWriter = new StringWriter();
		XMLGenerator gen = new XMLGenerator(stWriter);
		gen.println("<prefs>");
		gen.pad++;

		gen.printPad();
		String[] names = pref.defaultPropertyNames();
		for (int i=0; i<names.length; i++)
		{
			gen.println("<pref name=\""+names[i]+"\" value=\""+pref.getString(names[i])+"\"/>");
		}
		gen.pad--;
		gen.println("</prefs>");
		gen.close();
		try {
			return new ByteArrayInputStream(stWriter.toString().getBytes("UTF8"));
		} catch (UnsupportedEncodingException uee) {
			return null;
		}
	}
}