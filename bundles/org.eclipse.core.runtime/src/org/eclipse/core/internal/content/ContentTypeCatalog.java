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
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.*;

public class ContentTypeCatalog {
	private Map aliases = new HashMap();
	private Map allChildren = new HashMap();

	// a comparator used when resolving conflicts (two types associated to the same spec) 
	private Comparator conflictComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			ContentType type1 = (ContentType) o1;
			ContentType type2 = (ContentType) o2;
			// first criteria: depth - the lower, the better
			int depthCriteria = type1.getDepth(ContentTypeCatalog.this) - type2.getDepth(ContentTypeCatalog.this);
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
	private Map contentTypes = new HashMap();

	private Comparator depthComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			return ((ContentType) o2).getDepth(ContentTypeCatalog.this) - ((ContentType) o1).getDepth(ContentTypeCatalog.this);
		}
	};

	private Map fileExtensions = new HashMap();

	private Map fileNames = new HashMap();

	private ContentTypeManager manager;

	public ContentTypeCatalog(ContentTypeManager manager) {
		this.manager = manager;
	}

	void addContentType(IContentType contentType) {
		contentTypes.put(contentType.getId(), contentType);
	}

	private void addFileSpecContributor(ContentType contentType, int fileSpecType, Map fileSpecsMap) {
		String[] fileSpecs = contentType.getFileSpecs(this, fileSpecType);
		for (int i = 0; i < fileSpecs.length; i++) {
			String mappingKey = FileSpec.getMappingKeyFor(fileSpecs[i]);
			Set existing = (Set) fileSpecsMap.get(mappingKey);
			if (existing == null)
				fileSpecsMap.put(mappingKey, existing = new TreeSet(conflictComparator));
			existing.add(contentType);
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
		if (type.getValidation() != ContentType.STATUS_UNKNOWN)
			return type.isValid();
		if (type.getBaseTypeId() == null) {
			type.setValidation(ContentType.STATUS_VALID);
			return true;
		}
		ContentType baseType = (ContentType) contentTypes.get(type.getBaseTypeId());
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

	IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException {
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		InputStream buffer = ContentTypeManager.readBuffer(contents);
		return internalFindContentTypesFor(buffer, subset);
	}

	public IContentType[] findContentTypesFor(String fileName) {
		List result = new ArrayList(5);
		int count = 0;
		// files associated by name should appear before those associated by extension		
		SortedSet allByFileName = (SortedSet) fileNames.get(FileSpec.getMappingKeyFor(fileName));
		if (allByFileName != null && !allByFileName.isEmpty()) {
			ContentType main = ((ContentType) allByFileName.first()).getTarget(this, true);
			result.add(count++, main);
			ContentType[] children = getChildren(main);
			for (int i = 0; i < children.length; i++) {
				ContentType child = children[i];
				// must avoid duplicates and ensure children do not override filespecs
				if (!result.contains(child) && child.internalIsAssociatedWith(this, fileName) == ContentType.ASSOCIATED_BY_NAME)
					result.add(count++, child);
			}
		}

		String fileExtension = ContentTypeManager.getFileExtension(fileName);
		if (fileExtension != null) {
			SortedSet allByFileExtension = (SortedSet) fileExtensions.get(FileSpec.getMappingKeyFor(fileExtension));
			if (allByFileExtension != null && !allByFileExtension.isEmpty()) {
				ContentType main = ((ContentType) allByFileExtension.first()).getTarget(this, true);
				if (!result.contains(main)) {
					result.add(count++, main);
					ContentType[] children = getChildren(main);
					for (int i = 0; i < children.length; i++) {
						ContentType child = children[i];
						// must avoid duplicates and ensure children do not override filespecs						
						if (!result.contains(children[i]) && child.internalIsAssociatedWith(this, fileName) == ContentType.ASSOCIATED_BY_EXTENSION)
							result.add(count++, children[i]);
					}
				}
			}
		}
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}

	public ContentType getAliasTarget(ContentType alias) {
		return (ContentType) aliases.get(alias);
	}

	public IContentType[] getAllContentTypes() {
		List result = new ArrayList(contentTypes.size());
		for (Iterator i = contentTypes.values().iterator(); i.hasNext();) {
			ContentType type = (ContentType) i.next();
			if (type.isValid() && !type.isAlias(this))
				result.add(type);
		}
		return (IContentType[]) result.toArray(new IContentType[result.size()]);
	}

	public ContentType[] getChildren(ContentType parent) {
		ContentType[] children = (ContentType[]) allChildren.get(parent);
		if (children != null)
			return children;
		List result = new ArrayList(5);
		for (Iterator i = this.contentTypes.values().iterator(); i.hasNext();) {
			ContentType next = (ContentType) i.next();
			if (next != parent && next.isKindOf(this, parent))
				result.add(next);
		}
		children = (ContentType[]) result.toArray(new ContentType[result.size()]);
		allChildren.put(parent, children);
		return children;
	}

	public ContentType getContentType(String contentTypeIdentifier) {
		ContentType type = internalGetContentType(contentTypeIdentifier);
		return (type != null && type.isValid() && !type.isAlias(this)) ? type : null;
	}

	public IContentDescription getDescriptionFor(InputStream contents, String fileName, QualifiedName[] options) throws IOException {
		// naive implementation for now
		InputStream buffer = ContentTypeManager.readBuffer(contents);
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		IContentType[] selected = internalFindContentTypesFor(buffer, subset);
		if (selected.length == 0)
			return null;
		return ((ContentType) selected[0]).internalGetDescriptionFor(this, buffer, options);
	}

	public IContentDescription getDescriptionFor(Reader contents, String fileName, QualifiedName[] options) throws IOException {
		Reader buffer = ContentTypeManager.readBuffer(contents);
		IContentType[] subset = fileName != null ? findContentTypesFor(fileName) : getAllContentTypes();
		IContentType[] selected = internalFindContentTypesFor(buffer, subset);
		if (selected.length == 0)
			return null;
		return ((ContentType) selected[0]).internalGetDescriptionFor(this, buffer, options);
	}

	public ContentTypeManager getManager() {
		return manager;
	}

	public IContentType[] internalFindContentTypesFor(InputStream buffer, IContentType[] subset) throws IOException {
		if (buffer == null) {
			Arrays.sort(subset, depthComparator);
			return subset;
		}
		List appropriate = new ArrayList();
		int valid = 0;
		for (int i = 0; i < subset.length; i++) {
			ContentType current = (ContentType) subset[i];
			IContentDescriber describer = current.getDescriber(this);
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

	private IContentType[] internalFindContentTypesFor(Reader buffer, IContentType[] subset) throws IOException {
		if (buffer == null) {
			Arrays.sort(subset, depthComparator);
			return subset;
		}
		List appropriate = new ArrayList();
		int valid = 0;
		for (int i = 0; i < subset.length; i++) {
			ContentType current = (ContentType) subset[i];
			IContentDescriber describer = current.getDescriber(this);
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
		return (ContentType) contentTypes.get(contentTypeIdentifier);
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
			ContentType elected = (ContentType) j.next();
			while (j.hasNext())
				((ContentType) j.next()).setAliasTarget(this, elected);
		}
	}

	protected void reorganize() {
		fileExtensions.clear();
		fileNames.clear();
		// forget the validation status and aliases for all content types
		for (Iterator i = contentTypes.values().iterator(); i.hasNext();) {
			ContentType type = ((ContentType) i.next());
			type.setValidation(ContentType.STATUS_UNKNOWN);
			type.setAliasTarget(this, null);
		}
		// do the validation
		for (Iterator i = contentTypes.values().iterator(); i.hasNext();) {
			ContentType type = (ContentType) i.next();
			if (!ensureValid(type))
				continue;
			addFileSpecContributor(type, IContentType.FILE_EXTENSION_SPEC, fileExtensions);
			addFileSpecContributor(type, IContentType.FILE_NAME_SPEC, fileNames);
		}
		makeAliases(fileNames);
		makeAliases(fileExtensions);
		if (ContentTypeManager.DEBUGGING)
			for (Iterator i = contentTypes.values().iterator(); i.hasNext();) {
				ContentType type = (ContentType) i.next();
				if (!type.isValid())
					Policy.debug("Invalid: " + type); //$NON-NLS-1$
			}
	}

	void setAliasTarget(ContentType alias, ContentType target) {
		// don't allow a sub-type to be made into an alias to the base type
		if (target == null) {
			aliases.remove(alias);
			return;
		}
		// don't allow a sub-type to be made into an alias to the base type		
		if (alias.isKindOf(this, target))
			return;
		aliases.put(alias, target);
		if (ContentTypeManager.DEBUGGING)
			Policy.debug("Set alias target for " + alias + " -> " + target); //$NON-NLS-1$ //$NON-NLS-2$		
	}

}
