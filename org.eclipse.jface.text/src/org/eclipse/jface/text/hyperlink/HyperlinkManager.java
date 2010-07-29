/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Steffen Pingel <steffen.pingel@tasktop.com> (Tasktop Technologies Inc.) - [navigation] hyperlink decoration is not erased when mouse is moved out of Text widget - https://bugs.eclipse.org/bugs/show_bug.cgi?id=100278
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;


/**
 * Default implementation of a hyperlink manager.
 *
 * @since 3.1
 */
public class HyperlinkManager implements ITextListener, Listener, KeyListener, MouseListener, MouseMoveListener, FocusListener, MouseTrackListener {


	/**
	 * Text operation code for requesting to open the hyperlink at the caret position.
	 * @see #openHyperlink()
	 * @since 3.6
	 */
	public static final int OPEN_HYPERLINK= ISourceViewer.QUICK_ASSIST + 1;


	/**
	 * Detection strategy.
	 */
	public static final class DETECTION_STRATEGY {

		String fName;

		private DETECTION_STRATEGY(String name) {
			fName= name;
		}

		/*
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return fName;
		}
	}


	/**
	 * The first detected hyperlink is passed to the
	 * hyperlink presenter and no further detector
	 * is consulted.
	 */
	public static final DETECTION_STRATEGY FIRST= new DETECTION_STRATEGY("first"); //$NON-NLS-1$

	/**
	 * All detected hyperlinks from all detectors are collected
	 * and passed to the hyperlink presenter.
	 * <p>
	 * This strategy is only allowed if {@link IHyperlinkPresenter#canShowMultipleHyperlinks()}
	 * returns <code>true</code>.
	 * </p>
	 */
	public static final DETECTION_STRATEGY ALL= new DETECTION_STRATEGY("all"); //$NON-NLS-1$

	/**
	 * All detected hyperlinks from all detectors are collected
	 * and all those with the longest region are passed to the
	 * hyperlink presenter.
	 * <p>
	 * This strategy is only allowed if {@link IHyperlinkPresenter#canShowMultipleHyperlinks()}
	 * returns <code>true</code>.
	 * </p>
	 */
	public static final DETECTION_STRATEGY LONGEST_REGION_ALL= new DETECTION_STRATEGY("all with same longest region"); //$NON-NLS-1$

	/**
	 * All detected hyperlinks from all detectors are collected
	 * and form all those with the longest region only the first
	 * one is passed to the hyperlink presenter.
	 */
	public static final DETECTION_STRATEGY LONGEST_REGION_FIRST= new DETECTION_STRATEGY("first with longest region"); //$NON-NLS-1$


	/** The text viewer on which this hyperlink manager works. */
	private ITextViewer fTextViewer;
	/** The session is active. */
	private boolean fActive;
	/** The key modifier mask. */
	private int fHyperlinkStateMask;
	/**
	 * The active key modifier mask.
	 * @since 3.3
	 */
	private int fActiveHyperlinkStateMask;
	/** The active hyperlinks. */
	private IHyperlink[] fActiveHyperlinks;
	/** The hyperlink detectors. */
	private IHyperlinkDetector[] fHyperlinkDetectors;
	/** The hyperlink presenter. */
	private IHyperlinkPresenter fHyperlinkPresenter;
	/** The detection strategy. */
	private final DETECTION_STRATEGY fDetectionStrategy;


	/**
	 * Creates a new hyperlink manager.
	 *
	 * @param detectionStrategy the detection strategy one of {{@link #ALL}, {@link #FIRST}, {@link #LONGEST_REGION_ALL}, {@link #LONGEST_REGION_FIRST}}
	 */
	public HyperlinkManager(DETECTION_STRATEGY detectionStrategy) {
		Assert.isNotNull(detectionStrategy);
		fDetectionStrategy= detectionStrategy;
	}

