/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.Method;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.Serializer;
import org.apache.xml.serialize.SerializerFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.ISimpleLaunchVariable;
import org.eclipse.debug.core.variables.ISimpleVariableRegistry;
import org.eclipse.debug.core.variables.IVariableInitializer;
import org.eclipse.debug.core.variables.SimpleLaunchVariable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * A registry of simple variables, used for retrieving launch configuration
 * variable objects based on their names.
 */
public class SimpleVariableRegistry implements ISimpleVariableRegistry {
	
	// Variable extension point constants
	private static final String ELEMENT_VARIABLE="variable"; //$NON-NLS-1$
	private static final String ATTR_NAME= "name"; //$NON-NLS-1$
	private static final String ATTR_INITIAL_VALUE= "initialValue"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION="description"; //$NON-NLS-1$
	private static final String ATTR_INITIALIZER_CLASS= "initializerClass"; //$NON-NLS-1$
	// Persisted variable XML constants
	private static final String SIMPLE_VARIABLES_TAG= "simpleVariables"; //$NON-NLS-1$
	private static final String VARIABLE_TAG= "variable"; //$NON-NLS-1$
	private static final String NAME_TAG= "name"; //$NON-NLS-1$
	private static final String VALUE_TAG= "value"; //$NON-NLS-1$

	private Map fVariables= new HashMap(); 
	
	public SimpleVariableRegistry() {
		loadExtensions();
		loadVariables();
	}
	
	/**
	 * @see ISimpleVariableRegistry#addVariable(ISimpleLaunchVariable)
	 */
	public void addVariable(ISimpleLaunchVariable variable) {
		fVariables.put(variable.getName(), variable);
	}
	
	/**
	 * @see ISimpleVariableRegistry#addVariables(ISimpleLaunchVariable[])
	 */
	public void addVariables(ISimpleLaunchVariable[] variables) {
		for (int i = 0; i < variables.length; i++) {
			addVariable(variables[i]);
		}
	}
	
	/**
	 * @see ISimpleVariableRegistry#removeVariable(ISimpleLaunchVariable)
	 */
	public void removeVariable(ISimpleLaunchVariable variable) {
		fVariables.remove(variable.getName());
	}
	
	/**
	 * @see ISimpleVariableRegistry#clear()
	 */
	public void clear() {
		fVariables.clear();
	}
	
	/**
	 * @see ISimpleVariableRegistry#getVariable(String)
	 */
	public ISimpleLaunchVariable getVariable(String name) {
		return (ISimpleLaunchVariable) fVariables.get(name);
	}
	
	/**
	 * @see ISimpleVariableRegistry#getVariables()
	 */
	public ISimpleLaunchVariable[] getVariables() {
		return (ISimpleLaunchVariable[]) fVariables.values().toArray(new ISimpleLaunchVariable[0]);
	}
	
