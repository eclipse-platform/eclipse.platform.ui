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
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.osgi.service.prefs.Preferences;

public class ContentTypeManager implements IContentTypeManager {

	private static class UncloseableCharArrayReader extends CharArrayReader {
		public UncloseableCharArrayReader(char[] buf, int offset, int length) {
			super(buf, offset, length);
		}

		public void close() {
			// don't do anything on close
		}
	}

	final static String CONTENT_TYPE_PREF_NODE = Platform.PI_RUNTIME + IPath.SEPARATOR + "content-types"; //$NON-NLS-1$
	private static final String OPTION_DEBUG_CONTENT_TYPES = Platform.PI_RUNTIME + "/contenttypes/debug"; //$NON-NLS-1$;	
	static final boolean DEBUGGING = Boolean.TRUE.toString().equalsIgnoreCase(InternalPlatform.getDefault().getOption(OPTION_DEBUG_CONTENT_TYPES));
	private static ContentTypeManager instance;
	private static final int MARK_LIMIT = 0x400;

	private ContentTypeBuilder builder;
	private Map catalog = new HashMap();

	// a comparator used when resolving conflicts (two types associated to the same spec) 
	private Comparator conflictComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			ContentType type1 = (ContentType) o1;
			ContentType type2 = (ContentType) o2;
			// first criteria: depth - the lower, the better
			int depthCriteria = type1.getDepth() - type2.getDepth();
			if (depthCriteria != 0)
				return depthCriteria;
			// second criteria: priority - the higher, the better
			int priorityCriteria = type1.getPriority() - type2.getPriority();
			if (priorityCriteria != 0)
				return -priorityCriteria;
			// to ensure stability
			return type1.getId().compareTo(type2.getId());
		}
	};

	private Comparator depthComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			return ((ContentType) o2).getDepth() - ((ContentType) o1).getDepth();
		}
	};

	private Map fileExtensions = new HashMap();

	private Map fileNames = new HashMap();

	/*
	 * Returns the extension for a file name (omiting the leading '.').
	 */
	static String getFileExtension(String fileName) {
		int dotPosition = fileName.lastIndexOf('.');
		return (dotPosition == -1 || dotPosition == fileName.length() - 1) ? null : fileName.substring(dotPosition + 1);
	}

	public synchronized static ContentTypeManager getInstance() {
		if (instance != null)
			return instance;
		instance = new ContentTypeManager();
		instance.startup();
		return instance;
	}

	/*
	 * Returns null if no bytes can be read
	 */
	protected static ByteArrayInputStream readBuffer(InputStream contents) throws IOException {
		boolean failed = false;
		try {
			if (contents.markSupported())
				contents.mark(MARK_LIMIT);
			byte[] buffer = new byte[MARK_LIMIT];
			int read = contents.read(buffer);
			return new ByteArrayInputStream(buffer, 0, read == -1 ? 0 : read);
		} catch (IOException ioe) {
			failed = true;
			throw ioe;
		} finally {
			if (!failed && contents.markSupported())
				contents.reset();
		}
	}

	protected static CharArrayReader readBuffer(Reader contents) throws IOException {
		boolean failed = false;
		try {
			if (contents.markSupported())
				contents.mark(MARK_LIMIT);
			char[] buffer = new char[MARK_LIMIT];
			int read = contents.read(buffer);
			return new UncloseableCharArrayReader(buffer, 0, read == -1 ? 0 : read);
		} catch (IOException ioe) {
			failed = true;
			throw ioe;
		} finally {
			if (!failed && contents.markSupported())
				contents.reset();
		}
	}

	/**
	 * Constructs a new content type manager.
	 */
	protected ContentTypeManager() {
		// just to set visibility 
	}

	protected void addContentType(IContentType contentType) {
		catalog.put(contentType.getId(), contentType);
	}

	private void addFileSpecContributor(IContentType contentType, int fileSpecType, Map fileSpecsMap) {
		String[] fileSpecs = contentType.getFileSpecs(fileSpecType);
		for (int i = 0; i < fileSpecs.length; i++) {
			String mappingKey = FileSpec.getMappingKeyFor(fileSpecs[i]);
			Set existing = (Set) fileSpecsMap.get(mappingKey);
			if (existing == null)
				fileSpecsMap.put(mappingKey, existing = new TreeSet(conflictComparator));
			existing.add(contentType);
		}
	}

	protected ContentTypeBuilder createBuilder() {
		return new ContentTypeBuilder(this);
	}

	/**
	 * A content type will be valid if:
	 * <ol>
	 * <li>it does not designate a base type, or</li>
	 * <li>it designates a base type that exists and is valid</li>
	 * </ol>
	 */
	private boolean ensureValid(ContentType type) {
		if (type.getValidation() != ContentType.STATUS_UNKNOWN)
			return type.isValid();
		if (type.getBaseTypeId() == null) {
			type.setValidation(ContentType.STATUS_VALID);
			return true;
		}
		ContentType baseType = (ContentType) catalog.get(type.getBaseTypeId());
		if (baseType == null) {
			type.setValidation(ContentType.STATUS_INVALID);
			return false;
		}
		// set this type temporarily as invalid to prevent cycles
		// all types in a cycle would stay as invalid
		type.setValidation(ContentType.STATUS_INVALID);
		ensureValid(baseType);
		// base type is either valid or invalid - type will have the same status
		type.setValidation(baseType.getValidation());
		return type.isValid();
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
		return associated.length == 0 ? null : associated[0];
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException {
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		ByteArrayInputStream buffer = readBuffer(contents);
		return internalFindContentTypesFor(buffer, subset);
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType[] findContentTypesFor(String fileName) {
		List result = new ArrayList(5);
		int count = 0;
		// files associated by name should appear before those associated by extension		
		SortedSet allByFileName = (SortedSet) fileNames.get(FileSpec.getMappingKeyFor(fileName));
		if (allByFileName != null && !allByFileName.isEmpty()) {
			ContentType main = ((ContentType) allByFileName.first()).getTarget();
			result.add(count++, main);
			IContentType[] children = main.getChildren();
			for (int i = 0; i < children.length; i++) {
				ContentType child = (ContentType) children[i];
				// must avoid duplicates and ensure children do not override filespecs
				if (child.internalIsAssociatedWith(fileName) == ContentType.ASSOCIATED_BY_NAME && !result.contains(child))
					result.add(count++, child);
			}
		}

		String fileExtension = getFileExtension(fileName);
		if (fileExtension != null) {
			SortedSet allByFileExtension = (SortedSet) fileExtensions.get(FileSpec.getMappingKeyFor(fileExtension));
			if (allByFileExtension != null && !allByFileExtension.isEmpty()) {
				ContentType main = ((ContentType) allByFileExtension.first()).getTarget();
				if (!result.contains(main)) {
					result.add(count++, main);
					IContentType[] children = main.getChildren();
					for (int i = 0; i < children.length; i++) {
						ContentType child = (ContentType) children[i];
						if (child.internalIsAssociatedWith(fileName) == ContentType.ASSOCIATED_BY_EXTENSION && !result.contains(children[i]))
							result.add(count++, children[i]);
					}
				}
			}
		}
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType[] getAllContentTypes() {
		List result = new ArrayList(catalog.size());
		for (Iterator i = catalog.values().iterator(); i.hasNext();) {
			ContentType type = (ContentType) i.next();
			if (type.isValid() && !type.isAlias())
				result.add(type);
		}
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}

	//TODO need better performance here
	public IContentType[] getChildren(IContentType parent) {
		List result = new ArrayList(5);
		for (Iterator i = this.catalog.values().iterator(); i.hasNext();) {
			IContentType next = (IContentType) i.next();
			if (next != parent && next.isKindOf(parent))
				result.add(next);
		}
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentType getContentType(String contentTypeIdentifier) {
		ContentType type = internalGetContentType(contentTypeIdentifier);
		return (type != null && type.isValid() && !type.isAlias()) ? type : null;
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentDescription getDescriptionFor(InputStream contents, String fileName, QualifiedName[] options) throws IOException {
		// naive implementation for now
		ByteArrayInputStream buffer = readBuffer(contents);
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		IContentType[] selected = internalFindContentTypesFor(buffer, subset);
		if (selected.length == 0)
			return null;
		return ((ContentType) selected[0]).internalGetDescriptionFor(buffer, options);
	}

	/**
	 * @see IContentTypeManager
	 */
	public IContentDescription getDescriptionFor(Reader contents, String fileName, QualifiedName[] options) throws IOException {
		CharArrayReader buffer = readBuffer(contents);
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		IContentType[] selected = internalFindContentTypesFor(buffer, subset);
		if (selected.length == 0)
			return null;
		return ((ContentType) selected[0]).internalGetDescriptionFor(buffer, options);
	}

	Preferences getPreferences() {
		return InternalPlatform.getDefault().getPreferencesService().getRootNode().node(CONTENT_TYPE_PREF_NODE);
	}

	protected IContentType[] internalFindContentTypesFor(ByteArrayInputStream buffer, IContentType[] subset) {
		if (buffer == null) {
			Arrays.sort(subset, depthComparator);
			return subset;
		}
		List appropriate = new ArrayList();
		int valid = 0;
		for (int i = 0; i < subset.length; i++) {
			ContentType current = (ContentType) subset[i];
			IContentDescriber describer = current.getDescriber();
			int status = IContentDescriber.INDETERMINATE;
			if (describer != null) {
				status = current.describe(describer, buffer, null);
				if (status == IContentDescriber.INVALID)
					continue;
			}
			if (status == IContentDescriber.VALID)
				appropriate.add(valid++, current);
			else
				appropriate.add(current);
		}
		IContentType[] result = (IContentType[]) appropriate.toArray(new IContentType[appropriate.size()]);
		if (valid > 1)
			Arrays.sort(result, 0, valid, depthComparator);
		if (result.length - valid > 1)
			Arrays.sort(result, valid, result.length, depthComparator);
		return result;
	}

	private IContentType[] internalFindContentTypesFor(CharArrayReader buffer, IContentType[] subset) {
		if (buffer == null) {
			Arrays.sort(subset, depthComparator);
			return subset;
		}
		List appropriate = new ArrayList();
		int valid = 0;
		for (int i = 0; i < subset.length; i++) {
			ContentType current = (ContentType) subset[i];
			IContentDescriber describer = current.getDescriber();
			int status = IContentDescriber.INDETERMINATE;
			if (describer instanceof ITextContentDescriber) {
				status = current.describe((ITextContentDescriber) describer, buffer, null);
				if (status == IContentDescriber.INVALID)
					continue;
			}
			if (status == IContentDescriber.VALID)
				appropriate.add(valid++, current);
			else
				appropriate.add(current);
		}
		IContentType[] result = (IContentType[]) appropriate.toArray(new IContentType[appropriate.size()]);
		if (valid > 1)
			Arrays.sort(result, 0, valid, depthComparator);
		if (result.length - valid > 1)
			Arrays.sort(result, valid, result.length, depthComparator);
		return result;
	}

	ContentType internalGetContentType(String contentTypeIdentifier) {
		return (ContentType) catalog.get(contentTypeIdentifier);
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

	protected void reorganize() {
		fileExtensions.clear();
		fileNames.clear();
		// forget the validation status and aliases for all content types
		for (Iterator i = catalog.values().iterator(); i.hasNext();) {
			ContentType type = ((ContentType) i.next());
			type.setValidation(ContentType.STATUS_UNKNOWN);
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
		builder = createBuilder();
		catalog = new HashMap();
		builder.startup();
		builder.buildContentTypes();
	}
}