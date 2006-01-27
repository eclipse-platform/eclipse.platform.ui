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

package org.eclipse.ui.navigator.internal.filters;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorContentDescriptor;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;

/**
 * @since 3.2
 * 
 */
public class FilterDialogSelectionListener implements ISelectionChangedListener {
	
	
	private Text descriptionText;

	protected FilterDialogSelectionListener(Text aDescriptionText) {
		descriptionText = aDescriptionText;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {

		IStructuredSelection structuredSelection = (IStructuredSelection) event
				.getSelection();
		Object element = structuredSelection.getFirstElement();
		if (element instanceof INavigatorContentDescriptor) {
			INavigatorContentDescriptor ncd = (INavigatorContentDescriptor) element;
			String desc = NLS
					.bind(
							CommonNavigatorMessages.CommonFilterSelectionDialog_Hides_all_content_associated,
							new Object[] { ncd.getName() });
			descriptionText.setText(desc);
		} else if (element instanceof ICommonFilterDescriptor) {
			descriptionText.setText(((ICommonFilterDescriptor) element)
					.getDescription());
		}

	}

}
