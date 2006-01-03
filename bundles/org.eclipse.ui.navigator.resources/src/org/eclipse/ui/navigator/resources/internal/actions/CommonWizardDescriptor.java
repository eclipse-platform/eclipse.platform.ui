/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.navigator.resources.internal.actions;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonWizardDescriptor {

	private static final String ATT_WIZARD_ID = "wizardId"; //$NON-NLS-1$

	private static final String ATT_TYPE = "type"; //$NON-NLS-1$	

	private static final String TAG_ENABLEMENT = "enablement"; //$NON-NLS-1$ 

	private String wizardId;

	private String type;

	private Expression enablement;

	private IConfigurationElement configElement;

	/**
	 * @param aConfigElement The configuration element from the extension point. 
	 * * @throws WorkbenchException
	 *             if the configuration element could not be parsed. Reasons
	 *             include:
	 *             <ul>
	 *             <li>A required attribute is missing.</li>
	 *             <li>More elements are define than is allowed.</li>
	 *             </ul>
	 */
	public CommonWizardDescriptor(IConfigurationElement aConfigElement)
			throws WorkbenchException {
		super();
		configElement = aConfigElement;
		init();
	}

	/**
	 * Determine if this content extension is enabled for the given selection.
	 * The content extension is enabled for the selection if and only if it is
	 * enabled for each element in the selection.
	 * 
	 * @param aStructuredSelection
	 *            The selection from the viewer
	 * @return True if and only if the extension is enabled for each element in
	 *         the selection.
	 */
	public boolean isEnabledFor(IStructuredSelection aStructuredSelection) {
		if (enablement == null)
			return false;

		IEvaluationContext context = null;

		Iterator elements = aStructuredSelection.iterator();
		while (elements.hasNext()) {
			context = new EvaluationContext(null, elements.next());
			try {
				if (enablement.evaluate(context) == EvaluationResult.FALSE)
					return false;
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
				return false;
			}
		}
		return true;
	}

	/**
	 * Determine if this content extension is enabled for the given element.
	 * 
	 * @param anElement
	 *            The element that should be used for the evaluation.
	 * @return True if and only if the extension is enabled for the element.
	 */
	public boolean isEnabledFor(Object anElement) {

		if (enablement == null || anElement == null)
			return false;

		try {
			return (enablement.evaluate(new EvaluationContext(null, anElement)) == EvaluationResult.TRUE);
		} catch (CoreException e) {
			NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
		}
		return false;
	}

	void init() throws WorkbenchException {
		wizardId = configElement.getAttribute(ATT_WIZARD_ID);
		type = configElement.getAttribute(ATT_TYPE);

		if (wizardId == null || wizardId.length() == 0) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
					ATT_WIZARD_ID + " in common wizard extension: " + //$NON-NLS-1$
					configElement.getDeclaringExtension().getUniqueIdentifier());
		}

		if (type == null || type.length() == 0) {
			throw new WorkbenchException("Missing attribute: " + //$NON-NLS-1$
					ATT_TYPE + " in common wizard extension: " + //$NON-NLS-1$
					configElement.getDeclaringExtension().getUniqueIdentifier());
		}

		IConfigurationElement[] children = configElement
				.getChildren(TAG_ENABLEMENT);
		if (children.length == 1) {
			try {
				enablement = ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), children[0]);
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
			}
		} else if (children.length > 1) {
			throw new WorkbenchException("More than one element: " + //$NON-NLS-1$
					TAG_ENABLEMENT + " in common wizard extension: " + //$NON-NLS-1$
					configElement.getDeclaringExtension().getUniqueIdentifier());
		} 
	}

	/**
	 * 
	 * @return Returns the common wizard wizardId
	 */
	public String getWizardId() {
		return wizardId;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

}