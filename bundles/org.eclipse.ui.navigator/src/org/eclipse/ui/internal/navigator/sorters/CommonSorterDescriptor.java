/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.sorters;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;

/**
 * 
 * Describes a <b>commonSorter</b> element under a
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
		if (id == null) {
			id = ""; //$NON-NLS-1$
		}
		IConfigurationElement[] children = element
				.getChildren(TAG_PARENT_EXPRESSION);
		if (children.length == 1) {
			parentExpression = new CustomAndExpression(children[0]);
		}
	}

	/**
	 * 
	 * @return An identifier used to determine whether the sorter is visible.
	 *         May not be unique.
	 */
	public String getId() {
		return id;
	}
 
	/**
	 * 
	 * @param aParent
	 *            An element from the viewer
	 * @return True if and only if this CommonSorter can sort the children of
	 *         the given parent.
	 */
	public boolean isEnabledForParent(Object aParent) {
		if(aParent == null) {
			return false;
		}

		if (parentExpression != null) {
			IEvaluationContext context = NavigatorPlugin.getEvalContext(aParent);
			return NavigatorPlugin.safeEvaluate(parentExpression, context) == EvaluationResult.TRUE;
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
		final ViewerSorter[] sorter = new ViewerSorter[1];

		SafeRunner.run(new NavigatorSafeRunnable(element) {
			public void run() throws Exception {
				sorter[0] = (ViewerSorter) element.createExecutableExtension(ATT_CLASS);
			}
		});
		if (sorter[0] != null)
			return sorter[0];
		return SkeletonViewerSorter.INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "CommonSorterDescriptor[" + getId() + "]"; //$NON-NLS-1$//$NON-NLS-2$
	}
}
