/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Used to change the CFBundleURLTypes property of a Mac .plist file. Adds
 * handler entries for uri schemes in "CFBundleURLSchemes" elements. Can also
 * remove schemes.
 */
public class PlistFileWriter {

	private static final String XPATH_PLIST_DICT_CF_BUNDLE_URL_TYPES_KEY = "/plist/dict/key[text()=\"CFBundleURLTypes\"]"; //$NON-NLS-1$
	private static final String XPATH_PLIST_DICT_CF_BUNDLE_URL_TYPES_ARRAY = XPATH_PLIST_DICT_CF_BUNDLE_URL_TYPES_KEY
			+ "/following-sibling::array"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_KEY = "key"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_ARRAY = "array"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_STRING = "string"; //$NON-NLS-1$
	private static final String ELEMENT_NAME_DICT = "dict"; //$NON-NLS-1$
	private static final String KEY_VALUE_CF_BUNDLE_URL_TYPES = "CFBundleURLTypes"; //$NON-NLS-1$
	private static final String KEY_VALUE_CF_BUNDLE_URL_NAME = "CFBundleURLName"; //$NON-NLS-1$
	private static final String KEY_VALUE_CF_BUNDLE_URL_SCHEMES = "CFBundleURLSchemes"; //$NON-NLS-1$
	private Document document;
	private Element array;

	/**
	 * Creates an instance of the PlistFileWriter. Throws an
	 * {@link IllegalStateException} if the given {@link Reader} does not provide
	 * .plist file.
	 *
	 * @param reader The file reader of the .plist file
	 *
	 * @throws IllegalArgumentException if file cannot be understood as .plist file
	 */
	public PlistFileWriter(Reader reader) {
		this.document = getDom(reader);
		this.array = getOrCreateBundleUrlTypesAndArray();
	}

	/**
	 * Checks if the given scheme is registered in this .plist file
	 *
	 * @param scheme that should be checked for registration
	 * @return <code>true</code> if scheme registered; <code>false</code> otherwise
	 */
	public boolean isRegisteredScheme(String scheme) {
		Util.assertUriSchemeIsLegal(scheme);

		return getExistingElementFor(scheme) != null;
	}

	/**
	 * Adds an entry for the given scheme in the CFBundleURLSchemes element of the
	 * .plist file. Creates the CFBundleURLTypes element if not yet existing.
	 * Otherwise adds CFBundleURLSchemes element.
	 *
	 * @param scheme            The uri scheme which should be handled by the
	 *                          application of the .plist file
	 * @param schemeDescription The human readable description of the scheme
	 *
	 * @throws IllegalArgumentException if the given scheme contains illegal
	 *                                  characters
	 *
	 * @see #removeScheme(String)
	 *
	 * @see <a href= "https://tools.ietf.org/html/rfc3986#section-3.1">Uniform
	 *      Resource Identifier (URI): Generic Syntax</a>
	 *
	 */
	public void addScheme(String scheme, String schemeDescription) {
		// check precondition
		Util.assertUriSchemeIsLegal(scheme);

		if (getExistingElementFor(scheme) != null) {
			return;
		}

		// add dict element
		addIndent(array, 3);
		Element dictInArray = addChildNode(array, ELEMENT_NAME_DICT, null);

		// add key CFBundleURLName
		addIndent(dictInArray, 4);
		addChildNode(dictInArray, ELEMENT_NAME_KEY, KEY_VALUE_CF_BUNDLE_URL_NAME);

		// add string
		addIndent(dictInArray, 5);
		addChildNode(dictInArray, ELEMENT_NAME_STRING, schemeDescription);

		// add key CFBundleURLSchemes
		addIndent(dictInArray, 4);
		addChildNode(dictInArray, ELEMENT_NAME_KEY, KEY_VALUE_CF_BUNDLE_URL_SCHEMES);

		// add array
		addIndent(dictInArray, 5);
		Element schemeArray = addChildNode(dictInArray, ELEMENT_NAME_ARRAY, null);

		// add string
		addIndent(schemeArray, 6);
		addChildNode(schemeArray, ELEMENT_NAME_STRING, scheme);

		// indent closing tags
		addIndent(schemeArray, 5);
		addIndent(dictInArray, 3);
	}

	/**
	 * Removes the corresponding CFBundleURLSchemes element for the given scheme
	 * from the CFBundleURLTypes element of the .plist file. Removes the
	 * CFBundleURLTypes element completely if it is empty (no handled schemes) after
	 * removal.
	 *
	 * @param scheme The uri scheme which should not be handled anymore by the
	 *               application of the .plist file.
	 *
	 * @throws IllegalArgumentException if the given scheme contains illegal
	 *                                  characters
	 *
	 * @see #addScheme(String, String)
	 *
	 * @see <a href=
	 *      "https://tools.ietf.org/html/rfc3986#section-3.1">https://tools.ietf.org/html/rfc3986#section-3.1</a>
	 *
	 */
	public void removeScheme(String scheme) {
		Util.assertUriSchemeIsLegal(scheme);

		Element dict = getExistingElementFor(scheme);
		if (dict == null) {
			// not found, no need to remove
			return;
		}

		Node arrayNode = dict.getParentNode();
		removeTextNode(arrayNode, dict.getPreviousSibling()); // remove tab and line break before dict
		arrayNode.removeChild(dict);
	}

