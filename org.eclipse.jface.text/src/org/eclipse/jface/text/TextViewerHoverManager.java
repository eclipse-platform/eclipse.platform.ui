/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;


/**
 * This manager controls the layout, content, and visibility of an information
 * control in reaction to mouse hover events issued by the text widget of a
 * text viewer. It overrides <code>computeInformation</code>, so that the
 * computation is performed in a dedicated background thread. This implies
 * that the used <code>ITextHover</code> objects must be capable of 
 * operating in a non-UI thread.
 * 
 * @since 2.0
 */
class TextViewerHoverManager extends AbstractHoverInformationControlManager implements IWidgetTokenKeeper {
	
	/** The text viewer */
	private TextViewer fTextViewer;
	/** The hover information computation thread */
	private Thread fThread;
	/** The stopper of the computation thread */
	private ITextListener fStopper;
	/** Internal monitor */
	private Object fMutex= new Object();
	/** The currently shown text hover. */
	private volatile ITextHover fTextHover;
	
	/**
	 * Creates a new text viewer hover manager specific for the given text viewer.
	 * The manager uses the given information control creator.
	 *
	 * @param textViewer the viewer for which the controller is created
	 * @param creator the information control creator
	 */
	public TextViewerHoverManager(TextViewer textViewer, IInformationControlCreator creator) {
		super(creator);
		fTextViewer= textViewer;
		fStopper= new ITextListener() {
			public void textChanged(TextEvent event) {
				synchronized (fMutex) {
					if (fThread != null) {
						fThread.interrupt();
						fThread= null;
					}
				}
			}
		};
	}
	
	/**
	 * Determines all necessary details and delegates the computation into
	 * a background thread.
	 */
	protected void computeInformation() {
		
		Point location= getHoverEventLocation();
		int offset= computeOffsetAtLocation(location.x, location.y);
		if (offset == -1) {
			setInformation(null, null);
			return;
		}
			
		final ITextHover hover= fTextViewer.getTextHover(offset, getHoverEventStateMask());
		if (hover == null) {
			setInformation(null, null);
			return;
		}
			
		final IRegion region= hover.getHoverRegion(fTextViewer, offset);
		if (region == null) {
			setInformation(null, null);
			return;
		}
			
		final Rectangle area= computeArea(region);
		if (area == null || area.isEmpty()) {
			setInformation(null, null);
			return;
		}
		
		if (fThread != null) {
			setInformation(null, null);
			return;
		}
		
		fThread= new Thread("Text Viewer Hover Presenter") { //$NON-NLS-1$
			public void run() {
				// http://bugs.eclipse.org/bugs/show_bug.cgi?id=17693			
				try {
					
					if (fThread != null) {
						String information;
						try {
							information= hover.getHoverInfo(fTextViewer, region);
						} catch (ArrayIndexOutOfBoundsException x) {
							/*
							 * This code runs in a separate thread which can
							 * lead to text offsets being out of bounds when
							 * computing the hover info (see bug 32848).
							 */
							information= null;
						}
						setInformation(information, area);
						if (information != null && area != null)
							fTextHover= hover;
					} else {
						setInformation(null, null);
					}
					
				} finally {
					synchronized (fMutex) {
						if (fTextViewer != null)
							fTextViewer.removeTextListener(fStopper);
						fThread= null;
					}
				}
			}
		};
		
		fThread.setDaemon(true);
		fThread.setPriority(Thread.MIN_PRIORITY);
		synchronized (fMutex) {
			fTextViewer.addTextListener(fStopper);
			fThread.start();
		}
	}
	
	/**
	 * As computation is done in the background, this method is
	 * also called in the background thread. Delegates the control
	 * flow back into the ui thread, in order to allow displaying the
	 * information in the information control.
	 */
	protected void presentInformation() {
		if (fTextViewer == null)
			return;
			
		StyledText textWidget= fTextViewer.getTextWidget();
		if (textWidget != null && !textWidget.isDisposed()) {
			Display display= textWidget.getDisplay();
			if (display == null)
				return;
				
			display.asyncExec(new Runnable() {
				public void run() {
					doPresentInformation();
				}
			});
		}
	}
	
	/*
	 * @see AbstractInformationControlManager#presentInformation()
	 */
	protected void doPresentInformation() {
		super.presentInformation();
	}

