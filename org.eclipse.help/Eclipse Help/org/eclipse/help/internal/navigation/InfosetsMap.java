package org.eclipse.help.internal.navigation;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.net.URL;
import java.util.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
import org.eclipse.help.internal.server.TempURL;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.contributors.xml.*;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.*;
/*
 * Persistent Hashtable with keys and values of type String.
 */
public class InfosetsMap extends Hashtable {
	public static final String INFOSETS_FILENAME = "infosets.xml";
	File infosetsFile = null;
	/**
	 * Creates empty table for storing valid Info Sets.
	 * @param name name of the table;
	 */
	public InfosetsMap() {
		super();
		infosetsFile =
			HelpPlugin
				.getDefault()
				.getStateLocation()
				.addTrailingSeparator()
				.append(INFOSETS_FILENAME)
				.toFile();

	}
	public void save() {
		XMLGenerator gen = new XMLGenerator(infosetsFile);
		gen.println("<infosets>");
		gen.pad++;
		for (Enumeration en = keys(); en.hasMoreElements();) {
			Object infosetID = en.nextElement();
			gen.printPad();
			gen.print("<infoset id=\"");
			gen.print(infosetID);
			gen.print("\" label=\"");
			gen.print(get(infosetID));
			gen.println("\"/>");
		}
		gen.pad--;
		gen.println("</infosets>");
		gen.close();
	}
	/**
	 * Restores contents of the table from a file or from the server,
	 * if called on the client.
	 * @return true if persistant data was read in
	 */
	public boolean restore() {
		if (!this.isEmpty())
			clear();

		InputStream input = null;
		try {
			InputSource source = null;
			if (HelpSystem.isClient()) {
				URL remoteInfosetFile =
					new URL(
						HelpSystem.getRemoteHelpServerURL(),
						HelpSystem.getRemoteHelpServerPath()
							+ "/"
							+ TempURL.getPrefix()
							+ "/"
							+ INFOSETS_FILENAME);
				if (Logger.DEBUG)
					Logger.logDebugMessage(
						"InfosetsMap",
						"Loading infosets= " + remoteInfosetFile.toExternalForm());

				input = remoteInfosetFile.openStream();
				source = new InputSource(input);
				// set SystemId for error reporting only
				source.setSystemId(remoteInfosetFile.toExternalForm());
			} else {
				String xmlFile =
					HelpPlugin
						.getDefault()
						.getStateLocation()
						.append(INFOSETS_FILENAME)
						.toOSString();
				input = new FileInputStream(xmlFile);
				source = new InputSource(input);
				// set id info for parser exceptions.
				// use toString method to capture protocol...etc
				source.setSystemId(xmlFile);
			}
			ContributionParser parser =
				new ContributionParser(new InfosetsContributionFactory().instance());
			if(source==null)
				return false;
			parser.parse(source);

			Iterator infosetsIt = parser.getContribution().getChildren();
			while (infosetsIt.hasNext()) {
				Object o = infosetsIt.next();
				if (o instanceof InfoSet) {
					InfoSet iset = (InfoSet) o;
					if (iset.getID() != null && iset.getID() != "" && iset.getLabel() != null)
						put(iset.getID(), iset.getRawLabel());
				}
			}

		} catch (SAXException se) {
			Logger.logError("E016", se);
				return false;
		} catch (Exception e) {
			Logger.logError("E013", e);
				return false;
		} finally {
			try {
				if (input != null)
					input.close();
			} catch (IOException e) {
			}
		}
		return true;
	}
}