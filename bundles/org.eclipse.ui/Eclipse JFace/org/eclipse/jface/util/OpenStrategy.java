package org.eclipse.jface.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
/**
 * Implementation of single-click and double-click strategies.
 * <p>
 * Usage:
 * <pre>
 *	OpenStrategy handler = new OpenStrategy(control);
 *	handler.addOpenListener(new IOpenEventListener() {
 *		public void handleOpen(SelectionEvent e) {
 *			... // code to handle the open event.
 *		}
 *	});
 * </pre>
 * </p>
 */
public class OpenStrategy {
	/** 
	 * Default behavior. Double click to open the item.
	 */
	public static final int DOUBLE_CLICK = 0;
	/** 
	 * Single click will open the item.
	 */
	public static final int SINGLE_CLICK = 1;
	/** 
	 * Hover will select the item.
	 */
	public static final int SELECT_ON_HOVER = 1 << 1;
	/**
	 * Open item when using arrow keys
	 */
	public static final int ARROW_KEYS_OPEN = 1 << 2;
	/** A single click will generate
	 * an open event but key arrows will not do anything.
	 * 
	 * @deprecated
	 */
	public static final int NO_TIMER = SINGLE_CLICK;
	/** A single click will generate an open
	 * event and key arrows will generate an open event after a
	 * small time.
	 * 
	 * @deprecated
	 */
	public static final int FILE_EXPLORER = SINGLE_CLICK | ARROW_KEYS_OPEN;
	/** Pointing to an item will change the selection
	 * and a single click will gererate an open event
	 * 
	 * @deprecated
	 */
	public static final int ACTIVE_DESKTOP = SINGLE_CLICK | SELECT_ON_HOVER;		
	
	// Time used in FILE_EXPLORER and ACTIVE_DESKTOP
	private static final int TIME = 500;
	
	/* SINGLE_CLICK or DOUBLE_CLICK;
	 * In case of SINGLE_CLICK, the bits SELECT_ON_HOVER and ARROW_KEYS_OPEN
	 * my be set as well. */
	private static int CURRENT_METHOD = DOUBLE_CLICK;
	
	private Listener eventHandler;
	
	private ListenerList listeners = new ListenerList(1);
	
	public OpenStrategy(Control control) {
		initializeHandler(control.getDisplay());
		addListener(control);
	}
	/**
	 * Adds an IOpenEventListener to the collection of listeners
	 */
	public void addOpenListener(IOpenEventListener listener) {
		listeners.add(listener);
	}
	/**
	 * Removes an IOpenEventListener to the collection of listeners
	 */
	public void removeOpenListener(IOpenEventListener listener) {
		listeners.remove(listener);
	}
	/**
	 * Returns the current used single/double-click method
	 * 
	 * This method is internal to the framework; it should not be implemented outside
	 * the framework.
	 */
	public static int getOpenMethod() {
		return CURRENT_METHOD;
	}
	/**
	 * Set the current used single/double-click method.
	 * 
	 * This method is internal to the framework; it should not be implemented outside
	 * the framework.
	 */
	public static void setOpenMethod(int method) {
		if(method == DOUBLE_CLICK) {
			CURRENT_METHOD = method;
			return;
		}
		if((method & SINGLE_CLICK) == 0)
			throw new IllegalArgumentException("Invalid open mode"); //$NON-NLS-1$
		if((method & (SINGLE_CLICK | SELECT_ON_HOVER | ARROW_KEYS_OPEN)) == 0)
			throw new IllegalArgumentException("Invalid open mode"); //$NON-NLS-1$
		CURRENT_METHOD = method;
	}
	/**
	 * Return true if editors should be activated when opened.
	 */
	public static boolean activateOnOpen() {
		return getOpenMethod() == DOUBLE_CLICK;
	}
	/*
	 * Adds all needed listener to the control in order to implement
	 * single-click/double-click strategies.
	 */ 
	private void addListener(Control c) {
		c.addListener(SWT.MouseEnter,eventHandler);
		c.addListener(SWT.MouseExit,eventHandler);
		c.addListener(SWT.MouseMove,eventHandler);
		c.addListener(SWT.MouseDown, eventHandler);
		c.addListener(SWT.MouseUp, eventHandler);
		c.addListener(SWT.KeyDown, eventHandler);
		c.addListener(SWT.Selection, eventHandler);
		c.addListener(SWT.DefaultSelection, eventHandler);
	}
	/*
	 * Fire the open event to all listeners
	 */ 
	private void handleOpen(SelectionEvent e) {
		Object l[] = listeners.getListeners();
		for (int i = 0; i < l.length; i++) {
			((IOpenEventListener)l[i]).handleOpen(e);
		}
	}
	
