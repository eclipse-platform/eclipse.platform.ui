/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.data;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.views.*;

public class CheatSheetSaveHelper {
	protected Hashtable stateHash = new Hashtable();
	protected Vector stateVector = new Vector();
	private CheatSheetXMLSaveHelper xmlsh;
	/**
	 * Constructor for CheatSheetSaveHelper.
	 */
	public CheatSheetSaveHelper() {
		super();
		loadDATStates();
	}

	private Properties createProperties(URL urlKey, int stateNum, ViewItem[] myitems, boolean buttonIsDown, ArrayList expandRestoreStates, String csID) {
		Properties props = new Properties();
		Hashtable subcompletedTable = new Hashtable(10);
		Hashtable subskippedTable = new Hashtable(10);

		int buttonState = 0;
		if (buttonIsDown)
			buttonState = 1;

		props.put(IParserTags.CHEATSHEETID, csID);
		props.put(IParserTags.URL, urlKey.toString()); //$NON-NLS-1$
		props.put(IParserTags.CURRENT, Integer.toString(stateNum)); //$NON-NLS-1$
		ArrayList mylist = new ArrayList();
		ArrayList elist = new ArrayList();

		if (expandRestoreStates == null)
			expandRestoreStates = new ArrayList();

		//Assemble lists of expanded items and completed items.
		for (int i = 0; i < myitems.length; i++) {
			ViewItem item = myitems[i];
			if (item.isCompleted()) {
				mylist.add(Integer.toString(i));
			}
			if (item.isExpanded()) {
				elist.add(Integer.toString(i));
			}

			if (item instanceof CoreItem) {
				CoreItem withsubs = (CoreItem) item;
				ArrayList compList = withsubs.getListOfSubItemCompositeHolders();
				if (compList != null) {
					StringBuffer skippedsubItems = new StringBuffer();
					StringBuffer completedsubItems = new StringBuffer();
					for (int j = 0; j < compList.size(); j++) {
						SubItemCompositeHolder sch = (SubItemCompositeHolder) compList.get(j);
						if (sch.isCompleted())
							completedsubItems.append(Integer.toString(j) + ","); //$NON-NLS-1$
						if (sch.isSkipped())
							skippedsubItems.append(Integer.toString(j) + ","); //$NON-NLS-1$
					}
					if (completedsubItems.toString().length() > 0) {
						String csi = completedsubItems.toString();
						if (csi.endsWith(",")) //$NON-NLS-1$
							csi = csi.substring(0, csi.length() - 1);
						subcompletedTable.put(Integer.toString(i), csi);

					}
					if (skippedsubItems.toString().length() > 0) {
						String csi = skippedsubItems.toString();
						if (csi.endsWith(",")) //$NON-NLS-1$
							csi = csi.substring(0, csi.length() - 1);
						subskippedTable.put(Integer.toString(i), csi);
					}
				}
			}
		}

		//put expanded item list, completed list, button state
		props.put(IParserTags.COMPLETED, mylist); //$NON-NLS-1$
		props.put(IParserTags.EXPANDED, elist); //$NON-NLS-1$
		props.put(IParserTags.EXPANDRESTORE, expandRestoreStates); //$NON-NLS-1$
		props.put(IParserTags.BUTTON, Integer.toString(buttonState)); //$NON-NLS-1$
		if (subcompletedTable != null)
			props.put(IParserTags.SUBITEMCOMPLETED, subcompletedTable); //$NON-NLS-1$
		if (subskippedTable != null)
			props.put(IParserTags.SUBITEMSKIPPED, subskippedTable); //$NON-NLS-1$

		return props;
	}

	public Properties getSavedStateProperties(URL urlToCheck, float version) {
		Properties datData = null;
		Properties xmlData = null;

		//Step 1:  Check stateVector for url. (Check for v1 saved data)
		datData = v1DataExists(urlToCheck.toString());
		removeState(urlToCheck.toString());
		saveStates();

		//Step 2:  Check for xml file with data for that url. (V2 saved data)
		if (xmlsh == null)
			xmlsh = new CheatSheetXMLSaveHelper();
		xmlData = xmlsh.loadState(urlToCheck);

		if (xmlData != null & datData != null) {
			return xmlData;
		} else if (xmlData != null) {
			return xmlData;
		} else if (datData != null) {
			return datData;
		} else
			return null;
	}

