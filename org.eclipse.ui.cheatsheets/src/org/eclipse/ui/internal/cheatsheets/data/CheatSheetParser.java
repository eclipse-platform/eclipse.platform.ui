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
import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.registry.CheatSheetRegistryReader;
import org.w3c.dom.*;
import org.xml.sax.*;

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
public class CheatSheetParser {

	
	class CheatSheetParserException extends Exception {
	}
	
	private static final String TRUE_STRING = "true"; //$NON-NLS-1$

	private DocumentBuilder documentBuilder;
	private ArrayList itemExtensionContainerList;


	/**
	 * Java constructor comment.
	 */
	public CheatSheetParser() {
		super();
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (Exception e) {
			//TODO do something better here
			e.printStackTrace();
		}
	}

	private Node findNode(Node startNode, String nodeName) {
		if(startNode == null) {
			return null;
		}

		if(startNode.getNodeName().equals(nodeName)) {
			return startNode;
		}

		NodeList nodes = startNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if(node.getNodeName().equals(nodeName)) {
				return node;
			}
		}
		
		return null;
	}

	/**
	 * Remove the new line, form feed and tab chars
	 * 
	 * @param string the string to be formatted
	 * @return the formatted string
	 */
	private String formatString(String string) {
		int hasNewLine = 0;
		int hasTab = 0;
		int hasRetLine = 0;

		hasNewLine = string.indexOf("\n"); //$NON-NLS-1$
		if (hasNewLine != -1)
			string = string.replace('\n', ' ');

		hasRetLine = string.indexOf("\r"); //$NON-NLS-1$
		if (hasRetLine != -1)
			string = string.replace('\r', ' ');

		hasTab = string.indexOf("\t"); //$NON-NLS-1$
		if (hasTab != -1)
			string = string.replace('\t', ' ');

		//TODO determine if the following is really needed, double spaces after a period is common.
		//now take out double/tripple/etc. spaces.
		char last = 'x';
		char current = 'x';
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < string.length(); i++) {
			current = string.charAt(i);

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

	private void handleAction(IActionItem item, Node actionNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(actionNode);
		Assert.isTrue(actionNode.getNodeName().equals(IParserTags.ACTION));

		Action action = new Action();
	
		String[] params = null;
	
		NamedNodeMap attributes = actionNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.PLUGINID)) {
					action.setPluginID(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.CLASS)) {
					action.setClass(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.CONFIRM)) {
					action.setConfirm(attribute.getNodeValue().equals(TRUE_STRING));
				} else if (attributeName.startsWith(IParserTags.PARAM)) {
					try {
						if(params == null) {
							params = new String[9];
						}
						String paramNum = attributeName.substring(IParserTags.PARAM.length());
						int num = Integer.parseInt(paramNum)-1;
						
						if(num>-1 && num<9){
							params[num] = attribute.getNodeValue();
						} else {
							// TODO: Error
							throw new NumberFormatException();
						}
					} catch(NumberFormatException e) {
						// TODO: Error
					}
					
					//TODO: handle action params
					//Set the action parameters if there are any.
					//l = getParamList(attributes);
					//System.out.println("Parsing parameters for actions");
					/*
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
					*/
				} else if (attributeName.equals(IParserTags.WHEN)) {
					action.setWhen(attribute.getNodeValue());
				} else {
					//TODO Warning
				}
			}
		}
		if(params != null) {
			action.setParams(params);
		}
		item.setAction(action);
	}

	private void handleCheatSheet(CheatSheet cheatSheet, Node rootnode) throws CheatSheetParserException {
		Assert.isNotNull(cheatSheet);
		Assert.isNotNull(rootnode);
		Assert.isTrue(rootnode.getNodeName().equals(IParserTags.CHEATSHEET));

		NamedNodeMap rootatts = rootnode.getAttributes();
		String title = rootatts.getNamedItem(IParserTags.TITLE).getNodeValue();
		
		cheatSheet.setTitle(title);
	}

	private void handleConditionalSubItem(Item item, Node conditionalSubItemNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(conditionalSubItemNode);
		Assert.isTrue(conditionalSubItemNode.getNodeName().equals(IParserTags.CONDITIONALSUBITEM));

		ConditionalSubItem conditionalSubItem = new ConditionalSubItem();

		// Handle attributes
		NamedNodeMap attributes = conditionalSubItemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.CONDITION)) {
					conditionalSubItem.setCondition(attribute.getNodeValue());
				} else {
					//TODO Warning
				}
			}
		}

		// Handle nodes
		NodeList nodes = conditionalSubItemNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.ACTION)) {
				handleSubItem(conditionalSubItem, node);
			}
		}

		item.addConditionalSubItem(conditionalSubItem);
	}

	private void handleDescription(Item item, Node startNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(startNode);

		Node descriptionNode = findNode(startNode, IParserTags.DESCRIPTION);
		
		if(descriptionNode != null) {
			NodeList nodes = descriptionNode.getChildNodes();
			
			StringBuffer text = new StringBuffer();
			boolean containsMarkup = false;

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if(node.getNodeType() == Node.TEXT_NODE) {
					text.append(node.getNodeValue());
				} else if(node.getNodeType() == Node.ELEMENT_NODE) {
					// handle <b></b> and <br/>
					if(node.getNodeName().equals(IParserTags.BOLD)) {
						containsMarkup = true;
						text.append(IParserTags.BOLD_START_TAG);
						text.append(node.getFirstChild().getNodeValue());
						text.append(IParserTags.BOLD_END_TAG);
					} else if(node.getNodeName().equals(IParserTags.BREAK)) {
						containsMarkup = true;
						text.append(IParserTags.BREAK_TAG);
					}
				}
			}

			if(containsMarkup) {
				text.insert(0, IParserTags.FORM_START_TAG);
				text.append(IParserTags.FORM_END_TAG);
			}

			// Remove the new line, form feed and tab chars
			item.setDescription(stripWhiteSpace(text.toString()));
		} else {
			item.setDescription(""); //$NON-NLS-1$
		}
	}
	
	private void handleIntro(CheatSheet cheatSheet, Document document) throws CheatSheetParserException {
		Assert.isNotNull(cheatSheet);
		Assert.isNotNull(document);

		//Get the Intro item.
		NodeList introList = document.getElementsByTagName(IParserTags.INTRO);
		Node introNode = introList.item(0);
		
		if(introNode == null) {
			//TODO: Error when there is no intro
		}
		if(introList.getLength() > 1) {
			//TODO: Error or warning when there are more than 1 intro
		}

		Item introItem = new Item();
		introItem.setTitle(CheatSheetPlugin.getResourceString(ICheatSheetResource.CHEAT_SHEET_INTRO_TITLE));

		handleIntroAttributes(introItem, introNode);
		handleDescription(introItem, introNode);

		cheatSheet.setIntroItem(introItem);
	}

	private void handleIntroAttributes(Item item, Node introNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(introNode);

		NamedNodeMap attributes = introNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.CONTEXTID)) {
					item.setContextId(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.HREF)) {
					item.setHref(attribute.getNodeValue());
				} else {
					//TODO: Warning about unknown attributes
				}
			}
		}
	}

	private Item handleItem(Node itemNode) throws CheatSheetParserException {
		Assert.isNotNull(itemNode);
		Assert.isTrue(itemNode.getNodeName().equals(IParserTags.ITEM));

		Item item = new Item();

		handleItemAttributes(item, itemNode);

		NodeList nodes = itemNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.ACTION)) {
				handleAction(item, node);
			} else if(node.getNodeName().equals(IParserTags.DESCRIPTION)) {
				handleDescription(item, node);
			} else if(node.getNodeName().equals(IParserTags.SUBITEM)) {
				handleSubItem(item, node);
			} else if(node.getNodeName().equals(IParserTags.CONDITIONALSUBITEM)) {
				handleConditionalSubItem(item, node);
			} else if(node.getNodeName().equals(IParserTags.REPEATEDSUBITM)) {
				handleRepeatedSubItem(item, node);
			} else if(node.getNodeName().equals(IParserTags.PERFORMWHEN)) {
				handlePerformWhen(item, node);
			}
		}

		

