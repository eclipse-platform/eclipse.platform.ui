package org.eclipse.help.internal.navigation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.net.URL;
import java.util.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.server.TempURL;

/**
 * Persistent Hashtable with keys and values of type String.
 */
public class InfosetsMap extends HelpProperties {
	/**
	 * Creates empty table for storing valid Info Sets.
	 * @param name name of the table;
	 */
	public InfosetsMap(String name) {
		super(name);
	}
	/**
	 * Restores contents of the table from a file or from the server,
	 * if called on the client.
	 * @return true if persistant data was read in
	 */
	public boolean restore() {
		if (!this.isEmpty())
			clear();
			
		if (!HelpSystem.isClient())
			return super.restore();
			
		else {
			// get them from the server
			InputStream in = null;

			try {
				URL remoteInfosetFile =
					new URL(
						HelpSystem.getRemoteHelpServerURL(),
						HelpSystem.getRemoteHelpServerPath()
							+ "/"
							+ TempURL.getPrefix()
							+ "/infosets.properties");

				if (Logger.DEBUG)
					Logger.logDebugMessage(
						"InfosetsMap",
						"Loading infosets= " + remoteInfosetFile.toExternalForm());

				try {
					in = remoteInfosetFile.openStream();
					super.load(in);
				} catch (Exception ioe) {
					Logger.logError("E013", ioe);
					return false;
				}
				try {
					in.close();
				} catch (Exception ioe) {
				}
			} catch (Exception ioe) {
				Logger.logError("E013", ioe);
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
				}
				return false;
			}

		}
		return true;
	}
}
