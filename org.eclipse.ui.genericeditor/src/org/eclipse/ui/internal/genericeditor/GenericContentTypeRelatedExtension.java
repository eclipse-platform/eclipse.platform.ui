/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;

/**
 * This class wraps and proxies an instance of T provided through extensions
 * and loads it lazily when it can contribute to the editor, then delegates all operations to
 * actual instance.
 * 
 * @param <T> the actual type to proxy, typically the one defined on the extension point.
 */
public class GenericContentTypeRelatedExtension<T> {
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String CONTENT_TYPE_ATTRIBUTE = "contentType"; //$NON-NLS-1$

	public final IConfigurationElement extension;
	public final IContentType targetContentType;

	public GenericContentTypeRelatedExtension(IConfigurationElement element) throws Exception {
		this.extension = element;
		this.targetContentType = Platform.getContentTypeManager().getContentType(element.getAttribute(CONTENT_TYPE_ATTRIBUTE));
	}

	@SuppressWarnings("unchecked")
	public T createDelegate() {
		try {
			return (T) extension.createExecutableExtension(CLASS_ATTRIBUTE);
		} catch (CoreException e) {
			GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, e.getMessage(), e));
		}
		return null;
	}
}
