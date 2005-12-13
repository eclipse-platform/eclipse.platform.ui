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
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.cheatsheets.AbstractItemExtensionElement;
import org.eclipse.ui.internal.cheatsheets.*;
import org.eclipse.ui.internal.cheatsheets.registry.*;
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
		private static final long serialVersionUID = 6009335074727417445L;
		public CheatSheetParserException(String message) {
			super(message);
		}
	}
	
	private static final String TRUE_STRING = "true"; //$NON-NLS-1$

	private DocumentBuilder documentBuilder;
	private ArrayList itemExtensionContainerList;


	/**
	 * Java constructor comment.
	 */
	public CheatSheetParser() {
		super();
		documentBuilder = CheatSheetPlugin.getPlugin().getDocumentBuilder();
	}

	/**
	 * Converts any characters required to escaped by an XML parser to 
	 * their escaped counterpart.
	 * 
	 * Characters			XML escaped counterpart
	 * <			->		&lt;
	 * >			->		&gt;
	 * &			->		&amp;
	 * '			->		&apos;
	 * "			->		&quot;
	 *
	 * Tags that will be ignored <b>, </b> and <br/>.
	 *
	 * @param text the string buffer to have its characters escaped
	 * @return string buffer with any of the characters requiring XML escaping escaped
	 */
	private StringBuffer escapeXMLCharacters(StringBuffer text) {
		// Set the maximum length of the tags to ignore
		final int MAXIMUM_TAG_LENGTH = 5;
		
		// Keep a local variable for the orignal string's length
		int length = text.length();
		
		// Create the buffer to store the resulting string
		StringBuffer result = new StringBuffer(length);
		
		// Loop for the characters of the original string
		for(int i=0; i<length; i++) {
			// Grab the next character and determine how to handle it
			char c = text.charAt(i);
			switch (c) {
				case '<': {
					// We have a less than, grab the maximum tag length of characters
					// or the remaining characters which follow and determine if it
					// is the start of a tag to ignore.
					String tmp = ICheatSheetResource.EMPTY_STRING;
					if(i+MAXIMUM_TAG_LENGTH < length)
						tmp = text.substring(i, i+MAXIMUM_TAG_LENGTH).toLowerCase();
					else {
						tmp = text.substring(i, length).toLowerCase();
					}
					if(tmp.startsWith(IParserTags.BOLD_START_TAG) || tmp.startsWith(IParserTags.BOLD_END_TAG) || tmp.startsWith(IParserTags.BREAK_TAG)) {
						// We have a tag to ignore so just emit the character
						result.append(c);
					} else {
						// We have detemined that it is just a less than
						// so emit the XML escaped counterpart
						result.append(IParserTags.LESS_THAN);
					}
					break; }
				case '>': {
					// We have a greater than, grab the maximum tag length of characters
					// or the starting characters which come before and determine if it
					// is the end of a tag to ignore.
					String tmp = ICheatSheetResource.EMPTY_STRING;
					if(i>=MAXIMUM_TAG_LENGTH) {
						tmp = text.substring(i-MAXIMUM_TAG_LENGTH, i+1).toLowerCase();
					} else {
						tmp = text.substring(0, i+1).toLowerCase();
					}
					if(tmp.endsWith(IParserTags.BOLD_START_TAG) || tmp.endsWith(IParserTags.BOLD_END_TAG) || tmp.endsWith(IParserTags.BREAK_TAG)) {
						// We have a tag to ignore so just emit the character
						result.append(c);
					} else {
						// We have detemined that it is just a greater than
						// so emit the XML escaped counterpart
						result.append(IParserTags.GREATER_THAN);
					}
					break; }
				case '&':
					// We have an ampersand so emit the XML escaped counterpart
					result.append(IParserTags.AMPERSAND);
					break;
				case '\'':
					// We have an apostrophe so emit the XML escaped counterpart
					result.append(IParserTags.APOSTROPHE);
					break;
				case '"':
					// We have a quote so emit the XML escaped counterpart
					result.append(IParserTags.QUOTE);
					break;
				default:
					// We have a character that does not require escaping
					result.append(c);
					break;
			}
		}
		return result;
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

	private void handleAction(IActionItem item, Node actionNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(actionNode);
		Assert.isTrue(actionNode.getNodeName().equals(IParserTags.ACTION));

		Action action = new Action();
	
		String[] params = null;

		boolean classAttr = false;
		boolean pluginId = false;

		NamedNodeMap attributes = actionNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.PLUGINID)) {
					pluginId = true;
					action.setPluginID(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.CLASS)) {
					classAttr = true;
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
							String message = NLS.bind(Messages.ERROR_PARSING_PARAM_INVALIDRANGE, (new Object[] {attributeName, paramNum}));
							throw new NumberFormatException(message);
						}
					} catch(NumberFormatException e) {
						String message = Messages.ERROR_PARSING_PARAM_INVALIDNUMBER;
						logMessage(IStatus.ERROR, false, message, null, e);
						throw new CheatSheetParserException(message);
					}
				} else if (attributeName.equals(IParserTags.WHEN)) {
					action.setWhen(attribute.getNodeValue());
				} else {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {attributeName, actionNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!classAttr) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_CLASS, (new Object[] {actionNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}
		if(!pluginId) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_PLUGINID, (new Object[] {actionNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		if(params != null) {
			action.setParams(params);
		}
		item.setAction(action);
	}

	private void handleCheatSheet(CheatSheet cheatSheet, Node cheatSheetNode) throws CheatSheetParserException {
		Assert.isNotNull(cheatSheet);
		Assert.isNotNull(cheatSheetNode);
		Assert.isTrue(cheatSheetNode.getNodeName().equals(IParserTags.CHEATSHEET));

		boolean title = false;

		NamedNodeMap attributes = cheatSheetNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.TITLE)) {
					title = true;
					cheatSheet.setTitle(attribute.getNodeValue());
				} else {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {attributeName, cheatSheetNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!title) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_TITLE, (new Object[] {cheatSheetNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}
	}

	private void handleConditionalSubItem(Item item, Node conditionalSubItemNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(conditionalSubItemNode);
		Assert.isTrue(conditionalSubItemNode.getNodeName().equals(IParserTags.CONDITIONALSUBITEM));

		ConditionalSubItem conditionalSubItem = new ConditionalSubItem();

		boolean condition = false;

		// Handle attributes
		NamedNodeMap attributes = conditionalSubItemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.CONDITION)) {
					condition = true;
					conditionalSubItem.setCondition(attribute.getNodeValue());
				} else {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {attributeName, conditionalSubItemNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!condition) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_CONDITION, (new Object[] {conditionalSubItemNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		boolean subitem = false;

		// Handle nodes
		NodeList nodes = conditionalSubItemNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.SUBITEM)) {
				subitem = true;
				handleSubItem(conditionalSubItem, node);
			} else {
				if(node.getNodeType() != Node.TEXT_NODE && node.getNodeType() != Node.COMMENT_NODE ) {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ELEMENT, (new Object[] {node.getNodeName(), conditionalSubItemNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!subitem) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_SUBITEM, (new Object[] {conditionalSubItemNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		item.addSubItem(conditionalSubItem);
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
					} else {
						Node parentNode = startNode;
						if( startNode.getNodeName().equals(IParserTags.DESCRIPTION) ) {
							parentNode = startNode.getParentNode();
						}
						String message = NLS.bind(Messages.WARNING_PARSING_DESCRIPTION_UNKNOWN_ELEMENT, (new Object[] {parentNode.getNodeName(), node.getNodeName()}));
						logMessage(IStatus.WARNING, false, message, null, null);
					}
				}
			}

			if(containsMarkup) {
				text = escapeXMLCharacters(text);
				text.insert(0, IParserTags.FORM_START_TAG);
				text.append(IParserTags.FORM_END_TAG);
			}

			// Remove the new line, form feed and tab chars
			item.setDescription(text.toString().trim());
		} else {
			Node parentNode = startNode;
			if( startNode.getNodeName().equals(IParserTags.DESCRIPTION) ) {
				parentNode = startNode.getParentNode();
			}
			String message = NLS.bind(Messages.ERROR_PARSING_NO_DESCRIPTION, (new Object[] {parentNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}
	}
	
	private void handleIntro(CheatSheet cheatSheet, Document document) throws CheatSheetParserException {
		Assert.isNotNull(cheatSheet);
		Assert.isNotNull(document);

		//Get the Intro item.
		NodeList introList = document.getElementsByTagName(IParserTags.INTRO);
		Node introNode = introList.item(0);
		
		if(introNode == null) {
			// Error: there is no intro
			throw new CheatSheetParserException(Messages.ERROR_PARSING_NO_INTRO);
		}
		if(introList.getLength() > 1) {
			// Error: there are more than 1 intro
			throw new CheatSheetParserException(Messages.ERROR_PARSING_MORE_THAN_ONE_INTRO);
		}

		Item introItem = new Item();
		introItem.setTitle(Messages.CHEAT_SHEET_INTRO_TITLE);

		handleIntroAttributes(introItem, introNode);
		handleDescription(introItem, introNode);

		cheatSheet.setIntroItem(introItem);
	}

	private void handleIntroAttributes(Item item, Node introNode) {
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
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {attributeName, introNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}
	}

	private Item handleItem(Node itemNode) throws CheatSheetParserException {
		Assert.isNotNull(itemNode);
		Assert.isTrue(itemNode.getNodeName().equals(IParserTags.ITEM));

		Item item = new Item();

		handleItemAttributes(item, itemNode);

		boolean description = false;
		
		NodeList nodes = itemNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.ACTION)) {
				handleAction(item, node);
			} else if(node.getNodeName().equals(IParserTags.DESCRIPTION)) {
				description = true;
				handleDescription(item, node);
			} else if(node.getNodeName().equals(IParserTags.SUBITEM)) {
				handleSubItem(item, node);
			} else if(node.getNodeName().equals(IParserTags.CONDITIONALSUBITEM)) {
				handleConditionalSubItem(item, node);
			} else if(node.getNodeName().equals(IParserTags.REPEATEDSUBITM)) {
				handleRepeatedSubItem(item, node);
			} else if(node.getNodeName().equals(IParserTags.PERFORMWHEN)) {
				handlePerformWhen(item, node);
			} else {
				if(node.getNodeType() != Node.TEXT_NODE && node.getNodeType() != Node.COMMENT_NODE ) {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ELEMENT, (new Object[] {node.getNodeName(), itemNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!description) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_DESCRIPTION, (new Object[] {itemNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		return item;
	}

	private void handleItemAttributes(Item item, Node itemNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(itemNode);

		ArrayList itemExtensionElements = new ArrayList();

		boolean title = false;

		NamedNodeMap attributes = itemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.TITLE)) {
					title = true;
					item.setTitle(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.CONTEXTID)) {
					item.setContextId(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.HREF)) {
					item.setHref(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.SKIP)) {
					item.setSkip(attribute.getNodeValue().equals(TRUE_STRING));
				} else {
					AbstractItemExtensionElement[] ie = handleUnknownItemAttribute(attribute, itemNode);
					if (ie != null)
						itemExtensionElements.add(ie);
				}
			}
		}

		if(!title) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_TITLE, (new Object[] {itemNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		if (itemExtensionElements != null)
			item.setItemExtensions(itemExtensionElements);
	}
	
	private void handleItems(CheatSheet cheatSheet, Document document) throws CheatSheetParserException {
		//Get the items.
		NodeList itemList = document.getElementsByTagName(IParserTags.ITEM);

		if (itemList == null || itemList.getLength() == 0) {
			throw new CheatSheetParserException(Messages.ERROR_PARSING_NO_ITEM);
		}
		
		//parse the items.  assemble CheatSheetItem objects.
		ArrayList items = handleItems(itemList);

		if (items == null || items.size() == 0) {
			// This should never occur but just to be safe let's check.
			throw new CheatSheetParserException(Messages.ERROR_PARSING_NO_ITEM);
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
		}
		return localList;
	}

	private void handlePerformWhen(IPerformWhenItem item, Node performWhenNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(performWhenNode);
		Assert.isTrue(performWhenNode.getNodeName().equals(IParserTags.PERFORMWHEN));

		PerformWhen performWhen = new PerformWhen();

		 boolean condition = false;

		// Handle attributes
		NamedNodeMap attributes = performWhenNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.CONDITION)) {
					condition = true;
					performWhen.setCondition(attribute.getNodeValue());
				} else {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {attributeName, performWhenNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!condition) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_CONDITION, (new Object[] {performWhenNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		boolean action = false;

		// Handle nodes
		NodeList nodes = performWhenNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.ACTION)) {
				action = true;
				handleAction(performWhen, node);
			} else {
				if(node.getNodeType() != Node.TEXT_NODE && node.getNodeType() != Node.COMMENT_NODE ) {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ELEMENT, (new Object[] {node.getNodeName(), performWhenNode .getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!action) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_ACTION, (new Object[] {performWhenNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		item.setPerformWhen(performWhen);
	}

	private void handleRepeatedSubItem(Item item, Node repeatedSubItemNode) throws CheatSheetParserException {
		Assert.isNotNull(item);
		Assert.isNotNull(repeatedSubItemNode);
		Assert.isTrue(repeatedSubItemNode.getNodeName().equals(IParserTags.REPEATEDSUBITM));

		RepeatedSubItem repeatedSubItem = new RepeatedSubItem();

		boolean values = false;

		// Handle attributes
		NamedNodeMap attributes = repeatedSubItemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.VALUES)) {
					values = true;
					repeatedSubItem.setValues(attribute.getNodeValue());
				} else {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {attributeName, repeatedSubItemNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!values) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_VALUES, (new Object[] {repeatedSubItemNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		boolean subitem = false;

		// Handle nodes
		NodeList nodes = repeatedSubItemNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);

			if(node.getNodeName().equals(IParserTags.SUBITEM)) {
				subitem = true;
				handleSubItem(repeatedSubItem, node);
			} else {
				if(node.getNodeType() != Node.TEXT_NODE && node.getNodeType() != Node.COMMENT_NODE ) {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ELEMENT, (new Object[] {node.getNodeName(), repeatedSubItemNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!subitem) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_SUBITEM, (new Object[] {repeatedSubItemNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}

		item.addSubItem(repeatedSubItem);
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
				handlePerformWhen(subItem, node);
			} else {
				if(node.getNodeType() != Node.TEXT_NODE && node.getNodeType() != Node.COMMENT_NODE ) {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ELEMENT, (new Object[] {node.getNodeName(), subItemNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}
		item.addSubItem(subItem);
	}

	private void handleSubItemAttributes(SubItem subItem, Node subItemNode) throws CheatSheetParserException {
		Assert.isNotNull(subItem);
		Assert.isNotNull(subItemNode);

		boolean label = false;

		NamedNodeMap attributes = subItemNode.getAttributes();
		if (attributes != null) {
			for (int x = 0; x < attributes.getLength(); x++) {
				Node attribute = attributes.item(x);
				String attributeName = attribute.getNodeName();
				if (attribute == null || attributeName == null)
					continue;

				if (attributeName.equals(IParserTags.LABEL)) {
					label = true;
					subItem.setLabel(attribute.getNodeValue());
				} else if (attributeName.equals(IParserTags.SKIP)) {
					subItem.setSkip(attribute.getNodeValue().equals(TRUE_STRING));
				} else if (attributeName.equals(IParserTags.WHEN)) {
					subItem.setWhen(attribute.getNodeValue());
				} else {
					String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {attributeName, subItemNode.getNodeName()}));
					logMessage(IStatus.WARNING, false, message, null, null);
				}
			}
		}

		if(!label) {
			String message = NLS.bind(Messages.ERROR_PARSING_NO_LABEL, (new Object[] {subItemNode.getNodeName()}));
			throw new CheatSheetParserException(message);
		}
	}

	private AbstractItemExtensionElement[] handleUnknownItemAttribute(Node item, Node node) {
		ArrayList al = new ArrayList();
		if (itemExtensionContainerList == null)
			return null;

		for (int i = 0; i < itemExtensionContainerList.size(); i++) {
			CheatSheetItemExtensionElement itemExtensionElement = (CheatSheetItemExtensionElement) itemExtensionContainerList.get(i);

			if (itemExtensionElement.getItemAttribute().equals(item.getNodeName())) {
				AbstractItemExtensionElement itemElement = itemExtensionElement.createInstance();
				if(itemElement != null) {
					itemElement.handleAttribute(item.getNodeValue());
					al.add(itemElement);
				}
			}
		}

		if(al.size() == 0) {
			String message = NLS.bind(Messages.WARNING_PARSING_UNKNOWN_ATTRIBUTE, (new Object[] {item.getNodeName(), node.getNodeName()}));
			logMessage(IStatus.WARNING, false, message, null, null);
		}
		return (AbstractItemExtensionElement[])al.toArray(new AbstractItemExtensionElement[al.size()]);
	}

	/**
	 * @param severity
	 * @param informUser
	 * @param message
	 * @param title
	 * @param exception
	 */
	private void logMessage(int severity, boolean informUser, String message, String title, Throwable exception) {
		IStatus status = new Status(severity, ICheatSheetResource.CHEAT_SHEET_PLUGIN_ID, IStatus.OK, message, exception);
		CheatSheetPlugin.getPlugin().getLog().log(status);
		
		if(informUser) {
			org.eclipse.jface.dialogs.ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), title, null, status);
		}
	}

	public CheatSheet parse(URL url) {
		if(url == null) {
			return null;
		}

		InputStream is = null;
		InputSource inputSource = null;


		try {
			is = url.openStream();

			if (is != null) {
				inputSource = new InputSource(is);
			}
		} catch (Exception e) {
			String message = NLS.bind(Messages.ERROR_OPENING_FILE, (new Object[] {url.getFile()}));
			logMessage(IStatus.ERROR, true, message, Messages.ERROR_TITLE, e);
			return null;
		}

		Document document;
		try {
			if(documentBuilder == null) {
				logMessage(IStatus.ERROR, false, Messages.ERROR_DOCUMENT_BUILDER_NOT_INIT, null, null);
				return null;
			}
			document = documentBuilder.parse(inputSource);
		} catch (IOException e) {
			String message = NLS.bind(Messages.ERROR_OPENING_FILE_IN_PARSER, (new Object[] {url.getFile()}));
			logMessage(IStatus.ERROR, false, message, null, e);
			return null;
		} catch (SAXParseException spe) {
			String message = NLS.bind(Messages.ERROR_SAX_PARSING_WITH_LOCATION, (new Object[] {url.getFile(), new Integer(spe.getLineNumber()), new Integer(spe.getColumnNumber())}));
			logMessage(IStatus.ERROR, false, message, null, spe);
			return null;
		} catch (SAXException se) {
			String message = NLS.bind(Messages.ERROR_SAX_PARSING, (new Object[] {url.getFile()}));
			logMessage(IStatus.ERROR, false, message, null, se);
			return null;
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		
		try {
			return parseCheatSheet(document);
		} catch(CheatSheetParserException e) {
			logMessage(IStatus.ERROR, true, e.getMessage(), Messages.ERROR_TITLE, e);
		}
		return null;
	}

	private CheatSheet parseCheatSheet(Document document) throws CheatSheetParserException {
		// If the document passed is null return a null tree and update the status
		if (document != null) {
			Node rootnode = document.getDocumentElement();
			
			// Is the root node really <cheatsheet>?
			if( !rootnode.getNodeName().equals(IParserTags.CHEATSHEET) ) {
				throw new CheatSheetParserException(Messages.ERROR_PARSING_CHEATSHEET_ELEMENT);
			}

			// Create the cheat sheet model object
			CheatSheet cheatSheet = new CheatSheet();

			handleCheatSheet(cheatSheet, rootnode);
	
			handleIntro(cheatSheet, document);

			handleItems(cheatSheet, document);
			
			return cheatSheet;
		} 
		throw new CheatSheetParserException(Messages.ERROR_PARSING_CHEATSHEET_CONTENTS);
	}
/*
	private String getNormalizedText(String text) {
		int [] spaceCounter = new int[1];
		StringBuffer buf = new StringBuffer();
		
		if (text==null) return null;


		for (int j=0; j<text.length(); j++) {
			char c = text.charAt(j);
			if (c==' ' || c=='\t') {
				// space
				if (++spaceCounter[0] == 1) {
					buf.append(c);
				}
			}
			else if (c=='\n' || c=='\r' || c=='\f') {
				// new line
				if (++spaceCounter[0]==1) {
					buf.append(' ');
				}
			}
			else {
				// other characters
				spaceCounter[0]=0;
				buf.append(c);
			}
		}

		return buf.toString();
	}
*/
}
