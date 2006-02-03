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

package org.eclipse.ui.navigator.internal.sorters;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.navigator.internal.CustomAndExpression;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;
import org.eclipse.ui.navigator.internal.extensions.INavigatorContentExtPtConstants;

/**
 * 
 * Describes a <b>commonFilter</b> element under a
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension.
 * 
 * @since 3.2
 */
public class CommonSorterDescriptor implements INavigatorContentExtPtConstants {

	private IConfigurationElement element;

	private Expression parentExpression;

	private String id;

	protected CommonSorterDescriptor(IConfigurationElement anElement) {

		element = anElement;
		init();
	}

	private void init() {
		id = element.getAttribute(ATT_ID);
		if (id == null)
			id = ""; //$NON-NLS-1$
		IConfigurationElement[] children = element
				.getChildren(TAG_PARENT_EXPRESSION);
		if (children.length == 1)
			parentExpression = new CustomAndExpression(children[0]);
	}

	/**
	 * 
	 * @return An identifier used to determine whether the filter is visible.
	 *         May not be unique.
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return A translated name to identify the filter
	 */
	public String getName() {
		return element.getAttribute(ATT_NAME);
	}

	/**
	 * 
	 * @return A translated description to explain to the user what the defined
	 *         filter will hide from the view.
	 */
	public String getDescription() {
		return element.getAttribute(ATT_DESCRIPTION);
	}

	/**
	 * 
	 * @param aParent
	 *            An element from the viewer
	 * @return True if and only if this CommonSorter can sort the children of
	 *         the given parent.
	 */
	public boolean isEnabledForParent(Object aParent) {
		if(aParent == null)
			return false;

		if (parentExpression != null) {
			EvaluationContext context = new EvaluationContext(null, aParent);
			try {
				return parentExpression.evaluate(context) == EvaluationResult.TRUE;
			} catch (CoreException e) {
				NavigatorPlugin.logError(0, e.getMessage(), e);
			}
		}
		return true;
	}

	/**
	 * 
	 * @return An instance of the ViewerSorter defined by the extension. Callers
	 *         of this method are responsible for managing the instantiated
	 *         filter.
	 */
	public ViewerSorter createSorter() {
		try {
			return (ViewerSorter) element.createExecutableExtension(ATT_CLASS);
		} catch (RuntimeException re) {
			NavigatorPlugin.logError(0, re.getMessage(), re);
		} catch (CoreException e) {
			NavigatorPlugin.logError(0, e.getMessage(), e);
		}
		return SkeletonViewerSorter.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "CommonSorterDescriptor[" + getName() + " (" + getId() + ")]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}
