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
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @see IContentType
 */
public final class ContentType implements IContentType {

	/* A placeholder for missing/invalid binary/text describers. */
	private class InvalidDescriber implements IContentDescriber, ITextContentDescriber {
		public int describe(InputStream contents, IContentDescription description) {
			return INVALID;
		}

		public int describe(Reader contents, IContentDescription description) {
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
	private static final Object NO_DESCRIBER = "NO DESCRIBER"; //$NON-NLS-1$
	private static final Object INHERITED_DESCRIBER = "INHERITED DESCRIBER"; //$NON-NLS-1$
	private String baseTypeId;
	private String aliasTargetId;
	private IConfigurationElement contentTypeElement;
	private String userCharset;
	private IContentDescription defaultDescription;
	private Object describer;
	private List fileSpecs;
	private ContentTypeManager manager;
	private String name;
	private byte priority;
	private byte validation = STATUS_UNKNOWN;
	private Map defaultProperties;
	private boolean builtInAssociations = false;
	private String id;

	public static ContentType createContentType(ContentTypeCatalog catalog, String uniqueId, String name, byte priority, String[] fileExtensions, String[] fileNames, String baseTypeId, String aliasTargetId, Map defaultProperties, IConfigurationElement contentTypeElement) {
		ContentType contentType = new ContentType(catalog.getManager());
		contentType.defaultDescription = new DefaultDescription(contentType);
		contentType.id = uniqueId;
		contentType.name = name;
		contentType.priority = priority;
		if ((fileExtensions != null && fileExtensions.length > 0) || (fileNames != null && fileNames.length > 0)) {
			contentType.builtInAssociations = true;
			contentType.fileSpecs = new ArrayList(fileExtensions.length + fileNames.length);
			for (int i = 0; i < fileNames.length; i++)
				contentType.internalAddFileSpec(catalog, fileNames[i], FILE_NAME_SPEC | SPEC_PRE_DEFINED);
			for (int i = 0; i < fileExtensions.length; i++)
				contentType.internalAddFileSpec(catalog, fileExtensions[i], FILE_EXTENSION_SPEC | SPEC_PRE_DEFINED);
		}
		contentType.defaultProperties = defaultProperties;
		contentType.contentTypeElement = contentTypeElement;
		contentType.baseTypeId = baseTypeId;
		contentType.aliasTargetId = aliasTargetId;
		return contentType;
	}

	void processPreferences(ContentTypeCatalog catalog, Preferences contentTypeNode) {
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
		Preferences contentTypeNode = manager.getPreferences().node(id);
		String newValue = Util.toListString(userSet);
		// we are adding stuff, newValue must be non-null
		Assert.isNotNull(newValue);
		contentTypeNode.put(getPreferenceKey(type), newValue);
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = NLS.bind(Messages.content_errorSavingSettings, id);
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, bse);
			throw new CoreException(status);
		}
		// notify listeners
		manager.fireContentTypeChangeEvent(this);
	}

