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

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @see IContentType
 */
public final class ContentType implements IContentType {

	/* A placeholder for missing/invalid binary/text describers. */
	private class InvalidDescriber implements IContentDescriber, ITextContentDescriber {
		public int describe(InputStream contents, IContentDescription description) throws IOException {
			return INVALID;
		}

		public int describe(Reader contents, IContentDescription description) throws IOException {
			return INVALID;
		}

		public QualifiedName[] getSupportedOptions() {
			return new QualifiedName[0];
		}
	}

	final static byte ASSOCIATED_BY_EXTENSION = 2;
	final static byte ASSOCIATED_BY_NAME = 1;
	private static final String DESCRIBER_ELEMENT = "describer"; //$NON-NLS-1$
	final static byte NOT_ASSOCIATED = 0;

	public final static String PREF_DEFAULT_CHARSET = "charset"; //$NON-NLS-1$	
	public final static String PREF_FILE_EXTENSIONS = "file-extensions"; //$NON-NLS-1$
	public final static String PREF_FILE_NAMES = "file-names"; //$NON-NLS-1$
	final static byte PRIORITY_HIGH = 1;
	final static byte PRIORITY_LOW = -1;
	final static byte PRIORITY_NORMAL = 0;
	final static int SPEC_PRE_DEFINED = IGNORE_PRE_DEFINED;
	final static int SPEC_USER_DEFINED = IGNORE_USER_DEFINED;
	final static byte STATUS_INVALID = 2;
	final static byte STATUS_UNKNOWN = 0;
	final static byte STATUS_VALID = 1;
	private String baseTypeId;
	private String aliasTargetId;
	private IConfigurationElement contentTypeElement;
	private String defaultCharset;
	private String userCharset;
	private IContentDescription defaultDescription;
	private IContentDescriber describer;
	private List fileSpecs;
	private ContentTypeManager manager;
	private String name;
	private String namespace;
	private byte priority;
	private String simpleId;
	private byte validation = STATUS_UNKNOWN;

	public static ContentType createContentType(ContentTypeCatalog catalog, String namespace, String simpleId, String name, byte priority, String[] fileExtensions, String[] fileNames, String baseTypeId, String aliasTargetId, String defaultCharset, IConfigurationElement contentTypeElement) {
		ContentType contentType = new ContentType(catalog.getManager());
		contentType.defaultDescription = new DefaultDescription(contentType, catalog);
		contentType.simpleId = simpleId;
		contentType.namespace = namespace;
		contentType.name = name;
		contentType.priority = priority;
		if ((fileExtensions != null && fileExtensions.length > 0) || (fileNames != null && fileNames.length > 0)) {
			contentType.fileSpecs = new ArrayList(fileExtensions.length + fileNames.length);
			for (int i = 0; i < fileNames.length; i++)
				contentType.internalAddFileSpec(catalog, fileNames[i], FILE_NAME_SPEC | SPEC_PRE_DEFINED);
			for (int i = 0; i < fileExtensions.length; i++)
				contentType.internalAddFileSpec(catalog, fileExtensions[i], FILE_EXTENSION_SPEC | SPEC_PRE_DEFINED);
		}
		contentType.defaultCharset = defaultCharset;
		contentType.contentTypeElement = contentTypeElement;
		contentType.baseTypeId = baseTypeId;
		contentType.aliasTargetId = aliasTargetId;
		contentType.processPreferences(catalog);
		return contentType;
	}

	private void processPreferences(ContentTypeCatalog catalog) {
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		// user set default charset
		this.userCharset = contentTypeNode.get(PREF_DEFAULT_CHARSET, null);
		// user set file names 
		String userSetFileNames = contentTypeNode.get(PREF_FILE_NAMES, null);
		String[] fileNames = Util.parseItems(userSetFileNames);
		for (int i = 0; i < fileNames.length; i++)
			internalAddFileSpec(catalog, fileNames[i], FILE_NAME_SPEC | SPEC_USER_DEFINED);
		// user set file extensions
		String userSetFileExtensions = contentTypeNode.get(PREF_FILE_EXTENSIONS, null);
		String[] fileExtensions = Util.parseItems(userSetFileExtensions);
		for (int i = 0; i < fileExtensions.length; i++)
			internalAddFileSpec(catalog, fileExtensions[i], FILE_EXTENSION_SPEC | SPEC_USER_DEFINED);
	}

