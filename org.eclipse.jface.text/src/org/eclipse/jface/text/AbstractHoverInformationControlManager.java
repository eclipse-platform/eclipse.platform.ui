/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.util.Geometry;


/**
 * An information control manager that shows information in response to mouse
 * hover events. The mouse hover events are caught by registering a
 * {@link org.eclipse.swt.events.MouseTrackListener} on the manager's subject
 * control. The manager has by default an information control closer that closes
 * the information control as soon as the mouse pointer leaves the subject area,
 * the user presses a key, or the subject control is resized, moved, or
 * deactivated.
 * <p>
 * When being activated by a mouse hover event, the manager disables itself,
 * until the mouse leaves the subject area. Thus, the manager is usually still
 * disabled, when the information control has already been closed by the closer.
 *
 * @see org.eclipse.swt.events.MouseTrackListener
 * @since 2.0
 */
abstract public class AbstractHoverInformationControlManager extends AbstractInformationControlManager {
	
	private static final boolean DEBUG_ENRICH_ONLY_ON_CLICK= false;

	/**
	 * The number of pixels to blow up the keep-up zone.
	 * @since 3.4
	 */
	static final int KEEP_UP_ZONE_BLOW_UP= 20;

	
	/**
	 * The  information control closer for the hover information. Closes the information control as
	 * soon as the mouse pointer leaves the subject area, a mouse button is pressed, the user presses a key,
	 * or the subject control is resized or moved.
	 */
	class Closer implements IInformationControlCloser, MouseListener, MouseMoveListener, ControlListener, KeyListener, ShellListener, Listener {
		
		// FIXME: if subject control is a Scrollable, should add selection listeners to both scroll bars
		
		/** The closer's subject control */
		private Control fSubjectControl;
		/** The subject area */
		private Rectangle fSubjectArea;
		/** Indicates whether this closer is active */
		private boolean fIsActive= false;
		/**
		 * The cached display.
		 * @since 3.1
		 */
		private Display fDisplay;


		/**
		 * Creates a new information control closer.
		 */
		public Closer() {
		}

		/*
		 * @see IInformationControlCloser#setSubjectControl(Control)
		 */
		public void setSubjectControl(Control control) {
			fSubjectControl= control;
		}

		/*
		 * @see IInformationControlCloser#setHoverControl(IHoverControl)
		 */
		public void setInformationControl(IInformationControl control) {
			// NOTE: we use fInformationControl from the outer class
		}

		/*
		 * @see IInformationControlCloser#start(Rectangle)
		 */
		public void start(Rectangle subjectArea) {

			if (fIsActive) return;
			fIsActive= true;

			fSubjectArea= subjectArea;

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.addMouseListener(this);
				fSubjectControl.addMouseMoveListener(this);
				fSubjectControl.addControlListener(this);
				fSubjectControl.addKeyListener(this);
				
				fSubjectControl.getShell().addShellListener(this);

				fDisplay= fSubjectControl.getDisplay();
				if (!fDisplay.isDisposed()) {
					fDisplay.addFilter(SWT.Show, this);
					fDisplay.addFilter(SWT.MouseMove, this);
					fDisplay.addFilter(SWT.MouseEnter, this);
					fDisplay.addFilter(SWT.MouseExit, this);
					fDisplay.addFilter(SWT.Activate, this);
					fDisplay.addFilter(SWT.MouseWheel, this);
				}
			}
		}

		/*
		 * @see IInformationControlCloser#stop()
		 */
		public void stop() {
			if (!fIsActive)
				return;

			fIsActive= false;

			if (DEBUG)
				System.out.println("AbstractHoverInformationControlManager.Closer stopped"); //$NON-NLS-1$

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeMouseListener(this);
				fSubjectControl.removeMouseMoveListener(this);
				fSubjectControl.removeControlListener(this);
				fSubjectControl.removeKeyListener(this);
				
				fSubjectControl.getShell().removeShellListener(this);
			}