//		item.setIsDynamic(dynamic);

		return item;
	}

	private void handleItemAttributes(Item item, Node itemNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(itemNode);

		ArrayList itemExtensionElements = new ArrayList();
		
		NamedNodeMap attributes = itemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.TITLE)) {
					item.setTitle(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.CONTEXTID)) {
					item.setContextId(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.HREF)) {
					item.setHref(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.SKIP)) {
					item.setSkip(attribute.getNodeValue().equals(TRUE_STRING));
				} else {
					AbstractItemExtensionElement[] ie = handleUnknownItemAttribute(attribute);
					if (ie != null)
						itemExtensionElements.add(ie);
				}
			}
		}
		if (itemExtensionElements != null)
			item.setItemExtensions(itemExtensionElements);
	}
	
	private void handleItems(CheatSheet cheatSheet, Document document) throws CheatSheetParserException {
		//Get the items.
		NodeList itemList = document.getElementsByTagName(IParserTags.ITEM);
		//parse the items.  assemble CheatSheetItem objects.
		ArrayList items = handleItems(itemList);

		if (items == null) {
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_PARSING_ITEMS), null);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE_TITLE), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_PARSING_ITEMS), status);
			return;
		}
		
		cheatSheet.addItems(items);
	}

	private ArrayList handleItems(NodeList itemList) throws CheatSheetParserException {
		CheatSheetRegistryReader reader = CheatSheetRegistryReader.getInstance();
		itemExtensionContainerList = reader.readItemExtensions();

		ArrayList localList = new ArrayList();

		for (int i = 0; i < itemList.getLength(); i++) {
			//get the item node.  
			Node itemNode = itemList.item(i);
			Item item = handleItem(itemNode);
			
			localList.add(item);

//			NodeList itemchildren = itemnode.getChildNodes();
//
//			//			System.out.println("Checking for sub items.");
//			NodeList subItems = itemnode.getChildNodes();
//			ArrayList subItemArrayList = null;
//
//			//Gather sub items and add them to the sub item array list.  this will be parsed later.
//			for (int m = 0; m < subItems.getLength(); m++) {
//				Node n = subItems.item(m);
//				if (n.getNodeName().equals(IParserTags.SUBITEM)) {
//					if (subItemArrayList == null)
//						subItemArrayList = new ArrayList(10);
//					subItemArrayList.add(n);
//				}
//			}
//
//			//Create the cheatsheetitem and fill it with data.
//			if (subItemArrayList == null) {
//				Item itemtoadd = new Item();
//				itemtoadd.setActionPluginID(actionPid);
//				itemtoadd.setActionClass(actionClass);
//				itemtoadd.setHref(topicHref);
//				itemtoadd.setTitle(title);
//				itemtoadd.setActionParams((String[]) l.toArray(new String[l.size()]));
//				itemtoadd.setPerform(perform);
//				itemtoadd.setSkip(skip);
//				itemtoadd.setComplete(complete);
//				itemtoadd.setText(bodyString);
//				itemtoadd.setIsDynamic(dynamic);
//				itemtoadd.setID(itemID);
//				if (itemExtensionElements != null)
//					itemtoadd.setItemExtensions(itemExtensionElements);
//
//				//Add the item to the list of items to build the view from.
//				localList.add(itemtoadd);
//			} else {
//				ItemWithSubItems itemtoadd = new ItemWithSubItems();
//				itemtoadd.setHref(topicHref);
//				itemtoadd.setTitle(title);
//				itemtoadd.setText(bodyString);
//				itemtoadd.setIsDynamic(dynamic);
//				itemtoadd.setID(itemID);
//				//Parse the sub items for this item and add it to the sub item list.
//				ArrayList subs = parseSubItems(subItemArrayList);
//				if (subs == null || subs.size() <=1 )
//					return null;
//				itemtoadd.addSubItems((SubItem[]) subs.toArray(new SubItem[subs.size()]));
//
//				if (itemExtensionElements != null)
//					itemtoadd.setItemExtensions(itemExtensionElements);
//
//				//Add the item to the list of items to build the view from.
//				localList.add(itemtoadd);
//			}
//			idChecker.add(itemID);
		}
		return localList;
	}

	private void handlePerformWhen(IPerformWhenItem item, Node performWhenNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(performWhenNode);
		Assert.isTrue(performWhenNode.getNodeName().equals(IParserTags.PERFORMWHEN));

		PerformWhen performWhen = new PerformWhen();

		// Handle attributes
		NamedNodeMap attributes = performWhenNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.CONDITION)) {
					performWhen.setCondition(attribute.getNodeValue());
				} else {
					//TODO Warning
				}
			}
		}

		// Handle nodes
		NodeList nodes = performWhenNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.ACTION)) {
				handleAction(performWhen, node);
			}
		}

		item.setPerformWhen(performWhen);
	}

	private void handleRepeatedSubItem(Item item, Node repeatedSubItemNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(repeatedSubItemNode);
		Assert.isTrue(repeatedSubItemNode.getNodeName().equals(IParserTags.REPEATEDSUBITM));

		RepeatedSubItem repeatedSubItem = new RepeatedSubItem();

		// Handle attributes
		NamedNodeMap attributes = repeatedSubItemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.VALUES)) {
					repeatedSubItem.setValues(attribute.getNodeValue());
				} else {
					//TODO Warning
				}
			}
		}

		// Handle nodes
		NodeList nodes = repeatedSubItemNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.ACTION)) {
				handleSubItem(repeatedSubItem, node);
			}
		}

		item.addRepeatedSubItem(repeatedSubItem);
	}

	private void handleSubItem(ISubItemItem item, Node subItemNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(subItemNode);
		Assert.isTrue(subItemNode.getNodeName().equals(IParserTags.SUBITEM));

		SubItem subItem = new SubItem();

		handleSubItemAttributes(subItem, subItemNode);

		NodeList nodes = subItemNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.ACTION)) {
				handleAction(subItem, node);
			} else if(node.getNodeName().equals(IParserTags.PERFORMWHEN)) {
				if(item instanceof IPerformWhenItem) {
					handlePerformWhen((IPerformWhenItem)item, node);
				} else {
					//TODO: Error, only subitems can have a perform-when. Not conditional-subitem or repeated-subitem.
				}
			}
		}
