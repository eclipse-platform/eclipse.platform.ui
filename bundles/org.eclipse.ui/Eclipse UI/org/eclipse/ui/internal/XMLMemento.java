package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import org.eclipse.ui.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.*;

/**
 * A Memento is a class independent container for persistence
 * info.  It is a reflection of 3 storage requirements.
 *
 * 1) 	We need the ability to persist an object and restore it.  
 * 2) 	The class for an object may be absent.  If so we would 
 * 		like to skip the object and keep reading. 
 * 3) 	The class for an object may change.  If so the new class 
 * 		should be able to read the old persistence info.
 *
 * We could ask the objects to serialize themselves into an 
 * ObjectOutputStream, DataOutputStream, or Hashtable.  However 
 * all of these approaches fail to meet the second requirement.
 *
 * Memento supports binary persistance with a version ID.
 */
public final class XMLMemento implements IMemento {
	private Document factory;
	private Element element;
/**
 * Answer a memento for the document and element.  For simplicity
 * you should use createReadRoot and createWriteRoot to create the initial
 * mementos on a document.
 */
public XMLMemento(Document doc, Element el) {
	factory = doc;
	element = el;
}
/**
 * @see IMemento.
 */
public IMemento createChild(String type) {
	Element child = factory.createElement(type);
	element.appendChild(child);
	return new XMLMemento(factory, child);
}
/**
 * @see IMemento.
 */
public IMemento createChild(String type, String id) {
	Element child = factory.createElement(type);
	child.setAttribute(TAG_ID, id);
	element.appendChild(child);
	return new XMLMemento(factory, child);
}
/**
 * Create a Document from a Reader and answer a root memento for reading 
 * a document.
 */
static public XMLMemento createReadRoot(Reader reader) {
	Document document = null;
	try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		document = parser.parse(new InputSource(reader));
		Node node = document.getFirstChild();
		if (node instanceof Element)
			return new XMLMemento(document, (Element) node);
	} catch (ParserConfigurationException e) {
	} catch (IOException e) {
	} catch (SAXException e) {
	}
	return null;
}
/**
 * Answer a root memento for writing a document.
 */
static public XMLMemento createWriteRoot(String type) {
	Document document = new DocumentImpl();
	Element element = document.createElement(type);
	document.appendChild(element);
	return new XMLMemento(document, element);
}
/**
 * Copy a child from one document to another.
 */
public IMemento copyChild(IMemento child) {
	Element childElement = ((XMLMemento)child).element;
	Element newElement = (Element)factory.importNode(childElement, true);
	element.appendChild(newElement);	
	return new XMLMemento(factory, newElement);
}

/**
 * @see IMemento.
 */
public IMemento getChild(String type) {
	
	// Get the nodes.
	NodeList nodes = element.getChildNodes();
	int size = nodes.getLength();
	if (size == 0)
		return null;

	// Find the first node which is a child of this node.
	for (int nX = 0; nX < size; nX ++) {
		Node node = nodes.item(nX);
		if (node instanceof Element) {
			Element element = (Element)node;
			if (element.getNodeName().equals(type))
				return new XMLMemento(factory, element);
		}
	}

	// A child was not found.
	return null;
}
/**
 * @see IMemento.
 */
public IMemento [] getChildren(String type) {

	// Get the nodes.
	NodeList nodes = element.getChildNodes();
	int size = nodes.getLength();
	if (size == 0)
		return new IMemento[0];

	// Extract each node with given type.
	ArrayList list = new ArrayList(size);
	for (int nX = 0; nX < size; nX ++) {
		Node node = nodes.item(nX);
		if (node instanceof Element) {
			Element element = (Element)node;
			if (element.getNodeName().equals(type))
				list.add(element);
		}
	}

	// Create a memento for each node.
	size = list.size();
	IMemento [] results = new IMemento[size];
	for (int x = 0; x < size; x ++) {
		results[x] = new XMLMemento(factory, (Element)list.get(x));
	}
	return results;
}
/**
 * Answer the XML element contained in this memento.
 */
private Element getElement() {
	return element;
}
/**
 * @see IMemento.
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
			+ key + " value: " + strValue);//$NON-NLS-1$
		return null;
	}
}
/**
 * @see IMemento.
 */
public String getID() {
	return element.getAttribute(TAG_ID);
}
/**
 * @see IMemento.
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
			+ " value: " + strValue);//$NON-NLS-1$
		return null;
	}
}
/**
 * @see IMemento.
 */
public String getString(String key) {
	Attr attr = element.getAttributeNode(key);
	if (attr == null)
		return null; 
	return attr.getValue();
}
/**
 * @see IMemento.
 */
private void putElement(Element element) {
	NamedNodeMap nodeMap = element.getAttributes();
	int size = nodeMap.getLength();
	for (int i = 0; i < size; i++){
		Attr attr = (Attr)nodeMap.item(i);
		putString(attr.getName(),attr.getValue());
	}
				
	NodeList nodes = element.getChildNodes();
	size = nodes.getLength();
	for (int i = 0; i < size; i ++) {
		Node node = nodes.item(i);
		if (node instanceof Element) {
			XMLMemento child = (XMLMemento)createChild(node.getNodeName());
			child.putElement((Element)node);
		}
	}
}
/**
 * @see IMemento.
 */
public void putFloat(String key, float f) {
	element.setAttribute(key, String.valueOf(f));
}
/**
 * @see IMemento.
 */
public void putInteger(String key, int n) {
	element.setAttribute(key, String.valueOf(n));
}
/**
 * @see IMemento.
 */
public void putMemento(IMemento memento) {
	XMLMemento xmlMemento = (XMLMemento)memento;
	putElement(((XMLMemento)memento).element);
}
/**
 * @see IMemento.
 */
public void putString(String key, String value) {
	if(value==null) return;
	element.setAttribute(key, value);
}
/**
 * Save this Memento to a Writer.
 */
public void save(Writer writer) throws IOException {
	OutputFormat format = new OutputFormat();
	Serializer serializer = SerializerFactory.getSerializerFactory("xml").makeSerializer(writer, format);//$NON-NLS-1$
	serializer.asDOMSerializer().serialize(factory);
}
}
