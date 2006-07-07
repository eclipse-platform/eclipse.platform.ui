/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.examples.databinding.compositetable.day;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.examples.databinding.compositetable.CompositeTable;
import org.eclipse.jface.examples.databinding.compositetable.IRowContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.RowConstructionListener;
import org.eclipse.jface.examples.databinding.compositetable.ScrollEvent;
import org.eclipse.jface.examples.databinding.compositetable.ScrollListener;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.DayEditorCalendarableItemControl;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.EventLayoutComputer;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.ICalendarableItemControl;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.TimeSlice;
import org.eclipse.jface.examples.databinding.compositetable.day.internal.TimeSlot;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableModel;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider;
import org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

/**
 * A DayEditor is an SWT control that can display events on a time line that can
 * span one or more days.  This class is not intended to be subclassed.
 * 
 * @since 3.2
 */
public class DayEditor extends Composite implements IEventEditor {
	private CompositeTable compositeTable = null;
	private CalendarableModel model = new CalendarableModel();
	private List recycledCalendarableEventControls = new LinkedList();
	protected TimeSlice daysHeader = null;
	private final boolean headerDisabled;
	private int defaultEventDuration;
	
	
	/**
	 * NO_HEADER constant.  A style bit constant to indicate that no header
	 * should be displayed at the top of the editor window.
	 */
	public static final int NO_HEADER=SWT.NO_TRIM;
	
