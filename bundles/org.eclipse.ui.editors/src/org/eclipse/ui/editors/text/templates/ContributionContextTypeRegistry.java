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
package org.eclipse.ui.editors.text.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * A registry for context types. Editor implementors will usually instantiate a
 * registry and configure the context types available in their editor.
 * <code>ContextType</code>s can be added either directly using
 * {@link #addContextType(TemplateContextType)} or by instantiating and adding a
 * contributed context type using {@link #addContextType(String)}.
 *
 * @since 3.0
 */
public class ContributionContextTypeRegistry extends ContextTypeRegistry {

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
	private static final String REGISTRY= "contextTypeRegistry"; //$NON-NLS-1$
	private static final String REGISTRY_ID= "registryId"; //$NON-NLS-1$


	/**
	 * Creates a new context type registry and registers all context types contributed for the given
	 * registry ID.
	 *
	 * @param registryId the registry ID
	 * @since 3.5
	 */
	public ContributionContextTypeRegistry(String registryId) {
		readRegistry(registryId);
	}

	/**
	 * Creates a new context type registry.
	 * <p>
	 * Clients need to enable the desired context types by calling {@link #addContextType(String)}.
	 * </p>
	 */
	public ContributionContextTypeRegistry() {
	}

	/**
	 * Registers all context types contributed for the given registry ID.
	 *
	 * @param registryId the registry ID
	 * @since 3.5
	 */
	private void readRegistry(String registryId) {
		Assert.isNotNull(registryId);

		IConfigurationElement[] extensions= getTemplateExtensions();

		for (IConfigurationElement extension : extensions) {
			if (extension.getName().equals(REGISTRY)) {
				String id = extension.getAttribute(ID);
				if (registryId.equals(id)) {
					for (IConfigurationElement extension2 : extensions) {
						if (extension2.getName().equals(CONTEXT_TYPE)) {
							if (registryId.equals(extension2.getAttribute(REGISTRY_ID))) {
								addContextType(extension2.getAttribute(ID));
							}
						}
					}
					return;
				}

			}
		}

		Assert.isTrue(false, "invalid registry id"); //$NON-NLS-1$
	}

	/**
	 * Tries to create a context type given an id. If there is already a context
	 * type registered under the given id, nothing happens. Otherwise,
	 * contributions to the <code>org.eclipse.ui.editors.templates</code>
	 * extension point are searched for the given identifier and the specified
	 * context type instantiated if it is found.
	 *
	 * @param id the id for the context type as specified in XML
	 */
	public void addContextType(String id) {
		Assert.isNotNull(id);
		if (getContextType(id) != null) {
			return;
		}

		TemplateContextType type= createContextType(id);
		if (type != null) {
			addContextType(type);
		}

	}

	/**
	 * Tries to create a context type given an id. Contributions to the
	 * <code>org.eclipse.ui.editors.templates</code> extension point are
	 * searched for the given identifier and the specified context type
	 * instantiated if it is found. Any contributed
	 * {@link org.eclipse.jface.text.templates.TemplateVariableResolver}s
	 * are also instantiated and added to the context type.
	 *
	 * @param id the id for the context type as specified in XML
	 * @return the instantiated and configured context type, or
	 *         <code>null</code> if it is not found or cannot be instantiated
	 */
	public static TemplateContextType createContextType(String id) {
		Assert.isNotNull(id);

		IConfigurationElement[] extensions= getTemplateExtensions();
		TemplateContextType type;
		try {
			type= createContextType(extensions, id);
			if (type != null) {
				TemplateVariableResolver[] resolvers= createResolvers(extensions, id);
				for (TemplateVariableResolver resolver : resolvers) {
					type.addResolver(resolver);
				}
			}
		} catch (CoreException e) {
			EditorsPlugin.log(e);
			type= null;
		}

		return type;
	}

	private static TemplateContextType createContextType(IConfigurationElement[] extensions, String contextTypeId) throws CoreException {
		for (IConfigurationElement extension : extensions) {
			// TODO create half-order over contributions
			if (extension.getName().equals(CONTEXT_TYPE)) {
				String id = extension.getAttribute(ID);
				if (contextTypeId.equals(id)) {
					return createContextType(extension);
				}
			}
		}

		return null;
	}

	/**
	 * Instantiates the resolvers contributed to the context type with id
	 * <code>contextTypeId</code>. If instantiation of one resolver fails,
	 * the exception are logged and operation continues.
	 *
	 * @param extensions the configuration elements to parse
	 * @param contextTypeId the id of the context type for which resolvers are
	 *        instantiated
	 * @return the instantiated resolvers
	 */
	private static TemplateVariableResolver[] createResolvers(IConfigurationElement[] extensions, String contextTypeId) {
		List<TemplateVariableResolver> resolvers= new ArrayList<>();
		for (IConfigurationElement extension : extensions) {
			if (extension.getName().equals(RESOLVER)) {
				String declaredId = extension.getAttribute(CONTEXT_TYPE_ID);
				if (contextTypeId.equals(declaredId)) {
					try {
						TemplateVariableResolver resolver = createResolver(extension);
						if (resolver != null) {
							resolvers.add(resolver);
						}
					} catch (CoreException e) {
						EditorsPlugin.log(e);
					}
				}
			}
		}

		return resolvers.toArray(new TemplateVariableResolver[resolvers.size()]);

	}

	private static IConfigurationElement[] getTemplateExtensions() {
		return Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATES_EXTENSION_POINT);
	}

	private static TemplateContextType createContextType(IConfigurationElement element) throws CoreException {
		String id= element.getAttribute(ID);
		try {
			TemplateContextType contextType= (TemplateContextType) element.createExecutableExtension(CLASS);
			String name= element.getAttribute(NAME);
			if (name == null) {
				name= id;
			}

			if (contextType.getId() == null) {
				contextType.setId(id);
			}
			if (contextType.getName() == null) {
				contextType.setName(name);
			}

			return contextType;
		} catch (ClassCastException e) {
			throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, "extension does not implement " + TemplateContextType.class.getName(), e)); //$NON-NLS-1$
		}
	}

	private static TemplateVariableResolver createResolver(IConfigurationElement element) throws CoreException {
		try {
			String type= element.getAttribute(TYPE);
			if (type != null) {

				TemplateVariableResolver resolver= (TemplateVariableResolver) element.createExecutableExtension(CLASS);
				if (resolver.getType() != null) {
					throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, resolver.getClass() + "() must call super() and not set the variable's type", null)); //$NON-NLS-1$
				}
				resolver.setType(type);

				if (resolver.getDescription() != null) {
					throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, resolver.getClass() + "() must call super() and not set the variable's description", null)); //$NON-NLS-1$
				}
				String desc= element.getAttribute(DESCRIPTION);
				resolver.setDescription(desc == null ? "" : desc); //$NON-NLS-1$

				return resolver;
			}
		} catch (ClassCastException e) {
			throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, IStatus.OK, "extension does not implement " + TemplateVariableResolver.class.getName(), e)); //$NON-NLS-1$
		}

		return null;
	}
}

