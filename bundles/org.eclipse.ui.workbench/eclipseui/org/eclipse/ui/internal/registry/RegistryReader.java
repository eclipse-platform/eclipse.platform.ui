/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Mickael Istria (Red Hat Inc) - [507295] orderExtensions
 *******************************************************************************/
package org.eclipse.ui.internal.registry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.ui.internal.workbench.ExtensionsSort;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Template implementation of a registry reader that creates objects
 * representing registry contents. Typically, an extension contains one element,
 * but this reader handles multiple elements per extension.
 *
 * To start reading the extensions from the registry for an extension point,
 * call the method <code>readRegistry</code>.
 *
 * To read children of an IConfigurationElement, call the method
 * <code>readElementChildren</code> from your implementation of the method
 * <code>readElement</code>, as it will not be done by default.
 */
public abstract class RegistryReader {

	// for dynamic UI - remove this cache to avoid inconsistency
	// protected static Hashtable extensionPoints = new Hashtable();
	/**
	 * The constructor.
	 */
	protected RegistryReader() {
	}

	/**
	 * Logs the error in the workbench log using the provided text and the
	 * information in the configuration element.
	 */
	protected static void logError(IConfigurationElement element, String text) {
		IExtension extension = element.getDeclaringExtension();
		StringBuilder buf = new StringBuilder();
		buf.append("Plugin " + extension.getContributor().getName() + ", extension " //$NON-NLS-1$//$NON-NLS-2$
				+ extension.getExtensionPointUniqueIdentifier());
		// look for an ID if available - this should help debugging
		String id = element.getAttribute("id"); //$NON-NLS-1$
		if (id != null) {
			buf.append(", id "); //$NON-NLS-1$
			buf.append(id);
		}
		buf.append(": " + text);//$NON-NLS-1$
		WorkbenchPlugin.log(buf.toString());
	}

	/**
	 * Logs a very common registry error when a required attribute is missing.
	 */
	protected static void logMissingAttribute(IConfigurationElement element, String attributeName) {
		logError(element, "Required attribute '" + attributeName + "' not defined");//$NON-NLS-2$//$NON-NLS-1$
	}

	/**
	 * Logs a very common registry error when a required child is missing.
	 */
	protected static void logMissingElement(IConfigurationElement element, String elementName) {
		logError(element, "Required sub element '" + elementName + "' not defined");//$NON-NLS-2$//$NON-NLS-1$
	}

	/**
	 * Logs a registry error when the configuration element is unknown.
	 */
	protected static void logUnknownElement(IConfigurationElement element) {
		logError(element, "Unknown extension tag found: " + element.getName());//$NON-NLS-1$
	}

	/**
	 * Apply a reproducable order to the list of extensions provided, such that the
	 * order will not change as extensions are added or removed.
	 *
	 * @param extensions the extensions to order
	 * @return ordered extensions
	 */
	public static IExtension[] orderExtensions(IExtension[] extensions) {
		return new ExtensionsSort().sort(extensions);
	}

	/**
	 * Implement this method to read element's attributes. If children should also
	 * be read, then implementor is responsible for calling
	 * <code>readElementChildren</code>. Implementor is also responsible for logging
	 * missing attributes.
	 *
	 * @return true if element was recognized, false if not.
	 */
	protected abstract boolean readElement(IConfigurationElement element);

	/**
	 * Read the element's children. This is called by the subclass' readElement
	 * method when it wants to read the children of the element.
	 */
	protected void readElementChildren(IConfigurationElement element) {
		readElements(element.getChildren());
	}

	/**
	 * Read each element one at a time by calling the subclass implementation of
	 * <code>readElement</code>.
	 *
	 * Logs an error if the element was not recognized.
	 */
	protected void readElements(IConfigurationElement[] elements) {
		for (IConfigurationElement element : elements) {
			if (!readElement(element)) {
				logUnknownElement(element);
			}
		}
	}

	/**
	 * Read one extension by looping through its configuration elements.
	 */
	protected void readExtension(IExtension extension) {
		readElements(extension.getConfigurationElements());
	}

	/**
	 * Start the registry reading process using the supplied plugin ID and extension
	 * point.
	 *
	 * @param registry       the registry to read from
	 * @param pluginId       the plugin id of the extenion point
	 * @param extensionPoint the extension point id
	 */
	public void readRegistry(IExtensionRegistry registry, String pluginId, String extensionPoint) {
		IExtensionPoint point = registry.getExtensionPoint(pluginId, extensionPoint);
		if (point == null) {
			return;
		}
		IExtension[] extensions = point.getExtensions();
		extensions = orderExtensions(extensions);
		for (IExtension extension : extensions) {
			readExtension(extension);
		}
	}

	/**
	 * Utility for extracting the description child of an element.
	 *
	 * @param configElement the element
	 * @return the description
	 * @since 3.1
	 */
	public static String getDescription(IConfigurationElement configElement) {
		IConfigurationElement[] children = configElement.getChildren(IWorkbenchRegistryConstants.TAG_DESCRIPTION);
		if (children.length >= 1) {
			return children[0].getValue();
		}
		return "";//$NON-NLS-1$
	}

	/**
	 * Utility for extracting the value of a class attribute or a nested class
	 * element that follows the pattern set forth by
	 * {@link org.eclipse.core.runtime.IExecutableExtension}.
	 *
	 * @param configElement      the element
	 * @param classAttributeName the name of the class attribute to check
	 * @return the value of the attribute or nested class element
	 * @since 3.1
	 */
	public static String getClassValue(IConfigurationElement configElement, String classAttributeName) {
		String className = configElement.getAttribute(classAttributeName);
		if (className != null) {
			return className;
		}
		IConfigurationElement[] candidateChildren = configElement.getChildren(classAttributeName);
		if (candidateChildren.length == 0) {
			return null;
		}

		return candidateChildren[0].getAttribute(IWorkbenchRegistryConstants.ATT_CLASS);
	}
}
