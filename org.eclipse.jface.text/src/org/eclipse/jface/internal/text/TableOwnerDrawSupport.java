/*******************************************************************************
 * Copyright (c) 2008, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;


/**
 * Adds owner draw support for tables.
 *
 * @since 3.4
 */
public class TableOwnerDrawSupport implements Listener {

	private static final String STYLED_RANGES_KEY= "styled_ranges"; //$NON-NLS-1$

	// shared text layout
	private TextLayout fSharedLayout;

	private int fDeltaOfLastMeasure;

	public static void install(Table table) {
		TableOwnerDrawSupport listener= new TableOwnerDrawSupport(table);
		table.addListener(SWT.Dispose, listener);
		table.addListener(SWT.MeasureItem, listener);
		table.addListener(SWT.EraseItem, listener);
		table.addListener(SWT.PaintItem, listener);
	}

	/**
	 * Stores the styled ranges in the given table item.
	 *
	 * @param item table item
	 * @param column the column index
	 * @param ranges the styled ranges or <code>null</code> to remove them
	 */
	public static void storeStyleRanges(TableItem item, int column, StyleRange[] ranges) {
		item.setData(STYLED_RANGES_KEY + column, ranges);
	}

	/**
	 * Returns the styled ranges which are stored in the given table item.
	 *
	 * @param item table item
	 * @param column the column index
	 * @return the styled ranges
	 */
	private static StyleRange[] getStyledRanges(TableItem item, int column) {
		return (StyleRange[])item.getData(STYLED_RANGES_KEY + column);
	}

	private TableOwnerDrawSupport(Table table) {
		int orientation= table.getStyle() & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
		fSharedLayout= new TextLayout(table.getDisplay());
		fSharedLayout.setOrientation(orientation);
	}

	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
			case SWT.MeasureItem:
				measureItem(event);
				break;
			case SWT.EraseItem:
				event.detail &= ~SWT.FOREGROUND;
				break;
			case SWT.PaintItem:
				performPaint(event);
				break;
			case SWT.Dispose:
				widgetDisposed();
				break;
		}
	}

	/**
	 * Handles the measure event.
	 *
	 * @param event the measure event
	 */
	private void measureItem(Event event) {
		boolean isSelected= (event.detail & SWT.SELECTED) != 0;
		fDeltaOfLastMeasure= updateTextLayout((TableItem) event.item, event.index, isSelected);
		event.width+= fDeltaOfLastMeasure;
	}

	private int updateTextLayout(TableItem item, int index, boolean isSelected) {
		fSharedLayout.setFont(item.getFont(index));

		// XXX: needed to clear the style info, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=226090
		fSharedLayout.setText(""); //$NON-NLS-1$

		fSharedLayout.setText(item.getText(index));

		int originalTextWidth= fSharedLayout.getBounds().width; // text width without any styles

		StyleRange[] ranges= getStyledRanges(item, index);
		if (ranges != null) {
			for (StyleRange range : ranges) {
				StyleRange curr= range;
				if (isSelected) {
					curr= (StyleRange) curr.clone();
					curr.foreground= null;
					curr.background= null;
				}
				fSharedLayout.setStyle(curr, curr.start, curr.start + curr.length - 1);
			}
		}

		return fSharedLayout.getBounds().width - originalTextWidth;
	}

	/**
	 * Performs the paint operation.
	 *
	 * @param event the event
	 */
	private void performPaint(Event event) {
		TableItem item= (TableItem) event.item;
		GC gc= event.gc;
		int index= event.index;

		boolean isSelected= (event.detail & SWT.SELECTED) != 0;

		// Remember colors to restore the GC later
		Color oldForeground= gc.getForeground();
		Color oldBackground= gc.getBackground();

		if (!isSelected) {
			Color foreground= item.getForeground(index);
			gc.setForeground(foreground);

			Color background= item.getBackground(index);
			gc.setBackground(background);
		}

		Image image=item.getImage(index);
		if (image != null) {
			Rectangle imageBounds=item.getImageBounds(index);
			Rectangle bounds=image.getBounds();
			int x=imageBounds.x + Math.max(0, (imageBounds.width - bounds.width) / 2);
			int y=imageBounds.y + Math.max(0, (imageBounds.height - bounds.height) / 2);
			gc.drawImage(image, x, y);
		}

		// fSharedLayout already configured in measureItem(Event)
		Rectangle textBounds=item.getTextBounds(index);
		if (textBounds != null) {
			Rectangle layoutBounds=fSharedLayout.getBounds();
			int x=textBounds.x;
			int y=textBounds.y + Math.max(0, (textBounds.height - layoutBounds.height) / 2);
			fSharedLayout.draw(gc, x, y);
		}

		if ((event.detail & SWT.FOCUSED) != 0) {
			Rectangle focusBounds=item.getBounds();
			gc.drawFocus(focusBounds.x, focusBounds.y, focusBounds.width + fDeltaOfLastMeasure, focusBounds.height);
		}

		if (!isSelected) {
			gc.setForeground(oldForeground);
			gc.setBackground(oldBackground);
		}
	}

	private void widgetDisposed() {
		fSharedLayout.dispose();
	}
}

