/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SplitFeedbackOverlay {
	final Display display = Display.getCurrent();

	private Shell feedbackShell;
	private int curSide = 0;
	private float ratio;

	private List<Rectangle> rects = new ArrayList<Rectangle>();
	private Rectangle outerRect;

	Boolean isModified = null;
	private IStylingEngine stylingEngine;

	public SplitFeedbackOverlay(Shell dragShell, Rectangle rect, int side, float pct,
			boolean enclosed, boolean modified) {
		outerRect = rect;
		curSide = side;
		ratio = pct;

		feedbackShell = new Shell(dragShell, SWT.NO_TRIM | SWT.ON_TOP);
		feedbackShell.setBounds(dragShell.getBounds());

		MWindow winModel = (MWindow) dragShell.getData(AbstractPartRenderer.OWNING_ME);
		stylingEngine = winModel.getContext().get(IStylingEngine.class);

		// Show the appropriate feedback rectangles
		setFeedback(enclosed, modified);

		defineRegion();
	}

	public void dispose() {
		if (feedbackShell != null && !feedbackShell.isDisposed()) {
			Region region = feedbackShell.getRegion();
			if (region != null && !region.isDisposed())
				region.dispose();
			feedbackShell.dispose();
		}
		feedbackShell = null;
	}

	private void showRects(boolean enclosed) {
		if (curSide == 0)
			return;

		Rectangle ca = new Rectangle(outerRect.x, outerRect.y, outerRect.width, outerRect.height);
		rects.clear();

		if (enclosed) {
			addRect(ca);
			ca.x += 4;
			ca.y += 4;
			ca.width -= 8;
			ca.height -= 8;
		}

		int pctWidth = (int) (ca.width * ratio);
		int pctHeight = (int) (ca.height * ratio);

		Rectangle r1 = null, r2 = null;
		if (curSide == SWT.LEFT) {
			r1 = new Rectangle(ca.x, ca.y, pctWidth, ca.height);
			r2 = new Rectangle(ca.x + r1.width + 2, ca.y, ca.width - (pctWidth + 2), ca.height);
		} else if (curSide == SWT.RIGHT) {
			r1 = new Rectangle(ca.x, ca.y, ca.width - pctWidth, ca.height);
			r2 = new Rectangle(ca.x + r1.width + 2, ca.y, pctWidth - 2, ca.height);
		} else if (curSide == SWT.TOP) {
			r1 = new Rectangle(ca.x, ca.y, ca.width, pctHeight);
			r2 = new Rectangle(ca.x, ca.y + pctHeight + 2, ca.width, ca.height - (pctHeight + 2));
		} else if (curSide == SWT.BOTTOM) {
			r1 = new Rectangle(ca.x, ca.y, ca.width, ca.height - pctHeight);
			r2 = new Rectangle(ca.x, ca.y + r1.height + 2, ca.width, pctHeight - 2);
		}

		addRect(r1);
		addRect(r2);
	}

	private void defineRegion() {
		Region rgn = new Region();
		for (Rectangle r : rects) {
			rgn.add(r);
			rgn.subtract(r.x + 2, r.y + 2, r.width - 4, r.height - 4);
		}

		// Workaround: Some window managers draw a drop shadow even if the shell
		// is set to NO_TRIM. By making the shell contain a component in the
		// bottom-right of its parent shell, SWT won't resize it and any extra
		// shadows will end up being drawn on top of the shadows for the parent
		// shell rather than in the middle of the workbench window.
		Composite parent = feedbackShell.getParent();
		if (parent instanceof Shell) {
			Shell parentShell = (Shell) parent;

			Rectangle bounds = parentShell.getBounds();
			rgn.add(bounds.width - 1, bounds.height - 1, 1, 1);
		}

		if (feedbackShell.getRegion() != null && !feedbackShell.getRegion().isDisposed())
			feedbackShell.getRegion().dispose();
		feedbackShell.setRegion(rgn);

		feedbackShell.redraw();
		display.update();
	}

	private void addRect(Rectangle rect) {
		// Map the rect to the feedback shell
		rect = display.map(null, feedbackShell, rect);
		rects.add(rect);
	}

	public void setFeedback(boolean enclosed, boolean modified) {
		if (isModified == null)
			isModified = !modified;

		// Update the feedback color if the drag is 'modified'
		if (modified != isModified) {
			if (!modified) {
				stylingEngine.setClassname(feedbackShell, "DragFeedback");
			} else {
				stylingEngine.setClassname(feedbackShell, "ModifiedDragFeedback");
			}
			stylingEngine.style(feedbackShell);
			isModified = modified;
		}

		showRects(enclosed);
		defineRegion();
		feedbackShell.update();
	}

	/**
	 * Control this instance's visibility.
	 *
	 * @param visible
	 *            make visible if {@code true} or invisible if {@code false}
	 * @since 0.11
	 */
	public void setVisible(boolean visible) {
		if (feedbackShell != null) {
			feedbackShell.setVisible(visible);
		}
	}
}
