package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import java.io.*;

/**
 * Controls the drag and drop of an editor or view
 * layout part.
 *
 * @see IPartDropListener
 * @see IPartDropTarget
 * @see PartDropEvent
 * @see LayoutPart
 */
public class PartDragDrop extends Object
	implements MouseMoveListener, MouseListener
{
	// Define the relative position
	public static final int INVALID    = 0;
	public static final int LEFT       = 1;
	public static final int RIGHT      = 2;
	public static final int TOP        = 3;
	public static final int BOTTOM     = 4;
	public static final int CENTER     = 5;
	public static final int OFFSCREEN  = 6;
	
	// Move this many pixels before dragging starts	
	private final static int HYSTERESIS= 10;

	// Define width of part's "hot" border	
	private final static int MARGIN= 10;
	
	private final static Cursor cursors[]= new Cursor[7];

	// Drag source layout part
	private LayoutPart sourcePart;
	// Control which acts as the drag object (could be a titlebar, could be the entire VisualPart)
	private Control dragControl; 
	
	private int xAnchor;
	private int yAnchor;
	private boolean mouseDown = false;

	private IPartDropListener[] dropListeners;
/**
 * Constructs a new drag drop.
 */
public PartDragDrop(LayoutPart dragPart, Control dragHandle) {
	sourcePart = dragPart;
	dragControl = dragHandle;     
	dragControl.addMouseListener(this);
	dragControl.addMouseMoveListener(this);
}
/**	 
 * Adds the listener to receive events.
 * <p>
 *
 * @param listener the listener
 * @see PartDropListener
 */
public void addDropListener(IPartDropListener listener) {

	if (listener == null) return;
	
	if (dropListeners == null) {
		dropListeners = new IPartDropListener[1];
		dropListeners[0] = listener;
	} else {
		IPartDropListener[] newDropListeners = new IPartDropListener[dropListeners.length + 1];
		System.arraycopy(dropListeners, 0, newDropListeners, 0, dropListeners.length);
		newDropListeners[dropListeners.length] = listener;
		dropListeners = newDropListeners;
	}
}
/**
 * Returns a drag event representing the current state of dragging.
 */
private PartDropEvent createDropEvent(Tracker tracker) {
	
	Display display= dragControl.getDisplay();
	Control targetControl= display.getCursorControl();
	
	PartDropEvent event = new PartDropEvent();

	Rectangle rect= tracker.getRectangles()[0];
	event.x = rect.x;
	event.y = rect.y;
	event.dragSource = sourcePart;
	
	if (targetControl == null) {
		// cursor is outside of the shell
		event.relativePosition = OFFSCREEN;
		return event;
	}
	
	LayoutPart targetPart = getTargetPart(targetControl);
	if (targetPart == null) {
		event.relativePosition = INVALID;
		return event;
	}

	event.dropTarget = targetPart;
	Control c = targetPart.getControl();
	if (c == null) {
		event.relativePosition = INVALID;
		return event;
	}
	
	Point p = c.toControl(display.getCursorLocation());
	Point e = c.getSize();
	
	// first determine whether mouse position is in center of part
	int hmargin= Math.min(e.x/3, MARGIN);
	int vmargin= Math.min(e.y/3, MARGIN);
		
	Rectangle inner= new Rectangle(hmargin, vmargin, e.x-(hmargin*2), e.y-(vmargin*2));	
	if (inner.contains(p)) {
		event.relativePosition = CENTER;
	} else {
		// normalize to center
		p.x-= e.x/2;
		p.y-= e.y/2;
		
		// now determine quadrant
		double a= Math.atan2(p.y*e.x, p.x*e.y) * (180/Math.PI);

		if (a >= -135 && a < -45)
			event.relativePosition = TOP;
		else if (a > -45 && a < 45)
			event.relativePosition = RIGHT;
		else if (a > 45 && a < 135)
			event.relativePosition = BOTTOM;
		else
			event.relativePosition = LEFT;		
	}
	return event;
}
/**
 * Dispose of the drag drop.
 */
public void dispose() {
	// Get rid of control.
	if (dragControl != null && !dragControl.isDisposed()){
		dragControl.removeMouseMoveListener(this);
		dragControl.removeMouseListener(this);
	}
	dragControl = null;

	// Get rid of cursors.
	for (int i = 0, length = cursors.length; i < length; i++){
		if (cursors[i] != null && !cursors[i].isDisposed())
			cursors[i].dispose();
		cursors[i] = null;
	}

	// Deref all else.
	dropListeners = null;
	sourcePart = null;
}
/**
 * Return the cursor for a drop scenario, as identified by code.
 * Code must be one of INVALID, LEFT, RIGHT, TOP, etc.
 * If the code is not found default to INVALID.
 */
private Cursor getCursor(Display display, int code) {
	if (cursors[code] == null) {
		ImageDescriptor source = null;
		ImageDescriptor mask = null;
		switch (code) {
			case LEFT:
				source = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_LEFT_SOURCE);
				mask = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_LEFT_MASK);
				cursors[LEFT]= new Cursor(display, source.getImageData(), mask.getImageData(), 16, 16);
				break;
			case RIGHT:
				source = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_RIGHT_SOURCE);
				mask = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_RIGHT_MASK);
				cursors[RIGHT]= new Cursor(display, source.getImageData(), mask.getImageData(), 16, 16);
				break;
			case TOP:
				source = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOP_SOURCE);
				mask = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_TOP_MASK);
				cursors[TOP]= new Cursor(display, source.getImageData(), mask.getImageData(), 16, 16);
				break;
			case BOTTOM:
				source = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_BOTTOM_SOURCE);
				mask = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_BOTTOM_MASK);
				cursors[BOTTOM]= new Cursor(display, source.getImageData(), mask.getImageData(), 16, 16);
				break;
			case CENTER:
				source = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_STACK_SOURCE);
				mask = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_STACK_MASK);
				cursors[CENTER]= new Cursor(display, source.getImageData(), mask.getImageData(), 16, 16);
				break;
			case OFFSCREEN:
				source = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_OFFSCREEN_SOURCE);
				mask = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_OFFSCREEN_MASK);
				cursors[OFFSCREEN]= new Cursor(display, source.getImageData(), mask.getImageData(), 16, 16);
				break;
			default:
			case INVALID:
				source = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_INVALID_SOURCE);
				mask = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJS_DND_INVALID_MASK);
				cursors[INVALID]= new Cursor(display, source.getImageData(), mask.getImageData(), 16, 16);
				break;
		}
	}
	return cursors[code];
}
/**
 * Returns the drag handle.
 */
