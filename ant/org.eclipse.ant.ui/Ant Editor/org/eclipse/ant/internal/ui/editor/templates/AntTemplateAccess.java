/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.editor.templates;

import java.io.IOException;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;


/**
 *  
 */

public class AntTemplateAccess {
	/** Key to store custom templates. */
	private static final String CUSTOM_TEMPLATES_KEY= "org.eclipse.ant.ui.customtemplates"; //$NON-NLS-1$
	
	/** The shared instance. */
	private static AntTemplateAccess fInstance;
	
	/** The template store. */
	private TemplateStore fStore;
	
	/** The context type registry. */
	private ContextTypeRegistry fRegistry;
	
	private AntTemplateAccess() {
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static AntTemplateAccess getDefault() {
		if (fInstance == null)
			fInstance= new AntTemplateAccess();
		return fInstance;
	}

	/**
	 * Returns this plug-in's template store.
	 * 
	 * @return the template store of this plug-in instance
	 */
	public TemplateStore getTemplateStore() {
		if (fStore == null) {
			fStore= new TemplateStore(AntUIPlugin.getDefault().getPreferenceStore(), CUSTOM_TEMPLATES_KEY);
			try {
				fStore.load();
			} catch (IOException e) {
				AntUIPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, "org.eclipse.ant.ui", IStatus.OK, "", e)); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return fStore;
	}

	/**
	 * Returns this plug-in's context type registry.
	 * 
	 * @return the context type registry for this plug-in instance
	 */
	public ContextTypeRegistry getContextTypeRegistry() {
		if (fRegistry == null) {
			// create and configure the contexts available in the template editor
			fRegistry= new ContextTypeRegistry();
			fRegistry.addContextType(XMLContextType.XML_CONTEXT_TYPE);
		}
		return fRegistry;
	}

	public IPreferenceStore getPreferenceStore() {	    
		return AntUIPlugin.getDefault().getPreferenceStore();
	}

	public void savePluginPreferences() {
		AntUIPlugin.getDefault().savePluginPreferences();
	}
}
