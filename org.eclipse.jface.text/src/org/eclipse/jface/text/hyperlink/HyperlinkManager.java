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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;


/**
 * Default implementation of a hyperlink manager.
 *
 * @since 3.1
 */
public class HyperlinkManager implements KeyListener, MouseListener, MouseMoveListener, FocusListener {

	/**
	 * Detection strategy.
	 */
	private static final class DETECTION_STRATEGY {

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
		Assert.isLegal(!fHyperlinkPresenter.canShowMultipleHyperlinks() && (fDetectionStrategy == FIRST || fDetectionStrategy == LONGEST_REGION_FIRST));
		setHyperlinkDetectors(hyperlinkDetectors);
		setHyperlinkStateMask(eventStateMask);

		StyledText text= fTextViewer.getTextWidget();
		if (text == null || text.isDisposed())
			return;

		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);

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
			text.removeMouseListener(this);
			text.removeMouseMoveListener(this);
			text.removeFocusListener(this);
		}
		fHyperlinkPresenter.uninstall();

		fHyperlinkPresenter= null;
		fTextViewer= null;
		fHyperlinkDetectors= null;
	}

	protected void deactivate() {
		if (!fActive)
			return;

		fHyperlinkPresenter.hideHyperlinks();
		fActive= false;
	}

	protected IHyperlink[] findHyperlinks() {
		int offset= getCurrentTextOffset();
		if (offset == -1)
			return null;

		boolean canShowMultipleHyperlinks= fHyperlinkPresenter.canShowMultipleHyperlinks();
		IRegion region= new Region(offset, 0);
		List allHyperlinks= new ArrayList(fHyperlinkDetectors.length * 2);
		synchronized (fHyperlinkDetectors) {
			for (int i= 0, length= fHyperlinkDetectors.length; i < length; i++) {
				IHyperlinkDetector detector= fHyperlinkDetectors[i];
				if (detector == null)
					continue;

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

	protected int getCurrentTextOffset() {

		try {
			StyledText text= fTextViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return -1;

			Display display= text.getDisplay();
			Point absolutePosition= display.getCursorLocation();
			Point relativePosition= text.toControl(absolutePosition);

			int widgetOffset= text.getOffsetAtLocation(relativePosition);
			if (fTextViewer instanceof ITextViewerExtension5) {
				ITextViewerExtension5 extension= (ITextViewerExtension5)fTextViewer;
				return extension.widgetOffset2ModelOffset(widgetOffset);
			}

			return widgetOffset + fTextViewer.getVisibleRegion().getOffset();

		} catch (IllegalArgumentException e) {
			return -1;
		}
	}

	/*
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent event) {

		if (fActive) {
			deactivate();
			return;
		}

		if (event.keyCode != fHyperlinkStateMask) {
			deactivate();
			return;
		}

		fActive= true;

//			removed for #25871
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

		if (!fActive)
			return;

		deactivate();
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

		if (event.stateMask != fHyperlinkStateMask) {
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
	
		if (!fActive) {
			if (event.stateMask != fHyperlinkStateMask)
				return;
			// modifier was already pressed
			fActive= true;
		}

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
		if (fActiveHyperlinks == null || fActiveHyperlinks.length == 0) {
			fHyperlinkPresenter.hideHyperlinks();
			return;
		}

		fHyperlinkPresenter.showHyperlinks(fActiveHyperlinks);
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
}
