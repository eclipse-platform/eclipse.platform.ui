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

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPlatform;
import org.eclipse.core.runtime.content.*;
import org.osgi.service.prefs.Preferences;

public class ContentTypeManager implements IContentTypeManager {
	class ContentTypeComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			ContentType type1 = (ContentType) o1;
			ContentType type2 = (ContentType) o2;
			if (type1.getPriority() != type2.getPriority())
				return type1.getPriority() - type2.getPriority();
			// to ensure stability
			return type1.getId().compareTo(type2.getId());
		}
	}

	final static String CONTENT_TYPE_PREF_NODE = IPlatform.PI_RUNTIME + IPath.SEPARATOR + "content-types"; //$NON-NLS-1$	
	private static ContentTypeManager instance;
	private static final int MARK_LIMIT = 0x400;
	private ContentTypeBuilder builder;
	private Map catalog = new HashMap();
	private Comparator comparator = new ContentTypeComparator();
	private Map fileExtensions = new HashMap();
	private Map fileNames = new HashMap();

	/**
	 * Constructs a new content type manager.
	 */
	protected ContentTypeManager() {
		builder = createBuilder();
	}

	protected void addContentType(IContentType contentType) {
		catalog.put(contentType.getId(), contentType);
	}

	private void addFileSpecContributor(IContentType contentType, int fileSpecType, Map fileSpecsMap) {
		String[] fileSpecs = contentType.getFileSpecs(fileSpecType);
		for (int i = 0; i < fileSpecs.length; i++) {
			Set existing = (Set) fileSpecsMap.get(fileSpecs[i]);
			if (existing == null)
				fileSpecsMap.put(fileSpecs[i], existing = new TreeSet(comparator));
			existing.add(contentType);
		}
	}

	protected ContentTypeBuilder createBuilder() {
		return new ContentTypeBuilder(this);
	}

	private int describe(final IContentDescriber selectedDescriber, ByteArrayInputStream contents, ContentDescription description, int optionsMask) throws IOException {
		try {
			return selectedDescriber.describe(contents, description, optionsMask);
		} finally {
			contents.reset();
		}
	}

	/**
	 * A content type will be valid if:
	 * <ol>
	 * <li>it does not designate a base type, or</li>
	 * <li>it designates a base type that exists and is valid</li>
	 * </ol> 
	 */
	private boolean ensureValid(ContentType type) {
		if (type.getValidation() != ContentType.UNKNOWN)
			return type.isValid();
		if (type.getBaseTypeId() == null) {
			type.setValidation(ContentType.VALID);
			return true;
		}
		ContentType baseType = (ContentType) catalog.get(type.getBaseTypeId());
		if (baseType == null) {
			type.setValidation(ContentType.INVALID);
			return false;
		}
		// set this temporarily as invalid to prevent cycles
		// all types in the cycle will be marked as invalid
		type.setValidation(ContentType.INVALID);
		ensureValid(baseType);
		// base type is either be valid or invalid - type will have the same status
		type.setValidation(baseType.getValidation());
		return type.isValid();
	}

	/*
	 * "public" to make testing easier 
	 */
	public IContentType findContentTypeFor(InputStream contents, IContentType[] subset) throws IOException {
		ByteArrayInputStream buffer = readBuffer(contents);
		if (buffer == null)
			return null;
		if (subset == null)
			subset = getAllContentTypes();
		List appropriate = new ArrayList();
		for (int i = 0; i < subset.length; i++) {
			buffer.reset();
			IContentDescriber describer = ((ContentType) subset[i]).getDescriber();
			if (describer != null)
				if (describe(describer, buffer, null, 0) == IContentDescriber.INVALID)
					continue;
			appropriate.add(subset[i]);
		}
		return mostAppropriate(appropriate);
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType findContentTypeFor(InputStream contents, String fileName) throws IOException {
		IContentType[] all = findContentTypesFor(contents, fileName);
		return all.length > 0 ? all[0] : null;
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType findContentTypeFor(String fileName) {
		// basic implementation just gets all content types		
		IContentType[] associated = findContentTypesFor(fileName);
		return associated.length == 0 ? null : associated[1];
	}

	/*
	 * "public" to make testing easier 
	 */
	public IContentType[] findContentTypesFor(InputStream contents, IContentType[] subset) throws IOException {
		if (subset.length == 0)
			return new IContentType[0];
		ByteArrayInputStream buffer = readBuffer(contents);
		if (buffer == null)
			return new IContentType[0];
		return internalFindContentTypesFor(buffer, subset);
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException {
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		ByteArrayInputStream buffer = readBuffer(contents);
		if (buffer == null)
			return new IContentType[0];
		return internalFindContentTypesFor(buffer, subset);
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType[] findContentTypesFor(String fileName) {
		//TODO: should include children
		IContentType[] result = new IContentType[2];
		int count = 0;
		// files associated by name should appear before those associated by extension		
		SortedSet allByFileName = (SortedSet) fileNames.get(fileName);
		if (allByFileName != null && !allByFileName.isEmpty())
			result[count++] = (IContentType) ((ContentType) allByFileName.first()).getTarget();
		String fileExtension = getFileExtension(fileName);
		if (fileExtension != null) {
			SortedSet allByFileExtension = (SortedSet) fileExtensions.get(fileExtension);
			if (allByFileExtension != null && !allByFileExtension.isEmpty())
				result[count++] = (IContentType) ((ContentType) allByFileExtension.first()).getTarget();
		}
		if (count == 0)
			return new IContentType[0];
		if (count == 1 || result[0] == result[1])
			return new IContentType[] {result[0]};
		return result;
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
		ContentType type = internalGetContentType(contentTypeIdentifier);
		return (type != null && type.isValid()) ? type : null;
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentDescription getDescriptionFor(InputStream contents, String fileName, int optionsMask) throws IOException {
		// naïve implementation for now
		ByteArrayInputStream buffer = readBuffer(contents);
		if (buffer == null)
			return null;
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		IContentType[] selected = internalFindContentTypesFor(buffer, subset);
		if (selected.length == 0)
			return null;
		ContentDescription description = new ContentDescription();
		description.setContentType(selected[0]);
		// TODO less-than-optimal: causes stream to be read again
		IContentDescriber selectedDescriber = ((ContentType) selected[0]).getDescriber();
		if (selectedDescriber != null && (selectedDescriber.getSupportedOptions() & optionsMask) == optionsMask)
			describe(selectedDescriber, buffer, description, optionsMask);
		// check if any of the defaults need to be applied
		if ((optionsMask & IContentDescription.CHARSET) != 0 && description.getCharset() == null)
			description.setCharset(selected[0].getDefaultCharset());
		description.markAsImmutable();
		return description;
	}

	Preferences getPreferences() {
		return InternalPlatform.getDefault().getPreferencesService().getRootNode().node(CONTENT_TYPE_PREF_NODE);
	}

	private IContentType[] internalFindContentTypesFor(ByteArrayInputStream buffer, IContentType[] subset) throws IOException {
		List appropriate = new ArrayList();
		int valid = 0;
		for (int i = 0; i < subset.length; i++) {
			IContentDescriber describer = ((ContentType) subset[i]).getDescriber();
			int status = IContentDescriber.INDETERMINATE;
			if (describer != null) {
				status = describe(describer, buffer, null, 0);
				if (status == IContentDescriber.INVALID)
					continue;
			}
			if (status == IContentDescriber.VALID)
				appropriate.add(valid++, subset[i]);
			else
				appropriate.add(subset[i]);
		}
		return (IContentType[]) appropriate.toArray(new IContentType[appropriate.size()]);
	}

	ContentType internalGetContentType(String contentTypeIdentifier) {
		return (ContentType) catalog.get(contentTypeIdentifier);
	}

	private boolean isBaseTypeOf(IContentType base, IContentType target) {
		IContentType targetBase = target.getBaseType();
		if (targetBase == null)
			return false;
		return targetBase == base ? true : isBaseTypeOf(base, targetBase);
	}

	private void makeAliases(Map fileSpecs) {
		// process all content types per file specs
		// marking conflicting content types as aliases 
		// for the higher priority content type
		for (Iterator i = fileSpecs.values().iterator(); i.hasNext();) {
			Set associated = (Set) i.next();
			if (associated.size() < 2)
				// no conflicts here
				continue;
			Iterator j = associated.iterator();
			ContentType ellected = (ContentType) j.next();
			while (j.hasNext())
				((ContentType) j.next()).setAliasTarget(ellected);
		}
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

	protected void reorganize() {
		fileExtensions.clear();
		fileNames.clear();
		// forget the validation status and aliases for all content types 
		for (Iterator i = catalog.values().iterator(); i.hasNext();) {
			ContentType type = ((ContentType) i.next());
			type.setValidation(ContentType.UNKNOWN);
			type.setAliasTarget(null);
		}
		// do the validation
		for (Iterator i = catalog.values().iterator(); i.hasNext();) {
			ContentType type = (ContentType) i.next();
			if (!ensureValid(type))
				continue;
			addFileSpecContributor(type, IContentType.FILE_EXTENSION_SPEC, fileExtensions);
			addFileSpecContributor(type, IContentType.FILE_NAME_SPEC, fileNames);
		}
		makeAliases(fileNames);
		makeAliases(fileExtensions);
	}

	protected void startup() {
		catalog = new HashMap();
		builder.startup();
		builder.buildContentTypes();
	}

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

	/*
	 * Returns null if no bytes can be read
	 */
	static ByteArrayInputStream readBuffer(InputStream contents) throws IOException {
		boolean failed = false;
		try {
			contents.mark(MARK_LIMIT);
			byte[] buffer = new byte[MARK_LIMIT];
			int read = contents.read(buffer);
			return read == -1 ? null : new ByteArrayInputStream(buffer, 0, read);
		} catch (IOException ioe) {
			failed = true;
			throw ioe;
		} finally {
			if (!failed)
				contents.reset();
		}
	}
}