	/**
	 * Installs this hyperlink manager with the given arguments.
	 *
	 * @param textViewer the text viewer
	 * @param hyperlinkPresenter the hyperlink presenter
	 * @param hyperlinkDetectors the array of hyperlink detectors, must not be empty
	 * @param eventStateMask the SWT event state mask to activate hyperlink mode
	 */
	public void install(ITextViewer textViewer, IHyperlinkPresenter hyperlinkPresenter, IHyperlinkDetector[] hyperlinkDetectors, int eventStateMask) {
		Assert.isNotNull(textViewer);
		Assert.isNotNull(hyperlinkPresenter);
		fTextViewer= textViewer;
		fHyperlinkPresenter= hyperlinkPresenter;
		Assert.isLegal(fHyperlinkPresenter.canShowMultipleHyperlinks() || fDetectionStrategy == FIRST || fDetectionStrategy == LONGEST_REGION_FIRST);
		setHyperlinkDetectors(hyperlinkDetectors);
		setHyperlinkStateMask(eventStateMask);

		StyledText text= fTextViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		text.getDisplay().addFilter(SWT.KeyUp, this);
		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);
		text.addMouseTrackListener(this);

		fTextViewer.addTextListener(this);

		fHyperlinkPresenter.install(fTextViewer);
	}

	/**
	 * Sets the hyperlink detectors for this hyperlink manager.
	 * <p>
	 * It is allowed to call this method after this
	 * hyperlink manger has been installed.
	 * </p>
	 *
	 * @param hyperlinkDetectors and array of hyperlink detectors, must not be empty
	 */
	public void setHyperlinkDetectors(IHyperlinkDetector[] hyperlinkDetectors) {
		Assert.isTrue(hyperlinkDetectors != null && hyperlinkDetectors.length > 0);
		if (fHyperlinkDetectors == null)
			fHyperlinkDetectors= hyperlinkDetectors;
		else {
			synchronized (fHyperlinkDetectors) {
				fHyperlinkDetectors= hyperlinkDetectors;
			}
		}
	}

	/**
	 * Sets the SWT event state mask which in combination
	 * with the left mouse button triggers the hyperlink mode.
	 * <p>
	 * It is allowed to call this method after this
	 * hyperlink manger has been installed.
	 * </p>
	 *
	 * @param eventStateMask the SWT event state mask to activate hyperlink mode
	 */
	public void setHyperlinkStateMask(int eventStateMask) {
		fHyperlinkStateMask= eventStateMask;
	}

	/**
	 * Uninstalls this hyperlink manager.
	 */
	public void uninstall() {
		deactivate();

		StyledText text= fTextViewer.getTextWidget();
		if (text != null && !text.isDisposed()) {
			text.removeKeyListener(this);
			text.getDisplay().removeFilter(SWT.KeyUp, this);
			text.removeMouseListener(this);
			text.removeMouseMoveListener(this);
			text.removeFocusListener(this);
			text.removeMouseTrackListener(this);
		}
		fTextViewer.removeTextListener(this);

		fHyperlinkPresenter.uninstall();

		fHyperlinkPresenter= null;
		fTextViewer= null;
		fHyperlinkDetectors= null;
	}

	/**
	 * Deactivates the currently shown hyperlinks.
	 */
	protected void deactivate() {
		fHyperlinkPresenter.hideHyperlinks();
		fActive= false;
	}

	/**
	 * Finds hyperlinks at the current offset.
	 *
	 * @return the hyperlinks or <code>null</code> if none.
	 */
	protected IHyperlink[] findHyperlinks() {
		int offset= getCurrentTextOffset();
		if (offset == -1)
			return null;

		IRegion region= new Region(offset, 0);
		return findHyperlinks(region);
	}

