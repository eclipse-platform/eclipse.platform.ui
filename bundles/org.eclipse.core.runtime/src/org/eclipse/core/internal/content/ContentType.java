/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPlatform;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.runtime.content.IContentType;

public class ContentType implements IContentType {
	final static byte INVALID = 2;
	final static byte UNKNOWN = 3;
	final static byte VALID = 1;
	private String baseTypeId;
	private IConfigurationElement configurationElement;
	private String defaultCharset;
	private boolean hasDescriberClass;
	private boolean failedDescriberCreation;	
	private String[] fileExtensions;
	private String[] fileNames;
	private ContentTypeManager manager;
	private String mimeType;
	private String name;
	private String namespace;
	private String simpleId;
	private byte validation;
	public ContentType(ContentTypeManager manager) {
		this.manager = manager;
	}
	public ContentType(ContentTypeManager manager, IConfigurationElement configurationElement) {
		this.manager = manager;
		this.configurationElement = configurationElement;
	}	
	public IContentType getBaseType() {
		if (baseTypeId == null)
			return null;
		return manager.getContentType(baseTypeId);
	}
	String getBaseTypeId() {
		return baseTypeId;
	}
	public String getDefaultCharset() {
		return defaultCharset;
	}
	public String getDefaultFileExtension() {
		// TODO Auto-generated method stub
		return null;
	}
	public IContentDescriber getDescriber() {
		if (!failedDescriberCreation && hasDescriberClass)
			try {
				return (IContentDescriber) configurationElement.createExecutableExtension("describer-class"); //$NON-NLS-1$
			} catch (CoreException ce) {
				// ensure this is logged once during a session
				failedDescriberCreation = true;
				String message = Policy.bind("content.invalidContentDescriber", getId()); //$NON-NLS-1$ 
				IStatus status = new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, ce); 
				InternalPlatform.getDefault().log(status);			
			}
		ContentType baseType = (ContentType) getBaseType();
		return baseType == null ? null : baseType.getDescriber();		
	}
	public String[] getFileExtensions() {
		return fileExtensions == null ? new String[0] : fileExtensions;
	}
	public String[] getFileNames() {
		return fileNames == null ? new String[0] : fileNames;
	}
	public String getId() {
		return namespace + '.' + simpleId; 
	}
	public IContentTypeManager getManager() {
		return manager;
	}
	public String getMIMEType() {
		return mimeType;
	}
	public String getName() {
		return name;
	}
	public String getNamespace() {
		return namespace;
	}
	byte getValidation() {
		return validation;
	}
	public boolean isAssociatedWith(String fileName) {
		if (fileNames != null)
			for (int i = 0; i < fileNames.length; i++)
				if (fileName.equalsIgnoreCase(fileNames[i]))
					return true;
		if (fileExtensions != null) {
			String fileExtension = ContentTypeManager.getFileExtension(fileName);
			if (fileExtension == null)
				return false;
			for (int i = 0; i < fileNames.length; i++)
				if (fileExtension.equalsIgnoreCase(fileExtensions[i]))
					return true;
		}
		return false;
	}
	public boolean isKindOf(IContentType another) {
		if (this == another)
			return true;
		IContentType baseType = getBaseType();
		return baseType != null && baseType.isKindOf(another);
	}
	boolean isValid() {
		return validation == VALID;
	}
	void setBaseTypeId(String baseTypeId) {
		if (baseTypeId == null) {
			this.baseTypeId = null;
			return;
		}
		int separatorPosition = baseTypeId.lastIndexOf('.');
		// base type is defined in the same namespace
		if (separatorPosition == -1)
			baseTypeId = namespace + '.' + baseTypeId;
		this.baseTypeId = baseTypeId;
	}
	public void setDefaultCharset(String defaultCharset) {
		this.defaultCharset = defaultCharset;
	}
	public void setDescriberClass(boolean hasDescriberClass) {
		this.hasDescriberClass = hasDescriberClass;
	}
	void setFileExtensions(String[] fileExtensions) {
		this.fileExtensions = fileExtensions;
	}
	void setFileNames(String[] fileNames) {
		this.fileNames = fileNames;
	}
	void setName(String name) {
		this.name = name;
	}
	void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	void setSimpleId(String simpleId) {
		this.simpleId = simpleId;
	}
	void setValidation(byte validation) {
		this.validation = validation;
	}
	public String toString() {
		return getId(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}