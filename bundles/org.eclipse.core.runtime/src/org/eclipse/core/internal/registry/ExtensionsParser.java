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

package org.eclipse.core.internal.registry;

import java.io.IOException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.osgi.util.tracker.ServiceTracker;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/*
 * This class is NOT thread safe. Clients must ensure at most one thread
 * operates on instances of this class at a time. 
 */
public class ExtensionsParser extends DefaultHandler {
	// Introduced for backward compatibility
	private final static String NO_EXTENSION_MUNGING = "eclipse.noExtensionMunging"; //$NON-NLS-1$ //System property
	private static Map extensionPointMap;
	private static ServiceTracker parserFactoryTracker;
	static {
		initializeExtensionPointMap();
	}

	/**
	 * Initialize the list of renamed extension point ids
	 */
	private static void initializeExtensionPointMap() {
		Map map = new HashMap(13);
		// TODO should this be hard coded? can we use a properties file?
		map.put("org.eclipse.ui.markerImageProvider", "org.eclipse.ui.ide.markerImageProvider"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerHelp", "org.eclipse.ui.ide.markerHelp"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerImageProviders", "org.eclipse.ui.ide.markerImageProviders"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerResolution", "org.eclipse.ui.ide.markerResolution"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.projectNatureImages", "org.eclipse.ui.ide.projectNatureImages"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.resourceFilters", "org.eclipse.ui.ide.resourceFilters"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.markerUpdaters", "org.eclipse.ui.editors.markerUpdaters"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.documentProviders", "org.eclipse.ui.editors.documentProviders"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.ui.workbench.texteditor.markerAnnotationSpecification", "org.eclipse.ui.editors.markerAnnotationSpecification"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.browser", "org.eclipse.help.base.browser"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.luceneAnalyzer", "org.eclipse.help.base.luceneAnalyzer"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.webapp", "org.eclipse.help.base.webapp"); //$NON-NLS-1$ //$NON-NLS-2$
		map.put("org.eclipse.help.support", "org.eclipse.ui.helpSupport"); //$NON-NLS-1$ //$NON-NLS-2$
		extensionPointMap = map;
	}

	private static long cumulativeTime = 0;

	// is in compatibility mode
	private boolean compatibilityMode;

	// File name for this extension manifest
	// This to help with error reporting
	private String locationName = null;

	// Current State Information
	private Stack stateStack = new Stack();

	// Current object stack (used to hold the current object we are
	// populating in this plugin descriptor
	private Stack objectStack = new Stack();

	private String schemaVersion = null;

	// A status for holding results.
	private MultiStatus status;

	private ResourceBundle resources;

	/** 
	 * Status code constant (value 1) indicating a problem in a bundle extensions
	 * manifest (<code>extensions.xml</code>) file.
	 */
	public static final int PARSE_PROBLEM = 1;

	public static final String PLUGIN = "plugin"; //$NON-NLS-1$
	public static final String PLUGIN_ID = "id"; //$NON-NLS-1$
	public static final String PLUGIN_NAME = "name"; //$NON-NLS-1$
	public static final String FRAGMENT = "fragment"; //$NON-NLS-1$	
	public static final String BUNDLE_UID = "id"; //$NON-NLS-1$

	public static final String EXTENSION_POINT = "extension-point"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_NAME = "name"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_ID = "id"; //$NON-NLS-1$
	public static final String EXTENSION_POINT_SCHEMA = "schema"; //$NON-NLS-1$

	public static final String EXTENSION = "extension"; //$NON-NLS-1$
	public static final String EXTENSION_NAME = "name"; //$NON-NLS-1$
	public static final String EXTENSION_ID = "id"; //$NON-NLS-1$
	public static final String EXTENSION_TARGET = "point"; //$NON-NLS-1$

	public static final String ELEMENT = "element"; //$NON-NLS-1$
	public static final String ELEMENT_NAME = "name"; //$NON-NLS-1$
	public static final String ELEMENT_VALUE = "value"; //$NON-NLS-1$

	public static final String PROPERTY = "property"; //$NON-NLS-1$
	public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$
	public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

	// Valid States
	private static final int IGNORED_ELEMENT_STATE = 0;
	private static final int INITIAL_STATE = 1;
	private static final int BUNDLE_STATE = 2;
	private static final int BUNDLE_EXTENSION_POINT_STATE = 5;
	private static final int BUNDLE_EXTENSION_STATE = 6;
	private static final int CONFIGURATION_ELEMENT_STATE = 10;

	// Keep a group of vectors as a temporary scratch space.  These
	// vectors will be used to populate arrays in the bundle model
	// once processing of the XML file is complete.
	private static final int EXTENSION_POINT_INDEX = 0;
	private static final int EXTENSION_INDEX = 1;
	private static final int LAST_INDEX = 1;

	private ArrayList scratchVectors[] = new ArrayList[LAST_INDEX + 1];

	private String manifestType;

	private Locator locator = null;

	public ExtensionsParser() {
		parserFactoryTracker = new ServiceTracker(InternalPlatform.getDefault().getBundleContext(), SAXParserFactory.class.getName(), null);
		parserFactoryTracker.open();
	}

	/**
	 * @see ContentHandler#setDocumentLocator
	 */
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	public void characters(char[] ch, int start, int length) {
		int state = ((Integer) stateStack.peek()).intValue();
		if (state != CONFIGURATION_ELEMENT_STATE)
			return;
		if (state == CONFIGURATION_ELEMENT_STATE) {
			// Accept character data within an element, is when it is
			// part of a configuration element (i.e. an element within an EXTENSION element
			ConfigurationElement currentConfigElement = (ConfigurationElement) objectStack.peek();
			String value = new String(ch, start, length);
			String oldValue = currentConfigElement.getValueAsIs();
			if (oldValue == null) {
				if (value.trim().length() != 0)
					currentConfigElement.setValue(translate(value));
			} else {
				currentConfigElement.setValue(oldValue + value);
			}
		}
	}

	public void endDocument() {
		// do nothing
	}

	public void endElement(String uri, String elementName, String qName) {
		switch (((Integer) stateStack.peek()).intValue()) {
			case IGNORED_ELEMENT_STATE :
				stateStack.pop();
				break;
			case INITIAL_STATE :
				// shouldn't get here
				internalError(Policy.bind("parse.internalStack", elementName)); //$NON-NLS-1$
				break;
			case BUNDLE_STATE :
				if (elementName.equals(manifestType)) {
					stateStack.pop();
					Namespace root = (Namespace) objectStack.peek();

					// Put the extension points into this bundle model
					ArrayList extensionPoints = scratchVectors[EXTENSION_POINT_INDEX];
					if (extensionPoints.size() > 0) {
						root.setExtensionPoints((ExtensionPoint[]) extensionPoints.toArray(new ExtensionPoint[extensionPoints.size()]));
						scratchVectors[EXTENSION_POINT_INDEX].clear();
					}

					// Put the extensions into this bundle model too
					ArrayList extensions = scratchVectors[EXTENSION_INDEX];
					if (extensions.size() > 0) {
						root.setExtensions(fixRenamedExtensionPoints((Extension[]) extensions.toArray(new Extension[extensions.size()])));
						scratchVectors[EXTENSION_INDEX].clear();
					}
				}
				break;
			case BUNDLE_EXTENSION_POINT_STATE :
				if (elementName.equals(EXTENSION_POINT)) {
					stateStack.pop();
				}
				break;
			case BUNDLE_EXTENSION_STATE :
				if (elementName.equals(EXTENSION)) {
					stateStack.pop();
					// Finish up extension object
					Extension currentExtension = (Extension) objectStack.pop();
					Namespace parent = (Namespace) objectStack.peek();
					currentExtension.setParent(parent);
					scratchVectors[EXTENSION_INDEX].add(currentExtension);
				}
				break;
			case CONFIGURATION_ELEMENT_STATE :
				// We don't care what the element name was
				stateStack.pop();
				// Now finish up the configuration element object
				ConfigurationElement currentConfigElement = (ConfigurationElement) objectStack.pop();

				String value = currentConfigElement.getValueAsIs();
				if (value != null) {
					currentConfigElement.setValue(value.trim());
				}

				Object parent = objectStack.peek();
				currentConfigElement.setParent((RegistryModelObject) parent);
				if (((Integer) stateStack.peek()).intValue() == BUNDLE_EXTENSION_STATE) {
					// Want to add this configuration element to the subelements of an extension
					IConfigurationElement[] oldValues = ((Extension) parent).getConfigurationElements();
					int size = (oldValues == null) ? 0 : oldValues.length;
					IConfigurationElement[] newValues = new IConfigurationElement[size + 1];
					for (int i = 0; i < size; i++) {
						newValues[i] = oldValues[i];
					}
					newValues[size] = currentConfigElement;
					((Extension) parent).setSubElements(newValues);
				} else {
					IConfigurationElement[] oldValues = ((ConfigurationElement) parent).getChildren();
					int size = (oldValues == null) ? 0 : oldValues.length;
					IConfigurationElement[] newValues = new IConfigurationElement[size + 1];
					for (int i = 0; i < size; i++) {
						newValues[i] = oldValues[i];
					}
					newValues[size] = currentConfigElement;
					((ConfigurationElement) parent).setChildren(newValues);
				}
				break;
		}
	}

	public void error(SAXParseException ex) {
		logStatus(ex);
	}

	public void fatalError(SAXParseException ex) throws SAXException {
		logStatus(ex);
		throw ex;
	}

	private void handleExtensionPointState(String elementName) {
		// We ignore all elements under extension points (if there are any)
		stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
		unknownElement(EXTENSION_POINT, elementName);
	}

	private void handleExtensionState(String elementName, Attributes attributes) {
		// You need to change the state here even though we will be executing the same
		// code for ExtensionState and ConfigurationElementState.  We ignore the name
		// of the element for ConfigurationElements.  When we are wrapping up, we will
		// want to add each configuration element object to the subElements vector of
		// its parent configuration element object.  However, the first configuration
		// element object we created (the last one we pop off the stack) will need to
		// be added to a vector in the extension object called _configuration.
		stateStack.push(new Integer(CONFIGURATION_ELEMENT_STATE));

		// create a new Configuration Element and push it onto the object stack
		ConfigurationElement currentConfigurationElement = new ConfigurationElement();
		objectStack.push(currentConfigurationElement);
		currentConfigurationElement.setName(elementName);

		// Processing the attributes of a configuration element involves creating
		// a new configuration property for each attribute and populating the configuration
		// property with the name/value pair of the attribute.  Note there will be one
		// configuration property for each attribute
		parseConfigurationElementAttributes(attributes);
	}

	private void handleInitialState(String elementName, Attributes attributes) {
		if (!elementName.equals(manifestType)) {
			stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
			internalError(Policy.bind("parse.unknownTopElement", elementName)); //$NON-NLS-1$
			return;
		}
		// new manifests should have the plugin (not fragment) element empty
		// in compatibility mode, any extraneous elements will be silently ignored
		compatibilityMode = !(elementName.equals(PLUGIN) && attributes.getLength() == 0);
		stateStack.push(new Integer(BUNDLE_STATE));
		Namespace current = new Namespace();
		objectStack.push(current);
	}

	/**
	 * convert a list of comma-separated tokens into an array
	 */
	protected static String[] getArrayFromList(String line) {
		if (line == null || line.trim().length() == 0)
			return null;
		Vector list = new Vector();
		StringTokenizer tokens = new StringTokenizer(line, ","); //$NON-NLS-1$
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (token.length() != 0)
				list.addElement(token);
		}
		return list.isEmpty() ? null : (String[]) list.toArray(new String[0]);
	}

