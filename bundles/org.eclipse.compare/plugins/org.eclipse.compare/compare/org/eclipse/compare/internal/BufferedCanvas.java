/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;

/**
 * A Canvas which reduces flicker by drawing in an off screen buffer.
 */
public abstract class BufferedCanvas extends Canvas {

	//private static final boolean USE_DOUBLE_BUFFER= !"carbon".equals(SWT.getPlatform()); //$NON-NLS-1$
	private static final boolean USE_DOUBLE_BUFFER= true;
	
	/** The drawable for double buffering */
	Image fBuffer;

	public BufferedCanvas(Composite parent, int flags) {
		super(parent, flags + SWT.NO_BACKGROUND);

		addPaintListener(
			new PaintListener() {
				public void paintControl(PaintEvent event) {
					doubleBufferPaint(event.gc);
				}
			}
		);

		addDisposeListener(
			new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (fBuffer != null) {
						fBuffer.dispose();
						fBuffer= null;
					}
				}
			}
		);
	}

	public void repaint() {
		if (!isDisposed()) {
			GC gc= new GC(this);
			doubleBufferPaint(gc);
			gc.dispose();
		}
	}

	/**
	 * Double buffer drawing.
	 */
	void doubleBufferPaint(GC dest) {
		
		if (!USE_DOUBLE_BUFFER) {
			doPaint(dest);
			return;
		}

		Point size= getSize();

		if (size.x <= 0 || size.y <= 0)
			return;

		if (fBuffer != null) {
			Rectangle r= fBuffer.getBounds();
			if (r.width != size.x || r.height != size.y) {
				fBuffer.dispose();
				fBuffer= null;
			}
		}
		if (fBuffer == null)
			fBuffer= new Image(getDisplay(), size.x, size.y);

		GC gc= new GC(fBuffer);
		try {
			gc.setBackground(getBackground());
			gc.fillRectangle(0, 0, size.x, size.y);
			doPaint(gc);
		} finally {
			gc.dispose();
		}

		dest.drawImage(fBuffer, 0, 0);
	}

	abstract public void doPaint(GC gc);
}
