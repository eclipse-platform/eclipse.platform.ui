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
package org.eclipse.jface.text.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.Assert;

/**
 * A registry for context types. Editor implementors will usually instantiate a
 * registry and configure the context types available in their editor.
 * <code>ContextType</code>s can be added either directly using
 * {@link #addContextType(ContextType)} or by instantiating and adding a
 * contributed context type using {@link #addContextType(String)}.
 * 
 * @since 3.0
 */
public class ContextTypeRegistry {

	/* extension point string literals */
	private static final String TEMPLATES_EXTENSION_POINT= "org.eclipse.ui.editors.templates"; //$NON-NLS-1$

	private static final String CONTEXT_TYPE= "contextType"; //$NON-NLS-1$
	private static final String ID= "id"; //$NON-NLS-1$
	private static final String NAME= "name"; //$NON-NLS-1$
	private static final String CLASS= "class"; //$NON-NLS-1$
	
	private static final String RESOLVER= "resolver"; //$NON-NLS-1$
	private static final String CONTEXT_TYPE_ID= "contextTypeId"; //$NON-NLS-1$
	private static final String DESCRIPTION= "description"; //$NON-NLS-1$
	private static final String TYPE= "type"; //$NON-NLS-1$

	/** all known context types */
	private final Map fContextTypes= new HashMap();
	
	/**
	 * Adds a context type to the registry.
	 * 
	 * @param contextType the context type to add
	 */	
	public void addContextType(ContextType contextType) {
		fContextTypes.put(contextType.getId(), contextType);
	}
	
	/**
	 * Returns the context type if the id is valid, <code>null</code> otherwise.
	 * 
	 * @param id the id of the context type to retrieve
	 * @return the context type if <code>name</code> is valid, <code>null</code> otherwise
	 */
	public ContextType getContextType(String id) {
		return (ContextType) fContextTypes.get(id);
	}

	/**
	 * Returns an iterator over all registered context types.
	 * 
	 * @return an iterator over all registered context types
	 */
	public Iterator contextTypes() {
		return fContextTypes.values().iterator();
	}

	/**
	 * Tries to create a context type given an id. If there is already a context
	 * type registered under <code>id</code>, nothing happens. Otherwise,
	 * contributions to the <code>org.eclipse.ui.editors.templates</code>
	 * extension point are searched for the given identifier and the specified
	 * context type instantiated if it is found.
	 * 
	 * @param id the id for the context type as specified in XML
	 */
	public void addContextType(String id) {
		Assert.isNotNull(id);
		if (fContextTypes.containsKey(id))
			return;
		
		ContextType type= createContextType(id);
		if (type != null)
			addContextType(type);
		
	}
	
	/**
	 * Tries to create a context type given an id. Contributions to the
	 * <code>org.eclipse.ui.editors.templates</code> extension point are
	 * searched for the given identifier and the specified context type
	 * instantiated if it is found. Any contributed
	 * <code>TemplateVariableResolver</code> s are also instantiated and added
	 * to the context type.
	 * 
	 * @param id the id for the context type as specified in XML
	 * @return the instantiated and configured context type, or <code>null</code> if it is not found or cannot be instantiated
	 */
	public static ContextType createContextType(String id) {
		Assert.isNotNull(id);
		
		IConfigurationElement[] extensions= getTemplateExtensions();
		ContextType type= createContextType(extensions, id);
		
		if (type != null) {
			TemplateVariableResolver[] resolvers= createResolvers(extensions, id);
			for (int i= 0; i < resolvers.length; i++)
				type.addResolver(resolvers[i]);
		}
		
		return type;
	}

	private static ContextType createContextType(IConfigurationElement[] extensions, String contextTypeId) {
		for (int i= 0; i < extensions.length; i++) {
			// TODO create half-order over contributions
			if (extensions[i].getName().equals(CONTEXT_TYPE)) {
				String id= extensions[i].getAttributeAsIs(ID);
				if (contextTypeId.equals(id))
					return createContextType(extensions[i]);
			}
		}
		
		return null;
	}

	private static TemplateVariableResolver[] createResolvers(IConfigurationElement[] extensions, String contextTypeId) {
		List resolvers= new ArrayList();
		for (int i= 0; i < extensions.length; i++) {
			if (extensions[i].getName().equals(RESOLVER)) {
				String declaredId= extensions[i].getAttributeAsIs(CONTEXT_TYPE_ID);
				if (contextTypeId.equals(declaredId)) {
					TemplateVariableResolver resolver= createResolver(extensions[i]);
					if (resolver != null)
						resolvers.add(resolver);
				}
			}
		}
		
		return (TemplateVariableResolver[]) resolvers.toArray(new TemplateVariableResolver[resolvers.size()]);
			
	}

	private static IConfigurationElement[] getTemplateExtensions() {
		return Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATES_EXTENSION_POINT);
	}

	private static ContextType createContextType(IConfigurationElement element) {
		String id= element.getAttributeAsIs(ID);
		try {
			ContextType contextType= (ContextType) element.createExecutableExtension(CLASS);
			String name= element.getAttribute(NAME);
			if (name == null)
				name= id;
			
			contextType.setId(id);
			contextType.setName(name);
			
			return contextType;
		} catch (ClassCastException e) {
			// ignore
		} catch (CoreException e) {
			// ignore
		}
		return null;
	}

	private static TemplateVariableResolver createResolver(IConfigurationElement element) {
		try {
			String type= element.getAttributeAsIs(TYPE);
			if (type != null) {

				TemplateVariableResolver resolver= (TemplateVariableResolver) element.createExecutableExtension(CLASS);
				resolver.setType(type);
				
				String desc= element.getAttribute(DESCRIPTION);
				if (desc == null)
					desc= new String();
				resolver.setDescription(desc);
				
				return resolver;
			}
		} catch (CoreException e) {
			// ignore
		} catch (ClassCastException e) {
			// ignore
		}
		
		return null;
	}
	
}
