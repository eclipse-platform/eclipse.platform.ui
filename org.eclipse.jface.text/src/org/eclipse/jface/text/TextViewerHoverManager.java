/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
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
class TextViewerHoverManager extends AbstractHoverInformationControlManager implements IWidgetTokenKeeper, IWidgetTokenKeeperExtension {


	/**
	 * Priority of the hovers managed by this manager.
	 * Default value: <code>0</code>;
	 * @since 3.0
	 */
	public final static int WIDGET_PRIORITY= 0;


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
	 * Tells whether the next mouse hover event
	 * should be processed.
	 * @since 3.0
	 */
	private boolean fProcessMouseHoverEvent= true;
	/**
	 * Internal mouse move listener.
	 * @since 3.0
	 */
	private MouseMoveListener fMouseMoveListener;
	/**
	 * Internal view port listener.
	 * @since 3.0
	 */
	private IViewportListener fViewportListener;


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
		fViewportListener= new IViewportListener() {
			/*
			 * @see org.eclipse.jface.text.IViewportListener#viewportChanged(int)
			 */
			public void viewportChanged(int verticalOffset) {
				fProcessMouseHoverEvent= false;
			}
		};
		fTextViewer.addViewportListener(fViewportListener);
		fMouseMoveListener= new MouseMoveListener() {
			/*
			 * @see MouseMoveListener#mouseMove(MouseEvent)
			 */
			public void mouseMove(MouseEvent event) {
				fProcessMouseHoverEvent= true;
			}
		};
		fTextViewer.getTextWidget().addMouseMoveListener(fMouseMoveListener);
	}

	/**
	 * Determines all necessary details and delegates the computation into
	 * a background thread.
	 */
	protected void computeInformation() {

		if (!fProcessMouseHoverEvent) {
			setInformation(null, null);
			return;
		}

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
				boolean hasFinished= false;
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

						if (hover instanceof ITextHoverExtension)
							setCustomInformationControlCreator(((ITextHoverExtension) hover).getHoverControlCreator());
						else
							setCustomInformationControlCreator(null);

						setInformation(information, area);
						if (information != null && area != null)
							fTextHover= hover;
					} else {
						setInformation(null, null);
					}
					hasFinished= true;
				} finally {
					synchronized (fMutex) {
						if (fTextViewer != null)
							fTextViewer.removeTextListener(fStopper);
						fThread= null;
						// https://bugs.eclipse.org/bugs/show_bug.cgi?id=44756
						if (!hasFinished)
							setInformation(null, null);
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
	 * flow back into the UI thread, in order to allow displaying the
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
	 * as bidirectional text.)
	 *
	 * @param x the horizontal coordinate inside the text widget
	 * @param y the vertical coordinate inside the text widget
	 * @return the document offset corresponding to the given point
	 */
	private int computeOffsetAtLocation(int x, int y) {

		try {

			StyledText styledText= fTextViewer.getTextWidget();
			int widgetOffset= styledText.getOffsetAtLocation(new Point(x, y));

			if (fTextViewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5) fTextViewer;
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

		int start= 0;
		int end= 0;
		IRegion widgetRegion= modelRange2WidgetRange(region);
		if (widgetRegion != null) {
			start= widgetRegion.getOffset();
			end= widgetRegion.getOffset() + widgetRegion.getLength();
		}

		StyledText styledText= fTextViewer.getTextWidget();
		Rectangle bounds;
		if (end > 0 && start < end)
			bounds= styledText.getTextBounds(start, end - 1);
		else {
			Point loc= styledText.getLocationAtOffset(widgetRegion.getOffset());
			bounds= new Rectangle(loc.x, loc.y, fTextViewer.getAverageCharWidth(), styledText.getLineHeight());
		}
		
		return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
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
		if (fTextViewer instanceof ITextViewerExtension5) {
			ITextViewerExtension5 extension= (ITextViewerExtension5) fTextViewer;
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
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#showInformationControl(org.eclipse.swt.graphics.Rectangle)
	 */
	protected void showInformationControl(Rectangle subjectArea) {
		if (fTextViewer != null && fTextViewer.requestWidgetToken(this, WIDGET_PRIORITY))
			super.showInformationControl(subjectArea);
	}

	/*
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#hideInformationControl()
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
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#handleInformationControlDisposed()
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
	 * @see org.eclipse.jface.text.IWidgetTokenKeeper#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner)
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner) {
		fTextHover= null;
		super.hideInformationControl();
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#requestWidgetToken(org.eclipse.jface.text.IWidgetTokenOwner, int)
	 * @since 3.0
	 */
	public boolean requestWidgetToken(IWidgetTokenOwner owner, int priority) {
		if (priority > WIDGET_PRIORITY) {
			fTextHover= null;
			super.hideInformationControl();
			return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IWidgetTokenKeeperExtension#setFocus(org.eclipse.jface.text.IWidgetTokenOwner)
	 * @since 3.0
	 */
	public boolean setFocus(IWidgetTokenOwner owner) {
		return false;
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

	/*
	 * @see org.eclipse.jface.text.AbstractHoverInformationControlManager#dispose()
	 * @since 3.0
	 */
	public void dispose() {
		if (fTextViewer != null) {
			fTextViewer.removeViewportListener(fViewportListener);
			fViewportListener= null;

			StyledText st= fTextViewer.getTextWidget();
			if (st != null && !st.isDisposed())
				st.removeMouseMoveListener(fMouseMoveListener);
			fMouseMoveListener= null;
		}
		super.dispose();
	}
}
