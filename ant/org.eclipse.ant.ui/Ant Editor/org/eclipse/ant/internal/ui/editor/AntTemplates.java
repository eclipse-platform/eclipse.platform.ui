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
package org.eclipse.ant.internal.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.jface.text.templates.ContextType;
import org.eclipse.jface.text.templates.Template;

public class AntTemplates {
	
	private static final String EXTENSION_POINT_NAME= "antTemplates"; //$NON-NLS-1$
	
	public static final ContextType CONTEXT= new ContextType(AntTemplates.CONTEXT_NAME);
	public static final String CONTEXT_NAME= "ant"; //$NON-NLS-1$
	private static final String NAME= "name"; //$NON-NLS-1$
	private static final String DESCRIPTION= "description"; //$NON-NLS-1$
	private static final String PATTERN= "pattern"; //$NON-NLS-1$

	private static Template[] fgDefaultAntTemplates= null;
	
	private AntTemplates() {
	}
	
	/**
	 * Returns the set of Ant templates
     *
     * @returns set of Ant templates.
     */
	public static Template[] getAntTemplates() {
		if (fgDefaultAntTemplates == null) {
			IConfigurationElement[] extensions= extractExtensions();
			if (extensions == null) {
				return new Template[0];
			}
			computeDefaultTemplates(extensions);
		}
		return fgDefaultAntTemplates;
	}
	
	private static IConfigurationElement[] extractExtensions() {
		IExtensionPoint extensionPoint = AntUIPlugin.getDefault().getDescriptor().getExtensionPoint(EXTENSION_POINT_NAME);
		if (extensionPoint == null) {
			return null;
		}
		return extensionPoint.getConfigurationElements();
	}
	
	private static void computeDefaultTemplates(IConfigurationElement[] extensions) {
		List temp = new ArrayList(extensions.length);
		for (int i=0; i < extensions.length; i++) {
			IConfigurationElement element= extensions[i];
			String name = element.getAttribute(NAME);
			if (name == null) {
				continue;
			}
			String description = element.getAttribute(DESCRIPTION);
			String pattern = element.getAttribute(PATTERN);
			Template template= new Template(name, description, CONTEXT_NAME, pattern);
			temp.add(template);
		}
		fgDefaultAntTemplates= (Template[])temp.toArray(new Template[temp.size()]);
	}
}
