package org.eclipse.jface.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.*;


/**
 * Concrete implementation of a dialog settings (<code>IDialogSettings</code>)
 * using a hash table and XML. The dialog store can be read
 * from and saved to a stream. All keys and values must be strings or array of
 * strings. Primitive types are converted to strings.
 * <p>
 * This class was not designed to be subclassed.
 *
 * Here is an example of using a DialogSettings:
 * </p>
 * <code>
 * DialogSettings settings = new DialogSettings("root");
 * settings.put("Boolean1",true);
 * settings.put("Long1",100);
 * settings.put("Array1",new String[]{"aaaa1","bbbb1","cccc1"});
 * DialogSettings section = new DialogSettings("sectionName");
 * settings.addSection(section);
 * section.put("Int2",200);
 * section.put("Float2",1.1);
 * section.put("Array2",new String[]{"aaaa2","bbbb2","cccc2"});
 * settings.save("c:\\temp\\test\\dialog.xml");
 * </code>
 */
 
public class DialogSettings implements IDialogSettings {
	// The name of the DialogSettings.
	private String name;
	/* A Map of DialogSettings representing each sections in a DialogSettings.
	   It maps the DialogSettings' name to the DialogSettings */
	private Map sections;
	/* A Map with all the keys and values of this sections.
	   Either the keys an values are restricted to strings. */
	private Map items;
	// A Map with all the keys mapped to array of strings.
	private Map arrayItems;

