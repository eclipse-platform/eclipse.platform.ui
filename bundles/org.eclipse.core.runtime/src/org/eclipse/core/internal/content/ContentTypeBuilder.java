/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentType;

/**
 * This class is a sidekick for ContentTypeManager that provides mechanisms for 
 * creating content types from the extension registry (which ContentTypeManager
 *  is oblivious to).
 */
public class ContentTypeBuilder {
	public static final String PT_CONTENTTYPES = "contentTypes"; //$NON-NLS-1$	
	private ContentTypeCatalog catalog;

	private static String getUniqueId(String namespace, String baseTypeId) {
		if (baseTypeId == null)
			return null;
		int separatorPosition = baseTypeId.lastIndexOf('.');
		// base type is defined in the same namespace
		if (separatorPosition == -1)
			baseTypeId = namespace + '.' + baseTypeId;
		return baseTypeId;
	}

	private static byte parsePriority(String priority) {
		if (priority == null)
			return ContentType.PRIORITY_NORMAL;
		if (priority.equals("high")) //$NON-NLS-1$
			return ContentType.PRIORITY_HIGH;
		if (priority.equals("low")) //$NON-NLS-1$
			return ContentType.PRIORITY_LOW;
		if (!priority.equals("normal")) //$NON-NLS-1$
			return ContentType.PRIORITY_NORMAL;
		//TODO: should log - INVALID PRIORITY
		return ContentType.PRIORITY_NORMAL;
	}

	protected ContentTypeBuilder(ContentTypeCatalog catalog) {
		this.catalog = catalog;
	}

	private void addFileAssociation(IConfigurationElement fileAssociationElement, ContentType target) {
		String[] fileNames = Util.parseItems(fileAssociationElement.getAttributeAsIs("file-names")); //$NON-NLS-1$
		for (int i = 0; i < fileNames.length; i++)
			target.internalAddFileSpec(catalog, fileNames[i], IContentType.FILE_NAME_SPEC | ContentType.SPEC_PRE_DEFINED);
		String[] fileExtensions = Util.parseItems(fileAssociationElement.getAttributeAsIs("file-extensions")); //$NON-NLS-1$
		for (int i = 0; i < fileExtensions.length; i++)
			target.internalAddFileSpec(catalog, fileExtensions[i], IContentType.FILE_EXTENSION_SPEC | ContentType.SPEC_PRE_DEFINED);
	}

	/**
	 * Builds all content types found in the extension registry.
	 */
	public void buildCatalog() {
		IConfigurationElement[] allContentTypeCEs = getConfigurationElements();
		for (int i = 0; i < allContentTypeCEs.length; i++) {
			if (allContentTypeCEs[i].getName().equals("content-type")) //$NON-NLS-1$
				registerContentType(allContentTypeCEs[i]);
		}
		for (int i = 0; i < allContentTypeCEs.length; i++) {
			if (allContentTypeCEs[i].getName().equals("file-association")) //$NON-NLS-1$
				registerFileAssociation(allContentTypeCEs[i]);
		}
	}

	private ContentType createContentType(IConfigurationElement contentTypeCE) {
		//TODO: need to ensure the config. element is valid
		String simpleId = contentTypeCE.getAttributeAsIs("id"); //$NON-NLS-1$
		byte priority = parsePriority(contentTypeCE.getAttributeAsIs("priority")); //$NON-NLS-1$);
		String namespace = contentTypeCE.getDeclaringExtension().getNamespace();
		String name = contentTypeCE.getAttribute("name"); //$NON-NLS-1$
		String[] fileNames = Util.parseItems(contentTypeCE.getAttributeAsIs("file-names")); //$NON-NLS-1$
		String[] fileExtensions = Util.parseItems(contentTypeCE.getAttributeAsIs("file-extensions")); //$NON-NLS-1$
		String baseTypeId = getUniqueId(namespace, contentTypeCE.getAttributeAsIs("base-type")); //$NON-NLS-1$
		String aliasTargetTypeId = getUniqueId(namespace, contentTypeCE.getAttributeAsIs("alias-for")); //$NON-NLS-1$		
		String defaultCharset = contentTypeCE.getAttributeAsIs("default-charset"); //$NON-NLS-1$
		return ContentType.createContentType(catalog, namespace, simpleId, name, priority, fileExtensions, fileNames, baseTypeId, aliasTargetTypeId, defaultCharset, contentTypeCE);
	}

	protected IConfigurationElement[] getConfigurationElements() {
		IExtensionRegistry registry = InternalPlatform.getDefault().getRegistry();
		IExtensionPoint contentTypesXP = registry.getExtensionPoint(Platform.PI_RUNTIME, PT_CONTENTTYPES);
		IConfigurationElement[] allContentTypeCEs = contentTypesXP.getConfigurationElements();
		return allContentTypeCEs;
	}

	/* Checks whether the content type has all required pieces. */
	private boolean isComplete(ContentType contentType) {
		String message = null;
		if (contentType.getSimpleId() == null)
			message = NLS.bind(Messages.content_missingIdentifier, contentType.getId());
		else if (contentType.getName() == null)
			message = NLS.bind(Messages.content_missingName, contentType.getId());
		if (message == null)
			return true;
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, null);
		InternalPlatform.getDefault().log(status);
		return false;
	}

	private void registerContentType(IConfigurationElement contentTypeCE) {
		//TODO: need to ensure the config. element is valid
		ContentType contentType = createContentType(contentTypeCE);
		if (!isComplete(contentType))
			return;
		catalog.addContentType(contentType);
	}

	/* Adds extra file associations to existing content types. If the content 
	 * type has not been added, the file association is ignored.
	 */
	private void registerFileAssociation(IConfigurationElement fileAssociationElement) {
		//TODO: need to ensure the config. element is valid		
		String contentTypeId = getUniqueId(fileAssociationElement.getDeclaringExtension().getNamespace(), fileAssociationElement.getAttribute("content-type")); //$NON-NLS-1$
		ContentType target = catalog.internalGetContentType(contentTypeId);
		if (target == null)
			return;
		addFileAssociation(fileAssociationElement, target);
	}
}