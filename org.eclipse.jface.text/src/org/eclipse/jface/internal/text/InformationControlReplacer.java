/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Geometry;

import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension3;


/**
 * An information control replacer can replace an {@link AbstractInformationControlManager}'s
 * control.
 * <p>
 * The {@link AbstractInformationControlManager} can be configured with such a replacer by calling
 * <code>setInformationControlReplacer</code>.
 * </p>
 *
 * @since 3.4
 */
public class InformationControlReplacer extends AbstractInformationControlManager {

	/**
	 * Minimal width in pixels.
	 */
	private static final int MIN_WIDTH= 80;
	/**
	 * Minimal height in pixels.
	 */
	private static final int MIN_HEIGHT= 50;

	/**
	 * Default control creator.
	 */
	protected static class DefaultInformationControlCreator extends AbstractReusableInformationControlCreator {
		public IInformationControl doCreateInformationControl(Shell shell) {
			return new DefaultInformationControl(shell, true);
		}
	}

	private boolean fIsReplacing;
	private Object fReplacableInformation;
	private boolean fDelayedInformationSet;
	private Rectangle fReplaceableArea;
	private Rectangle fContentBounds;


	/**
	 * Creates a new information control replacer.
	 *
	 * @param creator the default information control creator
	 */
	public InformationControlReplacer(IInformationControlCreator creator) {
		super(creator);
		takesFocusWhenVisible(false);
	}

	/**
	 * Replace the information control.
	 *
	 * @param informationPresenterControlCreator the information presenter control creator
	 * @param contentBounds the bounds of the content area of the information control
	 * @param information the information to show
	 * @param subjectArea the subject area
	 * @param takeFocus <code>true</code> iff the replacing information control should take focus
	 */
	public void replaceInformationControl(IInformationControlCreator informationPresenterControlCreator, Rectangle contentBounds, Object information, final Rectangle subjectArea, boolean takeFocus) {

		try {
			fIsReplacing= true;
			if (! fDelayedInformationSet)
				fReplacableInformation= information;
			else
				takeFocus= true; // delayed input has been set, so the original info control must have been focused
			fContentBounds= contentBounds;
			fReplaceableArea= subjectArea;

			setCustomInformationControlCreator(informationPresenterControlCreator);

			takesFocusWhenVisible(takeFocus);

			showInformation();
		} finally {
			fIsReplacing= false;
			fReplacableInformation= null;
			fDelayedInformationSet= false;
			fReplaceableArea= null;
			setCustomInformationControlCreator(null);
		}
	}

	/*
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#computeInformation()
	 */
	protected void computeInformation() {
		if (fIsReplacing && fReplacableInformation != null) {
			setInformation(fReplacableInformation, fReplaceableArea);
			return;
		}

		if (DEBUG)
			System.out.println("InformationControlReplacer: no active replaceable"); //$NON-NLS-1$
	}

	/**
	 * Opens the information control with the given information and the specified
	 * subject area. It also activates the information control closer.
	 *
	 * @param subjectArea the information area
	 * @param information the information
	 */
	public void showInformationControl(Rectangle subjectArea, Object information) {
		IInformationControl informationControl= getInformationControl();

		Rectangle controlBounds= fContentBounds;
		if (informationControl instanceof IInformationControlExtension3) {
			IInformationControlExtension3 iControl3= (IInformationControlExtension3) informationControl;
			Rectangle trim= iControl3.computeTrim();
			controlBounds= Geometry.add(controlBounds, trim);

			/*
			 * Ensure minimal size. Interacting with a tiny information control
			 * (resizing, selecting text) would be a pain.
			 */
			controlBounds.width= Math.max(controlBounds.width, MIN_WIDTH);
			controlBounds.height= Math.max(controlBounds.height, MIN_HEIGHT);

			getInternalAccessor().cropToClosestMonitor(controlBounds);
		}

		Point location= Geometry.getLocation(controlBounds);
		Point size= Geometry.getSize(controlBounds);

		// Caveat: some IInformationControls fail unless setSizeConstraints(..) is called with concrete values
		informationControl.setSizeConstraints(size.x, size.y);

		if (informationControl instanceof IInformationControlExtension2)
			((IInformationControlExtension2) informationControl).setInput(information);
		else
			informationControl.setInformation(information.toString());

		informationControl.setLocation(location);
		informationControl.setSize(size.x, size.y);

		showInformationControl(subjectArea);
	}

	/*
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#hideInformationControl()
	 */
	public void hideInformationControl() {
		super.hideInformationControl();
	}

	/**
	 * @param input the delayed input, or <code>null</code> to request cancellation
	 */
	public void setDelayedInput(Object input) {
		fReplacableInformation= input;
		if (! isReplacing()) {
			fDelayedInformationSet= true;
		} else if (getCurrentInformationControl2() instanceof IInformationControlExtension2) {
			((IInformationControlExtension2) getCurrentInformationControl2()).setInput(input);
		} else if (getCurrentInformationControl2() != null) {
			getCurrentInformationControl2().setInformation(input.toString());
		}
	}

	/**
	 * Tells whether the replacer is currently replacing another information control.
	 *
	 * @return <code>true</code> while code from {@link #replaceInformationControl(IInformationControlCreator, Rectangle, Object, Rectangle, boolean)} is run
	 */
	public boolean isReplacing() {
		return fIsReplacing;
	}

	/**
	 * @return the current information control, or <code>null</code> if none available
	 */
	public IInformationControl getCurrentInformationControl2() {
		return getInternalAccessor().getCurrentInformationControl();
	}

	/**
	 * The number of pixels to blow up the keep-up zone.
	 *
	 * @return the margin in pixels
	 */
	public int getKeepUpMargin() {
		return 15;
	}
}
