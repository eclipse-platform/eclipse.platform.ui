/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class represents the default implementation of the
 * <code>IMemento</code> interface.
 * <p>
 * This class is not intended to be extended by clients.
 * </p>
 *
 * @see IMemento
 */
public final class XMLMemento implements IMemento {
	private Document factory;
	private Element element;

	/**
	 * Creates a <code>Document</code> from the <code>Reader</code>
	 * and returns a memento on the first <code>Element</code> for reading
	 * the document.
	 * <p>
	 * Same as calling createReadRoot(reader, null)
	 * </p>
	 * 
	 * @param reader the <code>Reader</code> used to create the memento's document
	 * @return a memento on the first <code>Element</code> for reading the document
	 * @throws <code>WorkbenchException</code> if IO problems, invalid format, or no element.
	 */
	public static XMLMemento createReadRoot(Reader reader) throws WorkbenchException {
		return createReadRoot(reader, null);
	}

	/**
	 * Creates a <code>Document</code> from the <code>Reader</code>
	 * and returns a memento on the first <code>Element</code> for reading
	 * the document.
	 * 
	 * @param reader the <code>Reader</code> used to create the memento's document
	 * @param baseDir the directory used to resolve relative file names
	 * 		in the XML document. This directory must exist and include the
	 * 		trailing separator. The directory format, including the separators,
	 * 		must be valid for the platform. Can be <code>null</code> if not
	 * 		needed.
	 * @return a memento on the first <code>Element</code> for reading the document
	 * @throws <code>WorkbenchException</code> if IO problems, invalid format, or no element.
	 */
	public static XMLMemento createReadRoot(Reader reader, String baseDir) throws WorkbenchException {
		String messageKey = "XMLMemento.noElement"; //$NON-NLS-1$
		Exception exception = null;
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			InputSource source = new InputSource(reader);
			if (baseDir != null)
				source.setSystemId(baseDir);
			Document document = parser.parse(source);
			NodeList list = document.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if (node instanceof Element)
					return new XMLMemento(document, (Element) node);
			}
		} catch (ParserConfigurationException e) {
			exception = e;
			messageKey = "XMLMemento.parserConfigError"; //$NON-NLS-1$
		} catch (IOException e) {
			exception = e;
			messageKey = "XMLMemento.ioError"; //$NON-NLS-1$
		} catch (SAXException e) {
			exception = e;
			messageKey = "XMLMemento.formatError"; //$NON-NLS-1$
		}
		
		String problemText = null;
		if (exception != null)
			problemText = exception.getMessage();
		if (problemText == null || problemText.length() == 0)
			problemText = WorkbenchMessages.getString(messageKey);
		throw new WorkbenchException(problemText, exception);
	}
	
	/**
	 * Returns a root memento for writing a document.
	 * 
	 * @param type the element node type to create on the document
	 * @return the root memento for writing a document
	 */
	public static XMLMemento createWriteRoot(String type) {
		Document document = new DocumentImpl();
		Element element = document.createElement(type);
		document.appendChild(element);
		return new XMLMemento(document, element);
	}
	
	/**
	 * Creates a memento for the specified document and element.
	 * <p>
	 * Clients should use <code>createReadRoot</code> and
	 * <code>createWriteRoot</code> to create the initial
	 * memento on a document.
	 * </p>
	 * 
	 * @param document the document for the memento
	 * @param element the element node for the memento
	 * @return the memento on the specified document and element
	 */
	public XMLMemento(Document document, Element element) {
		super();
		this.factory = document;
		this.element = element;
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public IMemento createChild(String type) {
		Element child = factory.createElement(type);
		element.appendChild(child);
		return new XMLMemento(factory, child);
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public IMemento createChild(String type, String id) {
		Element child = factory.createElement(type);
		child.setAttribute(TAG_ID, id);
		element.appendChild(child);
		return new XMLMemento(factory, child);
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public IMemento copyChild(IMemento child) {
		Element childElement = ((XMLMemento) child).element;
		Element newElement = (Element) factory.importNode(childElement, true);
		element.appendChild(newElement);
		return new XMLMemento(factory, newElement);
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public IMemento getChild(String type) {

		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		if (size == 0)
			return null;

		// Find the first node which is a child of this node.
		for (int nX = 0; nX < size; nX++) {
			Node node = nodes.item(nX);
			if (node instanceof Element) {
				Element element = (Element) node;
				if (element.getNodeName().equals(type))
					return new XMLMemento(factory, element);
			}
		}

		// A child was not found.
		return null;
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public IMemento[] getChildren(String type) {

		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		if (size == 0)
			return new IMemento[0];

		// Extract each node with given type.
		ArrayList list = new ArrayList(size);
		for (int nX = 0; nX < size; nX++) {
			Node node = nodes.item(nX);
			if (node instanceof Element) {
				Element element = (Element) node;
				if (element.getNodeName().equals(type))
					list.add(element);
			}
		}

		// Create a memento for each node.
		size = list.size();
		IMemento[] results = new IMemento[size];
		for (int x = 0; x < size; x++) {
			results[x] = new XMLMemento(factory, (Element) list.get(x));
		}
		return results;
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public Float getFloat(String key) {
		Attr attr = element.getAttributeNode(key);
		if (attr == null)
			return null;
		String strValue = attr.getValue();
		try {
			return new Float(strValue);
		} catch (NumberFormatException e) {
			WorkbenchPlugin.log("Memento problem - Invalid float for key: " //$NON-NLS-1$
			+ key + " value: " + strValue); //$NON-NLS-1$
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public String getID() {
		return element.getAttribute(TAG_ID);
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public Integer getInteger(String key) {
		Attr attr = element.getAttributeNode(key);
		if (attr == null)
			return null;
		String strValue = attr.getValue();
		try {
			return new Integer(strValue);
		} catch (NumberFormatException e) {
			WorkbenchPlugin.log("Memento problem - invalid integer for key: " + key //$NON-NLS-1$
			+ " value: " + strValue); //$NON-NLS-1$
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public String getString(String key) {
		Attr attr = element.getAttributeNode(key);
		if (attr == null)
			return null;
		return attr.getValue();
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public String getTextData() {
		Text textNode = getTextNode();
		if (textNode != null) {
			return textNode.getData();
		} else {
			return null;
		}
	}

	/**
	 * Returns the Text node of the memento. Each memento is allowed only 
	 * one Text node.
	 * 
	 * @return the Text node of the memento, or <code>null</code> if
	 * the memento has no Text node.
	 */
	private Text getTextNode() {
		// Get the nodes.
		NodeList nodes = element.getChildNodes();
		int size = nodes.getLength();
		if (size == 0)
			return null;
		for (int nX = 0; nX < size; nX++) {
			Node node = nodes.item(nX);
			if (node instanceof Text) {
				return (Text) node;
			}
		}
		// a Text node was not found
		return null;
	}

	/**
	 * Places the element's attributes into the document.
	 */
	private void putElement(Element element) {
		NamedNodeMap nodeMap = element.getAttributes();
		int size = nodeMap.getLength();
		for (int i = 0; i < size; i++) {
			Attr attr = (Attr) nodeMap.item(i);
			putString(attr.getName(), attr.getValue());
		}

		NodeList nodes = element.getChildNodes();
		size = nodes.getLength();
		for (int i = 0; i < size; i++) {
			Node node = nodes.item(i);
			if (node instanceof Element) {
				XMLMemento child = (XMLMemento) createChild(node.getNodeName());
				child.putElement((Element) node);
			}
		}
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public void putFloat(String key, float f) {
		element.setAttribute(key, String.valueOf(f));
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public void putInteger(String key, int n) {
		element.setAttribute(key, String.valueOf(n));
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public void putMemento(IMemento memento) {
		putElement(((XMLMemento) memento).element);
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public void putString(String key, String value) {
		if (value == null)
			return;
		element.setAttribute(key, value);
	}
	
	/* (non-Javadoc)
	 * Method declared in IMemento.
	 */
	public void putTextData(String data) {
		Text textNode = getTextNode();
		if (textNode == null) {
			textNode = factory.createTextNode(data);
			element.appendChild(textNode);
		} else {
			textNode.setData(data);
		}
	}
	
	/**
	 * Saves this memento's document current values to the
	 * specified writer. 
	 * 
	 * @param writer the writer used to save the memento's document
	 */
	public void save(Writer writer) throws IOException {
		OutputFormat format = new OutputFormat();
		format.setPreserveSpace(true);
		Serializer serializer = SerializerFactory.getSerializerFactory("xml").makeSerializer(writer, format); //$NON-NLS-1$
		serializer.asDOMSerializer().serialize(factory);
	}
}