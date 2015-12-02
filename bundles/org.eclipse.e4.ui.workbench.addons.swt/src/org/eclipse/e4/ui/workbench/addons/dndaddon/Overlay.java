/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

class Overlay {
	private Shell baseShell;
	private Shell overlayShell;

	private Color blue;
	private int offsetX;
	private int offsetY;

	public abstract class Adornment {
		public abstract void updateRegion(Region region);

		public abstract void drawAdornment(GC gc);
	}

	public class OutlineRegion extends Adornment {
		Rectangle innerRect;
		Rectangle outerRect;
		int width;

		public OutlineRegion(Rectangle rect, int width) {
			innerRect = rect;
			this.width = width;
		}

		@Override
		public void updateRegion(Region region) {
			outerRect = new Rectangle(innerRect.x - width, innerRect.y - width,
					innerRect.width + (2 * width), innerRect.height
							+ (2 * width));
			region.add(outerRect);
			region.subtract(innerRect);
		}

		@Override
		public void drawAdornment(GC gc) {
			gc.fillRectangle(outerRect);
		}
	}

	public class ActiveRegion extends Adornment {
		Label label;
		Listener activeListener;

		public ActiveRegion(Rectangle rect, Listener listener) {
			label = new Label(overlayShell, SWT.NONE);
			label.setBounds(rect);

		}

		@Override
		public void updateRegion(Region region) {
			region.add(label.getBounds());
		}

		@Override
		public void drawAdornment(GC gc) {
		}
	}

	List<Adornment> adornments = new ArrayList<Adornment>();

	public void addAdornment(Adornment a) {
		adornments.add(a);
		updateRegion();
		overlayShell.redraw();
	}

	public void addOutline(Rectangle rect, int width) {
		rect.x += offsetX;
		rect.y += offsetY;
		addAdornment(new OutlineRegion(rect, width));
	}

	public void removeAdornment(Adornment a) {
		adornments.remove(a);

		if (adornments.size() == 0)
			overlayShell.setVisible(false);
		else
			overlayShell.redraw();
	}

	public void clear() {
		adornments.clear();
		overlayShell.setVisible(false);
	}

	public Overlay(Shell shell) {
		baseShell = shell;

		Rectangle cr = baseShell.getClientArea();
		Rectangle trim = baseShell.computeTrim(cr.x, cr.y, cr.width, cr.height);
		offsetX = -trim.x;
		offsetY = -trim.y;

		overlayShell = new Shell(baseShell, SWT.NO_TRIM | SWT.ON_TOP);
		overlayShell.setBounds(baseShell.getBounds());
		overlayShell.setBackground(baseShell.getDisplay().getSystemColor(
				SWT.COLOR_DARK_GREEN));
		overlayShell.setAlpha(128);

		blue = new Color(baseShell.getDisplay(), 0, 0, 128);
		overlayShell.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				e.gc.setForeground(blue);
				e.gc.setBackground(blue);
				for (Adornment adornment : adornments) {
					adornment.drawAdornment(e.gc);
				}
			}
		});
	}

	private void updateRegion() {
		Region region = new Region();
		for (Adornment adornment : adornments) {
			adornment.updateRegion(region);
		}
		overlayShell.setRegion(region);
		if (!overlayShell.getVisible())
			overlayShell.setVisible(true);
	}

	public void dispose() {
		adornments.clear();
		if (!overlayShell.isDisposed())
			overlayShell.dispose();
	}
}
