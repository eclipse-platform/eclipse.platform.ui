/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.variables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableListener;
import org.eclipse.core.variables.VariablesPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Singleton string variable manager. 
 */
public class StringVariableManager implements IStringVariableManager, IPropertyChangeListener {
	
	/**
	 * Dynamic variables - maps variable names to variables.
	 */
	private Map fDynamicVariables;
	
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
	
	// true during internal updates indicates that change notification
	// should be suppressed/ignored.
	private boolean fInternalChange = false;
	
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
	private static final String PREF_VALUE_VARIABLES= VariablesPlugin.getUniqueIdentifier() + ".valueVariables"; //$NON-NLS-1$	
		
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
			IStatus status = new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), VariablesPlugin.INTERNAL_ERROR, "An exception occurred during string variable change notification", exception); //$NON-NLS-1$
			VariablesPlugin.log(status);
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
	}	

	/**
	 * Load contributed variables and persisted variables
	 */
	private synchronized void initialize() {
		if (fDynamicVariables == null) {
			fInternalChange = true;
			fDynamicVariables = new HashMap(5);
			fValueVariables = new HashMap(5);
			loadPersistedValueVariables();
			loadContributedValueVariables();
			loadDynamicVariables();
			VariablesPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(this);
			fInternalChange = false;
		}
	}
	
	/**
	 * Loads contributed dynamic variables
	 */
	private void loadDynamicVariables() {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(VariablesPlugin.PI_CORE_VARIABLES, EXTENSION_POINT_DYNAMIC_VARIABLES);
		IConfigurationElement elements[]= point.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String name= element.getAttribute(ATTR_NAME);
			if (name == null) {
				VariablesPlugin.logMessage(MessageFormat.format("Variable extension missing required 'name' attribute: {0}", new String[] {element.getDeclaringExtension().getLabel()}), null); //$NON-NLS-1$
				continue;
			}
			String description= element.getAttribute(ATTR_DESCRIPTION);
			DynamicVariable variable= new DynamicVariable(name, description, element);
			fDynamicVariables.put(variable.getName(), variable);
		}
	}

	/**
	 * Loads any persisted value varialbes from the preference store.
	 */
	private void loadPersistedValueVariables() {
		String variablesString= VariablesPlugin.getDefault().getPluginPreferences().getString(PREF_VALUE_VARIABLES);
		if (variablesString.length() == 0) {
			return;
		}
		Element root= null;
		Throwable ex = null;
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(variablesString.getBytes("UTF-8")); //$NON-NLS-1$
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(stream).getDocumentElement();
		} catch (UnsupportedEncodingException e) {
			ex = e;
		} catch (ParserConfigurationException e) {
			ex = e;
		} catch (FactoryConfigurationError e) {
			ex = e;
		} catch (SAXException e) {
			ex = e;
		} catch (IOException e) {
			ex = e;
		}
		if (ex != null) {
			VariablesPlugin.logMessage("An exception occurred while loading persisted value variables.", ex); //$NON-NLS-1$
			return;
		}
		if (!root.getNodeName().equals(VALUE_VARIABLES_TAG)) {
			VariablesPlugin.logMessage("Invalid format encountered while loading persisted value variables.", null); //$NON-NLS-1$
			return;
		}
		NodeList list= root.getChildNodes();
		for (int i= 0, numItems= list.getLength(); i < numItems; i++) {
			Node node= list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (!element.getNodeName().equals(VALUE_VARIABLE_TAG)) {
					VariablesPlugin.logMessage(MessageFormat.format("Invalid XML element encountered while loading value variables: {0}", new String[] {node.getNodeName()}), null); //$NON-NLS-1$
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
					VariablesPlugin.logMessage("Invalid variable entry encountered while loading value variables. Variable name is null.", null); //$NON-NLS-1$
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
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(VariablesPlugin.PI_CORE_VARIABLES, EXTENSION_POINT_VALUE_VARIABLES);
		IConfigurationElement elements[]= point.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String name= element.getAttribute(ATTR_NAME);
			if (name == null) {
				VariablesPlugin.logMessage(MessageFormat.format("Variable extension missing required 'name' attribute: {0}", new String[] {element.getDeclaringExtension().getLabel()}), null); //$NON-NLS-1$
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
	public synchronized IStringVariable[] getVariables() {
		initialize();
		List list = new ArrayList(fDynamicVariables.size() + fValueVariables.size());
		list.addAll(fDynamicVariables.values());
		list.addAll(fValueVariables.values());
		return (IStringVariable[]) list.toArray(new IStringVariable[list.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getValueVariables()
	 */
	public synchronized IValueVariable[] getValueVariables() {
		initialize();
		return (IValueVariable[]) fValueVariables.values().toArray(new IValueVariable[fValueVariables.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getDynamicVariables()
	 */
	public synchronized IDynamicVariable[] getDynamicVariables() {
		initialize();
		return (IDynamicVariable[]) fDynamicVariables.values().toArray(new IDynamicVariable[fDynamicVariables.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#performStringSubstitution(java.lang.String)
	 */
	public String performStringSubstitution(String expression) throws CoreException {
		return performStringSubstitution(expression, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#newValueVariable(java.lang.String, java.lang.String)
	 */
	public IValueVariable newValueVariable(String name, String description) {
		IConfigurationElement element = null;
		ValueVariable existing = (ValueVariable)getValueVariable(name);
		if (existing != null && existing.isContributed()) {
			element = existing.getConfigurationElement();
		}
		return new ValueVariable(name, description, element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#addVariables(org.eclipse.debug.internal.core.stringsubstitution.IValueVariable[])
	 */
	public synchronized void addVariables(IValueVariable[] variables) throws CoreException {
		initialize();
		MultiStatus status = new MultiStatus(VariablesPlugin.getUniqueIdentifier(), VariablesPlugin.INTERNAL_ERROR, VariablesMessages.StringVariableManager_26, null); 
		for (int i = 0; i < variables.length; i++) {
			IValueVariable variable = variables[i];
			if (getValueVariable(variable.getName()) != null) {
				status.add(new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), VariablesPlugin.INTERNAL_ERROR, MessageFormat.format(VariablesMessages.StringVariableManager_27, new String[]{variable.getName()}), null)); 
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
	public synchronized void removeVariables(IValueVariable[] variables) {
		initialize();
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
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getDynamicVariable(java.lang.String)
	 */
	public synchronized IDynamicVariable getDynamicVariable(String name) {
		initialize();
		return (IDynamicVariable) fDynamicVariables.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#getValueVariable(java.lang.String)
	 */
	public synchronized IValueVariable getValueVariable(String name) {
		initialize();
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
	private String getValueVariablesAsXML() throws IOException, ParserConfigurationException, TransformerException {
		IValueVariable[] variables = getValueVariables();

		Document document= getDocument();
		Element rootElement= document.createElement(VALUE_VARIABLES_TAG);
		document.appendChild(rootElement);
		for (int i = 0; i < variables.length; i++) {
			ValueVariable variable = (ValueVariable)variables[i];
			Element element= document.createElement(VALUE_VARIABLE_TAG);
			element.setAttribute(NAME_TAG, variable.getName());
			String value= variable.getValue();
			if (value != null) {
				element.setAttribute(VALUE_TAG, value);
			}
			String description= variable.getDescription();
			if (description != null) {
				element.setAttribute(DESCRIPTION_TAG, description);
			}
			element.setAttribute(INITIALIZED_TAG, variable.isInitialized() ? TRUE_VALUE : FALSE_VALUE);
			rootElement.appendChild(element);
		}
		return serializeDocument(document);
	}
	
	private Document getDocument() throws ParserConfigurationException {
		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
		Document doc =docBuilder.newDocument();
		return doc;
	}
	
	/**
	 * Serializes a XML document into a string - encoded in UTF8 format,
	 * with platform line separators.
	 * 
	 * @param doc document to serialize
	 * @return the document as a string
	 * @throws TransformerException if an unrecoverable error occurs during the serialization
	 * @throws IOException if the encoding attempted to be used is not supported
	 */
	private String serializeDocument(Document doc) throws TransformerException, UnsupportedEncodingException {
		ByteArrayOutputStream s= new ByteArrayOutputStream();
		
		TransformerFactory factory= TransformerFactory.newInstance();
		Transformer transformer= factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		
		DOMSource source= new DOMSource(doc);
		StreamResult outputTarget= new StreamResult(s);
		transformer.transform(source, outputTarget);
		
		return s.toString("UTF8"); //$NON-NLS-1$			
	}
	
	/**
	 * Saves the value variables currently registered in the
	 * preference store. 
	 */
	private synchronized void storeValueVariables() {
		Preferences prefs= VariablesPlugin.getDefault().getPluginPreferences();
		String variableString= ""; //$NON-NLS-1$
		if (!fValueVariables.isEmpty()) {
			try {
				variableString= getValueVariablesAsXML();
			} catch (IOException e) {
				VariablesPlugin.log(new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while storing launch configuration variables.", e)); //$NON-NLS-1$
				return;
			} catch (ParserConfigurationException e) {
				VariablesPlugin.log(new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while storing launch configuration variables.", e)); //$NON-NLS-1$
				return;
			} catch (TransformerException e) {
				VariablesPlugin.log(new Status(IStatus.ERROR, VariablesPlugin.getUniqueIdentifier(), IStatus.ERROR, "An exception occurred while storing launch configuration variables.", e)); //$NON-NLS-1$
				return;
			}
		}
		fInternalChange = true;
		prefs.setValue(PREF_VALUE_VARIABLES, variableString);
		VariablesPlugin.getDefault().savePluginPreferences();
		fInternalChange = false;
	}

	/**
	 * Fire a change notification for the given variable.
	 * 
	 * @param variable the variable that has changed
	 */
	protected void notifyChanged(ValueVariable variable) {
		if (!fInternalChange) {
			IValueVariable existing = getValueVariable(variable.getName());
			if (variable.equals(existing)) {
				// do not do change notification for unregistered variables
				getNotifier().notify(new IValueVariable[]{variable}, CHANGED);
			}
		}
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#generateVariableExpression(java.lang.String, java.lang.String)
	 */
	public String generateVariableExpression(String varName, String arg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("${"); //$NON-NLS-1$
		buffer.append(varName);
		if (arg != null) {
			buffer.append(":"); //$NON-NLS-1$
			buffer.append(arg);
		}
		buffer.append("}"); //$NON-NLS-1$
		return buffer.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.stringsubstitution.IStringVariableManager#performStringSubstitution(java.lang.String, boolean)
	 */
	public String performStringSubstitution(String expression,	boolean reportUndefinedVariables) throws CoreException {
		return new StringSubstitutionEngine().performStringSubstitution(expression, reportUndefinedVariables, true, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.variables.IStringVariableManager#validateStringVariables(java.lang.String)
	 */
	public void validateStringVariables(String expression) throws CoreException {
		new StringSubstitutionEngine().validateStringVariables(expression, this);
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.variables.IStringVariableManager#getContributingPluginId(org.eclipse.core.variables.IStringVariable)
     */
    public String getContributingPluginId(IStringVariable variable) {
        if (variable instanceof StringVariable) {
            return ((StringVariable) variable).getConfigurationElement().getNamespace();
        }
        return null;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if (PREF_VALUE_VARIABLES.equals(event.getProperty())) {
			synchronized (this) {
				if (!fInternalChange) {
					fValueVariables.clear();
					loadPersistedValueVariables();
					loadContributedValueVariables();
				}
			}
		}
	}
}
