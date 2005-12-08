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
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.themes.ColorUtil;

/**
 * Utility class that wraps a given control with a black 'border'. Moving the
 * border control will cause the given control to move to stay within its bounds.
 *  
 * @since 3.2
 *
 */
public class DragBorder {
	// Controls
	private Composite clientControl = null;
	private Control dragControl = null;
	private Canvas border = null;

	// Colors
	private Color baseColor;
	private Color hilightColor;
	private boolean isHighlight;

	/**
	 * Construct a new DragBorder.
	 * 
	 * @param client The client window that the border must stay within
	 * @param toDrag The control to be placed 'inside' the border
	 */
	public DragBorder(Composite client, Control toDrag) {
		clientControl = client;
		dragControl = toDrag;
		Point dragSize = toDrag.getSize();
		
		// Create a control large enough to 'contain' the dragged control
		border = new Canvas(dragControl.getParent(), SWT.NONE);
		border.setSize(dragSize.x+2, dragSize.y+2);
		
		// Use the SWT 'title' colors since they should always have a proper contrast
		// and are 'related' (i.e. should look good together)
		baseColor = border.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
		RGB background  = border.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND).getRGB();
		RGB blended = ColorUtil.blend(baseColor.getRGB(), background);
		hilightColor = new Color(border.getDisplay(), blended);
		
		// Ensure the border is visible and the control is 'above' it...
		border.moveAbove(null);
		dragControl.moveAbove(null);
		
		border.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (isHighlight) {
					e.gc.setForeground(hilightColor);
				}
				else {
					e.gc.setForeground(baseColor);
				}
				
				// Draw a rectangle as our 'border'
				Rectangle bb = border.getBounds();
				e.gc.drawRectangle(0,0,bb.width-1, bb.height-1);
			}
		});
	}
	
    
    /**
     * Move the border (and its 'contained' control to a new position. The new
     * position will be adjusted to lie entirely within the client area of the
     * <code>clientControl</code>.
     * 
     * @param newPos The new position for the border
     * @param centered <code>true</code> iff the border should be centered on the point
     */
    public void setLocation(Point newPos, boolean centered) {
		// Move the border but ensure that it is still inside the Client area
    	if (centered) {
    		Point size = border.getSize();
    		border.setLocation(newPos.x - (size.x/2), newPos.y - (size.y/2));
    	}
    	else
    		border.setLocation(newPos.x, newPos.y);
    	
		Rectangle bb = border.getBounds();
		Rectangle cr = clientControl.getClientArea();
		Geometry.moveInside(bb,cr);
		
		// OK, now move the drag control and the border to their new locations
		dragControl.setLocation(bb.x+1, bb.y+1);
		border.setBounds(bb);
    }

	/**
	 * Sets the hilight 'mode' for the control.
	 * @param highlight true if the border should be drawn as 'hilighted'
	 */
	public void setHighlight(boolean highlight) {
		isHighlight = highlight;
		border.redraw();
	}

	/**
	 * Dispose the controls owned by the border.
	 */
	public void dispose() {
		hilightColor.dispose();
		border.dispose();
	}


	/**
	 * @return The bounds of the border's control.
	 */
	public Rectangle getBounds() {
		return border.getBounds();
	}
}