			if (fDisplay != null && !fDisplay.isDisposed()) {
				fDisplay.removeFilter(SWT.Show, this);
				fDisplay.removeFilter(SWT.MouseMove, this);
				fDisplay.removeFilter(SWT.MouseEnter, this);
				fDisplay.removeFilter(SWT.MouseExit, this);
				fDisplay.removeFilter(SWT.Activate, this);
				fDisplay.removeFilter(SWT.MouseWheel, this);
			}
			fDisplay= null;
		}

		/*
		 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseMove(MouseEvent event) {
			if (!hasInformationControlReplacer()) {
				if (!fSubjectArea.contains(event.x, event.y)) {
					hideInformationControl();
				}

			} else if (fInformationControl != null && !fInformationControl.isFocusControl()) {
				if (!inKeepUpZone(event.x, event.y, fSubjectControl, fSubjectArea, true)) {
					hideInformationControl();
				}
			}
		}

		/*
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent event) {
		}

		/*
		 * @see MouseListener#mouseDown(MouseEvent)
		 */
		public void mouseDown(MouseEvent event) {
			hideInformationControl();
		}

		/*
		 * @see MouseListener#mouseDoubleClick(MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent event) {
			hideInformationControl();
		}

		/*
		 * @see ControlListener#controlResized(ControlEvent)
		 */
		public void controlResized(ControlEvent event) {
			hideInformationControl();
		}

		/*
		 * @see ControlListener#controlMoved(ControlEvent)
		 */
		public void controlMoved(ControlEvent event) {
			hideInformationControl();
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
			hideInformationControl();
		}

		/*
		 * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
		 * @since 3.1
		 */
		public void shellActivated(ShellEvent e) {
		}

		/*
		 * @see org.eclipse.swt.events.ShellListener#shellClosed(org.eclipse.swt.events.ShellEvent)
		 * @since 3.1
		 */
		public void shellClosed(ShellEvent e) {
		}

		/*
		 * @see org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt.events.ShellEvent)
		 * @since 3.1
		 */
		public void shellDeactivated(ShellEvent e) {
			if (fInformationControl.isFocusControl()) {
				if (DEBUG)
					System.out.println("not hidden @ AbstractHoverInformationControlManager.Closer.shellDeactivated()"); //$NON-NLS-1$
			} else {
				if (DEBUG)
					System.out.println("hidden @ AbstractHoverInformationControlManager.Closer.shellDeactivated()"); //$NON-NLS-1$
				hideInformationControl();
			}
		}

		/*
		 * @see org.eclipse.swt.events.ShellListener#shellDeiconified(org.eclipse.swt.events.ShellEvent)
		 * @since 3.1
		 */
		public void shellDeiconified(ShellEvent e) {
		}

		/*
		 * @see org.eclipse.swt.events.ShellListener#shellIconified(org.eclipse.swt.events.ShellEvent)
		 * @since 3.1
		 */
		public void shellIconified(ShellEvent e) {
		}

		/*
		 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
		 * @since 3.1
		 */
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Activate:
				case SWT.Show:
				case SWT.MouseWheel:
					// already triggers when replacement shell is SWT.Show ...
					if (!hasInformationControlReplacer() || !isReplaceInProgress())
						hideInformationControl();
					break;

				case SWT.MouseMove:
				case SWT.MouseEnter:
				case SWT.MouseExit:
					// MouseEnter and MouseExit are required as a workaround for SWT bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=49696
					handleMouseMove(event);
					break;
			}
		}

		/**
		 * Handle mouse movement events.
		 * 
		 * @param event the event
		 * @since 3.4
		 */
		private void handleMouseMove(Event event) {
			if (DEBUG_ENRICH_ONLY_ON_CLICK)
				return;
			
			if (!(event.widget instanceof Control))
				return;
			
			Control eventControl= (Control) event.widget;
			
			Point mouseLoc= event.display.map(eventControl, fSubjectControl, event.x, event.y);
			if (!hasInformationControlReplacer() && !fSubjectArea.contains(mouseLoc)) {
				hideInformationControl();
				return;
			}
			
			if (DEBUG)
				System.out.println("event.type= " + event.type); //$NON-NLS-1$
			
			IInformationControl iControl= fInformationControl;
			if (iControl != null && !iControl.isFocusControl() && iControl instanceof IInformationControlExtension3) {
				IInformationControlExtension3 iControl3= (IInformationControlExtension3) iControl;
				Rectangle controlBounds= iControl3.getBounds();
				if (controlBounds != null) {
					Rectangle tooltipBounds= event.display.map(null, eventControl, controlBounds);
					if (tooltipBounds.contains(event.x, event.y)) {
						event.type= SWT.None;
						if (!isReplaceInProgress())
							replaceInformationControl();
						return;
					}
					if (DEBUG) {
						System.out.println("not replaced (out of bounds) @ AbstractHoverInformationControlManager.Closer.handleEvent()"); //$NON-NLS-1$
						System.out.println("x= " + event.x + ", y= " + event.y + ", control: " + controlBounds + ", tooltip: " + tooltipBounds); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
				}

				if (!fSubjectControl.getBounds().contains(mouseLoc)) {
					/*
					 *  Use inKeepUpZone() to make sure it also works when the hover is
					 *  completely outside of the subject control.
					 */
					if (!inKeepUpZone(mouseLoc.x, mouseLoc.y, fSubjectControl, fSubjectArea, true))
						hideInformationControl();
				}

			} else {
				// FIXME: need better understanding of why/if this is needed.
				// panic code - should not happen
				stop();
			}
		}
	}


	/**
	 * To be installed on the manager's subject control.  Serves two different purposes:
	 * <ul>
	 * <li> start function: initiates the computation of the information to be presented. This happens on
	 * 		receipt of a mouse hover event and disables the information control manager,
	 * <li> restart function: tracks mouse move and shell activation event to determine when the information
	 * 		control manager needs to be reactivated.
	 * </ul>
	 */
	class MouseTracker extends ShellAdapter implements MouseTrackListener, MouseMoveListener {

		/** Margin around the original hover event location for computing the hover area. */
		private final static int EPSILON= 3;

		/** The area in which the original hover event occurred. */
		private Rectangle fHoverArea;
		/** The area for which is computed information is valid. */
		private Rectangle fSubjectArea;
		/** The tracker's subject control. */
		private Control fSubjectControl;

		/** Indicates whether the tracker is in restart mode ignoring hover events. */
		private boolean fIsInRestartMode= false;
		/** Indicates whether the tracker is computing the information to be presented. */
		private boolean fIsComputing= false;
		/** Indicates whether the mouse has been lost. */
		private boolean fMouseLostWhileComputing= false;
		/** Indicates whether the subject control's shell has been deactivated. */
		private boolean fShellDeactivatedWhileComputing= false;

		/**
		 * Creates a new mouse tracker.
		 */
		public MouseTracker() {
		}

		/**
		 * Sets this mouse tracker's subject area, the area to be tracked in order
		 * to re-enable the information control manager.
		 *
		 * @param subjectArea the subject area
		 */
		public void setSubjectArea(Rectangle subjectArea) {
			Assert.isNotNull(subjectArea);
			fSubjectArea= subjectArea;
		}

		/**
		 * Starts this mouse tracker. The given control becomes this tracker's subject control.
		 * Installs itself as mouse track listener on the subject control.
		 *
		 * @param subjectControl the subject control
		 */
		public void start(Control subjectControl) {
			fSubjectControl= subjectControl;
			if (fSubjectControl != null && !fSubjectControl.isDisposed())
				fSubjectControl.addMouseTrackListener(this);

			fIsInRestartMode= false;
			fIsComputing= false;
			fMouseLostWhileComputing= false;
			fShellDeactivatedWhileComputing= false;
		}

		/**
		 * Stops this mouse tracker. Removes itself  as mouse track, mouse move, and
		 * shell listener from the subject control.
		 */
		public void stop() {
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeMouseTrackListener(this);
				fSubjectControl.removeMouseMoveListener(this);
				fSubjectControl.getShell().removeShellListener(this);
			}
		}

		/**
		 * Initiates the computation of the information to be presented. Sets the initial hover area
		 * to a small rectangle around the hover event location. Adds mouse move and shell activation listeners
		 * to track whether the computed information is, after completion, useful for presentation and to
		 * implement the restart function.
		 *
		 * @param event the mouse hover event
		 */
		public void mouseHover(MouseEvent event) {
			if (fIsComputing || fIsInRestartMode) {
				if (DEBUG)
					System.out.println("hover cancelled: fIsComputing= " + fIsComputing + ", fIsInRestartMode= " + fIsInRestartMode + " (in AbstractHoverInformationControlManager...mouseHover)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return;
			}
			
			fIsInRestartMode= true;
			fIsComputing= true;
			fMouseLostWhileComputing= false;
			fShellDeactivatedWhileComputing= false;

			fHoverEventStateMask= event.stateMask;
			fHoverEvent= event;
			fHoverArea= new Rectangle(event.x - EPSILON, event.y - EPSILON, 2 * EPSILON, 2 * EPSILON );
			if (fHoverArea.x < 0)
				fHoverArea.x= 0;
			if (fHoverArea.y < 0)
				fHoverArea.y= 0;
			setSubjectArea(fHoverArea);

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.addMouseMoveListener(this);
				fSubjectControl.getShell().addShellListener(this);
			}
			doShowInformation();
		}

		/**
		 * Deactivates this tracker's restart function and enables the information control
		 * manager. Does not have any effect if the tracker is still executing the start function (i.e.
		 * computing the information to be presented.
		 */
		protected void deactivate() {
			if (fIsComputing)
				return;
			
			fIsInRestartMode= false;
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeMouseMoveListener(this);
				fSubjectControl.getShell().removeShellListener(this);
			}
		}

		/*
		 * @see MouseTrackListener#mouseEnter(MouseEvent)
		 */
		public void mouseEnter(MouseEvent e) {
		}

		/*
		 * @see MouseTrackListener#mouseExit(MouseEvent)
		 */
		public void mouseExit(MouseEvent e) {
			fMouseLostWhileComputing= true;
			deactivate();
		}

		/*
		 * @see MouseMoveListener#mouseMove(MouseEvent)
		 */
		public void mouseMove(MouseEvent event) {
			if (!hasInformationControlReplacer()) {
				if (!fSubjectArea.contains(event.x, event.y))
					deactivate();
			} else {
				if (!inKeepUpZone(event.x, event.y, fSubjectControl, fSubjectArea, false))
					deactivate();
			}
		}

		/*
		 * @see ShellListener#shellDeactivated(ShellEvent)
		 */
		public void shellDeactivated(ShellEvent e) {
			fShellDeactivatedWhileComputing= true;
			deactivate();
		}

		/*
		 * @see ShellListener#shellIconified(ShellEvent)
		 */
		public void shellIconified(ShellEvent e) {
			fShellDeactivatedWhileComputing= true;
			deactivate();
		}

		/**
		 * Tells this tracker that the start function processing has been completed.
		 */
		public void computationCompleted() {
			fIsComputing= false;
			fMouseLostWhileComputing= false;
			fShellDeactivatedWhileComputing= false;
		}

		/**
		 * Determines whether the computed information is still useful for presentation.
		 * This is not the case, if the shell of the subject control has been deactivated, the mouse
		 * left the subject control, or the mouse moved on, so that it is no longer in the subject
		 * area.
		 *
		 * @return <code>true</code> if information is still useful for presentation, <code>false</code> otherwise
		 */
		public boolean isMouseLost() {

			if (fMouseLostWhileComputing || fShellDeactivatedWhileComputing)
				return true;

			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				Display display= fSubjectControl.getDisplay();
				Point p= display.getCursorLocation();
				p= fSubjectControl.toControl(p);
				if (!fSubjectArea.contains(p) && !fHoverArea.contains(p))
					return true;
			}

			return false;
		}
	}

	/** The mouse tracker on the subject control */
	private MouseTracker fMouseTracker= new MouseTracker();
	/**
	 * The remembered hover event.
     * @since 3.0
	 */
	private MouseEvent fHoverEvent= null;
	/** The remembered hover event state mask of the keyboard modifiers */
	private int fHoverEventStateMask= 0;

	/**
	 * Creates a new hover information control manager using the given information control creator.
	 * By default a <code>Closer</code> instance is set as this manager's closer.
	 *
	 * @param creator the information control creator
	 */
	protected AbstractHoverInformationControlManager(IInformationControlCreator creator) {
		super(creator);
		setCloser(new Closer());
	}

	/**
	 * Tests whether a given mouse location is within the keep-up zone.
	 * The hover should not be hidden as long as the mouse stays inside this zone.
	 * 
	 * @param x the x coordinate, relative to the <em>subject control</em>
	 * @param y the y coordinate, relative to the <em>subject control</em>
	 * @param subjectControl the subject control
	 * @param subjectArea the area for which the presented information is valid
	 * @param blowUp If <code>true</code>, then calculate for the closer, i.e. blow up the keepUp area.
	 *        If <code>false</code>, then use tight bounds for hover detection.
	 * 
	 * @return <code>true</code> iff the mouse event occurred in the keep-up zone
	 * @since 3.4
	 */
	private boolean inKeepUpZone(int x, int y, Control subjectControl, Rectangle subjectArea, boolean blowUp) {
		if (subjectArea.contains(x, y))
			return true;
		
		IInformationControl iControl= fInformationControl;
		if (iControl instanceof IInformationControlExtension3) {
			// FIXME: Do we have to check whether the info control is visible (and return false in that case)?
			IInformationControlExtension3 iControl3= (IInformationControlExtension3) iControl;
			
			Rectangle iControlBounds= subjectControl.getDisplay().map(null, subjectControl, iControl3.getBounds());
			Rectangle totalBounds= Geometry.copy(iControlBounds);
			if (blowUp && isReplaceInProgress()) {
				//Problem: blown up iControl overlaps rest of subjectArea's line
				// solution for now: only blow up for keep up (closer), but not for further hover detection
				Geometry.expand(totalBounds, KEEP_UP_ZONE_BLOW_UP, KEEP_UP_ZONE_BLOW_UP, KEEP_UP_ZONE_BLOW_UP, KEEP_UP_ZONE_BLOW_UP);
			}
			
			if (!blowUp && (subjectArea.y + subjectArea.height) < iControlBounds.y) {
				// special case for hover events: subjectArea totally above iControl:
				//  +-----------+
				//  |subjectArea|
				//  +-----------+
				//  |also keepUp|
				// ++-----------+-------+
				// | InformationControl |
				// +--------------------+
				if (totalBounds.contains(x, y))
					return true;
				if (subjectArea.y + subjectArea.height <= y && y <= totalBounds.y) {
					// is vertically between subject area and iControl
					// FIXME: cases when subjectArea extends to left or right of iControl
					if (subjectArea.x <= x && x <= subjectArea.x + subjectArea.width) {
						// is below subject area (in a vertical projection)
						return true;
					}
				}
				
			} else {
				// FIXME: should maybe use convex hull, not bounding box
				totalBounds.add(subjectArea);
				if (totalBounds.contains(x, y))
					return true;
			}
		}
		return false;
	}
	
	/*
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#hideInformationControl()
	 */
	protected void hideInformationControl() {
		if (fMouseTracker != null)
			fMouseTracker.deactivate();
		super.hideInformationControl();
	}

	/*
	 * @see org.eclipse.jface.text.AbstractInformationControlManager#presentInformation()
	 */
	protected void presentInformation() {
		if (fMouseTracker == null) {
			super.presentInformation();
			return;
		}

		Rectangle area= getSubjectArea();
		if (area != null)
			fMouseTracker.setSubjectArea(area);

		if (fMouseTracker.isMouseLost()) {
			fMouseTracker.computationCompleted();
			fMouseTracker.deactivate();
		} else {
			fMouseTracker.computationCompleted();
			super.presentInformation();
		}
	}

	/**
	 * {@inheritDoc}
	 * @deprecated visibility will be changed to protected
	 */
	public void setEnabled(boolean enabled) {

		boolean was= isEnabled();
		super.setEnabled(enabled);
		boolean is= isEnabled();

		if (was != is && fMouseTracker != null) {
			if (is)
				fMouseTracker.start(getSubjectControl());
			else
				fMouseTracker.stop();
		}
	}

	/**
	 * Disposes this manager's information control.
	 */
	public void dispose() {
		if (fMouseTracker != null) {
			fMouseTracker.stop();
			fMouseTracker.fSubjectControl= null;
			fMouseTracker= null;
		}
		super.dispose();
	}

	/**
	 * Returns the location at which the most recent mouse hover event
	 * has been issued.
	 *
	 * @return the location of the most recent mouse hover event
	 */
	protected Point getHoverEventLocation() {
		return fHoverEvent != null ? new Point(fHoverEvent.x, fHoverEvent.y) : new Point(-1, -1);
	}

	/**
	 * Returns the most recent mouse hover event.
	 *
	 * @return the most recent mouse hover event or <code>null</code>
	 * @since 3.0
	 */
	protected MouseEvent getHoverEvent() {
		return fHoverEvent;
	}

 	/**
	 * Returns the SWT event state of the most recent mouse hover event.
	 *
	 * @return the SWT event state of the most recent mouse hover event
	 */
	protected int getHoverEventStateMask() {
		return fHoverEventStateMask;
 	}

}