	private void handleBundleState(String elementName, Attributes attributes) {
		if (elementName.equals(EXTENSION_POINT)) {
			stateStack.push(new Integer(BUNDLE_EXTENSION_POINT_STATE));
			parseExtensionPointAttributes(attributes);
			return;
		}
		if (elementName.equals(EXTENSION)) {
			stateStack.push(new Integer(BUNDLE_EXTENSION_STATE));
			parseExtensionAttributes(attributes);
			return;
		}

		// If we get to this point, the element name is one we don't currently accept.
		// Set the state to indicate that this element will be ignored
		stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
		if (!compatibilityMode)
			unknownElement(manifestType, elementName);
	}

	private void logStatus(SAXParseException ex) {
		String name = ex.getSystemId();
		if (name == null)
			name = locationName;
		if (name == null)
			name = ""; //$NON-NLS-1$
		else
			name = name.substring(1 + name.lastIndexOf("/")); //$NON-NLS-1$

		String msg;
		if (name.equals("")) //$NON-NLS-1$
			msg = Policy.bind("parse.error", ex.getMessage()); //$NON-NLS-1$
		else
			msg = Policy.bind("parse.errorNameLineColumn", //$NON-NLS-1$
					new String[] {name, Integer.toString(ex.getLineNumber()), Integer.toString(ex.getColumnNumber()), ex.getMessage()});
		error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, PARSE_PROBLEM, msg, ex));
	}

	public Namespace parseManifest(MultiStatus status, InputSource in, String manifestType, String manifestName, ResourceBundle bundle) throws ParserConfigurationException, SAXException, IOException {
		this.status = status;
		long start = 0;
		this.resources = bundle;
		if (InternalPlatform.DEBUG)
			start = System.currentTimeMillis();

		SAXParserFactory factory = (SAXParserFactory) parserFactoryTracker.getService();

		if (factory == null)
			throw new SAXException(Policy.bind("parse.xmlParserNotAvailable")); //$NON-NLS-1$

		try {
			if (manifestType == null)
				throw new NullPointerException();
			if (!(manifestType.equals(PLUGIN) || manifestType.equals(FRAGMENT)))
				throw new IllegalArgumentException("Invalid manifest type: " + manifestType); //$NON-NLS-1$
			this.manifestType = manifestType;
			locationName = in.getSystemId();
			if (locationName == null)
				locationName = manifestName;
			factory.setNamespaceAware(true);
			try {
				factory.setFeature("http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
			} catch (SAXException se) {
				// ignore; we can still operate without string-interning
			}
			factory.setValidating(false);
			factory.newSAXParser().parse(in, this);
			return (Namespace) objectStack.pop();
		} finally {
			if (InternalPlatform.DEBUG) {
				cumulativeTime = cumulativeTime + (System.currentTimeMillis() - start);
				InternalPlatform.getDefault().setOption("org.eclipse.core.runtime/registry/parsing/timing/value", Long.toString(cumulativeTime)); //$NON-NLS-1$
			}
		}
	}

	private void parseConfigurationElementAttributes(Attributes attributes) {
		ConfigurationElement parentConfigurationElement = (ConfigurationElement) objectStack.peek();
		Vector propVector = null;

		// process attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		if (len == 0)
			return;
		propVector = new Vector();
		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i);

			ConfigurationProperty currentConfigurationProperty = new ConfigurationProperty();
			currentConfigurationProperty.setName(attrName);
			currentConfigurationProperty.setValue(translate(attrValue));
			propVector.addElement(currentConfigurationProperty);
		}
		parentConfigurationElement.setProperties((ConfigurationProperty[]) propVector.toArray(new ConfigurationProperty[propVector.size()]));
		propVector = null;
	}

	private void parseExtensionAttributes(Attributes attributes) {
		Namespace parent = (Namespace) objectStack.peek();
		Extension currentExtension = new Extension();
		objectStack.push(currentExtension);

		// Process Attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i).trim();

			if (attrName.equals(EXTENSION_NAME))
				currentExtension.setName(translate(attrValue));
			else if (attrName.equals(EXTENSION_ID))
				currentExtension.setSimpleIdentifier(attrValue);
			else if (attrName.equals(EXTENSION_TARGET)) {
				// check if point is specified as a simple or qualified name
				String targetName;
				if (attrValue.lastIndexOf('.') == -1) {
					String baseId = parent.getName();
					targetName = baseId + "." + attrValue; //$NON-NLS-1$
				} else
					targetName = attrValue;
				currentExtension.setExtensionPointIdentifier(targetName);
			} else
				unknownAttribute(EXTENSION, attrName); //$NON-NLS-1$
		}
		if (currentExtension.getExtensionPointUniqueIdentifier() == null) {
			missingAttribute(EXTENSION_TARGET, EXTENSION);
			stateStack.pop();
			stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
			objectStack.pop();
			return;
		}

	}

	private void missingAttribute(String attribute, String element) {
		if (locator == null)
			internalError(Policy.bind("parse.missingAttribute", new String[] {attribute, element})); //$NON-NLS-1$
		else
			internalError(Policy.bind("parse.missingAttributeLine", new String[] {attribute, element, Integer.toString(locator.getLineNumber())})); //$NON-NLS-1$
	}

	private void unknownAttribute(String attribute, String element) {
		if (locator == null)
			internalError(Policy.bind("parse.unknownAttribute", new String[] {attribute, element})); //$NON-NLS-1$
		else
			internalError(Policy.bind("parse.unknownAttributeLine", new String[] {attribute, element, Integer.toString(locator.getLineNumber())})); //$NON-NLS-1$
	}

	private void unknownElement(String element, String parent) {
		if (locator == null)
			internalError(Policy.bind("parse.unknownAttribute", new String[] {parent, element})); //$NON-NLS-1$
		else
			internalError(Policy.bind("parse.unknownAttributeLine", new String[] {parent, element, Integer.toString(locator.getLineNumber())})); //$NON-NLS-1$
	}

	private void parseExtensionPointAttributes(Attributes attributes) {
		ExtensionPoint currentExtPoint = new ExtensionPoint();

		// Process Attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i).trim();

			if (attrName.equals(EXTENSION_POINT_NAME))
				currentExtPoint.setName(translate(attrValue));
			else if (attrName.equals(EXTENSION_POINT_ID))
				currentExtPoint.setSimpleIdentifier(attrValue);
			else if (attrName.equals(EXTENSION_POINT_SCHEMA))
				currentExtPoint.setSchema(attrValue);
			else
				unknownAttribute(EXTENSION_POINT, attrName); //$NON-NLS-1$
		}
		if (currentExtPoint.getSimpleIdentifier() == null || currentExtPoint.getName() == null) {
			String attribute = currentExtPoint.getSimpleIdentifier() == null ? EXTENSION_POINT_ID : EXTENSION_POINT_NAME;
			missingAttribute(attribute, EXTENSION_POINT);
			stateStack.pop();
			stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
			return;
		}

		// currentExtPoint contains a pointer to the parent bundle model.
		Namespace root = (Namespace) objectStack.peek();
		currentExtPoint.setParent(root);

		// Now populate the the vector just below us on the objectStack with this extension point
		scratchVectors[EXTENSION_POINT_INDEX].add(currentExtPoint);
	}

	public void startDocument() {
		stateStack.push(new Integer(INITIAL_STATE));
		for (int i = 0; i <= LAST_INDEX; i++) {
			scratchVectors[i] = new ArrayList();
		}
	}

	public void startElement(String uri, String elementName, String qName, Attributes attributes) {
		switch (((Integer) stateStack.peek()).intValue()) {
			case INITIAL_STATE :
				handleInitialState(elementName, attributes);
				break;
			case BUNDLE_STATE :
				handleBundleState(elementName, attributes);
				break;
			case BUNDLE_EXTENSION_POINT_STATE :
				handleExtensionPointState(elementName);
				break;
			case BUNDLE_EXTENSION_STATE :
			case CONFIGURATION_ELEMENT_STATE :
				handleExtensionState(elementName, attributes);
				break;
			default :
				stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
				if (!compatibilityMode)
					internalError(Policy.bind("parse.unknownTopElement", elementName)); //$NON-NLS-1$
		}
	}

	public void warning(SAXParseException ex) {
		logStatus(ex);
	}

	private void internalError(String message) {
		error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, PARSE_PROBLEM, message, null));
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#processingInstruction
	 * @since 3.0
	 */
	public void processingInstruction(String target, String data) throws SAXException {
		// Since 3.0, a processing instruction of the form <?eclipse version="3.0"?> at
		// the start of the manifest file is used to indicate the plug-in manifest
		// schema version in effect. Pre-3.0 (i.e., 2.1) plug-in manifest files do not
		// have one of these, and this is how we can distinguish the manifest of a
		// pre-3.0 plug-in from a post-3.0 one (for compatibility tranformations).
		if (target.equalsIgnoreCase("eclipse")) { //$NON-NLS-1$
			// just the presence of this processing instruction indicates that this
			// plug-in is at least 3.0
			schemaVersion = "3.0"; //$NON-NLS-1$
			StringTokenizer tokenizer = new StringTokenizer(data, "=\""); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.equalsIgnoreCase("version")) { //$NON-NLS-1$
					if (!tokenizer.hasMoreTokens()) {
						break;
					}
					schemaVersion = tokenizer.nextToken();
					break;
				}
			}
		}
	}

	/**
	 * Handles an error state specified by the status.  The collection of all logged status
	 * objects can be accessed using <code>getStatus()</code>.
	 *
	 * @param error a status detailing the error condition
	 */
	public void error(IStatus error) {
		status.add(error);
	}

	private String translate(String key) {
		return ResourceTranslator.getResourceString(null, key, resources);
	}

	/**
	 * Fixes up the extension declarations in the given pre-3.0 plug-in or fragment to compensate
	 * for extension points that were renamed between release 2.1 and 3.0.
	 */
	private Extension[] fixRenamedExtensionPoints(Extension[] extensions) {
		if (extensions == null || (schemaVersion != null && schemaVersion.equals("3.0")) || System.getProperties().get(NO_EXTENSION_MUNGING) != null) //$NON-NLS-1$
			return extensions;
		for (int i = 0; i < extensions.length; i++) {
			Extension extension = extensions[i];
			String oldPointId = extension.getExtensionPointIdentifier();
			String newPointId = (String) extensionPointMap.get(oldPointId);
			if (newPointId != null) {
				extension.setExtensionPointIdentifier(newPointId);
			}
		}
		return extensions;
	}
}