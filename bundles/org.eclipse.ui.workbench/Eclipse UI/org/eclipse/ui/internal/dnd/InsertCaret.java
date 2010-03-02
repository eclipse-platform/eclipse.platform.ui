/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.themes.ColorUtil;

/**
 * This class provides 'insertion' feedback to the User. It can be used to draw a
 * 'bracket' based on the trim area's rectangle.
 *
 * @since 3.2
 */
public class InsertCaret {
	// Constants
	private static final int width = 6; // the handle's 'thickness'
	private static final int pctInset = 10; // The percentage of the area left at each 'end'

	// Control info
	private Canvas caretControl;
	private Canvas end1;
	private Canvas end2;
	
	// Colors
	private Color baseColor;
	private Color hilightColor;
	private boolean isHighlight;
	
	/**
	 * Creates an affordance to indicate that the given trim area is a valid location for the
	 * trim being dragged.
	 * 
	 * @param windowComposite The window to create the affordance as a child of
	 * @param trimRect The rectangle to show the affordance for
	 * @param swtSide The 'side' that the rectangle is on
	 * @param threshold The amount to offfset the affordance by
	 */
	public InsertCaret(Composite parent, Rectangle trimRect, int swtSide, int threshold) {
		// Use the SWT 'title' colors since they should always have a proper contrast
		// and are 'related' (i.e. should look good together)
		baseColor = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		RGB background  = parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
		RGB blended = ColorUtil.blend(baseColor.getRGB(), background);
		hilightColor = new Color(parent.getDisplay(), blended);

		//Create the caret control
		createControl(parent, trimRect, swtSide, threshold);
	}

	/**
	 * Creates a control to show the 'area valid' affordance. The current implementation creates a
	 * simple rect half the length of the rect, centered and offset by the 'threshold' value.
	 * 
	 * @param parent The control to used as the parent of the affordance control
	 * @param trimRect The trim rectangle
	 * @param swtSide The SWT side that the trim is on
	 * @param threshold The offset value
	 */
	private void createControl(Composite parent, Rectangle trimRect, int swtSide, int threshold) {
		int hDelta = trimRect.width/pctInset;
		int vDelta = trimRect.height/pctInset;
		caretControl = new Canvas (parent.getShell(), SWT.BORDER);
		
		end1 = new Canvas (parent.getShell(), SWT.BORDER);
		end1.setSize(width, width);
		end2 = new Canvas (parent.getShell(), SWT.BORDER);
		end2.setSize(width, width);
		
		Rectangle bb;
		switch (swtSide) {
		case SWT.TOP:
			caretControl.setSize(trimRect.width-(2*hDelta), width);
			caretControl.setLocation(trimRect.x + hDelta, trimRect.y + trimRect.height + threshold);
			bb = caretControl.getBounds();
			end1.setLocation(bb.x, bb.y-width);
			end2.setLocation((bb.x+bb.width)-width, bb.y-width);
			break;
		case SWT.BOTTOM:
			caretControl.setSize(trimRect.width-(2*hDelta), width);
			caretControl.setLocation(trimRect.x + hDelta, trimRect.y - threshold); 
			bb = caretControl.getBounds();
			end1.setLocation(bb.x, bb.y+width);
			end2.setLocation((bb.x+bb.width)-width, bb.y+width);
			break;
		case SWT.LEFT:
			caretControl.setSize(width, trimRect.height -(2*vDelta));
			caretControl.setLocation(trimRect.x + trimRect.width + threshold,
									trimRect.y + vDelta); 
			bb = caretControl.getBounds();
			end1.setLocation(bb.x-bb.width, bb.y);
			end2.setLocation(bb.x-bb.width, (bb.y+bb.height)-width);
			break;
		case SWT.RIGHT:
			caretControl.setSize(width, trimRect.height -(2*vDelta));
			caretControl.setLocation(trimRect.x - threshold,
									trimRect.y + vDelta); 
			bb = caretControl.getBounds();
			end1.setLocation(bb.x+bb.width, bb.y);
			end2.setLocation(bb.x+bb.width, (bb.y+bb.height)-width);
			break;
		}
		
		// Initially create as not hilighted
		setHighlight(false);
		caretControl.moveAbove(null);
		end1.moveAbove(null);
		end2.moveAbove(null);
	}

	/**
	 * Sets the hilight 'mode' for the control.
	 * @param highlight true if the caret should be drawn as 'hilighted'
	 */
	public void setHighlight(boolean highlight) {
		isHighlight = highlight;

		// if we're displaying as a 'bar' then set the control's background to the
		// appropriate value
		if (isHighlight) {
			caretControl.setBackground(hilightColor);
			end1.setBackground(hilightColor);
			end2.setBackground(hilightColor);
		}
		else {
			caretControl.setBackground(baseColor);
			end1.setBackground(baseColor);
			end2.setBackground(baseColor);
		}
	}

	public void dispose() {
		// Dispose the control's resources (we don't have to dispose the
		// 'bacseColor' because it's a system color
		hilightColor.dispose();
		
		caretControl.dispose();
		end1.dispose();
		end2.dispose();
	}
}
