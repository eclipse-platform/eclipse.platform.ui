package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.util.Assert;


/**
 * An information control manager that shows information on mouse hover events.
 * The mouse hover events are caught by registering a <code>MouseTrackListener</code>
 * on the manager's subject control. The manager has by default an information control closer
 * that closes the information control as soon as the mouse pointer leaves the 
 * subject area.
 */
abstract public class AbstractHoverInformationControlManager extends AbstractInformationControlManager {		
	
	
	/**
	 * The  information control closer for the hover information.
	 */
	class Closer extends MouseTrackAdapter 
		implements IInformationControlCloser, MouseListener, MouseMoveListener, ControlListener, KeyListener {
		
		/** The closer's subject control */
		private Control fSubjectControl;
		/** The closer's information control */
		private IInformationControl fInformationControl;
		/** The subject area */
		private Rectangle fSubjectArea;
		/** Indicates whether this closer is active */
		private boolean fIsActive= false;
		
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
			fInformationControl= control;
		}
		
		/*
		 * @see IInformationControlCloser#start(Rectangle)
		 */
		public void start(Rectangle subjectArea) {
			
			if (fIsActive)
				return;
			fIsActive= true;
			
			fSubjectArea= subjectArea;
			
			setEnabled(false);
			
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.addMouseListener(this);
				fSubjectControl.addMouseMoveListener(this);
				fSubjectControl.addMouseTrackListener(this);
				fSubjectControl.addControlListener(this);
				fSubjectControl.addKeyListener(this);
			}
		}
		
		/*
		 * @see IInformationControlCloser#stop()
		 */
		public void stop() {
			stop(false);
		}
		
		/**
		 * Stops the information control and if <code>delayRestart</code> is set
		 * allows restart only after a certain delay.
		 */
		protected void stop(boolean delayRestart) {
			
			if (!fIsActive)
				return;
			fIsActive= false;
			
			hideInformationControl();
			
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeMouseListener(this);
				fSubjectControl.removeMouseMoveListener(this);
				fSubjectControl.removeMouseTrackListener(this);
				fSubjectControl.removeControlListener(this);
				fSubjectControl.removeKeyListener(this);
			}			
		}
		
		/*
		 * @see MouseMoveListener#mouseMove
		 */
		public void mouseMove(MouseEvent event) {
			if (!fSubjectArea.contains(event.x, event.y))
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
			stop(true);
		}
	};
	
	
	/**
	 * The mouse tracker to be installed on the manager's subject control.
	 */
	class MouseTracker extends MouseTrackAdapter implements MouseMoveListener {
		
		private final static int EPSILON= 3;
		
		private Rectangle fSubjectArea;
		private Control fSubjectControl;
		
		public MouseTracker() {
		}
				
		public void setSubjectArea(Rectangle subjectArea) {
			Assert.isNotNull(subjectArea);
			fSubjectArea= subjectArea;
		}
		
		public void start(Control subjectControl) {
			fSubjectControl= subjectControl;
			if (fSubjectControl != null && !fSubjectControl.isDisposed())
				fSubjectControl.addMouseTrackListener(this);
		}
		
		public void stop() {
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeMouseTrackListener(this);
				fSubjectControl.removeMouseMoveListener(this);
			}
		}
		
		/*
		 * @see MouseTrackAdapter#mouseHover
		 */
		public void mouseHover(MouseEvent event) {
			setEnabled(false);
			
			fHoverEventLocation= new Point(event.x, event.y);
			
			Rectangle r= new Rectangle(event.x - EPSILON, event.y - EPSILON, 2 * EPSILON, 2 * EPSILON );
			if (r.x < 0) r.x= 0;
			if (r.y < 0) r.y= 0;
			setSubjectArea(r);
			
			if (fSubjectControl != null && !fSubjectControl.isDisposed())
				fSubjectControl.addMouseMoveListener(this);
			
			doShowInformation();
		}
		
		/*
		 * @see MouseMoveListener#mouseMove(MouseEvent)
		 */
		public void mouseMove(MouseEvent event) {
			if (!fSubjectArea.contains(event.x, event.y))  {
				fSubjectControl.removeMouseMoveListener(this);
				setEnabled(true);
			}
		}
	};
		
	/** The mouse tracker on the subject control */
	private MouseTracker fMouseTracker= new MouseTracker();
	
	/** The remembered hover event location */
	private Point fHoverEventLocation= new Point(-1, -1);
	
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
	
	/*
	 * @see AbstractInformationControlManager#presentInformation()
	 */
	protected void presentInformation() {
		fMouseTracker.setSubjectArea(getSubjectArea());
		super.presentInformation();
	}
	
	/*
	 * @see AbstractInformationControlManager#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		
		boolean was= isEnabled();
		super.setEnabled(enabled);
		boolean is= isEnabled();
		
		if (was != is) {
			if (is)
				fMouseTracker.start(getSubjectControl());
			else
				fMouseTracker.stop();
		}
	}
	
	/**
	 * Returns the location at which the most recent mouse hover event
	 * has been issued.
	 * 
	 * @return the location of the most recent mouse hover event
	 */
	protected Point getHoverEventLocation() {
		return fHoverEventLocation;
	}
}