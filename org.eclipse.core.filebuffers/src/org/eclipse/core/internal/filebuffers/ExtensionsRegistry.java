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
package org.eclipse.core.internal.filebuffers;



import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.filebuffers.IDocumentFactory;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;


/**
 * This registry manages shareable document factories. Document factories are specified 
 * in <code>plugin.xml</code> per file name extension.
 */
public class ExtensionsRegistry {
	
	private final static String WILDCARD= "*";  //$NON-NLS-1$
	
	/** The mapping between name extensions and configuration elements describing document factories. */
	private Map fFactoryDescriptors= new HashMap();
	/** The mapping between configuration elements for document factories and instantiated document factories. */
	private Map fFactories= new HashMap();
	/** The mapping between name extensions and configuration elements describing document setup participants. */
	private Map fSetupParticipantDescriptors= new HashMap();
	/** The mapping between configuration elements for setup participants and instantiated setup participants. */
	private Map fSetupParticipants= new HashMap();
	
	
	/**
	 * Creates a new document factory registry and intializes it with the information
	 * found in the plugin registry.
	 */
	public ExtensionsRegistry() {
		initialize("documentCreation", "extensions",  fFactoryDescriptors); //$NON-NLS-1$ //$NON-NLS-2$
		initialize("documentSetup", "extensions", fSetupParticipantDescriptors);
	}
	
	/**
	 * Reads the comma-separated value of the given configuration element 
	 * for the given attribute name and remembers the configuration element
	 * in the given map under the individual tokens of the attribute value.
	 */
	private void read(String attributeName, IConfigurationElement element, Map map) {
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
	 * Adds an entry to the log of this plugin for the given status
	 * @param status the status to log
	 */
	private void log(IStatus status) {
		ILog log=  Platform.getPlugin(FileBuffersPlugin.PLUGIN_ID).getLog();
		log.log(status);
	}
	
	/**
	 * Initializes this registry. It retrieves all implementers of the given
	 * extension point and remembers those implementers based on the
	 * file name extensions in the given map.
	 * 
	 * @param extensionPointName the name of the extension point
	 * @param childElementName the name of the child elements
	 * @param descriptors the map to be filled 
	 */
	private void initialize(String extensionPointName, String childElementName, Map descriptors) {
		
		IExtensionPoint extensionPoint= Platform.getPluginRegistry().getExtensionPoint(FileBuffersPlugin.PLUGIN_ID, extensionPointName);
		if (extensionPoint == null) {
			log(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, 0, MessageFormat.format("Extension point \"{0}\" not found.", new Object[] { extensionPointName}), null));
			return;
		}
		
		IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
		for (int i= 0; i < elements.length; i++)
			read(childElementName, elements[i], descriptors);
	}
	
	/**
	 * Returns the executable extension for the given configuration element.
	 * If there is no instantiated extension remembered for this
	 * element, a new extension is created and put into the cache if it is of the requested type.
	 * 
	 * @param entry the configuration element
	 * @param extensions the map of instantiated extensions
	 * @param extensionType the requested result type
	 */
	private Object getExtension(IConfigurationElement entry, Map extensions, Class extensionType) {
		Object extension= extensions.get(entry);
		if (extension != null)
			return extension;
			
		try {
			extension= entry.createExecutableExtension("class"); //$NON-NLS-1$
		} catch (CoreException x) {
			log(x.getStatus());
		}
		
		if (extensionType.isInstance(extension)) {
			extensions.put(entry, extension);
			return extension;
		}
		
		return null;
	}
	
	/**
	 * Returns the first enumerated element of the given set.
	 * 
	 * @param set the set from which to choose
	 */
	private IConfigurationElement selectConfigurationElement(Set set) {
		if (set != null && !set.isEmpty()) {
			Iterator e= set.iterator();
			return (IConfigurationElement) e.next();
		}
		return null;
	}
	
	/**
	 * Returns a shareable document factory for the given file name extension.
	 *
	 * @param extension the name extension to be used for lookup
	 * @return the shareable document factory or <code>null</code>
	 */
	private IDocumentFactory getDocumentFactory(String extension) {
		
		Set set= (Set) fFactoryDescriptors.get(extension);
		if (set != null) {
			IConfigurationElement entry= selectConfigurationElement(set);
			return (IDocumentFactory) getExtension(entry, fFactories, IDocumentFactory.class);
		}
		return null;
	}
	
	/**
	 * Returns the set of setup participants for the given file name.
	 * 
	 * @param extension the name extension to be used for lookup
	 * @return the shareable set of document setup participants
	 */
	private List getDocumentSetupParticipants(String extension) {
		
		Set set= (Set) fSetupParticipantDescriptors.get(extension);
		if (set == null)
			return null;
		
		List participants= new ArrayList();
		Iterator e= set.iterator();
		while (e.hasNext()) {
			IConfigurationElement entry= (IConfigurationElement) e.next();
			Object participant= getExtension(entry, fSetupParticipants, IDocumentSetupParticipant.class);
			if (participant != null)
				participants.add(participant);
		}
		
		return participants;
	}
	
	/**
	 * Returns the shareable document factory for the given file.
	 *
	 * @param file the file for whose type the factory is looked up
	 * @return the shareable document factory
	 */
	public IDocumentFactory getDocumentFactory(IFile file) {
		IDocumentFactory factory= getDocumentFactory(file.getFileExtension());
		if (factory == null)
			factory= getDocumentFactory(WILDCARD);
		return factory;
	}
	
	/**
	 * Returns the shareable set of document setup participants for the given file.
	 * 
	 * @param file the file for which to look up the setup participants
	 * @return the shareable set of document setup participants
	 */
	public IDocumentSetupParticipant[] getDocumentSetupParticipants(IFile file) {
		List participants= new ArrayList();
		
		List p= getDocumentSetupParticipants(file.getFileExtension());
		if (p != null)
			participants.addAll(p);
			
		p= getDocumentSetupParticipants(WILDCARD);
		if (p != null)
			participants.addAll(p);
			
		IDocumentSetupParticipant[] result= new IDocumentSetupParticipant[participants.size()];
		participants.toArray(result);
		return result;
	}
}
