package org.eclipse.help.internal.contributors.xml;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import java.util.*;
import java.io.*;
import java.net.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.contributors.*;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.contributions.*;

import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.server.TempURL;

/**
 * A proxy to the real contribution manager.
 * Since the online contribution are on the server
 * this should first copy the information locally and
 * then create a real contribution manager to handle
 * the calls.
 */
public class HelpContributionManagerProxy implements ContributionManager {
	/** The real contribution manager */
	private HelpContributionManager contributionManager;

	/** Empty iterator*/
	private static Iterator emptyIterator = (new ArrayList(0)).iterator();
	/** the infosets obtained from the server */
	private ArrayList infosets = null;

	/**
	 * HelpContributionManagerProxy constructor comment.
	 */
	public HelpContributionManagerProxy() {
		super();
	}
	/**
	 * Not supported on the proxy.
	 */
	public Iterator getContributingPlugins() {
		return emptyIterator;
	}
	/**
	 * For remote install we only return the infosets
	 */
	public Iterator getContributionsOfType(String typeName) {
		if (!typeName.equals(ViewContributor.INFOSET_ELEM))
			return emptyIterator;

		// we already got them, so return it
		if (infosets != null)
			return infosets.iterator();

		// get them from the server
		infosets = new ArrayList();
		InputStream in = null;
		try {
			URL remoteInfosetFile =
				new URL(
					HelpSystem.getRemoteHelpServerURL(),
					HelpSystem.getRemoteHelpServerPath()
						+ "/"
						+ TempURL.getPrefix()
						+ "/infosets.tab");
			// may want to define constants as a resource

			if (Logger.DEBUG)
				Logger.logDebugMessage(
					"HelpContributionManagerProxy",
					"Loading infosets= " + remoteInfosetFile.toExternalForm());

			BufferedReader reader = null;
			try {
				reader =
					new BufferedReader(new InputStreamReader(remoteInfosetFile.openStream()));
			} catch (Exception ioe) {
				Logger.logError("Could not copy the infoset data from server", ioe);
				return emptyIterator;
			}

			String line;
			while ((line = reader.readLine()) != null) {
				StringTokenizer tokens =
					new StringTokenizer(line, PersistentMap.columnSeparator, true);
				HelpInfoSet infoset = new HelpInfoSet(null);
				infoset.setID(tokens.nextToken());
				infoset.setRawLabel(tokens.nextToken());
				infosets.add(infoset);
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
		} finally {
			return infosets.iterator();
		}
	}
	/**
	 */
	public Iterator getContributionsOfType(String pluginId, String typeName) {
		return emptyIterator;
	}
	/**
	 */
	public boolean hasNewContributions() {
		return false;
	}
	/** Saves the contribution info */
	public void versionContributions() {
	}
}
