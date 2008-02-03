/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Michael Krkoska - initial API and implementation (bug 188333)
 *******************************************************************************/
package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeItem;

/**
 * A {@link SimpleStyledCellLabelProvider} supports styled labels by using owner
 * draw by preserving native viewer behavior:
 * <ul>
 * <li>similar image and label positioning</li>
 * <li>native drawing of focus and selection</li>
 * </ul>
 * 
 * 
 * <p>
 * For providing the label's styles, create a subclass and overwrite
 * {@link SimpleStyledCellLabelProvider#getLabelPresentationInfo(Object)} to
 * return all information needed to render a element.
 * </p>
 * <p>
 * The {@link SimpleStyledCellLabelProvider} will ignore all font settings on
 * {@link StyleRange}. Different fonts would make labels wider, and the native
 * selection drawing could not be reused.
 * </p>
 * 
 * <p><strong>NOTE:</strong> This API is experimental and may be deleted or
 * changed before 3.4 is released.</p>
 * 
 * @since 3.4
 */
public abstract class SimpleStyledCellLabelProvider extends
		OwnerDrawLabelProvider {

	/**
	 * Holds all information used to render a styled element.
	 */
	public static class LabelPresentationInfo {

		private final String text;
		private final Image image;
		private final StyleRange[] ranges;

		private final Font defaultFont;
		private final Color defaultForegroundColor;
		private final Color defaultBackgroundColor;

		/**
		 * Creates a {@link SimpleStyledCellLabelProvider.LabelPresentationInfo}.
		 * 
		 * @param text
		 *            the text of the current element
		 * @param ranges
		 *            the styled ranges for the element
		 * @param image
		 *            the image for the element or <code>null</code>
		 * @param defaultFont
		 *            the default font for the element or <code>null</code>
		 * @param defaultForegroundColor
		 *            the default foreground color for the element or
		 *            <code>null</code>
		 * @param defaultBackgroundColor
		 *            the default background color for the element or
		 *            <code>null</code>
		 */
		public LabelPresentationInfo(String text, StyleRange[] ranges,
				Image image, Font defaultFont, Color defaultForegroundColor,
				Color defaultBackgroundColor) {
			Assert.isNotNull(text);
			Assert.isNotNull(ranges);
			this.text = text;
			this.ranges = ranges;
			this.image = image;
			this.defaultFont = defaultFont;
			this.defaultForegroundColor = defaultForegroundColor;
			this.defaultBackgroundColor = defaultBackgroundColor;
		}

		/**
		 * Provides the text of the current element.
		 * 
		 * @return returns the text.
		 */
		public String getText() {
			return this.text;
		}

		/**
		 * Provides the styled ranges that can be applied to the text provided
		 * by {@link #getText()}. The {@link SimpleStyledCellLabelProvider}
		 * will ignore all font settings.
		 * 
		 * @return the styled ranges for the element
		 */
		public StyleRange[] getStyleRanges() {
			return this.ranges;
		}

		/**
		 * Provides the image of the current element.
		 * 
		 * @return returns the image.
		 */
		public Image getImage() {
			return this.image;
		}

		/**
		 * Provides a default background color of the current element, which is
		 * used for the part of the label where no background color is specified
		 * in the StyleRanges provided by {@link #getStyleRanges}.
		 * 
		 * @return the background color for the element, or <code>null</code>
		 *         to use the default background color
		 */
		public Color getDefaultBackground() {
			return this.defaultBackgroundColor;
		}

		/**
		 * Provides a default font of the current element.
		 * 
		 * @return the font for the element, or <code>null</code> to use the
		 *         default font
		 */
		public Font getDefaultFont() {
			return this.defaultFont;
		}

		/**
		 * Provides a default foreground color of the current element, which is
		 * used for the part of the label where no foreground color is specified
		 * in the StyleRanges provided by {@link #getStyleRanges}.
		 * 
		 * @return the foreground color for the element, or <code>null</code>
		 *         to use the default foreground color
		 */
		public Color getDefaultForeground() {
			return this.defaultForegroundColor;
		}

	}

	private static final String KEY_TEXT_LAYOUT = "styled_label_key_"; //$NON-NLS-1$

	/**
	 * Style constant for indicating that the styled colors are to be applied
	 * even it the viewer's item is selected. Default is not to apply colors.
	 */
	public static final int COLORS_ON_SELECTION = 1 << 0;

	/**
	 * Style constant for indicating to draw the focus if requested by the owner
	 * draw event. Default is to draw the focus.
	 */
	public static final int NO_FOCUS = 1 << 1;
	
	/**
	 * Private constant to indicate if owner draw is enabled for the
	 * label provider's column.
	 */
	private static final int OWNER_DRAW_ENABLED = 1 << 4;

	private int style;

	private TextLayout cachedTextLayout; // reused text layout for
											// 'cachedLabelInfo'
	private LabelPresentationInfo cachedLabelInfo;
	private boolean cachedWasWithColors;
	
	private ColumnViewer viewer;
	private ViewerColumn column;

	/**
	 * Creates a new StyledCellLabelProvider. By default, owner draw is enabled, focus is drawn and no 
	 * colors are painted on selected elements.
	 */
	public SimpleStyledCellLabelProvider() {
		this(0);
	}

	/**
	 * Creates a new StyledCellLabelProvider. By default, owner draw is enabled.
	 * 
	 * @param style
	 *            the style bits
	 * @see SimpleStyledCellLabelProvider#COLORS_ON_SELECTION
	 * @see SimpleStyledCellLabelProvider#NO_FOCUS
	 */
	public SimpleStyledCellLabelProvider(int style) {
		this.style = style & (COLORS_ON_SELECTION | NO_FOCUS)
							| OWNER_DRAW_ENABLED;
	}
	
	/**
	 * Returns <code>true</code> is the owner draw rendering is enabled for this label provider.
	 * By default owner draw rendering is enabled. If owner draw rendering is disabled, rending is 
	 * done by the viewer and no styled ranges (see {@link LabelPresentationInfo#getStyleRanges()})
	 * are drawn.
	 * 
	 * @return <code>true</code> is the rendering of styles is enabled. 
	 */
	public boolean isOwnerDrawEnabled() {
		return (this.style & OWNER_DRAW_ENABLED) != 0;
	}
	
	/**
	 * Specifies whether owner draw rendering is enabled for this label
	 * provider. By default owner draw rendering is enabled. If owner draw
	 * rendering is disabled, rendering is done by the viewer and no styled
	 * ranges (see {@link LabelPresentationInfo#getStyleRanges()}) are drawn.
	 * It is the caller's responsibility to also call
	 * {@link StructuredViewer#refresh()} or similar methods to update the
	 * underlying widget.
	 * 
	 * @param enabled
	 *            specifies if owner draw rendering is enabled
	 */
	public void setOwnerDrawEnabled(boolean enabled) {
		boolean isEnabled= isOwnerDrawEnabled();
		if (isEnabled != enabled) {
			if (enabled) {
				this.style |= OWNER_DRAW_ENABLED;
			} else {
				this.style &= ~OWNER_DRAW_ENABLED;
			}
			if (this.viewer != null) {
				setOwnerDrawEnabled(this.viewer, this.column, enabled);
			}
		}
	}
	
	/**
	 * Returns the viewer on which this label provider is installed on or <code>null</code> if the
	 * label provider is not installed.
	 * 
	 * @return the viewer on which this label provider is installed on or <code>null</code> if the
	 * label provider is not installed.
	 */
	protected final ColumnViewer getViewer() {
		return this.viewer;
	}
	
	/**
	 * Returns the column on which this label provider is installed on or <code>null</code> if the
	 * label provider is not installed.
	 * 
	 * @return the column on which this label provider is installed on or <code>null</code> if the
	 * label provider is not installed.
	 */
	protected final ViewerColumn getColumn() {
		return this.column;
	}
	

	/**
	 * Returns a {@link LabelPresentationInfo} instance containing the text,
	 * image and style information to use for displaying element.
	 * 
	 * @param element
	 *            the element to create a presentation info for
	 * @return the presentation info
	 */
	protected abstract LabelPresentationInfo getLabelPresentationInfo(
			Object element);

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#initialize(org.eclipse.jface.viewers.ColumnViewer, org.eclipse.jface.viewers.ViewerColumn)
	 */
	public void initialize(ColumnViewer viewer, ViewerColumn column) {
		Assert.isTrue(this.viewer == null && this.column == null, "Label provider instance already in use"); //$NON-NLS-1$
		
		this.viewer= viewer;
		this.column= column;
		super.initialize(viewer, column, isOwnerDrawEnabled());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
	 */
	public void dispose() {
		if (this.cachedTextLayout != null) {
			cachedTextLayout.dispose();
			cachedTextLayout = null;
		}
		cachedLabelInfo = null;
	
		this.viewer= null;
		this.column= null;
		
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#update(org.eclipse.jface.viewers.ViewerCell)
	 */
	public void update(ViewerCell cell) {
		LabelPresentationInfo info = getLabelPresentationInfo(cell.getElement());

		cell.setImage(info.getImage());
		cell.setText(info.getText());
		cell.setFont(info.getDefaultFont());
		cell.setBackground(info.getDefaultBackground());
		cell.setForeground(info.getDefaultForeground());
		
		if (isOwnerDrawEnabled()) {
			// store info in the item to avoid recomputation
			cell.getItem().setData(KEY_TEXT_LAYOUT + cell.getColumnIndex(), info);
		} else {
			// make sure the info is cleared to avoid leaks
			cell.getItem().setData(KEY_TEXT_LAYOUT + cell.getColumnIndex(), null);
		}
		super.update(cell); // calls 'repaint' to trigger the paint listener
	}

	private TextLayout getSharedTextLayout(Display display) {
		if (cachedTextLayout == null) {
			cachedTextLayout = new TextLayout(display);
			cachedTextLayout.setOrientation(Window.getDefaultOrientation());
		}
		return cachedTextLayout;
	}

	private boolean useColors(Event event) {
		return (event.detail & SWT.SELECTED) == 0
				|| (this.style & COLORS_ON_SELECTION) != 0;
	}

	private boolean drawFocus(Event event) {
		return (event.detail & SWT.FOCUSED) != 0
				&& (this.style & NO_FOCUS) == 0;
	}
	
	private LabelPresentationInfo getInfo(Event event) {
		return (LabelPresentationInfo) event.item.getData(KEY_TEXT_LAYOUT + event.index);
	}

	/**
	 * Returns a {@link TextLayout} instance for the given
	 * {@link LabelPresentationInfo}. The text layout instance is managed by
	 * the label provider. Caller of the method must not dispose the text
	 * layout.
	 * 
	 * @param diplay
	 *            the current display
	 * @param labelPresentation
	 *            the viewerLabel the label info
	 * 
	 * @param applyColors
	 *            if set, create colors in the result
	 * @param element
	 *            the model element
	 * @param item
	 *            the item
	 * @param index
	 *            the index
	 * @return a TextLayout instance
	 */
	private TextLayout getTextLayoutForInfo(Display display,
			LabelPresentationInfo labelPresentation, boolean applyColors, Item item, int index) {
		// can use cache?
		if (cachedLabelInfo == labelPresentation
				&& applyColors == cachedWasWithColors) {
			return cachedTextLayout; // use cached layout
		}

		TextLayout sharedLayout = getSharedTextLayout(display);
		applyInfoToLayout(sharedLayout, labelPresentation, applyColors, item, index);

		cachedLabelInfo = labelPresentation;
		cachedWasWithColors = applyColors;

		return sharedLayout;
	}

	/**
	 * Fills the given text layout with the styles, text and font of the label
	 * info.
	 * 
	 * @param layout
	 *            the text layout to fill
	 * @param labelInfo
	 *            the viewer label
	 * @param applyColors
	 *            if set, colors will be used
	 * @param item
	 *            the item
	 * @param index
	 *            the index
	 */
	private void applyInfoToLayout(TextLayout layout,
			LabelPresentationInfo labelInfo, boolean applyColors, Item item, int index) {
		layout.setText(""); // make sure no previous ranges are kept //$NON-NLS-1$
		layout.setText(labelInfo.getText());
		Font font = labelInfo.getDefaultFont();
		if (font == null) {
			if (item instanceof TableItem) {
				font = ((TableItem)item).getFont(index);
			} else if (item instanceof TreeItem) {
				font = ((TreeItem)item).getFont(index);
			}
		}
		layout.setFont(font); // set also if null to clear previous usages

		StyleRange[] styleRanges = labelInfo.getStyleRanges();

		for (int i = 0; i < styleRanges.length; i++) {
			StyleRange curr = styleRanges[i];

			// if no colors apply or font is set, create a clone and clear the
			// colors and font
			if (curr.font != null || !applyColors
					&& (curr.foreground != null || curr.background != null)) {
				curr = (StyleRange) curr.clone();
				curr.font = null; // ignore font settings until bug 168807 is resolved
				if (!applyColors) {
					curr.foreground = null;
					curr.background = null;
				}
			}
			layout.setStyle(curr, curr.start, curr.start + curr.length - 1);
		}
	}

	/**
	 * Handle the erase event. The default implementation does nothing to ensure
	 * keep native selection highlighting working.
	 * 
	 * @param event
	 *            the erase event
	 * @param element
	 *            the model object
	 * @see SWT#EraseItem
	 */
	protected void erase(Event event, Object element) {
		// use native erase
		
		LabelPresentationInfo labelInfo = getInfo(event);
		if (labelInfo != null) {
			// info has been set by 'update': announce that we paint ourselves
			event.detail &= ~SWT.FOREGROUND;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event,
	 *      java.lang.Object)
	 */
	protected void measure(Event event, Object element) {
		// use native measuring
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#paint(org.eclipse.swt.widgets.Event,
	 *      java.lang.Object)
	 */
	protected void paint(Event event, Object element) {
		LabelPresentationInfo labelInfo = getInfo(event);
		if (labelInfo == null) {
			return; // no info cached: skip this entry, use native painting
		}

		boolean applyColors = useColors(event);
		GC gc = event.gc;
		Color oldForeground = gc.getForeground(); // remember colors to
													// restore the GC later
		Color oldBackground = gc.getBackground();

		if (applyColors) {
			Color foreground = labelInfo.getDefaultForeground();
			if (foreground != null) {
				gc.setForeground(foreground);
			}
			Color background = labelInfo.getDefaultBackground();
			if (background != null) {
				gc.setBackground(background);
			}
		}

		Image image = labelInfo.getImage();
		if (image != null) {
			Rectangle imageBounds = getImageBounds(event);
			Rectangle bounds = image.getBounds();

			// center the image in the given space
			int x = imageBounds.x
					+ Math.max(0, (imageBounds.width - bounds.width) / 2);
			int y = imageBounds.y
					+ Math.max(0, (imageBounds.height - bounds.height) / 2);
			gc.drawImage(image, x, y);
		}

		TextLayout textLayout = getTextLayoutForInfo(event.display, labelInfo,
				applyColors, (Item)event.item, event.index);

		Rectangle layoutBounds = textLayout.getBounds();
		Rectangle textBounds = getTextBounds(event);

		int x = textBounds.x;
		int y = textBounds.y
				+ Math.max(0, (textBounds.height - layoutBounds.height) / 2);

		textLayout.draw(gc, x, y);

		if (drawFocus(event)) {
			Rectangle focusBounds = getBounds(event);
			gc.drawFocus(focusBounds.x, focusBounds.y, focusBounds.width,
					focusBounds.height);
		}

		gc.setForeground(oldForeground);
		gc.setBackground(oldBackground);
	}

	private Rectangle getBounds(Event event) {
		Item item = (Item) event.item;
		if (item instanceof TreeItem) {
			return ((TreeItem) item).getBounds();
		} else if (item instanceof TableItem) {
			return ((TableItem) item).getBounds();
		}
		return null;
	}

	private Rectangle getImageBounds(Event event) {
		Item item = (Item) event.item;
		if (item instanceof TreeItem) {
			return ((TreeItem) item).getImageBounds(event.index);
		} else if (item instanceof TableItem) {
			return ((TableItem) item).getImageBounds(event.index);
		}
		return null;
	}

	private Rectangle getTextBounds(Event event) {
		Item item = (Item) event.item;
		if (item instanceof TreeItem) {
			return ((TreeItem) item).getTextBounds(event.index);
		} else if (item instanceof TableItem) {
			return ((TableItem) item).getTextBounds(event.index);
		}
		return null;
	}

}
