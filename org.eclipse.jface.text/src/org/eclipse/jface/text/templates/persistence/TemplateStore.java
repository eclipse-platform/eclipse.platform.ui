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
package org.eclipse.jface.text.templates.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.xml.sax.SAXException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.Template;

/**
 * Manages templates. Handles reading default templates contributed via XML and
 * user-defined (or overridden) templates stored in the preferences. Clients may
 * instantiate this class.
 * 
 * <p>This class will become final.</p>
 * 
 * @since 3.0
 */
public class TemplateStore {
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

	/** The stored templates. */
	private final List fTemplates= new ArrayList();
	/** The preference store. */
	private IPreferenceStore fPreferenceStore;
	/**
	 * The key into <code>fPreferenceStore</code> the value of which holds custom templates
	 * encoded as XML.
	 */
	private String fKey;
	/**
	 * The context type registry, or <code>null</code> if all templates regardless
	 * of context type should be loaded.
	 */
	private ContextTypeRegistry fRegistry;


	/**
	 * Creates a new template store.
	 * 
	 * @param store the preference store in which to store custom templates
	 *        under <code>key</code>
	 * @param key the key into <code>store</code> where to store custom
	 *        templates
	 */
	public TemplateStore(IPreferenceStore store, String key) {
		Assert.isNotNull(store);
		Assert.isNotNull(key);
		fPreferenceStore= store;
		fKey= key;
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
	public TemplateStore(ContextTypeRegistry registry, IPreferenceStore store, String key) {
		this(store, key);
		fRegistry= registry;
	}
	
	/**
	 * Loads the templates from contributions and preferences.
	 * 
	 * @throws IOException if a contributed templates file cannot be read
	 */
	public void load() throws IOException {
		fTemplates.clear();
		loadDefaultTemplates();
		loadCustomTemplates();
	}
	
	/**
	 * Saves the templates to the preferences.
	 * 
	 * @throws IOException if the templates cannot be written
	 */
	public void save() throws IOException {
		ArrayList custom= new ArrayList();
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isCustom() && !(data.isUserAdded() && data.isDeleted())) // don't save deleted user-added templates
				custom.add(data);
		}
		
		StringWriter output= new StringWriter();
		TemplateReaderWriter writer= new TemplateReaderWriter();
		writer.save((TemplatePersistenceData[]) custom.toArray(new TemplatePersistenceData[custom.size()]), output);
		
