/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.rulers;

import java.net.URL;

import org.osgi.framework.Bundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

public final class ExtensionPointHelper {
	
	private final IConfigurationElement fElement;
	private final String fName;
	private final String fBlameMsg;
	private final ILog fLog;

	public ExtensionPointHelper(IConfigurationElement element, ILog log) throws InvalidRegistryObjectException {
		Assert.isLegal(element != null);
		Assert.isLegal(log != null);
		fLog= log;
		fElement= element;
		fName= element.getName();
		// see if we have a conventional 'id' attribute
		String id= getDefaultAttribute("id", "<unknown>"); //$NON-NLS-1$ //$NON-NLS-2$
		String extensionPointId= element.getDeclaringExtension().getExtensionPointUniqueIdentifier();
		Object[] args= { fElement.getContributor().getName(), id, extensionPointId };
		fBlameMsg= NLS.bind(RulerColumnMessages.ExtensionPointHelper_invalid_contribution_msg, args);
	}
	
	public String getDefaultAttribute(String attribute, String dflt) throws InvalidRegistryObjectException {
		String value= fElement.getAttribute(attribute);
		return value == null ? dflt : value;
	}

	public String getNonNullAttribute(String attribute) throws InvalidRegistryObjectException {
		String value= fElement.getAttribute(attribute);
		if (value == null)
			fail(NLS.bind(RulerColumnMessages.ExtensionPointHelper_missing_attribute_msg, fName, attribute));
		return value;
	}
	
	public float getDefaultAttribute(String attribute, float dflt) {
		String value= getDefaultAttribute(attribute, null);
		if (value == null)
			return dflt;
		
		try {
			return Float.valueOf(value).floatValue();
		} catch (NumberFormatException x) {
			fail(NLS.bind(RulerColumnMessages.ExtensionPointHelper_invalid_number_attribute_msg, fName, attribute));
			return dflt;
		}
	}

	public boolean getDefaultAttribute(String attribute, boolean dflt) {
		String value= getDefaultAttribute(attribute, null);
		if (value == null)
			return dflt;
		
		try {
			return Boolean.valueOf(value).booleanValue();
		} catch (NumberFormatException x) {
			fail(NLS.bind(RulerColumnMessages.ExtensionPointHelper_invalid_number_attribute_msg, fName, attribute));
			return dflt;
		}
	}
	
	public void fail(String message) throws InvalidRegistryObjectException {
		IStatus status= new Status(IStatus.WARNING, TextEditorPlugin.PLUGIN_ID, IStatus.OK, fBlameMsg + message, null);
		fLog.log(status);
		throw new InvalidRegistryObjectException();
	}

	public URL getDefaultResourceURL(String attribute, URL dflt) {
		String value= getDefaultAttribute(attribute, null);
		if (value == null)
			return dflt;

		Bundle bundle= getBundle();
		if (bundle == null)
			return dflt;

		Path path= new Path(value);
		return FileLocator.find(bundle, path, null);
	}
	
	private Bundle getBundle() {
		String namespace= fElement.getDeclaringExtension().getContributor().getName();
		Bundle bundle= Platform.getBundle(namespace);
		return bundle;
	}
}
