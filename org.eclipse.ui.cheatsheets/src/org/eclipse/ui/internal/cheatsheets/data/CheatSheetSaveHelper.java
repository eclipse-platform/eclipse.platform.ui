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
import java.net.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.cheatsheets.CheatSheetPlugin;
import org.eclipse.ui.internal.cheatsheets.views.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class CheatSheetSaveHelper {
	//Get the path to the cheatsheet folder in the .metadata folder of workspace.
	private IPath savePath;
	protected Vector stateVector = new Vector();

	/**
	 * Constructor for CheatSheetSaveHelper.
	 */
	public CheatSheetSaveHelper() {
		super();
		savePath = Platform.getPluginStateLocation(CheatSheetPlugin.getPlugin());
	}

	private Properties createProperties(int stateNum, ViewItem[] myitems, boolean buttonIsDown, ArrayList expandRestoreStates, String csID) {
		Properties props = new Properties();
		Hashtable subcompletedTable = new Hashtable(10);
		Hashtable subskippedTable = new Hashtable(10);

		int buttonState = 0;
		if (buttonIsDown)
			buttonState = 1;

		props.put(IParserTags.ID, csID);
		props.put(IParserTags.CURRENT, Integer.toString(stateNum));
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

	/**
	 * Method parses attribute from named node map.  Returns value as string.
	 */
	private String getAttributeWithName(NamedNodeMap map, String name) {
		try {
			return map.getNamedItem(name).getNodeValue();
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * Method parses all elements in nodelist, attempts to pull out same attribute from each.
	 * attributes are put into an array list in order they occur in node list elements.
	 */
	private ArrayList getMultipleAttributesWithSameName(NodeList nl, String name) {
		ArrayList returnList = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			String value = nl.item(i).getAttributes().getNamedItem(name).getNodeValue();
			if (value != null)
				returnList.add(value);
		}
		return returnList;
	}

	private ArrayList getParamList(NamedNodeMap nnm) {
		ArrayList params = new ArrayList();

		try {
			String param = "param"; //$NON-NLS-1$
			for (int j = 0; param != null; j++) {
				String actionparam = nnm.getNamedItem(IParserTags.PARAM + j).getNodeValue();
				param = actionparam;
				if (param != null)
					params.add(param);
			}
		} catch (Exception e) {
		}

		return params;
	}

	public Properties loadState(String csID) {
		Properties returnProps = null;
		Hashtable subskipped = null;
		Hashtable subcompleted = null;

		Path filePath = new Path(savePath.append(csID+".xml").toOSString()); //$NON-NLS-1$
		Document doc = null;

		try {
			URL readURL = filePath.toFile().toURL();
			doc = readXMLFile(readURL);
		} catch (MalformedURLException mue) {
			//TODO: NLS!
			System.err.println("Could not create url of xml file to read in");
			return null;
		}

		if (doc != null) {
			//Parse stuff from document here.
			Hashtable ht = null;
			Node rootnode = doc.getDocumentElement();
			NamedNodeMap rootatts = rootnode.getAttributes();
			String currentID = getAttributeWithName(rootatts, IParserTags.ID);

			String number = getAttributeWithName(doc.getElementsByTagName(IParserTags.CURRENT).item(0).getAttributes(), IParserTags.ITEM);
			ArrayList completeList = getMultipleAttributesWithSameName(doc.getElementsByTagName(IParserTags.COMPLETED), IParserTags.ITEM);
			ArrayList expandedList = getMultipleAttributesWithSameName(doc.getElementsByTagName(IParserTags.EXPANDED), IParserTags.ITEM);
			ArrayList expandRestoreList = getMultipleAttributesWithSameName(doc.getElementsByTagName(IParserTags.EXPANDRESTORE), IParserTags.ITEM);
			String buttonState = getAttributeWithName(doc.getElementsByTagName(IParserTags.BUTTON).item(0).getAttributes(), IParserTags.BUTTONSTATE);

			NodeList nl = doc.getElementsByTagName(IParserTags.SUBITEMCOMPLETED);
			if (nl != null)
				subcompleted = new Hashtable(10);
			for (int i = 0; i < nl.getLength(); i++) {
				String item = getAttributeWithName(nl.item(i).getAttributes(), IParserTags.ITEM);
				String subItems = getAttributeWithName(nl.item(i).getAttributes(), IParserTags.SUBITEM);
				subcompleted.put(item, subItems);
			} //end for nl			

			NodeList snl = doc.getElementsByTagName(IParserTags.SUBITEMSKIPPED);
			if (snl != null) {
				subskipped = new Hashtable(10);
				for (int i = 0; i < snl.getLength(); i++) {
					String item = getAttributeWithName(snl.item(i).getAttributes(), IParserTags.ITEM);
					String subItems = getAttributeWithName(snl.item(i).getAttributes(), IParserTags.SUBITEM);
					subskipped.put(item, subItems);
				} //end for nl
			}
//TODO: Need to fix/handle null data or keys more gracefully!
			NodeList csmDatanl = doc.getElementsByTagName(IParserTags.MANAGERDATA);
			if (csmDatanl != null) {
				ht = new Hashtable(30);
				for (int i = 0; i < csmDatanl.getLength(); i++) {
					String key = getAttributeWithName(csmDatanl.item(i).getAttributes(), IParserTags.MANAGERDATAKEY);
					String data = csmDatanl.item(i).getFirstChild().getNodeValue();
					ht.put(key, data);
				}
			}

			returnProps = new Properties();
			returnProps.put(IParserTags.ID, currentID);
			returnProps.put(IParserTags.CURRENT, number);
			returnProps.put(IParserTags.COMPLETED, completeList);
			returnProps.put(IParserTags.EXPANDED, expandedList);
			returnProps.put(IParserTags.EXPANDRESTORE, expandRestoreList);
			returnProps.put(IParserTags.BUTTON, buttonState);
			returnProps.put(IParserTags.SUBITEMCOMPLETED, subcompleted);
			returnProps.put(IParserTags.SUBITEMSKIPPED, subskipped);
			returnProps.put(IParserTags.MANAGERDATA, ht);
		}
		return returnProps;
	}

	//Attempts to read an xml file from the provided url.  Returns a Dom Document object if parses ok,
	//returns null if the parse or read fails.
	private Document readXMLFile(URL url) {
		InputStream is = null;
		InputSource mysource = null;

		try {
			is = url.openStream();
			if (is != null) {
				mysource = new InputSource(is);
			}
		} catch (Exception e) {
			return null;
		}

		if (mysource == null)
			return null;

		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return documentBuilder.parse(mysource);
		} catch (Exception e) {
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException ioe) {
			}
		}

		return null;
	}

	private void saveState(Properties saveProperties, CheatSheetManager csm, ViewItem[] items) {

		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document doc = documentBuilder.newDocument();

			Properties myprop = saveProperties;
			String csID = (String) myprop.get(IParserTags.ID); //$NON-NLS-1$
			String number = (String) myprop.get(IParserTags.CURRENT); //non nls //$NON-NLS-1$

			Path filePath = new Path(savePath.append(csID+".xml").toOSString()); //$NON-NLS-1$

			ArrayList completedList = (ArrayList) myprop.get(IParserTags.COMPLETED); //$NON-NLS-1$
			ArrayList expandedList = (ArrayList) myprop.get(IParserTags.EXPANDED); //$NON-NLS-1$
			ArrayList expandRestoreList = (ArrayList) myprop.get(IParserTags.EXPANDRESTORE); //$NON-NLS-1$
			Hashtable subcompletedTable = (Hashtable) myprop.get(IParserTags.SUBITEMCOMPLETED); //$NON-NLS-1$
			Hashtable subskippedTable = (Hashtable) myprop.get(IParserTags.SUBITEMSKIPPED); //$NON-NLS-1$

			//Create the root element for the document now:
			Element root = doc.createElement(IParserTags.CHEATSHEET);
			root.setAttribute(IParserTags.ID, csID);
			doc.appendChild(root);

			//create current element.
			Element cEl = doc.createElement(IParserTags.CURRENT);
			cEl.setAttribute(IParserTags.ITEM, number);
			root.appendChild(cEl);

			for (int j = 0; j < completedList.size(); j++) {
				Element compEl = doc.createElement(IParserTags.COMPLETED);
				compEl.setAttribute(IParserTags.ITEM, (String) completedList.get(j));
				root.appendChild(compEl);
			}
			for (int j = 0; j < expandedList.size(); j++) {
				Element expandEl = doc.createElement(IParserTags.EXPANDED);
				expandEl.setAttribute(IParserTags.ITEM, (String) expandedList.get(j));
				root.appendChild(expandEl);
			}
			for (int j = 0; j < expandRestoreList.size(); j++) {
				Element eRel = doc.createElement(IParserTags.EXPANDRESTORE);
				eRel.setAttribute(IParserTags.ITEM, (String) expandRestoreList.get(j));
				root.appendChild(eRel);
			}
			if (subcompletedTable != null) {
				Enumeration enum = subcompletedTable.keys();
				while (enum.hasMoreElements()) {
					String itemNum = (String) enum.nextElement();
					String subItemNum = (String) subcompletedTable.get(itemNum);
					if (itemNum == null || subItemNum == null)
						continue;
					Element eRel = doc.createElement(IParserTags.SUBITEMCOMPLETED);
					eRel.setAttribute(IParserTags.ITEM, itemNum);
					eRel.setAttribute(IParserTags.SUBITEM, subItemNum);
					root.appendChild(eRel);
				}
			}
			if (subskippedTable != null) {
				Enumeration enum = subskippedTable.keys();
				while (enum.hasMoreElements()) {
					String itemNum = (String) enum.nextElement();
					String subItemNum = (String) subskippedTable.get(itemNum);
					if (itemNum == null || subItemNum == null)
						continue;
					Element eRel = doc.createElement(IParserTags.SUBITEMSKIPPED);
					eRel.setAttribute(IParserTags.ITEM, itemNum);
					eRel.setAttribute(IParserTags.SUBITEM, subItemNum);
					root.appendChild(eRel);
				}
			}
			Element bel = doc.createElement(IParserTags.BUTTON);
			bel.setAttribute(IParserTags.BUTTONSTATE, (String) myprop.get(IParserTags.BUTTON)); //$NON-NLS-1$
			root.appendChild(bel);

			//Store cheatsheet data here.
			Hashtable managerData = (Hashtable)csm.getData();
			if (managerData != null) {
				Enumeration e = managerData.keys();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					String data = (String) managerData.get(key);
					Element csmDataTag = doc.createElement(IParserTags.MANAGERDATA);
					csmDataTag.setAttribute(IParserTags.MANAGERDATAKEY, key); //$NON-NLS-1$
					Text t = doc.createTextNode(data);
					csmDataTag.appendChild(t);
					root.appendChild(csmDataTag);
				}
			}

			StreamResult streamResult = new StreamResult(filePath.toFile());
			
			DOMSource domSource = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.transform(domSource, streamResult);
		} catch (Exception e) {
			//TODO : log exception
		} finally {
			//TODO : need to close resources?
		}
	}

	public void saveState(int stateNum, ViewItem[] myitems, boolean buttonIsDown, ArrayList expandRestoreStates, String csID, CheatSheetManager csm) {
		Properties prop = createProperties(stateNum, myitems, buttonIsDown, expandRestoreStates, csID);
		saveState(prop, csm, myitems);
	}

}
