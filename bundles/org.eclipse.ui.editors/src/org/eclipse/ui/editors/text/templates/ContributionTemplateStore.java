/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
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
 *     Stephan Wahlbrink <stephan.wahlbrink@walware.de> - [templates] improve logging when reading templates into ContributionTemplateStore - https://bugs.eclipse.org/bugs/show_bug.cgi?id=212252
 *******************************************************************************/
package org.eclipse.ui.editors.text.templates;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import org.eclipse.text.templates.TemplatePersistenceData;
import org.eclipse.text.templates.TemplateReaderWriter;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.editors.text.NLSUtility;


/**
 * Manages templates. Handles reading default templates contributed via XML and
 * user-defined (or overridden) templates stored in the preferences.
 * <p>
 * Clients may instantiate but not subclass this class.
 * </p>
 *
 * @since 3.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ContributionTemplateStore extends TemplateStore {
	/* extension point string literals */
	private static final String TEMPLATES_EXTENSION_POINT= "org.eclipse.ui.editors.templates"; //$NON-NLS-1$

	private static final String ID= "id"; //$NON-NLS-1$
	private static final String NAME= "name"; //$NON-NLS-1$

	private static final String CONTEXT_TYPE_ID= "contextTypeId"; //$NON-NLS-1$
	private static final String DESCRIPTION= "description"; //$NON-NLS-1$
	private static final String AUTO_INSERT= "autoinsert"; //$NON-NLS-1$

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
	 */
	@Override
	protected void loadContributedTemplates() throws IOException {
		IConfigurationElement[] extensions= getTemplateExtensions();
		for (TemplatePersistenceData data : readContributedTemplates(extensions)) {
			internalAdd(data);
		}
	}

	private Collection<TemplatePersistenceData> readContributedTemplates(IConfigurationElement[] extensions) throws IOException {
		Collection<TemplatePersistenceData> templates= new ArrayList<>();
		for (IConfigurationElement extension : extensions) {
			if (extension.getName().equals(TEMPLATE)) {
				createTemplate(templates, extension);
			} else if (extension.getName().equals(INCLUDE)) {
				readIncludedTemplates(templates, extension);
			}
		}

		return templates;
	}

	private void readIncludedTemplates(Collection<TemplatePersistenceData> templates, IConfigurationElement element) throws IOException {
		String file= element.getAttribute(FILE);
		if (file != null) {
			Bundle plugin = Platform.getBundle(element.getContributor().getName());
			URL url= FileLocator.find(plugin, IPath.fromOSString(file), null);
			if (url != null) {
				ResourceBundle bundle= null;
				String translations= element.getAttribute(TRANSLATIONS);
				if (translations != null) {
					URL bundleURL= FileLocator.find(plugin, IPath.fromOSString(translations), null);
					if (bundleURL != null) {
						try (InputStream bundleStream= bundleURL.openStream()) {
							bundle= new PropertyResourceBundle(bundleStream);
						}
					}
				}

				try (InputStream stream= new BufferedInputStream(url.openStream())) {
					TemplateReaderWriter reader= new TemplateReaderWriter();
					TemplatePersistenceData[] datas= reader.read(stream, bundle);
					for (TemplatePersistenceData data : datas) {
						if (data.isCustom()) {
							if (data.getId() == null)
								EditorsPlugin.logErrorMessage(NLSUtility.format(ContributionTemplateMessages.ContributionTemplateStore_ignore_no_id, data.getTemplate().getName()));
							else
								EditorsPlugin.logErrorMessage(NLSUtility.format(ContributionTemplateMessages.ContributionTemplateStore_ignore_deleted, data.getTemplate().getName()));
						} else if (validateTemplate(data.getTemplate())) {
							templates.add(data);
						}
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
		if (!contextExists(contextTypeId))
			return false;

		if (getRegistry() != null) {
			try {
				getRegistry().getContextType(contextTypeId).validate(template.getPattern());
			} catch (TemplateException e) {
				EditorsPlugin.log(NLSUtility.format(ContributionTemplateMessages.ContributionTemplateStore_ignore_validation_failed, template.getName()), e);
				return false;
			}
		}
		return true;
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

	private void createTemplate(Collection<TemplatePersistenceData> map, IConfigurationElement element) {
		String contextTypeId= element.getAttribute(CONTEXT_TYPE_ID);
		// log failures since extension point id and name are mandatory
		if (contextExists(contextTypeId)) {
			String id= element.getAttribute(ID);
			if (isValidTemplateId(id)) {

				String name= element.getAttribute(NAME);
				if (name != null) {

					String pattern= element.getChildren(PATTERN)[0].getValue();
					if (pattern != null) {

						String desc= element.getAttribute(DESCRIPTION);
						if (desc == null)
							desc= ""; //$NON-NLS-1$

						String autoInsert= element.getAttribute(AUTO_INSERT);
						boolean bAutoInsert;
						if (autoInsert == null)
							bAutoInsert= true;
						else
							bAutoInsert= Boolean.parseBoolean(autoInsert);

						Template template= new Template(name, desc, contextTypeId, pattern, bAutoInsert);
						TemplatePersistenceData data= new TemplatePersistenceData(template, true, id);
						if (validateTemplate(template))
							map.add(data);
					}
				}
			}
		}
	}

	private static boolean isValidTemplateId(String id) {
		return id != null && !id.trim().isEmpty(); // TODO test validity?
	}

	@Override
	protected void handleException(IOException x) {
		EditorsPlugin.log(x);
	}
}

