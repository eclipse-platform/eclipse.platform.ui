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
package org.eclipse.debug.internal.core.variables;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.variables.ISimpleLaunchVariable;
import org.eclipse.debug.internal.core.LaunchManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A registry of simple variables, used for retrieving launch configuration
 * variable objects based on their names.
 */
public class SimpleLaunchVariableRegistry {
	
	private static final String PREF_SIMPLE_VARIABLES="simpleVariables"; //$NON-NLS-1$
	// Variable extension point constants
	private static final String ELEMENT_VARIABLE="variable"; //$NON-NLS-1$
	private static final String ATTR_NAME= "name"; //$NON-NLS-1$
	private static final String ATTR_INITIAL_VALUE= "initialValue"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION="description"; //$NON-NLS-1$
	protected static final String ATTR_INITIALIZER_CLASS= "initializerClass"; //$NON-NLS-1$
	// Persisted variable XML constants
	private static final String SIMPLE_VARIABLES_TAG= "simpleVariables"; //$NON-NLS-1$
	private static final String VARIABLE_TAG= "variable"; //$NON-NLS-1$
	private static final String NAME_TAG= "name"; //$NON-NLS-1$
	private static final String VALUE_TAG= "value"; //$NON-NLS-1$
	private static final String DESCRIPTION_TAG="description"; //$NON-NLS-1$
	private static final String CONTRIBUTED_TAG="contributed"; //$NON-NLS-1$
	// XML values
	private static final String TRUE_VALUE= "true"; //$NON-NLS-1$
	private static final String FALSE_VALUE= "false"; //$NON-NLS-1$

	private Map fVariables= new HashMap();
	private List fContributedVariables= new ArrayList(); 
	
	public SimpleLaunchVariableRegistry() {
		loadExtensions();
		loadVariables();
	}
	
	/**
	 * Adds the given variables to this variable registry
	 * 
	 * @param variables the variables to add
	 */
	public void addVariables(ISimpleLaunchVariable[] variables) {
		for (int i = 0; i < variables.length; i++) {
			fVariables.put(variables[i].getName(), variables[i]);
		}
		storeVariables();
	}
	
	/**
	 * Removes the given variables from this registry. Has no effect
	 * if any of the given variables are not in this registry.
	 * 
	 * @param variables the variables to remove
	 */
	public void removeVariables(ISimpleLaunchVariable[] variables) {
		for (int i = 0; i < variables.length; i++) {
			fVariables.remove(variables[i].getName());
		}
		storeVariables();
	}
	
	/**
	 * Returns the variable with the given name or <code>null</code>
	 * if no such variable exists. If multiple variables with the given name have
	 * been added to this registry, returns the most recently added variable
	 * with that name.
	 * 
	 * @param name the name of the variable
	 * @return the launch configuration variable with the given name or
	 * <code>null</code> if no such variable exists.
	 */
	public ISimpleLaunchVariable getVariable(String name) {
		return (ISimpleLaunchVariable) fVariables.get(name);
	}
	
	/**
	 * Returns all the variables contained in this registry
	 * 
	 * @return the variables in this registry.
	 */
	public ISimpleLaunchVariable[] getVariables() {
		return (ISimpleLaunchVariable[]) fVariables.values().toArray(new ISimpleLaunchVariable[0]);
	}
	
	/**
	 * Returns all the variables in this registry contributed via extension
	 * 
	 * @return the contributed variables in this registry
	 */
	public ISimpleLaunchVariable[] getContributedVariables() {
		return (ISimpleLaunchVariable[]) fContributedVariables.toArray(new ISimpleLaunchVariable[fContributedVariables.size()]);
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
			String initialValue= element.getAttribute(ATTR_INITIAL_VALUE);
			String description= element.getAttribute(ATTR_DESCRIPTION);
			ISimpleLaunchVariable variable= new SimpleLaunchVariable(name, initialValue, description, element);
			fVariables.put(variable.getName(), variable);
			fContributedVariables.add(variable);
		}
	}
	
	/**
	 * Loads the variables from the preferences.
	 */
	public void loadVariables() {
		String variablesString= DebugPlugin.getDefault().getPluginPreferences().getString(PREF_SIMPLE_VARIABLES);
		if (variablesString.length() == 0) {
			return;
		}
		Element root= null;
		try {
			ByteArrayInputStream stream= new ByteArrayInputStream(variablesString.getBytes("UTF-8")); //$NON-NLS-1$
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			root = parser.parse(stream).getDocumentElement();
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
					String value= element.getAttribute(VALUE_TAG);
					String description= element.getAttribute(DESCRIPTION_TAG);
					boolean contributed= TRUE_VALUE.equals(element.getAttribute(CONTRIBUTED_TAG));
					ISimpleLaunchVariable variable= new SimpleLaunchVariable(name, value, description, contributed);
					fVariables.put(name, variable);
				} else {
					DebugPlugin.logMessage("Invalid variable entry encountered while loading launch configuration variables. Variable name is null.", null); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * Saves the variables in this registry in the
	 * preference store. 
	 */
	protected void storeVariables() {
		Preferences prefs= DebugPlugin.getDefault().getPluginPreferences();
		String variableString= ""; //$NON-NLS-1$
		if (!fVariables.isEmpty()) {
			try {
				variableString= getVariablesAsXML();
			} catch (IOException exception) {
				DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while storing launch configuration variables.", exception)); //$NON-NLS-1$
			}
		}
		prefs.setValue(PREF_SIMPLE_VARIABLES, variableString);
		DebugPlugin.getDefault().savePluginPreferences();
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
			ISimpleLaunchVariable variable= (ISimpleLaunchVariable) entry.getValue();
			Element element= document.createElement(VARIABLE_TAG);
			element.setAttribute(NAME_TAG, (String)entry.getKey());
			element.setAttribute(VALUE_TAG, variable.getValue());
			element.setAttribute(DESCRIPTION_TAG, variable.getDescription());
			element.setAttribute(CONTRIBUTED_TAG, variable.isContributed() ? TRUE_VALUE : FALSE_VALUE);
			rootElement.appendChild(element);
		}
		return LaunchManager.serializeDocument(document);
	}
}
