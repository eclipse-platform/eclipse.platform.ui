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

public class CheatSheetXMLSaveHelper {

	//Get the path to the cheatsheet folder in the .metadata folder of workspace.
	private IPath savePath;

	public CheatSheetXMLSaveHelper() {
		super();
		savePath = Platform.getPluginStateLocation(CheatSheetPlugin.getPlugin());
	}

//	//For unit testing only.
//	public static void main(String args[]) {
//		CheatSheetXMLSaveHelper xmlsh = new CheatSheetXMLSaveHelper();
//		URL url = null;
//		try {
//			url = new URL("file:D:/eclipse2.1/eclipse/workspace/org.eclipse.ui.cheatsheets.sample/testCheatSheet.xml");
//		} catch (MalformedURLException mue) {
//		}
//		Document doc = xmlsh.readXMLFile(url);
//
//	}

	/*package*/ Properties loadState(URL urlOfContent) {
		Properties returnProps = null;
		Hashtable subskipped = null;
		Hashtable subcompleted = null;

		String urlstring = urlOfContent.toString();
		String fileNameToLoad = urlstring.substring(urlstring.lastIndexOf("/"), urlstring.length()); //$NON-NLS-1$
		//		System.out.println("File name to load: " + fileNameToLoad);
		Path filePath = new Path(savePath.append(fileNameToLoad).toOSString()); //$NON-NLS-1$
		//		System.out.println("Reading from  this file: " + filePath);
		Document doc = null;

		try {
			URL readURL = filePath.toFile().toURL();
			doc = readXMLFile(readURL);
		} catch (MalformedURLException mue) {
			System.err.println("Could not create url of xml file to read in"); //$NON-NLS-1$
			return null;
		}

		if (doc != null) {
			//Parse stuff from document here.
			Hashtable ht = null;
			Node rootnode = doc.getDocumentElement();
			NamedNodeMap rootatts = rootnode.getAttributes();

			String number = getAttributeWithName(doc.getElementsByTagName(IParserTags.CURRENT).item(0).getAttributes(), IParserTags.ITEM);
			String currentID = getAttributeWithName(doc.getElementsByTagName(IParserTags.CHEATSHEETID).item(0).getAttributes(), IParserTags.ID);
			String url = getAttributeWithName(rootatts, IParserTags.URL);
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

			//Parse out the saved steps and sub steps that are dynamic.
			ArrayList dynamicItemDataList = null;
			NodeList dynamicNL = null;//TODO = doc.getElementsByTagName(IParserTags.DYNAMICDATA);
			if (dynamicNL != null) {
				dynamicItemDataList = new ArrayList(30);
				for (int i = 0; i < dynamicNL.getLength(); i++) {
					String itemid = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.ITEM);
/* TODO: Remove this! */
//					String buttonCodes = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.ACTIONPHRASE);
					String aclass = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.CLASS);
					String actionpid = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.PLUGINID);
					ArrayList actionParams = getParamList(dynamicNL.item(i).getAttributes());

					Properties p = new Properties();
					p.put(IParserTags.ITEM, itemid);
/* TODO: Remove this! */
//					p.put(IParserTags.ACTIONPHRASE, buttonCodes);
					p.put(IParserTags.CLASS, aclass);
					p.put(IParserTags.PLUGINID, actionpid);
					p.put(IParserTags.ACTIONPARAM, actionParams.toArray(new String[actionParams.size()]));
					dynamicItemDataList.add(p);
				}
			}

			//Parse out the saved steps and sub steps that are dynamic.
			ArrayList dynamicSubItemDataList = null;
			dynamicNL = null; //TODO doc.getElementsByTagName(IParserTags.DYNAMICSUBITEMDATA);
			if (dynamicNL != null) {
				dynamicSubItemDataList = new ArrayList(30);
				for (int i = 0; i < dynamicNL.getLength(); i++) {
					String itemid = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.ITEM);
					String subitemid = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.SUBITEM);
/* TODO: Remove this! */
//					String buttonCodes = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.ACTIONPHRASE);
					String aclass = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.CLASS);
					String actionpid = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.PLUGINID);
					String subItemLabel = getAttributeWithName(dynamicNL.item(i).getAttributes(), IParserTags.SUBITEMLABEL);
					ArrayList actionParams = getParamList(dynamicNL.item(i).getAttributes());

					Properties p = new Properties();
					p.put(IParserTags.ITEM, itemid);
					p.put(IParserTags.SUBITEM, subitemid);
/* TODO: Remove this! */
//					p.put(IParserTags.ACTIONPHRASE, buttonCodes);
					p.put(IParserTags.CLASS, aclass);
					p.put(IParserTags.PLUGINID, actionpid);
					p.put(IParserTags.SUBITEMLABEL, subItemLabel);
					p.put(IParserTags.ACTIONPARAM, actionParams.toArray(new String[actionParams.size()]));
					dynamicSubItemDataList.add(p);
				}
			}

			//			System.out.println("URL in xml state file read by xml helper is:" + url);
			returnProps = new Properties();
			returnProps.put(IParserTags.CHEATSHEETID, currentID);
			returnProps.put(IParserTags.CURRENT, number);
			returnProps.put(IParserTags.URL, url);
			returnProps.put(IParserTags.COMPLETED, completeList);
			returnProps.put(IParserTags.EXPANDED, expandedList);
			returnProps.put(IParserTags.EXPANDRESTORE, expandRestoreList);
			returnProps.put(IParserTags.BUTTON, buttonState);
			returnProps.put(IParserTags.SUBITEMCOMPLETED, subcompleted);
			returnProps.put(IParserTags.SUBITEMSKIPPED, subskipped);
			returnProps.put(IParserTags.MANAGERDATA, ht);
