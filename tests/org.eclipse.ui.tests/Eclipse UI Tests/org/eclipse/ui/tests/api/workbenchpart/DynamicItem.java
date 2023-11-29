/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.tests.api.workbenchpart;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;

/**
 * Test implementation for dynamic menu item support.
 *
 * @since 3.3
 */
public class DynamicItem extends CompoundContributionItem {

	private Action action1;
	private Action action2;

	/**
	 * Default constructor
	 */
	public DynamicItem() {
		makeActions();
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(null, "Sample View", message);
	}

	private void makeActions() {
		action1 = new Action() {
			@Override
			public void run() {
				showMessage("Dynamic Item 1 executed");
			}
		};
		action1.setText("Dynamic Item 1");
		action1.setToolTipText("Dynamic Item 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			@Override
			public void run() {
				showMessage("Dynamic Item 2 executed");
			}
		};
		action2.setText("Dynamic Item 2");
		action2.setToolTipText("Dynamic Item 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		return new IContributionItem[] { new ActionContributionItem(action1),
				new ActionContributionItem(action2) };
	}
}