	/**
	 * Returns the hyperlinks in the given region or <code>null</code> if none.
	 * 
	 * @param region the selection region
	 * @return the array of hyperlinks found or <code>null</code> if none
	 * @since 3.7
	 */
	private IHyperlink[] findHyperlinks(IRegion region) {
		List allHyperlinks= new ArrayList(fHyperlinkDetectors.length * 2);
		synchronized (fHyperlinkDetectors) {
			for (int i= 0, length= fHyperlinkDetectors.length; i < length; i++) {
				IHyperlinkDetector detector= fHyperlinkDetectors[i];
				if (detector == null)
					continue;

				if (detector instanceof IHyperlinkDetectorExtension2) {
					int stateMask= ((IHyperlinkDetectorExtension2)detector).getStateMask();
					if (stateMask != -1 && stateMask != fActiveHyperlinkStateMask)
						continue;
					else if (stateMask == -1 && fActiveHyperlinkStateMask != fHyperlinkStateMask)
					continue;
				} else if (fActiveHyperlinkStateMask != fHyperlinkStateMask)
					continue;

				boolean canShowMultipleHyperlinks= fHyperlinkPresenter.canShowMultipleHyperlinks();
				IHyperlink[] hyperlinks= detector.detectHyperlinks(fTextViewer, region, canShowMultipleHyperlinks);
				if (hyperlinks == null)
					continue;

				Assert.isLegal(hyperlinks.length > 0);

				if (fDetectionStrategy == FIRST) {
					if (hyperlinks.length == 1)
						return hyperlinks;
					return new IHyperlink[] {hyperlinks[0]};
				}
				allHyperlinks.addAll(Arrays.asList(hyperlinks));
			}
		}

		if (allHyperlinks.isEmpty())
			return null;

		if (fDetectionStrategy != ALL) {
			int maxLength= computeLongestHyperlinkLength(allHyperlinks);
			Iterator iter= new ArrayList(allHyperlinks).iterator();
			while (iter.hasNext()) {
				IHyperlink hyperlink= (IHyperlink)iter.next();
				if (hyperlink.getHyperlinkRegion().getLength() < maxLength)
					allHyperlinks.remove(hyperlink);
			}
		}

		if (fDetectionStrategy == LONGEST_REGION_FIRST)
			return new IHyperlink[] {(IHyperlink)allHyperlinks.get(0)};

		return (IHyperlink[])allHyperlinks.toArray(new IHyperlink[allHyperlinks.size()]);

	}

	/**
	 * Computes the length of the longest detected hyperlink.
	 *
	 * @param hyperlinks the list of hyperlinks
	 * @return the length of the longest detected
	 */
	protected int computeLongestHyperlinkLength(List hyperlinks) {
		Assert.isLegal(hyperlinks != null && !hyperlinks.isEmpty());
		Iterator iter= hyperlinks.iterator();
		int length= Integer.MIN_VALUE;
		while (iter.hasNext()) {
			IRegion region= ((IHyperlink)iter.next()).getHyperlinkRegion();
			if (region.getLength() < length)
				continue;
			length= region.getLength();
		}
		return length;
	}

	/**
	 * Returns the offset in the given viewer that corresponds to the current cursor location.
	 * 
	 * @return the offset in the given viewer that corresponds to the current cursor location.
	 */
	protected int getCurrentTextOffset() {
		return JFaceTextUtil.getOffsetForCursorLocation(fTextViewer);
	}

	/*
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent event) {

		if (fActive) {
			deactivate();
			return;
		}

		if (!isRegisteredStateMask(event.keyCode)) {
			deactivate();
			return;
		}

		fActive= true;
		fActiveHyperlinkStateMask= event.keyCode;

//			removed for #25871 (hyperlinks could interact with typing)
//
//			ITextViewer viewer= getSourceViewer();
//			if (viewer == null)
//				return;
//
//			IRegion region= getCurrentTextRegion(viewer);
//			if (region == null)
//				return;
//
//			highlightRegion(viewer, region);
//			activateCursor(viewer);
	}

	/*
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent event) {
	}

	/*
	 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDoubleClick(MouseEvent e) {

	}

	/*
	 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseDown(MouseEvent event) {

		if (!fActive)
			return;

		if (event.stateMask != fActiveHyperlinkStateMask) {
			deactivate();
			return;
		}

		if (event.button != 1) {
			deactivate();
			return;
		}
	}

	/*
	 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseUp(MouseEvent e) {

		if (!fActive) {
			fActiveHyperlinks= null;
			return;
		}

		if (e.button != 1)
			fActiveHyperlinks= null;

		deactivate();

		if (fActiveHyperlinks != null)
			fActiveHyperlinks[0].open();
	}

	/*
	 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
	 */
	public void mouseMove(MouseEvent event) {
		if (fHyperlinkPresenter instanceof IHyperlinkPresenterExtension) {
			if (!((IHyperlinkPresenterExtension)fHyperlinkPresenter).canHideHyperlinks())
				return;
		}

		if (!isRegisteredStateMask(event.stateMask)) {
			if (fActive)
				deactivate();

			return;
		}

		fActive= true;
		fActiveHyperlinkStateMask= event.stateMask;

		StyledText text= fTextViewer.getTextWidget();
		if (text == null || text.isDisposed()) {
			deactivate();
			return;
		}

		if ((event.stateMask & SWT.BUTTON1) != 0 && text.getSelectionCount() != 0) {
			deactivate();
			return;
		}

		fActiveHyperlinks= findHyperlinks();
		showHyperlinks(false);
	}

