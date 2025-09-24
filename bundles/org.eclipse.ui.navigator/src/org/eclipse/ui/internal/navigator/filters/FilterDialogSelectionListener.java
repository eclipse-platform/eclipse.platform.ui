/*******************************************************************************
 * Copyright (c) 2009, 2018 IBM Corporation and others.
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

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;

/**
 * @since 3.2
 */
public class FilterDialogSelectionListener implements ISelectionChangedListener {


	private final Label descriptionText;

	protected FilterDialogSelectionListener(Label aDescriptionText) {
		descriptionText = aDescriptionText;

	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		IStructuredSelection structuredSelection = event.getStructuredSelection();
		Object element = structuredSelection.getFirstElement();
		if (element instanceof INavigatorContentDescriptor ncd) {
			String desc = NLS
					.bind(
							CommonNavigatorMessages.CommonFilterSelectionDialog_Hides_all_content_associated,
							new Object[] { ncd.getName() });
			descriptionText.setText(desc);
		} else if (element instanceof ICommonFilterDescriptor cfd) {
			String description = 	cfd.getDescription();
			if(description != null) {
				descriptionText.setText(description);
			} else {
				descriptionText.setText(NLS.bind(CommonNavigatorMessages.FilterDialogSelectionListener_Enable_the_0_filter_, cfd.getName()));
			}
		}

	}
}
