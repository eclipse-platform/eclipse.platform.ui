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
package org.eclipse.ui.editors.text.templates;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

/**
 * Manages templates. Handles reading default templates contributed via XML and
 * user-defined (or overridden) templates stored in the preferences. Clients may
 * instantiate this class.
 * 
 * <p>This class will become final.</p>
 * 
 * @since 3.0
 */
public class ContributionTemplateStore extends TemplateStore {
	/* extension point string literals */
	private static final String TEMPLATES_EXTENSION_POINT= "org.eclipse.ui.editors.templates"; //$NON-NLS-1$

	private static final String ID= "id"; //$NON-NLS-1$
	private static final String NAME= "name"; //$NON-NLS-1$
	
	private static final String CONTEXT_TYPE_ID= "contextTypeId"; //$NON-NLS-1$
	private static final String DESCRIPTION= "description"; //$NON-NLS-1$

	private static final String TEMPLATE= "template"; //$NON-NLS-1$
	private static final String PATTERN= "pattern"; //$NON-NLS-1$
	
	private static final String INCLUDE= "include"; //$NON-NLS-1$
	private static final String FILE= "file"; //$NON-NLS-1$
	private static final String TRANSLATIONS= "translations"; //$NON-NLS-1$

	/**
	 * Creates a new template store.
	 * 
	 * @param store the preference store in which to store custom templates
	 *        under <code>key</code>
	 * @param key the key into <code>store</code> where to store custom
	 *        templates
	 */
	public ContributionTemplateStore(IPreferenceStore store, String key) {
		super(store, key);
	}
	
	/**
	 * Creates a new template store with a context type registry. Only templates
	 * that specify a context type contained in the registry will be loaded by
	 * this store if the registry is not <code>null</code>.
	 * 
	 * @param registry a context type registry, or <code>null</code> if all
	 *        templates should be loaded
	 * @param store the preference store in which to store custom templates
	 *        under <code>key</code>
	 * @param key the key into <code>store</code> where to store custom
	 *        templates
	 */
	public ContributionTemplateStore(ContextTypeRegistry registry, IPreferenceStore store, String key) {
		super(registry, store, key);
	}
	
	/**
	 * Loads the templates contributed via the templates extension point.
	 * 
	 * @throws IOException {@inheritDoc}
	 */
	protected void loadContributedTemplates() throws IOException {
		IConfigurationElement[] extensions= getTemplateExtensions();
		Collection contributed= readContributedTemplates(extensions);
		for (Iterator it= contributed.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			internalAdd(data);
		}
	}
	
	private Collection readContributedTemplates(IConfigurationElement[] extensions) throws IOException {
		Collection templates= new ArrayList();
		for (int i= 0; i < extensions.length; i++) {
			if (extensions[i].getName().equals(TEMPLATE))
				createTemplate(templates, extensions[i]);
			else if (extensions[i].getName().equals(INCLUDE)) {
				readIncludedTemplates(templates, extensions[i]);
			}
		}
		
		return templates;
	}

	private void readIncludedTemplates(Collection templates, IConfigurationElement element) throws IOException {
		String file= element.getAttributeAsIs(FILE);
		if (file != null) {
			Bundle plugin = Platform.getBundle(element.getDeclaringExtension().getNamespace());
			URL url= Platform.find(plugin, new Path(file));
			if (url != null) {
				ResourceBundle bundle= null;
				String translations= element.getAttributeAsIs(TRANSLATIONS);
				if (translations != null) {
					URL bundleURL= Platform.find(plugin, new Path(translations));
					if (url != null) {
						bundle= new PropertyResourceBundle(bundleURL.openStream());
					}
				}
				
				InputStream stream= new BufferedInputStream(url.openStream());
				TemplateReaderWriter reader= new TemplateReaderWriter();
				TemplatePersistenceData[] datas= reader.read(stream, bundle);
				for (int i= 0; i < datas.length; i++) {
					TemplatePersistenceData data= datas[i];
					if (data.isCustom()) {
						if (data.getId() == null)
							EditorsPlugin.logErrorMessage(ContributionTemplateMessages.getString("ContributionTemplateStore.ignore_prefix") + data.getTemplate().getName() + " " + ContributionTemplateMessages.getString("ContributionTemplateStore.ignore_postfix_no_id")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						else
							EditorsPlugin.logErrorMessage(ContributionTemplateMessages.getString("ContributionTemplateStore.ignore_prefix") + data.getTemplate().getName() + " " + ContributionTemplateMessages.getString("ContributionTemplateStore.ignore_postfix_deleted")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					} else if (!validateTemplate(data.getTemplate())) {
						if (contextExists(data.getTemplate().getContextTypeId()))
							EditorsPlugin.logErrorMessage(ContributionTemplateMessages.getString("ContributionTemplateStore.ignore_prefix") + data.getTemplate().getName() + " " + ContributionTemplateMessages.getString("ContributionTemplateStore.ignore_postfix_validation_failed")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					} else {
						templates.add(data);
					}
				}
			}
		}
	}

	/**
	 * Validates a template against the context type registered in the context
	 * type registry. Returns always <code>true</code> if no registry is
	 * present.
	 * 
	 * @param template the template to validate
	 * @return <code>true</code> if validation is successful or no context
	 *         type registry is specified, <code>false</code> if validation
	 *         fails
	 */
	private boolean validateTemplate(Template template) {
		String contextTypeId= template.getContextTypeId();
		if (contextExists(contextTypeId)) {
			if (getRegistry() != null)
				try {
					getRegistry().getContextType(contextTypeId).validate(template.getPattern());
				} catch (TemplateException e) {
					return false;
				}
			return true;
		} else
			return false;
	}

	/**
	 * Returns <code>true</code> if a context type id specifies a valid context type
	 * or if no context type registry is present.
	 * 
	 * @param contextTypeId the context type id to look for
	 * @return <code>true</code> if the context type specified by the id
	 *         is present in the context type registry, or if no registry is
	 *         specified
	 */
	private boolean contextExists(String contextTypeId) {
		return contextTypeId != null && (getRegistry() == null || getRegistry().getContextType(contextTypeId) != null);
	}

	private static IConfigurationElement[] getTemplateExtensions() {
		return Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATES_EXTENSION_POINT);
	}

	private void createTemplate(Collection map, IConfigurationElement element) {
		String contextTypeId= element.getAttributeAsIs(CONTEXT_TYPE_ID);
		// no need to log failures since id and name are guaranteed by the exsd 
		// specification
		if (contextExists(contextTypeId)) {
			String id= element.getAttributeAsIs(ID);
			if (isValidTemplateId(id)) {
				
				String name= element.getAttribute(NAME);
				if (name != null) {
					
					String desc= element.getAttribute(DESCRIPTION);
					if (desc == null)
						desc= new String();
					
					String pattern= element.getChildren(PATTERN)[0].getValue();
					if (pattern != null) {
						
						Template template= new Template(name, desc, contextTypeId, pattern);
						TemplatePersistenceData data= new TemplatePersistenceData(template, true, id);
						if (validateTemplate(template))
							map.add(data);
					}
				}
			}
		}
	}
	
	private static boolean isValidTemplateId(String id) {
		return id != null && id.trim().length() != 0; // TODO test validity?
	}
}

