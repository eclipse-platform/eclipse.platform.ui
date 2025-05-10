/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

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

import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider;

import org.eclipse.ui.editors.text.EditorsUI;

/**
 * Describes an extension to the <code>stickyLinesProviders</code> extension point.
 *
 * @noextend This class is not intended to be extended by clients.
 */
class StickyLinesProviderDescriptor {
	/** Name of the <code>class</code> attribute. */
	private static final String CLASS_ATTRIBUTE= "class"; //$NON-NLS-1$

	/** Name of the <code>id</code> attribute. */
	private static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$

	/** Name of the <code>enabledWhen</code> attribute. **/
	private static final String ENABLED_WHEN_ATTR= "enabledWhen"; //$NON-NLS-1$

	/** The configuration element describing this extension. */
	private IConfigurationElement configuration;

	/** The value of the <code>id</code> attribute, if read. */
	private String id;

	/** The expression value of the <code>enabledWhen</code> attribute. */
	private final Expression enabledWhen;

	/**
	 * Creates a new descriptor for <code>element</code>.
	 * <p>
	 * This method is for internal use only.
	 * </p>
	 *
	 * @param element the extension point element to be described.
	 * @throws CoreException when <code>enabledWhen</code> expression is not valid.
	 */
	public StickyLinesProviderDescriptor(IConfigurationElement element) throws CoreException {
		Assert.isLegal(element != null);
		configuration= element;
		enabledWhen= createEnabledWhen(configuration, getId());
	}

	/**
	 * Returns the expression {@link Expression} declared in the <code>enabledWhen</code> element.
	 *
	 * @param configElement the configuration element
	 * @param id the id of the sticky lines provider.
	 * @return the expression {@link Expression} declared in the enabledWhen element.
	 * @throws CoreException when enabledWhen expression is not valid.
	 */
	private static Expression createEnabledWhen(IConfigurationElement configElement, String id) throws CoreException {
		final IConfigurationElement[] children= configElement.getChildren(ENABLED_WHEN_ATTR);
		if (children.length > 0) {
			IConfigurationElement[] subChildren= children[0].getChildren();
			if (subChildren.length != 1) {
				throw new CoreException(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID,
						"One <enabledWhen> element is accepted. Disabling " + id)); //$NON-NLS-1$
			}
			final ElementHandler elementHandler= ElementHandler.getDefault();
			final ExpressionConverter converter= ExpressionConverter.getDefault();
			return elementHandler.create(converter, subChildren[0]);
		}
		return null;
	}

	/**
	 * Reads (if needed) and returns the id of this extension.
	 *
	 * @return the id for this extension.
	 */
	public String getId() {
		if (id == null) {
			id= configuration.getAttribute(ID_ATTRIBUTE);
			Assert.isNotNull(id);
		}
		return id;
	}

	/**
	 * Creates a sticky lines provider as described in the extension's XML and null otherwise.
	 *
	 * @return the created sticky lines provider and null otherwise.
	 */
	protected IStickyLinesProvider createStickyLinesProvider() {
		try {
			Object extension= configuration.createExecutableExtension(CLASS_ATTRIBUTE);
			if (extension instanceof IStickyLinesProvider stickyLinesProvider) {
				return stickyLinesProvider;
			} else {
				String message= "Invalid extension to stickyLinesProvider. Must extends IStickyLinesProvider: " //$NON-NLS-1$
						+ getId();
				EditorsPlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, message));
				return null;
			}
		} catch (CoreException e) {
			EditorsPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID,
					"Error while creating stickyLinesProvider: " + getId(), e)); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Returns true if the given viewer, editor matches the enabledWhen expression and false
	 * otherwise.
	 *
	 * @param viewer the viewer
	 * @param editor the editor
	 * @return true if the given viewer, editor matches the enabledWhen expression and false
	 *         otherwise.
	 */
	public boolean matches(ISourceViewer viewer, ITextEditor editor) {
		if (enabledWhen == null) {
			return true;
		}
		EvaluationContext context= new EvaluationContext(null, editor);
		context.setAllowPluginActivation(true);
		context.addVariable("viewer", viewer); //$NON-NLS-1$
		context.addVariable("editor", editor); //$NON-NLS-1$
		context.addVariable("editorInput", editor.getEditorInput()); //$NON-NLS-1$
		try {
			return enabledWhen.evaluate(context) == EvaluationResult.TRUE;
		} catch (CoreException e) {
			EditorsPlugin.getDefault().getLog().log(
					new Status(IStatus.ERROR, EditorsUI.PLUGIN_ID, "Error while 'enabledWhen' evaluation", e)); //$NON-NLS-1$
			return false;
		}
	}
}
