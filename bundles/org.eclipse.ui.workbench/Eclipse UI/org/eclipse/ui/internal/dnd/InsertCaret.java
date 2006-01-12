package org.eclipse.ui.internal.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWindowTrim;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.eclipse.ui.themes.ColorUtil;

/**
 * This class provides 'insertion' feedback to the User. It draws a
 * triangle pointing toward the given side and whose tip is at
 * <code>pos</code>.
 *
 * @since 3.2
 */
public class InsertCaret {
	// Control info
	private Composite clientControl;
	private Canvas caretControl;
	private static final int arrowSize = 10;
	private static final int barSize = 5;

	// Colors
	private Color baseColor;
	private Color hilightColor;
	private boolean isHighlight;
	
	// 'Model' info
	private int areaId;
	private IWindowTrim insertBefore;
	private TrimLayout layout;
	private boolean asBar;
	
	/**
	 * Creates the insert caret, remembering the parameters necessary to insert a piece
	 * of trim at the caret's 'location'.
	 * 
	 * @param parent The composite owning the caret
	 * @param pos The position at which to place the head
	 * @param areaId
	 * @param insertBefore
	 */
	public InsertCaret(Composite parent, Point pos, int areaId, IWindowTrim insertBefore, TrimLayout layout, boolean asBar) {
		this.clientControl = parent;
		caretControl = new Canvas (parent, SWT.NONE);
		caretControl.setSize(arrowSize, arrowSize);
		caretControl.setVisible(false);

		// Remember the trim item that this insert is associated with
		this.areaId = areaId;
		this.insertBefore = insertBefore;
		this.layout = layout;
		this.asBar = asBar;
		
		// Use the SWT 'title' colors since they should always have a proper contrast
		// and are 'related' (i.e. should look good together)
		baseColor = caretControl.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		RGB background  = caretControl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
		RGB blended = ColorUtil.blend(baseColor.getRGB(), background);
		hilightColor = new Color(caretControl.getDisplay(), blended);

		// set up the painting vars
		setHighlight(false);
		
		// if we are -not- displaying as a 'bar' then we'll need to paint the control
		if (!asBar) {
			caretControl.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					paintArrowCaret(e);
				}
			});
		}

		showCaret(pos, areaId);
	}

	/**
	 * @param e
	 */
	protected void paintArrowCaret(PaintEvent e) {
		if (isHighlight) {
			e.gc.setBackground(hilightColor);
		}
		else {
			e.gc.setBackground(baseColor);
		}

		switch (getAreaId()) {
		case SWT.LEFT:
			{
				int[] points = { 0, arrowSize/2, arrowSize, 0, arrowSize, arrowSize };
				e.gc.fillPolygon(points);
			}
			break;
		case SWT.RIGHT:
			{
				int[] points = { arrowSize, arrowSize/2, 0, arrowSize, 0, 0 };
				e.gc.fillPolygon(points);
			}
			break;
		case SWT.TOP:
			{
				int[] points = { arrowSize/2, 0, 0, arrowSize-1, arrowSize, arrowSize-1 };
				e.gc.fillPolygon(points);
			}
			break;
		case SWT.BOTTOM:
			{
				int[] points = { arrowSize/2, arrowSize, 0, 0, arrowSize, 0 };
				e.gc.fillPolygon(points);
			}
			break;
		}
	}

	/**
	 * Sets the hilight 'mode' for the control.
	 * @param highlight true if the caret should be drawn as 'hilighted'
	 */
	public void setHighlight(boolean highlight) {
		isHighlight = highlight;

		// if we're displaying as a 'bar' then set the control's background to the
		// appropriate value
		if (asBar) {
			if (isHighlight)
				caretControl.setBackground(hilightColor);
			else
				caretControl.setBackground(baseColor);
		}
		
		caretControl.redraw();
	}
	
	/**
	 * @return The area ID that this caret is 'on'
	 */
	public int getAreaId() {
		return areaId;
	}

	/**
	 * @return The 'beforeMe' trim for this insertion caret
	 */
	public IWindowTrim getInsertTrim() {
		return insertBefore;
	}
	
	public void showCaret(Point pos, int side) {
		// Set the appropriate size / location based on the format of the caret
		if (asBar)
			formatBarCaret(pos, side);
		else 
			formatArrowCaret(pos, side);

		// Force the control into the client rect
		Rectangle bb = caretControl.getBounds();
		Rectangle cr = clientControl.getClientArea();
		Geometry.moveInside(bb,cr);
		caretControl.setBounds(bb);
		
		caretControl.moveAbove(null);
		caretControl.setVisible(true);
		caretControl.redraw();
	}
	
	/**
	 * @param pos
	 * @param side
	 */
	private void formatBarCaret(Point pos, int side) {
		Rectangle trimRect = layout.getTrimRect(caretControl.getParent(), side);
		
		// KLUDGE!! the current trimRect calcs return a zero width/height if the area is 'empty'
		// for just hack it
		if (trimRect.width == 0) {
			trimRect.width = 5;
		
			// If it's on the right then we have to offset the rect's position as well
			if (areaId == SWT.RIGHT)
				trimRect.x -= trimRect.width;
		}
		
		if (trimRect.height == 0)
			trimRect.height = 5;
		
		trimRect = Geometry.toControl(caretControl.getParent(), trimRect);
		System.out.println("showBar: id = " + areaId + " trimRect = " + trimRect);  //$NON-NLS-1$//$NON-NLS-2$
		
		switch (side) {
		case SWT.LEFT:
		case SWT.RIGHT:
			caretControl.setSize(trimRect.width, barSize);
			caretControl.setLocation(trimRect.x, pos.y - (barSize/2));
			break;
		case SWT.TOP:
		case SWT.BOTTOM:
			caretControl.setSize(barSize, trimRect.height);
			caretControl.setLocation(pos.x - (barSize/2), trimRect.y);
			break;
		}
	}

	/**
	 * Formats the control to display the caret as an 'arrow'.
	 * 
	 * @param pos The position of the 'head' of the arrow
	 * @param side The SWT 'side' that the caret is pointing towards
	 */
	private void formatArrowCaret(Point pos, int side) {
		caretControl.setSize(arrowSize, arrowSize);
		
		switch (side) {
		case SWT.LEFT:
			caretControl.setLocation(pos.x, pos.y - (arrowSize/2));
			break;
		case SWT.RIGHT:
			caretControl.setLocation(pos.x-arrowSize, pos.y - (arrowSize/2));
			break;
		case SWT.TOP:
			caretControl.setLocation(pos.x-(arrowSize/2), pos.y);
			break;
		case SWT.BOTTOM:
			caretControl.setLocation(pos.x-(arrowSize/2), pos.y - arrowSize);
			break;
		}
	}

	public void hideCaret() {
		caretControl.setVisible(false);
	}

	public Rectangle getBounds() {
		return caretControl.getBounds();
	}

	public void dispose() {
		// Dispose the control's resources (we don't have to dispose the
		// 'bacseColor' because it's a system color
		hilightColor.dispose();
		caretControl.dispose();
	}
}
