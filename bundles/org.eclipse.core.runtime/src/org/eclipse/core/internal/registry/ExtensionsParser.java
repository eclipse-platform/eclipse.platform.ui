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

package org.eclipse.core.internal.registry;

import java.io.IOException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.osgi.framework.ServiceReference;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

public class ExtensionsParser extends DefaultHandler {
	private static long cumulativeTime = 0;

	// is in compatibility mode
	private boolean compatibilityMode;

	// concrete object factory
	private Factory factory;

	// File name for this extension manifest
	// This to help with error reporting
	private String locationName = null;

	// Current State Information
	private Stack stateStack = new Stack();

	// Current object stack (used to hold the current object we are
	// populating in this plugin descriptor
	private Stack objectStack = new Stack();

	private Locator locator = null;

	private ServiceReference parserReference;
	private String schemaVersion = null;

	/** 
	 * Status code constant (value 1) indicating a problem in a bundle extensions
	 * manifest (<code>extensions.xml</code>) file.
	 */
	public static final int PARSE_PROBLEM = 1;

	public static final String PLUGIN = "plugin"; //$NON-NLS-1$
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
	// TODO does this object need to be a synchronized Vector?
	// Don't see how this object could be accessed by more than one thread.
	private Vector scratchVectors[] = new Vector[LAST_INDEX + 1];

	private String manifestType;

	public ExtensionsParser(Factory factory) {
		super();
		this.factory = factory;
	}

