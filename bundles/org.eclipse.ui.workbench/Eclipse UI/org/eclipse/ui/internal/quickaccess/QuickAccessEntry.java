/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.quickaccess;

import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;

class QuickAccessEntry {
	boolean firstInCategory;
	boolean lastInCategory;
	QuickAccessElement element;
	QuickAccessProvider provider;
	int[][] elementMatchRegions;
	int[][] providerMatchRegions;

	QuickAccessEntry(QuickAccessElement element, QuickAccessProvider provider,
			int[][] elementMatchRegions, int[][] providerMatchRegions) {
		this.element = element;
		this.provider = provider;
		this.elementMatchRegions = elementMatchRegions;
		this.providerMatchRegions = providerMatchRegions;
	}

	Image getImage(QuickAccessElement element, ResourceManager resourceManager) {
		Image image = findOrCreateImage(element.getImageDescriptor(),
				resourceManager);
		if (image == null) {
			image = WorkbenchImages
					.getImage(IWorkbenchGraphicConstants.IMG_OBJ_ELEMENT);
		}
		return image;
	}

	private Image findOrCreateImage(ImageDescriptor imageDescriptor,
			ResourceManager resourceManager) {
		if (imageDescriptor == null) {
			return null;
		}
		Image image = (Image) resourceManager.find(imageDescriptor);
		if (image == null) {
			try {
				image = resourceManager.createImage(imageDescriptor);
			} catch (DeviceResourceException e) {
				WorkbenchPlugin.log(e);
			}
		}
		return image;
	}

	/**
	 * @param event
	 * @param boldStyle
	 */
	public void measure(Event event, TextLayout textLayout,
			ResourceManager resourceManager, TextStyle boldStyle) {
		Table table = ((TableItem) event.item).getParent();
		textLayout.setFont(table.getFont());
		event.width = 0;
		switch (event.index) {
		case 0:
			if (firstInCategory || providerMatchRegions.length > 0) {
				textLayout.setText(provider.getName());
				for (int i = 0; i < providerMatchRegions.length; i++) {
					int[] matchRegion = providerMatchRegions[i];
					textLayout.setStyle(boldStyle, matchRegion[0],
							matchRegion[1]);
				}
			} else {
				textLayout.setText(""); //$NON-NLS-1$
			}
			break;
		case 1:
			Image image = getImage(element, resourceManager);
			Rectangle imageRect = image.getBounds();
			event.width += imageRect.width + 4;
			event.height = Math.max(event.height, imageRect.height + 2);
			textLayout.setText(element.getLabel());
			for (int i = 0; i < elementMatchRegions.length; i++) {
				int[] matchRegion = elementMatchRegions[i];
				textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
			}
			break;
		}
		Rectangle rect = textLayout.getBounds();
		event.width += rect.width + 4;
		event.height = Math.max(event.height, rect.height + 2);
	}

	/**
	 * @param event
	 * @param textLayout
	 * @param resourceManager
	 * @param boldStyle
	 */
	public void paint(Event event, TextLayout textLayout,
			ResourceManager resourceManager, TextStyle boldStyle, Color grayColor) {
		final Table table = ((TableItem) event.item).getParent();
		textLayout.setFont(table.getFont());
		switch (event.index) {
		case 0:
			if (firstInCategory || providerMatchRegions.length > 0) {
				textLayout.setText(provider.getName());
				for (int i = 0; i < providerMatchRegions.length; i++) {
					int[] matchRegion = providerMatchRegions[i];
					textLayout.setStyle(boldStyle, matchRegion[0],
							matchRegion[1]);
				}
				if (providerMatchRegions.length > 0 && !firstInCategory) {
					event.gc.setForeground(grayColor);
				}
				Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
				Rectangle requiredBounds = textLayout.getBounds();
				textLayout.draw(event.gc, availableBounds.x + 1, availableBounds.y
						+ (availableBounds.height - requiredBounds.height) / 2);
			}
			break;
		case 1:
			Image image = getImage(element, resourceManager);
			event.gc.drawImage(image, event.x + 1, event.y + 1);
			textLayout.setText(element.getLabel());
			for (int i = 0; i < elementMatchRegions.length; i++) {
				int[] matchRegion = elementMatchRegions[i];
				textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
			}
			Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
			Rectangle requiredBounds = textLayout.getBounds();
			textLayout.draw(event.gc, availableBounds.x + 1 + image.getBounds().width, availableBounds.y
					+ (availableBounds.height - requiredBounds.height) / 2);
			break;
		}
		if (lastInCategory) {
			event.gc.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_GRAY));
			Rectangle bounds = ((TableItem)event.item).getBounds(event.index);
			event.gc.drawLine(Math.max(0, bounds.x - 1), bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y
					+ bounds.height - 1);
		}
	}

	/**
	 * @param event
	 */
	public void erase(Event event) {
		// We are only custom drawing the foreground.
		event.detail &= ~SWT.FOREGROUND;
	}
}