// TODO
//			returnProps.put(IParserTags.DYNAMICDATA, dynamicItemDataList);
//			returnProps.put(IParserTags.DYNAMICSUBITEMDATA, dynamicSubItemDataList);
		}
		return returnProps;
	}

	private ArrayList getParamList(NamedNodeMap nnm) {
		ArrayList params = new ArrayList();

		try {
			String param = "param"; //$NON-NLS-1$
			for (int j = 0; param != null; j++) {
				String actionparam = nnm.getNamedItem(IParserTags.ACTIONPARAM + j).getNodeValue();
				param = actionparam;
				if (param != null)
					params.add(param);
			}
		} catch (Exception e) {
		}

		return params;
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

	/*package*/ void saveStates(Properties saveProperties, CheatSheetManager csm, ViewItem[] items) {

		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document doc = documentBuilder.newDocument();

			Properties myprop = saveProperties;
			String csID = (String) myprop.get(IParserTags.CHEATSHEETID); //$NON-NLS-1$
			String urlstring = (String) myprop.get(IParserTags.URL); //non nls //$NON-NLS-1$
			String number = (String) myprop.get(IParserTags.CURRENT); //non nls //$NON-NLS-1$

			//			String urlstringWithoutFile = urlstring.substring(0, urlstring.lastIndexOf("/"));
			//			String pluginID = urlstringWithoutFile.substring(urlstringWithoutFile.lastIndexOf("/"), urlstringWithoutFile.length());

			String fileNameToSave = urlstring.substring(urlstring.lastIndexOf("/"), urlstring.length()); //$NON-NLS-1$

			//			System.out.println("File name: " + fileNameToSave);
			Path filePath = new Path(savePath.append(fileNameToSave).toOSString()); //$NON-NLS-1$
			//			System.out.println("Writing to this file: " + filePath);

			ArrayList completedList = (ArrayList) myprop.get(IParserTags.COMPLETED); //$NON-NLS-1$
			ArrayList expandedList = (ArrayList) myprop.get(IParserTags.EXPANDED); //$NON-NLS-1$
			ArrayList expandRestoreList = (ArrayList) myprop.get(IParserTags.EXPANDRESTORE); //$NON-NLS-1$
			Hashtable subcompletedTable = (Hashtable) myprop.get(IParserTags.SUBITEMCOMPLETED); //$NON-NLS-1$
			Hashtable subskippedTable = (Hashtable) myprop.get(IParserTags.SUBITEMSKIPPED); //$NON-NLS-1$

			//Create the root element for the document now:
			Element root = doc.createElement(IParserTags.CHEATSHEET);
			root.setAttribute(IParserTags.URL, urlstring);
			doc.appendChild(root);

			Element idEl = doc.createElement(IParserTags.CHEATSHEETID);
			idEl.setAttribute(IParserTags.ID, csID);
			root.appendChild(idEl);

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

			//Store dynamic single step data here.
			for (int i = 0; i < items.length; i++) {
				ViewItem item = items[i];
				IContainsContent c = item.getContentItem();
				if (c.isDynamic()) {
					if (c instanceof Item) {
						Item ci = (Item) c;
						Element dynamicTag = null; //TODO doc.createElement(IParserTags.DYNAMICDATA);
						dynamicTag.setAttribute(IParserTags.ITEM, ci.getID());
/* TODO: Remove this! */
//						dynamicTag.setAttribute(IParserTags.ACTIONPHRASE, ci.getButtonCodes());
						dynamicTag.setAttribute(IParserTags.CLASS, ci.getActionClass());
						dynamicTag.setAttribute(IParserTags.PLUGINID, ci.getActionPluginID());
						String[] params = ci.getActionParams();
						for (int j = 0; j < params.length; j++)
							dynamicTag.setAttribute(IParserTags.ACTIONPARAM + j, params[j]);
						root.appendChild(dynamicTag);
					} else if (c instanceof ItemWithSubItems) {
						ItemWithSubItems ciws = (ItemWithSubItems) c;
						String itemid = ciws.getID();
						SubItem[] subs = ciws.getSubItems();
						if (subs != null)
							for (int j = 0; j < subs.length; j++) {
								SubItem s = subs[j];
								String subitemid = s.getID();
								Element dynamicTag = null; //TODO doc.createElement(IParserTags.DYNAMICSUBITEMDATA);
								dynamicTag.setAttribute(IParserTags.ITEM, itemid);
								dynamicTag.setAttribute(IParserTags.SUBITEM, subitemid);
/* TODO: Remove this! */
//								dynamicTag.setAttribute(IParserTags.ACTIONPHRASE, s.getButtonCodes());
								dynamicTag.setAttribute(IParserTags.CLASS, s.getActionClass());
								dynamicTag.setAttribute(IParserTags.PLUGINID, s.getActionPluginID());
								dynamicTag.setAttribute(IParserTags.SUBITEMLABEL, s.getLabel());
								String[] params = s.getActionParams();
								if(params != null)
								for (int k = 0; k < params.length; k++)
									dynamicTag.setAttribute(IParserTags.ACTIONPARAM + k, params[k]);
								root.appendChild(dynamicTag);
							}
					}
				}
			}

			StreamResult streamResult = new StreamResult(filePath.toFile());
			
			DOMSource domSource = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.transform(domSource, streamResult);

//			OutputFormat format = new OutputFormat(doc);
//			FileWriter fw = new FileWriter(filePath.toOSString());
//			PrintWriter pw = new PrintWriter(fw);
//			XMLSerializer serial = new XMLSerializer(pw, format);
//			serial.serialize(doc.getDocumentElement());
//			
//			pw.close();
//			fw.close();
		} catch (Exception e) {
		}
		finally {
			
		}
	}
}
