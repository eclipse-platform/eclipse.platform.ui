/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sean Montgomery, sean_montgomery@comcast.net - https://bugs.eclipse.org/bugs/show_bug.cgi?id=45095
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.internal.text.InformationControlReplacer;
import org.eclipse.jface.internal.text.InternalAccessor;
import org.eclipse.jface.util.Geometry;

import org.eclipse.jface.text.ITextViewerExtension8.EnrichMode;


/**
 * Manages the life cycle, visibility, layout, and contents of an
 * {@link org.eclipse.jface.text.IInformationControl}. This manager can be
 * installed on and removed from a control, referred to as the subject control,
 * i.e. the one from which the subject of the information to be shown is
 * retrieved. Also a manager can be enabled or disabled. An installed and
 * enabled manager can be forced to show information in its information control
 * using <code>showInformation</code>. An information control manager uses an
 * <code>IInformationControlCloser</code> to define the behavior when a
 * presented information control must be closed. The disposal of the subject and
 * the information control are internally handled by the information control
 * manager and are not the responsibility of the information control closer.
 *
 * @see org.eclipse.jface.text.IInformationControl
 * @since 2.0
 */
abstract public class AbstractInformationControlManager {

	/**
	 * An internal class that gives access to internal methods.
	 *
	 * @since 3.4
	 */
	class MyInternalAccessor extends InternalAccessor {
		public IInformationControl getCurrentInformationControl() {
			return AbstractInformationControlManager.this.getCurrentInformationControl();
		}

		public void setInformationControlReplacer(InformationControlReplacer replacer) {
			AbstractInformationControlManager.this.setInformationControlReplacer(replacer);
		}

		public InformationControlReplacer getInformationControlReplacer() {
			return AbstractInformationControlManager.this.getInformationControlReplacer();
		}

		public boolean canReplace(IInformationControl control) {
			return AbstractInformationControlManager.this.canReplace(control);
		}

		public boolean isReplaceInProgress() {
			return AbstractInformationControlManager.this.isReplaceInProgress();
		}

		public void replaceInformationControl(boolean takeFocus) {
			AbstractInformationControlManager.this.replaceInformationControl(takeFocus);
		}

		public void cropToClosestMonitor(Rectangle bounds) {
			AbstractInformationControlManager.this.cropToClosestMonitor(bounds);
		}

		public void setHoverEnrichMode(EnrichMode mode) {
			throw new UnsupportedOperationException("only implemented in AbstractHoverInformationControlManager"); //$NON-NLS-1$
		}

		public boolean getAllowMouseExit() {
			throw new UnsupportedOperationException("only implemented in AnnotationBarHoverManager"); //$NON-NLS-1$
		}
	}

	/**
	 * Interface of an information control closer. An information control closer
	 * monitors its information control and its subject control and closes the
	 * information control if necessary.
	 * <p>
	 * Clients must implement this interface in order to equip an information
	 * control manager accordingly.
	 */
	public interface IInformationControlCloser {

		/**
		 * Sets the closer's subject control. This is the control that parents
		 * the information control and from which the subject of the information
		 * to be shown is retrieved. <p>
		 * Must be called before <code>start</code>. May again be called
		 * between <code>start</code> and <code>stop</code>.
		 *
		 * @param subject the subject control
		 */
		public void setSubjectControl(Control subject);

		/**
		 * Sets the closer's information control, the one to close if necessary. <p>
		 * Must be called before <code>start</code>. May again be called
		 * between <code>start</code> and <code>stop</code>.
		 *
		 * @param control the information control
		 */
		public void setInformationControl(IInformationControl control);

		/**
		 * Tells this closer to start monitoring the subject and the information
		 * control. The presented information is considered valid for the given
		 * area of the subject control's display.
		 *
		 * @param subjectArea the area for which the presented information is valid
		 */
		public void start(Rectangle subjectArea);

		/**
		 * Tells this closer to stop monitoring the subject and the information control.
		 */
		public void stop();
	}



	/**
	 * Constitutes entities to enumerate anchors for the layout of the information control.
	 */
	public static final class Anchor {
		private final int fFlag;
		private Anchor(int flag) {
			fFlag= flag;
		}
		/**
		 * Returns the SWT direction flag. One of {@link SWT#BOTTOM}, {@link SWT#TOP},
		 * {@link SWT#LEFT}, {@link SWT#RIGHT}, {@link SWT#CENTER},
		 *
		 * @return the SWT direction flag
		 * @since 3.3
		 */
		int getSWTFlag() {
			return fFlag;
		}

		public String toString() {
			switch (fFlag) {
				case SWT.BOTTOM: return "BOTTOM"; //$NON-NLS-1$
				case SWT.TOP: return "TOP"; //$NON-NLS-1$
				case SWT.LEFT: return "LEFT"; //$NON-NLS-1$
				case SWT.RIGHT: return "RIGHT"; //$NON-NLS-1$
				case SWT.CENTER: return "CENTER"; //$NON-NLS-1$
				default: return Integer.toHexString(fFlag);
			}
		}
	}

	/** Internal anchor list. */
	private final static Anchor[] ANCHORS= { new Anchor(SWT.TOP), new Anchor(SWT.BOTTOM), new Anchor(SWT.LEFT), new Anchor(SWT.RIGHT) };

	/** Anchor representing the top of the information area */
	public final static Anchor ANCHOR_TOP=  ANCHORS[0];
	/** Anchor representing the bottom of the information area */
	public final static Anchor ANCHOR_BOTTOM=  ANCHORS[1];
	/** Anchor representing the left side of the information area */
	public final static Anchor ANCHOR_LEFT=  ANCHORS[2];
	/** Anchor representing the right side of the information area */
	public final static Anchor ANCHOR_RIGHT= ANCHORS[3];
	/**
	 * Anchor representing the middle of the subject control
	 * @since 2.1
	 */
	public final static Anchor ANCHOR_GLOBAL= new Anchor(SWT.CENTER);

