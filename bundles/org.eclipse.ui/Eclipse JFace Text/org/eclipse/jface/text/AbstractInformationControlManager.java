package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.util.Assert;


/**
 * Manages the life cycle, visibility, layout, and contents of an <code>IInformationControl</code>.
 * This manager can be installed on and uninstalled from a control, refered to as the subject control, i.e.
 * the one from which the subject of the information to be shown is retrieved. Also a manager can 
 * be enabled or disabled. An installed and enabled manager can be forced to show information in 
 * its information control using <code>showInformation</code>.
 */
abstract public class AbstractInformationControlManager {
	
	/**
	 * Interface of a information control closer. An information control closer
	 * monitors its information control and its subject control and closes
	 * the information control if necessary. <p>
	 * Clients must implement this interface in order to equipe an information
	 * control manager accordingly.
	 */
	public static interface IInformationControlCloser {
		
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
		 * Sets the closer's information control, i.e. the one to close
		 * if necessary. <p>
		 * Must be called before <code>start</code>. May again be called 
		 * between <code>start</code> and <code>stop</code>.
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
	};
	
	
	
	/**
	 * Constitues entities to enumerate ankers for the layout of the information control.
	 */
	public static final class Anker {
		private Anker() {
		};
	};
	
	private final static Anker[] ANKERS= { new Anker(), new Anker(), new Anker(), new Anker() };
	
	/** Anker representing the top of the information area */
	public final static Anker ANKER_TOP=  ANKERS[0];
	/** Anker representing the bottom of the information area */
	public final static Anker ANKER_BOTTOM=  ANKERS[1];
	/** Anker representing the left side of the information area */
	public final static Anker ANKER_LEFT=  ANKERS[2];
	/** Anker representing the right side of the information area */
	public final static Anker ANKER_RIGHT= ANKERS[3];
	
	
	
	/** The subject control of the information control */
	private Control  fSubjectControl;
	
	/** The display area for which the information to be presented is valid */
	private Rectangle fSubjectArea;
	
	/** The information to be presented */
	private String fInformation;
	
	/** The information control */
	private IInformationControl fInformationControl;
	
	/** The information control creator */
	private IInformationControlCreator fInformationControlCreator;
	
	/** The information control closer */
	private IInformationControlCloser fInformationControlCloser;
	
	/** Indicates that the information control has been disposed */
	private boolean fDisposed= false;
	
	/** Indicates the enable state of this manager */
	private boolean fEnabled= false;
		
	/** Cached, computed size constraints of the information control in points */
	private Point fSizeConstraints;
	
	/** The y margin when laying out the information control */
	private int fMarginY= 5;
	
	/** The x margin when laying out the information control */
	private int fMarginX= 5;
	
	/** The width contraint of the information control in characters */
	private int fWidthConstraint= 60;
	
	/** The height constraint of the information control  in characters */
	private int fHeightConstraint= 6;
	
	/** Indicates wether the size constraints should be enforced as minimal control size */
	private boolean fEnforceAsMinimalSize= false;
	
	/** Indicates whether the size constraints should be enforced as maximal control size */
	private boolean fEnforceAsMaximalSize= false;
	
	/** The anker for laying out the information control in relation to the subject control */
	private Anker fAnker= ANKER_BOTTOM;
	
	/** 
	 * A list of ankers used to layout the information control if the original anker can not 
	 * be used because the information control would not fit in the display client area.
	 */
	private Anker[] fFallbackAnkers= ANKERS;
	
	
	/**
	 * Creates a new information control manager using the given information control creator.
	 * By default the following configuration is given:
	 * <ul>
	 * <li> enabled == false
	 * <li> x-margin == 5 points
	 * <li> y-margin == 5 points
	 * <li> width constraint == 60 characters
	 * <li> height constraint == 6 characters
	 * <li> enforce constraints as minimal size == false
	 * <li> enforece constraints as maximal size == false
	 * <li> layout anker == ANKER_BOTTOM
	 * <li> fallback ankers == { ANKER_TOP, ANKER_BOTTOM, ANKER_LEFT, ANKER_RIGHT }
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
	 * information is valid. The two values must be set using <code>setInformation</code>.
	 */
	abstract protected void computeInformation();
	
