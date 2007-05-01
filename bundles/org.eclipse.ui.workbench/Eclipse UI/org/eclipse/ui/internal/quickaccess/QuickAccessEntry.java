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
		switch (event.index) {
		case 0:
			if (firstInCategory || providerMatchRegions.length > 0) {
				textLayout.setText(provider.getName());
				for (int i = 0; i < providerMatchRegions.length; i++) {
					int[] matchRegion = providerMatchRegions[i];
					textLayout.setStyle(boldStyle, matchRegion[0],
							matchRegion[1]);
				}
			}
			break;
		case 1:
			Image image = getImage(element, resourceManager);
			Rectangle imageRect = image.getBounds();
			event.width += imageRect.width + 2;
			event.height = Math.max(event.height, imageRect.height + 2);
			textLayout.setText(element.getLabel());
			for (int i = 0; i < elementMatchRegions.length; i++) {
				int[] matchRegion = elementMatchRegions[i];
				textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
			}
			break;
		}
		Rectangle rect = textLayout.getBounds();
		event.width += rect.width;
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
		Color oldForeground = event.gc.getForeground();
		boolean selected = (event.detail & SWT.SELECTED) != 0;
		final Table table = ((TableItem) event.item).getParent();
		textLayout.setFont(table.getFont());
		boolean hasFocus = table.isFocusControl();
		event.gc.setForeground(hasFocus && selected ? table.getDisplay()
				.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT) : table
				.getForeground());
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
				textLayout.draw(event.gc, event.x + 1, event.y + 1);
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
			textLayout.draw(event.gc, event.x + 3 + image.getBounds().width,
					event.y + 2);
			break;
		}
		if (lastInCategory) {
			event.gc.setForeground(table.getDisplay().getSystemColor(SWT.COLOR_GRAY));
			Rectangle bounds = ((TableItem)event.item).getBounds(event.index);
			event.gc.drawLine(Math.max(0, bounds.x - 1), bounds.y + bounds.height - 1, bounds.x + bounds.width, bounds.y
					+ bounds.height - 1);
		}
		event.gc.setForeground(oldForeground);
	}

	/**
	 * @param event
	 */
	public void erase(Event event) {
		Rectangle bounds = event.getBounds();
		Color oldForeground = event.gc.getForeground();
		final Table table = ((TableItem) event.item).getParent();
		if ((event.detail & SWT.SELECTED) != 0) {
			Color oldBackground = event.gc.getBackground();

			final boolean hasFocus = table.isFocusControl();
			Color background = hasFocus ? table.getDisplay()
					.getSystemColor(SWT.COLOR_LIST_SELECTION) : table
					.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			event.gc.setBackground(background);
			event.gc.setForeground(event.item.getDisplay().getSystemColor(
					SWT.COLOR_LIST_SELECTION_TEXT));
			event.gc.fillRectangle(bounds);
			/* restore the old GC colors */
			event.gc.setForeground(oldForeground);
			event.gc.setBackground(oldBackground);
			/* ensure that default selection is not drawn */
			event.detail &= ~SWT.SELECTED;
		}
		event.detail &= ~SWT.FOREGROUND;
		if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
			event.detail &= ~SWT.FOCUSED;
		}
		event.detail &= ~SWT.FOREGROUND;
	}
}