	/**
	 * Dialog store constant for the location's x-coordinate.
	 * @since 3.0
	 */
	public static final String STORE_LOCATION_X= "location.x"; //$NON-NLS-1$
	/**
	 * Dialog store constant for the location's y-coordinate.
	 * @since 3.0
	 */
	public static final String STORE_LOCATION_Y= "location.y"; //$NON-NLS-1$
	/**
	 * Dialog store constant for the size's width.
	 * @since 3.0
	 */
	public static final String STORE_SIZE_WIDTH= "size.width"; //$NON-NLS-1$
	/**
	 * Dialog store constant for the size's height.
	 * @since 3.0
	 */
	public static final String STORE_SIZE_HEIGHT= "size.height"; //$NON-NLS-1$

	/**
	 * Tells whether this class and its subclasses are in debug mode.
	 * <p>
	 * Subclasses may use this.
	 * </p>
	 * @since 3.4
	 */
	protected static final boolean DEBUG= "true".equalsIgnoreCase(Platform.getDebugOption("org.eclipse.jface.text/debug/AbstractInformationControlManager"));  //$NON-NLS-1$//$NON-NLS-2$


	/** The subject control of the information control */
	private Control  fSubjectControl;

	/** The display area for which the information to be presented is valid */
	private Rectangle fSubjectArea;

	/** The information to be presented */
	private Object fInformation;

	/** Indicates whether the information control takes focus when visible */
	private boolean fTakesFocusWhenVisible= false;

	/**
	 * The information control.
	 *
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons.
	 */
	protected IInformationControl fInformationControl;

	/**
	 * The information control creator.
	 *
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons.
	 */
	protected IInformationControlCreator fInformationControlCreator;

	/**
	 * The information control closer.
	 *
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons.
	 */
	protected IInformationControlCloser fInformationControlCloser;

	/**
	 * Indicates that the information control has been disposed.
	 *
	 * <p>This field should not be referenced by subclasses. It is <code>protected</code> for API
	 * compatibility reasons.
	 */
	protected boolean fDisposed= false;

	/**
	 * The information control replacer to be used when this information control
	 * needs to be replaced with another information control.
	 *
	 * @since 3.4
	 */
	private InformationControlReplacer fInformationControlReplacer;

	/** Indicates the enable state of this manager */
	private boolean fEnabled= false;

	/** Cached, computed size constraints of the information control in points */
	private Point fSizeConstraints;

	/** The vertical margin when laying out the information control */
	private int fMarginY= 5;

	/** The horizontal margin when laying out the information control */
	private int fMarginX= 5;

	/** The width constraint of the information control in characters */
	private int fWidthConstraint= 60;

	/** The height constraint of the information control  in characters */
	private int fHeightConstraint= 6;

	/** Indicates whether the size constraints should be enforced as minimal control size */
	private boolean fEnforceAsMinimalSize= false;

	/** Indicates whether the size constraints should be enforced as maximal control size */
	private boolean fEnforceAsMaximalSize= false;

	/** The anchor for laying out the information control in relation to the subject control */
	private Anchor fAnchor= ANCHOR_BOTTOM;

	/**
	 * The anchor sequence used to layout the information control if the original anchor
	 * can not be used because the information control would not fit in the display client area.
	 * <p>
	 * The fallback anchor for a given anchor is the one that comes directly after the given anchor or
	 * is the first one in the sequence if the given anchor is the last one in the sequence.
	 * <p>
	 * </p>
	 * Note: This sequence is ignored if the original anchor is not contained in this sequence.
	 * </p>
	 *
	 * @see #fAnchor
	 */
	private Anchor[] fFallbackAnchors= ANCHORS;

	/**
	 * The custom information control creator.
	 * @since 3.0
	 */
	private volatile IInformationControlCreator fCustomInformationControlCreator;

	/**
	 * Tells whether a custom information control is in use.
	 * @since 3.0
	 */
	private boolean fIsCustomInformationControl= false;

	/**
	 * The dialog settings for the control's bounds.
	 * @since 3.0
	 */
	private IDialogSettings fDialogSettings;

	/**
	 * Tells whether the control's location should be read
	 * from the dialog settings and whether the last
	 * valid control's size is stored back into the  settings.
	 *
	 * @since 3.0
	 */
	private boolean fIsRestoringLocation;

	/**
	 * Tells whether the control's size should be read
	 * from the dialog settings and whether the last
	 * valid control's size is stored back into the  settings.
	 *
	 * @since 3.0
	 */
	private boolean fIsRestoringSize;

	/**
	 * The dispose listener on the subject control.
	 *
	 * @since 3.1
	 */
	private DisposeListener fSubjectControlDisposeListener;


	/**
	 * Creates a new information control manager using the given information control creator.
	 * By default the following configuration is given:
	 * <ul>
	 * <li> enabled == false
	 * <li> horizontal margin == 5 points
	 * <li> vertical margin == 5 points
	 * <li> width constraint == 60 characters
	 * <li> height constraint == 6 characters
	 * <li> enforce constraints as minimal size == false
	 * <li> enforce constraints as maximal size == false
	 * <li> layout anchor == ANCHOR_BOTTOM
	 * <li> fall back anchors == { ANCHOR_TOP, ANCHOR_BOTTOM, ANCHOR_LEFT, ANCHOR_RIGHT, ANCHOR_GLOBAL }
	 * <li> takes focus when visible == false
	 * </ul>
	 *
	 * @param creator the information control creator
	 */
	protected AbstractInformationControlManager(IInformationControlCreator creator) {
		Assert.isNotNull(creator);
		fInformationControlCreator= creator;
	}

	/**
	 * Computes the information to be displayed and the area in which the computed
	 * information is valid. Implementation of this method must finish their computation
	 * by setting the computation results using <code>setInformation</code>.
	 */
	abstract protected void computeInformation();