	private void loadDATStates() {
		//Get a handle to the save location for the cheatsheet plugin.
		IPath savePath = Platform.getPluginStateLocation(CheatSheetPlugin.getPlugin());
		Path filePath = new Path(savePath.append(ICheatSheetResource.CHEAT_SHEET_SAVE_FILE).toOSString());

		FileReader reader = null;
		BufferedReader mybuf = null;

		//Open a file handle.
		try {

			reader = new FileReader(filePath.toFile());
			mybuf = new BufferedReader(reader);

			String line = mybuf.readLine();
			while (line != null) {
				StringTokenizer st = new StringTokenizer(line, ";"); //$NON-NLS-1$
				String url = st.nextToken();
				String itemnum = st.nextToken();
				Properties myprop = new Properties();
				myprop.put(IParserTags.URL, url); //non nls //$NON-NLS-1$
				myprop.put(IParserTags.CURRENT, itemnum); //non nls //$NON-NLS-1$
				ArrayList completedNumStrings = new ArrayList();
				ArrayList expandedNumStrings = new ArrayList();
				ArrayList expandRestore = new ArrayList();
				while (st.hasMoreTokens()) {
					String mytoken = st.nextToken();
					if (mytoken.startsWith("c")) { //$NON-NLS-1$
						completedNumStrings.add(mytoken.substring(1, mytoken.length()));
					} else if (mytoken.startsWith("e")) { //$NON-NLS-1$
						expandedNumStrings.add(mytoken.substring(1, mytoken.length()));
					} else if (mytoken.startsWith("b")) { //$NON-NLS-1$
						myprop.put(IParserTags.BUTTON, mytoken.substring(1, mytoken.length())); //$NON-NLS-1$
					} else if (mytoken.startsWith("h")) { //$NON-NLS-1$
						expandRestore.add(mytoken.substring(1, mytoken.length()));
					}
				}
				myprop.put(IParserTags.COMPLETED, completedNumStrings); //$NON-NLS-1$
				myprop.put(IParserTags.EXPANDED, expandedNumStrings); //$NON-NLS-1$
				myprop.put(IParserTags.EXPANDRESTORE, expandRestore); //$NON-NLS-1$
				stateVector.addElement(myprop);
				line = mybuf.readLine();
			}
			reader.close();
			mybuf.close();

		} catch (FileNotFoundException fnfe) {
		} catch (IOException ioe) {
		}
	}

	public void removeState(String myurl) {
		int index = -1;
		for (int i = 0; i < stateVector.size(); i++) {
			Properties myprop = (Properties) stateVector.elementAt(i);
			String urlstring = (String) myprop.get(IParserTags.URL); //non nls //$NON-NLS-1$
			if (urlstring.equals(myurl))
				index = i;
		}
		if (index > -1) {
			stateVector.removeElementAt(index);
		}
	}

	private void saveStates() {

		IPath savePath = Platform.getPluginStateLocation(CheatSheetPlugin.getPlugin());
		Path filePath = new Path(savePath.append(ICheatSheetResource.CHEAT_SHEET_SAVE_FILE).toOSString());

		FileWriter mywriter = null;

		try {
			mywriter = new FileWriter(filePath.toFile());

			for (int i = 0; i < stateVector.size(); i++) {
				Properties myprop = (Properties) stateVector.elementAt(i);

				String urlstring = (String) myprop.get(IParserTags.URL); //non nls //$NON-NLS-1$
				String number = (String) myprop.get(IParserTags.CURRENT); //non nls //$NON-NLS-1$
				ArrayList mylist = (ArrayList) myprop.get(IParserTags.COMPLETED); //$NON-NLS-1$
				ArrayList elist = (ArrayList) myprop.get(IParserTags.EXPANDED); //$NON-NLS-1$
				ArrayList eRlist = (ArrayList) myprop.get(IParserTags.EXPANDRESTORE); //$NON-NLS-1$
				StringBuffer writeString = new StringBuffer();
				writeString.append(urlstring);
				writeString.append(";"); //$NON-NLS-1$
				writeString.append(number);
				writeString.append(";"); //$NON-NLS-1$

				for (int j = 0; j < mylist.size(); j++) {
					writeString.append("c"); //$NON-NLS-1$
					writeString.append((String) mylist.get(j));
					writeString.append(";"); //$NON-NLS-1$
				}
				for (int j = 0; j < elist.size(); j++) {
					writeString.append("e"); //$NON-NLS-1$
					writeString.append((String) elist.get(j));
					writeString.append(";"); //$NON-NLS-1$
				}
				for (int j = 0; j < eRlist.size(); j++) {
					writeString.append("h"); //$NON-NLS-1$
					writeString.append((String) eRlist.get(j));
					writeString.append(";"); //$NON-NLS-1$
				}

				String newstring = (String) myprop.get(IParserTags.BUTTON); //$NON-NLS-1$
				writeString.append("b"); //$NON-NLS-1$
				writeString.append(newstring);
				writeString.append(";"); //$NON-NLS-1$

				writeString.append("\n"); //$NON-NLS-1$
				mywriter.write(writeString.toString());

				mywriter.close();
			} //End else if version !>= 2.0
		} catch (Exception e) {
		}
	}

	public void saveThisState(URL urlKey, int stateNum, ViewItem[] myitems, boolean buttonIsDown, ArrayList expandRestoreStates, String csID, CheatSheetManager csm) {
		Properties prop = createProperties(urlKey, stateNum, myitems, buttonIsDown, expandRestoreStates, csID);
		saveXMLState(prop, csm, myitems);
	}

	private void saveXMLState(Properties props, CheatSheetManager csm, ViewItem[] items) {
		if (xmlsh == null)
			xmlsh = new CheatSheetXMLSaveHelper();
		xmlsh.saveStates(props, csm, items);
	}

	private Properties v1DataExists(String urlToCheck) {
		duck : for (int i = 0; i < stateVector.size(); i++) {
			Properties myprop = (Properties) stateVector.elementAt(i);
			String urlstring = (String) myprop.get(IParserTags.URL); //non nls //$NON-NLS-1$
			if (urlstring.equals(urlToCheck))
				return myprop;
		}
		return null;
	}
}