	/**
	 * Checks whether the given state mask is registered.
	 *
	 * @param stateMask the state mask
	 * @return <code>true</code> if a detector is registered for the given state mask
	 * @since 3.3
	 */
	private boolean isRegisteredStateMask(int stateMask) {
		if (stateMask == fHyperlinkStateMask)
			return true;

		synchronized (fHyperlinkDetectors) {
			for (int i= 0; i < fHyperlinkDetectors.length; i++) {
				if (fHyperlinkDetectors[i] instanceof IHyperlinkDetectorExtension2) {
					if (stateMask == ((IHyperlinkDetectorExtension2)fHyperlinkDetectors[i]).getStateMask())
						return true;
				}
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {}

	/*
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusLost(FocusEvent event) {
		deactivate();
	}

	/*
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 * @since 3.2
	 */
	public void handleEvent(Event event) {
		//key up
		deactivate();
	}

	/*
	 * @see org.eclipse.jface.text.ITextListener#textChanged(TextEvent)
	 * @since 3.2
	 */
	public void textChanged(TextEvent event) {
		if (event.getDocumentEvent() != null)
			deactivate();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public void mouseExit(MouseEvent e) {
		if (fHyperlinkPresenter instanceof IHyperlinkPresenterExtension) {
			if (!((IHyperlinkPresenterExtension)fHyperlinkPresenter).canHideHyperlinks())
				return;
		}
		deactivate();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public void mouseEnter(MouseEvent e) {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public void mouseHover(MouseEvent e) {
	}

	/**
	 * Opens the hyperlink at the current caret location directly if there's only one link, else
	 * opens the hyperlink control showing all the hyperlinks at that location.
	 * 
	 * @param takesFocusWhenVisible <code>true</code> if the control takes focus when visible,
	 *            <code>false</code> otherwise
	 * 
	 * @return <code>true</code> if at least one hyperlink has been found at the caret location,
	 *         <code>false</code> otherwise
	 * @since 3.7
	 */
	private boolean showHyperlinks(boolean takesFocusWhenVisible) {
		
		if (fActiveHyperlinks == null || fActiveHyperlinks.length == 0) {
			fHyperlinkPresenter.hideHyperlinks();
			return false;
		}
		if (fActiveHyperlinks.length == 1 && takesFocusWhenVisible) {
			fActiveHyperlinks[0].open();
		} else {
			if (fHyperlinkPresenter instanceof IHyperlinkPresenterExtension2)
				((IHyperlinkPresenterExtension2)fHyperlinkPresenter).showHyperlinks(fActiveHyperlinks, takesFocusWhenVisible);
			else
				fHyperlinkPresenter.showHyperlinks(fActiveHyperlinks);
		}
		return true;

	}

	/**
	 * Opens the hyperlink at the caret location or opens a chooser
	 * if more than one hyperlink is available.
	 * 
	 * @return <code>true</code> if at least one hyperlink has been found at the caret location, <code>false</code> otherwise
	 * @see #OPEN_HYPERLINK
	 * @since 3.6
	 */
	public boolean openHyperlink() {
		fActiveHyperlinkStateMask= fHyperlinkStateMask;

		if (fHyperlinkPresenter instanceof IHyperlinkPresenterExtension) {
			if (!((IHyperlinkPresenterExtension)fHyperlinkPresenter).canHideHyperlinks())
				return false;
		}
		ITextSelection sel= (ITextSelection)((TextViewer)fTextViewer).getSelection();
		int offset= sel.getOffset();
		if (offset == -1)
			return false;

		IRegion region= new Region(offset, 0);
		fActiveHyperlinks= findHyperlinks(region);
		return showHyperlinks(true);
	}
}
