/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.ui.internal.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class EngineDescriptorManager implements IHelpUIConstants {
	private ArrayList descriptors;

	private EngineTypeDescriptor[] engineTypes;

	private static final String USER_FILE = "userSearches.xml";
	private static final String ATT_ENGINE_TYPE_ID = "engineTypeId";

	public EngineDescriptorManager() {
		descriptors = new ArrayList();
		load();
	}

	public EngineDescriptor[] getDescriptors() {
		return (EngineDescriptor[]) descriptors
				.toArray(new EngineDescriptor[descriptors.size()]);
	}

	public EngineTypeDescriptor[] getEngineTypes() {
		return engineTypes;
	}

	public void save() {
		IPath stateLoc = HelpUIPlugin.getDefault().getStateLocation();
		String fileName = stateLoc.append(USER_FILE).toOSString();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			PrintWriter writer = new PrintWriter(fos);
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.println("<engines>");
			for (int i = 0; i < descriptors.size(); i++) {
				EngineDescriptor desc = (EngineDescriptor) descriptors.get(i);
				if (desc.isUserDefined()) {
					save(writer, desc);
				}
			}
			writer.println("</engines>");
		}
		catch (IOException e) {
			HelpUIPlugin.logError("Error while saving user searches", e);
		}
		finally {
			if (fos!=null) {
				try {
					fos.close();
					fos=null;
				}
				catch (IOException e) {
				}
			}
		}
	}

	public void load() {
		loadFromExtensionRegistry();
		IPath stateLoc = HelpUIPlugin.getDefault().getStateLocation();
		String fileName = stateLoc.append(USER_FILE).toOSString();
		try {
			load(fileName);
		}
		catch (IOException e) {
			HelpUIPlugin.logError("Errors while loading user searches", e);
		}
	}

	private void loadFromExtensionRegistry() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(ENGINE_EXP_ID);
		Hashtable engineTypes = loadEngineTypes(elements);
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals(TAG_ENGINE)) { //$NON-NLS-1$
				EngineDescriptor desc = new EngineDescriptor(element);
				String engineId = desc.getEngineTypeId();
				if (engineId != null) {
					EngineTypeDescriptor etdesc = (EngineTypeDescriptor) engineTypes
							.get(engineId);
					if (etdesc != null) {
						desc.setEngineType(etdesc);
						descriptors.add(desc);
					}
				}
			}
		}
	}

	private Hashtable loadEngineTypes(IConfigurationElement[] elements) {
		Hashtable result = new Hashtable();
		ArrayList list = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("engineType")) { //$NON-NLS-1$
				EngineTypeDescriptor etdesc = new EngineTypeDescriptor(element);
				String id = etdesc.getId();
				if (id != null) {
					list.add(etdesc);
					result.put(etdesc.getId(), etdesc);
				}
			}
		}
		engineTypes = (EngineTypeDescriptor[]) list
				.toArray(new EngineTypeDescriptor[list.size()]);
		return result;
	}

	public void load(Reader r) {
		Document document = null;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			// parser.setProcessNamespace(true);
			document = parser.parse(new InputSource(r));

			// Strip out any comments first
			Node root = document.getFirstChild();
			while (root.getNodeType() == Node.COMMENT_NODE) {
				document.removeChild(root);
				root = document.getFirstChild();
			}
			load(document, (Element) root);
		} catch (ParserConfigurationException e) {
			// ignore
		} catch (IOException e) {
			// ignore
		} catch (SAXException e) {
			// ignore
		}
	}

	/*
	 * (non-Javadoc) Method declared on IDialogSettings.
	 */
	public void load(String fileName) throws IOException {
		FileInputStream stream = new FileInputStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				stream, "utf-8"));//$NON-NLS-1$
		load(reader);
		reader.close();
	}

	private void load(Document doc, Element root) {
		NodeList engines = root.getElementsByTagName(TAG_ENGINE);
		for (int i=0; i<engines.getLength(); i++) {
			Node node = engines.item(i);
			loadUserEntry(node);
		}
	}

	private void loadUserEntry(Node node) {
		EngineDescriptor edesc = new EngineDescriptor(null);
		String id = getAttribute(node, ATT_ID);
		String engineTypeId = getAttribute(node, ATT_ENGINE_TYPE_ID);
		EngineTypeDescriptor etdesc = findEngineType(engineTypeId);
		String label = getAttribute(node, ATT_LABEL);
		String desc = getDescription(node);
		if (etdesc == null)
			return;
		edesc.setEngineType(etdesc);
		edesc.setId(id);
		edesc.setLabel(label);
		edesc.setDescription(desc);
		edesc.setUserDefined(true);
		descriptors.add(edesc);
	}
	
	private String getDescription(Node node) {
		NodeList list = ((Element)node).getElementsByTagName(TAG_DESC);
		if (list.getLength()==1) {
			Node desc = list.item(0);
			return desc.getNodeValue();
		}
		return null;
	}
	
	private void save(PrintWriter writer, EngineDescriptor desc) {
		String indent = "   ";
		String attIndent = indent + indent;
		writer.print(indent);
		writer.println("<engine ");
		saveAttribute(writer, attIndent, "id", desc.getId());
		saveAttribute(writer, attIndent, ATT_ENGINE_TYPE_ID, desc.getEngineTypeId());
		saveAttribute(writer, attIndent, ATT_LABEL, desc.getLabel());
		writer.println(">");
		saveDescription(writer, indent+indent, desc.getDescription());
		writer.print(indent);
		writer.println("</engine>");
	}
	
	private String getAttribute(Node node, String name) {
		Node att = node.getAttributes().getNamedItem(name);
		if (att!=null)
			return att.getNodeValue();
		return null;
	}
	
	private void saveAttribute(PrintWriter writer, String indent, String name, String value) {
		if (value==null)
			return;
		writer.print(indent);
		writer.print(name);
		writer.print("=\"");
		writer.print(value);
		writer.println("\"");
	}
	private void saveDescription(PrintWriter writer, String indent, String desc) {
		if (desc==null)
			return;
		writer.print(indent);
		writer.println("<description>");
		writer.println(desc);
		writer.print(indent);
		writer.println("</description>");
	}

	private EngineTypeDescriptor findEngineType(String id) {
		if (id == null)
			return null;
		for (int i = 0; i < engineTypes.length; i++) {
			EngineTypeDescriptor etd = engineTypes[i];
			if (etd.getId().equals(id))
				return etd;
		}
		return null;
	}
}