protected Control getDragControl() {
	return dragControl;
}
/**
 * Returns the source's bounds
 */
protected Rectangle getSourceBounds() {
	return sourcePart.getControl().getBounds();
}
/**
 * Returns the drag source part.
 */
public LayoutPart getSourcePart() {
	return sourcePart;
}
/**
 * Returns the target part containing a particular control.  If the
 * target part is not in the same window as the source part return null.
 */
private LayoutPart getTargetPart(Control target) {
	while (target != null) {
		Object data= target.getData();
		if (data instanceof IPartDropTarget)
			return ((IPartDropTarget)data).targetPartFor(sourcePart);
		target = target.getParent();
	}
	return null;
}
/**
 * Returns whether the mouse has moved enough to warrant
 * opening a tracker.
 */
protected boolean hasMovedEnough(MouseEvent e) {
	int dx= e.x - xAnchor;
	int dy= e.y - yAnchor;
	if (Math.abs(dx) < HYSTERESIS && Math.abs(dy) < HYSTERESIS)
		return false;
	else
		return true;	
}
/**
 * @see MouseListener::mouseDoubleClick
 */
public void mouseDoubleClick(MouseEvent e) {
	mouseDown = false;
}
/**
 * @see MouseListener::mouseDown
 */
public void mouseDown(MouseEvent e) {
	// track left button only.
	if (e.button != 1) 
		return;
	if (getSourceBounds().width == 0 || getSourceBounds().height == 0)
		return;
	if (!sourcePart.isDragAllowed(new Point(e.x,e.y)))
		return;
	// remember anchor position for hysteresis in mouseMove
	xAnchor = e.x;
	yAnchor = e.y;

	mouseDown = true;
}
/**
 * @see MouseMoveListener::mouseMove
 */
public void mouseMove(MouseEvent e) {
	
	// If the mouse is not down or the mouse has moved only a small amount
	// ignore the move.
	// Bug 9004: If a previous MouseDown event caused a dialog to open, 
	// the PartDragDrop will not be notified of the MouseUp event and the 
	// mouseDown flag will not be reset. The fix is to check and make sure 
	// that the mouse button is still pressed.
	// Can not use a focus listener since the dragControl may not actually 
	// receive focus on a MouseDown.
	if (!mouseDown || (e.stateMask & SWT.BUTTON1) == 0)
		return;
	if (!hasMovedEnough(e))
		return;

	// If the source part is not in a state to allow drag & drop
	// operation to start, ignore the move
	if (!sourcePart.isDragAllowed(new Point(e.x,e.y)))
		return;
		
	openTracker();
}
/**
 * Open a tracker (a XOR rect on the screen) change
 * the cursor indicanting where the part will be dropped 
 * and notify the drag listeners.
 */
