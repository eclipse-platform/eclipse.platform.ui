/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs.cpd;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog.DisplayItem;

/**
 * A check listener which, upon changing the check state of a contribution
 * item, checks if that item is eligible to be checked (i.e. it is in an
 * available action set), and if not, informs the user of the illegal
 * operation. If the operation is legal, the event is forwarded to the check
 * listener to actually perform a useful action.
 *
 * @since 3.5
 */
class UnavailableContributionItemCheckListener implements ICheckStateListener {

	private final CustomizePerspectiveDialog dialog;
	private CheckboxTreeViewer viewer;
	private ICheckStateListener originalListener;

	/**
	 * @param viewer
	 *            the viewer being listened to
	 * @param originalListener
	 *            the listener to invoke upon a legal action
	 * @param dialog
	 *            parent
	 */
	public UnavailableContributionItemCheckListener(CustomizePerspectiveDialog dialog, CheckboxTreeViewer viewer,
			ICheckStateListener originalListener) {
		this.dialog = dialog;
		this.viewer = viewer;
		this.originalListener = originalListener;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		DisplayItem item = (DisplayItem) event.getElement();
		ViewerFilter[] filters = viewer.getFilters();
		boolean isEffectivelyAvailable = CustomizePerspectiveDialog.isEffectivelyAvailable(item, filters.length > 0 ? filters[0] : null);

		if (isEffectivelyAvailable) {
			// legal action - invoke the listener which will do actual work
			originalListener.checkStateChanged(event);
			return;
		}

		boolean isAvailable = CustomizePerspectiveDialog.isAvailable(item);
		viewer.update(event.getElement(), null);

		if (isAvailable) {
			// the case where this item is unavailable because of its
			// children
			if (!viewer.getExpandedState(item)) {
				viewer.expandToLevel(item, AbstractTreeViewer.ALL_LEVELS);
			}
			MessageBox mb = new MessageBox(viewer.getControl().getShell(), SWT.OK | SWT.ICON_WARNING | SWT.SHEET);
			mb.setText(WorkbenchMessages.HideItemsCannotMakeVisible_dialogTitle);
			mb.setMessage(NLS.bind(WorkbenchMessages.HideItemsCannotMakeVisible_unavailableChildrenText,
					item.getLabel()));
			mb.open();
		} else {
			// the case where this item is unavailable because it belongs to
			// an unavailable action set
			MessageBox mb = new MessageBox(viewer.getControl().getShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING
					| SWT.SHEET);
			mb.setText(WorkbenchMessages.HideItemsCannotMakeVisible_dialogTitle);
			final String errorExplanation = NLS.bind(
					WorkbenchMessages.HideItemsCannotMakeVisible_unavailableCommandGroupText, item.getLabel(),
					item.getActionSet());
			final String message = NLS
					.bind("{0}{1}{1}{2}", new Object[] { errorExplanation, CustomizePerspectiveDialog.NEW_LINE, WorkbenchMessages.HideItemsCannotMakeVisible_switchToCommandGroupTab }); //$NON-NLS-1$
			mb.setMessage(message);
			if (mb.open() == SWT.YES) {
				dialog.showActionSet(item);
			}
		}
	}

}