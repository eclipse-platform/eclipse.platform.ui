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
			
			if (fInformationControl != null)
				fInformationControl.setVisible(false);
			
			if (fSubjectControl != null && !fSubjectControl.isDisposed()) {
				fSubjectControl.removeMouseListener(this);
				fSubjectControl.removeMouseMoveListener(this);
				fSubjectControl.removeMouseTrackListener(this);
				fSubjectControl.removeControlListener(this);
				fSubjectControl.removeKeyListener(this);
			}
			
			setEnabled(true);
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
			stop();
		}
	};
	
	/**
	 * The mouse tracker to be installed on the manager's subject control.
	 */
	class MouseTracker extends MouseTrackAdapter {
		
		/** The radius of the circle in which mouse hover locations are considered equal. */
		private final static int EPSILON= 3;
		
		/**
		 * Returns whether the given event ocurred within a cicle of <code>EPSILON</code>
		 * pixels of the previous mouse hover location. In addition, the location of
		 * the mouse event is remembered as the previous mouse hover location.
		 * 
		 * @param event the event to check
		 * @return <code>false</code> if the event occured too close to the previous location
		 */
		private boolean isPreviousMouseHoverLocation(MouseEvent event) {
			
			boolean tooClose= false;	
			
			if (fHoverEventLocation.x != -1 && fHoverEventLocation.y != -1) {
				tooClose= Math.abs(fHoverEventLocation.x - event.x) <= EPSILON;
				tooClose= tooClose && (Math.abs(fHoverEventLocation.y - event.y) <= EPSILON);
			}
			
			fHoverEventLocation.x= event.x;
			fHoverEventLocation.y= event.y;
			
			return tooClose;
		}		
		
		/*
		 * @see MouseTrackAdapter#mouseHover
		 */
		public void mouseHover(MouseEvent event) {
			if ( !isPreviousMouseHoverLocation(event))
				showInformation();
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
	 * @see AbstractInformationControlManager#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		boolean e= isEnabled();
		super.setEnabled(enabled);
		if (e != isEnabled()) {
			Control c= getSubjectControl();
			if (c != null && !c.isDisposed()) {
				if (isEnabled())
					c.addMouseTrackListener(fMouseTracker);
				else
					c.removeMouseTrackListener(fMouseTracker);
			}
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