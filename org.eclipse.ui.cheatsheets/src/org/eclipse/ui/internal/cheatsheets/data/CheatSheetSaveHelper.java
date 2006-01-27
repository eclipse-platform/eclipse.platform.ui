/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.views.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class CheatSheetSaveHelper {

	// Get the path to the cheatsheet folder in the .metadata folder of
	// workspace.
	protected IPath savePath;

	protected Vector stateVector = new Vector();

	/**
	 * Constructor for CheatSheetSaveHelper.
	 */
	public CheatSheetSaveHelper() {
		super();
		savePath = Platform
				.getPluginStateLocation(CheatSheetPlugin.getPlugin());
	}

	private Properties createProperties(int currentItemNum, ArrayList items,
			boolean buttonIsDown, ArrayList expandRestoreStates, String csID) {
		Properties props = new Properties();
		Hashtable subcompletedTable = new Hashtable(10);
		Hashtable subskippedTable = new Hashtable(10);

		int buttonState = 0;
		if (buttonIsDown)
			buttonState = 1;

		props.put(IParserTags.ID, csID);
		props.put(IParserTags.CURRENT, Integer.toString(currentItemNum));
		ArrayList completedList = new ArrayList();
		ArrayList expandedList = new ArrayList();

		if (expandRestoreStates == null)
			expandRestoreStates = new ArrayList();

		// Assemble lists of expanded items and completed items.
		for (int i = 0; i < items.size(); i++) {
			ViewItem item = (ViewItem) items.get(i);
			if (item.isCompleted()) {
				completedList.add(Integer.toString(i));
			}
			if (item.isExpanded()) {
				expandedList.add(Integer.toString(i));
			}

			if (item instanceof CoreItem) {
				CoreItem withsubs = (CoreItem) item;
				ArrayList compList = withsubs
						.getListOfSubItemCompositeHolders();
				if (compList != null) {
					StringBuffer skippedsubItems = new StringBuffer();
					StringBuffer completedsubItems = new StringBuffer();
					for (int j = 0; j < compList.size(); j++) {
						SubItemCompositeHolder sch = (SubItemCompositeHolder) compList
								.get(j);
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

		// put expanded item list, completed list, button state
		props.put(IParserTags.COMPLETED, completedList);
		props.put(IParserTags.EXPANDED, expandedList);
		props.put(IParserTags.EXPANDRESTORE, expandRestoreStates);
		props.put(IParserTags.BUTTON, Integer.toString(buttonState));
		if (subcompletedTable != null)
			props.put(IParserTags.SUBITEMCOMPLETED, subcompletedTable);
		if (subskippedTable != null)
			props.put(IParserTags.SUBITEMSKIPPED, subskippedTable);

		return props;
	}

	/**
	 * Method parses attribute from named node map. Returns value as string.
	 */
	protected String getAttributeWithName(NamedNodeMap map, String name) {
		try {
			return map.getNamedItem(name).getNodeValue();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Method parses all elements in nodelist, attempts to pull out same
	 * attribute from each. attributes are put into an array list in order they
	 * occur in node list elements.
	 */
	private ArrayList getMultipleAttributesWithSameName(NodeList nl, String name) {
		ArrayList returnList = new ArrayList();
		for (int i = 0; i < nl.getLength(); i++) {
			String value = nl.item(i).getAttributes().getNamedItem(name)
					.getNodeValue();
			if (value != null)
				returnList.add(value);
		}
		return returnList;
	}

	public Path getStateFile(String csID) {
		return getStateFile(csID, savePath);
	}
	
	protected Path getStateFile(String csID, IPath rootPath) {
		return new Path(rootPath.append(csID + ".xml").toOSString()); //$NON-NLS-1$
	}

	public Properties loadState(String csID, IPath savePath) {
		if (savePath != null) {
			this.savePath = savePath;
	    } else {
		    this.savePath = Platform
		        .getPluginStateLocation(CheatSheetPlugin.getPlugin());
	    }
		Properties returnProps = null;
		Hashtable subskipped = null;
		Hashtable subcompleted = null;

		Path filePath = getStateFile(csID);
		Document doc = null;
		URL readURL = null;

		try {
			readURL = filePath.toFile().toURL();
			doc = readXMLFile(readURL);
		} catch (MalformedURLException mue) {
			String message = NLS.bind(Messages.ERROR_CREATING_STATEFILE_URL,
					(new Object[] { readURL }));
			IStatus status = new Status(IStatus.ERROR,
					ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK,
					message, mue);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return null;
		}

		if (doc != null) {
			// Parse stuff from document here.
			Hashtable ht = null;
			Node rootnode = doc.getDocumentElement();
			NamedNodeMap rootatts = rootnode.getAttributes();
			if (isReference(doc)) {
				String path = getAttributeWithName(rootatts, IParserTags.PATH);
				return loadState(csID, new Path(path));
			}
			String currentID = getAttributeWithName(rootatts, IParserTags.ID);

			String number = getAttributeWithName(doc.getElementsByTagName(
					IParserTags.CURRENT).item(0).getAttributes(),
					IParserTags.ITEM);
			ArrayList completeList = getMultipleAttributesWithSameName(doc
					.getElementsByTagName(IParserTags.COMPLETED),
					IParserTags.ITEM);
			ArrayList expandedList = getMultipleAttributesWithSameName(doc
					.getElementsByTagName(IParserTags.EXPANDED),
					IParserTags.ITEM);
			ArrayList expandRestoreList = getMultipleAttributesWithSameName(doc
					.getElementsByTagName(IParserTags.EXPANDRESTORE),
					IParserTags.ITEM);
			String buttonState = getAttributeWithName(doc.getElementsByTagName(
					IParserTags.BUTTON).item(0).getAttributes(),
					IParserTags.BUTTONSTATE);

			NodeList nl = doc
					.getElementsByTagName(IParserTags.SUBITEMCOMPLETED);
			if (nl != null) {
				subcompleted = new Hashtable(10);
				for (int i = 0; i < nl.getLength(); i++) {
					String item = getAttributeWithName(nl.item(i)
							.getAttributes(), IParserTags.ITEM);
					String subItems = getAttributeWithName(nl.item(i)
							.getAttributes(), IParserTags.SUBITEM);
					subcompleted.put(item, subItems);
				}
			} // end for nl

			NodeList snl = doc.getElementsByTagName(IParserTags.SUBITEMSKIPPED);
			if (snl != null) {
				subskipped = new Hashtable(10);
				for (int i = 0; i < snl.getLength(); i++) {
					String item = getAttributeWithName(snl.item(i)
							.getAttributes(), IParserTags.ITEM);
					String subItems = getAttributeWithName(snl.item(i)
							.getAttributes(), IParserTags.SUBITEM);
					subskipped.put(item, subItems);
				} // end for nl
			}

			NodeList csmDatanl = doc
					.getElementsByTagName(IParserTags.MANAGERDATA);
			if (csmDatanl != null) {
				ht = new Hashtable(30);
				for (int i = 0; i < csmDatanl.getLength(); i++) {
					String key = null;
					try {
						key = getAttributeWithName(csmDatanl.item(i)
								.getAttributes(), IParserTags.MANAGERDATAKEY);
						String data = csmDatanl.item(i).getFirstChild()
								.getNodeValue();
						ht.put(key, data);
					} catch (Exception e) {
						String message = NLS
								.bind(
										Messages.ERROR_READING_MANAGERDATA_FROM_STATEFILE,
										(new Object[] { key, currentID }));
						IStatus status = new Status(IStatus.ERROR,
								ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID,
								IStatus.OK, message, e);
						CheatSheetPlugin.getPlugin().getLog().log(status);
					}
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

	protected boolean isReference(Document doc) {
		Node rootnode = doc.getDocumentElement();
		return IParserTags.CHEATSHEET_STATE_REFERENCE.equals(rootnode.getNodeName());
	}

	// Attempts to read an xml file from the provided url. Returns a Dom
	// Document object if parses ok,
	// returns null if the parse or read fails.
	protected Document readXMLFile(URL url) {
		InputStream is = null;
		InputSource source = null;

		try {
			is = url.openStream();
			if (is != null) {
				source = new InputSource(is);
			}
		} catch (Exception e) {
			return null;
		}

		if (source == null)
			return null;

		try {
			DocumentBuilder documentBuilder = CheatSheetPlugin.getPlugin()
					.getDocumentBuilder();
			return documentBuilder.parse(source);
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

	private void saveState(Properties saveProperties, String contentPath, CheatSheetManager csm) {

		String csID = null;

		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();

			Document doc = documentBuilder.newDocument();

			Properties properties = saveProperties;
			csID = (String) properties.get(IParserTags.ID);
			String number = (String) properties.get(IParserTags.CURRENT);

			Path filePath = getStateFile(csID);

			ArrayList completedList = (ArrayList) properties
					.get(IParserTags.COMPLETED);
			ArrayList expandedList = (ArrayList) properties
					.get(IParserTags.EXPANDED);
			ArrayList expandRestoreList = (ArrayList) properties
					.get(IParserTags.EXPANDRESTORE);
			Hashtable subcompletedTable = (Hashtable) properties
					.get(IParserTags.SUBITEMCOMPLETED);
			Hashtable subskippedTable = (Hashtable) properties
					.get(IParserTags.SUBITEMSKIPPED);

			// Create the root element for the document now:
			Element root = doc.createElement(IParserTags.CHEATSHEET);
			root.setAttribute(IParserTags.ID, csID);
			if (contentPath != null) {
				root.setAttribute(IParserTags.CONTENT_URL, contentPath);
			}
			doc.appendChild(root);

			// create current element.
			Element cEl = doc.createElement(IParserTags.CURRENT);
			cEl.setAttribute(IParserTags.ITEM, number);
			root.appendChild(cEl);

			for (int j = 0; j < completedList.size(); j++) {
				Element compEl = doc.createElement(IParserTags.COMPLETED);
				compEl.setAttribute(IParserTags.ITEM, (String) completedList
						.get(j));
				root.appendChild(compEl);
			}
			for (int j = 0; j < expandedList.size(); j++) {
				Element expandEl = doc.createElement(IParserTags.EXPANDED);
				expandEl.setAttribute(IParserTags.ITEM, (String) expandedList
						.get(j));
				root.appendChild(expandEl);
			}
			for (int j = 0; j < expandRestoreList.size(); j++) {
				Element eRel = doc.createElement(IParserTags.EXPANDRESTORE);
				eRel.setAttribute(IParserTags.ITEM, (String) expandRestoreList
						.get(j));
				root.appendChild(eRel);
			}
			if (subcompletedTable != null) {
				Enumeration enumeration = subcompletedTable.keys();
				while (enumeration.hasMoreElements()) {
					String itemNum = (String) enumeration.nextElement();
					String subItemNum = (String) subcompletedTable.get(itemNum);
					if (itemNum == null || subItemNum == null)
						continue;
					Element eRel = doc
							.createElement(IParserTags.SUBITEMCOMPLETED);
					eRel.setAttribute(IParserTags.ITEM, itemNum);
					eRel.setAttribute(IParserTags.SUBITEM, subItemNum);
					root.appendChild(eRel);
				}
			}
			if (subskippedTable != null) {
				Enumeration enumeration = subskippedTable.keys();
				while (enumeration.hasMoreElements()) {
					String itemNum = (String) enumeration.nextElement();
					String subItemNum = (String) subskippedTable.get(itemNum);
					if (itemNum == null || subItemNum == null)
						continue;
					Element eRel = doc
							.createElement(IParserTags.SUBITEMSKIPPED);
					eRel.setAttribute(IParserTags.ITEM, itemNum);
					eRel.setAttribute(IParserTags.SUBITEM, subItemNum);
					root.appendChild(eRel);
				}
			}
			Element bel = doc.createElement(IParserTags.BUTTON);
			bel.setAttribute(IParserTags.BUTTONSTATE, (String) properties
					.get(IParserTags.BUTTON));
			root.appendChild(bel);

			// Store cheatsheet data here.
			Hashtable managerData = (Hashtable) csm.getData();
			if (managerData != null) {
				Enumeration e = managerData.keys();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					String data = (String) managerData.get(key);
					Element csmDataTag = doc
							.createElement(IParserTags.MANAGERDATA);
					csmDataTag.setAttribute(IParserTags.MANAGERDATAKEY, key);
					Text t = doc.createTextNode(data);
					csmDataTag.appendChild(t);
					root.appendChild(csmDataTag);
				}
			}

			StreamResult streamResult = new StreamResult(filePath.toFile());

			DOMSource domSource = new DOMSource(doc);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.transform(domSource, streamResult);
		} catch (Exception e) {
			String message = NLS.bind(Messages.ERROR_SAVING_STATEFILE_URL,
					(new Object[] { csID }));
			IStatus status = new Status(IStatus.ERROR,
					ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK,
					message, e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * Save the state of a cheatsheet
	 * @param currentItemNum the current item
	 * @param items a list of the items in this cheatsheet
	 * @param buttonIsDown 
	 * @param expandRestoreStates
	 * @param csID the cheatsheet id
	 * @param contentPath will be null if the cheatsheet was launched using information from
	 * the registry, otherwise it is the url of the cheatsheet content file.
	 * @param csm the cheatsheet manager
	 */
	public void saveState(int currentItemNum, ArrayList items,
			boolean buttonIsDown, ArrayList expandRestoreStates, String csID,
			String contentPath, CheatSheetManager csm) {
		Properties properties = createProperties(currentItemNum, items,
				buttonIsDown, expandRestoreStates, csID);
		saveState(properties, contentPath, csm);
	}
	
	public IStatus setSavePath(IPath path, String csID, boolean saveReference) {
		IStatus status = Status.OK_STATUS;
		this.savePath = path;
		if (saveReference) {
			// Store a reference to the new location in the metadata
			DocumentBuilder documentBuilder;
			try {
				documentBuilder = DocumentBuilderFactory
						.newInstance().newDocumentBuilder();
			

				Document doc = documentBuilder.newDocument();
	
				IPath filePath = getStateFile(csID, Platform
						   .getPluginStateLocation(CheatSheetPlugin.getPlugin()));
	
				// Create the root element for the document now:
				Element root = doc.createElement(IParserTags.CHEATSHEET_STATE_REFERENCE);
				root.setAttribute(IParserTags.PATH, path.toOSString());
				doc.appendChild(root);
	
				StreamResult streamResult = new StreamResult(filePath.toFile());
	
				DOMSource domSource = new DOMSource(doc);
				Transformer transformer = TransformerFactory.newInstance()
						.newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
				transformer.transform(domSource, streamResult);
			} catch (ParserConfigurationException e) {
				status = saveErrorStatus(e);
			} catch (FactoryConfigurationError e) {
				status = saveErrorStatus(e);
			} catch (TransformerConfigurationException e) {
				status = saveErrorStatus(e);
			} catch (TransformerFactoryConfigurationError e) {
				status = saveErrorStatus(e);
			} catch (TransformerException e) {
				status = saveErrorStatus(e);
			}
		}
		return status;
	}

	private IStatus saveErrorStatus(Throwable e) {
		return new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, 0, Messages.ERROR_SAVING_STATE_REFERENCE, e);
	}
	
	
	/**
	 * Data needed to locate the content file for a cheatsheet, which is
	 * the contentPath or if that is null the id
	 * 
	 */
	public class RestoreInfo {
		public String id;
		public String contentURL;
	}
	
	/**
	 * Read a saved state file and get the Cheatsheet id and content file path. 
	 * @param url
	 * @return An object containing the id and content file path. The content file
	 * path will be null if this cheatsheet was opened using the registry.
	 */
	public RestoreInfo getIdFromStateFile(URL url) {
		Document doc = null;
		doc = readXMLFile(url);

		if (doc != null) {
			RestoreInfo result = new RestoreInfo();
			// Parse document here.
			Node rootnode = doc.getDocumentElement();
			NamedNodeMap rootatts = rootnode.getAttributes();
			result.id = getAttributeWithName(rootatts, IParserTags.ID);
			if (result.id == null) {
				IStatus status = new Status(IStatus.ERROR,
						ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK,
						Messages.ERROR_READING_ID_FROM_STATEFILE, null);
				CheatSheetPlugin.getPlugin().getLog().log(status);
			}
			result.contentURL = getAttributeWithName(rootatts, IParserTags.CONTENT_URL); // May be null
			return result;
		}
		return null;
	}

	/**
	 * Read a saved state file and get the Cheatsheet id. 
	 * @param url
	 * @return A cheatsheet id, or null if the state file could not be opened
	 * or has invalid format.
	 */
	public IPath getSavePath() {
		return savePath;
	}

}
