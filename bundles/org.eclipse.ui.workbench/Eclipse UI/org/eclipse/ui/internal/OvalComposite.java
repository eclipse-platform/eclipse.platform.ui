package org.eclipse.ui.internal;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class OvalComposite extends Composite implements PaintListener {
    
    static final int[] TOP_LEFT_CORNER = new int[] { 0, 2, 1, 1, 2, 0 };
	    
	private int orientation;
    private Color interiorColor;
		
	public OvalComposite(Composite parent, int orientation) {
	    super(parent, SWT.NONE);
	    
	    addPaintListener(this);
	    this.orientation = orientation;
	}
	
	public void setOrientation(int orientation) {
		this.orientation = orientation;
        redraw();
	}
	
	public void paintControl(PaintEvent e) {
	    GC gc = e.gc;
	    Color color = e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
	    gc.setForeground(color);
        if (interiorColor != null) {
            gc.setBackground(interiorColor);
        }
	
	    Shape shape = new Shape(TOP_LEFT_CORNER.length + 2);
	    
	    IntAffineMatrix rotation = IntAffineMatrix
	        .getRotation(orientation);
	    
	    rotation = rotation.multiply(IntAffineMatrix.ROT_180);
	    
	    Point size = getSize();

	    if (!Geometry.isHorizontal(orientation)) {
	    	Geometry.flipXY(size);
	    }
	    
	    shape.add(0, size.y);
	    shape.add(new Shape(TOP_LEFT_CORNER));
	    shape.add(IntAffineMatrix.translation(size.x - 3, 0).multiply(IntAffineMatrix.FLIP_YAXIS), 
	    		shape.reverse());

        Point rawSize = getSize();
	    Point adjust = new Point(0,0);
        switch(orientation) {
	        case SWT.TOP: adjust = rawSize; break;
	        case SWT.LEFT: adjust = new Point(rawSize.x - 1, 0); break;
	        case SWT.RIGHT: adjust = new Point(0, rawSize.y - 3); break;
        }
        
	    Shape targetShape = IntAffineMatrix.translation(adjust.x, adjust.y)
        	.multiply(rotation)
        	.transform(shape);
	    
	    int[] shapeArray = targetShape.getData();
        if (interiorColor != null) {
            gc.fillPolygon(shapeArray);
        }
	    gc.drawPolyline(shapeArray);
	}

	public Rectangle getClientArea() {
		Rectangle result = Geometry.copy(super.getClientArea());
		
		if (Geometry.isHorizontal(orientation)) {
			Geometry.expand(result, -6, -6, orientation == SWT.BOTTOM ? -1 : 0, orientation == SWT.TOP ? -1 : 0);
		} else {
			Geometry.expand(result, orientation == SWT.RIGHT ? -1 : 0, orientation == SWT.LEFT ? -1 : 0, -6, -6);
		}
		
		return result;
	}
	
	public Rectangle computeTrim(int x, int y, int width, int height) {
		Rectangle result = Geometry.copy(super.computeTrim(x, y, width, height));
		
        if (Geometry.isHorizontal(orientation)) {
            Geometry.expand(result, 6, 6, orientation == SWT.BOTTOM ? 1 : 0, orientation == SWT.TOP ? 1 : 0);
        } else {
            Geometry.expand(result, orientation == SWT.RIGHT ? 1 : 0, orientation == SWT.LEFT ? 1 : 0, 6, 6);
        }
        		
		return result;
	}

	/**
	 * @return Returns the interiorColor.
	 */
	public Color getInteriorColor() {
		return interiorColor;
	}

	/**
	 * @param interiorColor The interiorColor to set.
	 */
	public void setInteriorColor(Color interiorColor) {
		this.interiorColor = interiorColor;
	}
}
