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
import org.eclipse.core.internal.runtime.Policy;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.content.*;
import org.osgi.service.prefs.Preferences;

public class ContentType implements IContentType {
	final static String CONTENT_TYPE_CHARSET_PREF = "charset"; //$NON-NLS-1$	
	final static String CONTENT_TYPE_FILE_EXTENSIONS_PREF = "file-extensions"; //$NON-NLS-1$
	final static String CONTENT_TYPE_FILE_NAMES_PREF = "file-names"; //$NON-NLS-1$
	final static byte HIGH = 1;
	final static byte INVALID = 2;
	final static byte LOW = -1;
	final static byte NORMAL = 0;
	static final int PRE_DEFINED_SPEC = IGNORE_PRE_DEFINED;
	final static byte UNKNOWN = 3;
	private static final int USER_DEFINED_SPEC = IGNORE_USER_DEFINED;
	final static byte VALID = 1;

	private ContentType aliasTarget;
	private String baseTypeId;
	private IContentType[] children;
	private String defaultCharset;
	private IConfigurationElement contentTypeElement;
	private boolean failedDescriberCreation;
	private List fileSpecs;
	private ContentTypeManager manager;
	private String name;
	private String namespace;
	private byte priority;
	private String simpleId;
	private byte validation;
	private boolean text;

	public static ContentType createContentType(ContentTypeManager manager, String namespace, String simpleId, String name, byte priority, String[] fileExtensions, String[] fileNames, String baseTypeId, String defaultCharset, IConfigurationElement contentTypeElement) {
		ContentType contentType = new ContentType(manager);
		contentType.simpleId = simpleId;
		contentType.namespace = namespace;
		contentType.name = name;
		contentType.priority = priority;
		if ((fileExtensions != null && fileExtensions.length > 0) || (fileNames != null && fileNames.length > 0)) {
			contentType.fileSpecs = new ArrayList(fileExtensions.length + fileNames.length);
			for (int i = 0; i < fileNames.length; i++)
				contentType.fileSpecs.add(createFileSpec(fileNames[i], FILE_NAME_SPEC | PRE_DEFINED_SPEC));
			for (int i = 0; i < fileExtensions.length; i++)
				contentType.fileSpecs.add(createFileSpec(fileExtensions[i], FILE_EXTENSION_SPEC | PRE_DEFINED_SPEC));
		}
		contentType.defaultCharset = defaultCharset;
		contentType.contentTypeElement = contentTypeElement;
		contentType.baseTypeId = baseTypeId;
		return contentType;
	}

	static FileSpec createFileSpec(String fileSpec, int type) {
		return new FileSpec(fileSpec, type);
	}

	private static String getPreferenceKey(int flags) {
		if ((flags & FILE_EXTENSION_SPEC) != 0)
			return CONTENT_TYPE_FILE_EXTENSIONS_PREF;
		if ((flags & FILE_NAME_SPEC) != 0)
			return CONTENT_TYPE_FILE_NAMES_PREF;
		throw new IllegalArgumentException("Unknown type: " + flags); //$NON-NLS-1$
	}

	static String[] parseItems(String string) {
		if (string == null)
			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(string, ","); //$NON-NLS-1$
		if (!tokenizer.hasMoreTokens())
			return new String[0];
		String first = tokenizer.nextToken();
		if (!tokenizer.hasMoreTokens())
			return new String[] {first};
		ArrayList items = new ArrayList();
		items.add(first);
		do {
			items.add(tokenizer.nextToken());
		} while (tokenizer.hasMoreTokens());
		return (String[]) items.toArray(new String[items.size()]);
	}

