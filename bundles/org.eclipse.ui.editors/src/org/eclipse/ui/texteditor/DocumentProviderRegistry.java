/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.resources.IFile;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.editors.text.NLSUtility;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * This registry manages shared document providers. Document
 * providers are specified in <code>plugin.xml</code> either
 * per name extension or per editor input type. A name extension
 * rule always overrules an editor input type rule. Editor input
 * type rules follow the same rules <code>IAdapterManager</code>
 * used to find object adapters.
 *
 * @see org.eclipse.core.runtime.IAdapterManager
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DocumentProviderRegistry {

	/** The registry singleton. */
	private static DocumentProviderRegistry fgRegistry;

	/**
	 * Returns the standard document provider registry.
	 *
	 * @return the default document provider registry
	 */
	public static DocumentProviderRegistry getDefault() {
		if (fgRegistry == null) {
			fgRegistry= new DocumentProviderRegistry();
		}
		return fgRegistry;
	}


	/** The mapping between name extensions and configuration elements. */
	private final Map<String, Set<IConfigurationElement>> fExtensionMapping= new HashMap<>();
	/** The mapping between editor input type names and configuration elements. */
	private final Map<String, Set<IConfigurationElement>> fInputTypeMapping= new HashMap<>();
	/** The mapping between configuration elements and instantiated document providers. */
	private final Map<IConfigurationElement, IDocumentProvider> fInstances= new HashMap<>();


	/**
	 * Creates a new document provider registry and initializes it with the information
	 * found in the plug-in registry.
	 */
	private DocumentProviderRegistry() {
		initialize();
	}

	/**
	 * Reads the comma-separated value of the given configuration element
	 * for the given attribute name and remembers the configuration element
	 * in the given map under the individual tokens of the attribute value.
	 *
	 * @param map the map
	 * @param element the configuration element
	 * @param attributeName the attribute name
	 */
	private void read(Map<String, Set<IConfigurationElement>> map, IConfigurationElement element, String attributeName) {
		String value= element.getAttribute(attributeName);
		if (value != null) {
			StringTokenizer tokenizer= new StringTokenizer(value, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token= tokenizer.nextToken().trim();

				Set<IConfigurationElement> s= map.get(token);
				if (s == null) {
					s= new HashSet<>();
					map.put(token, s);
				}
				s.add(element);
			}
		}
	}

	/**
	 * Initializes the document provider registry. It retrieves all implementers of the <code>documentProviders</code>
	 * extension point and remembers those implementers based on the name extensions and the editor input
	 * types they are for.
	 */
	private void initialize() {

		IExtensionPoint extensionPoint;
		extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(EditorsUI.PLUGIN_ID, "documentProviders"); //$NON-NLS-1$

		if (extensionPoint == null) {
			String msg= NLSUtility.format(TextEditorMessages.DocumentProviderRegistry_error_extension_point_not_found, PlatformUI.PLUGIN_ID);
			Bundle bundle = Platform.getBundle(PlatformUI.PLUGIN_ID);
			ILog log= ILog.of(bundle);
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, msg, null));
			return;
		}

		IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			read(fExtensionMapping, element, "extensions"); //$NON-NLS-1$
			read(fInputTypeMapping, element, "inputTypes"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the document provider for the given configuration element.
	 * If there is no instantiated document provider remembered for this
	 * element, a new document provider is created and put into the cache.
	 *
	 * @param entry the configuration element
	 * @return the document provider for the given entry
	 */
	private IDocumentProvider getDocumentProvider(IConfigurationElement entry) {
		IDocumentProvider provider= fInstances.get(entry);
		if (provider == null) {
			try {
				provider= (IDocumentProvider) entry.createExecutableExtension("class"); //$NON-NLS-1$
				fInstances.put(entry, provider);
			} catch (CoreException x) {
			}
		}
		return provider;
	}

	/**
	 * Returns the first enumerated element of the given set.
	 *
	 * @param set the set
	 * @return the first configuration element in the set or <code>null</code> if none
	 */
	private IConfigurationElement selectConfigurationElement(Set<IConfigurationElement> set) {
		if (set != null && !set.isEmpty()) {
			Iterator<IConfigurationElement> e= set.iterator();
			return e.next();
		}
		return null;
	}

	/**
	 * Returns a shared document provider for the given name extension.
	 *
	 * @param extension the name extension to be used for lookup
	 * @return the shared document provider or <code>null</code>
	 */
	public IDocumentProvider getDocumentProvider(String extension) {

		Set<IConfigurationElement> set= fExtensionMapping.get(extension);
		if (set != null) {
			IConfigurationElement entry= selectConfigurationElement(set);
			return getDocumentProvider(entry);
		}
		return null;
	}

	/**
	 * Computes the class hierarchy of the given type. The type is
	 * part of the computed hierarchy.
	 *
	 * @param type the type
	 * @return a list containing the super class hierarchy
	 */
	private List<Class<?>> computeClassList(Class<?> type) {

		List<Class<?>> result= new ArrayList<>();

		Class<?> c= type;
		while (c != null) {
			result.add(c);
			c= c.getSuperclass();
		}

		return result;
	}

	/**
	 * Computes the list of all interfaces for the given list of
	 * classes. The interface lists of the given classes are
	 * concatenated.
	 *
	 * @param classes a list of {@link java.lang.Class} objects
	 * @return a list with elements of type <code>Class</code>
	 */
	private List<Class<?>> computeInterfaceList(List<Class<?>> classes) {

		List<Class<?>> result= new ArrayList<>(4);
		Hashtable<Class<?>, Class<?>> visited= new Hashtable<>(4);

		Iterator<Class<?>> e= classes.iterator();
		while (e.hasNext()) {
			Class<?> c= e.next();
			computeInterfaceList(c.getInterfaces(), result, visited);
		}

		return result;
	}

	/**
	 * Computes the list of all interfaces of the given list of interfaces,
	 * taking a depth-first approach.
	 *
	 * @param interfaces an array of {@link java.lang.Class} objects denoting interfaces
	 * @param result the result list
	 * @param visited map of visited interfaces
	 */
	private void computeInterfaceList(Class<?>[] interfaces, List<Class<?>> result, Hashtable<Class<?>, Class<?>> visited) {

		List<Class<?>> toBeVisited= new ArrayList<>(interfaces.length);

		for (Class<?> iface : interfaces) {
			if (visited.get(iface) == null) {
				visited.put(iface, iface);
				result.add(iface);
				toBeVisited.add(iface);
			}
		}

		Iterator<Class<?>> e= toBeVisited.iterator();
		while(e.hasNext()) {
			Class<?> iface= e.next();
			computeInterfaceList(iface.getInterfaces(), result, visited);
		}
	}

	/**
	 * Returns the configuration elements for the first class in the list
	 * of given classes for which configuration elements have been remembered.
	 *
	 * @param classes a list of {@link java.lang.Class} objects
	 * @return an input type mapping or <code>null</code>
	 */
	private Set<IConfigurationElement> getFirstInputTypeMapping(List<Class<?>> classes) {
		Iterator<Class<?>> e= classes.iterator();
		while (e.hasNext()) {
			Class<?> c= e.next();
			Set<IConfigurationElement> mapping= fInputTypeMapping.get(c.getName());
			if (mapping != null) {
				return mapping;
			}
		}
		return null;
	}

	/**
	 * Returns the appropriate configuration element for the given type. If
	 * there is no configuration element for the type's name, first the list of
	 * super classes is searched, and if not successful the list of all interfaces.
	 *
	 * @param type a {@link java.lang.Class} object
	 * @return an input type mapping or <code>null</code>
	 */
	private Set<IConfigurationElement> findInputTypeMapping(Class<?> type) {

		if (type == null) {
			return null;
		}

		Set<IConfigurationElement> mapping= fInputTypeMapping.get(type.getName());
		if (mapping != null) {
			return mapping;
		}

		List<Class<?>> classList= computeClassList(type);
		mapping= getFirstInputTypeMapping(classList);
		if (mapping != null) {
			return mapping;
		}

		return getFirstInputTypeMapping(computeInterfaceList(classList));
	}

	/**
	 * Returns the shared document for the type of the given editor input.
	 *
	 * @param editorInput the input for whose type the provider is looked up
	 * @return the shared document provider
	 */
	public IDocumentProvider getDocumentProvider(IEditorInput editorInput) {

		IDocumentProvider provider= null;

		IFile file= editorInput.getAdapter(IFile.class);
		if (file != null) {
			provider= getDocumentProvider(file.getFileExtension());
		} else {
			IPathEditorInput pathInput= Adapters.adapt(editorInput, IPathEditorInput.class);
			if (pathInput != null) {
				IPath path= pathInput.getPath();
				if (path != null) {
					provider= getDocumentProvider(path.getFileExtension());
				}
			}
		}

		if (provider == null) {
			Set<IConfigurationElement> set= findInputTypeMapping(editorInput.getClass());
			if (set != null) {
				IConfigurationElement entry= selectConfigurationElement(set);
				provider= getDocumentProvider(entry);
			}
		}

		return provider;
	}
}
