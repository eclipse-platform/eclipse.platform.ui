/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class wraps and proxies an instance of T provided through extensions and loads it lazily when it can contribute to the editor, then delegates all operations to actual instance.
 * 
 * @param <T>
 *            the actual type to proxy, typically the one defined on the extension point.
 */
public class GenericContentTypeRelatedExtension<T> {
	private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static final String CONTENT_TYPE_ATTRIBUTE = "contentType"; //$NON-NLS-1$
	private static final String ENABLED_WHEN_ATTRIBUTE = "enabledWhen"; //$NON-NLS-1$

	public final IConfigurationElement extension;
	public final IContentType targetContentType;
	public final Expression enabledWhen;

	public GenericContentTypeRelatedExtension(IConfigurationElement element) throws Exception {
		this.extension = element;
		this.targetContentType = Platform.getContentTypeManager().getContentType(element.getAttribute(CONTENT_TYPE_ATTRIBUTE));
		this.enabledWhen = buildEnabledWhen(element);
	}

	@SuppressWarnings("unchecked") public T createDelegate() {
		try {
			return (T) extension.createExecutableExtension(CLASS_ATTRIBUTE);
		} catch (CoreException e) {
			GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, e.getMessage(), e));
		}
		return null;
	}

	/**
	 * Returns the expression {@link Expression} declared in the <code>enabledWhen</code> element.
	 *
	 * @param configElement
	 *            the configuration element
	 * @return the expression {@link Expression} declared in the enabledWhen element.
	 * @throws CoreException
	 *             when enabledWhen expression is not valid.
	 */
	private static Expression buildEnabledWhen(IConfigurationElement configElement) throws CoreException {
		final IConfigurationElement[] children = configElement.getChildren(ENABLED_WHEN_ATTRIBUTE);
		if (children.length > 0) {
			IConfigurationElement[] subChildren = children[0].getChildren();
			if (subChildren.length != 1) {
				throw new CoreException(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, "One <enabledWhen> element is accepted. Disabling " //$NON-NLS-1$
						+ configElement.getAttribute(ID_ATTRIBUTE)));
			}
			final ElementHandler elementHandler = ElementHandler.getDefault();
			final ExpressionConverter converter = ExpressionConverter.getDefault();
			return elementHandler.create(converter, subChildren[0]);
		}
		return null;
	}

	/**
	 * Returns true if the given viewer, editor matches the enabledWhen expression and false otherwise.
	 *
	 * @param viewer
	 *            the viewer
	 * @param editor
	 *            the editor
	 * @return true if the given viewer, editor matches the enabledWhen expression and false otherwise.
	 */
	public boolean matches(ISourceViewer viewer, ITextEditor editor) {
		if (enabledWhen == null) {
			return true;
		}
		EvaluationContext context = new EvaluationContext(null, editor != null ? editor : viewer);
		context.setAllowPluginActivation(true);
		context.addVariable("viewer", viewer); //$NON-NLS-1$
		if (viewer.getDocument() != null) {
			context.addVariable("document", viewer.getDocument()); //$NON-NLS-1$
		}
		if (editor != null) {
			context.addVariable("editor", editor); //$NON-NLS-1$
			context.addVariable("editorInput", editor.getEditorInput()); //$NON-NLS-1$
		}
		try {
			return enabledWhen.evaluate(context) == EvaluationResult.TRUE;
		} catch (CoreException e) {
			GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, GenericEditorPlugin.BUNDLE_ID, "Error while 'enabledWhen' evaluation", e)); //$NON-NLS-1$
			return false;
		}
	}
}
