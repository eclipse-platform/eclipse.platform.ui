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

import javax.xml.parsers.*;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;

/**
 * Parser for the cheatsheet content files.
 * 
 * Construct an intance of the CheatSheetDomParser.
 * Call <code>parse()</code>.
 * Then get the content items by calling
 * <code>getIntroItem()</code> and <code>getItemsList()</code>.
 * The title of the cheatsheet can be retrieved by calling
 * <code>getTitle()</code>.
 * 
 */
public class CheatSheetDomParser {
	private static final String TRUE_STRING = "true"; //$NON-NLS-1$
	private static final String KIND_ACTION = "action"; //$NON-NLS-1$
	private static final String KIND_MANUAL = "manual"; //$NON-NLS-1$
	private static final String KIND_BOTH = "both"; //$NON-NLS-1$

	private float csversion;
	private DocumentBuilder documentBuilder;
	private ArrayList idChecker;
	private Item introItem;
	private ArrayList itemExtensionContainerList;
	private ArrayList items;
	private Document myDocument;
	private InputSource mysource;
	private String title;
	private URL url;

	/**
	 * ConvertJava constructor comment.
	 */
	public CheatSheetDomParser(URL url) {
		super();
		this.url = url;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
		}

		items = new ArrayList();
	}

	private boolean checkThisID(String idToCheck) {
		if (idChecker.contains(idToCheck))
			return true;
		return false;
	}

	private String formatBodyString(String bodyString) {
		int hasNewLine = 0;
		int hasTab = 0;
		int hasRetLine = 0;

		hasNewLine = bodyString.indexOf("\n"); //$NON-NLS-1$
		if (hasNewLine != -1)
			bodyString = bodyString.replace('\n', ' ');

		hasRetLine = bodyString.indexOf("\r"); //$NON-NLS-1$
		if (hasRetLine != -1)
			bodyString = bodyString.replace('\r', ' ');

		hasTab = bodyString.indexOf("\t"); //$NON-NLS-1$
		if (hasTab != -1)
			bodyString = bodyString.replace('\t', ' ');

		//now take out double/tripple/etc. spaces.
		char last = 'x';
		char current = 'x';
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < bodyString.length(); i++) {
			current = bodyString.charAt(i);

			if (current != ' ')
				sb.append(current);
			else {
				if (last != ' ')
					sb.append(current);
			}
			last = current;
		}

		return sb.toString();
	}

	public float getCsversion() {
		return (csversion != 0.0) ? csversion : 1.0f;
	}

	/**
	 * Returns the intro item.
	 */
	public Item getIntroItem() {
		return introItem;
	}

	/**
	 * Returns the items.
	 */
	public ArrayList getItems() {
		return items;
	}

	private ArrayList getParamList(NamedNodeMap nnm) {
		//		System.out.println("Parsing parameters for actions");
		ArrayList params = new ArrayList();

		try {
			String param = "param"; //$NON-NLS-1$
			for (int j = 0; param != null; j++) {
				String actionparam = nnm.getNamedItem(IParserTags.ACTIONPARAM + j).getNodeValue();
				//				System.out.println("Action parameter found: " + actionparam);
				param = actionparam;
				if (param != null)
					params.add(param);
			}
		} catch (Exception e) {
		}

		return params;
	}

	/**
	 * Gets the title.
	 * @return Returns a String
	 */
	public String getTitle() {
		return title;
	}

	private AbstractItemExtensionElement[] handleUnknownItemAttribute(Node item) {
		ArrayList al = new ArrayList();
		if (itemExtensionContainerList == null)
			return null;

		for (int i = 0; i < itemExtensionContainerList.size(); i++) {
			AbstractItemExtensionElement itemElement = (AbstractItemExtensionElement) itemExtensionContainerList.get(i);
			String itemExtensionAtt = itemElement.getAttributeName();

			if (itemExtensionAtt.equals(item.getNodeName())) {
				// TODO (lorne) - please verify that item.getNodeValue() returns attribute string value sans extra quotes
				// i.e., for attr="xxx" we want to pass "xxx" not "\"xxx\""
				itemElement.handleAttribute(item.getNodeValue());
				al.add(itemElement);
			}
		}

		return (AbstractItemExtensionElement[])al.toArray(new AbstractItemExtensionElement[al.size()]);
	}

	public boolean parse() {
		InputStream is = null;

		try {
			is = url.openStream();

			if (is != null) {
				mysource = new InputSource(is);
			}
		} catch (Exception e) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), null, status);
			return false;
		}

		try {
			if(documentBuilder == null) {
				throw new Exception("Exception");
			}
			myDocument = documentBuilder.parse(mysource);
		} catch (IOException e) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE_IN_PARSER), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return false;
		} catch (SAXParseException spe) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_SAX_PARSING), spe);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return false;
		} catch (SAXException se) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_SAX_PARSING), se);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return false;
		} catch (Exception se) {
			//TODO: Needs to update the error message
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_SAX_PARSING), se);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return false;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}

		// If the document passed is null return a null tree and update the status
		if (myDocument != null) {

			Node rootnode = myDocument.getDocumentElement();
			NamedNodeMap rootatts = rootnode.getAttributes();
			setTitle(rootatts.getNamedItem(IParserTags.TITLE).getNodeValue());

			//Must be in a try catch here, the named item may not be there if old cs version of xml.
			String fversion = null;
			try {
				fversion = rootatts.getNamedItem(IParserTags.VERSION).getNodeValue();
			} catch (Exception e) {
			}

			if (fversion != null) {
				try {
					csversion = Float.parseFloat(fversion);
				} catch (Exception e) {
				}
			}

			// Version Stuff, reads version number here.
			//			String printString = (fversion != null) ? "Version Number Found: " + fversion : "No Version Number";
			//			System.out.println(printString);

			//Get the Intro item.
			NodeList introlist = myDocument.getElementsByTagName(IParserTags.INTRO);
			Node introelement = introlist.item(0);
			NamedNodeMap mymap = introelement.getAttributes();
			String introTitle = null;
			String introHref = null;

			introTitle = CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEET_INTRO_TITLE);
			try {
				introHref = mymap.getNamedItem(IParserTags.HREF).getNodeValue();
			} catch (Exception e) {
			}

			String introtext = introelement.getFirstChild().getNodeValue();
			// comment out for defect 25997 undo text formatting.introtext = formatBodyString(introtext);
			//introtext = formatBodyString(introtext);
			introItem = new Item(introTitle, introtext, introHref);

			//Get the items.
			NodeList itemList = myDocument.getElementsByTagName(IParserTags.ITEM);
			//parse the items.  assemble CheatSheetItem objects.
			items = parseItems(itemList);

			if (items == null) {
				IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_PARSING_ITEMS), null);
				CheatSheetPlugin.getPlugin().getLog().log(status);
				org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE_TITLE), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_PARSING_ITEMS), status);
				return false;
			}

			return true;
		} else {
			//System.err.println("Null document");
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), null);
			CheatSheetPlugin.getPlugin().getLog().log(status);

			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE_TITLE), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), status);
			return false;
		}

	}

	private ArrayList parseItems(NodeList itemList) {
		CheatSheetRegistryReader reader = CheatSheetRegistryReader.getInstance();
		itemExtensionContainerList = reader.readItemExtensions();

		ArrayList localList = new ArrayList();
		Vector counterslist = new Vector();
		idChecker = new ArrayList();
		counterslist.addElement(new Integer(0));

		for (int i = 0; i < itemList.getLength(); i++) {
			//get the item node.  
			Node itemnode = itemList.item(i);
			NodeList itemchildren = itemnode.getChildNodes();

			boolean perform = false;
			boolean skip = false;
			boolean complete = false;
			String title = null;
			String actionPid = null;
			String actionClass = null;
			String topicHref = null;
			boolean dynamic = false;
			String itemID = null;

			StringBuffer bodyText = new StringBuffer();

			//			System.out.println("Checking for sub items.");
			NodeList subItems = itemnode.getChildNodes();
			ArrayList subItemArrayList = null;
			ArrayList l = null;

			//Gather sub items and add them to the sub item array list.  this will be parsed later.
			for (int m = 0; m < subItems.getLength(); m++) {
				Node n = subItems.item(m);
				if (n.getNodeName().equals(IParserTags.SUBITEM)) {
					if (subItemArrayList == null)
						subItemArrayList = new ArrayList(10);
					subItemArrayList.add(n);
				}
			}

			ArrayList itemExtensionElements = new ArrayList();

			NamedNodeMap itematts = itemnode.getAttributes();
			if (itematts != null) {
				for (int x = 0; x < itematts.getLength(); x++) {
					Node item = itematts.item(x);
					String attName = item.getNodeName();
					if (item == null || attName == null)
						continue;

					if (attName.equals(IParserTags.TITLE)) {
						title = item.getNodeValue();
					} else if (attName.equals(IParserTags.ID)) {
						itemID = item.getNodeValue();
						if (itemID != null) {
							if (checkThisID(itemID))
								return null;
						} else {
							return null;
						}
					} else if (attName.equals(IParserTags.PLUGINID)) {
						actionPid = item.getNodeValue();
					} else if (attName.equals(IParserTags.CLASS)) {
						actionClass = item.getNodeValue();
					} else if (attName.equals(IParserTags.HREF)) {
						topicHref = item.getNodeValue();
					} else if (attName.equals(IParserTags.KIND)) {
						String value = item.getNodeValue();
						if(value != null) {
							if(value.equals(KIND_ACTION)) {
								perform = true;
							} else if(value.equals(KIND_MANUAL)) {
								complete = true;
							} else if(value.equals(KIND_BOTH)) {
								perform = true;
								complete = true;
							} else {
								return null;
							}
						} else {
							return null;
						}
					} else if (attName.equals(IParserTags.SKIP)) {
						skip = item.getNodeValue().equals(TRUE_STRING);
					} else {
						AbstractItemExtensionElement[] ie = handleUnknownItemAttribute(item);
						if (ie != null)
							itemExtensionElements.add(ie);
					}
				}
				//Set the action parameters if there are any.
				l = getParamList(itematts);
			}

			//Get the text for the description here.  Assemble a string, then remove new line chars.
			for (int j = 0; j < itemchildren.getLength(); j++) {
				Node child = itemchildren.item(j);
				if (child.getNodeValue() != null && child.getNodeType() == Node.TEXT_NODE) {
					bodyText.append(child.getNodeValue());
				}
			}

			//remove the new line and form feed chars here.
			//comment out for defect 25997 String bodyString = formatBodyString(bodyText.toString());
			//String bodyString = formatBodyString(bodyText.toString());
			String bodyString = bodyText.toString();

			//Create the cheatsheetitem and fill it with data.
			if (subItemArrayList == null) {
				Item itemtoadd = new Item();
				itemtoadd.setActionPluginID(actionPid);
				itemtoadd.setActionClass(actionClass);
				itemtoadd.setHref(topicHref);
				itemtoadd.setTitle(title);
				itemtoadd.setActionParams((String[]) l.toArray(new String[l.size()]));
				itemtoadd.setPerform(perform);
				itemtoadd.setSkip(skip);
				itemtoadd.setComplete(complete);
				itemtoadd.setText(bodyString);
				itemtoadd.setIsDynamic(dynamic);
				itemtoadd.setID(itemID);
				if (itemExtensionElements != null)
					itemtoadd.setItemExtensions(itemExtensionElements);

				//Add the item to the list of items to build the view from.
				localList.add(itemtoadd);
			} else {
				ItemWithSubItems itemtoadd = new ItemWithSubItems();
				itemtoadd.setHref(topicHref);
				itemtoadd.setTitle(title);
				itemtoadd.setText(bodyString);
				itemtoadd.setIsDynamic(dynamic);
				itemtoadd.setID(itemID);
				//Parse the sub items for this item and add it to the sub item list.
				ArrayList subs = parseSubItems(subItemArrayList);
				if (subs == null || subs.size() <=1 )
					return null;
				itemtoadd.addSubItems((SubItem[]) subs.toArray(new SubItem[subs.size()]));

				if (itemExtensionElements != null)
					itemtoadd.setItemExtensions(itemExtensionElements);

				//Add the item to the list of items to build the view from.
				localList.add(itemtoadd);
			}
			idChecker.add(itemID);
		}
		return localList;
	}

	//Returns an array list full of SubContentItems.
	private ArrayList parseSubItems(ArrayList sil) {
		//		System.out.println("Parsing sub items.");
		if (sil == null || sil.size() == 0)
			return null;
		ArrayList subItemList = new ArrayList(10);

		String label = null;
		String actionPid = null;
		String actionClass = null;
		boolean perform = false;
		boolean skip = false;
		boolean complete = false;
		ArrayList actionParamList = null;
		String subItemID = null;

		for (int i = 0; i < sil.size(); i++) {
			Node n = (Node) sil.get(i);
			NamedNodeMap nnm = n.getAttributes();
			try {
				Node node = nnm.getNamedItem(IParserTags.ID);
				if(node != null) {
					subItemID = node.getNodeValue();
					if (subItemID == null) {
						return null;
					}
				}
			} catch (Exception e) {
				return null;
			}
			try {
				label = nnm.getNamedItem(IParserTags.LABEL).getNodeValue();
				if (label == null)
					return null;
			} catch (Exception e) {
				return null;
			}
			try {
				String value = nnm.getNamedItem(IParserTags.SKIP).getNodeValue();
				if(value != null)
					skip = value.equals(TRUE_STRING);
			} catch (Exception e) {
				skip = false;
			}
			try {
				String value = nnm.getNamedItem(IParserTags.KIND).getNodeValue();
				if(value != null) {
					if(value.equals(KIND_ACTION)) {
						perform = true;
					} else if(value.equals(KIND_MANUAL)) {
						complete = true;
					} else if(value.equals(KIND_BOTH)) {
						perform = true;
						complete = true;
					} else {
						return null;
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			try {
				actionPid = nnm.getNamedItem(IParserTags.PLUGINID).getNodeValue();
			} catch (Exception e) {
				actionPid = null;
			}
			try {
				actionClass = nnm.getNamedItem(IParserTags.CLASS).getNodeValue();
			} catch (Exception e) {
				actionClass = null;
			}
			try {
				actionParamList = getParamList(nnm);
			} catch (Exception e) {
				actionParamList = null;
			}
			SubItem sub = new SubItem();
			sub.setID(subItemID);
			sub.setLabel(label);
			//			sub.setSuperItem(superItem);
			sub.setActionPluginID(actionPid);
			sub.setActionClass(actionClass);
			sub.setPerform(perform);
			sub.setSkip(skip);
			sub.setComplete(complete);
			sub.setActionParams((String[]) actionParamList.toArray(new String[actionParamList.size()]));
			subItemList.add(sub);

		} //end iterate through sub item node list.

		return subItemList;
	}

	/**
	 * Sets the title.
	 * @param title The title to set
	 */
	private void setTitle(String title) {
		this.title = title;
	}
}