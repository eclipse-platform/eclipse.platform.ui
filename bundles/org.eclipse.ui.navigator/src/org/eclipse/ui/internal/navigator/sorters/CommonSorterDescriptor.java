/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.sorters;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
	 * @return An instance of the ViewerComparator defined by the extension. Callers
	 *         of this method are responsible for managing the instantiated filter.
	 */
	public ViewerComparator createComparator() {
		final ViewerComparator[] sorter = new ViewerComparator[1];

		SafeRunner.run(new NavigatorSafeRunnable(element) {
			@Override
			public void run() throws Exception {
				sorter[0] = createComparatorInstance();
			}
		});
		if (sorter[0] != null)
			return sorter[0];
		return SkeletonViewerSorter.INSTANCE;
	}

	private ViewerComparator createComparatorInstance() throws CoreException {
		Object contributed = element.createExecutableExtension(ATT_CLASS);
		if (contributed instanceof ViewerComparator comparator) {
			return comparator;
		}
		throw new ClassCastException("Class contributed by " + element.getNamespaceIdentifier() + //$NON-NLS-1$
				" to " + INavigatorContentExtPtConstants.TAG_NAVIGATOR_CONTENT + //$NON-NLS-1$
				"/" + INavigatorContentExtPtConstants.TAG_COMMON_SORTER //$NON-NLS-1$
				+ " is not an instance of " + ViewerComparator.class.getName() + ": " + contributed.getClass().getName() //$NON-NLS-1$ //$NON-NLS-2$
		);
	}

	/**
	 * Public for tests only.
	 */
	public static class WrappedViewerComparator extends ViewerSorter {

		private final ViewerComparator comparator;

		public WrappedViewerComparator(ViewerComparator comparator) {
			this.comparator = comparator;
		}

		@Override
		public int category(Object element) {
			return comparator.category(element);
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return comparator.compare(viewer, e1, e2);
		}

		@Override
		public boolean isSorterProperty(Object element, String property) {
			return comparator.isSorterProperty(element, property);
		}

		/**
		 * Public for tests only.
		 *
		 * @return Returns the original comparator instance wrapped by this
		 *         instance.
		 */
		public ViewerComparator getWrappedComparator() {
			return comparator;
		}
	}

	@Override
	public String toString() {
		return "CommonSorterDescriptor[" + getId() + "]"; //$NON-NLS-1$//$NON-NLS-2$
	}
}