	public boolean isText() {
		return isKindOf(manager.getContentType(IContentTypeManager.CT_TEXT));
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
			result.append(list);
			result.append(',');
		}
		// ignore last comma
		return result.substring(0, result.length() - 1);
	}

	public ContentType(ContentTypeManager manager) {
		this.manager = manager;
	}

	public synchronized void addFileSpec(String fileSpec, int type) {
		if (aliasTarget != null) {
			getTarget().addFileSpec(fileSpec, type);
			return;
		}
		if (type != FILE_EXTENSION_SPEC && type != FILE_NAME_SPEC)
			throw new IllegalArgumentException("Unknown type: " + type); //$NON-NLS-1$		
		// ensure we don't have it already		
		if (hasFileSpec(fileSpec, type))
			return;
		if (fileSpecs == null)
			fileSpecs = new ArrayList(3);
		internalAddFileSpec(fileSpec, type | USER_DEFINED_SPEC);
		String key = getPreferenceKey(type);
		Preferences contentTypeNode = manager.getPreferences().node(getId());
		contentTypeNode.put(key, toListString(fileSpecs));
	}

	public IContentType getBaseType() {
		if (aliasTarget != null)
			return getTarget().getBaseType();
		if (baseTypeId == null)
			return null;
		return manager.getContentType(baseTypeId);
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
		return defaultCharset;
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
		if (!failedDescriberCreation && contentTypeElement.getChildren("describer").length > 0)
			try {
				return (IContentDescriber) contentTypeElement.createExecutableExtension("describer"); //$NON-NLS-1$
			} catch (CoreException ce) {
				// ensure this is logged once during a session
				failedDescriberCreation = true;
				String message = Policy.bind("content.invalidContentDescriber", getId()); //$NON-NLS-1$ 
				IStatus status = new Status(IStatus.WARNING, IPlatform.PI_RUNTIME, 0, message, ce);
				InternalPlatform.getDefault().log(status);
			}
		ContentType baseType = (ContentType) getBaseType();
		return baseType == null ? null : baseType.getDescriber();
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDescriptionFor(InputStream contents, QualifiedName[] options) throws IOException {
		if (aliasTarget != null)
			return getTarget().getDescriptionFor(contents, options);
		ByteArrayInputStream buffer = ContentTypeManager.readBuffer(contents);
		if (buffer == null)
			return null;
		ContentDescription description = new ContentDescription(options);
		IContentDescriber describer = this.getDescriber();
		if (describer != null)
			describer.describe(buffer, description);
		// check if any of the defaults need to be applied
		if (description.isRequested(IContentDescription.CHARSET) && description.getProperty(IContentDescription.CHARSET) == null)
			description.setProperty(IContentDescription.CHARSET, getDefaultCharset());
		description.setContentType(this);
		return description;
	}

	/**
	 * @see IContentType
	 */
	public IContentDescription getDescriptionFor(Reader contents, QualifiedName[] options) throws IOException {
		if (aliasTarget != null)
			return getTarget().getDescriptionFor(contents, options);
		CharArrayReader buffer = ContentTypeManager.readBuffer(contents);
		if (buffer == null)
			return null;
		ContentDescription description = new ContentDescription(options);
		IContentDescriber describer = this.getDescriber();
		if (describer != null) {
			if (!(describer instanceof ITextContentDescriber))
				throw new UnsupportedOperationException();
			((ITextContentDescriber) describer).describe(buffer, description);
		}
		// check if any of the defaults need to be applied
		if (description.isRequested(IContentDescription.CHARSET) && description.getProperty(IContentDescription.CHARSET) == null)
			description.setProperty(IContentDescription.CHARSET, getDefaultCharset());
		description.setContentType(this);
		return description;
	}

	public String[] getFileSpecs(int typeMask) {
		if (aliasTarget != null)
			return getTarget().getFileSpecs(typeMask);
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

	boolean hasAnyFileSpec() {
		return fileSpecs != null && !fileSpecs.isEmpty();
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
			if ((spec.getBasicType() == typeMask) && text.equals(spec.getText()))
				return true;
		}
		return false;
	}

	void internalAddFileSpec(String fileSpec, int typeMask) {
		if (aliasTarget != null) {
			aliasTarget.internalAddFileSpec(fileSpec, typeMask);
			return;
		}
		if (fileSpecs == null)
			fileSpecs = new ArrayList(3);
		fileSpecs.add(createFileSpec(fileSpec, typeMask));
	}

	void internalRemoveFileSpec(String fileSpec, int typeMask) {
		if (aliasTarget != null) {
			aliasTarget.internalRemoveFileSpec(fileSpec, typeMask);
			return;
		}
		if (fileSpecs == null)
			return;
		for (Iterator i = fileSpecs.iterator(); i.hasNext();) {
			FileSpec spec = (FileSpec) i.next();
			if ((spec.getType() == typeMask) && fileSpec.equals(spec.getText())) {
				i.remove();
				return;
			}
		}
	}

	public boolean isAssociatedWith(String fileName) {
		if (aliasTarget != null)
			return getTarget().isAssociatedWith(fileName);
		//TODO: should parent be queried?
		if (fileSpecs == null)
			return false;
		if (hasFileSpec(fileName, FILE_NAME_SPEC))
			return true;
		String fileExtension = ContentTypeManager.getFileExtension(fileName);
		return (fileExtension == null) ? false : hasFileSpec(fileExtension, FILE_EXTENSION_SPEC);
	}

	//TODO this can be done better
	public boolean isKindOf(IContentType another) {
		if (aliasTarget != null)
			return getTarget().isKindOf(another);
		if (this == another)
			return true;
		IContentType baseType = getBaseType();
		return baseType != null && baseType.isKindOf(another);
	}

	boolean isValid() {
		return validation == VALID;
	}

	public synchronized void removeFileSpec(String fileSpec, int type) {
		if (aliasTarget != null) {
			getTarget().removeFileSpec(fileSpec, type);
			return;
		}
		if (type != FILE_EXTENSION_SPEC && type != FILE_NAME_SPEC)
			throw new IllegalArgumentException("Unknown type: " + type); //$NON-NLS-1$
		internalRemoveFileSpec(fileSpec, type | USER_DEFINED_SPEC);
	}

	void setAliasTarget(ContentType newTarget) {
		// when changing the target, it must be cleared first
		if (aliasTarget != null && newTarget != null)
			return;
		aliasTarget = newTarget;
	}

	void setValidation(byte validation) {
		this.validation = validation;
	}

	public String toString() {
		return getId();
	}
}