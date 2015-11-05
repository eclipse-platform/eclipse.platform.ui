/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
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

	/**
	 * Provides a rough indicator of how good of a match this entry was to its
	 * filter. Lower values indicate better match quality. A value of 0
	 * indicates the filter string was an exact match to the label or that there
	 * is no filter being applied.
	 */
	private int matchQuality;

	/**
	 * Indicates the filter string was a perfect match to the label or there is
	 * no filter applied
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_PERFECT = 0;

	/**
	 * Indicates this entry is very relevant for the filter string. Recommended
	 * value for when the filter was found at the start of the element's label
	 * or a complete case sensitive camel case match.
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_EXCELLENT = 5;

	/**
	 * Indicates this entry is relevant for the filter string. Recommended value
	 * for when the complete filter was found somewhere inside the element's
	 * label or provider.
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_GOOD = 10;

	/**
	 * Indicates only part of the filter string matches to the element's label.
	 *
	 * @see #getMatchQuality()
	 */
	public static final int MATCH_PARTIAL = 15;

	/**
	 * Creates a new quick access entry from the given element and provider. If
	 * no filter was used to match this entry the element/provider match regions
	 * may be empty and the match quality should be {@link #MATCH_PERFECT}
	 *
	 * @param element
	 *            the element this entry will represent
	 * @param provider
	 *            the provider that owns this entry
	 * @param elementMatchRegions
	 *            list of text regions the filter string matched in the element
	 *            label, possibly empty
	 * @param providerMatchRegions
	 *            list of text regions the filter string matches in the provider
	 *            label, possible empty
	 * @param matchQuality
	 *            a rough indication of how closely the filter matched, lower
	 *            values indicate a better match. It is recommended to use the
	 *            constants available on this class: {@link #MATCH_PERFECT},
	 *            {@link #MATCH_EXCELLENT}, {@link #MATCH_GOOD},
	 *            {@link #MATCH_PARTIAL}
	 */
	QuickAccessEntry(QuickAccessElement element, QuickAccessProvider provider,
			int[][] elementMatchRegions, int[][] providerMatchRegions, int matchQuality) {
		this.element = element;
		this.provider = provider;
		this.elementMatchRegions = elementMatchRegions;
		this.providerMatchRegions = providerMatchRegions;
		this.matchQuality = matchQuality;
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

	public void measure(Event event, TextLayout textLayout,
			ResourceManager resourceManager, TextStyle boldStyle) {
		Table table = ((TableItem) event.item).getParent();
		textLayout.setFont(table.getFont());
		event.width = 0;
		switch (event.index) {
		case 0:
			if (firstInCategory || providerMatchRegions.length > 0) {
				textLayout.setText(provider.getName());
				if (boldStyle != null) {
					for (int i = 0; i < providerMatchRegions.length; i++) {
						int[] matchRegion = providerMatchRegions[i];
						textLayout.setStyle(boldStyle, matchRegion[0],
								matchRegion[1]);
					}
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
			if (boldStyle != null) {
				for (int i = 0; i < elementMatchRegions.length; i++) {
					int[] matchRegion = elementMatchRegions[i];
					textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
				}
			}
			break;
		}
		Rectangle rect = textLayout.getBounds();
		event.width += rect.width + 4;
		event.height = Math.max(event.height, rect.height + 2);
	}

	public void paint(Event event, TextLayout textLayout,
			ResourceManager resourceManager, TextStyle boldStyle, Color grayColor) {
		final Table table = ((TableItem) event.item).getParent();
		textLayout.setFont(table.getFont());
		switch (event.index) {
		case 0:
			if (firstInCategory || providerMatchRegions.length > 0) {
				textLayout.setText(provider.getName());
				if (boldStyle != null) {
					for (int i = 0; i < providerMatchRegions.length; i++) {
						int[] matchRegion = providerMatchRegions[i];
						textLayout.setStyle(boldStyle, matchRegion[0],
								matchRegion[1]);
					}
				}
				if (grayColor != null && providerMatchRegions.length > 0 && !firstInCategory) {
					event.gc.setForeground(grayColor);
				}
				Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
				Rectangle requiredBounds = textLayout.getBounds();
				textLayout.draw(event.gc, availableBounds.x + 1, availableBounds.y
						+ (availableBounds.height - requiredBounds.height) / 2);
			}
			break;
		case 1:
			String label = element.getLabel();
			if (element instanceof CommandElement) {
				CommandElement commandElement = (CommandElement) element;
				String binding = commandElement.getBinding();
				if (binding != null) {
					StyledString styledString = StyledCellLabelProvider.styleDecoratedString(label,
							StyledString.QUALIFIER_STYLER, new StyledString(commandElement
									.getCommand()));
					StyleRange[] styleRanges = styledString.getStyleRanges();
					for (int i = 0; i < styleRanges.length; i++) {
						textLayout.setStyle(styleRanges[i], styleRanges[i].start,
								styleRanges[i].start + styleRanges[i].length);
					}
				}
			}
			Image image = getImage(element, resourceManager);
			event.gc.drawImage(image, event.x + 1, event.y + 1);
			textLayout.setText(label);
			if (boldStyle != null) {
				for (int i = 0; i < elementMatchRegions.length; i++) {
					int[] matchRegion = elementMatchRegions[i];
					textLayout.setStyle(boldStyle, matchRegion[0], matchRegion[1]);
				}
			}
			Rectangle availableBounds = ((TableItem) event.item).getTextBounds(event.index);
			Rectangle requiredBounds = textLayout.getBounds();
			textLayout.draw(event.gc, availableBounds.x + 1 + image.getBounds().width, availableBounds.y
					+ (availableBounds.height - requiredBounds.height) / 2);
			break;
		}
		if (lastInCategory) {
			if (grayColor != null)
				event.gc.setForeground(grayColor);
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

	/**
	 * Provides a rough indicator of how good of a match this entry was to its
	 * filter. Lower values indicate better match quality. A value of
	 * {@link #MATCH_PERFECT} indicates the filter string was an exact match to
	 * the label or that there is no filter being applied.
	 *
	 * @return Returns the match quality
	 */
	public int getMatchQuality() {
		return matchQuality;
	}

}