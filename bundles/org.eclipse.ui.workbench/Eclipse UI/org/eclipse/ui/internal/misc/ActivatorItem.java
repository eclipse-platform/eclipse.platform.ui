package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;


/**
 * A Label which supports aligned text and/or an image and different border styles.
 * If there is not enough space a SmartLabel uses the following strategy to fit the information into the
 * available space:
 * <pre>
 * - ignores the indent in left align mode
 * - ignores the image and the gap
 * - shortens the text by replacing the center portion of the label with an ellipsis
 * - shortens the text by removing the center portion of the label
 * </pre>
 */
public class ActivatorItem extends Canvas implements PaintListener,
	MouseListener, DisposeListener
{

	protected String fText;
	protected Image fImage;
	static protected Image fCloseImage;
	static protected int fInstanceCount;
	protected ActivatorBar fParent;
	protected boolean fPressed = false;
	final static int GAP = 5;   
	/**
	 * Create a SmartLabel with the given borderStyle as a child of parent.
	 */
	public ActivatorItem(ActivatorBar parent) {
		super(parent, 0);
		addInstance();
		fParent = parent;
		addPaintListener(this);
		addMouseListener(this);
		addDisposeListener(this);
		fParent.addItem(this);
	}
/**
 * Add an instance.  Create the close image.
 */
static private void addInstance() {
	if (fInstanceCount == 0) {
		ImageDescriptor desc = WorkbenchImages.getImageDescriptor(
			IWorkbenchGraphicConstants.IMG_LCL_CLOSE_VIEW);
		if (desc != null)
			fCloseImage = desc.createImage();
	}
	++ fInstanceCount;
}
	//---- layout
	
	/**
	 * Compute the size.
 	 * @private
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point e= getMinimumSize();
		if (wHint != SWT.DEFAULT)
			e.x= wHint;
		if (hHint != SWT.DEFAULT)
			e.y= hHint;
		return e;
	}
	/**
	 * Dispose the control.
	 */
	public void dispose() {
		super.dispose();
		fParent.removeItem(this);
	}
	//---- private stuff
	
	/**
	 * Draw a rectangle in the given colors.
	 * @private
	 */
	private static void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
		
		gc.setForeground(topleft);
		gc.drawLine(x, y, x+w-1, y);
		gc.drawLine(x, y, x, y+h-1);
		
		gc.setForeground(bottomright);
		gc.drawLine(x+w, y, x+w, y+h);
		gc.drawLine(x, y+h, x+w, y+h);
	}
	/**
	 * Return the SmartLabel's image or <code>null</code>.
	 */
	public Image getImage() {
		return fImage;
	}
	/**
	 * Compute the minimum size.
 	 * @private
	 */
	public Point getMinimumSize() {
		Point size= new Point(0, 22);

		// get image extent.
		if (fImage != null) {
			Rectangle r= fImage.getBounds();
			size.x = r.width;
		}

		// get text extent.
		if (fText != null && fText.length() > 0) {
			GC gc= new GC(this);
			Point e= gc.textExtent(fText);
			size.x += e.x + 2 * GAP;
			gc.dispose();
		}

		// get close button extent.
		if (fCloseImage != null) {
			Rectangle r= fCloseImage.getBounds();
			size.x += r.width + 2;
		}

		return size;
	}
	//---- setters & getters
	
	/**
	 * Return the SmartLabel's label text.
	 */
	public String getText() {
		return fText;
	}
/**
 * mouseDoubleClick method comment.
 */
public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {}
/**
 * mouseDown method comment.
 */
public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
	if (e.button == 1) {
		int textExtent = 100000;
		if (fPressed && (fCloseImage != null))
			textExtent = getClientArea().width - fCloseImage.getBounds().width - 4;
		if (e.x < textExtent)
			fParent.itemSelected(this);
		else
			fParent.itemClosePressed(this);
	}
}
/**
 * mouseUp method comment.
 */
