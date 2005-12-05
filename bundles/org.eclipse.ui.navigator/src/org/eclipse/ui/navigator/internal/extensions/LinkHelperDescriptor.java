/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.navigator.internal.extensions;

import java.util.Iterator;

import org.eclipse.core.expressions.ElementHandler;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

public class LinkHelperDescriptor {

	private final IConfigurationElement configElement;

	private String id;

	/* May be null */
	private String navigatorContentExtensionId;

	private ILinkHelper linkHelper;

	public static final String LINK_HELPER = "linkHelper"; //$NON-NLS-1$

	public static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private static final String ATT_NAVIGATOR_CONTENT_EXTENSION_ID = "navigatorContentExtensionId"; //$NON-NLS-1$

	private Expression editorInputEnablement;

	/* The following field may be null */
	private Expression selectionEnablement;

	private static final String EDITOR_INPUT_ENABLEMENT = "editorInputEnablement"; //$NON-NLS-1$

	private static final String SELECTION_ENABLEMENT = "selectionEnablement"; //$NON-NLS-1$

	public LinkHelperDescriptor(IConfigurationElement element) {
		Assert.isNotNull(element, "LinkHelperRegistry.Descriptor objects cannot be null.");   //$NON-NLS-1$
		Assert.isLegal(LINK_HELPER.equals(element.getName()), "LinkHelperRegistry.Descriptor objects must have the name \"linkHelper\".");  //$NON-NLS-1$
		this.configElement = element;
		init();
	}

	void init() {
		id = this.configElement.getAttribute(ATT_ID);
		IConfigurationElement[] expressions = this.configElement.getChildren(EDITOR_INPUT_ENABLEMENT);
		Assert.isLegal(expressions.length == 1, "The linkHelper extension point requires exactly one editorInputEnablement child."); //$NON-NLS-1$
		try {
			editorInputEnablement = ElementHandler.getDefault().create(
					ExpressionConverter.getDefault(), expressions[0]);
		} catch (CoreException e) {
			NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
		}

		expressions = this.configElement.getChildren(SELECTION_ENABLEMENT);
		if (expressions.length > 0) {
			/* The following attribute is optional */
			navigatorContentExtensionId = expressions[0].getAttribute(ATT_NAVIGATOR_CONTENT_EXTENSION_ID);
			if (expressions[0].getChildren() != null && expressions[0].getChildren().length > 0) {
				try {
					selectionEnablement = ElementHandler.getDefault().create(
							ExpressionConverter.getDefault(), expressions[0]);
				} catch (CoreException e) {
					NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
				}					 
			}
		}
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	public ILinkHelper getLinkHelper() {
		if (linkHelper == null) {
			try {
				linkHelper = (ILinkHelper) this.configElement.createExecutableExtension(ATT_CLASS);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return linkHelper;
	}

	public boolean isEnabledFor(IEditorInput anInput) {

		if (editorInputEnablement == null || anInput == null)
			return false;

		try {
			return (editorInputEnablement.evaluate(new EvaluationContext(null, anInput)) == EvaluationResult.TRUE);
		} catch (CoreException e) {
			NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
		}
		return false; 
	}

	public boolean isEnabledFor(String aNavigatorContentExtensionId) {
		return (navigatorContentExtensionId != null) ? navigatorContentExtensionId.equals(aNavigatorContentExtensionId) : false;
	}

	public boolean isEnabledFor(IStructuredSelection aSelection) {
		if (selectionEnablement == null)
			return false;

		IEvaluationContext context = null;

		Iterator elements = aSelection.iterator();
		while (elements.hasNext()) {
			context = new EvaluationContext(null, elements.next());
			try {
				if (selectionEnablement.evaluate(context) == EvaluationResult.FALSE)
					return false;
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
				return false;
			}
		}
		return true; 
	}
}