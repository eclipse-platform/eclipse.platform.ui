/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	private Map contentTypes = new HashMap();

	/**
	 * A sorting policy where content types are sorted by id.
	 */
	private Comparator lexicographical = new Comparator() {
		public int compare(Object o1, Object o2) {
			ContentType type1 = (ContentType) o1;
			ContentType type2 = (ContentType) o2;
			return type1.getId().compareTo(type2.getId());
		}
	};

	/**
	 * A sorting policy where the more specific content type wins (if they are related), 
	 *   
	 */
	private Comparator specificIsBetter = new Comparator() {
		public int compare(Object o1, Object o2) {
			ContentType type1 = (ContentType) o1;
			ContentType type2 = (ContentType) o2;
			// first criteria: depth - the higher, the better
			int depthCriteria = type1.getDepth(ContentTypeCatalog.this) - type2.getDepth(ContentTypeCatalog.this);
			if (depthCriteria != 0)
				return -depthCriteria;
			// second criteria: priority - the higher, the better
			int priorityCriteria = type1.getPriority() - type2.getPriority();
			if (priorityCriteria != 0)
				return -priorityCriteria;
			return 0;
		}
	};
	private Comparator constantSpecificIsBetter = new Comparator() {
		public int compare(Object o1, Object o2) {
			ContentType type1 = (ContentType) o1;
			ContentType type2 = (ContentType) o2;
			// first criteria: depth - the higher, the better
			int depthCriteria = type1.getDepth(ContentTypeCatalog.this) - type2.getDepth(ContentTypeCatalog.this);
			if (depthCriteria != 0)
				return -depthCriteria;
			// second criteria: priority - the higher, the better
			int priorityCriteria = type1.getPriority() - type2.getPriority();
			if (priorityCriteria != 0)
				return -priorityCriteria;
			// they have same depth and priority - choose one arbitrarily (stability is important)
			return type1.getId().compareTo(type2.getId());
		}
	};

	private Comparator constantGeneralIsBetter = new Comparator() {
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
			// they have same depth and priority - choose one arbitrarily (stability is important)
			return type1.getId().compareTo(type2.getId());
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

	void associate(ContentType contentType) {
		String[] builtInFileNames = contentType.getFileSpecs(this, IContentType.IGNORE_USER_DEFINED | IContentType.FILE_NAME_SPEC);
		for (int i = 0; i < builtInFileNames.length; i++)
			associate(contentType, builtInFileNames[i], IContentType.FILE_NAME_SPEC);
		String[] builtInFileExtensions = contentType.getFileSpecs(this, IContentType.IGNORE_USER_DEFINED | IContentType.FILE_EXTENSION_SPEC);
		for (int i = 0; i < builtInFileExtensions.length; i++)
			associate(contentType, builtInFileExtensions[i], IContentType.FILE_EXTENSION_SPEC);
	}

	void associate(ContentType contentType, String text, int type) {
		Map fileSpecMap = ((type & IContentType.FILE_NAME_SPEC) != 0) ? fileNames : fileExtensions;
		String mappingKey = FileSpec.getMappingKeyFor(text);
		Set existing = (Set) fileSpecMap.get(mappingKey);
		if (existing == null)
			fileSpecMap.put(mappingKey, existing = new HashSet());
		existing.add(contentType);
	}

	void dissociate(ContentType contentType, String text, int type) {
		Map fileSpecMap = ((type & IContentType.FILE_NAME_SPEC) != 0) ? fileNames : fileExtensions;
		String mappingKey = FileSpec.getMappingKeyFor(text);
		Set existing = (Set) fileSpecMap.get(mappingKey);
		if (existing == null)
			return;
		existing.remove(contentType);
	}

	/**
	 * A content type will be valid if:
	 * <ol>
	 * <li>it does not designate a base type, or</li>
	 * <li>it designates a base type that exists and is valid</li>
	 * </ol>
	 * <p>And</p>:
	 * <ol>
	 * <li>it does not designate an alias type, or</li>
	 * <li>it designates an alias type that does not exist, or</li>
	 * <li>it designates an alias type that exists and is valid</li>
	 * </ol> 
	 */
	private boolean ensureValid(ContentType type) {
		if (type.getValidation() != ContentType.STATUS_UNKNOWN)
			// already processed
			return type.isValid();
		// set this type temporarily as invalid to prevent cycles
		// all types in a cycle would remain as invalid
		type.setValidation(ContentType.STATUS_INVALID);
		// check base type
		if (type.getBaseTypeId() != null) {
			ContentType baseType = (ContentType) contentTypes.get(type.getBaseTypeId());
			if (baseType == null)
				// invalid: specified base type is not known
				return false;
			// base type exists, ensure it is valid
			ensureValid(baseType);
			if (baseType.getValidation() != ContentType.STATUS_VALID)
				// invalid: base type was invalid
				return false;
		}
		// check alias target		
		ContentType aliasTarget = type.getTarget(this, false);
		if (aliasTarget != null) {
			// alias target type exists, ensure it is valid
			ensureValid(aliasTarget);
			if (aliasTarget.getValidation() != ContentType.STATUS_VALID)
				// invalid: alias target was invalid
				return false;
		}
		// valid: all conditions satisfied
		type.setValidation(ContentType.STATUS_VALID);
		return true;
	}

	protected IContentType[] findContentTypesFor(InputStream contents, String fileName) throws IOException {
		InputStream buffer = ContentTypeManager.readBuffer(contents);
		return internalFindContentTypesFor(buffer, false, getSubset(fileName), fileName == null ? constantSpecificIsBetter : specificIsBetter);
	}

	public IContentType[] findContentTypesFor(final String fileName) {
		return internalFindContentTypesFor(fileName, constantGeneralIsBetter);
	}

	/**
	 * For testing purposes only.
	 */
	public IContentType[] findContentTypesFor(InputStream contents, IContentType[] subset) throws IOException {
		InputStream buffer = ContentTypeManager.readBuffer(contents);
		return internalFindContentTypesFor(buffer, false, subset, specificIsBetter);
	}

	/**
	 * This is the implementation for file name based content type matching. 
	 * 
	 * @return all matching content types in the preferred order 
	 * @see IContentTypeManager#findContentTypesFor(String)
	 */
	public IContentType[] internalFindContentTypesFor(final String fileName, Comparator sortingPolicy) {
		final List selected = new ArrayList(5);
		// files associated by name should appear before those associated by extension		
		Set allByFileName = (Set) fileNames.get(FileSpec.getMappingKeyFor(fileName));
		collectMatchingByName(allByFileName, selected, fileName, IContentType.FILE_NAME_SPEC);
		int boundary = selected.size();
		final String fileExtension = ContentTypeManager.getFileExtension(fileName);
		if (fileExtension != null) {
			Set allByFileExtension = (Set) fileExtensions.get(FileSpec.getMappingKeyFor(fileExtension));
			collectMatchingByName(allByFileExtension, selected, fileExtension, IContentType.FILE_EXTENSION_SPEC);
		}
		IContentType[] result = (IContentType[]) selected.toArray(new IContentType[selected.size()]);
		if (sortingPolicy == null)
			return result;
		if (boundary > 1)
			Arrays.sort(result, 0, boundary, sortingPolicy);
		if (boundary < result.length - 1)
			Arrays.sort(result, boundary, result.length, sortingPolicy);
		return result;
	}

	/**
	 * Processes all content types in source, adding those matching the given file spec to the
	 * destination collection.
	 */
	private void collectMatchingByName(Collection source, final Collection destination, final String fileSpecText, final int fileSpecType) {
		if (source == null || source.isEmpty())
			return;
		// process all content types in the given collection
		for (Iterator i = source.iterator(); i.hasNext();) {
			final ContentType root = (ContentType) i.next();
			// From a given content type, check if it matches, and 
			// include any children that match as well.
			internalAccept(new ContentTypeVisitor() {
				public int visit(ContentType type) {
					if (type != root && type.hasBuiltInAssociations())
						// this content type has built-in associations - visit it later as root						
						return RETURN;
					if (type == root && !type.hasFileSpec(fileSpecText, fileSpecType))
						// it is the root and does not match the file name - do not add it nor look into its children						
						return RETURN;
					// either the content type is the root and matches the file name or 
					// is a sub content type and does not have built-in files specs
					if (!destination.contains(type))
						// make sure we don't add it twice						
						destination.add(type);
					return CONTINUE;
				}
			}, root);
		}
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
			if (next.getBaseType(this) == parent)
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
		InputStream buffer = ContentTypeManager.readBuffer(contents);
		IContentType[] selected = internalFindContentTypesFor(buffer, false, getSubset(fileName), fileName == null ? constantSpecificIsBetter : specificIsBetter);
		if (selected.length == 0)
			return null;
		return ((ContentType) selected[0]).internalGetDescriptionFor(this, buffer, options);
	}

	private IContentType[] getSubset(String fileName) {
		return fileName == null ? getAllContentTypes() : internalFindContentTypesFor(fileName, lexicographical);
	}

	public IContentDescription getDescriptionFor(Reader contents, String fileName, QualifiedName[] options) throws IOException {
		Reader buffer = ContentTypeManager.readBuffer(contents);
		IContentType[] selected = internalFindContentTypesFor(buffer, true, getSubset(fileName), fileName == null ? constantSpecificIsBetter : specificIsBetter);
		if (selected.length == 0)
			return null;
		return ((ContentType) selected[0]).internalGetDescriptionFor(this, buffer, options);
	}

	public ContentTypeManager getManager() {
		return manager;
	}

	public IContentType[] internalFindContentTypesFor(Object buffer, boolean text, IContentType[] subset, Comparator sortingPolicy) throws IOException {
		List appropriate = new ArrayList();
		int valid = 0;
		for (int i = 0; i < subset.length; i++) {
			ContentType current = (ContentType) subset[i];
			IContentDescriber describer = current.getDescriber(this);
			int status = IContentDescriber.INDETERMINATE;
			if (describer != null) {
				status = current.describe(describer, text, buffer, null);
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
			Arrays.sort(result, 0, valid, sortingPolicy);
		if (result.length - valid > 1)
			Arrays.sort(result, valid, result.length, sortingPolicy);
		return result;
	}

	ContentType internalGetContentType(String contentTypeIdentifier) {
		return (ContentType) contentTypes.get(contentTypeIdentifier);
	}

	private void makeAliases() {
		// process all content types marking aliases appropriately
		for (Iterator i = contentTypes.values().iterator(); i.hasNext();) {
			ContentType type = (ContentType) i.next();
			String targetId = type.getAliasTargetId();
			if (targetId == null)
				continue;
			ContentType target = internalGetContentType(targetId);
			if (target != null)
				type.setAliasTarget(this, target);
		}
	}

	protected void organize() {
		// build the aliasing
		makeAliases();
		// do the validation
		for (Iterator i = contentTypes.values().iterator(); i.hasNext();) {
			ContentType type = (ContentType) i.next();
			if (ensureValid(type))
				associate(type);
		}
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

	public boolean internalAccept(ContentTypeVisitor visitor, ContentType root) {
		if (!root.isValid() || root.isAlias(this))
			return true;
		int result = visitor.visit(root);
		switch (result) {
			// stop traversing the tree
			case ContentTypeVisitor.STOP :
				return false;
			// stop traversing this subtree
			case ContentTypeVisitor.RETURN :
				return true;
		}
		ContentType[] children = getChildren(root);
		if (children == null)
			// this content type has no subtypes - keep traversing the tree
			return true;
		for (int i = 0; i < children.length; i++)
			if (!internalAccept(visitor, children[i]))
				// stop the traversal
				return false;
		return true;
	}
}