	private final String TAG_SECTION = "section";//$NON-NLS-1$
	private final String TAG_NAME = "name";//$NON-NLS-1$
	private final String TAG_KEY = "key";//$NON-NLS-1$
	private final String TAG_VALUE = "value";//$NON-NLS-1$
	private final String TAG_LIST = "list";//$NON-NLS-1$
	private final String TAG_ITEM = "item";//$NON-NLS-1$
/**
 * Create an empty dialog settings which loads and saves its
 * content to a file.
 * Use the methods <code>load(String)</code> and <code>store(String)</code>
 * to load and store this dialog settings.
 *
 * @param sectionName the name of the section in the settings.
 */
public DialogSettings(String sectionName) {
	name = sectionName;
	items = new HashMap();
	arrayItems = new HashMap();
	sections = new HashMap();
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public IDialogSettings addNewSection(String name) {
	DialogSettings section = new DialogSettings(name);
	addSection(section);
	return section;
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void addSection(IDialogSettings section) { 
	sections.put(section.getName(),section);
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public String get(String key) {
	return (String)items.get(key);
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public String[] getArray(String key) {
	return (String[])arrayItems.get(key);
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public boolean getBoolean(String key) {
	return new Boolean((String)items.get(key)).booleanValue();
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public double getDouble(String key) throws NumberFormatException {
	String setting = (String)items.get(key);
	if(setting == null)
		throw new NumberFormatException("There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
		
	return new Double(setting).doubleValue();
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public float getFloat(String key) throws NumberFormatException {
	String setting = (String)items.get(key);
	if(setting == null)
		throw new NumberFormatException("There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
		
	return new Float(setting).floatValue();
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public int getInt(String key) throws NumberFormatException {
	String setting = (String)items.get(key);
	if(setting == null) {
		//new Integer(null) will throw a NumberFormatException and meet our spec, but this message
		//is clearer.
		throw new NumberFormatException("There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
	}
		
	return new Integer(setting).intValue();
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public long getLong(String key) throws NumberFormatException {
	String setting = (String)items.get(key);
	if(setting == null) {
		//new Long(null) will throw a NumberFormatException and meet our spec, but this message
		//is clearer.
		throw new NumberFormatException("There is no setting associated with the key \"" + key + "\"");//$NON-NLS-1$ //$NON-NLS-2$
	}
		
	return new Long(setting).longValue();
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public String getName() {
	return name;
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public IDialogSettings getSection(String sectionName) {
	return (IDialogSettings)sections.get(sectionName);
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public IDialogSettings[] getSections() {
	Collection values = sections.values();
	DialogSettings[] result = new DialogSettings[values.size()];
	values.toArray(result);
	return result;
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void load(Reader r) {
	Document document = null;
	try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
//		parser.setProcessNamespace(true);
		document = parser.parse(new InputSource(r));
		Element root = (Element) document.getFirstChild();
		load(document, root);
	} catch (ParserConfigurationException e) {
	} catch (IOException e) {
	} catch (SAXException e) {
	}
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void load(String fileName) throws IOException {
	FileInputStream stream = new FileInputStream(fileName);
	InputStreamReader reader = new InputStreamReader(stream, "utf-8");//$NON-NLS-1$
	load(reader);
	reader.close();
}
/* (non-Javadoc)
 * Load the setting from the <code>document</code>
 */
private void load(Document document,Element root) {
	name = root.getAttribute(TAG_NAME);
	NodeList l = root.getElementsByTagName(TAG_ITEM);
	for (int i = 0; i < l.getLength(); i++){
		Node n = l.item(i);
		if(root == n.getParentNode()) {
			String key = ((Element)l.item(i)).getAttribute(TAG_KEY);
			String value = ((Element)l.item(i)).getAttribute(TAG_VALUE);
			items.put(key,value);
		}
	}
	l = root.getElementsByTagName(TAG_LIST);
	for (int i = 0; i < l.getLength(); i++){
		Node n = l.item(i);
		if(root == n.getParentNode()) {
			Element child = (Element)l.item(i);
			String key = child.getAttribute(TAG_KEY);
			NodeList list = child.getElementsByTagName(TAG_ITEM);
			List valueList = new ArrayList();
			for (int j = 0; j < list.getLength(); j++){
				Element node = (Element)list.item(j);
				if(child == node.getParentNode()) {
					valueList.add(node.getAttribute(TAG_VALUE));
				}
			}
			String[] value = new String[valueList.size()];
			valueList.toArray(value);
			arrayItems.put(key,value);
		}
	}
	l = root.getElementsByTagName(TAG_SECTION);
	for (int i = 0; i < l.getLength(); i++){
		Node n = l.item(i);
		if(root == n.getParentNode()) {
			DialogSettings s = new DialogSettings("NoName");//$NON-NLS-1$
			s.load(document,(Element)n);
			addSection(s);
		}
	}
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void put(String key,String[] value) {
	arrayItems.put(key,value);
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void put(String key,double value) {
	put(key,String.valueOf(value));
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void put(String key,float value) {
	put(key,String.valueOf(value));
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void put(String key,int value) {
	put(key,String.valueOf(value));
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void put(String key,long value) {
	put(key,String.valueOf(value));
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void put(String key,String value) {
	items.put(key,value);
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void put(String key,boolean value) {
	put(key,String.valueOf(value));
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void save(Writer writer) throws IOException {
	Document document = new DocumentImpl();
	save(document, (Node) document);
	OutputFormat format = new OutputFormat();
	Serializer serializer = SerializerFactory.getSerializerFactory("xml").makeSerializer(writer, format);//$NON-NLS-1$
	serializer.asDOMSerializer().serialize(document);
}
/* (non-Javadoc)
 * Method declared on IDialogSettings.
 */
public void save(String fileName) throws IOException {
	FileOutputStream stream = new FileOutputStream(fileName);
	OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8");//$NON-NLS-1$
	save(writer);
	writer.close();
}
/* (non-Javadoc)
 * Save the settings in the <code>document</code>.
 */
private void save(Document document,Node parent) {
	Element root = document.createElement(TAG_SECTION);
	parent.appendChild(root);
	root.setAttribute(TAG_NAME, name);
	
	for(Iterator i = items.keySet().iterator();i.hasNext();) {
		String key = (String)i.next();
		Element child = document.createElement(TAG_ITEM);
		root.appendChild(child);
		child.setAttribute(TAG_KEY, key);
		child.setAttribute(TAG_VALUE, (String)items.get(key));	
	}

	for(Iterator i = arrayItems.keySet().iterator();i.hasNext();) {
		String key = (String)i.next();
		Element child = document.createElement(TAG_LIST);
		root.appendChild(child);
		child.setAttribute(TAG_KEY, key);
		String[] value = (String[])arrayItems.get(key);
		for (int index = 0; index < value.length; index++){
			Element c = document.createElement(TAG_ITEM);
			child.appendChild(c);
			c.setAttribute(TAG_VALUE, value[index]);
		}	
	}
	for(Iterator i = sections.values().iterator();i.hasNext();) {
		((DialogSettings)i.next()).save(document,root);
	}	
}
}