	/**
	 * Receive a Locator object for document events.
	 *
	 * <p>By default, do nothing.  Application writers may override this
	 * method in a subclass if they wish to store the locator for use
	 * with other document events.</p>
	 *
	 * @param locator A locator for all SAX document events.
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 * @see org.xml.sax.Locator
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
					currentConfigElement.setValue(value);
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
					BundleModel root = (BundleModel) objectStack.peek();

					// Put the extension points into this bundle model
					Vector extPointVector = scratchVectors[EXTENSION_POINT_INDEX];
					if (extPointVector.size() > 0) {
						root.setExtensionPoints((ExtensionPoint[]) extPointVector.toArray(new ExtensionPoint[extPointVector.size()]));
						scratchVectors[EXTENSION_POINT_INDEX].removeAllElements();
					}

					// Put the extensions into this bundle model too
					Vector extVector = scratchVectors[EXTENSION_INDEX];
					if (extVector.size() > 0) {
						root.setExtensions((Extension[]) extVector.toArray(new Extension[extVector.size()]));
						scratchVectors[EXTENSION_INDEX].removeAllElements();
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
					BundleModel parent = (BundleModel) objectStack.peek();
					currentExtension.setParent(parent);
					scratchVectors[EXTENSION_INDEX].addElement(currentExtension);
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

	private void handleExtensionPointState(String elementName, Attributes attributes) {

		// We ignore all elements under extension points (if there are any)
		stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
		internalError(Policy.bind("parse.unknownElement", EXTENSION_POINT, elementName)); //$NON-NLS-1$
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
		ConfigurationElement currentConfigurationElement = factory.createConfigurationElement();
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
		BundleModel current = factory.createBundle();
		current.setSchemaVersion(schemaVersion);
		current.setStartLine(locator.getLineNumber());
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
		internalError(Policy.bind("parse.unknownElement", manifestType, elementName)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void ignoreableWhitespace(char[] ch, int start, int length) {
		// do nothing
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
		factory.error(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, PARSE_PROBLEM, msg, ex));
	}

	private SAXParserFactory acquireXMLParsing() {
		parserReference = InternalPlatform.getDefault().getBundleContext().getServiceReference("javax.xml.parsers.SAXParserFactory");
		if (parserReference == null)
			return null;
		return (SAXParserFactory) InternalPlatform.getDefault().getBundleContext().getService(parserReference);
	}

	private void releaseXMLParsing() {
		if (parserReference != null)
			InternalPlatform.getDefault().getBundleContext().ungetService(parserReference);
	}

	// TODO why is this synchronized; does not appear to be called by multiple threads.
	// a new ExtensionParser is created for each parsing action.
	synchronized public BundleModel parseManifest(InputSource in, String manifestType) throws SAXException, IOException {
		long start = 0;
		if (InternalPlatform.DEBUG)
			start = System.currentTimeMillis();

		SAXParserFactory factory = acquireXMLParsing();
		if (factory == null)
			return null; //TODO We should log a warning

		try {
			if (manifestType == null)
				throw new NullPointerException();
			if (!(manifestType.equals(PLUGIN) || manifestType.equals(FRAGMENT)))
				throw new IllegalArgumentException("Invalid manifest type: " + manifestType);
			this.manifestType = manifestType;
			locationName = in.getSystemId();
			try {
				factory.setNamespaceAware(true);
				factory.setFeature("http://xml.org/sax/features/string-interning", true); //$NON-NLS-1$
				factory.setValidating(false);
				factory.newSAXParser().parse(in, this);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				// If this happens seems like we should throw an exception.
				e.printStackTrace();
			}

			return (BundleModel) objectStack.pop();
		} finally {
			releaseXMLParsing();
			if (InternalPlatform.DEBUG) {
				cumulativeTime = cumulativeTime + (System.currentTimeMillis() - start);
				InternalPlatform.getDefault().setOption("org.eclipse.core.runtime/registry/parsing/timing" + "/value", Long.toString(cumulativeTime));
			}
		}
	}

	private void parseConfigurationElementAttributes(Attributes attributes) {

		ConfigurationElement parentConfigurationElement = (ConfigurationElement) objectStack.peek();
		parentConfigurationElement.setStartLine(locator.getLineNumber());

		Vector propVector = null;

		// process attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		if (len == 0)
			return;
		propVector = new Vector();

		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i);

			ConfigurationProperty currentConfigurationProperty = factory.createConfigurationProperty();
			currentConfigurationProperty.setName(attrName);
			currentConfigurationProperty.setValue(attrValue);
			propVector.addElement(currentConfigurationProperty);
		}
		parentConfigurationElement.setProperties((ConfigurationProperty[]) propVector.toArray(new ConfigurationProperty[propVector.size()]));
		propVector = null;
	}

	private void parseExtensionAttributes(Attributes attributes) {

		BundleModel parent = (BundleModel) objectStack.peek();
		Extension currentExtension = factory.createExtension();
		currentExtension.setStartLine(locator.getLineNumber());
		objectStack.push(currentExtension);

		// Process Attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i).trim();

			if (attrName.equals(EXTENSION_NAME))
				currentExtension.setName(attrValue);
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
				internalError(Policy.bind("parse.unknownAttribute", EXTENSION, attrName)); //$NON-NLS-1$
		}
	}

	private void parseExtensionPointAttributes(Attributes attributes) {

		ExtensionPoint currentExtPoint = factory.createExtensionPoint();
		currentExtPoint.setStartLine(locator.getLineNumber());

		// Process Attributes
		int len = (attributes != null) ? attributes.getLength() : 0;
		for (int i = 0; i < len; i++) {
			String attrName = attributes.getLocalName(i);
			String attrValue = attributes.getValue(i).trim();

			if (attrName.equals(EXTENSION_POINT_NAME))
				currentExtPoint.setName(attrValue);
			else if (attrName.equals(EXTENSION_POINT_ID))
				currentExtPoint.setSimpleIdentifier(attrValue);
			else if (attrName.equals(EXTENSION_POINT_SCHEMA))
				currentExtPoint.setSchema(attrValue);
			else
				internalError(Policy.bind("parse.unknownAttribute", EXTENSION_POINT, attrName)); //$NON-NLS-1$
		}
		// currentExtPoint contains a pointer to the parent bundle model.
		BundleModel root = (BundleModel) objectStack.peek();
		currentExtPoint.setParent(root);

		// Now populate the the vector just below us on the objectStack with this extension point
		scratchVectors[EXTENSION_POINT_INDEX].addElement(currentExtPoint);
	}

	public void startDocument() {
		stateStack.push(new Integer(INITIAL_STATE));
		for (int i = 0; i <= LAST_INDEX; i++) {
			scratchVectors[i] = new Vector();
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
				handleExtensionPointState(elementName, attributes);
				break;
			case BUNDLE_EXTENSION_STATE :
			case CONFIGURATION_ELEMENT_STATE :
				handleExtensionState(elementName, attributes);
				break;
			default :
				stateStack.push(new Integer(IGNORED_ELEMENT_STATE));
				internalError(Policy.bind("parse.unknownTopElement", elementName)); //$NON-NLS-1$
		}
	}

	public void warning(SAXParseException ex) {
		// no warnings if in compatibility mode
		if (!compatibilityMode)
			logStatus(ex);
	}

	private void internalError(String message) {
		// no warnings if in compatibility mode
		if (compatibilityMode)
			return;
		if (locationName != null)
			factory.error(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, PARSE_PROBLEM, locationName + ": " + message, null)); //$NON-NLS-1$
		else
			factory.error(new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, PARSE_PROBLEM, message, null));
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
}