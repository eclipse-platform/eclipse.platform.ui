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
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

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
	final static byte STATUS_UNKNOWN = 3;
	final static byte STATUS_VALID = 1;
	private ContentType aliasTarget;
	private String baseTypeId;
	private IContentType[] children;
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
	private byte validation;

	public static ContentType createContentType(ContentTypeManager manager, String namespace, String simpleId, String name, byte priority, String[] fileExtensions, String[] fileNames, String baseTypeId, String defaultCharset, IConfigurationElement contentTypeElement) {
		ContentType contentType = new ContentType(manager);
		contentType.defaultDescription = new DefaultDescription(contentType);
		contentType.simpleId = simpleId;
		contentType.namespace = namespace;
		contentType.name = name;
		contentType.priority = priority;
		if ((fileExtensions != null && fileExtensions.length > 0) || (fileNames != null && fileNames.length > 0)) {
			contentType.fileSpecs = new ArrayList(fileExtensions.length + fileNames.length);
			for (int i = 0; i < fileNames.length; i++)
				contentType.fileSpecs.add(createFileSpec(fileNames[i], FILE_NAME_SPEC | SPEC_PRE_DEFINED));
			for (int i = 0; i < fileExtensions.length; i++)
				contentType.fileSpecs.add(createFileSpec(fileExtensions[i], FILE_EXTENSION_SPEC | SPEC_PRE_DEFINED));
		}
		contentType.defaultCharset = defaultCharset;
		contentType.contentTypeElement = contentTypeElement;
		contentType.baseTypeId = baseTypeId;
		contentType.processPreferences();
		return contentType;
	}

	private void processPreferences() {
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		// user set default charset
		this.userCharset = contentTypeNode.get(PREF_DEFAULT_CHARSET, null);
		// user set file names 
		String userSetFileNames = contentTypeNode.get(PREF_FILE_NAMES, null);
		String[] fileNames = parseItems(userSetFileNames);
		for (int i = 0; i < fileNames.length; i++)
			internalAddFileSpec(fileNames[i], FILE_NAME_SPEC | SPEC_USER_DEFINED);
		// user set file extensions
		String userSetFileExtensions = contentTypeNode.get(PREF_FILE_EXTENSIONS, null);
		String[] fileExtensions = parseItems(userSetFileExtensions);
		for (int i = 0; i < fileExtensions.length; i++)
			internalAddFileSpec(fileExtensions[i], FILE_EXTENSION_SPEC | SPEC_USER_DEFINED);
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

	static String[] parseItems(String string) {
		if (string == null)
			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(string, ","); //$NON-NLS-1$
		if (!tokenizer.hasMoreTokens())
			return new String[0];
		String first = tokenizer.nextToken().trim();
		if (!tokenizer.hasMoreTokens())
			return new String[] {first};
		ArrayList items = new ArrayList();
		items.add(first);
		do {
			items.add(tokenizer.nextToken().trim());
		} while (tokenizer.hasMoreTokens());
		return (String[]) items.toArray(new String[items.size()]);
	}

	static String toListString(List list) {
		if (list.isEmpty())
			return ""; //$NON-NLS-1$
		StringBuffer result = new StringBuffer();
		for (Iterator i = list.iterator(); i.hasNext();) {
			result.append(i.next());
			result.append(',');
		}
		// ignore last comma
		return result.substring(0, result.length() - 1);
	}

	static String toListString(Object[] list) {
		if (list.length == 0)
			return ""; //$NON-NLS-1$
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < list.length; i++) {
			result.append(list[i]);
			result.append(',');
		}
		// ignore last comma
		return result.substring(0, result.length() - 1);
	}

	public ContentType(ContentTypeManager manager) {
		this.manager = manager;
	}

	public synchronized void addFileSpec(String fileSpec, int type) throws CoreException {
		Assert.isLegal(type == FILE_EXTENSION_SPEC || type == FILE_NAME_SPEC, "Unknown type: " + type); //$NON-NLS-1$		
		if (aliasTarget != null) {
			getTarget().addFileSpec(fileSpec, type);
			return;
		}
		if (!internalAddFileSpec(fileSpec, type | SPEC_USER_DEFINED))
			return;
		manager.fireContentTypeChangeEvent(this);		
		// persist using preferences
		String key = getPreferenceKey(type);
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		final String[] userSet = internalGetFileSpecs(type | IGNORE_PRE_DEFINED);
		contentTypeNode.put(key, toListString(userSet));
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = Policy.bind("content.errorSavingSettings", getId()); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, bse);
			throw new CoreException(status);
		}
	}

	int describe(IContentDescriber selectedDescriber, InputStream contents, ContentDescription description) throws IOException {
		try {
			return selectedDescriber.describe(contents, description);
		} catch (RuntimeException re) {
			// describer seems to be buggy. just disable it (logging the reason)
			invalidateDescriber(re);
			return IContentDescriber.INVALID;
		} catch (Error e) {
			// describer got some serious problem. disable it (logging the reason) and throw the error again 
			invalidateDescriber(e);
			throw e;
		} finally {
			((LazyInputStream) contents).rewind();
		}
	}

	int describe(ITextContentDescriber selectedDescriber, Reader contents, ContentDescription description) throws IOException {
		try {
			return selectedDescriber.describe(contents, description);
		} catch (RuntimeException re) {
			// describer seems to be buggy. just disable it (logging the reason)
			invalidateDescriber(re);
			return IContentDescriber.INVALID;
		} catch (Error e) {
			// describer got some serious problem. disable it (logging the reason) and throw the error again			
			invalidateDescriber(e);
			throw e;
		} finally {
			((LazyReader) contents).rewind();
		}
	}

	public IContentType getBaseType() {
		if (aliasTarget != null)
			return getTarget().getBaseType();
		if (baseTypeId == null)
			return null;
		ContentType originalBaseType = manager.internalGetContentType(baseTypeId);
		return originalBaseType != null ? originalBaseType.getTarget() : null;
	}

	String getBaseTypeId() {
		return baseTypeId;
	}

	IContentType[] getChildren() {
		if (children == null)
			children = manager.getChildren(this);
		return children;
	}

	/**
	 * @see IContentType
	 */
	public String getDefaultCharset() {
		if (aliasTarget != null)
			return getTarget().getDefaultCharset();
		String currentCharset = userCharset != null ? userCharset : internalGetDefaultCharset();
		// an empty string as charset means: no default charset
		return "".equals(currentCharset) ? null : currentCharset; //$NON-NLS-1$
	}

	public IContentDescription getDefaultDescription() {
		if (aliasTarget != null)
			return getTarget().getDefaultDescription();
		return defaultDescription;
	}

	public int getDepth() {
		ContentType baseType = (ContentType) getBaseType();
		if (baseType == null)
			return 0;
		return 1 + baseType.getDepth();
	}

	public IContentDescriber getDescriber() {
		if (aliasTarget != null)
			return getTarget().getDescriber();
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
		ContentType baseType = (ContentType) getBaseType();
		return baseType == null ? null : baseType.getDescriber();
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDescriptionFor(InputStream contents, QualifiedName[] options) throws IOException {
		InputStream buffer = ContentTypeManager.readBuffer(contents);
		if (buffer == null)
			return defaultDescription;
		return internalGetDescriptionFor(buffer, options);
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDescriptionFor(Reader contents, QualifiedName[] options) throws IOException {
		Reader buffer = ContentTypeManager.readBuffer(contents);
		if (buffer == null)
			return defaultDescription;
		return internalGetDescriptionFor(buffer, options);
	}

	public String[] getFileSpecs(int typeMask) {
		if (aliasTarget != null)
			return getTarget().getFileSpecs(typeMask);
		return internalGetFileSpecs(typeMask);
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

	public String getId() {
		return namespace + '.' + simpleId;
	}

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
	ContentType getTarget() {
		if (aliasTarget == null)
			return this;
		return aliasTarget.getTarget();
	}

	byte getValidation() {
		return validation;
	}

	/**
	 * @param text the file spec string
	 * @param typeMask FILE_NAME_SPEC or FILE_EXTENSION_SPEC
	 * @return true if this file spec has already been added, false otherwise
	 */
	private boolean hasFileSpec(String text, int typeMask) {
		if (fileSpecs == null)
			return false;
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if (spec.equals(text, typeMask))
				return true;
		}
		return false;
	}

	boolean internalAddFileSpec(String fileSpec, int typeMask) {
		if (hasFileSpec(fileSpec, typeMask))
			return false;
		if (aliasTarget != null)
			return aliasTarget.internalAddFileSpec(fileSpec, typeMask);
		if (fileSpecs == null)
			fileSpecs = new ArrayList(3);
		fileSpecs.add(createFileSpec(fileSpec, typeMask));
		return true;
	}

	private String internalGetDefaultCharset() {
		if (defaultCharset == null) {
			ContentType baseType = (ContentType) getBaseType();
			return baseType == null ? null : baseType.getDefaultCharset();
		}
		return defaultCharset;
	}

	IContentDescription internalGetDescriptionFor(InputStream buffer, QualifiedName[] options) throws IOException {
		if (aliasTarget != null)
			return getTarget().internalGetDescriptionFor(buffer, options);
		if (buffer == null)
			return defaultDescription;
		IContentDescriber describer = this.getDescriber();
		// no describer - just return the default description
		if (describer == null)
			return defaultDescription;
		ContentDescription description = new ContentDescription(options);
		describe(describer, buffer, description);
		// if the describer didn't add any details, just return the default
		// description
		if (!description.isSet())
			return defaultDescription;
		// check if any of the defaults need to be applied
		if (description.isRequested(IContentDescription.CHARSET) && description.getProperty(IContentDescription.CHARSET) == null)
			description.setProperty(IContentDescription.CHARSET, getDefaultCharset());
		description.setContentType(this);
		return description;
	}

	IContentDescription internalGetDescriptionFor(Reader buffer, QualifiedName[] options) throws IOException {
		if (aliasTarget != null)
			return getTarget().internalGetDescriptionFor(buffer, options);
		if (buffer == null)
			return defaultDescription;
		IContentDescriber describer = this.getDescriber();
		// no describer - just return the default description
		if (describer == null)
			return defaultDescription;
		ContentDescription description = new ContentDescription(options);
		if (!(describer instanceof ITextContentDescriber))
			throw new UnsupportedOperationException();
		describe((ITextContentDescriber) describer, buffer, description);
		// if the describer didn't add any details, just return the default description
		if (!description.isSet())
			return defaultDescription;
		// check if any of the defaults need to be applied
		if (description.isRequested(IContentDescription.CHARSET) && description.getProperty(IContentDescription.CHARSET) == null)
			description.setProperty(IContentDescription.CHARSET, getDefaultCharset());
		description.setContentType(this);
		return description;
	}

	public byte internalIsAssociatedWith(String fileName) {
		if (aliasTarget != null)
			return getTarget().internalIsAssociatedWith(fileName);
		if (hasFileSpec(fileName, FILE_NAME_SPEC))
			return ASSOCIATED_BY_NAME;
		String fileExtension = ContentTypeManager.getFileExtension(fileName);
		if (hasFileSpec(fileExtension, FILE_EXTENSION_SPEC))
			return ASSOCIATED_BY_EXTENSION;
		// if does not have *built-in* file specs, delegate to parent (if any)
		if (!hasAnyFileSpec(SPEC_PRE_DEFINED)) {
			IContentType baseType = getBaseType();
			if (baseType != null)
				return ((ContentType) baseType).internalIsAssociatedWith(fileName);
		}
		return NOT_ASSOCIATED;
	}

	private boolean hasAnyFileSpec(int typeMask) {
		if (fileSpecs == null || fileSpecs.isEmpty())
			return false;
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if ((spec.getType() & typeMask) == typeMask)
				return true;
		}
		return false;
	}

	boolean internalRemoveFileSpec(String fileSpec, int typeMask) {
		if (aliasTarget != null)
			return aliasTarget.internalRemoveFileSpec(fileSpec, typeMask);
		if (fileSpecs == null)
			return false;
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if ((spec.getType() == typeMask) && fileSpec.equals(spec.getText())) {
				i.remove();
				return true;
			}
		}
		return false;
	}

	private IContentDescriber invalidateDescriber(Throwable reason) {
		setValidation(STATUS_INVALID);
		String message = Policy.bind("content.invalidContentDescriber", getId()); //$NON-NLS-1$ 
		log(message, reason);
		return describer = new InvalidDescriber();
	}

	public boolean isAssociatedWith(String fileName) {
		return internalIsAssociatedWith(fileName) != NOT_ASSOCIATED;
	}

	public boolean isKindOf(IContentType another) {
		if (another == null)
			return false;
		if (aliasTarget != null)
			return getTarget().isKindOf(another);
		if (this == another)
			return true;
		if (getDepth() <= ((ContentType) another).getDepth())
			return false;
		IContentType baseType = getBaseType();
		return baseType != null && baseType.isKindOf(another);
	}

	public boolean isText() {
		return isKindOf(manager.getContentType(IContentTypeManager.CT_TEXT));
	}

	boolean isValid() {
		return validation == STATUS_VALID;
	}

	public static void log(String message, Throwable reason) {
		// don't log CoreExceptions again
		IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, reason instanceof CoreException ? null : reason);
		InternalPlatform.getDefault().log(status);
	}

	public synchronized void removeFileSpec(String fileSpec, int type) throws CoreException {
		Assert.isLegal(type == FILE_EXTENSION_SPEC || type == FILE_NAME_SPEC, "Unknown type: " + type); //$NON-NLS-1$		
		if (aliasTarget != null) {
			getTarget().removeFileSpec(fileSpec, type);
			return;
		}
		if (!internalRemoveFileSpec(fileSpec, type | SPEC_USER_DEFINED))
			return;
		manager.fireContentTypeChangeEvent(this);			
		// persist using preferences
		String key = getPreferenceKey(type);
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		final String[] userSet = internalGetFileSpecs(type | IGNORE_PRE_DEFINED);
		if (userSet.length == 0)
			contentTypeNode.remove(key);
		else
			contentTypeNode.put(key, toListString(userSet));
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = Policy.bind("content.errorSavingSettings", getId()); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, bse);
			throw new CoreException(status);
		}
	}

	void setAliasTarget(ContentType newTarget) {
		// when changing the target, it must be cleared first
		if (aliasTarget != null && newTarget != null)
			return;
		// don't allow a sub-type to be made into an alias to the base type
		if (aliasTarget == null && isKindOf(newTarget))
			return;
		if (ContentTypeManager.DEBUGGING)
			Policy.debug("Setting alias target for " + this + " -> " + newTarget); //$NON-NLS-1$ //$NON-NLS-2$		
		aliasTarget = newTarget;
	}

	/*
	 * (non-Javadoc) 
	 * @see org.eclipse.core.runtime.content.IContentType#setDefaultCharset(java.lang.String)
	 */
	public void setDefaultCharset(String newCharset) throws CoreException {
		if (userCharset == null) {
			if (newCharset == null)
				return;
		} else if (userCharset.equals(newCharset))
			return;
		userCharset = newCharset;
		// notify listeners
		manager.fireContentTypeChangeEvent(this);
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		if (userCharset == null)
			contentTypeNode.remove(PREF_DEFAULT_CHARSET);
		else
			contentTypeNode.put(PREF_DEFAULT_CHARSET, userCharset);
		try {
			contentTypeNode.flush();
		} catch (BackingStoreException bse) {
			String message = Policy.bind("content.errorSavingSettings", getId()); //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, bse);
			throw new CoreException(status);
		}
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

	public boolean isAlias() {
		return aliasTarget != null;
	}
}