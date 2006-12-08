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

package org.eclipse.ui.tests.api.workbenchpart;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.AbstractDynamicContribution;

/**
 * Test implementation for dynamic menu item support.
 * 
 * @since 3.3
 *
 */
public class DynamicItem extends AbstractDynamicContribution {

	private Action action1;
	private Action action2;

	/**
	 * Default constructor
	 */
	public DynamicItem() {
		makeActions();
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			null,
			"Sample View",
			message);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Dynamic Item 1 executed");
			}
		};
		action1.setText("Dynamic Item 1");
		action1.setToolTipText("Dynamic Item 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Dynamic Item 2 executed");
			}
		};
		action2.setText("Dynamic Item 2");
		action2.setToolTipText("Dynamic Item 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.menus.AbstractDynamicContribution#createContributionItems(java.util.List)
	 */
	public void createContributionItems(List items) {
		items.clear();
		
		ActionContributionItem aci1 = new ActionContributionItem(action1);
		items.add(aci1);
		ActionContributionItem aci2 = new ActionContributionItem(action2);
		items.add(aci2);
	}

}