	/**
	 * Sets the parameters of the information to be displayed. These are the information itself and
	 * the area for which the given information is valid. This so called subject area is a graphical
	 * region of the information control's subject control.
	 * 
	 * @param information the information
	 * @param subjectArea the subject area
	 */
	protected void setInformation(String information, Rectangle subjectArea) {
		fInformation= information;
		fSubjectArea= subjectArea;
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
	 * Sets the x- and y- margin to be used when laying out the information control
	 * relative to the subject control.
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
		fWidthConstraint= widthInChar;
		fHeightConstraint= heightInChar;
		fEnforceAsMinimalSize= enforceAsMinimalSize;
		fEnforceAsMaximalSize= enforceAsMaximalSize;
	}
	
	/**
	 * Sets the anker used for laying out the information control relative to the
	 * subject control. E.g, using <code>ANKER_TOP</code> indicates that the
	 * information control is position above the area for which the information to
	 * be displayed is valid.
	 * 
	 * @param anker the layout anker
	 */
	public void setAnker(Anker anker) {
		fAnker= anker;
	}
	
	/**
	 * Sets the sequence of ankers along which the information control is tried to 
	 * be laid out until it is fully visible. This fallback is initiated when the information
	 * control does not fit into the client area of the subject control's display.
	 * 
	 * @param fallbackAnkers the list of ankers to be tried
	 */
	public void setFallbackAnkers(Anker[] fallbackAnkers) {
		fFallbackAnkers= fallbackAnkers;
	}
	
	/**
	 * Installs the manager on the given control. The control is now taking the role of
	 * the subject control. This implementation sets the control also as the information
	 * control closer's subject control and automatically enables this manager.
	 * 
	 * @param subjectControl the subject control
	 */
	public void install(Control subjectControl) {
		fSubjectControl= subjectControl;
		if (fInformationControlCloser != null)
			fInformationControlCloser.setSubjectControl(subjectControl);
		setEnabled(true);
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
	 * Sets the enable state of this manager.
	 * 
	 * @param enabled the enable state
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
			
			if (subjectControl == null)
				return null;
				
			GC gc= new GC(subjectControl);
			gc.setFont(subjectControl.getFont());
			int width= gc.getFontMetrics().getAverageCharWidth();
			int height = gc.getFontMetrics().getHeight();
			gc.dispose();
			
			fSizeConstraints= new Point (fWidthConstraint * width, fHeightConstraint * height);
		}
		