	/**
	 * Sets the parameters of the information to be displayed. These are the information itself and
	 * the area for which the given information is valid. This so called subject area is a graphical
	 * region of the information control's subject control. This method calls <code>presentInformation()</code>
	 * to trigger the presentation of the computed information.
	 *
	 * @param information the information, or <code>null</code> if none is available
	 * @param subjectArea the subject area, or <code>null</code> if none is available
	 */
	protected final void setInformation(String information, Rectangle subjectArea) {
		setInformation((Object)information, subjectArea);
	}

	/**
	 * Sets the parameters of the information to be displayed. These are the information itself and
	 * the area for which the given information is valid. This so called subject area is a graphical
	 * region of the information control's subject control. This method calls <code>presentInformation()</code>
	 * to trigger the presentation of the computed information.
	 *
	 * @param information the information, or <code>null</code> if none is available
	 * @param subjectArea the subject area, or <code>null</code> if none is available
	 * @since  2.1
	 */
	protected final void setInformation(Object information, Rectangle subjectArea) {
		fInformation= information;
		fSubjectArea= subjectArea;
		presentInformation();
	}

	/**
	 * Sets the information control closer for this manager.
	 *
	 * @param closer the information control closer for this manager
	 */
	protected void setCloser(IInformationControlCloser closer) {
		fInformationControlCloser= closer;
	}

	/**
	 * Sets the information control replacer for this manager and disposes the
	 * old one if set.
	 *
	 * @param replacer the information control replacer for this manager, or
	 *            <code>null</code> if no information control replacing should
	 *            take place
	 * @since 3.4
	 */
	void setInformationControlReplacer(InformationControlReplacer replacer) {
		if (fInformationControlReplacer != null)
			fInformationControlReplacer.dispose();
		fInformationControlReplacer= replacer;
	}

	/**
	 * Returns the current information control replacer or <code>null</code> if none has been installed.
	 *
	 * @return the current information control replacer or <code>null</code> if none has been installed
	 * @since 3.4
	 */
	InformationControlReplacer getInformationControlReplacer() {
		return fInformationControlReplacer;
	}

	/**
	 * Returns whether an information control replacer has been installed.
	 *
	 * @return whether an information control replacer has been installed
	 * @since 3.4
	 */
	boolean hasInformationControlReplacer() {
		return fInformationControlReplacer != null;
	}

	/**
	 * Tests whether the given information control is replaceable.
	 *
	 * @param iControl information control or <code>null</code> if none
	 * @return <code>true</code> if information control is replaceable, <code>false</code> otherwise
	 * @since 3.4
	 */
	boolean canReplace(IInformationControl iControl) {
		return iControl instanceof IInformationControlExtension3
				&& iControl instanceof IInformationControlExtension5
				&& ((IInformationControlExtension5) iControl).getInformationPresenterControlCreator() != null;
	}

	/**
	 * Returns the current information control, or <code>null</code> if none.
	 *
	 * @return the current information control, or <code>null</code> if none
	 * @since 3.4
	 */
	IInformationControl getCurrentInformationControl() {
		return fInformationControl;
	}

	/**
	 * Tells whether this manager's information control is currently being replaced.
	 *
	 * @return <code>true</code> if a replace is in progress
	 * @since 3.4
	 */
	boolean isReplaceInProgress() {
		return fInformationControlReplacer != null && fInformationControlReplacer.isReplacing();
	}

	/**
	 * Sets the horizontal and vertical margin to be used when laying out the
	 * information control relative to the subject control.
	 *
	 * @param xMargin the x-margin
	 * @param yMargin the y-Margin
	 */
	public void setMargins(int xMargin, int yMargin) {
		fMarginX= xMargin;
		fMarginY= yMargin;
	}

	/**
	 * Sets the width- and height constraints of the information control.
	 *
	 * @param widthInChar the width constraint in number of characters
	 * @param heightInChar the height constrain in number of characters
	 * @param enforceAsMinimalSize indicates whether the constraints describe the minimal allowed size of the control
	 * @param enforceAsMaximalSize indicates whether the constraints describe the maximal allowed size of the control
	 */
	public void setSizeConstraints(int widthInChar, int heightInChar, boolean enforceAsMinimalSize, boolean enforceAsMaximalSize) {
		fSizeConstraints= null;
		fWidthConstraint= widthInChar;
		fHeightConstraint= heightInChar;
		fEnforceAsMinimalSize= enforceAsMinimalSize;
		fEnforceAsMaximalSize= enforceAsMaximalSize;

	}

	/**
	 * Tells this information control manager to open the information control with the values
	 * contained in the given dialog settings and to store the control's last valid size in the
	 * given dialog settings.
	 * <p>
	 * Note: This API is only valid if the information control implements
	 * {@link IInformationControlExtension3}. Not following this restriction will later result in an
	 * {@link UnsupportedOperationException}.
	 * </p>
	 * <p>
	 * The constants used to store the values are:
	 * <ul>
	 * <li>{@link AbstractInformationControlManager#STORE_LOCATION_X}</li>
	 * <li>{@link AbstractInformationControlManager#STORE_LOCATION_Y}</li>
	 * <li>{@link AbstractInformationControlManager#STORE_SIZE_WIDTH}</li>
	 * <li>{@link AbstractInformationControlManager#STORE_SIZE_HEIGHT}</li>
	 * </ul>
	 * </p>
	 *
	 * @param dialogSettings the dialog settings
	 * @param restoreLocation <code>true</code> iff the location is must be (re-)stored
	 * @param restoreSize <code>true</code>iff the size is (re-)stored
	 * @since 3.0
	 */
	public void setRestoreInformationControlBounds(IDialogSettings dialogSettings, boolean restoreLocation, boolean restoreSize) {
		Assert.isTrue(dialogSettings != null && (restoreLocation || restoreSize));
		fDialogSettings= dialogSettings;
		fIsRestoringLocation= restoreLocation;
		fIsRestoringSize= restoreSize;
	}

	/**
	 * Sets the anchor used for laying out the information control relative to the
	 * subject control. E.g, using <code>ANCHOR_TOP</code> indicates that the
	 * information control is position above the area for which the information to
	 * be displayed is valid.
	 *
	 * @param anchor the layout anchor
	 */
	public void setAnchor(Anchor anchor) {
		fAnchor= anchor;
	}