	/**
	 * Writes the content (xml) of the .plist file to the given {@link Writer}
	 *
	 * @param writer The Writer to which the xml should be written to, e.g.
	 *               {@link BufferedWriter}
	 *
	 */
	public void writeTo(Writer writer) {
		boolean hasDict = false;
		for (int i = 0; i < array.getChildNodes().getLength(); i++) {
			Node child = array.getChildNodes().item(i);
			if ("dict".equals(child.getNodeName())) { //$NON-NLS-1$
				hasDict = true;
				break;
			}
		}
		// check if no schemes in cfbundlurltypes
		if (!hasDict) {
			Node keyNode = evaluateXpathOnElement(document, XPATH_PLIST_DICT_CF_BUNDLE_URL_TYPES_KEY);
			if (keyNode != null) {
				keyNode.getParentNode().removeChild(keyNode);
				array.getParentNode().removeChild(array);
			}
		} else {
			// indent last closing tag
			addIndent(array, 2);
		}

		transformDocument(writer);
	}

	private void transformDocument(Writer writer) {
		try {
			DOMSource source = new DOMSource(this.document);
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(writer));
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		} finally {
			close(writer);
		}
	}

	private Document getDom(Reader reader) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(reader));
		} catch (ParserConfigurationException | IOException | SAXException e) {
			throw new IllegalArgumentException(e);
		} finally {
			close(reader);
		}
	}

	private void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private Element getOrCreateBundleUrlTypesAndArray() {
		Element arrayNode = evaluateXpathOnElement(this.document, XPATH_PLIST_DICT_CF_BUNDLE_URL_TYPES_ARRAY);
		if (arrayNode != null) {
			// adapt whitespace of array
			if (removeTextNode(arrayNode, arrayNode.getLastChild())) {
				addLineBreak(arrayNode);
			}
		} else {
			// create CFBundleURLTypes and it's array
			Element plistElement = document.getDocumentElement();
			NodeList dictElements = plistElement.getElementsByTagName(ELEMENT_NAME_DICT);
			if (dictElements.getLength() == 0) {
				throw new IllegalStateException("Top level 'DICT' element could not be found"); //$NON-NLS-1$
			}
			Node dictElement = dictElements.item(0);
			addIndent(dictElement, 1);
			addChildNode(dictElement, ELEMENT_NAME_KEY, KEY_VALUE_CF_BUNDLE_URL_TYPES);

			addIndent(dictElement, 2);
			arrayNode = addChildNode(dictElement, ELEMENT_NAME_ARRAY, null);
		}
		return arrayNode;
	}

	private boolean removeTextNode(Node parent, Node textNode) {
		if (textNode instanceof Text) {
			parent.removeChild(textNode);
			return true;
		}
		return false;
	}

	private Element addChildNode(Node parent, String name, String value) {
		Element newElement = document.createElement(name);
		if (value != null) {
			newElement.appendChild(document.createTextNode(value));
		} else {
			addLineBreak(newElement);
		}
		parent.appendChild(newElement);
		addLineBreak(parent);
		return newElement;
	}

	private void addLineBreak(Node node) {
		node.appendChild(document.createTextNode("\n")); //$NON-NLS-1$
	}

	private void addIndent(Node node, int indent) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			builder.append("	"); //$NON-NLS-1$
		}
		node.appendChild(document.createTextNode(builder.toString()));
	}

	private Element getExistingElementFor(String scheme) {
		String xpathToSchemeDictElement = XPATH_PLIST_DICT_CF_BUNDLE_URL_TYPES_ARRAY
				+ "/dict/key[text()=\"CFBundleURLSchemes\"]/following-sibling::array/string[text()=\"" + scheme //$NON-NLS-1$
				+ "\"]/../.."; //$NON-NLS-1$

		return evaluateXpathOnElement(this.document, xpathToSchemeDictElement);
	}

	private Element evaluateXpathOnElement(Node node, String xpath) {
		try {
			XPathExpression xpathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
			NodeList nodeList = (NodeList) xpathExpression.evaluate(node, XPathConstants.NODESET);
			return nodeList.getLength() == 0 ? null : (Element) nodeList.item(0);
		} catch (XPathExpressionException e) {
			throw new IllegalStateException(e); // cannot happen
		}
	}
}