//		item.setIsDynamic(dynamic);
		item.addSubItem(subItem);
	}

	private void handleSubItemAttributes(SubItem subItem, Node subItemNode) throws CheatSheetParserException {
		Assert.isNotNull(subItem);
		Assert.isNotNull(subItemNode);

		NamedNodeMap attributes = subItemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.LABEL)) {
					subItem.setLabel(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.SKIP)) {
					subItem.setSkip(attribute.getNodeValue().equals(TRUE_STRING));
				} else if (attributeName.equals(IParserTags.WHEN)) {
					subItem.setWhen(attribute.getNodeValue());
				} else {
					//TODO: Warning about unknown attributes
				}
			}
		}
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

	public CheatSheet parse(URL url) {
		InputStream is = null;
		InputSource inputSource = null;


		try {
			is = url.openStream();

			if (is != null) {
				inputSource = new InputSource(is);
			}
		} catch (Exception e) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), null, status);
			return null;
		}

		Document document;
		try {
			if(documentBuilder == null) {
				//TODO Do something better here!
				throw new Exception("Exception");
			}
			document = documentBuilder.parse(inputSource);
		} catch (IOException e) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE_IN_PARSER), e);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return null;
		} catch (SAXParseException spe) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_SAX_PARSING), spe);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return null;
		} catch (SAXException se) {
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_SAX_PARSING), se);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return null;
		} catch (Exception se) {
			//TODO: Needs to update the error message
			//Need to log exception here. 
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_SAX_PARSING), se);
			CheatSheetPlugin.getPlugin().getLog().log(status);
			return null;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		
		try {
			CheatSheet cheatSheet = parseCheatSheet(document);
			return cheatSheet;
		} catch(CheatSheetParserException e) {
			e.printStackTrace();
		}
		return null;
	}

	private CheatSheet parseCheatSheet(Document document) throws CheatSheetParserException {
		// If the document passed is null return a null tree and update the status
		if (document != null) {
			Node rootnode = document.getDocumentElement();
			
			// TODO: Is the root node really <cheatsheet>
			// IParserTags.CHEATSHEET
			rootnode.getNodeName();

			// Create the cheat sheet model object
			CheatSheet cheatSheet = new CheatSheet();

			handleCheatSheet(cheatSheet, rootnode);
	
			handleIntro(cheatSheet, document);

			handleItems(cheatSheet, document);
			
			return cheatSheet;
		} else {
			//System.err.println("Null document");
			IStatus status = new Status(IStatus.ERROR, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), null);
			CheatSheetPlugin.getPlugin().getLog().log(status);

			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE_TITLE), CheatSheetPlugin.getResourceString(ICheatSheetResource.ERROR_OPENING_FILE), status);
		}
		
		return null;
	}
	
	private String stripWhiteSpace(String string) {
		if( string.indexOf('\n') != -1 ||
			string.indexOf('\f') != -1 ||
			string.indexOf('\r') != -1 ||
			string.indexOf('\t') != -1 ||
			string.indexOf("  ") != -1) { //$NON-NLS-1$
			StringBuffer buffer = new StringBuffer(string);

			char last = 'x';
			for (int i = string.length()-1; i > -1; i--) {
				char c = string.charAt(i);
				if(c == '\n' || c == '\f' || c == '\r' || c == '\t') {
					buffer.deleteCharAt(i);
				}

				//TODO determine if the following is really needed, double spaces after a period is common.
				//now take out double/tripple/etc. spaces.
				if(c == ' ' && last == ' ') {
					buffer.deleteCharAt(i+1);
				}
				last = c;
			}
			return buffer.toString();
		}
		
		
		return string;
	}
}