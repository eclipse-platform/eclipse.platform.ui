package org.eclipse.jface.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;
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
	/* A single click will generate
	 * an open event but key arrows will not do anything.*/
	public static final int NO_TIMER = 1;
	/* A single click will generate an open
	 * event and key arrows will generate an open event after a
	 * small time. */
	public static final int FILE_EXPLORER = 2;
	/* Pointing to an item will change the selection
	 * and a single click will gererate an open event */
	public static final int ACTIVE_DESKTOP = 3;
	/* Default SWT behavior */
	public static final int DOUBLE_CLICK = 4;
	
	// Time used in FILE_EXPLORER and ACTIVE_DESKTOP
	private static final int TIME = 500;
	
	// NO_TIMER, FILE_EXPLORER, ACTIVE_DESKTOP, DOUBLE_CLICK.
	private static int CURRENT_METHOD = DOUBLE_CLICK;
	
	private Listener noTimerHandler;
	private Listener fileExplorerHandler;
	private Listener activeDesktopHandler;
	private Listener doubleClickHandler;
	
	private ListenerList listeners = new ListenerList(1);
	
	public OpenStrategy(Control control) {
		initializeNoTimerHandler();
		initializeFileExplorerHandler(control.getDisplay());
		initializeActiveDesktopHandler(control.getDisplay());
		initializeDoubleClickHandler();
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
		switch (method) {
			case NO_TIMER:
			case FILE_EXPLORER:
			case ACTIVE_DESKTOP:
			case DOUBLE_CLICK:
				CURRENT_METHOD = method;
				return;
		}
	}
	/*
	 * Adds all needed listener to the control in order to implement
	 * single-click/double-click strategies.
	 */ 
	private void addListener(Control c) {
		Listener wrapper = new Listener() {			
			public void handleEvent (Event event) {
				switch (CURRENT_METHOD) {
					case NO_TIMER:
						noTimerHandler.handleEvent(event);
						break;
					case FILE_EXPLORER:
						fileExplorerHandler.handleEvent(event);
						break;
					case ACTIVE_DESKTOP:
						activeDesktopHandler.handleEvent(event);
						break;
					case DOUBLE_CLICK:
						doubleClickHandler.handleEvent(event);
						break;
				}
			}
		};
		c.addListener(SWT.MouseEnter,wrapper);
		c.addListener(SWT.MouseExit,wrapper);
		c.addListener(SWT.MouseMove,wrapper);
		c.addListener(SWT.MouseDown, wrapper);
		c.addListener(SWT.KeyDown, wrapper);
		c.addListener(SWT.Selection, wrapper);
		c.addListener(SWT.DefaultSelection, wrapper);
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
	//Initialize no timer handler.
	private void initializeNoTimerHandler() {
		noTimerHandler = new Listener() {
			Event mouseDownEvent = null;
			boolean selectionPendent = false;
			
			public void handleEvent(Event e) {
				switch (e.type) {
					case SWT.MouseEnter:
					case SWT.MouseExit:
						mouseDownEvent = null;
						selectionPendent = false;
						break;
					case SWT.MouseDown:
						if(e.button != 1 || e.stateMask != 0)
							return;
						if(selectionPendent)
							mouseSelectItem(e);
						else
							mouseDownEvent = e;
						break;
					case SWT.Selection:
						if (mouseDownEvent != null)
							mouseSelectItem(e);
						else
							selectionPendent = true;
						break;
					case SWT.DefaultSelection:
						handleOpen(new SelectionEvent(e));
						break;
				}
			}
			void mouseSelectItem(Event e) {
				handleOpen(new SelectionEvent(e));
				mouseDownEvent = null;
				selectionPendent = false;
			}
		};
	}	
	//Initialize file explorer handler.	
	private void initializeFileExplorerHandler(final Display display) {
		fileExplorerHandler = new Listener() {
			boolean keyDown = false;
			final int[] count = new int[1];
			public void handleEvent(final Event e) {
				switch (e.type) {
					case SWT.KeyDown :
						keyDown = true;
						break;
					case SWT.MouseDown :
						keyDown = false;
						break;
					case SWT.Selection :
						count[0]++;
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
								} else {
									handleOpen(new SelectionEvent(e));
								}
							}
						});
						break;
				}
			}
		};
	}	
	//Initialize active desktop handler.
	private void initializeActiveDesktopHandler(final Display display) {
		activeDesktopHandler = new Listener() {
			boolean timerStarted = false;
			Event mouseDownEvent = null;
			Event mouseMoveEvent = null;
			boolean selectionPendent = false;
			
			long startTime = System.currentTimeMillis();
			final int[] count = new int[1];
			
			public void handleEvent(Event e) {
				switch (e.type) {
					case SWT.MouseEnter:
					case SWT.MouseExit:
						mouseDownEvent = null;
						selectionPendent = false;
						break;
					case SWT.MouseMove:
						if(e.stateMask != 0)
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
									setSelection(mouseMoveEvent,mouseMoveEvent.widget);
								}
							}
						};
						startTime = System.currentTimeMillis();
						if(!timerStarted) {
							timerStarted = true;
							display.timerExec(TIME * 2 / 3,runnable[0]);
						}
						break;
					case SWT.MouseDown:
						if(e.button != 1 || e.stateMask != 0)
							return;
						if(selectionPendent)
							mouseSelectItem(e);
						else
							mouseDownEvent = e;
						break;
					case SWT.KeyDown:
						mouseDownEvent = null;
						break;
					case SWT.Selection:
						if (mouseDownEvent != null)
							mouseSelectItem(e);
						else
							selectionPendent = true;
						break;
					case SWT.DefaultSelection:
						handleOpen(new SelectionEvent(e));
						break;
				}
			}

			void mouseSelectItem(Event e) {
				handleOpen(new SelectionEvent(e));
				mouseDownEvent = null;
				selectionPendent = false;
			}
			void setSelection(Event e,Widget w) {
				if(w.isDisposed())
					return;
				if(w instanceof Tree) {
					Tree tree = (Tree)w;
					TreeItem item = tree.getItem(new Point(e.x,e.y));
					if(item != null)
						tree.setSelection(new TreeItem[]{item});
				}
			}
		};
	}
	//Initialize double click handler.
	private void initializeDoubleClickHandler() {
		doubleClickHandler = new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
					case SWT.DefaultSelection:
						handleOpen(new SelectionEvent(e));
				}
			}
		};
	}	
}
