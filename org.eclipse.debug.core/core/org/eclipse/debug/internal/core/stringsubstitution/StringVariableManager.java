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
package org.eclipse.debug.internal.core.stringsubstitution;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DocumentImpl;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.LaunchManager;
import org.eclipse.debug.internal.core.ListenerList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Singleton string variable manager. 
 */
public class StringVariableManager implements IStringVariableManager {
	
	/**
	 * Context variables - maps variable names to variables.
	 */
	private Map fContextVariables;
	
	/**
	 * Value varialbes - maps variable names to variables.
	 */
	private Map fValueVariables;
	
	/**
	 * Variable listeners
	 */
	private ListenerList fListeners;
	
	// notifications
	private static final int ADDED = 0;
	private static final int CHANGED = 1;
	private static final int REMOVED = 2;
	
	/**
	 * Singleton variable manager.
	 */
	private static StringVariableManager fgManager; 
	
	/**
	 * Simple identifier constant (value <code>"contextVariables"</code>) for the
	 * context variables extension point.
	 * 
	 * @since 3.0
	 * TODO: this should be public API
	 */
	public static final String EXTENSION_POINT_CONTEXT_VARIABLES = "contextVariables"; //$NON-NLS-1$
	
	/**
	 * Simple identifier constant (value <code>"valueVariables"</code>) for the
	 * value variables extension point.
	 * 
	 * @since 3.0
	 * TODO: this should be public API
	 */
	public static final String EXTENSION_POINT_VALUE_VARIABLES = "valueVariables"; //$NON-NLS-1$	
	
	// Variable extension point constants
	private static final String ATTR_NAME= "name"; //$NON-NLS-1$
	private static final String ATTR_DESCRIPTION="description"; //$NON-NLS-1$	
	// Persisted variable XML constants
	private static final String VALUE_VARIABLES_TAG= "valueVariables"; //$NON-NLS-1$
	private static final String VALUE_VARIABLE_TAG= "valueVariable"; //$NON-NLS-1$
	private static final String NAME_TAG= "name"; //$NON-NLS-1$
	private static final String VALUE_TAG= "value"; //$NON-NLS-1$
	private static final String DESCRIPTION_TAG="description"; //$NON-NLS-1$
	private static final String INITIALIZED_TAG="contributed"; //$NON-NLS-1$
	// XML values
	private static final String TRUE_VALUE= "true"; //$NON-NLS-1$
	private static final String FALSE_VALUE= "false"; //$NON-NLS-1$
	// preference store key for value variables
	private static final String PREF_VALUE_VARIABLES= DebugPlugin.getUniqueIdentifier() + ".valueVariables"; //$NON-NLS-1$	
		
	/**
	 * Notifies a string variable listener in a safe runnable to handle
	 * exceptions.
	 */
	class StringVariableNotifier implements ISafeRunnable {
		
		private IValueVariableListener fListener;
		private int fType;
		private IValueVariable[] fVariables;
		
		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#handleException(java.lang.Throwable)
		 */
		public void handleException(Throwable exception) {
			IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "An exception occurred during string variable change notification", exception); //$NON-NLS-1$
			DebugPlugin.log(status);
		}

		/**
		 * @see org.eclipse.core.runtime.ISafeRunnable#run()
		 */
		public void run() throws Exception {
			switch (fType) {
				case ADDED:
					fListener.variablesAdded(fVariables);
					break;
				case REMOVED:
					fListener.variablesRemoved(fVariables);
					break;
				case CHANGED:
					fListener.variablesChanged(fVariables);
					break;
			}			
		}

