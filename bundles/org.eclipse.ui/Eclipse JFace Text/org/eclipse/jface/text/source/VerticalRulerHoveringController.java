package org.eclipse.jface.text.source;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.Assert;



/**
 * A vertical ruler hovering controller. The controller registers with the 
 * vertical ruler's control as a <code>MouseTrackListener<code>. When receiving a
 * mouse hover event, it opens a popup window using its <code>IAnnotationHover</code> 
 * to initialize the window's display information. The controller closes the window
 * if the mouse pointer leaves the popup window.
 */
class VerticalRulerHoveringController extends MouseTrackAdapter {	
	
	/**
	 * The popup window closer.
	 */
	class WindowCloser extends MouseTrackAdapter 
		implements MouseListener, MouseMoveListener, FocusListener {
		
		
		Rectangle fCoveredArea;
		
		/**
		 * Creates a new window closer for the given area.
		 */
		public WindowCloser(Rectangle coveredArea) {
			fCoveredArea= coveredArea;
		}
		
		/**
		 * Starts watching whether to close the popup window.
		 */
		public void start() {			
			Control c= fVerticalRuler.getControl();
			c.addMouseMoveListener(this);
			c.addMouseTrackListener(this);
			c.addMouseListener(this);
			
			fSourceViewer.getTextWidget().addFocusListener(this);
		}
		
		/**
		 * Closes the popup window and stops watching.
		 */
		private void stop() {
			fWindowShell.setVisible(false);
			
			Control c= fVerticalRuler.getControl();
			c.removeMouseMoveListener(this);
			c.removeMouseTrackListener(this);
			c.removeMouseListener(this);
						
			fSourceViewer.getTextWidget().removeFocusListener(this);
		}
		
		/*
		 * @see MouseTrackListener#mouseExit
		 */
		public void mouseExit(MouseEvent event) {
			stop();
		}
		
		/*
		 * @see MouseListener#mouseUp(MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
		}
		
		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {
			stop();
		}
		
		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent event) {
			stop();
		}
		
		/*
		 * @see MouseMoveListener#mouseMove(MouseEvent)
		 */
		public void mouseMove(MouseEvent event) {
			if (!fCoveredArea.contains(event.x, event.y))
				stop();
		}
		
		/*
		 * @see FocusListener#focusLost(FocusEvent)
		 */
		public void focusLost(FocusEvent event) {
			if (fSourceViewer.getTextWidget() == event.widget) {
				fWindowShell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						stop();
					}
				});
			}
		}
		
		/*
		 * @see FocusListener#focusGained(FocusEvent)
		 */
		public void focusGained(FocusEvent event) {
		}
	};
	
		
	/** The source viewer the controller is connected to */
	private ISourceViewer fSourceViewer;
	/** The vertical ruler the controller is registered with */
	private IVerticalRuler fVerticalRuler;
	/** The annotation hover the controller used to retrieve the display information */
	private IAnnotationHover fAnnotationHover;
	
	/** The popup window shell */
	private Shell fWindowShell;
	/** The label shown in the popup window */
	private Label fWindowLabel;
	
	
	/**
	 * Creates a vertical hovering controller. The controller's opup window is initially
	 * invisible. The controller is registered with the vertical ruler's control.
	 *
	 * @param sourceViewer the source viewer to whose vertical ruler this hover popup window connects to
	 * @param ruler the vertical ruler this hover popup window connects to
	 * @param annotationHover the annotation hover providing the display information for this popup window
	 */
	public VerticalRulerHoveringController(ISourceViewer sourceViewer, IVerticalRuler ruler, IAnnotationHover annotationHover) {
		
		Assert.isNotNull(sourceViewer);
		Assert.isNotNull(ruler);
		Assert.isNotNull(annotationHover);
		
		fSourceViewer= sourceViewer;
		fVerticalRuler= ruler;
		fAnnotationHover= annotationHover;
		
		Control control= ruler.getControl();
		
		fWindowShell= new Shell(control.getShell(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		fWindowLabel= new Label(fWindowShell, SWT.NO_FOCUS);
		
		Display display= control.getShell().getDisplay();
		fWindowShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		fWindowLabel.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
	}
	/**
	 * Determines graphical area covered by the given line.
	 *
	 * @param line the number of the line in the viewer whose 
	 *			graphical extend in the vertical ruler must be computed
	 * @return the graphical extend of the given line
	 */
	private Rectangle computeCoveredArea(int visibleLine) {
		StyledText text= fSourceViewer.getTextWidget();
		int lineHeight= text.getLineHeight();
		
		int y= visibleLine * lineHeight - text.getTopPixel();
		Point size= fVerticalRuler.getControl().getSize();
				
		return new Rectangle(0, y, size.x, lineHeight);
	}
	/**
	 * Returns the location of the popup window based on given
	 * coordinates of the vertical ruler's control.
	 *
	 * @param visibleLine the line visible in the viewer in which the given point is located
	 * @param x the x coordinate in the vertical ruler's control
	 * @param y the y coordinate in the vertical ruler's control
	 * @return the position of the popup window
	 */
	private Point computeWindowLocation(int visibleLine, int x, int y) {
		StyledText text= fSourceViewer.getTextWidget();
		Control control= fVerticalRuler.getControl();
		
		int height= visibleLine * text.getLineHeight() - text.getTopPixel();
		x += control.getSize().x; // the size should mimic the cursor size,
		                          // although there no relationship at all <g>
		
		return control.toDisplay(new Point(x, height));
	}
	/**
	 * Disposes this hovering controller.
	 */
	public void dispose() {
		if (fWindowShell != null && !fWindowShell.isDisposed()) {
			fWindowShell.dispose();
			fWindowShell= null;
		}
	}
	/**
	 * Returns for a given absolute line number the corresponding line
	 * number relative to the viewer's visible region.
	 *
	 * @param line the absolute line number
	 * @return the line number relative to the viewer's visible region
	 */
	private int getRelativeLineNumber(int line) {
		IRegion region= fSourceViewer.getVisibleRegion();
		try {
			int firstLine= fSourceViewer.getDocument().getLineOfOffset(region.getOffset());
			return line - firstLine;
		} catch (BadLocationException x) {
			// cannot happen
		}
		return line;
	}
	/**
	 * Enables this hovering controller on its vertical ruler.
	 */	
	public void install() {
		fVerticalRuler.getControl().addMouseTrackListener(this);
	}
	/*
	 * @see MouseTrackAdapter#mouseHover
	 */
	public void mouseHover(MouseEvent event) {
		int line= fVerticalRuler.toDocumentLineNumber(event.y);
		String info= fAnnotationHover.getHoverInfo(fSourceViewer, line);
		
		if (info != null && info.trim().length() > 0 && fWindowShell != null && !fWindowShell.isDisposed()) {
			int relativeLine= getRelativeLineNumber(line);
			fWindowLabel.setText(info);
			showWindow(computeCoveredArea(relativeLine), computeWindowLocation(relativeLine, event.x, event.y));
		}
	}
	/**
	 * Opens the popup window at the specified location. The window closes
	 * if the mouse pointer leaves the popup window.
	 *
	 * @param location the location at which this popup window will open
	 */	
	 private void showWindow(Rectangle coverArea, Point location) {
		
		Point size= fWindowLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		fWindowLabel.setSize(size.x + 3, size.y);
		fWindowShell.setSize(size.x + 5, size.y + 2);
		
		fWindowLabel.setLocation(1,1);
		fWindowShell.setLocation(location);
		
		new WindowCloser(coverArea).start();
		fWindowShell.setVisible(true);
	}
}
