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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;

public class ContentTypeManager implements IContentTypeManager, IRegistryChangeListener {
	private static ContentTypeManager instance;
	private static final int MARK_LIMIT = 0x100;
	 public static final String PT_CONTENTTYPES= "contentTypes"; //$NON-NLS-1$	
	/*
	 * Returns the extension for a file name (omiting the leading '.'). 
	 */
	static String getFileExtension(String fileName) {
		int dotPosition = fileName.lastIndexOf('.');
		return (dotPosition == -1 || dotPosition == fileName.length() - 1) ? null : fileName.substring(dotPosition + 1);
	}
	public synchronized static IContentTypeManager getInstance() {
		if (instance != null)
			return instance;
		instance = new ContentTypeManager();
		instance.startup();
		return instance;
	}
	private Map catalog = new HashMap();
	/**
	 * Constructs a new content type manager.
	 */
	protected ContentTypeManager() {
		// to prevent external instatiation of this implementation
	}
	protected void addContentType(IContentType contentType) {
		catalog.put(contentType.getId(), contentType);
	}
	protected IContentType createContentType(String namespace, String simpleId, String name, String[] fileExtensions, String[] fileNames, String baseTypeId) {
		ContentType contentType = new ContentType(this);
		contentType.setSimpleId(simpleId);
		contentType.setNamespace(namespace);
		contentType.setName(name);
		contentType.setFileExtensions(fileExtensions);
		contentType.setFileNames(fileNames);
		contentType.setBaseTypeId(baseTypeId);
		return contentType;
	}
	/**
	 * A content type will be valid if:
	 * <ol>
	 * <li>it does not designate a base type, or</li>
	 * <li>it designates a base type that exists and is valid</li>
	 * </ol> 
	 */
	private void ensureValid(ContentType type) {
		if (type.getValidation() != ContentType.UNKNOWN)
			return;
		if (type.getBaseTypeId() == null) {
			type.setValidation(ContentType.VALID);
			return;
		}
		ContentType baseType = (ContentType) catalog.get(type.getBaseTypeId());
		if (baseType == null) {
			type.setValidation(ContentType.INVALID);
			return;
		}
		// set this temporarily as invalid to prevent cycles
		// all types in the cycle will be marked as invalid
		type.setValidation(ContentType.INVALID);
		ensureValid(baseType);
		// base type is either be valid or invalid - type will have the same status
		type.setValidation(baseType.getValidation());
	}
	/**
	 * @see IContentTypeManager
	 */
	public IContentType findContentTypeFor(InputStream contents, IContentType[] subset) throws IOException {
		if (subset == null)
			subset = getAllContentTypes();
		List appropriate = new ArrayList();
		for (int i = 0; i < subset.length; i++) {
			IContentDescriber describer = ((ContentType) subset[i]).getDescriber();
			if (describer != null) {
				contents.mark(MARK_LIMIT);
				if (describer.describe(contents, null, 0))
					appropriate.add(subset[i]);
				contents.reset();
			}
		}
		return mostAppropriate(appropriate);
	}
	/**
	 * @see IContentTypeManager
	 */
	public IContentType findContentTypeForFileName(String fileName) {
		// basic implementation just gets all content types		
		IContentType[] associated = findContentTypesForFileName(fileName);
		return associated.length == 0 ? null : associated[1];
	}
	/**
	 * @see IContentTypeManager
	 */	
	public IContentType[] findContentTypesFor(InputStream contents, IContentType[] subset) throws IOException {
		if (subset == null)
			subset = getAllContentTypes();
		List appropriate = new ArrayList();
		for (int i = 0; i < subset.length; i++) {
			IContentDescriber describer = ((ContentType) subset[i]).getDescriber();
			if (describer != null && describer.describe(contents, null, 0))
				appropriate.add(subset[i]);
		}
		return (IContentType[]) appropriate.toArray(new IContentType[appropriate.size()]);
	}
	/**
	 * @see IContentTypeManager
	 */	
	public IContentType[] findContentTypesForFileName(String fileName) {
		Set types = new HashSet();
		for (Iterator iter = catalog.values().iterator(); iter.hasNext();) {
			ContentType contentType = (ContentType) iter.next();
			if (contentType.isValid() && indexOf(contentType.getFileNames(), fileName) >= 0)
				types.add(contentType);
		}
		String fileExtension = getFileExtension(fileName);
		if (fileExtension != null)
			for (Iterator iter = catalog.values().iterator(); iter.hasNext();) {
				ContentType contentType = (ContentType) iter.next();
				if (contentType.isValid() && indexOf(contentType.getFileExtensions(), fileExtension) >= 0)
					types.add(contentType);
			}
		return (IContentType[]) types.toArray(new IContentType[types.size()]);
	}
	/**
	 * @see IContentTypeManager
	 */	
	public IContentType[] getAllContentTypes() {
		List result = new ArrayList(catalog.size());
		for (Iterator i = catalog.values().iterator(); i.hasNext();) {
			ContentType type = (ContentType) i.next();
			if (type.isValid())
				result.add(type);
		}
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}
	/**
	 * @see IContentTypeManager
	 */	
	public IContentType getContentType(String contentTypeIdentifier) {
		ContentType type = (ContentType) catalog.get(contentTypeIdentifier);
		return (type != null && type.isValid()) ? type : null;
	}
	/**
	 * @see IContentTypeManager
	 */
	public IContentDescription getDescriptionFor(InputStream contents, IContentType[] subset, int optionsMask) throws IOException {
		// naïve implementation for now
		IContentType selected = findContentTypeFor(contents, subset);
		if (selected == null)
			return null;
		ContentDescription description = new ContentDescription();
		description.setContentType(selected);
		// TODO less-than-optimal: causes stream to be read again
		IContentDescriber selectedDescriber = ((ContentType) selected).getDescriber();
		if (selectedDescriber != null && (selectedDescriber.getSupportedOptions() & optionsMask) == optionsMask) {
			contents.mark(MARK_LIMIT);
			selectedDescriber.describe(contents, description, optionsMask);			
			contents.reset();
		}
		// check if any of the defaults need to be applied
		if ((optionsMask & IContentDescription.CHARSET) != 0 && description.getCharset() == null)
			description.setCharset(selected.getDefaultCharset());
		description.markAsImmutable();
		return description;
	}
	private int indexOf(Object[] array, Object object) {
		for (int i = 0; i < array.length; i++)
			if (array[i].equals(object))
				return i;
		return -1;
	}
	private boolean isBaseTypeOf(IContentType base, IContentType target) {
		IContentType targetBase = target.getBaseType();
		if (targetBase == null)
			return false;
		return targetBase == base ? true : isBaseTypeOf(base, targetBase);
	}
	private int moreAppropriate(IContentType type1, IContentType type2) {
		return isBaseTypeOf(type1, type2) ? -1 : (isBaseTypeOf(type2, type1) ? 1 : 0);
	}
	//TODO: should take any user-defined precedences into account
	//TODO: should pick a common ancestor if two different specific types are deemed appropriate
	private IContentType mostAppropriate(List candidates) {
		int candidatesCount = candidates.size();
		if (candidatesCount == 0)
			return null;
		IContentType chosen = (IContentType) candidates.remove(candidatesCount - 1);
		if (candidatesCount == 1)
			return chosen;
		for (Iterator i = candidates.iterator(); i.hasNext();) {
			IContentType current = (IContentType) i.next();
			i.remove();
			switch (moreAppropriate(current, chosen)) {
				case -1 :
					// currently chosen is more appropriate
					break;
				case 1 :
					// a more appropriate content type has been found - elect it as the new chosen one
					chosen = current;
					continue;
				default :
					// two candidates are equally appropriate - cannot choose
					return null;
			}
		}
		return chosen;
	}
	private String[] parseItems(String string) {
		if (string == null)
			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(string, ","); //$NON-NLS-1$
		if (!tokenizer.hasMoreTokens())
			return new String[0];
		String first = tokenizer.nextToken();
		if (!tokenizer.hasMoreTokens())
			return new String[]{first};
		ArrayList items = new ArrayList();
		items.add(first);
		do {
			items.add(tokenizer.nextToken());
		} while (tokenizer.hasMoreTokens());
		return (String[]) items.toArray(new String[items.size()]);
	}
	protected void registerContentType(IConfigurationElement contentTypeCE) {
		//TODO: need to ensure the config. element is valid
		ContentType contentType = new ContentType(this, contentTypeCE);
		contentType.setSimpleId(contentTypeCE.getAttributeAsIs("id")); //$NON-NLS-1$
		contentType.setNamespace(contentTypeCE.getDeclaringExtension().getNamespace());
		contentType.setName(contentTypeCE.getAttribute("name")); //$NON-NLS-1$
		contentType.setFileExtensions(parseItems(contentTypeCE.getAttributeAsIs("file-extensions"))); //$NON-NLS-1$
		contentType.setFileNames(parseItems(contentTypeCE.getAttributeAsIs("file-names"))); //$NON-NLS-1$
		contentType.setBaseTypeId(contentTypeCE.getAttributeAsIs("base-type")); //$NON-NLS-1$
		contentType.setDefaultCharset(contentTypeCE.getAttributeAsIs("default-charset")); //$NON-NLS-1$
		contentType.setDescriberClass(contentTypeCE.getAttributeAsIs("describer-class") != null); //$NON-NLS-1$
		catalog.put(contentType.getId(), contentType);
	}
	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] deltas = (event.getExtensionDeltas(IPlatform.PI_RUNTIME, PT_CONTENTTYPES));
		for (int i = 0; i < deltas.length; i++) {
			IConfigurationElement[] configElements = deltas[i].getExtension().getConfigurationElements();
			if (deltas[i].getKind() == IExtensionDelta.ADDED)
				for (int j = 0; j < configElements.length; j++)
					registerContentType(configElements[j]);
			else {
				//TODO should unregister removed types
			}
		}
		// remove orphan types / cycles
		validateContentTypes();
	}
	protected void startup() {
		IExtensionRegistry registry = InternalPlatform.getDefault().getRegistry();
		InternalPlatform.getDefault().getRegistry().addRegistryChangeListener(this, IPlatform.PI_RUNTIME);
		catalog = new HashMap();
		IExtensionPoint contentTypesXP = registry.getExtensionPoint(IPlatform.PI_RUNTIME, PT_CONTENTTYPES);
		IConfigurationElement[] allContentTypeCEs = contentTypesXP.getConfigurationElements();
		for (int j = 0; j < allContentTypeCEs.length; j++)
			registerContentType(allContentTypeCEs[j]);
		validateContentTypes();
	}
	protected void validateContentTypes() {
		// forget the validation status for all content types
		for (Iterator i = catalog.values().iterator(); i.hasNext();)
			((ContentType) i.next()).setValidation(ContentType.UNKNOWN);
		for (Iterator i = catalog.values().iterator(); i.hasNext();) {
			ensureValid((ContentType) i.next());
		}
	}
}