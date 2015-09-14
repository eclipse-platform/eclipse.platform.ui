/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.custom.TableTreeItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.core.runtime.ListenerList;

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
    @Deprecated
	public static final int NO_TIMER = SINGLE_CLICK;

    /** A single click will generate an open
     * event and key arrows will generate an open event after a
     * small time.
     *
     * @deprecated
     */
    @Deprecated
	public static final int FILE_EXPLORER = SINGLE_CLICK | ARROW_KEYS_OPEN;

    /** Pointing to an item will change the selection
     * and a single click will gererate an open event
     *
     * @deprecated
     */
    @Deprecated
	public static final int ACTIVE_DESKTOP = SINGLE_CLICK | SELECT_ON_HOVER;

    // Time used in FILE_EXPLORER and ACTIVE_DESKTOP
    // Not declared final, see bug 246209
    private static int TIME = 500;

	/**
	 * Returns the delay for post selection events.
	 *
	 * @return the delay for post selection events in milliseconds
	 * @since 3.7
	 */
	public static final int getPostSelectionDelay() {
		return TIME;
	}

    /* SINGLE_CLICK or DOUBLE_CLICK;
     * In case of SINGLE_CLICK, the bits SELECT_ON_HOVER and ARROW_KEYS_OPEN
     * my be set as well. */
    private static int CURRENT_METHOD = DOUBLE_CLICK;

    private Listener eventHandler;

    private ListenerList openEventListeners = new ListenerList();

    private ListenerList selectionEventListeners = new ListenerList();

    private ListenerList postSelectionEventListeners = new ListenerList();

    /**
     * @param control the control the strategy is applied to
     */
    public OpenStrategy(Control control) {
        initializeHandler(control.getDisplay());
        addListener(control);
    }

    /**
     * Adds an IOpenEventListener to the collection of openEventListeners
     * @param listener the listener to add
     */
    public void addOpenListener(IOpenEventListener listener) {
        openEventListeners.add(listener);
    }

    /**
     * Removes an IOpenEventListener to the collection of openEventListeners
     * @param listener the listener to remove
     */
    public void removeOpenListener(IOpenEventListener listener) {
        openEventListeners.remove(listener);
    }

    /**
     * Adds an SelectionListener to the collection of selectionEventListeners
     * @param listener the listener to add
     */
    public void addSelectionListener(SelectionListener listener) {
        selectionEventListeners.add(listener);
    }

    /**
     * Removes an SelectionListener to the collection of selectionEventListeners
     * @param listener the listener to remove
     */
    public void removeSelectionListener(SelectionListener listener) {
        selectionEventListeners.remove(listener);
    }

    /**
     * Adds an SelectionListener to the collection of selectionEventListeners
     * @param listener the listener to add
     */
    public void addPostSelectionListener(SelectionListener listener) {
        postSelectionEventListeners.add(listener);
    }

    /**
     * Removes an SelectionListener to the collection of selectionEventListeners
     * @param listener the listener to remove
     */
    public void removePostSelectionListener(SelectionListener listener) {
        postSelectionEventListeners.remove(listener);
    }

    /**
     * This method is internal to the framework; it should not be implemented outside
     * the framework.
     * @return the current used single/double-click method
     *
     */
    public static int getOpenMethod() {
        return CURRENT_METHOD;
    }

    /**
     * Set the current used single/double-click method.
     *
     * This method is internal to the framework; it should not be implemented outside
     * the framework.
     * @param method the method to be used
     * @see OpenStrategy#DOUBLE_CLICK
     * @see OpenStrategy#SINGLE_CLICK
     * @see OpenStrategy#SELECT_ON_HOVER
     * @see OpenStrategy#ARROW_KEYS_OPEN
     */
    public static void setOpenMethod(int method) {
        if (method == DOUBLE_CLICK) {
            CURRENT_METHOD = method;
            return;
        }
        if ((method & SINGLE_CLICK) == 0) {
			throw new IllegalArgumentException("Invalid open mode"); //$NON-NLS-1$
		}
        if ((method & (SINGLE_CLICK | SELECT_ON_HOVER | ARROW_KEYS_OPEN)) == 0) {
			throw new IllegalArgumentException("Invalid open mode"); //$NON-NLS-1$
		}
        CURRENT_METHOD = method;
    }

    /**
     * @return true if editors should be activated when opened.
     */
    public static boolean activateOnOpen() {
        return getOpenMethod() == DOUBLE_CLICK;
    }

    /*
     * Adds all needed listener to the control in order to implement
     * single-click/double-click strategies.
     */
    private void addListener(Control c) {
        c.addListener(SWT.MouseEnter, eventHandler);
        c.addListener(SWT.MouseExit, eventHandler);
        c.addListener(SWT.MouseMove, eventHandler);
        c.addListener(SWT.MouseDown, eventHandler);
        c.addListener(SWT.MouseUp, eventHandler);
        c.addListener(SWT.KeyDown, eventHandler);
        c.addListener(SWT.Selection, eventHandler);
        c.addListener(SWT.DefaultSelection, eventHandler);
        c.addListener(SWT.Collapse, eventHandler);
        c.addListener(SWT.Expand, eventHandler);
    }

    /*
     * Fire the selection event to all selectionEventListeners
     */
    private void fireSelectionEvent(SelectionEvent e) {
        if (e.item != null && e.item.isDisposed()) {
			return;
		}
        Object l[] = selectionEventListeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((SelectionListener) l[i]).widgetSelected(e);
        }
    }

    /*
     * Fire the default selection event to all selectionEventListeners
     */
    private void fireDefaultSelectionEvent(SelectionEvent e) {
        Object l[] = selectionEventListeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((SelectionListener) l[i]).widgetDefaultSelected(e);
        }
    }

    /*
     * Fire the post selection event to all postSelectionEventListeners
     */
    private void firePostSelectionEvent(SelectionEvent e) {
        if (e.item != null && e.item.isDisposed()) {
			return;
		}
        Object l[] = postSelectionEventListeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((SelectionListener) l[i]).widgetSelected(e);
        }
    }

    /*
     * Fire the open event to all openEventListeners
     */
    private void fireOpenEvent(SelectionEvent e) {
        if (e.item != null && e.item.isDisposed()) {
			return;
		}
        Object l[] = openEventListeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            ((IOpenEventListener) l[i]).handleOpen(e);
        }
    }

    //Initialize event handler.
    private void initializeHandler(final Display display) {
        eventHandler = new Listener() {
            boolean timerStarted = false;

            Event mouseUpEvent = null;

            Event mouseMoveEvent = null;

            SelectionEvent selectionPendent = null;

            boolean enterKeyDown = false;

            SelectionEvent defaultSelectionPendent = null;

            boolean arrowKeyDown = false;

            final int[] count = new int[1];

            long startTime = System.currentTimeMillis();

            boolean collapseOccurred = false;

            boolean expandOccurred = false;

            @Override
			public void handleEvent(final Event e) {
                if (e.type == SWT.DefaultSelection) {
                    SelectionEvent event = new SelectionEvent(e);
                    fireDefaultSelectionEvent(event);
                    if (CURRENT_METHOD == DOUBLE_CLICK) {
                        fireOpenEvent(event);
                    } else {
                        if (enterKeyDown) {
                            fireOpenEvent(event);
                            enterKeyDown = false;
                            defaultSelectionPendent = null;
                        } else {
                            defaultSelectionPendent = event;
                        }
                    }
                    return;
                }

                switch (e.type) {
                case SWT.MouseEnter:
                case SWT.MouseExit:
                    mouseUpEvent = null;
                    mouseMoveEvent = null;
                    selectionPendent = null;
                    break;
                case SWT.MouseMove:
                    if ((CURRENT_METHOD & SELECT_ON_HOVER) == 0) {
						return;
					}
                    if (e.stateMask != 0) {
						return;
					}
                    if (e.widget.getDisplay().getFocusControl() != e.widget) {
						return;
					}
                    mouseMoveEvent = e;
                    final Runnable runnable[] = new Runnable[1];
                    runnable[0] = () -> {
					    long time = System.currentTimeMillis();
					    int diff = (int) (time - startTime);
					    if (diff <= TIME) {
					        display.timerExec(diff * 2 / 3, runnable[0]);
					    } else {
					        timerStarted = false;
					        setSelection(mouseMoveEvent);
					    }
					};
                    startTime = System.currentTimeMillis();
                    if (!timerStarted) {
                        timerStarted = true;
                        display.timerExec(TIME * 2 / 3, runnable[0]);
                    }
                    break;
                case SWT.MouseDown:
                    mouseUpEvent = null;
                    arrowKeyDown = false;
                    break;
                case SWT.Expand:
                    expandOccurred = true;
                    break;
                case SWT.Collapse:
                    collapseOccurred = true;
                    break;
                case SWT.MouseUp:
                    mouseMoveEvent = null;
                    if ((e.button != 1) || ((e.stateMask & ~SWT.BUTTON1) != 0)) {
						return;
					}
                    if (selectionPendent != null
                            && !(collapseOccurred || expandOccurred)) {
                        mouseSelectItem(selectionPendent);
                    } else {
                        mouseUpEvent = e;
                        collapseOccurred = false;
                        expandOccurred = false;
                        selectionPendent= null;
                    }
                    break;
                case SWT.KeyDown:
                    mouseMoveEvent = null;
                    mouseUpEvent = null;
                    arrowKeyDown = ((e.keyCode == SWT.ARROW_UP) || (e.keyCode == SWT.ARROW_DOWN))
                            && e.stateMask == 0;
                    if (e.character == SWT.CR) {
                        if (defaultSelectionPendent != null) {
                            fireOpenEvent(new SelectionEvent(e));
                            enterKeyDown = false;
                            defaultSelectionPendent = null;
                        } else {
                            enterKeyDown = true;
                        }
                    }
                    collapseOccurred= false;
                    expandOccurred= false;
                    break;
                case SWT.Selection:
                    SelectionEvent event = new SelectionEvent(e);
                    fireSelectionEvent(event);
                    mouseMoveEvent = null;
                    if (mouseUpEvent != null) {
						mouseSelectItem(event);
					} else {
						selectionPendent = event;
					}
                    count[0]++;
                    final int id = count[0];
                    // In the case of arrowUp/arrowDown when in the arrowKeysOpen mode, we
                    // want to delay any selection until the last arrowDown/Up occurs.  This
                    // handles the case where the user presses arrowDown/Up successively.
                    // We only want to open an editor for the last selected item.
                    display.asyncExec(() -> {
					    if (arrowKeyDown) {
					        display.timerExec(TIME, () -> {
							    if (id == count[0]) {
							        firePostSelectionEvent(new SelectionEvent(
							                e));
							        if ((CURRENT_METHOD & ARROW_KEYS_OPEN) != 0) {
										fireOpenEvent(new SelectionEvent(
							                    e));
									}
							    }
							});
					    } else {
					        firePostSelectionEvent(new SelectionEvent(e));
					    }
					});
                    break;
                }
            }

            void mouseSelectItem(SelectionEvent e) {
                if ((CURRENT_METHOD & SINGLE_CLICK) != 0) {
					fireOpenEvent(e);
				}
                mouseUpEvent = null;
                selectionPendent = null;
            }

            void setSelection(Event e) {
                if (e == null) {
					return;
				}
                Widget w = e.widget;
                if (w.isDisposed()) {
					return;
				}

                SelectionEvent selEvent = new SelectionEvent(e);

                /*ISSUE: May have to create a interface with method:
                 setSelection(Point p) so that user's custom widgets
                 can use this class. If we keep this option. */
                if (w instanceof Tree) {
                    Tree tree = (Tree) w;
                    TreeItem item = tree.getItem(new Point(e.x, e.y));
                    if (item != null) {
						tree.setSelection(new TreeItem[] { item });
					}
                    selEvent.item = item;
                } else if (w instanceof Table) {
                    Table table = (Table) w;
                    TableItem item = table.getItem(new Point(e.x, e.y));
                    if (item != null) {
						table.setSelection(new TableItem[] { item });
					}
                    selEvent.item = item;
                } else if (w instanceof TableTree) {
                    TableTree table = (TableTree) w;
                    TableTreeItem item = table.getItem(new Point(e.x, e.y));
                    if (item != null) {
						table.setSelection(new TableTreeItem[] { item });
					}
                    selEvent.item = item;
                } else {
                    return;
                }
                if (selEvent.item == null) {
					return;
				}
                fireSelectionEvent(selEvent);
                firePostSelectionEvent(selEvent);
            }
        };
    }
}