	/**
	 * Computes the document offset underlying the given text widget coordinates.
	 * This method uses a linear search as it cannot make any assumption about
	 * how the document is actually presented in the widget. (Covers cases such
	 * as bidi text.)
	 *
	 * @param x the x coordinate inside the text widget
	 * @param y the y coordinate inside the text widget
	 * @return the document offset corresponding to the given point
	 */
	private int computeOffsetAtLocation(int x, int y) {
		
		try {
			
			StyledText styledText= fTextViewer.getTextWidget();
			int widgetOffset= styledText.getOffsetAtLocation(new Point(x, y));
			
			if (fTextViewer instanceof ITextViewerExtension3) {
				ITextViewerExtension3 extension= (ITextViewerExtension3) fTextViewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			}
			
			return widgetOffset + fTextViewer._getVisibleRegionOffset();

		} catch (IllegalArgumentException e) {
			return -1;	
		}
	}
	
	/**
	 * Determines graphical area covered by the given text region.
	 *
	 * @param region the region whose graphical extend must be computed
	 * @return the graphical extend of the given region
	 */
	private Rectangle computeArea(IRegion region) {
				
		IRegion widgetRegion= modelRange2WidgetRange(region);
		int start= widgetRegion.getOffset();
		int end= widgetRegion.getOffset() + widgetRegion.getLength();
				
		StyledText styledText= fTextViewer.getTextWidget();
		Point upperLeft= styledText.getLocationAtOffset(start);
		Point lowerRight= new Point(upperLeft.x, upperLeft.y);
		
		for (int i= start +1; i < end; i++) {
			
			Point p= styledText.getLocationAtOffset(i);
			
			if (upperLeft.x > p.x)
				upperLeft.x= p.x;
				
			if (upperLeft.y > p.y)
				upperLeft.y= p.y;
				
			if (lowerRight.x  < p.x)
				lowerRight.x= p.x;
				
			if (lowerRight.y < p.y)
				lowerRight.y= p.y;
		}

		lowerRight.x += fTextViewer.getAverageCharWidth();
		lowerRight.y += styledText.getLineHeight();
		
		int width= lowerRight.x - upperLeft.x;
		int height= lowerRight.y - upperLeft.y;
		return new Rectangle(upperLeft.x, upperLeft.y, width, height);
	}
	
	/**
	 * Translates a given region of the text viewer's document into
	 * the corresponding region of the viewer's widget.
	 * 
	 * @param region the document region
	 * @return the corresponding widget region
	 * @since 2.1
	 */
	private IRegion modelRange2WidgetRange(IRegion region) {
		if (fTextViewer instanceof ITextViewerExtension3) {
			ITextViewerExtension3 extension= (ITextViewerExtension3) fTextViewer;
			return extension.modelRange2WidgetRange(region);
		}
		
		IRegion visibleRegion= fTextViewer.getVisibleRegion();
		int start= region.getOffset() - visibleRegion.getOffset();
		int end= start + region.getLength();
		if (end > visibleRegion.getLength())
			end= visibleRegion.getLength();
			
		return new Region(start, end - start);
	}
	
	/*
	 * @see AbstractInformationControlManager#showInformationControl(Rectangle)
	 */
	protected void showInformationControl(Rectangle subjectArea) {
		if (fTextViewer != null && fTextViewer.requestWidgetToken(this))
			super.showInformationControl(subjectArea);
	}

	/*
	 * @see AbstractInformationControlManager#hideInformationControl()
	 */
	protected void hideInformationControl() {
		try {
			fTextHover= null;
			super.hideInformationControl();
		} finally {
			if (fTextViewer != null)
				fTextViewer.releaseWidgetToken(this);
		}
	}

	/*
	 * @see AbstractInformationControlManager#handleInformationControlDisposed()
	 */
	protected void handleInformationControlDisposed() {
		try {
			super.handleInformationControlDisposed();
		} finally {
			if (fTextViewer != null)
				fTextViewer.releaseWidgetToken(this);
		}
	}
	
	/*
	 * @see IWidgetTokenKeeper#requestWidgetToken(IWidgetTokenOwner)
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		fTextHover= null;
		super.hideInformationControl();		
		return true;
	}
	
	/**
	 * Returns the currently shown text hover or <code>null</code> if no text
	 * hover is shown.
	 * 
	 * @return the currently shown text hover or <code>null</code>
	 */
	protected ITextHover getCurrentTextHover() {
		return fTextHover;
	}
}