	/**
	 * Sets the anchors fallback sequence used to layout the information control if the original
	 * anchor can not be used because the information control would not fit in the display client
	 * area.
	 * <p>
	 * The fallback anchor for a given anchor is the one that comes directly after the given anchor or
	 * is the first one in the sequence if the given anchor is the last one in the sequence.
	 * <p>
	 * </p>
	 * Note: This sequence is ignored if the original anchor is not contained in this list.
	 * </p>
	 *
	 * @param fallbackAnchors the array with the anchor fallback sequence
	 * @see #setAnchor(AbstractInformationControlManager.Anchor)
	 */
	public void setFallbackAnchors(Anchor[] fallbackAnchors) {
		if (fallbackAnchors != null) {
			fFallbackAnchors= new Anchor[fallbackAnchors.length];
			System.arraycopy(fallbackAnchors, 0, fFallbackAnchors, 0, fallbackAnchors.length);
		} else
			fFallbackAnchors= null;
	}

	/**
	 * Sets the temporary custom control creator, overriding this manager's default information control creator.
	 *
	 * @param informationControlCreator the creator, possibly <code>null</code>
	 * @since 3.0
	 */
	protected void setCustomInformationControlCreator(IInformationControlCreator informationControlCreator)  {
		if (informationControlCreator != null && fCustomInformationControlCreator  instanceof IInformationControlCreatorExtension) {
			IInformationControlCreatorExtension extension= (IInformationControlCreatorExtension) fCustomInformationControlCreator;
			if (extension.canReplace(informationControlCreator))
				return;
		}
		fCustomInformationControlCreator= informationControlCreator;
	}

	/**
	 * Tells the manager whether it should set the focus to the information control when made visible.
	 *
	 * @param takesFocus <code>true</code> if information control should take focus when made visible
	 */
	public void takesFocusWhenVisible(boolean takesFocus) {
		fTakesFocusWhenVisible= takesFocus;
	}

	/**
	 * Tells whether the control takes focus when visible.
	 * 
	 * @return <code>true</code> if the control takes focus when visible, <code>false</code>
	 *         otherwise
	 * @since 3.7
	 */
	protected boolean isTakingFocusWhenVisible() {
		return fTakesFocusWhenVisible;
	}

	/**
	 * Handles the disposal of the subject control. By default, the information control
	 * is disposed by calling <code>disposeInformationControl</code>. Subclasses may extend
	 * this method.
	 */
	protected void handleSubjectControlDisposed() {
		disposeInformationControl();
	}

	/**
	 * Installs this manager on the given control. The control is now taking the role of
	 * the subject control. This implementation sets the control also as the information
	 * control closer's subject control and automatically enables this manager.
	 *
	 * @param subjectControl the subject control
	 */
	public void install(Control subjectControl) {
		if (fSubjectControl != null && !fSubjectControl.isDisposed() && fSubjectControlDisposeListener != null)
			fSubjectControl.removeDisposeListener(fSubjectControlDisposeListener);

		fSubjectControl= subjectControl;

		if (fSubjectControl != null)
			fSubjectControl.addDisposeListener(getSubjectControlDisposeListener());

		if (fInformationControlCloser != null)
			fInformationControlCloser.setSubjectControl(subjectControl);

		setEnabled(true);
		fDisposed= false;
	}

