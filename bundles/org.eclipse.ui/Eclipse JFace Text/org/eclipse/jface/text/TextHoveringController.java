package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Assert;



/**
 * A text hovering controller. The controller registers with the 
 * text widget's control as a <code>MouseTrackListener<code>. When
 * receiving a mouse hover event, it opens a popup window using the 
 * appropriate <code>ITextHover</code>  to initialize the window's 
 * display information. The controller closes the window if the mouse 
 * pointer leaves the area for which the display information has been computed.<p>
 */
class TextHoveringController extends MouseTrackAdapter {		
	
	
	/**
	 * The  window closer.
	 */
	class WindowCloser extends MouseTrackAdapter implements
		MouseListener, MouseMoveListener, ControlListener, KeyListener, FocusListener {
		
		Rectangle fCoveredArea;
		
		/**
		 * Creates a new window closer for the given area.
		 */
		public WindowCloser(Rectangle coveredArea) {
			fCoveredArea= coveredArea;
		}
		
		/**
		 * Starts watching whether the popup window must be closed.
		 */
		public void start() {
			StyledText text= fTextViewer.getTextWidget();
			text.addMouseListener(this);
			text.addMouseMoveListener(this);
			text.addMouseTrackListener(this);
			text.addControlListener(this);
			text.addKeyListener(this);
			text.addFocusListener(this);
		}
		
		/**
		 * Closes the popup window and stops watching.
		 */
		private void stop() {
			
			fWindowShell.setVisible(false);
			
			StyledText text= fTextViewer.getTextWidget();
			text.removeMouseListener(this);
			text.removeMouseMoveListener(this);
			text.removeMouseTrackListener(this);
			text.removeControlListener(this);
			text.removeKeyListener(this);
			text.removeFocusListener(this);
			
			install();
		}
		
		/*
		 * @see MouseMoveListener#mouseMove
		 */
		public void mouseMove(MouseEvent event) {
			if (!fCoveredArea.contains(event.x, event.y))
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
		 * @see MouseTrackAdapter#mouseExit(MouseEvent)
		 */
		public void mouseExit(MouseEvent event) {
			stop();
		}
		
		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		public void controlResized(ControlEvent event) {
			stop();
		}
		
		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		public void controlMoved(ControlEvent event) {
			stop();
		}
		
		/*
		 * @see KeyListener#keyReleased(KeyEvent)
		 */
		public void keyReleased(KeyEvent event) {
		}
		
		/*
		 * @see KeyListener#keyPressed(KeyEvent)
		 */
		public void keyPressed(KeyEvent event) {
			stop();
		}
		
		/*
		 * @see FocusListener#focusLost(FocusEvent)
		 */
		public void focusLost(FocusEvent event) {
			if (fTextViewer.getTextWidget() == event.widget) {
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
	
	/** The text viewer this controller is connected to */
	private TextViewer fTextViewer;
	
	/** The popup window shell */
	private Shell fWindowShell;
	/** The label shown in the popup window shell */
	private Label fWindowLabel;
	
	
	
	/**
	 * Creates a new text hovering controller for the given text viewer. The
	 * controller registers as mouse track listener on the text viewer's text widget.
	 * Initially, the popup window is invisible.
	 *
	 * @param textViewer the viewer for which the controller is created
	 */
	public TextHoveringController(TextViewer textViewer) {
		Assert.isNotNull(textViewer);
		
		fTextViewer= textViewer;
		
		StyledText styledText= textViewer.getTextWidget();
		fWindowShell= new Shell(styledText.getShell(), SWT.NO_FOCUS | SWT.NO_TRIM | SWT.ON_TOP);
		fWindowLabel= new Label(fWindowShell, SWT.NO_FOCUS);
		
		Display display= styledText.getShell().getDisplay();
		fWindowShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		fWindowLabel.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
	}
	/**
	 * Determines graphical area covered by the given text region.
	 *
	 * @param region the region whose graphical extend must be computed
	 * @return the graphical extend of the given region
	 */
	private Rectangle computeCoveredArea(IRegion region) {
		
		// This is a hack - area too big/small - should be three rectangles and should consider line breaks
		
		int offset= fTextViewer.getVisibleRegionOffset();
		
		StyledText styledText= fTextViewer.getTextWidget();
		Point p1= styledText.getLocationAtOffset(region.getOffset() - offset);
		Point p2= styledText.getLocationAtOffset(region.getOffset() - offset + region.getLength());
		p2.y += styledText.getLineHeight();
		
		int x= Math.min(p1.x, p2.x);
		int y= p1.y;
		int width= Math.abs(p1.x - p2.x);
		int height= p2.y - p1.y;
		
		return new Rectangle(x, y, width, height);
	}
	/**
	 * Computes the document offset underlying the given text widget coordinates.
	 *
	 * @param x the x coordinate inside the text widget
	 * @param y the y coordinate inside the text widget
	 * @return the document offset corresponding to the given point
	 */
	private int computeOffsetAtLocation(int x, int y) {
		
		IDocument document= fTextViewer.getVisibleDocument();
		StyledText styledText= fTextViewer.getTextWidget();
		
		
		int line= (y + styledText.getTopPixel()) / styledText.getLineHeight();
		int lineCount= document.getNumberOfLines();
		
		if (line > lineCount - 1)
			line= lineCount - 1;
		
		if (line < 0)
			line= 0;
		
		try {
			
			IRegion lineInfo= document.getLineInformation(line);
			int low= lineInfo.getOffset();
			int high= low + lineInfo.getLength() - 1;
			
			while (high > low) {
				int offset= (low + high) / 2;
				int lookup= styledText.getLocationAtOffset(offset).x;
				if (lookup > x)
					high= offset - 1;
				else if (lookup < x)
					low= offset + 1;
				else
					low= high= offset;
			}
			
			return high + fTextViewer.getVisibleRegionOffset();
		
		} catch (BadLocationException e) {
		}
		
		return -1;
	}
	/**
	 * Determines the location of the popup window depending on
	 * the size of the covered area and the coordinates at which 
	 * the window has been requested.
	 * 
	 * @param x the x coordinate at which the window has been requested
	 * @param y the y coordinate at which the window has been requested
	 * @param coveredArea graphical area of the hover region
	 * @return the location of the hover popup window
	 */
	private Point computeWindowLocation(int x, int y, Rectangle coveredArea) {
		y= coveredArea.y + coveredArea.height + 5;
		return fTextViewer.getTextWidget().toDisplay(new Point(x, y));
	}
	/**
	 * Disposes this hovering controller	 
	 */
	public void dispose() {
		if (fWindowShell != null && !fWindowShell.isDisposed()) {
			fWindowShell.dispose();
			fWindowShell= null;
		}
	}
	/**
	 * Installs this hovering controller on its text viewer.
	 */
	public void install() {
		fTextViewer.getTextWidget().addMouseTrackListener(this);
	}
	/*
	 * @see MouseTrackAdapter#mouseHover
	 */
	public void mouseHover(MouseEvent event) {
		
		int offset= computeOffsetAtLocation(event.x, event.y);
		if (offset == -1)
			return;
			
		ITextHover hover= fTextViewer.getTextHover(offset);
		if (hover == null)
			return;
			
		IRegion region= hover.getHoverRegion(fTextViewer, offset);
		if (region == null)
			return;
			
		String info= hover.getHoverInfo(fTextViewer, region);
		if (info != null && info.trim().length() > 0) {
			Rectangle coveredArea= computeCoveredArea(region);
			if (fWindowShell != null && !fWindowShell.isDisposed()) {
				fWindowLabel.setText(info);
				showWindow(coveredArea, computeWindowLocation(event.x, event.y, coveredArea));
			}
		}
	}
	/**
	 * Opens the hover popup window at the specified location. The window closes if the
	 * mouse pointer leaves the specified area.
	 *
	 * @param coveredArea the area about which the hover popup window presents information
	 * @param location the location of the hover popup window will pop up
	 */
	private void showWindow(Rectangle coveredArea, Point location) {
		
		Point size= fWindowLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		fWindowLabel.setSize(size.x + 3, size.y);
		fWindowShell.setSize(size.x + 5, size.y + 2);
		
		fWindowLabel.setLocation(1,1);
		fWindowShell.setLocation(location);
		
		new WindowCloser(coveredArea).start();
		uninstall();
		
		fWindowShell.setVisible(true);
	}
	/**
	 * Uninstalls this hovering controller from its text viewer.
	 */
	public void uninstall() {
		fTextViewer.getTextWidget().removeMouseTrackListener(this);
	}
}