		fPreferenceStore.setValue(fKey, output.toString());
	}
	
	/**
	 * Adds a template encapsulated in its persistent form.
	 *  
	 * @param data the template to add
	 */
	public void add(TemplatePersistenceData data) {
		
		if (!validateTemplate(data.getTemplate()))
			return;
		
		if (data.isUserAdded()) {
			fTemplates.add(data);
		} else {
			for (Iterator it= fTemplates.iterator(); it.hasNext();) {
				TemplatePersistenceData d2= (TemplatePersistenceData) it.next();
				if (d2.getId() != null && d2.getId().equals(data.getId())) {
					d2.setTemplate(data.getTemplate());
					d2.setDeleted(data.isDeleted());
					d2.setEnabled(data.isEnabled());
					return;
				}
			}
			
			// add an id which is not contributed as add-on
			if (data.getTemplate() != null) {
				TemplatePersistenceData newData= new TemplatePersistenceData(data.getTemplate(), data.isEnabled());
				fTemplates.add(newData);
			}
		}
	}

	/**
	 * Removes a template from the store.
	 * 
	 * @param data the template to remove
	 */
	public void delete(TemplatePersistenceData data) {
		if (data.isUserAdded())
			fTemplates.remove(data);
		else
			data.setDeleted(true);
	}
	
	/**
	 * Restores all contributed templates that have been deleted.
	 */
	public void restoreDeleted() {
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isDeleted())
				data.setDeleted(false);
		}
	}
	
	/**
	 * Deletes all user-added templates and reverts all contributed templates.
	 */
	public void restoreDefaults() {
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isUserAdded())
				it.remove();
			else
				data.revert();
		}
	}
	
	/**
	 * Returns all enabled templates.
	 * 
	 * @return all enabled templates
	 */
	public Template[] getTemplates() {
		return getTemplates(null);
	}
	
	/**
	 * Returns all enabled templates for the given context type.
	 * 
	 * @param contextTypeId the id of the context type of the requested templates, or <code>null</code> if all templates should be returned
	 * @return all enabled templates for the given context type
	 */
	public Template[] getTemplates(String contextTypeId) {
		List templates= new ArrayList();
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isEnabled() && !data.isDeleted() && (contextTypeId == null || contextTypeId.equals(data.getTemplate().getContextTypeId())))
				templates.add(data.getTemplate());
		}
		
		return (Template[]) templates.toArray(new Template[templates.size()]);
	}
	
	/**
	 * Returns the first enabled template that matches the name.
	 *  
	 * @param name the name of the template searched for
	 * @return the first enabled template that matches both name and context type id, or <code>null</code> if none is found
	 */
	public Template findTemplate(String name) {
		return findTemplate(name, null);
	}
	
	/**
	 * Returns the first enabled template that matches both name and context type id.
	 *  
	 * @param name the name of the template searched for
	 * @param contextTypeId the context type id to clip unwanted templates, or <code>null</code> if any context type is ok
	 * @return the first enabled template that matches both name and context type id, or <code>null</code> if none is found
	 */
	public Template findTemplate(String name, String contextTypeId) {
		Assert.isNotNull(name);
		
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			Template template= data.getTemplate();
			if (data.isEnabled() && !data.isDeleted() 
					&& (contextTypeId == null || contextTypeId.equals(template.getContextTypeId()))
					&& name.equals(template.getName()))
				return template;
		}
		
		return null;
	}
	
	/**
	 * Returns all template datas.
	 * 
	 * @param includeDeleted whether to include deleted datas
	 * @return all template datas, whether enabled or not
	 */
	public TemplatePersistenceData[] getTemplateData(boolean includeDeleted) {
		List datas= new ArrayList();
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (includeDeleted || !data.isDeleted())
				datas.add(data);
		}
		
		return (TemplatePersistenceData[]) datas.toArray(new TemplatePersistenceData[datas.size()]);
	}
	
	private void loadCustomTemplates() throws IOException {
		try {
			String pref= fPreferenceStore.getString(fKey);
			if (pref != null && pref.trim().length() > 0) {
				Reader input= new StringReader(pref);
				TemplateReaderWriter reader= new TemplateReaderWriter();
				TemplatePersistenceData[] datas= reader.read(input);
				for (int i= 0; i < datas.length; i++) {
					TemplatePersistenceData data= datas[i];
					add(data);
				}
			}
		} catch (SAXException e) {
			// won't happen unless someone messes with our preferences.
			throw (IOException) new IOException("Illegal XML content: " + e.getLocalizedMessage()).fillInStackTrace(); //$NON-NLS-1$
		}
	}
	
	private void loadDefaultTemplates() throws IOException {
		IConfigurationElement[] extensions= getTemplateExtensions();
		fTemplates.addAll(readContributedTemplates(extensions));
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
			IPluginDescriptor descriptor= element.getDeclaringExtension().getDeclaringPluginDescriptor();
			URL url= descriptor.find(new Path(file));
			if (url != null) {
				try {
					ResourceBundle bundle= null;
					String translations= element.getAttributeAsIs(TRANSLATIONS);
					if (translations != null) {
						URL bundleURL= descriptor.find(new Path(translations));
						if (url != null) {
							bundle= new PropertyResourceBundle(bundleURL.openStream());
						}
					}
					
					InputStream stream= url.openStream();
					Reader input= new InputStreamReader(stream);
					TemplateReaderWriter reader= new TemplateReaderWriter();
					TemplatePersistenceData[] datas= reader.read(input, bundle);
					for (int i= 0; i < datas.length; i++) {
						TemplatePersistenceData data= datas[i];
						if (!data.isCustom() && validateTemplate(data.getTemplate()))
							templates.add(data);
					}
				} catch (SAXException e) {
					// someone contributed an xml template file with invalid syntax. Propagate as IOException
					throw new IOException("Illegal XML content: " + e.getLocalizedMessage()); //$NON-NLS-1$
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
		return contextExists(contextTypeId) && (fRegistry == null || fRegistry.getContextType(contextTypeId).validate(template.getPattern()) == null);
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
		return contextTypeId != null && (fRegistry == null || fRegistry.getContextType(contextTypeId) != null);
	}

	private static IConfigurationElement[] getTemplateExtensions() {
		return Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATES_EXTENSION_POINT);
	}

	private void createTemplate(Collection map, IConfigurationElement element) {
		String contextTypeId= element.getAttributeAsIs(CONTEXT_TYPE_ID);
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