public void openTracker() {
	// Create a tracker.  This is just an XOR rect on the screen.
	// As it moves we notify the drag listeners.
	final Display display= dragControl.getDisplay();	 						 	
	final Tracker tracker= new Tracker(display, SWT.NULL);
	tracker.addListener(SWT.Move, new Listener() {
		public void handleEvent(Event event) {
			PartDropEvent dropEvent = createDropEvent(tracker);
			// 1GBXIEO: SWT:ALL - DCR: Include cursor pos in Tracker move event
			// Until support is provided, just get the current
			// location (which could be different than when the event occured
			// if the user moves the mouse quickly!)
			Point p = dragControl.toControl(display.getCursorLocation());
			dropEvent.cursorX = p.x;
			dropEvent.cursorY = p.y;
			if (dropListeners != null) {
				for(int i = 0, length = dropListeners.length; i < length; i++) {
					dropListeners[i].dragOver(dropEvent);
				}
			}
			Cursor cursor = getCursor(display, dropEvent.relativePosition);
			tracker.setCursor(cursor);
		}
	});

	// Create a drag rect.
	Control sourceControl = sourcePart.getControl();
	Rectangle sourceBounds = getSourceBounds();
	Point sourcePos = new Point(sourceBounds.x, sourceBounds.y);
	if (!(sourceControl instanceof Shell)) {
		sourcePos = sourceControl.getParent().toDisplay(sourcePos);
	}	
	if(mouseDown) {
		Point anchorPos = dragControl.toDisplay(new Point(xAnchor, yAnchor));
		Point cursorPos = display.getCursorLocation();
		sourceBounds.x = sourcePos.x - (anchorPos.x - cursorPos.x);
		sourceBounds.y = sourcePos.y - (anchorPos.y - cursorPos.y);
	} else {
		sourceBounds.x = sourcePos.x + HYSTERESIS;
		sourceBounds.y = sourcePos.y + HYSTERESIS;
	}
	
	tracker.setRectangles(new Rectangle[] {sourceBounds});
		
	// Run tracker until mouse up occurs or escape key pressed.
	boolean trackingOk = tracker.open();
	mouseDown = false;
		
	// Generate drop event.  
	PartDropEvent event = createDropEvent(tracker);
	// 1GBXIEO: SWT:ALL - DCR: Include cursor pos in Tracker move event
	// Until support is provided, just get the current
	// location (which could be different than when the event occured
	// if the user moves the mouse quickly!)
	Point p1 = dragControl.toControl(display.getCursorLocation());
	event.cursorX = p1.x;
	event.cursorY = p1.y;
	if (!dragControl.isDisposed())
		dragControl.setCursor(null);
	if (dropListeners != null) {
		if (trackingOk) {
			for(int i = 0, length = dropListeners.length; i < length; i++) {
				dropListeners[i].dragOver(event);
			}
		} else {
			event.relativePosition = INVALID;
		}
		
		for(int i = 0, length = dropListeners.length; i < length; i++) {
			dropListeners[i].drop(event);
		}
	}

	// Cleanup.
	tracker.dispose();
}
/**
 * @see MouseListener::mouseUp
 */
public void mouseUp(MouseEvent e) {
	mouseDown = false;
}
/**	 
 * Removes the listener.
 * <p>
 *
 * @param listener the listener
 * @see PartDropListener
 */
public void removeDropListener(IPartDropListener listener) {
	if (listener == null) 
		return;

	int index = -1;
	for (int i = 0, length = dropListeners.length; i < length; i++){
		if (dropListeners[i].equals(listener)){
			index = i;
			break;
		}
	}
	if (index == -1) return;

	if (dropListeners.length == 1) {
		dropListeners = null;
	} else {
		IPartDropListener[] newListeners = new IPartDropListener[dropListeners.length - 1];
		System.arraycopy(dropListeners, 0, newListeners, 0, index);
		System.arraycopy(dropListeners, index+1, newListeners, index, newListeners.length - index);
		dropListeners = newListeners;
	}
}
}