	/**
	 * Constructor DayEditor.  Constructs a calendar control that can display
	 * events on one or more days.
	 * 
	 * @param parent
	 * @param style  DayEditor.NO_HEADER or SWT.NO_TRIM means not to display a header.
	 */
	public DayEditor(Composite parent, int style) {
		super(parent, SWT.NULL);
		if ((style & NO_HEADER) != 0) {
			headerDisabled = true;
		} else {
			headerDisabled = false;
		}
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setTimeBreakdown(int, int)
	 */
	public void setTimeBreakdown(int numberOfDays, int numberOfDivisionsInHour) {
		checkWidget();
		model.setTimeBreakdown(numberOfDays, numberOfDivisionsInHour);
		
		if (compositeTable != null) {
			compositeTable.dispose();
		}
		
		createCompositeTable(numberOfDays, numberOfDivisionsInHour);
	}

	/**
	 * This method initializes compositeTable
	 * 
	 * @param numberOfDays
	 *            The number of day columns to display
	 */
	private void createCompositeTable(final int numberOfDays,
			final int numberOfDivisionsInHour) {
		
		compositeTable = new CompositeTable(this, SWT.NONE);
		if (background != null) {
			compositeTable.setBackground(background);
		}
		compositeTable.setTraverseOnTabsEnabled(false);
		
		if (!headerDisabled) {
			new TimeSlice(compositeTable, SWT.BORDER);		// The prototype header
		}
		new TimeSlice(compositeTable, SWT.NONE); // The prototype row
		
		compositeTable.setNumRowsInCollection(computeNumRowsInCollection(numberOfDivisionsInHour));
		
		compositeTable.addRowConstructionListener(new RowConstructionListener() {
			public void headerConstructed(Control newHeader) {
				daysHeader = (TimeSlice) newHeader;
				daysHeader.setHeaderControl(true);
				daysHeader.setNumberOfColumns(numberOfDays);
				if (model.getStartDate() == null) {
					return;
				}
				refreshColumnHeaders(daysHeader.getColumns());
			}
			
			public void rowConstructed(Control newRow) {
				TimeSlice timeSlice = (TimeSlice) newRow;
				timeSlice.setNumberOfColumns(numberOfDays);
				timeSlice.addCellFocusListener(cellFocusListener);
				timeSlice.addKeyListener(keyListener);
				timeSlice.addMouseListener(cellMouseListener);
			}
		});
		compositeTable.addRowContentProvider(new IRowContentProvider() {
			public void refresh(CompositeTable sender, int currentObjectOffset,
					Control row) {
				TimeSlice timeSlice = (TimeSlice) row;
				refreshRow(currentObjectOffset, timeSlice);
			}
		});
		compositeTable.addScrollListener(new ScrollListener() {
			public void tableScrolled(ScrollEvent scrollEvent) {
				layoutEventControls();
			}
		});
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle bounds = DayEditor.this.getBounds();
				compositeTable.setBounds(0, 0, bounds.width, bounds.height);
				layoutEventControlsDeferred();
			}
		});
		
		compositeTable.setRunTime(true);
	}
	
	private Menu menu = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
	 */
	public void setMenu(final Menu menu) {
		checkWidget();
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (isDisposed()) return;
				DayEditor.super.setMenu(menu);
				DayEditor.this.menu = menu;
				compositeTable.setMenu(menu);
				setMenuOnCollection(recycledCalendarableEventControls, menu);
				for (int day=0; day < model.getNumberOfDays(); ++day) {
					List calendarablesForDay = model.getCalendarableItems(day);
					setMenuOnCollection(calendarablesForDay, menu);
				}
			}
		});
	}
	


	private void setMenuOnCollection(List collection, Menu menu) {
		for (Iterator controls = collection.iterator(); controls.hasNext();) {
			ICalendarableItemControl control = (ICalendarableItemControl) controls.next();
			control.setMenu(menu);
		}
	}
	
	private ArrayList keyListeners = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void addKeyListener(KeyListener listener) {
		checkWidget();
		keyListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
	public void removeKeyListener(KeyListener listener) {
		checkWidget();
		keyListeners.remove(listener);
	}
	
	private KeyListener keyListener = new KeyAdapter() {
		public void keyReleased(KeyEvent e) {
			for (Iterator i = keyListeners.iterator(); i.hasNext();) {
				KeyListener keyListener = (KeyListener) i.next();
				keyListener.keyReleased(e);
				if (!e.doit) return;
			}
		}
		
		public void keyPressed(KeyEvent e) {
			for (Iterator i = keyListeners.iterator(); i.hasNext();) {
				KeyListener keyListener = (KeyListener) i.next();
				keyListener.keyPressed(e);
				if (!e.doit) return;
			}
			CalendarableItem selection = selectedCalendarable;
			int selectedRow;
			int selectedDay;
			boolean allDayEventRowSelected = false;
			int compositeTableRow = compositeTable.getSelection().y + compositeTable.getTopRow();
			if (compositeTableRow < numberOfAllDayEventRows) {
				allDayEventRowSelected = true;
			}
			
			if (selection == null) {
				selectedRow = convertViewportRowToDayRow(compositeTable.getCurrentRow());
				selectedDay = compositeTable.getCurrentColumn();
			} else {
				selectedDay = model.getDay(selection);
				if (allDayEventRowSelected) {
					selectedRow = compositeTableRow;
				} else {
					Point selectedCoordinates = selection.getUpperLeftPositionInDayRowCoordinates();
					if (selectedCoordinates == null) {
						return;
					}
					selectedRow = selectedCoordinates.y;
				}
			}
			
			switch (e.character) {
			case SWT.TAB:
				if ((e.stateMask & SWT.SHIFT) != 0) {
					CalendarableItem newSelection = model.findPreviousCalendarable(selectedDay, selectedRow, selection, allDayEventRowSelected);
					if (newSelection == null) {
						// There was only 0 or one visible event--nothing to scroll to
						return;
					}
					int newTopRow = computeNewTopRowBasedOnSelection(newSelection);
					if (newTopRow != compositeTable.getTopRow()) {
						compositeTable.setTopRow(newTopRow);
					}
					setSelection(newSelection);
				} else {
					CalendarableItem newSelection = model.findNextCalendarable(selectedDay, selectedRow, selection, allDayEventRowSelected);
					if (newSelection == null) {
						// There was only 0 or one visible event--nothing to scroll to
						return;
					}
					int newTopRow = computeNewTopRowBasedOnSelection(newSelection);
					if (newTopRow != compositeTable.getTopRow()) {
						compositeTable.setTopRow(newTopRow);
					}
					setSelection(newSelection);
				}
			}
		}
	};
	
	private ArrayList mouseListeners = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	public void addMouseListener(MouseListener listener) {
		checkWidget();
		mouseListeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#removeMouseListener(org.eclipse.swt.events.MouseListener)
	 */
	public void removeMouseListener(MouseListener listener) {
		checkWidget();
		mouseListeners.remove(listener);
	}
	
	private MouseListener cellMouseListener = new MouseListener() {
		public void mouseDoubleClick(MouseEvent e) {
			fireMouseDoubleClickEvent(e);
		}
		public void mouseDown(MouseEvent e) {
			fireMouseDownEvent(e);
		}
		public void mouseUp(MouseEvent e) {
			fireMouseUpEvent(e);
		}
	};
	
	protected void fireMouseDownEvent(MouseEvent e) {
		for (Iterator i = mouseListeners.iterator(); i.hasNext();) {
			MouseListener mouseListener = (MouseListener) i.next();
			mouseListener.mouseDown(e);
		}
	}
	
	protected void fireMouseUpEvent(MouseEvent e) {
		for (Iterator i = mouseListeners.iterator(); i.hasNext();) {
			MouseListener mouseListener = (MouseListener) i.next();
			mouseListener.mouseUp(e);
		}
	}
	
	protected void fireMouseDoubleClickEvent(MouseEvent e) {
		for (Iterator i = mouseListeners.iterator(); i.hasNext();) {
			MouseListener mouseListener = (MouseListener) i.next();
			mouseListener.mouseDoubleClick(e);
		}
	}
	
	private int computeNewTopRowBasedOnSelection(CalendarableItem newSelection) {
		int topRow = compositeTable.getTopRow();
		int numberOfRowsInDisplay = compositeTable.getNumRowsVisible();
		int newTopRow = topRow;
		
		Point endRowPoint = newSelection.getLowerRightPositionInDayRowCoordinates();
		if (endRowPoint != null) {
			int endRow = convertDayRowToViewportCoordinates(endRowPoint.y);
			if (endRow >= newTopRow + numberOfRowsInDisplay) {
				newTopRow += (endRow - (newTopRow + numberOfRowsInDisplay)) + 1;
			}
			int startRow = newSelection.getUpperLeftPositionInDayRowCoordinates().y;
			startRow = convertDayRowToViewportCoordinates(startRow);
			if (startRow < newTopRow) {
				newTopRow = startRow;
			}
		}
		return newTopRow;
	}
	
	private boolean selectCalendarableControlOnSetFocus = true;
	
	private FocusListener cellFocusListener = new FocusAdapter() {
		public void focusGained(FocusEvent e) {
			TimeSlice sendingRow = (TimeSlice) ((Composite) e.widget).getParent();
			int day = sendingRow.getControlColumn(e.widget);
			int row = compositeTable.getControlRow(sendingRow);
			if (selectCalendarableControlOnSetFocus) {
				setSelectionByDayAndRow(day, row, null);
			} else {
				selectCalendarableControlOnSetFocus = true;
			}
		}
	};
	
	private void setSelectionByDayAndRow(int day, int row, CalendarableItem aboutToSelect) {
		int dayRow = convertViewportRowToDayRow(row);
		if (aboutToSelect == null && dayRow >= 0)
			aboutToSelect = getFirstCalendarableAt(day, dayRow);
		if (aboutToSelect == null || dayRow < 0) {
			aboutToSelect = getAllDayCalendarableAt(day, row + compositeTable.getTopRow());
		}
		selectCalenderableControl(aboutToSelect);
		aboutToSelect = null;
	}

	/** (non-API)
	 * Method getFirstCalendarableAt. Finds the calendarable event at the 
	 * specified day/row in DayRow coordinates.  If no calendarable exists
	 * at the specified coordinates, does nothing.
	 * 
	 * @param day The day offset
	 * @param row The row offset in DayRow coordinates
	 * @return the first Calendarable in the specified (day, row) or null if none.
	 */
	protected CalendarableItem getFirstCalendarableAt(int day, int row) {
		CalendarableItem[][] eventLayout = model.getEventLayout(day);
		CalendarableItem selectedCalendarable = null;
		for (int column=0; column < eventLayout.length; ++column) {
			CalendarableItem calendarable = eventLayout[column][row];
			if (calendarable != null) {
				if (selectedCalendarable == null) {
					selectedCalendarable = calendarable;
				} else if (calendarable.getStartTime().after(selectedCalendarable.getStartTime())) {
					selectedCalendarable = calendarable;
				}
			}
		}
		return selectedCalendarable;
	}
	
	/**
	 * Find the all day event that is positioned at the specified day and row in viewport coordinates
	 * 
	 * @param day
	 * @param row
	 * @return The found Calendarable or null if none
	 */
	protected CalendarableItem getAllDayCalendarableAt(int day, int row) {
		CalendarableItem[] allDayEvents = model.getAllDayCalendarables(day);
		for (int allDayEventRow = 0; allDayEventRow < allDayEvents.length; allDayEventRow++) {
			CalendarableItem candidate = allDayEvents[allDayEventRow];
			if (allDayEventRow == row) {
				return candidate;
			}
		}
//		int allDayEventRow = 0;
//		for (Iterator calendarablesIter = model.getCalendarableEvents(day).iterator(); calendarablesIter.hasNext();) {
//			Calendarable candidate = (Calendarable) calendarablesIter.next();
//			if (candidate.isAllDayEvent()) {
//				if (allDayEventRow == row) {
//					return candidate;
//				}
//				++allDayEventRow;
//			}
//		}
		return null;
	}

	private CalendarableItem selectedCalendarable = null;
	
	/**
	 * Method selectCalendarable.  Selects the specified Calendarable event.
	 * 
	 * @param newSelection The Calendarable to select.
	 */
	public void setSelection(CalendarableItem newSelection) {
		checkWidget();
		if (newSelection != null) {
			int day = model.getDay(newSelection);
			int row = computeRowForCalendarable(newSelection, day);
			selectCalendarableControlOnSetFocus = false;
			compositeTable.setSelection(day, row);
			selectCalenderableControl(newSelection);
		} else {
			selectCalenderableControl(null);
		}
	}

	private void selectCalenderableControl(CalendarableItem newSelection) {
		if (selectedCalendarable == newSelection) {
			return;
		}
		if (selectedCalendarable != null) {
			// The control could be null if it just got scrolled off the screen top or bottom
			if (selectedCalendarable.getControl() != null) {
				selectedCalendarable.getControl().setSelected(false);
			}
		}
		
		CalendarableItem oldSelection = selectedCalendarable;
		selectedCalendarable = newSelection;
		
		if (newSelection != null && newSelection.getControl() != null) {
			newSelection.getControl().setSelected(true);
		}
		fireSelectionChangeEvent(oldSelection, newSelection);
	}
	
	/**
	 * Method getSelection.  Returns the selection.  This is computed as follows:
	 * <ol>
	 * <li>If a CalendarableItem is currently selected, it is returned.
	 * <li>If the selection rectangle is in an all-day event row, null is returned.
	 * <li>Otherwise, the date/time corresponding to the selection rectangle is returned as a java.util.Date.
	 * </ol>
	 * 
	 * @return the current DayEditorSelection
	 */
	public DayEditorSelection getSelection() {
		checkWidget();
		DayEditorSelection selection = new DayEditorSelection();
		Point compositeTableSelection = compositeTable.getSelection();
		
		if (selectedCalendarable != null) {
			selection.setSelectedCalendarable(selectedCalendarable);
			if (selectedCalendarable.isAllDayEvent()) {
				selection.setAllDay(true);
			}
		} else {
			int visibleAllDayEventRows = model.computeNumberOfAllDayEventRows();
			visibleAllDayEventRows -= compositeTable.getTopRow();
			if (visibleAllDayEventRows > 0) {
				if (compositeTableSelection.y <= visibleAllDayEventRows) {
					selection.setAllDay(true);
				}
			}
		}
		selection.setDateTime(computeDateTimeFromViewportCoordinates(compositeTableSelection));
		return selection;
	}
	
	private List selectionChangeListeners = new ArrayList();

	private void fireSelectionChangeEvent(CalendarableItem currentSelection, CalendarableItem newSelection) {
		SelectionChangeEvent sce = new SelectionChangeEvent(currentSelection, newSelection);
		for (Iterator listenersIter = selectionChangeListeners.iterator(); listenersIter.hasNext();) {
			CalendarableSelectionChangeListener listener = (CalendarableSelectionChangeListener) listenersIter.next();
			listener.selectionChanged(sce);
		}
	}
	
	/**
	 * Adds the listener to the collection of listeners who will
	 * be notified when the receiver's selection changes, by sending
	 * it one of the messages defined in the <code>CalendarableSelectionChangeListener</code>
	 * interface.
	 * <p>
	 * <code>selectionChanged</code> is called when the selection changes.
	 * </p>
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableSelectionChangeListener
	 * @see #removeSelectionChangeListener
	 * @see SelectionChangeEvent
	 */
	public void addSelectionChangeListener(CalendarableSelectionChangeListener l) {
		checkWidget();
		if (l == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		selectionChangeListeners.add(l);
	}
	
	private boolean fireEvents(CalendarableItemEvent e, List handlers) {
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			CalendarableItemEventHandler h = (CalendarableItemEventHandler) i.next();
			h.handleRequest(e);
			if (!e.doit) {
				break;
			}
		}
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			CalendarableItemEventHandler h = (CalendarableItemEventHandler) i.next();
			h.requestHandled(e);
			if (!e.doit) {
				break;
			}
		}
		return e.doit;
	}

	private boolean fireEvents(CalendarableItem calendarableItem, List handlers) {
		CalendarableItemEvent e = new CalendarableItemEvent();
		e.calendarableItem = calendarableItem;
		for (Iterator iter = handlers.iterator(); iter.hasNext();) {
			CalendarableItemEventHandler handler = (CalendarableItemEventHandler) iter.next();
			handler.handleRequest(e);
			if (!e.doit) {
				break;
			}
		}
		for (Iterator i = handlers.iterator(); i.hasNext();) {
			CalendarableItemEventHandler h = (CalendarableItemEventHandler) i.next();
			h.requestHandled(e);
			if (!e.doit) {
				break;
			}
		}
		return e.doit;
	}
	
	private List editHandlers = new ArrayList();
	
	/**
	 * Fire the itemEdit event.
	 * 
	 * @param toEdit The CalendarableItem to edit.
	 * @return true if the object represented by the CalendarableItem was changed; false otherwise.
	 */
	public boolean fireEdit(CalendarableItem toEdit) {
		checkWidget();
		CalendarableItemEvent e = new CalendarableItemEvent();
		e.calendarableItem = toEdit;
		boolean changed = fireEvents(e, editHandlers);
		if (changed) {
			// TODO: only refresh the days that are necessary
	        refresh();
		}
		return changed;
	}
	
	/**
	 * Adds the handler to the collection of handlers who will hand editing of
	 * calendarable events, by sending it one of the messages defined in the
	 * <code>CalendarableItemInsertHandler</code> abstract class.
	 * <p>
	 * <code>itemInserted</code> is called when the CalendarableItem is
	 * inserted.
	 * </p>
	 * 
	 * @param handler
	 *            the handler which should be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see CalendarableItemInsertHandler
	 * @see #removeItemInsertHandler
	 */
	public void addItemEditHandler(CalendarableItemEventHandler handler) {		
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		editHandlers.add(handler);
	}
	
	/**
	 * Removes the handler from the collection of handlers who will hand editing of
	 * calendarable events, by sending it one of the messages defined in the
	 * <code>CalendarableItemInsertHandler</code> abstract class.
	 * <p>
	 * <code>itemInserted</code> is called when the CalendarableItem is
	 * inserted.
	 * </p>
	 * 
	 * @param handler
	 *            the handler which should be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see CalendarableItemInsertHandler
	 * @see #removeItemInsertHandler
	 */
	public void removeItemEditHandler(CalendarableItemEventHandler handler) {		
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		editHandlers.remove(handler);
	}
	
	private List insertHandlers = new ArrayList();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#fireInsert(java.util.Date)
	 */
	public NewEvent fireInsert(Date date, boolean allDayEvent) {
		checkWidget();
		CalendarableItem item = new CalendarableItem(date);
		item.setAllDayEvent(allDayEvent);
		item.setStartTime(date);
		item.setEndTime(incrementHour(date, getDefaultEventDuration()));
		CalendarableItemEvent e = new CalendarableItemEvent();
		e.calendarableItem = item;
		if (fireEvents(e, insertHandlers)) {
			// TODO: Only refresh the affected days
			refresh();
			return (NewEvent) e.result;
		}
		return null;
	}

	private Date incrementHour(Date date, int increment) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR_OF_DAY, increment);
		return c.getTime();
	}

	/**
	 * Adds the handler to the collection of handlers who will be notified when
	 * a CalendarableItem is inserted in the receiver, by sending it one of the
	 * messages defined in the <code>CalendarableItemInsertHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemInserted</code> is called when the CalendarableItem is
	 * inserted.
	 * </p>
	 * 
	 * @param handler
	 *            the handler which should be notified
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see CalendarableItemInsertHandler
	 * @see #removeItemInsertHandler
	 */
	public void addItemInsertHandler(CalendarableItemEventHandler handler) {		
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		insertHandlers.add(handler);
	}
	
	/**
	 * Removes the handler from the collection of handlers who will
	 * be notified when a CalendarableItem is inserted into the receiver, by sending
	 * it one of the messages defined in the <code>CalendarableItemInsertHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemInserted</code> is called when the CalendarableItem is inserted.
	 * </p>
	 *
	 * @param handler the handler which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableItemInsertHandler
	 * @see #addItemInsertHandler
	 */
	public void removeItemInsertHandler(CalendarableItemEventHandler handler) {		
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		insertHandlers.remove(handler);
	}
	
	private List deleteHandlers = new ArrayList();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#fireDelete(org.eclipse.jface.examples.databinding.compositetable.timeeditor.CalendarableItem)
	 */
	public boolean fireDelete(CalendarableItem item) {
		checkWidget();
		boolean result = fireEvents(item, deleteHandlers);
		if (result) {
			// TODO: Only refresh the affected days.
			refresh();
		}
		return result;
	}
	
	/**
	 * Adds the handler to the collection of handlers who will
	 * be notified when a CalendarableItem is deleted from the receiver, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
	 * </p>
	 *
	 * @param handler the handler which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableItemEventHandler
	 * @see #removeDeleteItemHandler
	 */
	public void addItemDeleteHandler(CalendarableItemEventHandler handler) {
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}		
		deleteHandlers.add(handler);
	}
	
	/**
	 * Removes the handler from the collection of handlers who will
	 * be notified when a CalendarableItem is deleted from the receiver, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
	 * </p>
	 *
	 * @param handler the handler which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableItemEventHandler
	 * @see #addDeleteItemHandler
	 */
	public void removeItemDeleteHandler(CalendarableItemEventHandler handler) {
		checkWidget();
		deleteHandlers.remove(handler);
	}
	
	private List itemDisposeHandlers = new ArrayList();
	
	private boolean fireDisposeItemStrategy(CalendarableItem item) {
		return fireEvents(item, itemDisposeHandlers);
	}
	
	/**
	 * Adds the handler to the collection of handler who will
	 * be notified when a CalendarableItem's control is disposed, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.  This is normally used to remove any data bindings
	 * that may be attached to the (now-unused) CalendarableItem.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
	 * </p>
	 *
	 * @param handler the handler which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableItemEventHandler
	 * @see #removeCalendarableItemDisposeHandler
	 */
	public void addItemDisposeHandler(CalendarableItemEventHandler handler) {
		checkWidget();
		if (handler == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}		
		itemDisposeHandlers.add(handler);
	}
	
	/**
	 * Removes the handler from the collection of handlers who will
	 * be notified when a CalendarableItem is disposed, by sending
	 * it one of the messages defined in the <code>CalendarableItemEventHandler</code>
	 * abstract class.  This is normally used to remove any data bindings
	 * that may be attached to the (now-unused) CalendarableItem.
	 * <p>
	 * <code>itemDeleted</code> is called when the CalendarableItem is deleted.
	 * </p>
	 *
	 * @param handler the handler which should be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the handler is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableItemEventHandler
	 * @see #removeDeleteListener
	 */
	public void removeItemDisposeHandler(CalendarableItemEventHandler handler) {
		checkWidget();
		itemDisposeHandlers.remove(handler);
	}
	
	/**
	 * Removes the listener from the collection of listeners who will
	 * be notified when the receiver's selection changes, by sending
	 * it one of the messages defined in the <code>CalendarableSelectionChangeListener</code>
	 * interface.
	 * <p>
	 * <code>selectionChanged</code> is called when the selection changes.
	 * </p>
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see CalendarableSelectionChangeListener
	 * @see #addSelectionChangeListener
	 * @see SelectionChangeEvent
	 */
	public void removeSelectionChangeListener(CalendarableSelectionChangeListener l) {
		checkWidget();
		if (l == null) {
			throw new IllegalArgumentException("The argument cannot be null");
		}
		if (isDisposed()) {
			throw new SWTException("Widget is disposed");
		}
		selectionChangeListeners.remove(l);
	}

	/**
	 * @return Returns the defaultStartHour.
	 */
	public int getDefaultStartHour() {
		return model.getDefaultStartHour();
	}

	/**
	 * @param defaultStartHour The defaultStartHour to set.
	 */
	public void setDefaultStartHour(int defaultStartHour) {
		checkWidget();
		model.setDefaultStartHour(defaultStartHour);
		updateVisibleRows();
		layoutEventControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setDayEventCountProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventCountProvider)
	 */
	public void setEventCountProvider(EventCountProvider eventCountProvider) {
		checkWidget();
		model.setEventCountProvider(eventCountProvider);
		updateVisibleRows();
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (isDisposed()) return;
				layoutEventControls();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setEventContentProvider(org.eclipse.jface.examples.databinding.compositetable.timeeditor.EventContentProvider)
	 */
	public void setEventContentProvider(EventContentProvider eventContentProvider) {
		checkWidget();
		model.setEventContentProvider(eventContentProvider);
		updateVisibleRows();
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (isDisposed()) return;
				layoutEventControls();
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#setStartDate(java.util.Date)
	 */
	public void setStartDate(Date startDate) {
		checkWidget();
		List removedDays = model.setStartDate(startDate);
		computeEventRowsForNewDays();
		if (daysHeader != null) {
			refreshColumnHeaders(daysHeader.getColumns());
		}
		updateVisibleRows();
		freeObsoleteCalendarableEventControls(removedDays);
		if (compositeTable.getNumRowsVisible() > 0) {
			layoutEventControls();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#getStartDate()
	 */
	public Date getStartDate() {
		checkWidget();
		return model.getStartDate();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#refresh(java.util.Date)
	 */
	public void refresh(Date date) {
		checkWidget();
		List removedDays = model.refresh(date);
		freeObsoleteCalendarableEventControls(removedDays);
		updateVisibleRows();
		computeEventRowsForDate(date);
		layoutEventControls();
	}
	
	private boolean refreshing = false;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#refresh()
	 */
	public void refresh() {
		checkWidget();
		if (!refreshing) {
			refreshing = true;
			Display.getCurrent().asyncExec(new Runnable() {
				public void run() {
					if (isDisposed()) return;
					Date dateToRefresh = getStartDate();
					GregorianCalendar gc = new GregorianCalendar();
					gc.setTime(dateToRefresh);
					for (int i=0; i < getNumberOfDays(); ++i) {
						refresh(gc.getTime());
						gc.add(Calendar.DATE, 1);
					}
					refreshing = false;
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#getNumberOfDays()
	 */
	public int getNumberOfDays() {
		checkWidget();
		return model.getNumberOfDays();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.examples.databinding.compositetable.timeeditor.IEventEditor#getNumberOfDivisionsInHour()
	 */
	public int getNumberOfDivisionsInHour() {
		checkWidget();
		return model.getNumberOfDivisionsInHour();
	}
	
	// Display Refresh logic here ----------------------------------------------
	
	/*
	 * There are four main coordinate systems the refresh algorithm has to
	 * deal with:
	 * 
	 * 1) Rows starting from midnight (the way the DayModel computes the layout).  
	 *    These are called Day Row coordinates.
	 *    
	 * 2) Rows starting from the top visible row, taking into account all-day 
	 *    event rows.  These are called Viewport Row coordinates
	 *    
	 * 3) Pixel coordinates for each TimeSlot, relative to its parent TimeSlice
	 *    (the CompositeTable row object) row.  This is relevant because these 
	 *    are transformed into #4 in order to place CalendarableEventControls.
	 *    
	 * 4) Pixel coordinates relative to the top left (the origin) of the entire
	 *    DayEditor control.
	 */
	
	private int numberOfAllDayEventRows = 0;
	Calendar calendar = new GregorianCalendar();

	private int computeNumRowsInCollection(final int numberOfDivisionsInHour) {
		numberOfAllDayEventRows = model.computeNumberOfAllDayEventRows();
		return (DISPLAYED_HOURS-model.computeStartHour()) * numberOfDivisionsInHour+numberOfAllDayEventRows;
	}
	
	private int convertViewportRowToDayRow(int row) {
		int topRowOffset = compositeTable.getTopRow() - numberOfAllDayEventRows;
		int startOfDayOffset = model.computeStartHour() * model.getNumberOfDivisionsInHour();
		return row + topRowOffset + startOfDayOffset;
	}

	private int convertDayRowToViewportCoordinates(int row) {
		row -= model.computeStartHour() * model.getNumberOfDivisionsInHour()
			- numberOfAllDayEventRows;
		return row;
	}
	
	private Date computeDateTimeFromViewportCoordinates(Point viewportSelection) {
		Date startDate = model.calculateDate(getStartDate(), viewportSelection.x);
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(startDate);
		calendar.set(Calendar.HOUR_OF_DAY, 
				model.computeHourFromRow(viewportSelection.y));
		calendar.set(Calendar.MINUTE,
				model.computeMinuteFromRow(viewportSelection.y));
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	

	/**
	 * @param calendarable
	 * @param day
	 * @return The row in DayRow coordinates
	 */
	private int computeRowForCalendarable(CalendarableItem calendarable, int day) {
		int row = 0;
		if (calendarable.isAllDayEvent()) {
			CalendarableItem[] allDayEvents = model.getAllDayCalendarables(day);
			for (int allDayEventRow = 0; allDayEventRow < allDayEvents.length; allDayEventRow++) {
				if (allDayEvents[allDayEventRow] == calendarable) {
					row = allDayEventRow - compositeTable.getTopRow();
					break;
				}
			}
		} else {
			// Convert to viewport coordinates
			Point upperLeft = calendarable.getUpperLeftPositionInDayRowCoordinates();
			int topRowOffset = compositeTable.getTopRow() - numberOfAllDayEventRows;
			int startOfDayOffset = model.computeStartHour() * model.getNumberOfDivisionsInHour();
			row = upperLeft.y - topRowOffset - startOfDayOffset;
			if (row < 0) {
				row = 0;
			}
		}
		return row;
	}
	
	
	/*
	 * Update the number of rows that are displayed inside the CompositeTable control
	 */
	private void updateVisibleRows() {
		compositeTable.setNumRowsInCollection(computeNumRowsInCollection(getNumberOfDivisionsInHour()));
	}
	
	private void refreshRow(int currentObjectOffset, TimeSlice timeSlice) {
		// Decrement currentObjectOffset for each all-day event line we need.
		for (int allDayEventRow = 0; allDayEventRow < numberOfAllDayEventRows; ++allDayEventRow) {
			--currentObjectOffset;
		}
		
		if (currentObjectOffset < 0) {
			timeSlice.setCurrentTime(null);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, 
					model.computeHourFromRow(currentObjectOffset));
			calendar.set(Calendar.MINUTE,
					model.computeMinuteFromRow(currentObjectOffset));
			timeSlice.setCurrentTime(calendar.getTime());
		}
	}

	/**
	 * (non-API) Method initializeColumnHeaders.  Called internally when the
	 * column header text needs to be updated.
	 * 
	 * @param columns A LinkedList of CLabels representing the column objects
	 */
	protected void refreshColumnHeaders(LinkedList columns) {
		Date startDate = getStartDate();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(startDate);

		SimpleDateFormat formatter = new SimpleDateFormat("EE, MMM d");
		formatter.applyPattern(formatter.toLocalizedPattern());
		
		for (Iterator iter = columns.iterator(); iter.hasNext();) {
			CLabel headerLabel = (CLabel) iter.next();
			headerLabel.setText(formatter.format(gc.getTime()));
			gc.add(Calendar.DATE, 1);
		}
	}
	
	private void freeObsoleteCalendarableEventControls(List removedCalendarables) {
		for (Iterator removedCalendarablesIter = removedCalendarables.iterator(); removedCalendarablesIter.hasNext();) {
			CalendarableItem toRemove = (CalendarableItem) removedCalendarablesIter.next();
			if (selectedCalendarable == toRemove) {
				setSelection(null);
			}
			freeCalendarableControl(toRemove);
		}
	}
	
	private void computeEventRowsForDate(Date date) {
		GregorianCalendar targetDate = new GregorianCalendar();
		targetDate.setTime(date);
		GregorianCalendar target = new GregorianCalendar();
		target.setTime(model.getStartDate());
		EventLayoutComputer dayModel = new EventLayoutComputer(model.getNumberOfDivisionsInHour());
		for (int dayOffset=0; dayOffset < model.getNumberOfDays(); ++dayOffset) {
			if (target.get(Calendar.DATE) == targetDate.get(Calendar.DATE) &&
				target.get(Calendar.MONTH) == targetDate.get(Calendar.MONTH) &&
				target.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR)) 
			{
				computeEventLayout(dayModel, dayOffset);
				break;
			}
			target.add(Calendar.DATE, 1);
		}
	}
	
	private void computeEventRowsForNewDays() {
		EventLayoutComputer dayModel = new EventLayoutComputer(model.getNumberOfDivisionsInHour());
		for (int dayOffset=0; dayOffset < model.getNumberOfDays(); ++dayOffset) {
			if (model.getNumberOfColumnsWithinDay(dayOffset) == -1) {
				computeEventLayout(dayModel, dayOffset);
			}
		}
	}

	private void computeEventLayout(EventLayoutComputer dayModel, int dayOffset) {
		List events = model.getCalendarableItems(dayOffset);
		CalendarableItem[][] eventLayout = dayModel.computeEventLayout(events);
		model.setEventLayout(dayOffset, eventLayout);
	}

	private void layoutEventControlsDeferred() {
		if (getStartDate() == null) {
			return;
		}
		refreshEventControlPositions.run();
		Display.getCurrent().asyncExec(refreshEventControlPositions);
	}
	
	private void layoutEventControls() {
		if (getStartDate() == null) {
			return;
		}
		refreshEventControlPositions.run();
	}
	
	private Runnable refreshEventControlPositions = new Runnable() {
		public void run() {
			if (isDisposed()) return;
			
			Control[] gridRows = compositeTable.getRowControls();
			
			for (int day=0; day < model.getNumberOfDays(); ++day) {
				int columnsWithinDay = model.getNumberOfColumnsWithinDay(day);
				Point[] columnPositions = computeColumns(day, columnsWithinDay, gridRows);
				
				int allDayEventRow = 0;
				
				for (Iterator calendarablesIter = model.getCalendarableItems(day).iterator(); calendarablesIter.hasNext();) {
					CalendarableItem calendarable = (CalendarableItem) calendarablesIter.next();
					if (calendarable.isAllDayEvent()) {
						layoutAllDayEvent(day, allDayEventRow, calendarable, gridRows);
						++allDayEventRow;
					} else {
						layoutTimedEvent(day, columnPositions, calendarable, gridRows);
					}
				}
			}
		}
	};
	
	protected Point[] computeColumns(int day, int numberOfColumns, Control[] gridRows) {
		Point[] columns = new Point[numberOfColumns];
		Rectangle timeSliceBounds = getTimeSliceBounds(day, compositeTable.getTopRow(), gridRows);
		timeSliceBounds.x += TimeSlot.TIME_BAR_WIDTH + 1;
		timeSliceBounds.width -= TimeSlot.TIME_BAR_WIDTH + 2;
		
		int baseWidth = timeSliceBounds.width / numberOfColumns;
		int extraWidth = timeSliceBounds.width % numberOfColumns;
		
		int startingPosition = timeSliceBounds.x;
		for (int column = 0; column < columns.length; column++) {
			int columnStart = startingPosition;
			int columnWidth = baseWidth;
			if (extraWidth > 0) {
				++columnWidth;
				--extraWidth;
			}
			columns[column] = new Point(columnStart, columnWidth);
			startingPosition += columnWidth;
		}
		return columns;
	}
	
	

	private void fillControlData(CalendarableItem calendarable, int clippingStyle) {
		calendarable.getControl().setText(calendarable.getText());
		calendarable.getControl().setToolTipText(calendarable.getToolTipText());
		calendarable.getControl().setClipping(clippingStyle);
	}

	private DayEditorCalendarableItemControl getControl(CalendarableItem item) {
		return (DayEditorCalendarableItemControl) item.getControl();
	}
	
	private void layoutAllDayEvent(int day, int allDayEventRow, CalendarableItem calendarable, Control[] gridRows) {
		if (eventRowIsVisible(allDayEventRow)) {
			createCalendarableControl(calendarable);
			fillControlData(calendarable, SWT.NULL);
			
			Rectangle timeSliceBounds = getTimeSliceBounds(day, allDayEventRow, gridRows);
			int gutterWidth = TimeSlot.TIME_BAR_WIDTH + 1;
			timeSliceBounds.x += gutterWidth;
			timeSliceBounds.width -= gutterWidth;
			getControl(calendarable).setBounds(timeSliceBounds);
			getControl(calendarable).moveAbove(compositeTable);
		} else {
			freeCalendarableControl(calendarable);
		}
	}

	private void layoutTimedEvent(int day, Point[] columnPositions, CalendarableItem calendarable, Control[] gridRows) {
		int firstVisibleRow = model.computeStartHour() * model.getNumberOfDivisionsInHour();
		
		int scrolledRows = compositeTable.getTopRow() - numberOfAllDayEventRows;
		int visibleAllDayEventRows = 0;
		if (scrolledRows < 0) {
			visibleAllDayEventRows = -1 * scrolledRows;
			scrolledRows = 0;
		}
		firstVisibleRow += scrolledRows;
		int lastVisibleRow = firstVisibleRow + compositeTable.getNumRowsVisible() - visibleAllDayEventRows - 1;
		
		int startRow = calendarable.getUpperLeftPositionInDayRowCoordinates().y;
		int endRow = calendarable.getLowerRightPositionInDayRowCoordinates().y;
		
		if (timedEventIsVisible(firstVisibleRow, lastVisibleRow, startRow, endRow)) {
			int clippingStyle = SWT.NULL;
			
			if (startRow < firstVisibleRow) {
				startRow = firstVisibleRow;
				clippingStyle |= SWT.TOP;
			}
			
			if (endRow > lastVisibleRow) {
				endRow = lastVisibleRow;
				clippingStyle |= SWT.BOTTOM;
			}
			
			startRow = convertDayRowToViewportCoordinates(startRow);
			endRow = convertDayRowToViewportCoordinates(endRow);
			
			createCalendarableControl(calendarable);
			fillControlData(calendarable, clippingStyle);
			
			Rectangle startRowBounds = getTimeSliceBounds(day, startRow, gridRows);
			Rectangle endRowBounds = getTimeSliceBounds(day, endRow, gridRows);
			
			int leftmostColumn = calendarable.getUpperLeftPositionInDayRowCoordinates().x;
			int rightmostColumn = calendarable.getLowerRightPositionInDayRowCoordinates().x;
			
			int left = columnPositions[leftmostColumn].x;
			int top = startRowBounds.y + 1;
			int width = columnPositions[rightmostColumn].x - columnPositions[leftmostColumn].x + columnPositions[rightmostColumn].y;
			int height = endRowBounds.y - startRowBounds.y + endRowBounds.height - 1;
			
			Rectangle finalPosition = new Rectangle(left, top, width, height);
			
			getControl(calendarable).setBounds(finalPosition);
			getControl(calendarable).moveAbove(compositeTable);
		} else {
			freeCalendarableControl(calendarable);
		}
	}

	private boolean eventRowIsVisible(int eventRow) {
		int topRow = compositeTable.getTopRow();
		if (topRow <= eventRow) {
			if (eventRow < compositeTable.getNumRowsVisible() - topRow) {
				return true;
			}
		}
		return false;
	}
	
	private boolean timedEventIsVisible(int firstVisibleRow, int lastVisibleRow, int startRow, int endRow) {
		if (startRow < firstVisibleRow && endRow < firstVisibleRow)
			return false;
		
		if (startRow > lastVisibleRow && endRow > lastVisibleRow)
			return false;
		
		return true;
	}

	private void createCalendarableControl(CalendarableItem calendarable) {
		if (calendarable.getControl() == null) {
			calendarable.setControl(newCEC());
			if (calendarable == selectedCalendarable) {
				calendarable.getControl().setSelected(true);
			}
		}
	}
	
	private Rectangle getTimeSliceBounds(int day, int eventRow, Control[] gridRows) {
		int row = eventRow - compositeTable.getTopRow();
		TimeSlice rowObject = (TimeSlice) gridRows[row];
		Control slot = rowObject.getColumnControl(day);
		return getBoundsInDayEditorCoordinates(slot);
	}
	
	private void freeCalendarableControl(CalendarableItem calendarableItem) {
		if (calendarableItem.getControl() != null) {
			freeCEC(getControl(calendarableItem));
			calendarableItem.setControl(null);
			fireDisposeItemStrategy(calendarableItem);
		}
	}
	
	private Rectangle getBoundsInDayEditorCoordinates(Control slot) {
		return Display.getCurrent().map(slot.getParent(), this, slot.getBounds());
	}

	// CalendarableItemControl construction/destruction here -----------------

	MouseAdapter selectCompositeTableOnMouseDownAdapter = new MouseAdapter() {
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDown(MouseEvent e) {
			fireMouseDownEvent(e);
			ICalendarableItemControl control = (ICalendarableItemControl) e.widget;
			CalendarableItem aboutToSelect = control.getCalendarableItem();
			setSelection(aboutToSelect);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseDoubleClick(MouseEvent e) {
			fireMouseDoubleClickEvent(e);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		public void mouseUp(MouseEvent e) {
			fireMouseUpEvent(e);
		}
	};

	private DayEditorCalendarableItemControl newCEC() {
		if (recycledCalendarableEventControls.size() > 0) {
			DayEditorCalendarableItemControl result = (DayEditorCalendarableItemControl) recycledCalendarableEventControls.remove(0);
			result.setVisible(true);
			return result;
		}
		DayEditorCalendarableItemControl dayEditorCalendarableItemControl = new DayEditorCalendarableItemControl(this, SWT.NULL);
		if (menu != null) {
			dayEditorCalendarableItemControl.setMenu(menu);
		}
		dayEditorCalendarableItemControl.addMouseListener(selectCompositeTableOnMouseDownAdapter);
		return dayEditorCalendarableItemControl;
	}
	
	private void freeCEC(DayEditorCalendarableItemControl control) {
		control.setSelected(false);
		control.setCalendarableItem(null);
		control.setVisible(false);
		recycledCalendarableEventControls.add(control);
	}

	/**
	 * Returns the default duration of a new event, in hours.
	 * 
	 * @return int the number of hours a new event occupies by default.
	 */
	public int getDefaultEventDuration() {
		checkWidget();
		return defaultEventDuration;
	}

	/**
	 * Sets the default duration of a new event, in hours.
	 * 
	 * @param defaultEventDuration
	 *            int the number of hours a new event occupies by default.
	 */
	public void setDefaultEventDuration(int defaultEventDuration) {
		checkWidget();
		this.defaultEventDuration = defaultEventDuration;
	}
	
	private Color background = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
	 */
	public void setBackground(Color color) {
		checkWidget();
		super.setBackground(color);
		this.background = color;
		if (compositeTable != null) {
			compositeTable.setBackground(color);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	public boolean setFocus() {
		checkWidget();
		if (!compositeTable.setFocus()) {
			return super.setFocus();
		}
		return true;
	}

} // @jve:decl-index=0:visual-constraint="10,10"


