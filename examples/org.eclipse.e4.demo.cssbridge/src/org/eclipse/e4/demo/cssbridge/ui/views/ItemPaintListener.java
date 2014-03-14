/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.demo.cssbridge.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Listener;

public abstract class ItemPaintListener<T extends Item> implements Listener {
	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(Event event) {
		if (isSelected(event.detail)) {
			T item = (T) event.item;
			String text = getText(item, event.index);
			Rectangle rec = getBounds(item, event.index);
			Image image = getImage(item, event.index);
			Font font = getFont(item);

			event.gc.setForeground(Theme
					.getColor(Theme.Shell.SELECTION_FOREGROUND));
			event.gc.setBackground(Theme
					.getColor(Theme.Shell.SELECTION_BACKGROUND));
			event.gc.fillRectangle(0, rec.y, getParentBounds(item).width,
					rec.height);

			if (image != null) {
				event.gc.drawImage(image, event.x, event.y);
			}
			if (font != null) {
				event.gc.setFont(font);
			}
			event.gc.drawText(text,
					event.x + calculateTextLeftPadding(item, event.index),
					event.y + calculateTextTopPadding(item, event.index), true);
		}
	}

	protected int calculateTextLeftPadding(T item, int index) {
		return 0;
	}

	protected int calculateTextTopPadding(T item, int index) {
		return 2;
	}

	protected Font getFont(T item) {
		return null;
	}

	private boolean isSelected(int details) {
		return (details & SWT.SELECTED) == SWT.SELECTED;
	}

	protected abstract String getText(T item, int index);

	protected abstract Rectangle getBounds(T item, int index);

	protected abstract Rectangle getParentBounds(T item);

	protected abstract Image getImage(T item, int index);
}
