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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.templates.Template;

/**
 * Manages templates. Handles reading default templates contributed via XML and
 * user-defined (or overridden) templates stored in the preferences. Clients may
 * instantiate this class.
 * 
 * @since 3.0
 */
public final class TemplateStore {
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

	/** The stored templates. */
	private final List fTemplates= new ArrayList();
	
	private IPath fCustomSaveLocation= new Path(""); //$NON-NLS-1$

	
	/**
	 * Sets the save location for custom templates. May be removed by a preferences mechanism.
	 * XXX to be changed
	 * @param customSaveLocation
	 */
	public void setCustomSaveLocation(IPath customSaveLocation) {
		fCustomSaveLocation= customSaveLocation;
	}

	/**
	 * Loads the templates from contributions and preferences.
	 */
	public void load() {
		fTemplates.clear();
		loadDefaultTemplates();
		loadCustomTemplates();
	}
	
	/**
	 * Saves the templates to the preferences.
	 */
	public void save() {
		try {
			ArrayList custom= new ArrayList();
			for (Iterator it= fTemplates.iterator(); it.hasNext();) {
				TemplatePersistenceData data= (TemplatePersistenceData) it.next();
				if (data.isCustom() && !(data.isUserAdded() && data.isDeleted())) // don't save deleted user
					custom.add(data);
			}
			
			OutputStream stream= new FileOutputStream(fCustomSaveLocation.toFile());
			TemplateReaderWriter writer= new TemplateReaderWriter();
			writer.saveToStream((TemplatePersistenceData[]) custom.toArray(new TemplatePersistenceData[custom.size()]), stream);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a template encapsulated in its persistent form.
	 *  
	 * @param data the template to add
	 */
	public void add(TemplatePersistenceData data) {
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
			// add an id which is not contributed as add on
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
	 * Returns all enabled templates that have not been deleted.
	 * 
	 * @return all enabled templates
	 */
	public Template[] getTemplates() {
		List templates= new ArrayList();
		for (Iterator it= fTemplates.iterator(); it.hasNext();) {
			TemplatePersistenceData data= (TemplatePersistenceData) it.next();
			if (data.isEnabled() && !data.isDeleted())
				templates.add(data.getTemplate());
		}
		
		return (Template[]) templates.toArray(new Template[templates.size()]);
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
	
	private void loadCustomTemplates() {
		try {
			File file= fCustomSaveLocation.toFile();
			if (file.exists()) {
				InputStream stream= new FileInputStream(file);
				TemplateReaderWriter reader= new TemplateReaderWriter();
				TemplatePersistenceData[] datas= reader.readFromStream(stream);
				for (int i= 0; i < datas.length; i++) {
					TemplatePersistenceData data= datas[i];
					add(data);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadDefaultTemplates() {
		IConfigurationElement[] extensions= getTemplateExtensions();
		fTemplates.addAll(readContributedTemplates(extensions));
	}
	
	private static Collection readContributedTemplates(IConfigurationElement[] extensions) {
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

	private static void readIncludedTemplates(Collection templates, IConfigurationElement element) {
		String file= element.getAttributeAsIs(FILE);
		if (file != null) {
			IPluginDescriptor descriptor= element.getDeclaringExtension().getDeclaringPluginDescriptor();
			URL url= descriptor.find(new Path(file));
			if (url != null) {
				try {
					InputStream stream= url.openStream();
					TemplateReaderWriter reader= new TemplateReaderWriter();
					TemplatePersistenceData[] datas= reader.readFromStream(stream);
					for (int i= 0; i < datas.length; i++) {
						TemplatePersistenceData data= datas[i];
						if (!data.isCustom())
							templates.add(data);
					}
				} catch (IOException e) {
					// TODO log
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO handle exception
					e.printStackTrace();
				}
			}
		}
	}

	private static IConfigurationElement[] getTemplateExtensions() {
		return Platform.getExtensionRegistry().getConfigurationElementsFor(TEMPLATES_EXTENSION_POINT);
	}

	private static void createTemplate(Collection map, IConfigurationElement element) {
		String contextTypeId= element.getAttributeAsIs(CONTEXT_TYPE_ID);
		if (contextTypeId != null) {
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
						map.add(data);
					}
				}
			}
		}
	}
	
	private static boolean isValidTemplateId(String id) {
		return id != null && id.trim().length() != 0; // TODO test validity
	}
}

