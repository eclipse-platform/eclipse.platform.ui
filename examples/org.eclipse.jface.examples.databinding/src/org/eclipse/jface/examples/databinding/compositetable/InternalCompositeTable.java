/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme     - Initial API and implementation
 */
package org.eclipse.jface.examples.databinding.compositetable;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.examples.databinding.compositetable.internal.EmptyTablePlaceholder;
import org.eclipse.jface.examples.databinding.compositetable.internal.ISelectableRegionControl;
import org.eclipse.jface.examples.databinding.compositetable.internal.TableRow;
import org.eclipse.jface.examples.databinding.compositetable.reflect.DuckType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Widget;


/** (non-API)
 * Class InternalCompositeTable.  This is the run-time CompositeTableControl.  It gets its prototype
 * row and (optional) header objects from its SWT parent, then uses them to implement an SWT 
 * virtual table control.
 * 
 * @author djo
 */
public class InternalCompositeTable extends Composite implements Listener {
	// The internal UI controls that make up this control.
	private Composite sliderHolder = null;
	private Composite controlHolder = null;
	private Slider slider = null;
	private EmptyTablePlaceholder emptyTablePlaceholder = null;

	// My parent CompositeTable
	private CompositeTable parent;
	
	// Property fields
	private int maxRowsVisible;
	private int numRowsInDisplay;
	private int numRowsInCollection;

	private int topRow;
	private int currentRow;
	private int currentColumn;

	// The visible/invisible row objects and bookeeping info about them
	private int currentVisibleTopRow = 0;
	private int numRowsVisible = 0;
	private LinkedList rows = new LinkedList();
	private LinkedList spareRows = new LinkedList();

	// The prototype header/row objects and Constructors so we can duplicate them
	private Constructor headerConstructor;
	private Constructor rowConstructor;
	private Control headerControl;
	private Control myHeader = null;
	private Control rowControl;
	
	/**
	 * Constructor InternalCompositeTable.  The usual SWT constructor.  The same style bits are
	 * allowed here as are allowed on Composite.
	 * 
	 * @param parentControl The SWT parent.
	 * @param style Style bits.
	 */
	public InternalCompositeTable(Composite parentControl, int style) {
		super(parentControl, style);
		initialize();
		
		this.parent = (CompositeTable) parentControl;
		
		setBackground(parentControl.getBackground());
		controlHolder.addListener(SWT.MouseWheel, this);

		maxRowsVisible = parent.getMaxRowsVisible();
		numRowsInCollection = parent.getNumRowsInCollection();
		topRow = parent.getTopRow();
		
		headerConstructor = parent.getHeaderConstructor();
		rowConstructor = parent.getRowConstructor();
		headerControl = parent.getHeaderControl();
		rowControl = parent.getRowControl();
		
		currentVisibleTopRow = topRow;
		showHeader();
		updateVisibleRows();
		
		if (numRowsVisible < 1) {
			createEmptyTablePlaceholer();
		}
	}

	public void setBackground(Color color) {
		super.setBackground(color);
		controlHolder.setBackground(color);
	}

	/**
	 * Initialize the overall table UI.
	 */
	private void initialize() {
		GridLayout gl = new GridLayout();
		gl.numColumns = 2;
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		gl.horizontalSpacing = 0;
		this.setLayout(gl);
		createControlHolder();
		createSliderHolder();
	}

