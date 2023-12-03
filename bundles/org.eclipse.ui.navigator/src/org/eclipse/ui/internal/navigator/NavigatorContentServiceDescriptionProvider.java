/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.eclipse.ui.navigator.IDescriptionProvider;

/**
 *
 * @since 3.2
 */
public final class NavigatorContentServiceDescriptionProvider implements
		IDescriptionProvider {

	private final NavigatorContentService contentService;

	/**
	 * Creates a description provider that targets the given service.
	 *
	 * @param aContentService
	 *            The content service associated with this provider.
	 */
	public NavigatorContentServiceDescriptionProvider(
			NavigatorContentService aContentService) {
		Assert.isNotNull(aContentService);
		contentService = aContentService;
	}

	@Override
	public String getDescription(Object anElement) {

		Object target;

		if (anElement instanceof IStructuredSelection) {

			IStructuredSelection structuredSelection = (IStructuredSelection) anElement;
			if (structuredSelection.size() > 1) {
				return getDefaultStatusBarMessage(structuredSelection.size());
			}
			target = structuredSelection.getFirstElement();
		} else {
			target = anElement;
		}
		String message = null;
		ILabelProvider[] providers = contentService
				.findRelevantLabelProviders(target);
		if (providers.length == 0) {
			return getDefaultStatusBarMessage(0);
		}
		for (int i = 0; i < providers.length && (message == null || message.length() == 0); i++) {
			if (providers[i] instanceof ICommonLabelProvider) {
				message = ((ICommonLabelProvider) providers[i])
						.getDescription(target);
			}
		}
		return (message != null) ? message : getDefaultStatusBarMessage(1);

	}

	/**
	 * @param aSize
	 *            The number of items selected.
	 * @return A string of the form "# item(s) selected"
	 */
	protected final String getDefaultStatusBarMessage(int aSize) {
		return NLS.bind(aSize != 1 ? CommonNavigatorMessages.Navigator_statusLineMultiSelect
				: CommonNavigatorMessages.Navigator_statusLineSingleSelect,
				new Object[] { Integer.valueOf(aSize) });

	}

}