	/**
	 * Loads the variables contributed via extension.
	 */
	public void loadExtensions() {
		IExtensionPoint point= DebugPlugin.getDefault().getDescriptor().getExtensionPoint(DebugPlugin.EXTENSION_POINT_SIMPLE_LAUNCH_VARIABLES);
		IConfigurationElement elements[]= point.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals(ELEMENT_VARIABLE)) {
				DebugPlugin.logMessage(MessageFormat.format("Invalid simple launch variable extension found: {0}", new String[] {element.getDeclaringExtension().getLabel()}), null); //$NON-NLS-1$
				continue;
			}
			String name= element.getAttribute(ATTR_NAME);
			if (name == null) {
				DebugPlugin.logMessage(MessageFormat.format("Invalid simple launch variable extension found: {0}", new String[] {element.getDeclaringExtension().getLabel()}), null); //$NON-NLS-1$
				continue;
			}
			IVariableInitializer initializer= null;
			if (element.getAttribute(ATTR_INITIALIZER_CLASS) != null) {
				try {
					initializer= (IVariableInitializer) element.createExecutableExtension(ATTR_INITIALIZER_CLASS);
				} catch (CoreException e) {
					DebugPlugin.logMessage(MessageFormat.format("Failed to load launch variable initializer: {0}", new String[] {element.getAttribute(ATTR_INITIALIZER_CLASS)}), e); //$NON-NLS-1$
				}
			}
			String initialValue= element.getAttribute(ATTR_INITIAL_VALUE);
			String description= element.getAttribute(ATTR_DESCRIPTION);
			ISimpleLaunchVariable variable= new SimpleLaunchVariable(name, initializer, initialValue, description);
			fVariables.put(variable.getName(), variable);
		}
	}
	
	/**
	 * Loads the variables from a file in the metadata.
	 */
	public void loadVariables() {
		File file= getVariableFile();
		if (!file.exists()) {
			return;
		}
		Element root= null;
		try {
			FileInputStream inputStream= new FileInputStream(file);
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			root = parser.parse(new InputSource(inputStream)).getDocumentElement();
		} catch (Throwable throwable) {
			DebugPlugin.logMessage("An exception occurred while loading launch configuration variables.", throwable); //$NON-NLS-1$
			return;
		}
		if (!root.getNodeName().equals(SIMPLE_VARIABLES_TAG)) {
			DebugPlugin.logMessage("Invalid format encountered while loading launch configuration variables.", null); //$NON-NLS-1$
			return;
		}
		NodeList list= root.getChildNodes();
		for (int i= 0, numItems= list.getLength(); i < numItems; i++) {
			Node node= list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (!element.getNodeName().equals(VARIABLE_TAG)) {
					DebugPlugin.logMessage(MessageFormat.format("Invalid XML element encountered while loading launch configuration variables: {0}", new String[] {node.getNodeName()}), null); //$NON-NLS-1$
					continue;
				}
				String name= element.getAttribute(NAME_TAG);
				if (name.length() > 0) {
					ISimpleLaunchVariable variable= getVariable(name);;
					if (variable == null) {
						variable= new SimpleLaunchVariable(name);
					}
					String value= element.getAttribute(VALUE_TAG);
					if (value.length() > 0) {
						variable.setText(value);
					}
					fVariables.put(name, variable);
				} else {
					DebugPlugin.logMessage("Invalid variable entry encountered while loading launch configuration variables. Variable name is null.", null); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * @see ISimpleVariableRegistry#storeVariables()
	 */
	public void storeVariables() {
		if (fVariables.isEmpty()) {
			getVariableFile().delete();
			return;
		}
		try {
			String xml= getVariablesAsXML();
			File file= getVariableFile();
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream stream = new FileOutputStream(file);
			stream.write(xml.getBytes("UTF8")); //$NON-NLS-1$
			stream.close();
		} catch (IOException exception) {
			DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while storing launch configuration variables.", exception)); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the file in which variables are persisted
	 * @return the file in which variables are persisted
	 */
	private File getVariableFile() {
		IPath path= DebugPlugin.getDefault().getStateLocation().append(".launchConfigurationVariables"); //$NON-NLS-1$
		return path.toFile();
	}
	
	/**
	 * Returns the map of variables in this registry in XML format suitable for persistance.
	 * @return the variables in this registry in XML format
	 * @throws IOException if an I/O exception occurs while creating the XML.
	 */
	private String getVariablesAsXML() throws IOException {
		Set entries= fVariables.entrySet();
		Iterator iter= entries.iterator();

		Document document= new DocumentImpl();
		Element rootElement= document.createElement(SIMPLE_VARIABLES_TAG);
		document.appendChild(rootElement);
		while (iter.hasNext()) {
			Map.Entry entry= (Map.Entry) iter.next();
			Element element= document.createElement(VARIABLE_TAG);
			element.setAttribute(NAME_TAG, (String)entry.getKey());
			element.setAttribute(VALUE_TAG, ((ISimpleLaunchVariable)entry.getValue()).getText());
			rootElement.appendChild(element);
		}

		ByteArrayOutputStream s= new ByteArrayOutputStream();
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setLineSeparator(System.getProperty("line.separator")); //$NON-NLS-1$
		
		Serializer serializer =
			SerializerFactory.getSerializerFactory(Method.XML).makeSerializer(
				new OutputStreamWriter(s, "UTF8"), //$NON-NLS-1$
				format);
		serializer.asDOMSerializer().serialize(document);
		return s.toString("UTF8"); //$NON-NLS-1$
	}
}
