/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.webapp;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.ILiveHelpAction;
import org.eclipse.help.internal.*;

/**
 * Action to be exectued from the help document
 */
public class AddBookmarkAction implements ILiveHelpAction {
	private String bookmark;

	/**
	 * @see ILiveHelpAction#setInitializationString(String)
	 */
	public void setInitializationString(String data) {
		bookmark = data;
	}

	/**
	 * @see Runnable#run()
	 */
	public void run() {
		System.out.println("add bookmark:" +bookmark);
		// add this bookmark to help preferences
		Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
		String bookmarks = prefs.getString(HelpSystem.BOOKMARKS);
		bookmarks = bookmarks + "," + bookmark;
		prefs.setValue(HelpSystem.BOOKMARKS, bookmarks);
		HelpPlugin.getDefault().savePluginPreferences();
	}

}