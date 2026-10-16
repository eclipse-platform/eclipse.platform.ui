/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

package org.eclipse.ui.internal;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;

/**
 * Menu contribution item which shows a working set.
 *
 * @since 2.1
 */
public class WorkingSetMenuContributionItem extends ContributionItem {
	private static final String RESOURCE_MANAGER_KEY = WorkingSetMenuContributionItem.class.getName();

	private final int id;

	private final IWorkingSet workingSet;

	private final WorkingSetFilterActionGroup actionGroup;

	/**
	 * Returns the id of this menu contribution item
	 *
	 * @param id numerical id
	 * @return String string id
	 */
	public static String getId(int id) {
		return WorkingSetMenuContributionItem.class.getName() + "." + id; //$NON-NLS-1$
	}

	/**
	 * Creates a new instance of the receiver.
	 *
	 * @param id          sequential id of the new instance
	 * @param actionGroup the action group this contribution item is created in
	 */
	public WorkingSetMenuContributionItem(int id, WorkingSetFilterActionGroup actionGroup, IWorkingSet workingSet) {
		super(getId(id));
		Assert.isNotNull(actionGroup);
		Assert.isNotNull(workingSet);
		this.id = id;
		this.actionGroup = actionGroup;
		this.workingSet = workingSet;
	}

	/**
	 * Adds a menu item for the working set. Overrides method from ContributionItem.
	 *
	 * @see org.eclipse.jface.action.ContributionItem#fill(Menu,int)
	 */
	@Override
	public void fill(Menu menu, int index) {
		MenuItem mi = new MenuItem(menu, SWT.RADIO, index);
		mi.setText("&" + id + " " + workingSet.getLabel()); //$NON-NLS-1$ //$NON-NLS-2$
		mi.setSelection(workingSet.equals(actionGroup.getWorkingSet()));
		mi.addSelectionListener(widgetSelectedAdapter(e -> {
			IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
			actionGroup.setWorkingSet(workingSet);
			manager.addRecentWorkingSet(workingSet);
		}));
		ImageDescriptor imageDescriptor = workingSet.getImageDescriptor();
		if (imageDescriptor != null) {
			mi.setImage(getImage(menu, imageDescriptor));
		}
	}

	/**
	 * Returns the image for the descriptor, cached for the lifetime of the menu
	 * because dynamic items are recreated on every menu update.
	 */
	private static Image getImage(Menu menu, ImageDescriptor descriptor) {
		ResourceManager resourceManager = (ResourceManager) menu.getData(RESOURCE_MANAGER_KEY);
		if (resourceManager == null) {
			LocalResourceManager localManager = new LocalResourceManager(JFaceResources.getResources());
			menu.addDisposeListener(e -> localManager.dispose());
			menu.setData(RESOURCE_MANAGER_KEY, localManager);
			resourceManager = localManager;
		}
		Image image = resourceManager.find(descriptor);
		if (image == null) {
			try {
				image = resourceManager.create(descriptor);
			} catch (DeviceResourceException e) {
				image = resourceManager.create(ImageDescriptor.getMissingImageDescriptor());
			}
		}
		return image;
	}

	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	@Override
	public boolean isDynamic() {
		return true;
	}
}
