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
package org.eclipse.ui.texteditor;



import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;


/**
 * This registry manages shareable document provider. Document
 * providers are specified in <code>plugin.xml</code> either 
 * per name extension or per editor input type. A name extension
 * rule always overrules an editor input type rule. Editor input
 * type rules follow the same rules <code>IAdapterManager</code>
 * used to find object adapters.
 *
 * @see org.eclipse.core.runtime.IAdapterManager
 */
public class DocumentProviderRegistry {
	
	/** The registry singleton. */
	private static DocumentProviderRegistry fgRegistry;
	
	/**
	 * Returns the standard document provider registry.
	 */
	public static DocumentProviderRegistry getDefault() {
		if (fgRegistry == null)
			fgRegistry= new DocumentProviderRegistry();
		return fgRegistry;
	}
	
	
	/** The mapping between name extensions and configuration elements. */
	private Map fExtensionMapping= new HashMap();
	/** The mapping between editor input type names and configuration elements. */
	private Map fInputTypeMapping= new HashMap();
	/** The mapping between configuration elements and instantiated document providers. */
	private Map fInstances= new HashMap();
	
	
	/**
	 * Creates a new document provider registry and intializes it with the information
	 * found in the plugin registry.
	 */
	private DocumentProviderRegistry() {
		initialize();
	}
	
	/**
	 * Reads the comma-separated value of the given configuration element 
	 * for the given attribute name and remembers the configuration element
	 * in the given map under the individual tokens of the attribute value.
	 */
	private void read(Map map, IConfigurationElement element, String attributeName) {
		String value= element.getAttribute(attributeName);
		if (value != null) {
			StringTokenizer tokenizer= new StringTokenizer(value, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token= tokenizer.nextToken().trim();
				
				Set s= (Set) map.get(token);
				if (s == null) {
					s= new HashSet();
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
		extensionPoint= Platform.getPluginRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, "documentProviders"); //$NON-NLS-1$
		
		if (extensionPoint == null) {
			String msg= MessageFormat.format(EditorMessages.getString("DocumentProviderRegistry.error.extension_point_not_found"), new Object[] { PlatformUI.PLUGIN_ID }); //$NON-NLS-1$
			ILog log= Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog();
			log.log(new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, 0, msg, null));
			return;
		}
		
		IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
		for (int i= 0; i < elements.length; i++) {
			read(fExtensionMapping, elements[i], "extensions"); //$NON-NLS-1$
			read(fInputTypeMapping, elements[i], "inputTypes"); //$NON-NLS-1$
		}
	}
	
	/**
	 * Returns the document provider for the given configuration element.
	 * If there is no instantiated document provider remembered for this
	 * element, a new document provider is created and put into the cache.
	 */
	private IDocumentProvider getDocumentProvider(IConfigurationElement entry) {
		IDocumentProvider provider= (IDocumentProvider) fInstances.get(entry);
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
	 */
	private IConfigurationElement selectConfigurationElement(Set set) {
		if (set != null && !set.isEmpty()) {
			Iterator e= set.iterator();
			return (IConfigurationElement) e.next();
		}
		return null;
	}
	
	/**
	 * Returns a shareable document provider for the given name extension.
	 *
	 * @param extension the name extension to be used for lookup
	 * @return the shareable document provider or <code>null</code>
	 */
	public IDocumentProvider getDocumentProvider(String extension) {
		
		Set set= (Set) fExtensionMapping.get(extension);
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
	 * @return a list with elements of type <code>Class</code>
	 */
	private List computeClassList(Class type) {
		
		List result= new ArrayList();
		
		Class c= type;
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
	 * @return a list with elements of type <code>Class</code>
	 */
	private List computeInterfaceList(List classes) {
		
		List result= new ArrayList(4);
		Hashtable visited= new Hashtable(4);
		
		Iterator e= classes.iterator();
		while (e.hasNext()) {
			Class c= (Class) e.next();
			computeInterfaceList(c.getInterfaces(), result, visited);
		}
		
		return result;
	}
	
	/**
	 * Computes the list of all interfaces of the given list of interfaces,
	 * taking a depth-first approach.
	 */
	private void computeInterfaceList(Class[] interfaces, List result, Hashtable visited) {
		
		List toBeVisited= new ArrayList(interfaces.length);
		
		for (int i= 0; i < interfaces.length; i++) {
			Class iface= interfaces[i];
			if (visited.get(iface) == null) {
				visited.put(iface, iface);
				result.add(iface);
				toBeVisited.add(iface);
			}
		}
		
		Iterator e= toBeVisited.iterator();
		while(e.hasNext()) {
			Class iface= (Class) e.next();
			computeInterfaceList(iface.getInterfaces(), result, visited);
		}
	}
	
	/**
	 * Returns the configuration elements for the first class in the list
	 * of given classes for which configuration elements have been remembered.
	 * 
	 * returns an input type mapping or <code>null</code>
	 */
	private Object getFirstInputTypeMapping(List classes) {
		Iterator e= classes.iterator();
		while (e.hasNext()) {
			Class c= (Class) e.next();
			Object mapping= fInputTypeMapping.get(c.getName());
			if (mapping != null)
				return mapping;
		}
		return null;
	}
	
	/**
	 * Returns the appropriate configuration element for the given type. If
	 * there is no configuration element for the type's name, first the list of
	 * super classes is searched, and if not successful the list of all interfaces.
	 * 
	 * returns an input type mapping or <code>null</code>
	 */
	private Object findInputTypeMapping(Class type) {
		
		if (type == null)
			return null;
			
		Object mapping= fInputTypeMapping.get(type.getName());
		if (mapping != null)
			return mapping;
			
		List classList= computeClassList(type);
		mapping= getFirstInputTypeMapping(classList);
		if (mapping != null)
			return mapping;
			
		return getFirstInputTypeMapping(computeInterfaceList(classList));
	}
	
	/**
	 * Returns the shareable document for the type of the given editor input.
	 *
	 * @param editorInput the input for whose type the provider is looked up
	 * @return the shareable document provider
	 */
	public IDocumentProvider getDocumentProvider(IEditorInput editorInput) {
		
		IDocumentProvider provider= null;
		
		IFile file= (IFile) editorInput.getAdapter(IFile.class);
		if (file != null)
			provider= getDocumentProvider(file.getFileExtension());
		
		if (provider == null) {
			Set set= (Set) findInputTypeMapping(editorInput.getClass());
			if (set != null) {
				IConfigurationElement entry= selectConfigurationElement(set);
				provider= getDocumentProvider(entry);
			}
		}
		
		return provider;
	}
}