		/**
		 * Notifies the given listener of the add/change/remove
		 * 
		 * @param listener the listener to notify
		 * @param launch the launch that has changed
		 * @param update the type of change
		 */
		public void notify(IValueVariable[] variables, int update) {
			fVariables = variables;
			fType = update;
			Object[] copiedListeners= fListeners.getListeners();
			for (int i= 0; i < copiedListeners.length; i++) {
				fListener = (IValueVariableListener)copiedListeners[i];
				Platform.run(this);
			}	
			fVariables = null;
			fListener = null;
			// persist variables whenever there is an add/change/remove	
			storeValueVariables();	
		}
	}	
	
	/**
	 * Returns a new notifier.
	 * 
	 * @return a new notifier
	 */
	private StringVariableNotifier getNotifier() {
		return new StringVariableNotifier();
	}
	
	/**
	 * Returns the default string variable manager
	 * 
	 * @return string variable manager
	 */
	public static StringVariableManager getDefault() {
		if (fgManager == null) {
			fgManager = new StringVariableManager();
		}
		return fgManager;
	}
	
	/**
	 * Constructs a new string variable manager. 
	 */
	private StringVariableManager() {
		fListeners = new ListenerList(5);
		fContextVariables = new HashMap(5);
		fValueVariables = new HashMap(5);
		initialize();
	}	

	/**
	 * Load contributed variables and persisted variables
	 */
	private void initialize() {
		loadPersistedValueVariables();
		loadContributedValueVariables();
		loadContextVariables();
	}
	
	/**
	 * Loads contributed context variables
	 */
	private void loadContextVariables() {
		IExtensionPoint point= DebugPlugin.getDefault().getDescriptor().getExtensionPoint(EXTENSION_POINT_CONTEXT_VARIABLES);
		IConfigurationElement elements[]= point.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String name= element.getAttribute(ATTR_NAME);
			if (name == null) {
				DebugPlugin.logMessage(MessageFormat.format("Value variable extension missing required 'name' attribute: {0}", new String[] {element.getDeclaringExtension().getLabel()}), null); //$NON-NLS-1$
				continue;
			}
			String description= element.getAttribute(ATTR_DESCRIPTION);
			ContextVariable variable= new ContextVariable(name, description, element);
			fContextVariables.put(variable.getName(), variable);
		}
	}

	/**
	 * Loads any persisted value varialbes from the preference store.
	 */
	private void loadPersistedValueVariables() {
		String variablesString= DebugPlugin.getDefault().getPluginPreferences().getString(PREF_VALUE_VARIABLES);
		if (variablesString.length() == 0) {
			return;
		}
		Element root= null;
		try {
			ByteArrayInputStream stream= new ByteArrayInputStream(variablesString.getBytes("UTF-8")); //$NON-NLS-1$
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			root = parser.parse(stream).getDocumentElement();
		} catch (Throwable throwable) {
			DebugPlugin.logMessage("An exception occurred while loading persisted value variables.", throwable); //$NON-NLS-1$
			return;
		}
		if (!root.getNodeName().equals(VALUE_VARIABLES_TAG)) {
			DebugPlugin.logMessage("Invalid format encountered while loading persisted value variables.", null); //$NON-NLS-1$
			return;
		}
		NodeList list= root.getChildNodes();
		for (int i= 0, numItems= list.getLength(); i < numItems; i++) {
			Node node= list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (!element.getNodeName().equals(VALUE_VARIABLE_TAG)) {
					DebugPlugin.logMessage(MessageFormat.format("Invalid XML element encountered while loading value variables: {0}", new String[] {node.getNodeName()}), null); //$NON-NLS-1$
					continue;
				}
				String name= element.getAttribute(NAME_TAG);
				if (name.length() > 0) {
					String value= element.getAttribute(VALUE_TAG);
					String description= element.getAttribute(DESCRIPTION_TAG);
					boolean initialized= TRUE_VALUE.equals(element.getAttribute(INITIALIZED_TAG));
					ValueVariable variable= new ValueVariable(name, description, null);
					if (initialized) {
						variable.setValue(value);
					}
					fValueVariables.put(name, variable);
				} else {
					DebugPlugin.logMessage("Invalid variable entry encountered while loading value variables. Variable name is null.", null); //$NON-NLS-1$
				}
			}
		}		
	}

	/**
	 * Loads contributed value variables. This is done after the persisted value
	 * varaibles are restored. Any contributed variables with the same name are
	 * merged with existing persisted values.
	 */
	private void loadContributedValueVariables() {
		IExtensionPoint point= DebugPlugin.getDefault().getDescriptor().getExtensionPoint(EXTENSION_POINT_VALUE_VARIABLES);
		IConfigurationElement elements[]= point.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String name= element.getAttribute(ATTR_NAME);
			if (name == null) {
				DebugPlugin.logMessage(MessageFormat.format("Value variable extension missing required 'name' attribute: {0}", new String[] {element.getDeclaringExtension().getLabel()}), null); //$NON-NLS-1$
				continue;
			}
			String description= element.getAttribute(ATTR_DESCRIPTION);
			ValueVariable variable= new ValueVariable(name, description, element);
			// if already present, merge with persisted value
			ValueVariable existing = (ValueVariable)getValueVariable(name);
			if (existing != null) {
				if (existing.isInitialized()) {
					variable.setValue(existing.getValue());
				}					
			}
			fValueVariables.put(variable.getName(), variable);
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getVariables()
	 */
	public IStringVariable[] getVariables() {
		List list = new ArrayList(fContextVariables.size() + fValueVariables.size());
		list.addAll(fContextVariables.values());
		list.addAll(fValueVariables.values());
		return (IStringVariable[]) list.toArray(new IStringVariable[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getValueVariables()
	 */
	public IValueVariable[] getValueVariables() {
		return (IValueVariable[]) fValueVariables.values().toArray(new IValueVariable[fValueVariables.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getContextVariables()
	 */
	public IContextVariable[] getContextVariables() {
		return (IContextVariable[]) fContextVariables.values().toArray(new IContextVariable[fContextVariables.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#performStringSubstitution(java.lang.String)
	 */
	public String performStringSubstitution(String expression) throws CoreException {
		return new StringSubstitutionEngine().performStringSubstitution(expression);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#newValueVariable(java.lang.String, java.lang.String)
	 */
	public IValueVariable newValueVariable(String name, String description) throws CoreException {
		return new ValueVariable(name, description, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#addVariables(org.eclipse.debug.internal.core.stringsubstitution.IValueVariable[])
	 */
	public void addVariables(IValueVariable[] variables) throws CoreException {
		MultiStatus status = new MultiStatus(DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, "Variables with the specified names are already registered.", null); //$NON-NLS-1$
		for (int i = 0; i < variables.length; i++) {
			IValueVariable variable = variables[i];
			if (getValueVariable(variable.getName()) != null) {
				status.add(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, MessageFormat.format("Variable named {0} already registered", new String[]{variable.getName()}), null)); //$NON-NLS-1$
			}			
		}
		if (status.isOK()) {
			for (int i = 0; i < variables.length; i++) {
				IValueVariable variable = variables[i];
				fValueVariables.put(variable.getName(), variable);
			}
			IValueVariable[] copy = new IValueVariable[variables.length];
			System.arraycopy(variables, 0, copy, 0, variables.length);
			getNotifier().notify(copy, ADDED);
			return;
		}
		throw new CoreException(status);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#removeVariables(org.eclipse.debug.internal.core.stringsubstitution.IValueVariable[])
	 */
	public void removeVariables(IValueVariable[] variables) {
		List removed = new ArrayList(variables.length);
		for (int i = 0; i < variables.length; i++) {
			IValueVariable variable = variables[i];
			if (fValueVariables.remove(variable.getName()) != null) {
				removed.add(variable);
			}
		}
		if (removed.size() > 0) {
			getNotifier().notify((IValueVariable[])removed.toArray(new IValueVariable[removed.size()]), REMOVED);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getContextVariable(java.lang.String)
	 */
	public IContextVariable getContextVariable(String name) {
		return (IContextVariable) fContextVariables.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getValueVariable(java.lang.String)
	 */
	public IValueVariable getValueVariable(String name) {
		return (IValueVariable) fValueVariables.get(name);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#addValueVariableListener(org.eclipse.debug.internal.core.stringsubstitution.IValueVariableListener)
	 */
	public void addValueVariableListener(IValueVariableListener listener) {
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#removeValueVariableListener(org.eclipse.debug.internal.core.stringsubstitution.IValueVariableListener)
	 */
	public void removeValueVariableListener(IValueVariableListener listener) {
		fListeners.remove(listener);
	}
	
	/**
	 * Returns a memento representing the value variables currently registered.
	 * 
	 * @return memento representing the value variables currently registered
	 * @throws IOException if an I/O exception occurs while creating the XML.
	 */
	private String getValueVariablesAsXML() throws IOException {
		IValueVariable[] variables = getValueVariables();

		Document document= new DocumentImpl();
		Element rootElement= document.createElement(VALUE_VARIABLES_TAG);
		document.appendChild(rootElement);
		for (int i = 0; i < variables.length; i++) {
			ValueVariable variable = (ValueVariable)variables[i];
			Element element= document.createElement(VALUE_VARIABLE_TAG);
			element.setAttribute(NAME_TAG, variable.getName());
			element.setAttribute(VALUE_TAG, variable.getValue());
			element.setAttribute(DESCRIPTION_TAG, variable.getDescription());
			element.setAttribute(INITIALIZED_TAG, variable.isInitialized() ? TRUE_VALUE : FALSE_VALUE);
			rootElement.appendChild(element);
		}
		return LaunchManager.serializeDocument(document);
	}	
	
	/**
	 * Saves the value variables currently registered in the
	 * preference store. 
	 */
	private void storeValueVariables() {
		Preferences prefs= DebugPlugin.getDefault().getPluginPreferences();
		String variableString= ""; //$NON-NLS-1$
		if (!fValueVariables.isEmpty()) {
			try {
				variableString= getValueVariablesAsXML();
			} catch (IOException exception) {
				DebugPlugin.log(new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while storing launch configuration variables.", exception)); //$NON-NLS-1$
				return;
			}
		}
		prefs.setValue(PREF_VALUE_VARIABLES, variableString);
		DebugPlugin.getDefault().savePluginPreferences();
	}

	/**
	 * Fire a change notificaiton for the given variable.
	 * 
	 * @param variable the variable that has changed
	 */
	protected void notifyChanged(ValueVariable variable) {
		getNotifier().notify(new IValueVariable[]{variable}, CHANGED);
	}	

}
