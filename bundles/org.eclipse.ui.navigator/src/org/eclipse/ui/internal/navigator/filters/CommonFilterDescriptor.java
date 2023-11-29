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

package org.eclipse.ui.internal.navigator.filters;

import org.eclipse.core.expressions.Expression;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.internal.navigator.CustomAndExpression;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.internal.navigator.NavigatorSafeRunnable;
import org.eclipse.ui.internal.navigator.extensions.INavigatorContentExtPtConstants;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;

/**
 *
 * Describes a <b>commonFilter</b> element under a
 * <b>org.eclipse.ui.navigator.navigatorContent</b> extension.
 *
 * @since 3.2
 */
public class CommonFilterDescriptor implements ICommonFilterDescriptor,
		INavigatorContentExtPtConstants {

	private IConfigurationElement element;

	private Expression filterExpression;

	private String id;

	protected CommonFilterDescriptor(IConfigurationElement anElement) {

		element = anElement;
		init();
	}

	private void init() {
		id = element.getAttribute(ATT_ID);
		if (id == null) {
			id = ""; //$NON-NLS-1$
		}
		IConfigurationElement[] children = element
				.getChildren(TAG_FILTER_EXPRESSION);
		if (children.length == 1) {
			filterExpression = new CustomAndExpression(children[0]);
		}
	}

	/**
	 *
	 * @return An identifier used to determine whether the filter is visible.
	 *         May not be unique.
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 *
	 * @return A translated name to identify the filter
	 */
	@Override
	public String getName() {
		return element.getAttribute(ATT_NAME);
	}

	/**
	 *
	 * @return A translated description to explain to the user what the defined
	 *         filter will hide from the view.
	 */
	@Override
	public String getDescription() {
		return element.getAttribute(ATT_DESCRIPTION);
	}

	/**
	 *
	 * @return Indicates the filter should be in an "Active" state by default.
	 */
	@Override
	public boolean isActiveByDefault() {
		return Boolean.parseBoolean(element.getAttribute(ATT_ACTIVE_BY_DEFAULT));
	}

	/**
	 *
	 * @return Indicates the filter should be shown in the UI.
	 */
	public boolean isVisibleInUi() {
		String attr = element.getAttribute(ATT_VISIBLE_IN_UI);
		if (attr == null)
			return true;
		return Boolean.parseBoolean(attr);
	}

	/**
	 *
	 * @return An instance of the ViewerFilter defined by the extension. Callers
	 *         of this method are responsible for managing the instantiated
	 *         filter.
	 */
	public ViewerFilter createFilter() {
		final ViewerFilter[] filter = new ViewerFilter[1];

		SafeRunner.run(new NavigatorSafeRunnable() {
			@Override
			public void run() throws Exception {
				if (filterExpression != null) {
					if (element.getAttribute(ATT_CLASS) != null) {
						NavigatorPlugin
								.log(
										IStatus.WARNING,
										0,
										"A \"commonFilter\" was specified in " + //$NON-NLS-1$
												element.getDeclaringExtension()
														.getContributor().getName()
												+ " which specifies a \"class\" attribute and an Core Expression.\n" + //$NON-NLS-1$
												"Only the Core Expression will be respected.", //$NON-NLS-1$
										null);
					}

					filter[0] = new CoreExpressionFilter(filterExpression);
					return;
				}
				filter[0] = (ViewerFilter) element.createExecutableExtension(ATT_CLASS);
			}
		});

		if (filter[0] != null)
			return filter[0];
		return SkeletonViewerFilter.INSTANCE;
	}

	@Override
	public String toString() {
		return "CommonFilterDescriptor[" + getName() + " (" + getId() + ")]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
	}
}
