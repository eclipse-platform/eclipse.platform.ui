/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
public class TableOwnerDrawSupport implements Listener, DisposeListener {
	
	private static final String STYLED_RANGES_KEY= "styled_ranges"; //$NON-NLS-1$
	
	private TextLayout fLayout;

	public static void install(Table table) {
		TableOwnerDrawSupport listener= new TableOwnerDrawSupport(table);
		table.addDisposeListener(listener);
		table.addListener(SWT.MeasureItem, listener);
		table.addListener(SWT.EraseItem, listener);
		table.addListener(SWT.PaintItem, listener);
	}
	
	/**
	 * Stores the styled ranges in the given table item.
	 * 
	 * @param item table item
	 * @param ranges the styled ranges or <code>null</code> to remove them
	 * @since 3.4
	 */
	public static void storeStyleRanges(TableItem item, StyleRange[] ranges) {
		item.setData(STYLED_RANGES_KEY, ranges);
	}
	
	/**
	 * Returns the styled ranges which are stored in the given table item.
	 * 
	 * @param item table item
	 * @return the styled ranges
	 * @since 3.4
	 */
	private static StyleRange[] getStyledRanges(TableItem item) {
		return (StyleRange[])item.getData(STYLED_RANGES_KEY);
	}

	private TableOwnerDrawSupport(Table table) {
		int orientation= table.getStyle() & (SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT);
		fLayout= new TextLayout(table.getDisplay());
		fLayout.setOrientation(orientation);
	}

	/*
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		switch (event.type) {
			case SWT.MeasureItem:
				break;
			case SWT.EraseItem:
				event.detail &= ~SWT.FOREGROUND;
				break;
			case SWT.PaintItem:
				performPaint(event);
				break;
			}
	}

	/**
	 * Performs the paint operation.
	 * 
	 * @param event the event
	 */
	private void performPaint(Event event) {
		TableItem item= (TableItem) event.item;
		GC gc= event.gc;
		
		Image image = item.getImage(0);
		if (image != null) {
			Rectangle imageBounds = item.getImageBounds(0);
			Rectangle bounds = image.getBounds();
			int x = imageBounds.x + Math.max(0, (imageBounds.width - bounds.width) / 2);
			int y = imageBounds.y + Math.max(0, (imageBounds.height - bounds.height) / 2);
			gc.drawImage(image, x, y);
		}
		
		fLayout.setFont(item.getFont(0));
		fLayout.setText(""); //$NON-NLS-1$
		fLayout.setText(item.getText(0));
		StyleRange[] ranges= getStyledRanges(item);
		if (ranges != null) {
			boolean isSelected= (event.detail & SWT.SELECTED) != 0;
			for (int i= 0; i < ranges.length; i++) {
				StyleRange curr= ranges[i];
				if (isSelected) {
					curr= (StyleRange) curr.clone();
					curr.foreground= null;
					curr.background= null;
				}
				fLayout.setStyle(curr, curr.start, curr.start + curr.length - 1);
			}
		}
		
		Rectangle textBounds = item.getTextBounds(0);
		if (textBounds != null) {
			Rectangle layoutBounds = fLayout.getBounds();
			int x = textBounds.x;
			int y = textBounds.y + Math.max(0, (textBounds.height - layoutBounds.height) / 2);
			fLayout.draw(gc, x, y);
		}
		
		
		if ((event.detail & SWT.FOCUSED) != 0) {
			Rectangle focusBounds = item.getBounds();
			gc.drawFocus(focusBounds.x, focusBounds.y, focusBounds.width, focusBounds.height);
		}
	}
	
	/*
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent e) {
		fLayout.dispose();
	}
}
	
