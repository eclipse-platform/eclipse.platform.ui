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

import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.IContentType;

/**
 * This class is a sidekick for ContentTypeManager that provides mechanisms for 
 * creating content types from the extension registry (which ContentTypeManager
 *  is oblivious to).
 */
public class ContentTypeBuilder implements IRegistryChangeListener {
	public static final String PT_CONTENTTYPES = "contentTypes"; //$NON-NLS-1$	
	private ContentTypeManager catalog;
	// map content-type-id -> file association configuration element set
	private Map orphanAssociations = new HashMap();

	public ContentTypeBuilder(ContentTypeManager catalog) {
		this.catalog = catalog;
	}

	private void addFileAssociation(IConfigurationElement fileAssociationElement, ContentType target) {
		String[] fileNames = ContentType.parseItems(fileAssociationElement.getAttributeAsIs("file-names")); //$NON-NLS-1$
		for (int i = 0; i < fileNames.length; i++)
			target.internalAddFileSpec(fileNames[i], IContentType.FILE_NAME_SPEC | ContentType.PRE_DEFINED_SPEC);
		String[] fileExtensions = ContentType.parseItems(fileAssociationElement.getAttributeAsIs("file-extensions")); //$NON-NLS-1$
		for (int i = 0; i < fileExtensions.length; i++)
			target.internalAddFileSpec(fileExtensions[i], IContentType.FILE_EXTENSION_SPEC | ContentType.PRE_DEFINED_SPEC);
	}

	/**
	 * Builds all content types found in the extension registry.
	 */
	public void buildContentTypes() {
		IConfigurationElement[] allContentTypeCEs = getConfigurationElements();
		for (int i = 0; i < allContentTypeCEs.length; i++) {
			if (allContentTypeCEs[i].getName().equals("content-type")) //$NON-NLS-1$
				registerContentType(allContentTypeCEs[i]);
		}
		for (int i = 0; i < allContentTypeCEs.length; i++) {
			if (allContentTypeCEs[i].getName().equals("file-association")) //$NON-NLS-1$
				registerFileAssociation(allContentTypeCEs[i]);
		}
		validateCatalog();
	}

	public ContentType createContentType(IConfigurationElement contentTypeCE) {
		//TODO: need to ensure the config. element is valid
		String simpleId = contentTypeCE.getAttributeAsIs("id"); //$NON-NLS-1$
		byte priority = parsePriority(contentTypeCE.getAttributeAsIs("priority")); //$NON-NLS-1$);
		String namespace = contentTypeCE.getDeclaringExtension().getNamespace();
		String name = contentTypeCE.getAttribute("name"); //$NON-NLS-1$
		String[] fileNames = ContentType.parseItems(contentTypeCE.getAttributeAsIs("file-names")); //$NON-NLS-1$
		String[] fileExtensions = ContentType.parseItems(contentTypeCE.getAttributeAsIs("file-extensions")); //$NON-NLS-1$
		String baseTypeId = getUniqueId(namespace, contentTypeCE.getAttributeAsIs("base-type")); //$NON-NLS-1$
		String defaultCharset = contentTypeCE.getAttributeAsIs("default-charset"); //$NON-NLS-1$
		return ContentType.createContentType(catalog, namespace, simpleId, name, priority, fileExtensions, fileNames, baseTypeId, defaultCharset, contentTypeCE);
	}

	protected IConfigurationElement[] getConfigurationElements() {
		IExtensionRegistry registry = InternalPlatform.getDefault().getRegistry();
		IExtensionPoint contentTypesXP = registry.getExtensionPoint(IPlatform.PI_RUNTIME, PT_CONTENTTYPES);
		IConfigurationElement[] allContentTypeCEs = contentTypesXP.getConfigurationElements();
		return allContentTypeCEs;
	}

	private byte parsePriority(String priority) {
		if (priority == null)
			return ContentType.NORMAL;
		if (priority.equals("high")) //$NON-NLS-1$
			return ContentType.HIGH;
		if (priority.equals("low")) //$NON-NLS-1$
			return ContentType.LOW;
		if (!priority.equals("normal")) //$NON-NLS-1$
			return ContentType.NORMAL;
		//TODO: should log - INVALID PRIORITY
		return ContentType.NORMAL;
	}

	protected void registerContentType(IConfigurationElement contentTypeCE) {
		//TODO: need to ensure the config. element is valid
		ContentType contentType = createContentType(contentTypeCE);
		catalog.addContentType(contentType);
		// ensure orphan associations are added		
		Set orphans = (Set) orphanAssociations.remove(contentType.getId());
		if (orphans == null)
			return;
		for (Iterator iter = orphans.iterator(); iter.hasNext();)
			addFileAssociation((IConfigurationElement) iter.next(), contentType);
	}

	/* Adds extra file associations to existing content types. If the content 
	 * type has not been added, the file association is ignored.
	 */
	private void registerFileAssociation(IConfigurationElement fileAssociationElement) {
		//TODO: need to ensure the config. element is valid		
		String contentTypeId = getUniqueId(fileAssociationElement.getDeclaringExtension().getNamespace(), fileAssociationElement.getAttribute("content-type")); //$NON-NLS-1$
		ContentType target = catalog.internalGetContentType(contentTypeId);
		if (target == null) {
			// the content type is not available yet... remember it as orphan 
			Set orphans = (Set) orphanAssociations.get(contentTypeId);
			if (orphans == null)
				orphanAssociations.put(contentTypeId, orphans = new HashSet(3));
			orphans.add(fileAssociationElement);
			return;
		}
		addFileAssociation(fileAssociationElement, target);
	}

	private static String getUniqueId(String namespace, String baseTypeId) {
		if (baseTypeId == null)
			return null;
		int separatorPosition = baseTypeId.lastIndexOf('.');
		// base type is defined in the same namespace
		if (separatorPosition == -1)
			baseTypeId = namespace + '.' + baseTypeId;
		return baseTypeId;
	}

	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = (event.getExtensionDeltas(IPlatform.PI_RUNTIME, PT_CONTENTTYPES));
		for (int i = 0; i < deltas.length; i++) {
			IConfigurationElement[] configElements = deltas[i].getExtension().getConfigurationElements();
			if (deltas[i].getKind() == IExtensionDelta.ADDED) {
				for (int j = 0; i < configElements.length; i++)
					if (configElements[j].getName().equals("content-type"))//$NON-NLS-1$
						registerContentType(configElements[j]);
				for (int j = 0; i < configElements.length; i++)
					if (configElements[j].getName().equals("file-association")) //$NON-NLS-1$
						registerFileAssociation(configElements[j]);
			} else {
				//TODO should unregister removed types
				//TODO remove any involved orphans as well
			}
		}
		// ensure there are no orphan types / cycles
		validateCatalog();
	}

	public void startup() {
		InternalPlatform.getDefault().getRegistry().addRegistryChangeListener(this, IPlatform.PI_RUNTIME);
	}

	protected void validateCatalog() {
		catalog.reorganize();
	}
}