	/**
	 * Initialize the controlHolder, which is the holder Composite for the header object (if
	 * applicable) and the row objects.
	 */
	private void createControlHolder() {
		GridData gridData = new org.eclipse.swt.layout.GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		controlHolder = new Composite(this, SWT.NONE);
		controlHolder.setLayoutData(gridData);
		controlHolder.setLayout(new Layout() {
			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
				if (rowControl != null) {
					int height = 0;
					int width = 0;
					if (headerControl != null) {
						Point headerSize = headerControl.getSize();
						width = headerSize.x;
						height = headerSize.y;
					}
					Point rowSize = rowControl.getSize();
					height += rowSize.y * 2;
					if (width < rowSize.x) {
						width = rowSize.x;
					}
					return new Point(height, width);
				}
				return new Point(50, 50);
			}
			protected void layout(Composite composite, boolean flushCache) {
				layoutControlHolder();
			}
		});
	}

	/**
	 * Initialize the sliderHolder and slider.  The SliderHolder is a Composite that is 
	 * responsible for showing and hiding the vertical slider upon request.
	 */
	private void createSliderHolder() {
		GridData gd = getSliderGridData();
		sliderHolder = new Composite(this, SWT.NONE);
		slider = new Slider(sliderHolder, SWT.VERTICAL);
		slider.addSelectionListener(sliderSelectionListener);
		sliderHolder.setLayout(new FillLayout());
		sliderHolder.setLayoutData(gd);
		sliderHolder.setTabList(new Control[] {});
	}

	// Slider utility methods ---------------------------------------------------------------------

	/**
	 * Returns a GridData for the SliderHolder appropriate for if the slider is visible or not.
	 * 
	 * @return A GridData with a widthHint of 0 if the slider is not visible or with a widthHint
	 * of SWT.DEFAULT otherwise.
	 */
	private GridData getSliderGridData() {
		GridData gd = new org.eclipse.swt.layout.GridData();
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gd.verticalSpan = 1;
		if (!sliderVisible) {
			gd.widthHint = 0;
		}
		gd.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
		return gd;
	}
	
	private boolean sliderVisible = false;
	
	/**
	 * Sets if the slider is visible or not.
	 * 
	 * @param visible true if the slider should be visible; false otherwise.
	 */
	public void setSliderVisible(boolean visible) {
		this.sliderVisible = visible;
		sliderHolder.setLayoutData(getSliderGridData());
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				sliderHolder.getParent().layout(true);
				sliderHolder.layout(true);
				Point sliderHolderSize = sliderHolder.getSize();
				slider.setBounds(0, 0, sliderHolderSize.x, sliderHolderSize.y);
			}
		});
	}
	
	/**
	 * Returns if the slider is visible.
	 * 
	 * @return true if the slider is visible; false otherwise.
	 */
	public boolean isSliderVisible() {
		return sliderVisible;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		disposeRows(rows);
		disposeRows(spareRows);
		super.dispose();
	}

	/**
	 * Disposes all the row objects in the specified LinkedList.
	 * 
	 * @param rowsCollection The collection containing TableRow objects to dispose.
	 */
	private void disposeRows(LinkedList rowsCollection) {
		for (Iterator rowsIter = rowsCollection.iterator(); rowsIter.hasNext();) {
			TableRow row = (TableRow) rowsIter.next();
			row.dispose();
		}
	}

	// Row object layout --------------------------------------------------------------------------
	
	/**
	 * Layout the child controls within the controlHolder Composite.
	 */
	protected void layoutControlHolder() {
		if (myHeader != null)
			layoutChild(myHeader);
		for (Iterator rowsIter = rows.iterator(); rowsIter.hasNext();) {
			TableRow row = (TableRow) rowsIter.next();
			layoutChild(row.getRowControl());
		}
		updateVisibleRows();
	}

	/**
	 * Layout a particular row or header control (child control of the controlHolder).
	 * If the child control has a layout manager, we delegate to that layout manager.
	 * Otherwise, we use the built in table layout manager.
	 * 
	 * @param child The row or header control to layout.
	 * @return height of child
	 */
	private int layoutChild(Control child) {
		if (child instanceof Composite) {
			Composite composite = (Composite) child;
			if (composite.getLayout() == null) {
				return parent.layoutHeaderOrRow(composite);
			}
			composite.layout(true);
			return composite.getSize().y;
		}
		return child.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).y;
	}

	// Table control layout -- utility methods ----------------------------------------------------

	/**
	 * Construct a header or row object on demand.  Logs an error and returns null on failure.
	 * 
	 * @param parent The SWT parent.
	 * @param constructor The header or row object's constructor.
	 * @return The constructed control or null if none could be constructed.
	 */
	private Control createInternalControl(Composite parent, Constructor constructor) {
		Control result = null;
		try {
			result = (Control) constructor.newInstance(new Object[] {parent, new Integer(SWT.NULL)});
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to construct control"); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * If the header control hasn't been created yet, create and show it.
	 */
	private void showHeader() {
		if (myHeader == null && headerConstructor != null) {
			myHeader = createInternalControl(controlHolder, headerConstructor);
			if (myHeader instanceof Composite) {
				Composite headerComp = (Composite) myHeader;
				if (headerComp.getLayout() == null) {
					headerComp.addPaintListener(headerPaintListener);
				}
			}
			layoutChild(myHeader);
		}
	}
	
	// Table control layout -- main refresh algorithm ---------------------------------------------

	/**
	 * Main refresh algorithm entry point.  This method refreshes everything in the table:
	 * 
	 * <ul>
	 * <li>Makes sure the correct number of rows are visible
	 * <li>Makes sure each row has been refreshed with data from the underlying model
	 * </ul>
	 */
	void updateVisibleRows() {
		// If we don't have our prototype row object yet, bail out
		if (rowControl == null) {
			return;
		}
		
		// Figure out how many rows we can stack vertically
		int clientAreaHeight = controlHolder.getSize().y;
		if (clientAreaHeight <= 0) {
			return;
		}
		
		int topPosition = 0;
		
		int headerHeight = 0;
		if (myHeader != null) {
			headerHeight = headerControl.getSize().y + 3;
			clientAreaHeight -= headerHeight;
			topPosition += headerHeight;
		}
		numRowsInDisplay = clientAreaHeight / rowControl.getSize().y;
		
		// Make sure we have something to lay out to begin with
		if (numRowsInCollection > 0) {
			numRowsVisible = numRowsInDisplay;
			
			disposeEmptyTablePlaceholder();
			
			int displayableRows = numRowsInCollection - topRow;
			if (numRowsVisible > displayableRows) {
				numRowsVisible = displayableRows;
			}
			if (numRowsVisible > maxRowsVisible) {
				numRowsVisible = maxRowsVisible;
			}
			if (numRowsVisible < 1) {
				numRowsVisible = 1;
			}
	
			// Scroll the view so that the right number of row
			// objects are showing and they have the right data
			if (rows.size() - Math.abs(currentVisibleTopRow - topRow) > 0) {
				if (currentRow >= numRowsVisible) {
					deleteRowAt(0);
					++currentVisibleTopRow;
					++topRow;
					--currentRow;
				}
				scrollTop();
				fixNumberOfRows();
			} else {
				currentVisibleTopRow = topRow;
				
				// The order of number fixing/refresh is important in order to
				// minimize the number of screen redraw operations
				if (rows.size() > numRowsVisible) {
					fixNumberOfRows();
					refreshAllRows();
				} else {
					refreshAllRows();
					fixNumberOfRows();
				}
			}
		} else {
			numRowsVisible = 0;
			topRow=0;
			currentRow=0;
			currentColumn=0;
			currentVisibleTopRow = 0;
			numRowsVisible = 0;
			
			if (emptyTablePlaceholder == null) {
				fixNumberOfRows();
				createEmptyTablePlaceholer();
			}
		}
		
		// Show, hide, reset the scroll bar
		if (numRowsVisible < numRowsInCollection) {
			int extra = numRowsInCollection - numRowsVisible;
			int pageIncrement = numRowsVisible;
			if (pageIncrement > extra)
				pageIncrement = extra;
			
			slider.setMaximum(numRowsInCollection);
			slider.setMinimum(0);
			slider.setIncrement(1);
			slider.setPageIncrement(pageIncrement);
			slider.setThumb(numRowsInCollection - (numRowsInCollection - numRowsVisible));
			
			slider.setSelection(topRow);
			
			if (!isSliderVisible()) {
				setSliderVisible(true);
			}
		} else {
			setSliderVisible(false);
		}

		// Lay out the header and rows correctly in the display
		int width = controlHolder.getSize().x;
		
		// First, the header...
		if (myHeader != null) {
			myHeader.setBounds(0, 0, width, headerHeight);
		}
		
		// Make sure we have rows to lay out...
		if (numRowsInCollection < 1) {
			return;
		}
		
		// Now the rows.
		int rowHeight = 50;
		rowHeight = rowControl.getSize().y;
		
		for (Iterator rowsIter = rows.iterator(); rowsIter.hasNext();) {
			TableRow row = (TableRow) rowsIter.next();
			Control rowControl = row.getRowControl();
			rowControl.setBounds(0, topPosition, width, rowHeight);
			layoutChild(rowControl);
			topPosition += rowHeight;
		}
	}

	/**
	 * Utility method: Makes sure that the currently visible top row is the same as the top row
	 * specified in the TopRow property.
	 */
	private void scrollTop() {
		while (currentVisibleTopRow < topRow) {
			deleteRowAt(0);
			++currentVisibleTopRow;
		}
		while (currentVisibleTopRow > topRow) {
			--currentVisibleTopRow;
			insertRowAt(0);
		}
	}

	/**
	 * Utility method: Makes sure that the number of rows that are visible correspond with
	 * what should be visible given the table control's size, where it is scrolled, and
	 * the number of rows in the underlying collection.
	 */
	private void fixNumberOfRows() {
		int numRows = rows.size();
		while (numRows > numRowsVisible) {
			deleteRowAt(numRows-1);
			numRows = rows.size();
		}
		while (numRows < numRowsVisible) {
			insertRowAt(numRows);
			numRows = rows.size();
		}
	}

	/**
	 * Fire the refresh event on all visible rows.
	 */
	void refreshAllRows() {
		int row=0;
		for (Iterator rowsIter = rows.iterator(); rowsIter.hasNext();) {
			TableRow rowControl = (TableRow) rowsIter.next();
			fireRefreshEvent(topRow + row, rowControl.getRowControl());
			++row;
		}
	}

	/**
	 * Insert a new row object at the specified 0-based position relatve to the topmost row.
	 * 
	 * @param position The 0-based position relative to the topmost row.
	 */
	private void insertRowAt(int position) {
		TableRow newRow = getNewRow();
		if (position > rows.size()) {
			position = rows.size();
		}
		rows.add(position, newRow);
		fireRefreshEvent(currentVisibleTopRow + position, newRow.getRowControl());
	}
	
	/**
	 * Delete the row at the specified 0-based position relative to the topmost row.
	 * 
	 * @param position The 0-based position relative to the topmost row.
	 */
	private void deleteRowAt(int position) {
		TableRow row = (TableRow) rows.remove(position);
		row.setVisible(false);
		spareRows.addLast(row);
	}
	
	/**
	 * Utility method: Creates a new row object or recycles one that had been previously 
	 * created but was no longer needed.
	 * 
	 * @return The new row object.
	 */
	private TableRow getNewRow() {
		if (spareRows.size() > 0) {
			TableRow recycledRow = (TableRow) spareRows.removeFirst();
			recycledRow.setVisible(true);
			return recycledRow;
		}
		TableRow newRow = new TableRow(this, createInternalControl(controlHolder, rowConstructor));
		fireRowConstructionEvent(newRow.getRowControl());
		if (newRow.getRowControl() instanceof Composite) {
			Composite rowComp = (Composite) newRow.getRowControl();
			if (rowComp.getLayout() == null) {
				rowComp.setBackground(getBackground());
				rowComp.addPaintListener(rowPaintListener);
			}
		}
		return newRow;
	}

	// Property getters/setters --------------------------------------------------------------


	/*
	 * These are internal API.
	 * <p>
	 * Plese refer to the JavaDoc on CompositeTable for detailed description of these property methods.
	 */
	
	/** (non-API)
	 * Method getHeaderControl.  Return the prototype control being used as a header.
	 * 
	 * @return The header control
	 */
	public Control getHeaderControl() {
		return headerControl;
	}

	/**
	 * Method setMaxRowsVisible.  Sets the maximum number of rows that will be permitted 
	 * in the table at once.  For example, setting this property to 1 will have the effect of 
	 * creating a single editing area with a scroll bar on the right allowing the user to scroll
	 * through all rows using either the mouse or the PgUp/PgDn keys.  The default value is
	 * Integer.MAX_VALUE.
	 * 
	 * @param maxRowsVisible the maximum number of rows that are permitted to be visible at one time, regardless
	 * of the control's size.
	 */
	public void setMaxRowsVisible(int maxRowsVisible) {
		this.maxRowsVisible = maxRowsVisible;
		updateVisibleRows();
	}

	/**
	 * Method getNumRowsVisible.  Returns the actual number of rows that are currently visible.
	 * Normally CompositeTable displays as many rows as will fit vertically given the control's
	 * size.  This value can be clamped to a maximum using the MaxRowsVisible property.
	 * 
	 * @return the actual number of rows that are currently visible.
	 */
	public int getNumRowsVisible() {
		return numRowsVisible;
	}
	
	/**
	 * Method setNumRowsInCollection.  Sets the number of rows in the data structure that is
	 * being edited.
	 * 
	 * @param numRowsInCollection the number of rows represented by the underlying data structure.
	 */
	public void setNumRowsInCollection(int numRowsInCollection) {
		this.topRow = 0;
		if (currentRow > 0) {
			currentRow = 0;
		}
		this.numRowsInCollection = numRowsInCollection;
		updateVisibleRows();
		refreshAllRows();
	}

	/**
	 * Method setTopRow. Set the number of the line that is being displayed in the top row
	 * of the CompositeTable editor (0-based).  If the new top row is not equal to the current
	 * top row, the table will automatically be scrolled to the new position.  This number must
	 * be greater than 0 and less than NumRowsInCollection.
	 * 
	 * @param topRow the line number of the new top row.
	 */
	public void setTopRow(int topRow) {
		fireRowDepartEvent();
		this.topRow = topRow;
		updateVisibleRows();
		fireRowArriveEvent();
	}
	
	/**
	 * Method getTopRow.  Return the number of the line that is being displayed in the top row
	 * of the CompositeTable editor (0-based).
	 * 
	 * @return the number of the top line.
	 */	
	public int getTopRow() {
		return topRow;
	}

	/**
	 * Method getSelection.  Returns the currently-selected (column, row) pair where the row 
	 * specifies the offset from the top of the table window.  In order to get the current 
	 * row in the underlying data structure, use getSelection().y + getCurrentRow().
	 * 
	 * @return  the currently-selected (column, row) pair where the row specifies the offset 
	 * from the top of the table window.
	 */
	public Point getSelection() {
		return new Point(currentColumn, currentRow);
	}

	/**
	 * Method setSelection.  Sets the currently-selected (column, row) pair where the row 
	 * specifies the offset from the top of the table window.  In order to get the current 
	 * row in the underlying data structure, use getSelection().y + getCurrentRow().
	 * 
	 * @param column the column to select
	 * @param row the row to select
	 */
	public void setSelection(int column, int row) {
		if (row == currentRow)
			internalSetSelection(column, row, false);
		else {
			if (fireRequestRowChangeEvent())
				internalSetSelection(column, row, true);
		}
	}

	/**
	 * Method setWeights.  Indicates that the column weights were just set and we should re-layout
	 * the control holder object.
	 */
	public void setWeights() {
		layoutControlHolder();
	}

	// Refresh Event API --------------------------------------------------------------------------

	/**
	 * Adds the specified listener to the set of listeners that will be notified when a row
	 * refresh event occurs.
	 * 
	 * @param listener the listener to add.
	 */
	public void addRefreshContentProvider(IRowContentProvider listener) {
		parent.contentProviders.add(listener);
	}

	/**
	 * Remove the specified listener from the set of listeners that will be notified when a
	 * row refresh event occurs.
	 * 
	 * @param listener the listener to remove.
	 */
	public void removeRefreshContentProvider(IRowContentProvider listener) {
		parent.contentProviders.remove(listener);
	}

	private void fireRefreshEvent(int positionInCollection, Control rowControl) {
		if (numRowsInCollection < 1) {
			return;
		}
		for (Iterator refreshListenersIter = parent.contentProviders.iterator(); refreshListenersIter.hasNext();) {
			IRowContentProvider listener = (IRowContentProvider) refreshListenersIter.next();
			listener.refresh(parent, positionInCollection, rowControl);
		}
	}
	
	
	// Empty table placeholder --------------------------------------------------------------------

	private void createEmptyTablePlaceholer() {
		emptyTablePlaceholder = new EmptyTablePlaceholder(controlHolder, SWT.NULL);
		if (rowControl != null)
			emptyTablePlaceholder.setBackground(rowControl.getBackground());
		emptyTablePlaceholder.setMessage(parent.getInsertHint());
	}
	
	private void disposeEmptyTablePlaceholder() {
		if (emptyTablePlaceholder != null) {
			emptyTablePlaceholder.dispose();
			emptyTablePlaceholder = null;
		}
	}

	// Event Handling -----------------------------------------------------------------------------

	private boolean needToRequestRC=true;
	
	/**
	 * Handle a keyPressed event on any row control.
	 * 
	 * @param sender The row that is sending the event
	 * @param e the actual KeyEvent
	 */
	public void keyPressed(TableRow sender, KeyEvent e) {
		if ((e.stateMask & SWT.CONTROL) != 0) {
			switch (e.keyCode) {
			case SWT.HOME:
				if (topRow <= 0) {
					return;
				}
				
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				deselect(e.widget);
				
				// If the focus is already in the top visible row, we will need to explicitly 
				// fire an arrive event.
				boolean needToArrive = true;
				if (currentRow > 0) {
					needToArrive = false;
				}
				
				setTopRow(0);
				
				if (needToArrive) {
					internalSetSelection(currentColumn, 0, true);
				} else {
					internalSetSelection(currentColumn, 0, false);
				}
				return;
			case SWT.END:
				if (topRow + numRowsVisible < numRowsInCollection) {
					if (!fireRequestRowChangeEvent()) {
						return;
					}
					needToRequestRC = false;
					
					deselect(e.widget);
					
					// If the focus is already in the last visible row, we will need to explicitly 
					// fire an arrive event.
					needToArrive = true;
					if (currentRow < numRowsVisible-1) {
						needToArrive = false;
					}
					
					setTopRow(numRowsInCollection - numRowsVisible);
					
					if (needToArrive) {
						internalSetSelection(currentColumn, numRowsVisible-1, true);
					} else {
						internalSetSelection(currentColumn, numRowsVisible-1, false);
					}
				}
				return;
			case SWT.INSERT:
				// If no insertHandler has been registered, bail out 
			 	if (parent.insertHandlers.size() < 1) {
					return;
				}
				
				// Make sure we can leave the current row
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				// Insert the new object
				int newRowPosition = fireInsertEvent();
				if (newRowPosition < 0) {
					// This should never happen, but...
					return;
				}
				
				disposeEmptyTablePlaceholder();
				
				// If the current widget has a selection, deselect it
				deselect(e.widget);
				
				// If the new row is in the visible space, refresh it
				if (topRow <= newRowPosition && 
						numRowsVisible > newRowPosition - topRow) {
					insertRowAt(newRowPosition - topRow);
					++numRowsInCollection;
					updateVisibleRows();
					int newRowNumber = newRowPosition - topRow;
					if (newRowNumber != currentRow) {
						internalSetSelection(currentColumn, newRowNumber, false);
					} else {
						internalSetSelection(currentColumn, newRowNumber, true);
					}
					return;
				}
				
				// else...
				
				++numRowsInCollection;

				// If the new row is above us, scroll up to it
				if (newRowPosition < topRow + currentRow) {
					setTopRow(newRowPosition);
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							updateVisibleRows();
							if (currentRow != 0) {
								internalSetSelection(currentColumn, 0, false);
							} else {
								internalSetSelection(currentColumn, 0, true);
							}
						}
					});
				} else {
					// If we're appending
					if (numRowsInDisplay > numRowsVisible) {
						updateVisibleRows();
						int newRowNumber = newRowPosition - topRow;
						if (newRowNumber != currentRow) {
							internalSetSelection(currentColumn, newRowNumber, false);
						} else {
							internalSetSelection(currentColumn, newRowNumber, true);
						}
					} else {
						// It's somewhere in the middle below us; scroll down to it
						setTopRow(newRowPosition-numRowsVisible+1);
						int newRowNumber = numRowsVisible-1;
						if (newRowNumber != currentRow) {
							internalSetSelection(currentColumn, newRowNumber, false);
						} else {
							internalSetSelection(currentColumn, newRowNumber, true);
						}
					}
				}
				return;
			case SWT.DEL:
				if (fireDeleteEvent()) {
					// We know the object is gone if we made it here, so now refresh the display...
					--numRowsInCollection;
					
					// If we deleted the last row in the list
					if (currentRow >= numRowsVisible-1) {
						// If that wasn't the last row in the collection, move the focus
						if (numRowsInCollection > 0) {

							// If we're only displaying one row, scroll first
							if (currentRow < 1) {
								needToRequestRC = false;
								deleteRowAt(currentRow);
								setTopRow(topRow-1);
								internalSetSelection(currentColumn, currentRow, true);
							} else {
								needToRequestRC = false;
								internalSetSelection(currentColumn, currentRow-1, false);
								Display.getCurrent().asyncExec(new Runnable() {
									public void run() {
										deleteRowAt(currentRow+1);
										updateVisibleRows();
									}
								});
							}
						} else {
							// Otherwise, show the placeholder object and give it focus
							deleteRowAt(currentRow);
							--numRowsVisible;
							createEmptyTablePlaceholer();
							emptyTablePlaceholder.setFocus();
						}
					} else {
						// else, keep the focus where it was
						deleteRowAt(currentRow);
						updateVisibleRows();
						internalSetSelection(currentColumn, currentRow, true);
					}
				}
				return;
			default:
				return;
			}
		}
		switch (e.keyCode) {
		case SWT.ARROW_UP:
			if (maxRowsVisible <= 1)
				return;
			
			if (currentRow > 0) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				deselect(e.widget);
				
				internalSetSelection(currentColumn, currentRow-1, false);
				return;
			}
			if (topRow > 0) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				deselect(e.widget);
				
				setTopRow(topRow - 1);
				internalSetSelection(currentColumn, currentRow, true);
				return;
			}
			return;
		case SWT.ARROW_DOWN:
			if (maxRowsVisible <= 1)
				return;
			
			if (currentRow < numRowsVisible-1) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				deselect(e.widget);
				
				internalSetSelection(currentColumn, currentRow+1, false);
				return;
			}
			if (topRow + numRowsVisible < numRowsInCollection) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				deselect(e.widget);
				
				setTopRow(topRow + 1);
				internalSetSelection(currentColumn, currentRow, true);
				return;
			}
			return;
		case SWT.PAGE_UP:
			if (topRow > 0) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				int newTopRow = topRow - numRowsInDisplay;
				if (newTopRow < 0) {
					newTopRow = 0;
				}
				setTopRow(newTopRow);
				internalSetSelection(currentColumn, currentRow, true);
			}
			return;
		case SWT.PAGE_DOWN:
			if (topRow + numRowsVisible < numRowsInCollection) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				needToRequestRC = false;
				
				int newTopRow = topRow + numRowsVisible;
				if (newTopRow >= numRowsInCollection - 1) {
					newTopRow = numRowsInCollection - 1;
				}
				setTopRow(newTopRow);
				if (currentRow < numRowsVisible) {
					internalSetSelection(currentColumn, currentRow, true);
				} else {
					internalSetSelection(currentColumn, numRowsVisible-1, true);
				}
			}
			return;
		}
	}

	/**
	 * Handle the keyTraversed event on any child control in the table.
	 * 
	 * @param sender The row sending the event.
	 * @param e The SWT TraverseEvent
	 */
	public void keyTraversed(TableRow sender, TraverseEvent e) {
		if (e.detail == SWT.TRAVERSE_TAB_NEXT) {
			if (currentColumn >= sender.getNumColumns() - 1) {
				e.detail = SWT.TRAVERSE_NONE;
				handleNextRowNavigation();
			}
		} else if (e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
			if (currentColumn == 0) {
				e.detail = SWT.TRAVERSE_NONE;
				handlePreviousRowNavigation(sender);
			}
		} else if (e.detail == SWT.TRAVERSE_RETURN) {
			e.detail = SWT.TRAVERSE_NONE;
			if (currentColumn >= sender.getNumColumns() - 1) {
				handleNextRowNavigation();
			} else {
				deferredSetFocus(getControl(currentColumn+1, currentRow), false);
			}
		}
	}

	/**
	 * The SelectionListener for the table's slider control.
	 */
	private SelectionListener sliderSelectionListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			if (slider.getSelection() == topRow) {
				return;
			}
			
			if (!fireRequestRowChangeEvent()) {
				slider.setSelection(topRow);
				return;
			}

			deselect(getControl(currentColumn, currentRow));
			
			setTopRow(slider.getSelection());
			deferredSetFocus(getControl(currentColumn, currentRow), true);
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	};
	
	/**
	 * Scroll wheel event handling.
	 */
	public void handleEvent(Event event) {
		
		if (event.count > 0) {
			if (topRow > 0) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				deselect(getControl(currentColumn, currentRow));
				setTopRow(topRow - 1);
				deferredSetFocus(getControl(currentColumn, currentRow), true);
			}
		} else {
			if (topRow < numRowsInCollection - numRowsVisible) {
				if (!fireRequestRowChangeEvent()) {
					return;
				}
				deselect(getControl(currentColumn, currentRow));
				setTopRow(topRow + 1);
				deferredSetFocus(getControl(currentColumn, currentRow), true);
			}
		}
	}
	
	/**
	 * Handle focusLost events on any child control.  This is not currently used.
	 * 
	 * @param sender The row containing the sending control.
	 * @param e The SWT FocusEvent.
	 */
	public void focusLost(TableRow sender, FocusEvent e) {
	}
	
	/**
	 * Handle focusGained events on any child control.
	 * 
	 * @param sender The row containing the sending control.
	 * @param e The SWT FocusEvent.
	 */
	public void focusGained(TableRow sender, FocusEvent e) {
		boolean rowChanged = false;
		if (getRowNumber(sender) != currentRow) {
			if (needToRequestRC) {
				if (!fireRequestRowChangeEvent()) {
					// Go back if we're not allowed to be here
					deferredSetFocus(getControl(currentColumn, currentRow), false);
				}
			} else {
				needToRequestRC = true;
			}
			rowChanged = true;
		}

		currentRow = getRowNumber(sender);
		currentColumn = sender.getColumnNumber((Control)e.widget);
		
		if (rowChanged)
			fireRowArriveEvent();
	}
	
	private PaintListener headerPaintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			if (parent.gridLinesOn) {
				drawGridLines(e, true);
			}
		}
	};
	
	private PaintListener rowPaintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			if (parent.gridLinesOn) {
				drawGridLines(e, false);
			}
		}
	};
	
	private void drawGridLines(PaintEvent e, boolean isHeader) {
		Color oldColor = e.gc.getForeground();
		try {
			// Get the colors we need
			Display display = Display.getCurrent();
			Color lineColor = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
			Color secondaryColor = display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
			Color hilightColor = display.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);
			if (!isHeader) {
				lineColor = display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
			}
			
			// Get the control
			Control toPaint = (Control) e.widget;
			Point controlSize = toPaint.getSize();
			
			// Draw the bottom line(s)
			e.gc.setForeground(lineColor);
			e.gc.drawLine(0, controlSize.y-1, controlSize.x, controlSize.y-1);
			if (isHeader) {
				e.gc.setForeground(secondaryColor);
				e.gc.drawLine(0, controlSize.y-2, controlSize.x, controlSize.y-2);
				e.gc.setForeground(hilightColor);
				e.gc.drawLine(0, 1, controlSize.x, 1);
			}
			
			// Now draw lines around the child controls, if there are any
			if (toPaint instanceof Composite) {
				Composite row = (Composite) toPaint;
				Control[] children = row.getChildren();
				for (int i = 0; i < children.length; i++) {
					Rectangle childBounds = children[i].getBounds();
					
					// Paint the beginning lines
					if (isHeader) {
						e.gc.setForeground(hilightColor);
						e.gc.drawLine(childBounds.x-2, 1, childBounds.x-2, controlSize.y-2);
					}
					
					// Paint the ending lines
					e.gc.setForeground(lineColor);
					int lineLeft = childBounds.x + childBounds.width+1;
					e.gc.drawLine(lineLeft, 0, lineLeft, controlSize.y);
					if (isHeader) {
						e.gc.setForeground(secondaryColor);
						e.gc.drawLine(lineLeft-1, 0, lineLeft-1, controlSize.y-1);
					}
				}
			}
		} finally {
			e.gc.setForeground(oldColor);
		}
	}
	
	// Event Firing -------------------------------------------------------------------------------

	/**
	 * Fire the row construction event
	 * 
	 * @param newControl The new row's SWT control
	 */
	private void fireRowConstructionEvent(Control newControl) {
		for (Iterator rowConstructionListenersIter = parent.rowConstructionListeners.iterator(); rowConstructionListenersIter.hasNext();) {
			IRowConstructionListener listener = (IRowConstructionListener) rowConstructionListenersIter.next();
			listener.rowConstructed(newControl);
		}
	}

	/**
	 * Indicate to listeners that the focus is arriving on the specified row
	 */
	private void fireRowArriveEvent() {
		if (rows.size() < 1) {
			return;
		}
		for (Iterator rowChangeListenersIter = parent.rowFocusListeners.iterator(); rowChangeListenersIter.hasNext();) {
			IRowFocusListener listener = (IRowFocusListener) rowChangeListenersIter.next();
			listener.arrive(parent, topRow+currentRow, currentRow().getRowControl());
		}
	}

	/**
	 * Request permission from all listeners to leave the current row.
	 * 
	 * @return true if all listeners permit the row change; false otherwise.
	 */
	private boolean fireRequestRowChangeEvent() {
		if (rows.size() < 1) {
			return true;
		}
		if (currentRow > rows.size()-1) {
			// (if the other row is already gone)
			return true;
		}
		for (Iterator rowChangeListenersIter = parent.rowFocusListeners.iterator(); rowChangeListenersIter.hasNext();) {
			IRowFocusListener listener = (IRowFocusListener) rowChangeListenersIter.next();
			if (!listener.requestRowChange(parent, topRow+currentRow, currentRow().getRowControl())) {
				return false;
			}
		}
		fireRowDepartEvent();
		return true;
	}

	/**
	 * Indicate to listeners that the focus is about to leave the current row.
	 */
	private void fireRowDepartEvent() {
		if (rows.size() < 1) {
			return;
		}
		for (Iterator rowChangeListenersIter = parent.rowFocusListeners.iterator(); rowChangeListenersIter.hasNext();) {
			IRowFocusListener listener = (IRowFocusListener) rowChangeListenersIter.next();
			listener.depart(parent, topRow+currentRow, currentRow().getRowControl());
		}
	}

	/**
	 * Request deletion of the current row from the underlying data structure.
	 * 
	 * @return true if the deletion was successful; false otherwise.
	 */
	private boolean fireDeleteEvent() {
		if (parent.deleteHandlers.size() < 1) {
			return false;
		}
		
		int absoluteRow = topRow + currentRow;
		for (Iterator deleteHandlersIter = parent.deleteHandlers.iterator(); deleteHandlersIter.hasNext();) {
			IDeleteHandler handler = (IDeleteHandler) deleteHandlersIter.next();
			if (!handler.canDelete(absoluteRow)) {
				return false;
			}
		}
		for (Iterator deleteHandlersIter = parent.deleteHandlers.iterator(); deleteHandlersIter.hasNext();) {
			IDeleteHandler handler = (IDeleteHandler) deleteHandlersIter.next();
			handler.deleteRow(absoluteRow);
		}
		return true;
	}
	
	/**
	 * Request that the model insert a new row into itself.
	 * 
	 * @return The 0-based offset of the new row from the start of the collection or -1 if a 
	 * new row could not be inserted.
	 */
	private int fireInsertEvent() {
		if (parent.insertHandlers.size() < 1) {
			return -1;
		}
		
		for (Iterator insertHandlersIter = parent.insertHandlers.iterator(); insertHandlersIter.hasNext();) {
			IInsertHandler handler = (IInsertHandler) insertHandlersIter.next();
			int resultRow = handler.insert(topRow+currentRow);
			if (resultRow >= 0) {
				return resultRow;
			}
		}
		
		return -1;
	}

	// Event Handling, utility methods ------------------------------------------------------------
	
	/**
	 * Set the widget's selection to an empty selection.
	 * 
	 * @param widget The widget to deselect
	 */
	private void deselect(Widget widget) {
		if (DuckType.instanceOf(ISelectableRegionControl.class, widget)) {
			ISelectableRegionControl control = (ISelectableRegionControl) DuckType.implement(ISelectableRegionControl.class, widget);
			control.setSelection(0, 0);
		}
	}
	
	/**
	 * Try to go to the next row in the collection.
	 */
	private void handleNextRowNavigation() {
		if (currentRow < numRowsVisible-1) {
			if (!fireRequestRowChangeEvent()) {
				return;
			}
			needToRequestRC = false;
			
			deselect(getControl(currentColumn, currentRow));
			
			deferredSetFocus(getControl(0, currentRow+1), false);
		} else {
			if (topRow + numRowsVisible >= numRowsInCollection) {
				// We're at the end; don't go anywhere
				return;
			}
			// We have to scroll forwards
			if (!fireRequestRowChangeEvent()) {
				return;
			}
			needToRequestRC = false;
			
			deselect(getControl(currentColumn, currentRow));
			
			setTopRow(topRow+1);
			deferredSetFocus(getControl(0, currentRow), true);
		}
	}

	/**
	 * Try to go to the previous row in the collection.
	 * 
	 * @param row The current table row.
	 */
	private void handlePreviousRowNavigation(TableRow row) {
		if (currentRow == 0) {
			if (topRow == 0) {
				// We're at the beginning of the table; don't go anywhere
				return;
			}
			// We have to scroll backwards
			if (!fireRequestRowChangeEvent()) {
				return;
			}
			needToRequestRC = false;
			
			deselect(getControl(currentColumn, currentRow));
			
			setTopRow(topRow-1);
			deferredSetFocus(getControl(row.getNumColumns()-1, 0), true);
		} else {
			if (!fireRequestRowChangeEvent()) {
				return;
			}
			needToRequestRC = false;
			
			deselect(getControl(currentColumn, currentRow));
			
			deferredSetFocus(getControl(row.getNumColumns()-1, currentRow-1), false);
		}
	}

	/**
	 * Gets the current TableRow.
	 * 
	 * @return the current TableRow
	 */
	private TableRow currentRow() {
		if (currentRow > rows.size()-1) {
			return null;
		}
		return (TableRow) rows.get(currentRow);
	}
	
	/**
	 * Returns the SWT control corresponding to the current row.
	 * 
	 * @return the current row control.
	 */
	public Control getCurrentRowControl() {
		TableRow currentRow = currentRow();
		if (currentRow == null) {
			return null;
		}
		return currentRow().getRowControl();
	}

	/**
	 * Returns the TableRow by the specified 0-based offset from the top visible row.
	 * 
	 * @param rowNumber 0-based offset of the requested fow starting from the top visible row.
	 * @return The corresponding TableRow or null if there is none.
	 */
	private TableRow getRowByNumber(int rowNumber) {
		if (rowNumber > rows.size()-1) {
			return null;
		}
		return (TableRow) rows.get(rowNumber);
	}
	
	/**
	 * Return the SWT control at (column, row), where row is a 0-based number starting
	 * from the top visible row.
	 * 
	 * @param column the 0-based column.
	 * @param row the 0-based row starting from the top visible row.
	 * @return the SWT control at (column, row)
	 */
	private Control getControl(int column, int row) {
		TableRow rowObject = getRowByNumber(row);
		if (rowObject == null) {
			throw new IndexOutOfBoundsException("Request for a nonexistent row"); //$NON-NLS-1$
		}
		Control result = rowObject.getColumnControl(column);
		return result;
	}

	/**
	 * Return the 0-based row number corresponding to a particular TableRow object.
	 * 
	 * @param row The TableRow to translate to row coordinates.
	 * @return the 0-based row number or -1 if the specified TableRow is not visible.
	 */
	private int getRowNumber(TableRow row) {
		int rowNumber = 0;
		for (Iterator rowIter = rows.iterator(); rowIter.hasNext();) {
			TableRow candidate = (TableRow) rowIter.next();
			if (candidate == row) {
				return rowNumber;
			}
			++rowNumber;
		}
		return -1;
	}

	/**
	 * Set the focus to the specified (column, row).  If rowChange is true, fire a
	 * row change event, otherwise be silent.
	 * 
	 * @param column The 0-based column to focus
	 * @param row The 0-based row to focus
	 * @param rowChange true if a row change event should be fired; false otherwise.
	 */
	private void internalSetSelection(int column, int row, boolean rowChange) {
		Control toFocus = getControl(column, row);
		deferredSetFocus(toFocus, rowChange);
	}

	/**
	 * Set the focus to the specified control after allowing all pending events to complete
	 * first.  If rowChange is true, fire a row arrive event after the focus has been set.
	 * 
	 * @param toFocus The SWT Control to focus
	 * @param rowChange true if the rowArrive event should be fired; false otherwise.
	 */
	private void deferredSetFocus(final Control toFocus, final boolean rowChange) {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				toFocus.setFocus();
				if (rowChange) {
					fireRowArriveEvent();
				}
			}
		});
	}

}