public void mouseUp(org.eclipse.swt.events.MouseEvent e) {}
	/**
	 * Paint the Label's border.
	 */
	protected void paintBorder(GC gc, Rectangle r) {

		Display disp= getDisplay();
		
		Color c1= null;
		Color c2= null;

		if (fPressed) {
			c1= disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			c2= disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		} else {
			c1= disp.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
			c2= disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		}
		
		if (c1 != null && c2 != null) {
			gc.setLineWidth(1);
			drawBevelRect(gc, r.x, r.y, r.width-1, r.height-1, c1, c2);
		}
	}
	/**
	 * Paint the Label's border.
	 */
	protected void paintBorder(GC gc, Rectangle r, boolean bPressed) {

		Display disp= getDisplay();
		
		Color c1= null;
		Color c2= null;

		if (bPressed) {
			c1= disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			c2= disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
		} else {
			c1= disp.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
			c2= disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		}
		
		if (c1 != null && c2 != null) {
			gc.setLineWidth(1);
			drawBevelRect(gc, r.x, r.y, r.width-1, r.height-1, c1, c2);
		}
	}
	//---- painting
	
	/**
 	 * Implements PaintListener.
 	 * @private
 	 */
	public void paintControl(PaintEvent event) {

		GC gc= event.gc;
		Rectangle r= getClientArea();

		// Draw background.
		if (fPressed) {
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
			gc.fillRectangle(r);
		} else
			ActivatorBar.paintGradient(getDisplay(), gc, r);

		// Draw border.
		paintBorder(gc, r);
		
		// Get image extent.
		int imageWidth = 0;
		Image image= fImage;
		Rectangle imageBounds = null;
		if (image != null) {
			imageBounds = image.getBounds();
			imageWidth = image.getBounds().width;
		}

		// Get text extent.
		Point textExtent = null;
		if (fText != null && fText.length() > 0)
			textExtent= gc.textExtent(fText);

		// Get close button extent.
		Rectangle closeBox = new Rectangle(0, 0, 0, 0);
		if (fCloseImage != null) {
			int width = fCloseImage.getBounds().width + 2;
			closeBox = new Rectangle(r.x + r.width - width, r.y,
				width, r.height);
		}
	
		// Shorten text as required.
		boolean shortenText= false;
		if (fText != null) {
			int availableWidth= r.width - 2 * GAP - imageWidth - closeBox.width;
			if (textExtent.x > availableWidth) {
				image = null;
				imageWidth = 0;
				availableWidth= r.width - 2 * GAP - imageWidth - closeBox.width;
				if (textExtent.x > availableWidth) {
					shortenText= true;
				}
			}
		}
		String t= fText;
		if (shortenText) {
			int availableWidth= r.width - 2 * GAP - imageWidth - closeBox.width;
			t= shortenText(gc, fText, availableWidth);
			textExtent.x= gc.textExtent(t).x;
			setToolTipText(fText);
		} else {
			setToolTipText(null);
		}
		
		// draw the image		
		int x= r.x;
		if (image != null) {
			gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, 
				x, (r.height-imageBounds.height)/2, imageBounds.width, imageBounds.height);
			x+= imageBounds.width;
		}

		// draw the text
		if (t != null) {
			x+= GAP;
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
			gc.drawText(t, x, r.y + (r.height - textExtent.y) / 2, true);
		}

		// draw the close box.
		if (fPressed && (fCloseImage != null)) {
			closeBox.x += 1;
			closeBox.width -= 2;
			closeBox.y += 1;
			closeBox.height -= 2;
			gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gc.fillRectangle(closeBox);
			Rectangle imgBounds = fCloseImage.getBounds();
			gc.drawImage(fCloseImage, 0, 0, imgBounds.width, imgBounds.height, 
				closeBox.x + (closeBox.width-imgBounds.width) / 2, 
				closeBox.y + (closeBox.height-imgBounds.height) / 2, 
				imgBounds.width, imgBounds.height);
			paintBorder(gc, closeBox, false); 
		}
	}
/**
 * Remove an instance.  Dispose the close image.
 */
static private void removeInstance() {
	-- fInstanceCount;
	if (fInstanceCount <= 0) {
	    if (fCloseImage != null) {
			fCloseImage.dispose();
			fCloseImage = null;
	    }
	}
}
	/**
	 * Sets the widget's font to the given value and forces
	 * a redraw of the widget.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		redraw();
	}
	/**
	 * Set the SmartLabel's Image.
	 * The value <code>null</code> clears it.
	 */
	public void setImage(Image image) {
		if (image != fImage) {
			fImage= image;
			redraw();
		}
	}
	/**
	 * Sets the control state.
	 */
	public void setPressed(boolean pressed) {
		fPressed = pressed;
		redraw();
	}
	/**
	 * Set the SmartLabel's label text.
	 * The value <code>null</code> clears it.
	 */
	public void setText(String text) {
		if (text == null)
			text= "";//$NON-NLS-1$
		if (!text.equals(fText)) {
			fText= text;
		}
		fParent.layout();
		redraw();
	}
	/**
	 * Shorten the given text <code>t</code> so that its length doesn't exceed
	 * the given width. The default implementation replaces characters in the
	 * center of the original string with an ellipsis ("...").
	 * Override if you need a different strategy.
	 */
	protected String shortenText(GC gc, String t, int width) {
		String ellipsis= "...";//$NON-NLS-1$
		int w= gc.textExtent(ellipsis).x;
		int l= t.length();
		int pivot= l/2;
		int s= pivot;
		int e= pivot+1;
		while (s >= 0 && e < l) {
			String s1= t.substring(0, s);
			String s2= t.substring(e, l);
			int l1= gc.textExtent(s1).x;
			int l2= gc.textExtent(s2).x;
			if (l1+w+l2 < width) {
				t= s1 + ellipsis + s2;
				break;
			}
			s--;
			e++;
		}
		return t;
	}
/**
 * widgetDisposed method comment.
 */
public void widgetDisposed(org.eclipse.swt.events.DisposeEvent e) {
	if (fImage != null)
		fImage.dispose();
	removeInstance();
}
}
