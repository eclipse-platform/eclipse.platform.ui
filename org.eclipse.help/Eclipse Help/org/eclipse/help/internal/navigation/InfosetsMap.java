package org.eclipse.help.internal.navigation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.net.URL;
import java.util.StringTokenizer;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.server.TempURL;

/**
 * Persistent Hashtable with keys and values of type String.
 */
public class InfosetsMap extends PersistentMap {
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
							+ "/infosets.tab");

				if (Logger.DEBUG)
					Logger.logDebugMessage(
						"InfosetsMap",
						"Loading infosets= " + remoteInfosetFile.toExternalForm());

				BufferedReader reader = null;
				try {
					reader =
						new BufferedReader(new InputStreamReader(remoteInfosetFile.openStream()));
				} catch (Exception ioe) {
					Logger.logError("Could not copy the infoset data from server", ioe);
					return false;
				}

				String line;
				while ((line = reader.readLine()) != null) {
					StringTokenizer tokens = new StringTokenizer(line, this.columnSeparator, true);
					put(tokens.nextToken(), tokens.nextToken());
				}
				try {
					in.close();
				} catch (Exception ioe) {
				}
			} catch (Exception ioe) {
				Logger.logError("Could not copy the infoset data from server", ioe);
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
