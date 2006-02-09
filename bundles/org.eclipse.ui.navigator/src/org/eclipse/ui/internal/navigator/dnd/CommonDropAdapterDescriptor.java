/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.dnd;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

/**
 * @since 3.2
 * 
 */
public final class CommonDropAdapterDescriptor implements
		INavigatorContentExtPtConstants {

	private IConfigurationElement element;

	private Expression dragExpr;

	private Expression dropExpr;

	/* package */CommonDropAdapterDescriptor(
			IConfigurationElement aConfigElement) {
		element = aConfigElement;
		init();
	}

	private void init() {

		IConfigurationElement[] children = element
				.getChildren(TAG_DRAG_EXPRESSION);
		if (children.length == 1)
			dragExpr = new CustomAndExpression(children[0]);

		children = element.getChildren(TAG_DROP_EXPRESSION);
		if (children.length == 1)
			dropExpr = new CustomAndExpression(children[0]);

	}

	/**
	 * 
	 * @param anElement
	 *            The element from the set of elements being dragged.
	 * @return True if the element matches the drag expression from the
	 *         extension.
	 */
	public boolean isDragElementSupported(Object anElement) {
		if (dragExpr != null)
			try {
				return dragExpr
						.evaluate(new EvaluationContext(null, anElement)) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				NavigatorPlugin.logError(0, e.getMessage(), e);
			}
		return false;
	}

	/**
	 * 
	 * @param anElement
	 *            The element from the set of elements benig dropped.
	 * @return True if the element matches the drop expression from the
	 *         extension.
	 */
	public boolean isDropElementSupported(Object anElement) {
		if (dropExpr != null)
			try {
				return dropExpr
						.evaluate(new EvaluationContext(null, anElement)) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				NavigatorPlugin.logError(0, e.getMessage(), e);
			}
		return false;
	}

	/**
	 * 
	 * @return An instance of {@link CommonDropAdapterAssistant} from the
	 *         descriptor or {@link SkeletonCommonDropAssistant}.
	 */
	public CommonDropAdapterAssistant createDropAssistant() {

		try {
			return (CommonDropAdapterAssistant) element
					.createExecutableExtension(ATT_CLASS);
		} catch (CoreException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		} catch (RuntimeException re) {
			NavigatorPlugin.logError(0, re.getMessage(), re);
		}
		return SkeletonCommonDropAssistant.INSTANCE;

	}

}
