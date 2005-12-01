/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.actions;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionProvider;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.SkeletonActionProvider;

/**
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 */
public class CommonActionProviderDescriptor {

	private static final String ENABLEMENT = "enablement"; //$NON-NLS-1$

	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private final IConfigurationElement configurationElement;

	private Expression enablement;

	private boolean hasLoadingFailed;

	/**
	 * 
	 */
	public CommonActionProviderDescriptor(IConfigurationElement aConfigElement) {
		super();
		configurationElement = aConfigElement;
		init();
	}

	/**
	 * 
	 */
	private void init() {

		IConfigurationElement[] children = configurationElement
				.getChildren(ENABLEMENT);
		if (children.length == 1) {
			try {
				enablement = ElementHandler.getDefault().create(
						ExpressionConverter.getDefault(), children[0]);
			} catch (CoreException e) {
				NavigatorPlugin.log(IStatus.ERROR, 0, e.getMessage(), e);
			}
		} else if (children.length > 1) {
			System.err.println("More than one element: " + //$NON-NLS-1$
					ENABLEMENT
					+ " in navigator extension: " + //$NON-NLS-1$
					configurationElement.getDeclaringExtension()
							.getUniqueIdentifier());
		}
	}

	public ICommonActionProvider createActionProvider() {
		if (hasLoadingFailed)
			return SkeletonActionProvider.INSTANCE;
		ICommonActionProvider provider = null;
		try {
			provider = (ICommonActionProvider) configurationElement
					.createExecutableExtension(ATT_CLASS);
		} catch (CoreException exception) {
			NavigatorPlugin.log("Unable to create navigator extension: " + //$NON-NLS-1$
					getClassName(), exception.getStatus());
			hasLoadingFailed = true;
		} catch (Exception e) {
			NavigatorPlugin.log("Unable to create navigator extension: " + //$NON-NLS-1$
					getClassName(), new Status(IStatus.ERROR,
					NavigatorPlugin.PLUGIN_ID, 0, e.getMessage(), e));
			e.printStackTrace();
			hasLoadingFailed = true;
		}
		return provider;
	}

	/**
	 * @return
	 */
	private String getClassName() {
		return configurationElement.getAttribute(ATT_CLASS);
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

}
