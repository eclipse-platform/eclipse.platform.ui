package org.eclipse.ui.internal.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWindowTrim;

/**
 * This class provides 'insertion' feedback to the User. It draws a
 * triangle pointing toward the given side and whose tip is at
 * <code>pos</code>.
 *
 * @since 3.2
 */
public class InsertCaret {
	private Composite clientControl;
	private Canvas caretControl;
	private static final int size = 10;
	private Color baseColor;
	private Color hilightColor;
	private boolean isHighlight;
	
	private int areaId;
	private IWindowTrim insertBefore;
	
	/**
	 * Creates the insert caret, remembering the parameters necessary to insert a piece
	 * of trim at the caret's 'location'.
	 * 
	 * @param parent The composite owning the caret
	 * @param pos The position at which to place the head
	 * @param areaId
	 * @param insertBefore
	 */
	public InsertCaret(Composite parent, Point pos, int areaId, IWindowTrim insertBefore) {
		this.clientControl = parent;
		caretControl = new Canvas (parent, SWT.NONE);
		caretControl.setSize(size, size);
		caretControl.setVisible(false);

		// Remember the trim item that this insert is associated with
		this.areaId = areaId;
		this.insertBefore = insertBefore;

		// set up the painting vars
		isHighlight = false;
		
		// Use the SWT 'title' colors since they should always have a proper contrast
		// and are 'related' (i.e. should look good together)
		baseColor = caretControl.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		hilightColor = caretControl.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
		
		caretControl.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (isHighlight) {
					e.gc.setBackground(hilightColor);
				}
				else {
					e.gc.setBackground(baseColor);
				}

				switch (getAreaId()) {
				case SWT.LEFT:
					{
						int[] points = { 0, size/2, size, 0, size, size };
						e.gc.fillPolygon(points);
					}
					break;
				case SWT.RIGHT:
					{
						int[] points = { size, size/2, 0, size, 0, 0 };
						e.gc.fillPolygon(points);
					}
					break;
				case SWT.TOP:
					{
						int[] points = { size/2, 0, 0, size-1, size, size-1 };
						e.gc.fillPolygon(points);
					}
					break;
				case SWT.BOTTOM:
					{
						int[] points = { size/2, size, 0, 0, size, 0 };
						e.gc.fillPolygon(points);
					}
					break;
				}
			}
		});
		
		showCaret(pos, areaId);
	}

	public void setHighlight(boolean highlight) {
		isHighlight = highlight;
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
		areaId = side;
		
		switch (side) {
		case SWT.LEFT:
			caretControl.setLocation(pos.x, pos.y - (size/2));
			break;
		case SWT.RIGHT:
			caretControl.setLocation(pos.x-size, pos.y - (size/2));
			break;
		case SWT.TOP:
			caretControl.setLocation(pos.x-(size/2), pos.y);
			break;
		case SWT.BOTTOM:
			caretControl.setLocation(pos.x-(size/2), pos.y - size);
			break;
		}
		
		// Force the control into the client rect
		Rectangle bb = caretControl.getBounds();
		Rectangle cr = clientControl.getClientArea();
		Geometry.moveInside(bb,cr);
		caretControl.setBounds(bb);
		
		caretControl.moveAbove(null);
		caretControl.setVisible(true);
		caretControl.redraw();
	}
	
	public void hideCaret() {
		caretControl.setVisible(false);
	}

	public Rectangle getBounds() {
		return caretControl.getBounds();
	}

	public void dispose() {
		//baseColor.dispose();
		caretControl.dispose();
	}
}