	int describe(IContentDescriber selectedDescriber, ILazySource contents, ContentDescription description) throws IOException {
		final boolean isText = contents.isText();
		if (isText && !(selectedDescriber instanceof ITextContentDescriber))
			throw new UnsupportedOperationException();
		try {
			return isText ? ((ITextContentDescriber) selectedDescriber).describe((Reader) contents, description) : selectedDescriber.describe((InputStream) contents, description);
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
				String message = NLS.bind(Messages.content_errorReadingContents, id);
				ContentType.log(message, ioe);
			}
		} finally {
			contents.rewind();
		}
		return IContentDescriber.INVALID;
	}

	public boolean equals(Object another) {
		if (!(another instanceof ContentType))
			return false;
		return ((ContentType) another).id.equals(id);
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
		return getDefaultProperty(manager.getCatalog(), IContentDescription.CHARSET);
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

	/**
	 * Returns the default value for the given property in this content type, or <code>null</code>. 
	 */
	String getDefaultProperty(QualifiedName key) {
		return getDefaultProperty(manager.getCatalog(), key);
	}

	String getDefaultProperty(ContentTypeCatalog catalog, QualifiedName key) {
		String propertyValue = getTarget(catalog, true).internalGetDefaultProperty(catalog, key);
		if ("".equals(propertyValue)) //$NON-NLS-1$
			return null;
		return propertyValue;
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
			// thread safety
			Object tmpDescriber = describer;
			if (tmpDescriber != null) {
				if (INHERITED_DESCRIBER == tmpDescriber) {
					ContentType baseType = getBaseType(catalog);
					return baseType == null ? null : baseType.getDescriber(catalog);
				}
				return (NO_DESCRIBER == tmpDescriber) ? null : (IContentDescriber) tmpDescriber;
			}
			final String describerValue = contentTypeElement.getAttributeAsIs(DESCRIBER_ELEMENT); 
			if (describerValue != null || contentTypeElement.getChildren(DESCRIBER_ELEMENT).length > 0)
				try {
					if ("".equals(describerValue)) { //$NON-NLS-1$
						describer = NO_DESCRIBER;
						return null;
					}
					describer = tmpDescriber = contentTypeElement.createExecutableExtension(DESCRIBER_ELEMENT);
					return (IContentDescriber) tmpDescriber;
				} catch (CoreException ce) {
					// the content type definition was invalid. Ensure we don't
					// try again, and this content type does not accept any
					// contents
					return invalidateDescriber(ce);
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
		if (baseTypeId == null) {
			describer = NO_DESCRIBER;
			return null;
		}
		// remember so we don't come here next time
		describer = INHERITED_DESCRIBER;
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
		return internalGetDescriptionFor(catalog, ContentTypeManager.readBuffer(contents), options);
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDescriptionFor(Reader contents, QualifiedName[] options) throws IOException {
		return getDescriptionFor(manager.getCatalog(), contents, options);
	}

	private IContentDescription getDescriptionFor(ContentTypeCatalog catalog, Reader contents, QualifiedName[] options) throws IOException {
		return internalGetDescriptionFor(catalog, ContentTypeManager.readBuffer(contents), options);
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
		return id.hashCode();
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
		return id;
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

	/**
	 * Returns the default value for a property, recursively if necessary.  
	 */
	private String internalGetDefaultProperty(ContentTypeCatalog catalog, QualifiedName key) {
		// a special case for charset - users can override
		if (userCharset != null && key.equals(IContentDescription.CHARSET))
			return userCharset;
		String defaultValue = defaultProperties == null ? null : (String) defaultProperties.get(key);
		if (defaultValue != null)
			return defaultValue;
		// not defined here, try base type
		ContentType baseType = getBaseType(catalog);
		return baseType == null ? null : baseType.internalGetDefaultProperty(catalog, key);
	}

	IContentDescription internalGetDescriptionFor(ContentTypeCatalog catalog, ILazySource buffer, QualifiedName[] options) throws IOException {
		ContentType aliasTarget = getTarget(catalog, false);
		if (aliasTarget != null)
			return aliasTarget.internalGetDescriptionFor(catalog, buffer, options);
		if (buffer == null)
			return defaultDescription;
		// use temporary local var to avoid sync'ing
		IContentDescriber tmpDescriber = this.getDescriber(catalog);
		// no describer - return default description
		if (tmpDescriber == null)
			return defaultDescription;
		ContentDescription description = new ContentDescription(options, this);
		describe(tmpDescriber, buffer, description);
		// the describer didn't add any details, return default description
		if (!description.isSet())
			return defaultDescription;
		// description cannot be changed afterwards
		description.markImmutable();
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
		// if does not have built-in file specs, delegate to parent (if any)
		if (!hasBuiltInAssociations()) {
			IContentType baseType = getBaseType(catalog);
			if (baseType != null)
				return ((ContentType) baseType).internalIsAssociatedWith(catalog, fileName);
		}
		return NOT_ASSOCIATED;
	}

	boolean hasBuiltInAssociations() {
		return builtInAssociations;
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
		String message = NLS.bind(Messages.content_invalidContentDescriber, id);
		log(message, reason);
		return (IContentDescriber) (describer = new InvalidDescriber());
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
	public boolean isAssociatedWith(String fileName, IScopeContext context) {
		//TODO should honor context parameter
		return isAssociatedWith(fileName);
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
		Preferences contentTypeNode = manager.getPreferences().node(id);
		//TODO: shouldn't this call take a catalog?
		final String[] userSet = internalGetFileSpecs(type | IGNORE_PRE_DEFINED);
		String preferenceKey = getPreferenceKey(type);
		String newValue = Util.toListString(userSet);
		if (newValue == null)
			contentTypeNode.remove(preferenceKey);
		else
			contentTypeNode.put(preferenceKey, newValue);
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = NLS.bind(Messages.content_errorSavingSettings, id);
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
		Preferences contentTypeNode = manager.getPreferences().node(id);
		setPreference(contentTypeNode, PREF_DEFAULT_CHARSET, userCharset);
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = NLS.bind(Messages.content_errorSavingSettings, id);
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
		return id;
	}

	boolean isAlias(ContentTypeCatalog catalog) {
		return getTarget(catalog, false) != null;
	}

	public IContentTypeSettings getSettings(IScopeContext context) throws CoreException {
		//TODO should honor context
		return this;
	}
}