	/**
	 * Returns the dispose listener which gets added
	 * to the subject control.
	 *
	 * @return the dispose listener
	 * @since 3.1
	 */
	private DisposeListener getSubjectControlDisposeListener() {
		if (fSubjectControlDisposeListener == null) {
			fSubjectControlDisposeListener= new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					handleSubjectControlDisposed();
				}
			};
		}
		return fSubjectControlDisposeListener;
	}

	/**
	 * Returns the subject control of this manager/information control.
	 *
	 * @return the subject control
	 */
	protected Control getSubjectControl() {
		return fSubjectControl;
	}

	/**
	 * Returns the actual subject area.
	 *
	 * @return the actual subject area
	 */
	protected Rectangle getSubjectArea() {
		return fSubjectArea;
	}

	/**
	 * Sets the enable state of this manager.
	 *
	 * @param enabled the enable state
	 * @deprecated visibility will be changed to protected
	 */
	public void setEnabled(boolean enabled) {
		fEnabled= enabled;
	}

	/**
	 * Returns whether this manager is enabled or not.
	 *
	 * @return <code>true</code> if this manager is enabled otherwise <code>false</code>
	 */
	protected boolean isEnabled() {
		return fEnabled;
	}

	/**
	 * Computes the size constraints of the information control in points based on the
	 * default font of the given subject control as well as the size constraints in character
	 * width.
	 *
	 * @param subjectControl the subject control
	 * @param informationControl the information control whose size constraints are computed
	 * @return the computed size constraints in points
	 */
	protected Point computeSizeConstraints(Control subjectControl, IInformationControl informationControl) {

		if (fSizeConstraints == null) {
			if (informationControl instanceof IInformationControlExtension5) {
				IInformationControlExtension5 iControl5= (IInformationControlExtension5) informationControl;
				fSizeConstraints= iControl5.computeSizeConstraints(fWidthConstraint, fHeightConstraint);
				if (fSizeConstraints != null)
					return Geometry.copy(fSizeConstraints);
			}
			if (subjectControl == null)
				return null;

			GC gc= new GC(subjectControl);
			gc.setFont(subjectControl.getFont());
			int width= gc.getFontMetrics().getAverageCharWidth();
			int height = gc.getFontMetrics().getHeight();
			gc.dispose();

			fSizeConstraints= new Point (fWidthConstraint * width, fHeightConstraint * height);
		}

		return new Point(fSizeConstraints.x, fSizeConstraints.y);
	}

	/**
	 * Computes the size constraints of the information control in points.
	 *
	 * @param subjectControl the subject control
	 * @param subjectArea the subject area
	 * @param informationControl the information control whose size constraints are computed
	 * @return the computed size constraints in points
	 * @since 3.0
	 */
	protected Point computeSizeConstraints(Control subjectControl, Rectangle subjectArea, IInformationControl informationControl) {
		return computeSizeConstraints(subjectControl, informationControl);
	}

	/**
	 * Handles the disposal of the information control. By default, the information
	 * control closer is stopped.
	 */
	protected void handleInformationControlDisposed() {

		storeInformationControlBounds();

		if (fInformationControl instanceof IInformationControlExtension5)
			fSizeConstraints= null;
		fInformationControl= null;
		if (fInformationControlCloser != null) {
			fInformationControlCloser.setInformationControl(null); //XXX: null is against the spec
			fInformationControlCloser.stop();
		}
	}

	/**
	 * Returns the information control. If the information control has not been created yet,
	 * it is automatically created.
	 *
	 * @return the information control
	 */
	protected IInformationControl getInformationControl() {

		if (fDisposed)
			return fInformationControl;

		IInformationControlCreator creator= null;

		if (fCustomInformationControlCreator == null) {
			creator= fInformationControlCreator;
			if (fIsCustomInformationControl && fInformationControl != null) {
				if (fInformationControl instanceof IInformationControlExtension5)
					fSizeConstraints= null;
				fInformationControl.dispose();
				fInformationControl= null;
			}
			fIsCustomInformationControl= false;

		} else  {

			creator= fCustomInformationControlCreator;
			if (creator instanceof IInformationControlCreatorExtension)  {
				IInformationControlCreatorExtension extension= (IInformationControlCreatorExtension) creator;
				if (fInformationControl != null && extension.canReuse(fInformationControl))
					return fInformationControl;
			}
			if (fInformationControl != null)  {
				if (fInformationControl instanceof IInformationControlExtension5)
					fSizeConstraints= null;
				fInformationControl.dispose();
				fInformationControl= null;
			}
			fIsCustomInformationControl= true;
		}

		if (fInformationControl == null) {
			fInformationControl= creator.createInformationControl(fSubjectControl.getShell());
			fInformationControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					handleInformationControlDisposed();
				}
			});

			if (fInformationControlCloser != null)
				fInformationControlCloser.setInformationControl(fInformationControl);
		}

		return fInformationControl;
	}

	/**
	 * Computes the display location of the information control. The location is computed
	 * considering the given subject area, the anchor at the subject area, and the
	 * size of the information control. This method does not care about whether the information
	 * control would be completely visible when placed at the result location.
	 *
	 * @param subjectArea the subject area
	 * @param controlSize the size of the information control
	 * @param anchor the anchor at the subject area
	 * @return the display location of the information control
	 */
	protected Point computeLocation(Rectangle subjectArea, Point controlSize, Anchor anchor) {
		int xShift= 0;
		int yShift= 0;

		switch (anchor.getSWTFlag()) {
			case SWT.CENTER:
				Point subjectControlSize= fSubjectControl.getSize();
				Point location= new Point(subjectControlSize.x / 2, subjectControlSize.y / 2);
				location.x -= (controlSize.x / 2);
				location.y -= (controlSize.y / 2);
				return fSubjectControl.toDisplay(location);
			case SWT.BOTTOM:
				yShift= subjectArea.height + fMarginY;
				break;
			case SWT.RIGHT:
				xShift= fMarginX + subjectArea.width;
				break;
			case SWT.TOP:
				yShift= -controlSize.y - fMarginY;
				break;
			case SWT.LEFT:
				xShift= -controlSize.x - fMarginX;
				break;
		}

		boolean isRTL= fSubjectControl != null && (fSubjectControl.getStyle() & SWT.RIGHT_TO_LEFT) != 0;
		if (isRTL)
			xShift += controlSize.x;

		return  fSubjectControl.toDisplay(new Point(subjectArea.x + xShift, subjectArea.y + yShift));
	}

	/**
	 * Computes the area available for an information control given an anchor and the subject area
	 * within <code>bounds</code>.
	 *
	 * @param subjectArea the subject area
	 * @param bounds the bounds
	 * @param anchor the anchor at the subject area
	 * @return the area available at the given anchor relative to the subject area, confined to the
	 *         monitor's client area
	 * @since 3.3
	 */
	protected Rectangle computeAvailableArea(Rectangle subjectArea, Rectangle bounds, Anchor anchor) {
		Rectangle area;
		switch (anchor.getSWTFlag()) {
			case SWT.CENTER:
				area= bounds;
				break;
			case SWT.BOTTOM:
				int y= subjectArea.y + subjectArea.height + fMarginY;
				area= new Rectangle(bounds.x, y, bounds.width, bounds.y + bounds.height - y);
				break;
			case SWT.RIGHT:
				int x= subjectArea.x + subjectArea.width + fMarginX;
				area= new Rectangle(x, bounds.y, bounds.x + bounds.width - x, bounds.height);
				break;
			case SWT.TOP:
				area= new Rectangle(bounds.x, bounds.y, bounds.width, subjectArea.y - bounds.y - fMarginY);
				break;
			case SWT.LEFT:
				area= new Rectangle(bounds.x, bounds.y, subjectArea.x - bounds.x - fMarginX, bounds.height);
				break;
			default:
				Assert.isLegal(false);
				return null;
		}

		// Don't return negative areas if the subjectArea overlaps with the monitor bounds.
		area.intersect(bounds);
		return area;
	}

	/**
	 * Checks whether a control of the given size at the given location would be completely visible
	 * in the given display area when laid out by using the given anchor. If not, this method tries
	 * to shift the control orthogonal to the direction given by the anchor to make it visible. If possible
	 * it updates the location.<p>
	 * This method returns <code>true</code> if the potentially updated position results in a
	 * completely visible control, or <code>false</code> otherwise.
	 *
	 *
	 * @param location the location of the control
	 * @param size the size of the control
	 * @param displayArea the display area in which the control should be visible
	 * @param anchor anchor for lying out the control
	 * @return <code>true</code>if the updated location is useful
	 */
	protected boolean updateLocation(Point location, Point size, Rectangle displayArea, Anchor anchor) {

		int displayLowerRightX= displayArea.x + displayArea.width;
		int displayLowerRightY= displayArea.y + displayArea.height;
		int lowerRightX= location.x + size.x;
		int lowerRightY= location.y + size.y;

		if (ANCHOR_BOTTOM == anchor || ANCHOR_TOP == anchor) {

			if (ANCHOR_BOTTOM == anchor) {
				if (lowerRightY > displayLowerRightY)
					return false;
			} else {
				if (location.y < displayArea.y)
					return false;
			}

			if (lowerRightX > displayLowerRightX)
				location.x= location.x - (lowerRightX - displayLowerRightX);

			return (location.x >= displayArea.x && location.y >= displayArea.y);

		} else if (ANCHOR_RIGHT == anchor || ANCHOR_LEFT == anchor) {

			if (ANCHOR_RIGHT == anchor) {
				if (lowerRightX > displayLowerRightX)
					return false;
			} else {
				if (location.x < displayArea.x)
					return false;
			}

			if (lowerRightY > displayLowerRightY)
				location.y= location.y - (lowerRightY - displayLowerRightY);

			return (location.x >= displayArea.x && location.y >= displayArea.y);

		} else if (ANCHOR_GLOBAL == anchor) {

			if (lowerRightX > displayLowerRightX)
				location.x= location.x - (lowerRightX - displayLowerRightX);

			if (lowerRightY > displayLowerRightY)
				location.y= location.y - (lowerRightY - displayLowerRightY);

			return (location.x >= displayArea.x && location.y >= displayArea.y);
		}

		return false;
	}

	/**
	 * Returns the next fallback anchor as specified by this manager's
	 * fallback anchor sequence.
	 * <p>
	 * The fallback anchor for the given anchor is the one that comes directly after
	 * the given anchor or is the first one in the sequence if the given anchor is the
	 * last one in the sequence.
	 * </p>
	 * <p>
	 * Note: It is the callers responsibility to prevent an endless loop i.e. to test
	 * whether a given anchor has already been used once.
	 * then
	 * </p>
	 *
	 * @param anchor the current anchor
	 * @return the next fallback anchor or <code>null</code> if no fallback anchor is available
	 */
	protected Anchor getNextFallbackAnchor(Anchor anchor) {

		if (anchor == null || fFallbackAnchors == null)
			return null;

		for (int i= 0; i < fFallbackAnchors.length; i++) {
			if (fFallbackAnchors[i] == anchor)
				return fFallbackAnchors[i + 1 == fFallbackAnchors.length ? 0 : i + 1];
		}

		return null;
	}

	/**
	 * Computes the location of the information control depending on the
	 * subject area and the size of the information control. This method attempts
	 * to find a location at which the information control lies completely in the display's
	 * client area while honoring the manager's default anchor. If this isn't possible using the
	 * default anchor, the fallback anchors are tried out.
	 *
	 * @param subjectArea the information area
	 * @param controlSize the size of the information control
	 * @return the computed location of the information control
	 */
	protected Point computeInformationControlLocation(Rectangle subjectArea, Point controlSize) {
		Rectangle subjectAreaDisplayRelative= Geometry.toDisplay(fSubjectControl, subjectArea);

		Point upperLeft;
		Anchor testAnchor= fAnchor;
		Rectangle bestBounds= null;
		int bestArea= Integer.MIN_VALUE;
		Anchor bestAnchor= null;
		do {

			upperLeft= computeLocation(subjectArea, controlSize, testAnchor);
			Monitor monitor= getClosestMonitor(subjectAreaDisplayRelative, testAnchor);
			if (updateLocation(upperLeft, controlSize, monitor.getClientArea(), testAnchor))
				return upperLeft;

			// compute available area for this anchor and update if better than best
			Rectangle available= computeAvailableArea(subjectAreaDisplayRelative, monitor.getClientArea(), testAnchor);
			Rectangle proposed= new Rectangle(upperLeft.x, upperLeft.y, controlSize.x, controlSize.y);
			available.intersect(proposed);
			int area= available.width * available.height;
			if (area > bestArea) {
				bestArea= area;
				bestBounds= available;
				bestAnchor= testAnchor;
			}

			testAnchor= getNextFallbackAnchor(testAnchor);

		} while (testAnchor != fAnchor && testAnchor != null);

		// no anchor is perfect - select the one with larges area and set the size to not overlap with the subjectArea
		if (bestAnchor != ANCHOR_GLOBAL)
			Geometry.set(controlSize, Geometry.getSize(bestBounds));
		return Geometry.getLocation(bestBounds);
	}

	/**
	 * Gets the closest monitor given an anchor and the subject area.
	 *
	 * @param area the subject area
	 * @param anchor the anchor
	 * @return the monitor closest to the edge of <code>area</code> defined by
	 *         <code>anchor</code>
	 * @since 3.3
	 */
	private Monitor getClosestMonitor(Rectangle area, Anchor anchor) {
		Point center;
		if (ANCHOR_GLOBAL == anchor)
			center= Geometry.centerPoint(area);
		else
			center= Geometry.centerPoint(Geometry.getExtrudedEdge(area, 0, anchor.getSWTFlag()));
		return getClosestMonitor(fSubjectControl.getDisplay(), Geometry.createRectangle(center, new Point(0, 0)));
	}

	/**
	 * Copied from org.eclipse.jface.window.Window. Returns the monitor whose client area contains
	 * the given point. If no monitor contains the point, returns the monitor that is closest to the
	 * point. If this is ever made public, it should be moved into a separate utility class.
	 *
	 * @param display the display to search for monitors
	 * @param rectangle the rectangle to find the closest monitor for (display coordinates)
	 * @return the monitor closest to the given point
	 * @since 3.3
	 */
	private Monitor getClosestMonitor(Display display, Rectangle rectangle) {
		int closest = Integer.MAX_VALUE;

		Point toFind= Geometry.centerPoint(rectangle);
		Monitor[] monitors = display.getMonitors();
		Monitor result = monitors[0];

		for (int idx = 0; idx < monitors.length; idx++) {
			Monitor current = monitors[idx];

			Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(toFind)) {
				return current;
			}

			int distance = Geometry.distanceSquared(Geometry.centerPoint(clientArea), toFind);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}

	/**
	 * Computes information to be displayed as well as the subject area
	 * and initiates that this information is presented in the information control.
	 * This happens only if this controller is enabled.
	 */
	public void showInformation() {
		if (fEnabled)
			doShowInformation();
	}

	/**
	 * Computes information to be displayed as well as the subject area
	 * and initiates that this information is presented in the information control.
	 */
	protected void doShowInformation() {
		fSubjectArea= null;
		fInformation= null;
		computeInformation();
	}

	/**
	 * Presents the information in the information control or hides the information
	 * control if no information should be presented. The information has previously
	 * been set using <code>setInformation</code>.
	 * <p>
	 * This method should only be called from overriding methods or from <code>setInformation</code>.
	 * </p>
	 */
	protected void presentInformation() {
		boolean hasContents= false;
		if (fInformation instanceof String)
			hasContents= ((String)fInformation).trim().length() > 0;
		else
			hasContents= (fInformation != null);

		if (fSubjectArea != null && hasContents)
			internalShowInformationControl(fSubjectArea, fInformation);
		else
			hideInformationControl();
	}

	/**
	 * Opens the information control with the given information and the specified
	 * subject area. It also activates the information control closer.
	 *
	 * @param subjectArea the information area
	 * @param information the information
	 */
	private void internalShowInformationControl(Rectangle subjectArea, Object information) {
		if (this instanceof InformationControlReplacer) {
			((InformationControlReplacer) this).showInformationControl(subjectArea, information);
			return;
		}

		IInformationControl informationControl= getInformationControl();
		if (informationControl != null) {

			Point sizeConstraints= computeSizeConstraints(fSubjectControl, fSubjectArea, informationControl);
			if (informationControl instanceof IInformationControlExtension3) {
				IInformationControlExtension3 iControl3= (IInformationControlExtension3) informationControl;
				Rectangle trim= iControl3.computeTrim();
				sizeConstraints.x += trim.width;
				sizeConstraints.y += trim.height;
			}
			informationControl.setSizeConstraints(sizeConstraints.x, sizeConstraints.y);

			if (informationControl instanceof IInformationControlExtension2)
				((IInformationControlExtension2)informationControl).setInput(information);
			else
				informationControl.setInformation(information.toString());

			if (informationControl instanceof IInformationControlExtension) {
				IInformationControlExtension extension= (IInformationControlExtension)informationControl;
				if (!extension.hasContents())
					return;
			}

			Point size= null;
			Point location= null;
			Rectangle bounds= restoreInformationControlBounds();

			if (bounds != null) {
				if (bounds.x > -1 && bounds.y > -1)
					location= Geometry.getLocation(bounds);

				if (bounds.width > -1 && bounds.height > -1)
					size= Geometry.getSize(bounds);
			}

			if (size == null)
				size= informationControl.computeSizeHint();

			if (fEnforceAsMinimalSize)
				size= Geometry.max(size, sizeConstraints);
			if (fEnforceAsMaximalSize)
				size= Geometry.min(size, sizeConstraints);

			if (location == null)
				location= computeInformationControlLocation(subjectArea, size);

			Rectangle controlBounds= Geometry.createRectangle(location, size);
			cropToClosestMonitor(controlBounds);
			location= Geometry.getLocation(controlBounds);
			size= Geometry.getSize(controlBounds);
			informationControl.setLocation(location);
			informationControl.setSize(size.x, size.y);

			showInformationControl(subjectArea);
		}
	}

	/**
	 * Crops the given bounds such that they lie completely on the closest monitor.
	 *
	 * @param bounds shell bounds to crop
	 * @since 3.4
	 */
	void cropToClosestMonitor(Rectangle bounds) {
		Rectangle monitorBounds= getClosestMonitor(fSubjectControl.getDisplay(), bounds).getClientArea();
		bounds.intersect(monitorBounds);
	}

	/**
	 * Hides the information control and stops the information control closer.
	 */
	protected void hideInformationControl() {
		if (fInformationControl != null) {
			storeInformationControlBounds();
			fInformationControl.setVisible(false);
			if (fInformationControlCloser != null)
				fInformationControlCloser.stop();
		}
		if (canClearDataOnHide()) {
			fSubjectArea= null;
			fInformation= null; // allow garbage collection of potentially large object
		}
	}

	/**
	 * Tells whether internal data can be cleared on hide.
	 * 
	 * @return <code>true</code> if data can be cleared on hide
	 * @see #hideInformationControl()
	 * @since 3.6
	 */
	protected boolean canClearDataOnHide() {
		return true;
	}

	/**
	 * Shows the information control and starts the information control closer.
	 * This method may not be called by clients.
	 *
	 * @param subjectArea the information area
	 */
	protected void showInformationControl(Rectangle subjectArea) {
		fInformationControl.setVisible(true);
		
		if (fInformationControl == null)
			return; // could already be disposed if setVisible(..) runs the display loop
		
		if (fTakesFocusWhenVisible)
			fInformationControl.setFocus();

		if (fInformationControlCloser != null)
			fInformationControlCloser.start(subjectArea);
	}

	/**
	 * Replaces this manager's information control as defined by
	 * the information control replacer.
	 * <strong>Must only be called when {@link #fInformationControl} instanceof {@link IInformationControlExtension3}!</strong>
	 *
	 * @param takeFocus <code>true</code> iff the replacing information control should take focus
	 *
	 * @since 3.4
	 */
	void replaceInformationControl(boolean takeFocus) {
		if (fInformationControlReplacer != null && canReplace(fInformationControl)) {
			IInformationControlExtension3 iControl3= (IInformationControlExtension3) fInformationControl;
			Rectangle b= iControl3.getBounds();
			Rectangle t= iControl3.computeTrim();
			Rectangle contentBounds= new Rectangle(b.x - t.x, b.y - t.y, b.width - t.width, b.height - t.height);
			IInformationControlCreator informationPresenterControlCreator= ((IInformationControlExtension5) fInformationControl).getInformationPresenterControlCreator();
			fInformationControlReplacer.replaceInformationControl(informationPresenterControlCreator, contentBounds, fInformation, fSubjectArea, takeFocus);
		}
		hideInformationControl();
	}

	/**
	 * Disposes this manager's information control.
	 */
	public void disposeInformationControl() {
		if (fInformationControl != null) {
			fInformationControl.dispose();
			handleInformationControlDisposed();
		}
	}

	/**
	 * Disposes this manager and if necessary all dependent parts such as
	 * the information control. For symmetry it first disables this manager.
	 */
	public void dispose() {
		if (!fDisposed) {

			fDisposed= true;

			setEnabled(false);
			disposeInformationControl();

			if (fInformationControlReplacer != null) {
				fInformationControlReplacer.dispose();
				fInformationControlReplacer= null;
			}

			if (fSubjectControl != null && !fSubjectControl.isDisposed() && fSubjectControlDisposeListener != null)
				fSubjectControl.removeDisposeListener(fSubjectControlDisposeListener);
			fSubjectControl= null;
			fSubjectControlDisposeListener= null;

			fIsCustomInformationControl= false;
			fCustomInformationControlCreator= null;
			fInformationControlCreator= null;
			fInformationControlCloser= null;
		}
	}

	// ------ control's size handling dialog settings ------

	/**
	 * Stores the information control's bounds.
	 *
	 * @since 3.0
	 */
	protected void storeInformationControlBounds() {
		if (fDialogSettings == null || fInformationControl == null || !(fIsRestoringLocation || fIsRestoringSize))
			return;

		if (!(fInformationControl instanceof IInformationControlExtension3))
			throw new UnsupportedOperationException();

		boolean controlRestoresSize= ((IInformationControlExtension3)fInformationControl).restoresSize();
		boolean controlRestoresLocation= ((IInformationControlExtension3)fInformationControl).restoresLocation();

		Rectangle bounds= ((IInformationControlExtension3)fInformationControl).getBounds();
		if (bounds == null)
			return;

		if (fIsRestoringSize && controlRestoresSize) {
			fDialogSettings.put(STORE_SIZE_WIDTH, bounds.width);
			fDialogSettings.put(STORE_SIZE_HEIGHT, bounds.height);
		}
		if (fIsRestoringLocation && controlRestoresLocation) {
			fDialogSettings.put(STORE_LOCATION_X, bounds.x);
			fDialogSettings.put(STORE_LOCATION_Y, bounds.y);
		}
	}
	/**
	 * Restores the information control's bounds.
	 *
	 * @return the stored bounds
	 * @since 3.0
	 */
	protected Rectangle restoreInformationControlBounds() {
		if (fDialogSettings == null || !(fIsRestoringLocation || fIsRestoringSize))
			return null;

		if (!(fInformationControl instanceof IInformationControlExtension3))
			throw new UnsupportedOperationException();

		boolean controlRestoresSize= ((IInformationControlExtension3)fInformationControl).restoresSize();
		boolean controlRestoresLocation= ((IInformationControlExtension3)fInformationControl).restoresLocation();

		Rectangle bounds= new Rectangle(-1, -1, -1, -1);

		if (fIsRestoringSize && controlRestoresSize) {
			try {
				bounds.width= fDialogSettings.getInt(STORE_SIZE_WIDTH);
				bounds.height= fDialogSettings.getInt(STORE_SIZE_HEIGHT);
			} catch (NumberFormatException ex) {
				bounds.width= -1;
				bounds.height= -1;
			}
		}

		if (fIsRestoringLocation && controlRestoresLocation) {
			try {
				bounds.x= fDialogSettings.getInt(STORE_LOCATION_X);
				bounds.y= fDialogSettings.getInt(STORE_LOCATION_Y);
			} catch (NumberFormatException ex) {
				bounds.x= -1;
				bounds.y= -1;
			}
		}

		// sanity check
		if (bounds.x == -1 && bounds.y == -1 && bounds.width == -1 && bounds.height == -1)
			return null;

		Rectangle maxBounds= null;
		if (fSubjectControl != null && !fSubjectControl.isDisposed())
			maxBounds= fSubjectControl.getDisplay().getBounds();
		else {
			// fallback
			Display display= Display.getCurrent();
			if (display == null)
				display= Display.getDefault();
			if (display != null && !display.isDisposed())
				maxBounds= display.getBounds();
		}


		if (bounds.width > -1 && bounds.height > -1) {
			if (maxBounds != null) {
				bounds.width= Math.min(bounds.width, maxBounds.width);
				bounds.height= Math.min(bounds.height, maxBounds.height);
			}

			// Enforce an absolute minimal size
			bounds.width= Math.max(bounds.width, 30);
			bounds.height= Math.max(bounds.height, 30);
		}

		if (bounds.x > -1 && bounds.y > -1 && maxBounds != null) {
			bounds.x= Math.max(bounds.x, maxBounds.x);
			bounds.y= Math.max(bounds.y, maxBounds.y);

			if (bounds .width > -1 && bounds.height > -1) {
				bounds.x= Math.min(bounds.x, maxBounds.width - bounds.width);
				bounds.y= Math.min(bounds.y, maxBounds.height - bounds.height);
			}
		}

		return bounds;
	}

	/**
	 * Returns an adapter that gives access to internal methods.
	 * <p>
	 * <strong>Note:</strong> This method is not intended to be referenced or overridden by clients.</p>
	 *
	 * @return the replaceable information control accessor
	 * @since 3.4
	 * @noreference This method is not intended to be referenced by clients.
	 * @nooverride This method is not intended to be re-implemented or extended by clients.
	 */
	public InternalAccessor getInternalAccessor() {
		return new MyInternalAccessor();
	}
}
