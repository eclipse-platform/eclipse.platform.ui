/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.views.properties.tabbed.decorations;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyComposite;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabItem;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class TabbedPropertySheetPageWithDecorations extends
		TabbedPropertySheetPage {

	private boolean useDecorations;

	private Image image;

	private Color color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	public TabbedPropertySheetPageWithDecorations(
			ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
		super(tabbedPropertySheetPageContributor);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection.equals(getCurrentSelection())) {
			return;
		}
		super.selectionChanged(part, selection);
		if (useDecorations) {
			/*
			 * Call ListElement's showDynamicImage(), hideDynamicImage(),
			 * setTextColor() and setDefaultTextColor() methods to make sure
			 * that they don't throw any exceptions.
			 */
			TabbedPropertyList tabbedPropertyList = ((TabbedPropertyComposite) this
					.getControl()).getList();
			for (int i = 0; i < tabbedPropertyList.getNumberOfElements(); i++) {
				TabbedPropertyList.ListElement tabListElement = (TabbedPropertyList.ListElement) tabbedPropertyList
						.getElementAt(i);
				if (tabListElement != null) {
					ITabItem tab = tabListElement.getTabItem();
					if (tab.getText().equals("Name")) {
						/*
						 * The Name tab can have 5 images. Check boundary
						 * conditions to make sure that the code does not throw
						 * IndexOutOfBoundsException.
						 */
						tabListElement.showDynamicImage(-1, image);
						tabListElement.hideDynamicImage(-1);

						tabListElement.showDynamicImage(0, image);
						tabListElement.hideDynamicImage(0);

						tabListElement.showDynamicImage(2, image);
						tabListElement.hideDynamicImage(2);

						tabListElement.showDynamicImage(4, image);
						tabListElement.hideDynamicImage(4);

						tabListElement.showDynamicImage(5, image);
						tabListElement.hideDynamicImage(5);

						tabListElement.showDynamicImage(7, image);
						tabListElement.hideDynamicImage(7);

						/*
						 * Set and reset the tab-label's color. Make sure that
						 * the code does not throw NullPointerException.
						 */
						tabListElement.setTextColor(null);
						tabListElement.setTextColor(color);
						tabListElement.setDefaultTextColor();
					} else if (tab.getText().equals("Message")) {
						/*
						 * The Name tab can have 3 images. Check boundary
						 * conditions to make sure that the code does not throw
						 * IndexOutOfBoundsException.
						 */
						tabListElement.showDynamicImage(-1, image);
						tabListElement.hideDynamicImage(-1);

						tabListElement.showDynamicImage(0, image);
						tabListElement.hideDynamicImage(0);

						tabListElement.showDynamicImage(1, image);
						tabListElement.hideDynamicImage(1);

						tabListElement.showDynamicImage(2, image);
						tabListElement.hideDynamicImage(2);

						tabListElement.showDynamicImage(3, image);
						tabListElement.hideDynamicImage(3);

						tabListElement.showDynamicImage(7, image);
						tabListElement.hideDynamicImage(7);
					}
				}
			}
		}
	}

	@Override
	protected void updateTabs(ITabDescriptor[] descriptors) {
		super.updateTabs(descriptors);
		if (useDecorations) {
			// Set the number of decoration-images in the TabbedPropertyList
			TabbedPropertyList tabbedPropertyList = ((TabbedPropertyComposite) this
					.getControl()).getList();
			Map tabToImageDecorationsMap = getImageDecorationsForTabs(descriptors);
			tabbedPropertyList.setDynamicImageCount(tabToImageDecorationsMap);
		}
	}

	private Map getImageDecorationsForTabs(ITabItem[] tabItems) {
		Map tabToImageDecorationsMap = new HashMap();
		for (int i = 0; i < tabItems.length; i++) {
			if (tabItems[i].getText().equals("Name")) {
				tabToImageDecorationsMap.put(tabItems[i], Integer.valueOf(5));
			} else if (tabItems[i].getText().equals("Message")) {
				tabToImageDecorationsMap.put(tabItems[i], Integer.valueOf(3));
			} else {
				tabToImageDecorationsMap.put(tabItems[i], Integer.valueOf(0));
			}
		}
		return tabToImageDecorationsMap;
	}

	public void useDecorations(boolean value) {
		this.useDecorations = value;
	}
}