	static FileSpec createFileSpec(String fileSpec, int type) {
		return new FileSpec(fileSpec, type);
	}

	private static String getPreferenceKey(int flags) {
		if ((flags & FILE_EXTENSION_SPEC) != 0)
			return PREF_FILE_EXTENSIONS;
		if ((flags & FILE_NAME_SPEC) != 0)
			return PREF_FILE_NAMES;
		throw new IllegalArgumentException("Unknown type: " + flags); //$NON-NLS-1$
	}

	public ContentType(ContentTypeManager manager) {
		this.manager = manager;
	}

	/**
	 * @see IContentType
	 */
	public void addFileSpec(String fileSpec, int type) throws CoreException {
		addFileSpec(manager.getCatalog(), fileSpec, type);
	}

	private void addFileSpec(ContentTypeCatalog catalog, String fileSpec, int type) throws CoreException {
		Assert.isLegal(type == FILE_EXTENSION_SPEC || type == FILE_NAME_SPEC, "Unknown type: " + type); //$NON-NLS-1$		
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null) {
			aliasTarget.addFileSpec(catalog, fileSpec, type);
			return;
		}
		String[] userSet;
		synchronized (this) {
			if (!internalAddFileSpec(catalog, fileSpec, type | SPEC_USER_DEFINED))
				return;
			// TODO shouldn't this be taking a catalog too?
			userSet = internalGetFileSpecs(type | IGNORE_PRE_DEFINED);
		}
		// persist using preferences		
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		contentTypeNode.put(getPreferenceKey(type), Util.toListString(userSet));
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = NLS.bind(Messages.content_errorSavingSettings, getId());
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, bse);
			throw new CoreException(status);
		}
		// notify listeners
		manager.fireContentTypeChangeEvent(this);
	}

	int describe(IContentDescriber selectedDescriber, boolean text, Object contents, ContentDescription description) throws IOException {
		try {
			if (!text)
				return selectedDescriber.describe((InputStream) contents, description);
			return ((ITextContentDescriber) selectedDescriber).describe((Reader) contents, description);
		} catch (RuntimeException re) {
			// describer seems to be buggy. just disable it (logging the reason)
			invalidateDescriber(re);
		} catch (Error e) {
			// describer got some serious problem. disable it (logging the reason) and throw the error again 
			invalidateDescriber(e);
			throw e;
		} catch (LowLevelIOException llioe) {
			// throw the actual exception
			throw llioe.getActualException();
		} catch (IOException ioe) {
			// bugs 67841/ 62443  - non-low level IOException should be "ignored"
			if (ContentTypeManager.DEBUGGING) {
				String message = NLS.bind(Messages.content_errorReadingContents, getId());
				ContentType.log(message, ioe);
			}
		} finally {
			if (!text)
				((LazyInputStream) contents).rewind();
			else
				((LazyReader) contents).rewind();
		}
		return IContentDescriber.INVALID;
	}

	public boolean equals(Object another) {
		if (!(another instanceof ContentType))
			return false;
		return ((ContentType) another).getId().equals(this.getId());
	}

	public String getAliasTargetId() {
		return aliasTargetId;
	}

	/**
	 * @see IContentType
	 */
	public IContentType getBaseType() {
		return getBaseType(manager.getCatalog());
	}

	ContentType getBaseType(ContentTypeCatalog catalog) {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.getBaseType(catalog);
		if (baseTypeId == null)
			return null;
		ContentType originalBaseType = catalog.internalGetContentType(baseTypeId);
		return originalBaseType != null ? originalBaseType.getTarget(catalog, true) : null;
	}

	String getBaseTypeId() {
		return baseTypeId;
	}

	/**
	 * @see IContentType
	 */
	public String getDefaultCharset() {
		return getDefaultCharset(manager.getCatalog());
	}

	String getDefaultCharset(ContentTypeCatalog catalog) {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.getDefaultCharset(catalog);
		String currentCharset = userCharset != null ? userCharset : internalGetDefaultCharset(catalog);
		// an empty string as charset means: no default charset
		return "".equals(currentCharset) ? null : currentCharset; //$NON-NLS-1$
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDefaultDescription() {
		return getDefaultDescription(manager.getCatalog());
	}

	private IContentDescription getDefaultDescription(ContentTypeCatalog catalog) {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.getDefaultDescription(catalog);
		return defaultDescription;
	}

	int getDepth(ContentTypeCatalog catalog) {
		ContentType baseType = getBaseType(catalog);
		if (baseType == null)
			return 0;
		return 1 + baseType.getDepth(catalog);
	}

	/**
	 * For tests only, should not be called by anyone else.
	 */
	public IContentDescriber getDescriber() {
		return getDescriber(manager.getCatalog());
	}

	IContentDescriber getDescriber(ContentTypeCatalog catalog) {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.getDescriber(catalog);
		try {
			// if "" is specified no describer should be created
			if ("".equals(contentTypeElement.getAttributeAsIs(DESCRIBER_ELEMENT))) //$NON-NLS-1$
				return null;
			synchronized (this) {
				if (describer != null)
					return describer;
				if (contentTypeElement.getChildren(DESCRIBER_ELEMENT).length > 0 || contentTypeElement.getAttributeAsIs(DESCRIBER_ELEMENT) != null)
					try {
						return describer = (IContentDescriber) contentTypeElement.createExecutableExtension(DESCRIBER_ELEMENT);
					} catch (CoreException ce) {
						// the content type definition was invalid. Ensure we don't
						// try again, and this content type does not accept any
						// contents
						return invalidateDescriber(ce);
					}
			}
		} catch (InvalidRegistryObjectException e) {
			/*
			 * This should only happen if  an API call is made after the registry has changed and before
			 * the corresponding registry change event has been broadcast.  
			 */
			// the configuration element is stale - need to rebuild the catalog
			manager.invalidate();
			// bad timing - next time the client asks for a describer, s/he will have better luck
			return null;
		}
		ContentType baseType = getBaseType(catalog);
		return baseType == null ? null : baseType.getDescriber(catalog);
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDescriptionFor(InputStream contents, QualifiedName[] options) throws IOException {
		return getDescriptionFor(manager.getCatalog(), contents, options);
	}

	private IContentDescription getDescriptionFor(ContentTypeCatalog catalog, InputStream contents, QualifiedName[] options) throws IOException {
		InputStream buffer = ContentTypeManager.readBuffer(contents);
		if (buffer == null)
			return defaultDescription;
		return internalGetDescriptionFor(catalog, buffer, options);
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDescriptionFor(Reader contents, QualifiedName[] options) throws IOException {
		return getDescriptionFor(manager.getCatalog(), contents, options);
	}

	private IContentDescription getDescriptionFor(ContentTypeCatalog catalog, Reader contents, QualifiedName[] options) throws IOException {
		Reader buffer = ContentTypeManager.readBuffer(contents);
		if (buffer == null)
			return defaultDescription;
		return internalGetDescriptionFor(catalog, buffer, options);
	}

	/**
	 * @see IContentType
	 */
	public String[] getFileSpecs(int typeMask) {
		return getFileSpecs(manager.getCatalog(), typeMask);
	}

	public String[] getFileSpecs(ContentTypeCatalog catalog) {
		return getFileSpecs(catalog, IContentType.FILE_NAME_SPEC | IContentType.FILE_EXTENSION_SPEC);
	}

	public String[] getFileSpecs(ContentTypeCatalog catalog, int typeMask) {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.getFileSpecs(catalog, typeMask);
		return internalGetFileSpecs(typeMask);
	}

	public int hashCode() {
		return this.getId().hashCode();
	}

	private String[] internalGetFileSpecs(int typeMask) {
		if (fileSpecs == null)
			return new String[0];
		// invert the last two bits so it is easier to compare
		typeMask ^= (IGNORE_PRE_DEFINED | IGNORE_USER_DEFINED);
		List result = new ArrayList(fileSpecs.size());
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if ((spec.getType() & typeMask) == spec.getType())
				result.add(spec.getText());
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * @see IContentType
	 */
	public String getId() {
		return namespace + '.' + simpleId;
	}

	/**
	 * @see IContentType
	 */
	public String getName() {
		return name;
	}

	byte getPriority() {
		return priority;
	}

	String getSimpleId() {
		return simpleId;
	}

	/*
	 * Returns the alias target, if one is found, or this object otherwise.
	 */
	ContentType getTarget(ContentTypeCatalog catalog, boolean self) {
		ContentType target = catalog.getAliasTarget(this);
		return (self && target == null) ? this : target;
	}

	byte getValidation() {
		return validation;
	}

	/**
	 * @param text the file spec string
	 * @param typeMask FILE_NAME_SPEC or FILE_EXTENSION_SPEC
	 * @return true if this file spec has already been added, false otherwise
	 */
	boolean hasFileSpec(String text, int typeMask) {
		if (fileSpecs == null)
			return false;
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if (spec.equals(text, typeMask))
				return true;
		}
		return false;
	}

	/**
	 * Adds a user-defined or pre-defined file spec.
	 */
	boolean internalAddFileSpec(ContentTypeCatalog catalog, String fileSpec, int typeMask) {
		// XXX shouldn't this be done *after* we check for aliasing?
		if (hasFileSpec(fileSpec, typeMask))
			return false;
		//TODO do we have to check for aliases at this point? 
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.internalAddFileSpec(catalog, fileSpec, typeMask);
		if (fileSpecs == null)
			fileSpecs = new ArrayList(3);
		FileSpec newFileSpec = createFileSpec(fileSpec, typeMask);
		fileSpecs.add(newFileSpec);
		if ((typeMask & ContentType.SPEC_USER_DEFINED) != 0)
			catalog.associate(this, newFileSpec.getText(), newFileSpec.getType());
		return true;
	}

	private String internalGetDefaultCharset(ContentTypeCatalog catalog) {
		if (defaultCharset == null) {
			ContentType baseType = getBaseType(catalog);
			return baseType == null ? null : baseType.getDefaultCharset(catalog);
		}
		return defaultCharset;
	}

	IContentDescription internalGetDescriptionFor(ContentTypeCatalog catalog, InputStream buffer, QualifiedName[] options) throws IOException {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.internalGetDescriptionFor(catalog, buffer, options);
		if (buffer == null)
			return defaultDescription;
		IContentDescriber describer = this.getDescriber(catalog);
		// no describer - just return the default description
		if (describer == null)
			return defaultDescription;
		ContentDescription description = new ContentDescription(options);
		describe(describer, false, buffer, description);
		// if the describer didn't add any details, just return the default
		// description
		if (!description.isSet())
			return defaultDescription;
		// check if any of the defaults need to be applied
		if (description.isRequested(IContentDescription.CHARSET) && description.getProperty(IContentDescription.CHARSET) == null)
			description.setProperty(IContentDescription.CHARSET, getDefaultCharset(catalog));
		description.setContentType(this);
		return description;
	}

	IContentDescription internalGetDescriptionFor(ContentTypeCatalog catalog, Reader buffer, QualifiedName[] options) throws IOException {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.internalGetDescriptionFor(catalog, buffer, options);
		if (buffer == null)
			return defaultDescription;
		IContentDescriber describer = this.getDescriber(catalog);
		// no describer - just return the default description
		if (describer == null)
			return defaultDescription;
		ContentDescription description = new ContentDescription(options);
		if (!(describer instanceof ITextContentDescriber))
			throw new UnsupportedOperationException();
		describe(describer, true, buffer, description);
		// if the describer didn't add any details, just return the default description
		if (!description.isSet())
			return defaultDescription;
		// check if any of the defaults need to be applied
		if (description.isRequested(IContentDescription.CHARSET) && description.getProperty(IContentDescription.CHARSET) == null)
			description.setProperty(IContentDescription.CHARSET, getDefaultCharset(catalog));
		description.setContentType(this);
		return description;
	}

	byte internalIsAssociatedWith(ContentTypeCatalog catalog, String fileName) {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.internalIsAssociatedWith(catalog, fileName);
		if (hasFileSpec(fileName, FILE_NAME_SPEC))
			return ASSOCIATED_BY_NAME;
		String fileExtension = ContentTypeManager.getFileExtension(fileName);
		if (hasFileSpec(fileExtension, FILE_EXTENSION_SPEC))
			return ASSOCIATED_BY_EXTENSION;
		// if does not have *built-in* file specs, delegate to parent (if any)
		if (!hasAnyFileSpec(SPEC_PRE_DEFINED)) {
			IContentType baseType = getBaseType(catalog);
			if (baseType != null)
				return ((ContentType) baseType).internalIsAssociatedWith(catalog, fileName);
		}
		return NOT_ASSOCIATED;
	}

	boolean hasAnyFileSpec() {
		return fileSpecs != null && !fileSpecs.isEmpty();
	}

	boolean hasAnyFileSpec(int typeMask) {
		if (!hasAnyFileSpec())
			return false;
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if ((spec.getType() & typeMask) == typeMask)
				return true;
		}
		return false;
	}

	boolean internalRemoveFileSpec(ContentTypeCatalog catalog, String fileSpec, int typeMask) {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.internalRemoveFileSpec(catalog, fileSpec, typeMask);
		if (fileSpecs == null)
			return false;
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if ((spec.getType() == typeMask) && fileSpec.equals(spec.getText())) {
				i.remove();
				catalog.dissociate(this, spec.getText(), spec.getType());
				return true;
			}
		}
		return false;
	}

	private IContentDescriber invalidateDescriber(Throwable reason) {
		setValidation(STATUS_INVALID);
		String message = NLS.bind(Messages.content_invalidContentDescriber, getId());
		log(message, reason);
		return describer = new InvalidDescriber();
	}

	/**
	 * @see IContentType
	 */
	public boolean isAssociatedWith(String fileName) {
		return internalIsAssociatedWith(manager.getCatalog(), fileName) != NOT_ASSOCIATED;
	}

	/**
	 * @see IContentType
	 */
	public boolean isKindOf(IContentType another) {
		return isKindOf(manager.getCatalog(), (ContentType) another);
	}

	boolean isKindOf(ContentTypeCatalog catalog, ContentType another) {
		if (another == null)
			return false;
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.isKindOf(catalog, another);
		if (this == another)
			return true;
		if (getDepth(catalog) <= another.getDepth(catalog))
			return false;
		ContentType baseType = getBaseType(catalog);
		return baseType != null && baseType.isKindOf(catalog, another);
	}

	boolean isValid() {
		return validation == STATUS_VALID;
	}

	public static void log(String message, Throwable reason) {
		// don't log CoreExceptions again
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, reason instanceof CoreException ? null : reason);
		InternalPlatform.getDefault().log(status);
	}

	/**
	 * @see IContentType
	 */
	public void removeFileSpec(String fileSpec, int type) throws CoreException {
		removeFileSpec(manager.getCatalog(), fileSpec, type);
	}

	private void removeFileSpec(ContentTypeCatalog catalog, String fileSpec, int type) throws CoreException {
		Assert.isLegal(type == FILE_EXTENSION_SPEC || type == FILE_NAME_SPEC, "Unknown type: " + type); //$NON-NLS-1$		
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null) {
			aliasTarget.removeFileSpec(catalog, fileSpec, type);
			return;
		}
		synchronized (this) {
			if (!internalRemoveFileSpec(catalog, fileSpec, type | SPEC_USER_DEFINED))
				return;
		}
		// persist the change
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		//TODO: shouldn't this call take a catalog?
		final String[] userSet = internalGetFileSpecs(type | IGNORE_PRE_DEFINED);
		if (userSet.length == 0)
			contentTypeNode.remove(getPreferenceKey(type));
		else
			contentTypeNode.put(getPreferenceKey(type), Util.toListString(userSet));
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = NLS.bind(Messages.content_errorSavingSettings, getId());
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, bse);
			throw new CoreException(status);
		}
		// notify listeners		
		manager.fireContentTypeChangeEvent(this);
	}

	void setAliasTarget(ContentTypeCatalog catalog, ContentType newTarget) {
		catalog.setAliasTarget(this, newTarget);
	}

	/**
	 * @see IContentType
	 */
	public void setDefaultCharset(String newCharset) throws CoreException {
		synchronized (this) {
			// don't do anything if there is no actual change
			if (userCharset == null) {
				if (newCharset == null)
					return;
			} else if (userCharset.equals(newCharset))
				return;
			// apply change in memory
			userCharset = newCharset;
		}
		// persist the change
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		setPreference(contentTypeNode, PREF_DEFAULT_CHARSET, userCharset);
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = NLS.bind(Messages.content_errorSavingSettings, getId());
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, bse);
			throw new CoreException(status);
		}
		// notify listeners
		manager.fireContentTypeChangeEvent(this);
	}

	private void setPreference(Preferences node, String key, String value) {
		if (value == null)
			node.remove(key);
		else
			node.put(key, value);
	}

	void setValidation(byte validation) {
		this.validation = validation;
		if (ContentTypeManager.DEBUGGING)
			Policy.debug("Validating " + this + ": " + getValidationString(validation)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static String getValidationString(byte validation) {
		return validation == STATUS_VALID ? "VALID" : (validation == STATUS_INVALID ? "INVALID" : "UNKNOWN"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public String toString() {
		return getId();
	}

	boolean isAlias(ContentTypeCatalog catalog) {
		return getTarget(catalog, false) != null;
	}
}