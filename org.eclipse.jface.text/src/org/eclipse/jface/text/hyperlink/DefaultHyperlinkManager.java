/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;

/**
 * Default implementation of a hyperlink manager.
 * <p>
 * NOTE: This API is work in progress and may change before the final API freeze.
 * </p>
 * 
 * @since 3.1
 */
public class DefaultHyperlinkManager implements IHyperlinkManager, KeyListener, MouseListener, MouseMoveListener, FocusListener {

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
	 * A named preference that controls if hyperlinks are turned on or off.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.1
	 */
	public static final String HYPERLINKS_ENABLED= "hyperlinksEnabled"; //$NON-NLS-1$

	/**
	 * A named preference that controls the key modifier for hyperlinks.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @since 3.1
	 */
	public static final String HYPERLINK_KEY_MODIFIER= "hyperlinkKeyModifier"; //$NON-NLS-1$

	/**
	 * A named preference that controls the key modifier mask for hyperlinks.
	 * The value is only used if the value of <code>EHYPERLINK_KEY_MODIFIER</code>
	 * cannot be resolved to valid SWT modifier bits.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @see #HYPERLINK_KEY_MODIFIER
	 * @since 3.1
	 */
	public static final String HYPERLINK_KEY_MODIFIER_MASK= "hyperlinkKeyModifierMask"; //$NON-NLS-1$

	/**
	 * The first detected hyperlink is passed to the
	 * hyperlink controller and no further detector
	 * is consulted.
	 */
	public static final DETECTION_STRATEGY FIRST= new DETECTION_STRATEGY("first"); //$NON-NLS-1$
	
	/**
	 * All detected hyperlinks from all detectors are collected
	 * and passed to the hyperlink controller.
	 * <p>
	 * This strategy is only allowed if {@link IHyperlinkController#canShowMultipleHyperlinks()}
	 * returns <code>true</code>.
	 * </p>
	 */
	public static final DETECTION_STRATEGY ALL= new DETECTION_STRATEGY("all"); //$NON-NLS-1$
	
	/**
	 * All detected hyperlinks from all detectors are collected
	 * and all those with the longest region are passed to the
	 * hyperlink controller.
	 * <p>
	 * This strategy is only allowed if {@link IHyperlinkController#canShowMultipleHyperlinks()}
	 * returns <code>true</code>.
	 * </p>
	 */
	public static final DETECTION_STRATEGY LONGEST_REGION_ALL= new DETECTION_STRATEGY("all with same longest region"); //$NON-NLS-1$
	
	/**
	 * All detected hyperlinks from all detectors are collected
	 * and form all those with the longest region only the first
	 * one is passed to the hyperlink controller.
	 */
	public static final DETECTION_STRATEGY LONGEST_REGION_FIRST= new DETECTION_STRATEGY("first with longest region"); //$NON-NLS-1$

	
	/** The text viewer on which this hyperlink manager works. */
	private ITextViewer fTextViewer;
	/** The session is active. */
	private boolean fActive;
	/** The key modifier mask. */
	private int fKeyModifierMask;
	/** The active hyperlinks. */
	private IHyperlink[] fActiveHyperlinks;
	/** The hyperlink detectors. */
	private IHyperlinkDetector[] fHyperlinkDetectors;
	/** The hyperlink controller. */
	private IHyperlinkController fHyperlinkController;
	/** The detection strategy. */
	private final DETECTION_STRATEGY fDetectionStrategy;

	
	/**
	 * Creates a new hyperlink manager.
	 * 
	 * @param detectionStrategy the detection strategy one of {{@link #ALL}, {@link #FIRST}, {@link #LONGEST_REGION_ALL}, {@link #LONGEST_REGION_FIRST}}
	 */
	public DefaultHyperlinkManager(DETECTION_STRATEGY detectionStrategy) {
		Assert.isNotNull(detectionStrategy);
		fDetectionStrategy= detectionStrategy;
	}

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkManager#install(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.hyperlink.IHyperlinkController, org.eclipse.jface.text.hyperlink.IHyperlinkDetector[], int)
	 * @since 3.1
	 */
	public void install(ITextViewer textViewer, IHyperlinkController hyperlinkController, IHyperlinkDetector[] hyperlinkDetectors, int keyModifierMask) {
		Assert.isNotNull(textViewer);
		Assert.isNotNull(hyperlinkController);
		fTextViewer= textViewer;
		fHyperlinkController= hyperlinkController;
		Assert.isLegal(!fHyperlinkController.canShowMultipleHyperlinks() && (fDetectionStrategy == FIRST || fDetectionStrategy == LONGEST_REGION_FIRST)); 
		setHyperlinkDetectors(hyperlinkDetectors);
		setKeyModifierMask(keyModifierMask);
		
		StyledText text= fTextViewer.getTextWidget();			
		if (text == null || text.isDisposed())
			return;
		
		text.addKeyListener(this);
		text.addMouseListener(this);
		text.addMouseMoveListener(this);
		text.addFocusListener(this);
		
		fHyperlinkController.install(fTextViewer);
	}
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkManager#setHyperlinkDetectors(org.eclipse.jface.text.hyperlink.IHyperlinkDetector[])
	 * @since 3.1
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
	
	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkManager#setKeyModifierMask(int)
	 * @since 3.1
	 */
	public void setKeyModifierMask(int keyModifierMask) {
		fKeyModifierMask= keyModifierMask;
	}

	/*
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkManager#uninstall()
	 * @since 3.1
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
		fHyperlinkController.uninstall();
		
		fHyperlinkController= null;
		fTextViewer= null;
		fHyperlinkDetectors= null;
	}

	protected void deactivate() {
		if (!fActive)
			return;

		fHyperlinkController.deactivate();
		fActive= false;
	}

	protected IHyperlink[] findHyperlinks() {
		int offset= getCurrentTextOffset();				
		if (offset == -1)
			return null;
		
		IRegion region= new Region(offset, 0);
		List allHyperlinks= new ArrayList(fHyperlinkDetectors.length * 2);
		synchronized (fHyperlinkDetectors) {
			for (int i= 0, length= fHyperlinkDetectors.length; i < length; i++) {
				IHyperlink[] hyperlinks= fHyperlinkDetectors[i].detectHyperlinks(fTextViewer, region);
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
				if (hyperlink.getRegion().getLength() < maxLength)
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
			IRegion region= ((IHyperlink)iter.next()).getRegion();
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

		if (event.keyCode != fKeyModifierMask) {
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
			
		if (event.stateMask != fKeyModifierMask) {
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
		
		if (event.widget instanceof Control && !((Control) event.widget).isFocusControl()) {
			deactivate();
			return;
		}
		
		if (!fActive) {
			if (event.stateMask != fKeyModifierMask)
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
			fHyperlinkController.deactivate();
			return;
		}
		
		fHyperlinkController.activate(fActiveHyperlinks);
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