	//Initialize event handler.
	private void initializeHandler(final Display display) {
		eventHandler = new Listener() {
			boolean timerStarted = false;
			Event mouseUpEvent = null;
			Event mouseMoveEvent = null;
			boolean selectionPendent = false;

			boolean keyDown = false;
			final int[] count = new int[1];
			
			long startTime = System.currentTimeMillis();

			public void handleEvent(final Event e) {
				if(e.type == SWT.DefaultSelection) {
						handleOpen(new SelectionEvent(e));
						return;
				} else {
					if(CURRENT_METHOD == DOUBLE_CLICK)
						return;
				}
				
				switch (e.type) {			
					case SWT.MouseEnter:
					case SWT.MouseExit:
						mouseUpEvent = null;
						mouseMoveEvent = null;
						selectionPendent = false;
						break;
					case SWT.MouseMove:
						if((CURRENT_METHOD & SELECT_ON_HOVER) == 0)
							return;
						if(e.stateMask != 0)
							return;
						if(e.widget.getDisplay().getFocusControl() != e.widget)
							return;							
						mouseMoveEvent = e;
						final Runnable runnable[] = new Runnable[1];
						runnable[0] = new Runnable() {
							public void run() {
								long time = System.currentTimeMillis();
								int diff = (int)(time - startTime);
								if(diff <= TIME) {
									display.timerExec(diff * 2 / 3,runnable[0]);
								} else {
									timerStarted = false;
									setSelection(mouseMoveEvent);
								}
							}
						};
						startTime = System.currentTimeMillis();
						if(!timerStarted) {
							timerStarted = true;
							display.timerExec(TIME * 2 / 3,runnable[0]);
						}
						break;
					case SWT.MouseDown :
						keyDown = false;
						break;						
					case SWT.MouseUp:
						mouseMoveEvent = null;
						if((e.button != 1) || ((e.stateMask & ~SWT.BUTTON1) != 0))
							return;
						if(selectionPendent)
							mouseSelectItem(e);
						else
							mouseUpEvent = e;
						break;
					case SWT.KeyDown:
						mouseMoveEvent = null;
						mouseUpEvent = null;
						keyDown = true;
						break;
					case SWT.Selection:
						mouseMoveEvent = null;
						if (mouseUpEvent != null)
							mouseSelectItem(e);
						else
							selectionPendent = true;
						count[0]++;
						if((CURRENT_METHOD & ARROW_KEYS_OPEN) == 0)
							return;
						display.asyncExec(new Runnable() {
							public void run() {
								if (keyDown) {
									display.timerExec(TIME, new Runnable() {
										int id = count[0];
										public void run() {
											if (id == count[0]) {
												handleOpen(new SelectionEvent(e));
											}
										}
									});
								}
							}
						});
						break;
				}
			}

			void mouseSelectItem(Event e) {
				handleOpen(new SelectionEvent(e));
				mouseUpEvent = null;
				selectionPendent = false;
			}
			void setSelection(Event e) {
				if(e == null)
					return;				
				Widget w = e.widget;
				if(w.isDisposed())
					return;
				/*ISSUE: May have to create a interface with method:
				setSelection(Point p) so that user's custom widgets 
				can use this class. If we keep this option. */
				if(w instanceof Tree) {
					Tree tree = (Tree)w;
					TreeItem item = tree.getItem(new Point(e.x,e.y));
					if(item != null)
						tree.setSelection(new TreeItem[]{item});
				} if(w instanceof Table) {
					Table table = (Table)w;
					TableItem item = table.getItem(new Point(e.x,e.y));
					if(item != null)
						table.setSelection(new TableItem[]{item});
				} if(w instanceof TableTree) {
					TableTree table = (TableTree)w;
					TableTreeItem item = table.getItem(new Point(e.x,e.y));
					if(item != null)
						table.setSelection(new TableTreeItem[]{item});
				} 
			}
		};
	}
}
