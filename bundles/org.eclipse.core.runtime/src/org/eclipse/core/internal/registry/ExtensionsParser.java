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

package org.eclipse.core.internal.registry;

import java.io.IOException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.util.tracker.ServiceTracker;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class ExtensionsParser extends DefaultHandler {
	// Introduced for backward compatibility
	private final static String NO_EXTENSION_MUNGING = "eclipse.noExtensionMunging"; //$NON-NLS-1$ //System property
	private static Map extensionPointMap;

	static {
		initializeExtensionPointMap();
	}

	/**
	 * Initialize the list of renamed extension point ids
	 */
	private static void initializeExtensionPointMap() {
		Map map = new HashMap(13);
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

	// Resource bundle used to translate the content of the plugin.xml
	private ResourceBundle resources;

	// Keep track of the object encountered.
	private RegistryObjectManager objectManager;

	private Contribution namespace;

	//This keeps tracks of the value of the configuration element in case the value comes in several pieces (see characters()). See as well bug 75592. 
	private String configurationElementValue;

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

	public ExtensionsParser(MultiStatus status) {
		super();
		this.status = status;
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
			if (configurationElementValue == null) {
				if (value.trim().length() != 0) {
					configurationElementValue = value;
				}
			} else {
				configurationElementValue = configurationElementValue + value;
			}
			if (configurationElementValue != null)
				currentConfigElement.setValue(translate(configurationElementValue));
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
				internalError(NLS.bind(Messages.parse_internalStack, elementName));
				break;
			case BUNDLE_STATE :
				if (elementName.equals(manifestType)) {
					stateStack.pop();

					ArrayList extensionPoints = scratchVectors[EXTENSION_POINT_INDEX];
					ArrayList extensions = scratchVectors[EXTENSION_INDEX];
					int[] namespaceChildren = new int[2 + extensionPoints.size() + extensions.size()];
					int position = 2;
					// Put the extension points into this namespace
					if (extensionPoints.size() > 0) {
						namespaceChildren[Contribution.EXTENSION_POINT] = extensionPoints.size();
						for (Iterator iter = extensionPoints.iterator(); iter.hasNext();) {
							namespaceChildren[position++] = ((RegistryObject) iter.next()).getObjectId();
						}
						extensionPoints.clear();
					}

					// Put the extensions into this namespace too
					if (extensions.size() > 0) {
						Extension[] renamedExtensions = fixRenamedExtensionPoints((Extension[]) extensions.toArray(new Extension[extensions.size()]));
						namespaceChildren[Contribution.EXTENSION] = renamedExtensions.length;
						for (int i = 0; i < renamedExtensions.length; i++) {
							namespaceChildren[position++] = renamedExtensions[i].getObjectId();
						}
						extensions.clear();
					}
					namespace.setRawChildren(namespaceChildren);
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
					currentExtension.setNamespace(namespace.getNamespace());
					scratchVectors[EXTENSION_INDEX].add(currentExtension);
				}
				break;
			case CONFIGURATION_ELEMENT_STATE :
				// We don't care what the element name was
				stateStack.pop();
				// Now finish up the configuration element object
				configurationElementValue = null;
				ConfigurationElement currentConfigElement = (ConfigurationElement) objectStack.pop();

				String value = currentConfigElement.getValueAsIs();
				if (value != null) {
					currentConfigElement.setValue(value.trim());
				}

				RegistryObject parent = (RegistryObject) objectStack.peek();
				// Want to add this configuration element to the subelements of an extension
				int[] oldValues = parent.getRawChildren();
				int size = oldValues.length;
				int[] newValues = new int[size + 1];
				for (int i = 0; i < size; i++) {
					newValues[i] = oldValues[i];
				}
				newValues[size] = currentConfigElement.getObjectId();
				parent.setRawChildren(newValues);
				currentConfigElement.setParentId(parent.getObjectId());
				currentConfigElement.setParentType(parent instanceof ConfigurationElement ? RegistryObjectManager.CONFIGURATION_ELEMENT : RegistryObjectManager.EXTENSION);
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

		configurationElementValue = null;

		// create a new Configuration Element and push it onto the object stack
		ConfigurationElement currentConfigurationElement = new ConfigurationElement();
		currentConfigurationElement.setContributingBundle(namespace.getNamespaceBundle());
		objectStack.push(currentConfigurationElement);
		currentConfigurationElement.setName(elementName);

		// Processing the attributes of a configuration element involves creating
		// a new configuration property for each attribute and populating the configuration
		// property with the name/value pair of the attribute.  Note there will be one
		// configuration property for each attribute
		parseConfigurationElementAttributes(attributes);
		objectManager.add(currentConfigurationElement, true);
	}

	private void handleInitialState(String elementName, Attributes attributes) {
		if (!elementName.equals(manifestType)) {
			stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
			internalError(NLS.bind(Messages.parse_unknownTopElement, elementName));
			return;
		}
		// new manifests should have the plugin (or fragment) element empty
		// in compatibility mode, any extraneous elements will be silently ignored
		compatibilityMode = attributes.getLength() > 0;
		stateStack.push(new Integer(BUNDLE_STATE));
		objectStack.push(namespace);
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
			msg = NLS.bind(Messages.parse_error, ex.getMessage());
		else
			msg = NLS.bind(Messages.parse_errorNameLineColumn, (new Object[] {name, Integer.toString(ex.getLineNumber()), Integer.toString(ex.getColumnNumber()), ex.getMessage()}));
		error(new Status(IStatus.WARNING, Platform.PI_RUNTIME, PARSE_PROBLEM, msg, ex));
	}

	public Contribution parseManifest(ServiceTracker factoryTracker, InputSource in, String manifestKind, String manifestName, RegistryObjectManager registryObjects, Contribution currentNamespace, ResourceBundle bundle) throws ParserConfigurationException, SAXException, IOException {
		long start = 0;
		this.resources = bundle;
		this.objectManager = registryObjects;
		//initialize the parser with this object
		this.namespace = currentNamespace;
		if (InternalPlatform.DEBUG)
			start = System.currentTimeMillis();

		SAXParserFactory factory = (SAXParserFactory) factoryTracker.getService();

		if (factory == null)
			throw new SAXException(Messages.parse_xmlParserNotAvailable);

		try {
			if (manifestKind == null)
				throw new NullPointerException();
			if (!(manifestKind.equals(PLUGIN) || manifestKind.equals(FRAGMENT)))
				throw new IllegalArgumentException("Invalid manifest type: " + manifestType); //$NON-NLS-1$
			this.manifestType = manifestKind;
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
			return (Contribution) objectStack.pop();
		} finally {
			if (InternalPlatform.DEBUG) {
				cumulativeTime = cumulativeTime + (System.currentTimeMillis() - start);
				InternalPlatform.getDefault().setOption("org.eclipse.core.runtime/registry/parsing/timing/value", Long.toString(cumulativeTime)); //$NON-NLS-1$
			}
		}
	}

	private void parseConfigurationElementAttributes(Attributes attributes) {
		ConfigurationElement parentConfigurationElement = (ConfigurationElement) objectStack.peek();

		// process attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		if (len == 0) {
			parentConfigurationElement.setProperties(RegistryObjectManager.EMPTY_STRING_ARRAY);
			return;
		}
		String[] properties = new String[len * 2];
		for (int i = 0; i < len; i++) {
			properties[i * 2] = attributes.getLocalName(i);
			properties[i * 2 + 1] = translate(attributes.getValue(i));
		}
		parentConfigurationElement.setProperties(properties);
		properties = null;
	}

	private void parseExtensionAttributes(Attributes attributes) {
		Extension currentExtension = new Extension();
		objectStack.push(currentExtension);

		// Process Attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i).trim();

			if (attrName.equals(EXTENSION_NAME))
				currentExtension.setLabel(translate(attrValue));
			else if (attrName.equals(EXTENSION_ID))
				currentExtension.setSimpleIdentifier(attrValue);
			else if (attrName.equals(EXTENSION_TARGET)) {
				// check if point is specified as a simple or qualified name
				String targetName;
				if (attrValue.lastIndexOf('.') == -1) {
					String baseId = namespace.getNamespace();
					targetName = baseId + '.' + attrValue;
				} else
					targetName = attrValue;
				currentExtension.setExtensionPointIdentifier(targetName);
			} else
				unknownAttribute(EXTENSION, attrName);
		}
		if (currentExtension.getExtensionPointIdentifier() == null) {
			missingAttribute(EXTENSION_TARGET, EXTENSION);
			stateStack.pop();
			stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
			objectStack.pop();
			return;
		}

		objectManager.add(currentExtension, true);
	}

	//todo: Are all three methods needed??
	private void missingAttribute(String attribute, String element) {
		if (locator == null)
			internalError(NLS.bind(Messages.parse_missingAttribute, attribute, element));
		else
			internalError(NLS.bind(Messages.parse_missingAttributeLine, (new String[] {attribute, element, Integer.toString(locator.getLineNumber())})));
	}

	private void unknownAttribute(String attribute, String element) {
		if (locator == null)
			internalError(NLS.bind(Messages.parse_unknownAttribute, attribute, element));
		else
			internalError(NLS.bind(Messages.parse_unknownAttributeLine, (new String[] {attribute, element, Integer.toString(locator.getLineNumber())})));
	}

	private void unknownElement(String element, String parent) {
		if (locator == null)
			internalError(NLS.bind(Messages.parse_unknownElement, parent, element));
		else
			internalError(NLS.bind(Messages.parse_unknownElementLine, (new String[] {parent, element, Integer.toString(locator.getLineNumber())})));
	}

	private void parseExtensionPointAttributes(Attributes attributes) {
		ExtensionPoint currentExtPoint = new ExtensionPoint();

		// Process Attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i).trim();

			if (attrName.equals(EXTENSION_POINT_NAME))
				currentExtPoint.setLabel(translate(attrValue));
			else if (attrName.equals(EXTENSION_POINT_ID)) {
				currentExtPoint.setUniqueIdentifier(namespace.getNamespace() + '.' + attrValue);
			} else if (attrName.equals(EXTENSION_POINT_SCHEMA))
				currentExtPoint.setSchema(attrValue);
			else
				unknownAttribute(EXTENSION_POINT, attrName);
		}
		if (currentExtPoint.getSimpleIdentifier() == null || currentExtPoint.getLabel() == null) {
			String attribute = currentExtPoint.getSimpleIdentifier() == null ? EXTENSION_POINT_ID : EXTENSION_POINT_NAME;
			missingAttribute(attribute, EXTENSION_POINT);
			stateStack.pop();
			stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
			return;
		}

		objectManager.addExtensionPoint(currentExtPoint, true);
		currentExtPoint.setNamespace(namespace.getNamespace());
		currentExtPoint.setBundleId(namespace.getNamespaceBundle().getBundleId());

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
					internalError(NLS.bind(Messages.parse_unknownTopElement, elementName));
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