		return fSizeConstraints;
	}
	
	/**
	 * Handles the disposal of the information control.
	 */
	protected void handleInformationControlDisposed() {
		fInformationControl= null;
		if (fInformationControlCloser != null) {
			fInformationControlCloser.setInformationControl(null);
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
		if (fInformationControl == null && !fDisposed) {
			
			fInformationControl= fInformationControlCreator.createInformationControl(fSubjectControl.getShell());
			
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
	 * considering the given subject area, the anker at the subject area, and the
	 * size of the information control. This method does not care about whether the information
	 * control would be completely visible when placed at the result location.
	 * 
	 * @param subjectArea the subject area
	 * @param controlSize the size of the information control
	 * @param anker the anker at the subject area
	 */
	protected Point computeLocation(Rectangle subjectArea, Point controlSize, Anker anker) {
		
		int xShift= 0;
		int yShift= 0;
				
		if (ANKER_BOTTOM == anker) {
			xShift= fMarginX;
			yShift= subjectArea.height + fMarginY;
		} else if (ANKER_RIGHT == anker) {
			xShift= fMarginX + subjectArea.width;
			yShift= fMarginY;
		} else if (ANKER_TOP == anker) {
			xShift= fMarginX;
			yShift= -controlSize.y - fMarginY;
		} else if (ANKER_LEFT == anker) {
			xShift= -controlSize.x - fMarginX;
			yShift= fMarginY;
		}
		
		return  fSubjectControl.toDisplay(new Point(subjectArea.x + xShift, subjectArea.y + yShift));
	}
	
	/**
	 * Checks whether a control of the given size at the given location would be completely visible
	 * in the given display area when laid out by using the given anker. If not, this method tries 
	 * to shift the control orthogonal to the direction given by the anker to make it visible. If possible
	 * it updates the location.<p>
	 * This method returns <code>true</code> if the potentially updated position results in a
	 * completely visible control, or <code>false</code> otherwise.
	 * 
	 * 
	 * @param location the location of the control
	 * @param size the size of the control
	 * @param displayArea the display area in which the control should be visible
	 * @param anker anker for alying out the control
	 * @return <code>true</code>if the updated location is useful
	 */
	protected boolean updateLocation(Point location, Point size, Rectangle displayArea, Anker anker) {
		
		int displayLowerRightX= displayArea.x + displayArea.width;
		int displayLowerRightY= displayArea.y + displayArea.height;
		int lowerRightX= location.x + size.x;
		int lowerRightY= location.y + size.y;
		
		if (ANKER_BOTTOM == anker || ANKER_TOP == anker) {
			
			if (ANKER_BOTTOM == anker) {
				if (lowerRightY > displayLowerRightY)
					return false;
			} else {
				if (location.y < displayArea.y)
					return false;
			}	
			
			if (lowerRightX > displayLowerRightX)
				location.x= location.x - (lowerRightX - displayLowerRightX);
			return true;
			
		} else if (ANKER_RIGHT == anker || ANKER_LEFT == anker) {
			
			if (ANKER_RIGHT == anker) {
				if (lowerRightX > displayLowerRightX)
					return false;
			} else {
				if (location.x < displayArea.x)
					return false;
			}
				
			if (lowerRightY > displayLowerRightY)
				location.y= location.y - (lowerRightY - displayLowerRightY);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the next fallback anker from this manager's list of fallback ankers.
	 * If no more fallback anker is available <code>null</code> is returned.
	 * 
	 * @param anker the current anker
	 * @return the next fallback anker or <code>null</code> if no more anker is available
	 */
	protected Anker getNextFallbackAnker(Anker anker) {
		
		if (anker == null || fFallbackAnkers == null)
			return null;
			
		for (int i= 0; i < fFallbackAnkers.length; i++) {
			if (fFallbackAnkers[i] == anker) 
				return fFallbackAnkers[i + 1 == fFallbackAnkers.length ? 0 : i + 1];
		}
		
		return null;
	}
	
	/**
	 * Computes the location of the information control depending on the 
	 * subject area and the size of the information control. This method attempts
	 * to find a location at which the information control lies completely in the display's
	 * client area honoring the manager's default anker. If this isn't possible using the
	 * default anker, the fallback ankers are tried out.
	 * 
	 * @param subjectArea the information area
	 * @param controlSize the size of the information control
	 * @return the computed location of the information control
	 */
	protected Point computeInformationControlLocation(Rectangle subjectArea, Point controlSize) {
		
		Rectangle displayBounds= fSubjectControl.getDisplay().getClientArea();
		
		Point upperLeft;
		Anker testAnker= fAnker;		
		do {
			
			upperLeft= computeLocation(subjectArea, controlSize, testAnker);			
			if (updateLocation(upperLeft, controlSize, displayBounds, testAnker))
				break;
			testAnker= getNextFallbackAnker(testAnker);
			
		} while (testAnker != fAnker && testAnker != null);
		
		return upperLeft;
	}
	
	/**
	 * Computes information to be displayed as well as the subject area and
	 * presents this information in the information control.
	 */
	public void showInformation() {
		
		if (!fEnabled)
			return;
				
		fSubjectArea= null;
		fInformation= null;
		
		computeInformation();
		
		if (fSubjectArea != null && fInformation != null && fInformation.trim().length() > 0)
			showInformationControl(fSubjectArea, fInformation);
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
	private void showInformationControl(Rectangle subjectArea, String information) {
	
		IInformationControl hoverControl= getInformationControl();
		if (hoverControl != null) {
			
			Point sizeConstraints= computeSizeConstraints(fSubjectControl, hoverControl);
			hoverControl.setSizeConstraints(sizeConstraints.x, sizeConstraints.y);
			hoverControl.setInformation(information);
			
			Point size= hoverControl.computeSizeHint();
			
			if (fEnforceAsMinimalSize) {
				if (size.x < sizeConstraints.x)
					size.x= sizeConstraints.x;
				if (size.y < sizeConstraints.y)
					size.y= sizeConstraints.y;
			}		
			
			if (fEnforceAsMaximalSize) {
				if (size.x > sizeConstraints.x)
					size.x= sizeConstraints.x;
				if (size.y > sizeConstraints.y)
					size.y= sizeConstraints.y;
			}					
					
			hoverControl.setSize(size.x, size.y);
			
			Point location= computeInformationControlLocation(subjectArea, size);
			hoverControl.setLocation(location);
			
			hoverControl.setVisible(true);
			
			if (fInformationControlCloser != null)
				fInformationControlCloser.start(subjectArea);
		}
	}
	
	/**
	 * Hides the information control and stops the information control closer.
	 */
	protected void hideInformationControl() {
		if (fInformationControl != null) {
			fInformationControl.setVisible(false);
			if (fInformationControlCloser != null)
				fInformationControlCloser.stop();
		}
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
			
			fInformationControlCreator= null;
			fInformationControlCloser= null;
		}
	}
}