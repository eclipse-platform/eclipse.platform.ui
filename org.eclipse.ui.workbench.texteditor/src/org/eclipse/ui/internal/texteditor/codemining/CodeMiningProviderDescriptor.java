/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide extension point for CodeMining - Bug 528419
 */
package org.eclipse.ui.internal.texteditor.codemining;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Describes an extension to the <code>codeMiningProviders</code> extension
 * point.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 *
 * @since 3.10
 * @noextend This class is not intended to be subclassed by clients.
 */
class CodeMiningProviderDescriptor {

	/** Name of the <code>label</code> attribute. */
	private static final String LABEL_ATTRIBUTE = "label"; //$NON-NLS-1$
	/** Name of the <code>class</code> attribute. */
	private static final String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	/** Name of the <code>id</code> attribute. */
	private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	/** Name of the <code>enabledWhen</code> attribute. **/
	private static final String ENABLED_WHEN_ATTR = "enabledWhen"; //$NON-NLS-1$

	/** The configuration element describing this extension. */
	private IConfigurationElement fConfiguration;
	/** The value of the <code>label</code> attribute, if read. */
	private String fLabel;
	/** The value of the <code>id</code> attribute, if read. */
	private String fId;
	/** The expression value of the <code>enabledWhen</code> attribute. */
	private final Expression fEnabledWhen;

	/**
	 * Creates a new descriptor for <code>element</code>.
	 * <p>
	 * This method is for internal use only.
	 * </p>
	 *
	 * @param element
	 *            the extension point element to be described.
	 * @throws CoreException
	 *             when <code>enabledWhen</code> expression is not valid.
	 */
	public CodeMiningProviderDescriptor(IConfigurationElement element) throws CoreException {
		Assert.isLegal(element != null);
		fConfiguration = element;
		fEnabledWhen = createEnabledWhen(fConfiguration, getId());
	}

	/**
	 * Returns the expression {@link Expression} declared in the
	 * <code>enabledWhen</code> element.
	 *
	 * @param configElement
	 *            the configuration element
	 * @param id
	 *            the id of the codemining provider.
	 * @return the expression {@link Expression} declared in the enabledWhen
	 *         element.
	 * @throws CoreException
	 *             when enabledWhen expression is not valid.
	 */
	private static Expression createEnabledWhen(IConfigurationElement configElement, String id) throws CoreException {
		final IConfigurationElement[] children = configElement.getChildren(ENABLED_WHEN_ATTR);
		if (children.length > 0) {
			IConfigurationElement[] subChildren = children[0].getChildren();
			if (subChildren.length != 1) {
				throw new CoreException(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID,
						"One <enabledWhen> element is accepted. Disabling " + id)); //$NON-NLS-1$
			}
			final ElementHandler elementHandler = ElementHandler.getDefault();
			final ExpressionConverter converter = ExpressionConverter.getDefault();
			return elementHandler.create(converter, subChildren[0]);

		}
		throw new CoreException(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID,
				"<enabledWhen> element is required. Disabling " + id)); //$NON-NLS-1$
	}

	/**
	 * Reads (if needed) and returns the label of this extension.
	 *
	 * @return the label for this extension.
	 */
	public String getLabel() {
		if (fLabel == null) {
			fLabel = fConfiguration.getAttribute(LABEL_ATTRIBUTE);
			Assert.isNotNull(fLabel);
		}
		return fLabel;
	}

	/**
	 * Reads (if needed) and returns the id of this extension.
	 *
	 * @return the id for this extension.
	 */
	public String getId() {
		if (fId == null) {
			fId = fConfiguration.getAttribute(ID_ATTRIBUTE);
			Assert.isNotNull(fId);
		}
		return fId;
	}

	/**
	 * Creates a codemining provider as described in the extension's xml. and null
	 * otherwise.
	 *
	 * @param editor
	 *            the text editor
	 *
	 * @return the created codemining provider and null otherwise.
	 */
	protected ICodeMiningProvider createCodeMiningProvider(ITextEditor editor) {
		try {
			Object extension = fConfiguration.createExecutableExtension(CLASS_ATTRIBUTE);
			if (extension instanceof ICodeMiningProvider) {
				if (extension instanceof AbstractCodeMiningProvider) {
					((AbstractCodeMiningProvider) extension).setContext(editor);
				}
				return (ICodeMiningProvider) extension;
			} else {
				String message = "Invalid extension to codeMiningProviders. Must extends ICodeMiningProvider: " //$NON-NLS-1$
						+ getId();
				TextEditorPlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, message));
				return null;
			}
		} catch (CoreException e) {
			TextEditorPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID,
					"Error while creating codeMiningProvider: " + getId(), e)); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Returns true if the given viewer, editor matches the enabledWhen expression
	 * and false otherwise.
	 *
	 * @param viewer
	 *            the viewer
	 * @param editor
	 *            the editor
	 * @return true if the given viewer, editor matches the enabledWhen expression
	 *         and false otherwise.
	 */
	public boolean matches(ISourceViewer viewer, ITextEditor editor) {
		EvaluationContext context = new EvaluationContext(null, editor);
		context.setAllowPluginActivation(true);
		context.addVariable("viewer", viewer); //$NON-NLS-1$
		context.addVariable("editor", editor); //$NON-NLS-1$
		context.addVariable("editorInput", editor.getEditorInput()); //$NON-NLS-1$
		try {
			return fEnabledWhen.evaluate(context) == EvaluationResult.TRUE;
		} catch (CoreException e) {
			TextEditorPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, TextEditorPlugin.PLUGIN_ID, "Error while 'enabledWhen' evaluation", e)); //$NON-NLS-1$
			return false;
		}
	}

}
