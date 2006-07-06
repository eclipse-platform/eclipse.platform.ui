/*
 * Copyright (C) 2005 David Orme <djo@coconut-palm-software.com>
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Orme                  - Initial API and implementation
 *     Coconut Palm Software, Inc. - API cleanup
 */
package org.eclipse.jface.examples.databinding.compositetable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.jface.examples.databinding.compositetable.internal.IRowFocusListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;

/**
 * Class CompositeTable. n. (1) An SWT virtual table control that extends
 * Composite. (2) An SWT virtual table control that is composed of many
 * Composites, each representing a header or a row, one below the other.
 * <p>
 * 
 * CompositeTable is designed specifically to work nicely in the Eclipse Visual
 * Editor, but it is equally easy to use in hand-coded layouts.
 * <p>
 * 
 * <b>Synopsis:</b>
 * <p>
 * 
 * In order to edit anything, one must:
 * <p>
 * 
 * <ul>
 * <li>Extend Composite or Canvas and create an object that can be duplicated
 * to represent the rows in your table.
 * <li>Optionally, extend Composite or Canvas and create a header object in the
 * same way.
 * <li>If the canvas and row objects do not have a layout manager, the
 * CompositeTable will automatically supply one that lays out child controls in
 * a visual table. If they have a layout manager, CompositeTable will let them
 * use that.
 * <li>Create a CompositeTable object, either using VE or using hand-coded SWT.
 * <li>Drop the header (if applicable), then the row object on the
 * CompositeTable or simply write code that creates instances of these objects
 * in that order as child controls of your CompositeTable.
 * <li>Set the RunTime property to "true". Your control is now "live."
 * <li>Add a RowConstructionListener if you need to add event handlers to
 * individual row controls when a row is created.
 * <li>Add a RowContentProvider that knows how to put data into your row
 * object's controls on demand.
 * <li>Add a RowFocusListener to validate and save changed data.
 * <li>Set the NumRowsInCollection property to the number of rows in the
 * underlying data structure.
 * </ul>
 * 
 * Detailed description:
 * <p>
 * 
 * This control is designed to work inside of the Eclipse Visual Editor. To use
 * it, drop one on the design surface. (Even though it extends Canvas, it does
 * not make sense to put a layout manager on it.)
 * <p>
 * 
 * Next create one or two new custom controls by using the Visual Editor to
 * extend Composite. If you create one custom control, it will be used as the
 * prototype for all rows that will be displayed in the table. If you create
 * two, the first one will be used as a prototype for the header and the second
 * one will be used as a prototype for the rows.
 * <p>
 * 
 * If these custom controls are not given layout managers (null layout), then
 * CompositeTable will automatically detect this situation and will supply its
 * own layout manager that will automatically lay out the children of these
 * controls in columns to form a table. However, if you supply layout managers
 * for your header prototype and row prototype objects, CompositeTable will
 * respect your choice. If you use CompositeTable's built-in layout manager,
 * then the weights property will be used to determine what percentage of the
 * total width will be allocated to each column. If this property is not set or
 * if the sum of their elements does not equal 100, the columns are created as
 * equal sizes.
 * <p>
 * 
 * Once you have created your (optional) Header and Row custom controls, simply
 * drop them onto your CompositeTable control in VE. The first of these two
 * custom controls to be instantiated in your code will be interpreted by the
 * CompositeTable as the header control and the second will be interpreted as
 * the row control.
 * <p>
 * 
 * Now that you have defined the (optional) header and row, you can switch your
 * CompositeTable into run mode and use it. This is done by switching the
 * RunTime property to true.
 * <p>
 * 
 * Once in run mode, all of the CompositeTable's properties will be active. In
 * order to use it, set the NumRowsInCollection property to the number of rows
 * in the collection you want to display. And add a RefreshContentProvider,
 * which will be called whenever CompositeTable needs to refresh a particular
 * row.
 * <p>
 * 
 * Please refer to the remainder of the JavaDoc for information on the remaining
 * properties and events.
 * <p>
 * 
 * Although this control extends Composite, it is not intended to be subclassed
 * except within its own implementation and it makes no sense to set a layout
 * manager on it (although as discussed above, the child controls may have
 * layout managers).
 * 
 * @author djo
 * @since 3.2
 */
public class CompositeTable extends Canvas {

	// Property fields here
	private boolean runTime = false;

	private int[] weights = new int[0];

	private int numRowsInCollection = 0;

	private int maxRowsVisible = Integer.MAX_VALUE;

	// Private fields here
	private Constructor headerConstructor = null;

	private Control headerControl = null;

	private Constructor rowConstructor = null;

	private Control rowControl = null;

	// TODO: on public API methods that reference contentPane, make sure it's not null before doing anything
	private InternalCompositeTable contentPane = null;

	/**
	 * Constructor CompositeTable. Construct a CompositeTable control.
	 * CompositeTable accepts the same style bits as the SWT Canvas.
	 * 
	 * @param parent
	 *            The SWT parent control.
	 * @param style
	 *            Style bits. These are the same as Canvas
	 */
	public CompositeTable(Composite parent, int style) {
		super(parent, style);
		setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		setLayout(new Layout() {
			protected Point computeSize(Composite composite, int wHint,
					int hHint, boolean flushCache) {
				if (headerControl == null && rowControl == null) {
					return new Point(2, 20);
				}
				Point headerSize = new Point(0, 0);
				if (headerControl != null) {
					headerSize = headerControl.computeSize(SWT.DEFAULT,
							SWT.DEFAULT);
				}
				Point rowSize = new Point(0, 0);
				if (rowControl != null) {
					rowSize = rowControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				}
				Point result = new Point(Math.max(headerSize.x, rowSize.x),
						headerSize.y + rowSize.y);
				return result;
			}

			protected void layout(Composite composite, boolean flushCache) {
				resize();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
	 */
	public void setBackground(Color color) {
		super.setBackground(color);
		if (contentPane != null) {
			contentPane.setBackground(color);
		}
	}

	private int numChildrenLastTime = 0;

	/**
	 * (non-API) Method resize. Resize this table's contents. Called from within
	 * the custom layout manager.
	 */
	protected final void resize() {
		if (isRunTime()) {
			int childrenLength = getChildren().length;
			if (numChildrenLastTime != childrenLength) {
				resizeAndRecordPrototypeRows();
				showPrototypes(false);
				contentPane.dispose();
				contentPane = new InternalCompositeTable(this, SWT.NULL);
			}
			updateVisibleRows();
		} else {
			resizeAndRecordPrototypeRows();
		}
	}

	/**
	 * (non-API) Method updateVisibleRows. Makes sure that the content pane is
	 * displaying the correct number of visible rows given the control's size.
	 * Called from within #resize.
	 */
	protected void updateVisibleRows() {
		if (contentPane == null) {
			switchToRunMode();
		}
		Point size = getSize();
		contentPane.setBounds(0, 0, size.x, size.y);
	}

	/**
	 * Switch from design mode where prototype header/row objects can be dropped
	 * on the control into run mode where all of the properties do what you
	 * would expect.
	 */
	private void switchToRunMode() {
		showPrototypes(false);
		contentPane = new InternalCompositeTable(this, SWT.NULL);
	}

	/**
	 * Switch back to design mode so that the prototype header/row objects may
	 * be manipulated directly in a GUI design tool.
	 */
	private void switchToDesignMode() {
		contentPane.dispose();
		contentPane = null;
		showPrototypes(true);
		resizeAndRecordPrototypeRows();
	}

	/**
	 * Turns display of the prototype objects on or off.
	 * 
	 * @param newValue
	 *            true of the prototype objects should be displayed; false
	 *            otherwise.
	 */
	private void showPrototypes(boolean newValue) {
		if (headerControl != null) {
			headerControl.setVisible(newValue);
		}
		if (rowControl != null) {
			rowControl.setVisible(newValue);
		}
	}

	/**
	 * (non-API) Method resizeAndRecordPrototypeRows. Figure out what child
	 * controls are the header and row prototype rows respectively and resize
	 * them so they occupy the entire width and their preferred height.
	 */
	protected void resizeAndRecordPrototypeRows() {
		Control[] children = getChildren();
		ArrayList realChildren = new ArrayList();
		Control[] finalChildren = children;

		// Find first two prototypes
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof InternalCompositeTable) {
				continue;
			}
			if (realChildren.size() < 2) {
				realChildren.add(children[i]);
			}
		}
		finalChildren = (Control[]) realChildren
				.toArray(new Control[realChildren.size()]);

		if (finalChildren.length == 0) {
			headerConstructor = null;
			headerControl = null;
			rowConstructor = null;
			rowControl = null;
			return;
		}

		// Get a constructor for the header and/or the row prototype
		headerConstructor = null;
		headerControl = null;
		rowConstructor = null;
		rowControl = null;

		if (finalChildren.length == 1) {
			try {
				rowControl = (Composite) finalChildren[0];
				rowConstructor = finalChildren[0].getClass().getConstructor(
						new Class[] { Composite.class, Integer.TYPE });
			} catch (Exception e) {
				throw new RuntimeException("Unable to get constructor object for header or row", e);
			}
		} else {
			try {
				headerConstructor = finalChildren[0].getClass().getConstructor(
						new Class[] { Composite.class, Integer.TYPE });
				headerControl = finalChildren[0];
				rowConstructor = finalChildren[1].getClass().getConstructor(
						new Class[] { Composite.class, Integer.TYPE });
				rowControl = finalChildren[1];
			} catch (Exception e) {
				throw new RuntimeException("Unable to get constructor object for header or row", e);
			}
		}

		// Now actually resize the children
		int top = 0;
		int width = getSize().x;
		for (int i = 0; i < finalChildren.length; ++i) {
			int height = 50;
			if (finalChildren[i] instanceof Composite) {
				Composite child = (Composite) finalChildren[i];
				if (child.getLayout() == null) {
					height = layoutHeaderOrRow(child, i==0);	// The 0th element is the header
				} else {
					height = finalChildren[i].computeSize(SWT.DEFAULT,
							SWT.DEFAULT).y;
				}
			} else {
				height = finalChildren[i].computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			}

			finalChildren[i].setBounds(0, top, width, height);
			top += height;
		}

		numChildrenLastTime = children.length;
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				if (!CompositeTable.this.isDisposed()) {
					if (!getParent().isDisposed()) {
						getParent().layout(true);
					}
				}
			}
		});
	}

	/**
	 * (non-API) Method layoutHeaderOrRow. If a header or row object does not
	 * have a layout manager, this method will automatically be called to layout
	 * the child controls of that header or row object.
	 * 
	 * @param child
	 *            The child object to layout.
	 * @param isHeader If we're laying out a header or a row object
	 * @return the height of the header or row
	 */
	int layoutHeaderOrRow(Composite child, boolean isHeader) {
		Control[] children = child.getChildren();
		if (children.length == 0) {
			return 50;
		}
		int[] weights = this.weights;
		weights = checkWeights(weights, children.length);

		int maxHeight = 0;
		for (int i = 0; i < children.length; i++) {
			int height = computeDesiredHeight(children[i], isHeader);
			if (maxHeight < height) {
				maxHeight = height;
			}
		}
		++maxHeight;

		int widthRemaining = child.getParent().getSize().x;
		int totalSize = widthRemaining;
		for (int i = 0; i < children.length - 1; i++) {
			int left = totalSize - widthRemaining;
			int desiredHeight = computeDesiredHeight(children[i], isHeader);
			int top = maxHeight - desiredHeight - 1;
			int width = (int) (((float) weights[i]) / 100 * totalSize);
			children[i].setBounds(left + 2, top, width - 4, desiredHeight);
			widthRemaining -= width;
		}

		int left = totalSize - widthRemaining;
		int desiredHeight = computeDesiredHeight(children[children.length - 1], isHeader);
		int top = maxHeight - desiredHeight - 1;
		children[children.length - 1].setBounds(left + 2, top,
				widthRemaining - 4, desiredHeight);

		return maxHeight;
	}
	
	int computeDesiredHeight(Control control, boolean isHeader) {
		int rowControlHeight = control.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, false).y;
		if (maxRowsVisible == Integer.MAX_VALUE || isHeader) {
			return rowControlHeight;
		}
		if (contentPane != null && fittingVertically && isRunTime()) {
			// FIXME: Yuck: bad code smell here...  (coupling with contentPane)
			int fitControlHeight = contentPane.clientAreaHeight / maxRowsVisible;
			if (fitControlHeight < rowControlHeight) {
				return rowControlHeight;
			}
			return fitControlHeight-2;
		}
		return rowControlHeight;
	}

	/**
	 * Compute and return a weights array where each weight is the percentage
	 * the corresponding column should occupy of the entire control width. If
	 * the elements in the supplied weights array add up to 100 and the length
	 * of the array is the same as the number of columns, the supplied weights
	 * array is used. Otherwise, this method computes and returns a weights
	 * array that makes each column an equal size.
	 * 
	 * @param weights
	 *            The default or user-supplied weights array.
	 * @param numChildren
	 *            The number of child controls.
	 * @return The weights array that will be used by the layout manager.
	 */
	private int[] checkWeights(int[] weights, int numChildren) {
		if (weights.length == numChildren) {
			int sum = 0;
			for (int i = 0; i < weights.length; i++) {
				sum += weights[i];
			}
			if (sum == 100) {
				return weights;
			}
		}

		// Either the number of weights doesn't match or they don't add up.
		// Compute something sane and return that instead.
		int[] result = new int[numChildren];
		int weight = 100 / numChildren;
		int extra = 100 % numChildren;
		for (int i = 0; i < result.length - 1; i++) {
			result[i] = weight;
			if (extra > 0) {
				result[i]++;
				extra--;
			}
		}
		result[numChildren - 1] = weight + extra;
		return result;
	}

	/**
	 * Method isRunTime. Returns if the CompositeTable is in run time mode as
	 * opposed to design time mode. In design time mode, the only permitted
	 * operations are to add or remove child Composites to be used as the header
	 * and/or row prototype objects.
	 * 
	 * @return true if this CompositeTable is in run mode. false otherwise.
	 */
	public boolean isRunTime() {
		return runTime;
	}

	/**
	 * Method setRunTime. Turns run-time mode on or off. When run-time mode is
	 * off, CompositeTable ignores most property operations and will accept
	 * prototype child controls to be added. When run-time mode is on, the
	 * prototype controls are interpreted and all properties become active.
	 * 
	 * @param runTime
	 *            true if run-time mode should be enabled; false otherwise.
	 */
	public void setRunTime(boolean runTime) {
		if (this.runTime != runTime) {
			this.runTime = runTime;
			if (runTime) {
				if (rowControl == null) {
					resizeAndRecordPrototypeRows();
				}
				switchToRunMode();
			} else {
				switchToDesignMode();
			}
		}
	}

	/**
	 * Method getWeights. Returns an array representing the percentage of the
	 * total width each column is allocated or null if no weights have been
	 * specified. This property is ignored if the programmer has set a layout
	 * manager on the header and/or the row prototype objects.
	 * 
	 * @return the current weights array or null if no weights have been
	 *         specified.
	 */
	public int[] getWeights() {
		return weights;
	}

	/**
	 * Method setWeights. Specifies the percentage of the total width that will
	 * be allocated to each column. This property is ignored if the programmer
	 * has set a layout manager on the header and/or the row prototype objects.
	 * <p>
	 * 
	 * The number of elements in the array must match the number of columns and
	 * the sum of all elements must equal 100. If either of these constraints is
	 * not true, this property will be ignored and all columns will be created
	 * equal in width.
	 * 
	 * @param weights
	 *            the weights to use if the CompositeTable is automatically
	 *            laying out controls.
	 */
	public void setWeights(int[] weights) {
		this.weights = weights;
		if (contentPane != null) {
			contentPane.setWeights();
		}
	}

	boolean linesVisible = true;

	/**
	 * Method getLinesVisible. Returns if the CompositeTable will draw grid lines
	 * on the header and row Composite objects. This property is ignored if the
	 * programmer has set a layout manager on the header and/or the row
	 * prototype objects.
	 * 
	 * @return true if the CompositeTable will draw grid lines; false otherwise.
	 */
	public boolean getLinesVisible() {
		return linesVisible;
	}

	/**
	 * Method setLinesVisible. Sets if the CompositeTable will draw grid lines on
	 * the header and row Composite objects. This property is ignored if the
	 * programmer has set a layout manager on the header and/or the row
	 * prototype objects.
	 * 
	 * @param linesVisible
	 *            true if the CompositeTable will draw grid lines; false
	 *            otherwise.
	 */
	public void setLinesVisible(boolean linesVisible) {
		this.linesVisible = linesVisible;
	}

	String insertHint = "Press <Ctrl-INSERT> to insert new data."; //$NON-NLS-1$

	/**
	 * Returns the hint string that will be displayed when there are no rows in
	 * the table.
	 * 
	 * @return The hint string that will be displayed when there are no rows in
	 *         the table.
	 */
	public String getInsertHint() {
		return insertHint;
	}

	/**
	 * Sets the hint string that will be displayed when there are no rows in the
	 * table. The default value is "Press &lt;INS> to insert a new row."
	 * 
	 * @param newHint
	 */
	public void setInsertHint(String newHint) {
		this.insertHint = newHint;
	}

	/**
	 * Method getMaxRowsVisible. Returns the maximum number of rows that will be
	 * permitted in the table at once. For example, setting this property to 1
	 * will have the effect of creating a single editing area with a scroll bar
	 * on the right allowing the user to scroll through all rows using either
	 * the mouse or the PgUp/PgDn keys. The default value is Integer.MAX_VALUE.
	 * 
	 * @return the maximum number of rows that are permitted to be visible at
	 *         one time, regardless of the control's size.
	 */
	public int getMaxRowsVisible() {
		return maxRowsVisible;
	}

	/**
	 * Method setMaxRowsVisible. Sets the maximum number of rows that will be
	 * permitted in the table at once. For example, setting this property to 1
	 * will have the effect of creating a single editing area with a scroll bar
	 * on the right allowing the user to scroll through all rows using either
	 * the mouse or the PgUp/PgDn keys. The default value is Integer.MAX_VALUE.
	 * 
	 * @param maxRowsVisible
	 *            the maximum number of rows that are permitted to be visible at
	 *            one time, regardless of the control's size.
	 */
	public void setMaxRowsVisible(int maxRowsVisible) {
		this.maxRowsVisible = maxRowsVisible;
		if (contentPane != null) {
			contentPane.setMaxRowsVisible(maxRowsVisible);
		}
	}

	private boolean fittingVertically = false;

	/**
	 * Method isFittingVertically. Returns if the CompositeTable control will
	 * scale the height of all rows so that maxRowsVisible rows exactly fits
	 * vertically in the available space.
	 * 
	 * @return true if the visible rows will be scaled; false otherwise.
	 */
	public boolean isFittingVertically() {
		return fittingVertically;
	}

	/**
	 * Method setFittingVertically. Sets if the CompositeTable will scale the
	 * visible rows sot that maxRowsVisible rows will exactly fit vertically in
	 * the available space.
	 * 
	 * @param fittingVertically
	 *            true if the visible rows will be scaled; false otherwise.
	 */
	public void setFittingVertically(boolean fittingVertically) {
		this.fittingVertically = fittingVertically;
		if (contentPane != null) {
			contentPane.setFittingVertically(fittingVertically);
		}
	}

	/**
	 * Method getNumRowsVisible. Returns the actual number of rows that are
	 * currently visible. Normally CompositeTable displays as many rows as will
	 * fit vertically given the control's size. This value can be clamped to a
	 * maximum using the MaxRowsVisible property.
	 * 
	 * @return the actual number of rows that are currently visible.
	 */
	public int getNumRowsVisible() {
		if (contentPane != null)
			return contentPane.getNumRowsVisible();

		return -1;
	}

	/**
	 * Method getNumRowsInCollection. Returns the number of rows in the data
	 * structure that is being edited.
	 * 
	 * @return the number of rows in the underlying data structure.
	 */
	public int getNumRowsInCollection() {
		return numRowsInCollection;
	}

	/**
	 * Method setNumRowsInCollection. Sets the number of rows in the data
	 * structure that is being edited.
	 * 
	 * @param numRowsInCollection
	 *            the number of rows represented by the underlying data
	 *            structure.
	 */
	public void setNumRowsInCollection(int numRowsInCollection) {
		this.numRowsInCollection = numRowsInCollection;
		if (contentPane != null) {
			contentPane.setNumRowsInCollection(numRowsInCollection);
		}
	}

	private int topRow = 0;

	/**
	 * Method getTopRow. Return the number of the line that is being displayed
	 * in the top row of the CompositeTable editor (0-based).
	 * 
	 * @return the number of the top line.
	 */
	public int getTopRow() {
		if (contentPane != null) {
			return contentPane.getTopRow();
		}
		return topRow;
	}

	/**
	 * Method setTopRow. Set the number of the line that is being displayed in
	 * the top row of the CompositeTable editor (0-based). If the new top row is
	 * not equal to the current top row, the table will automatically be
	 * scrolled to the new position. This number must be greater than 0 and less
	 * than NumRowsInCollection.
	 * 
	 * @param topRow
	 *            the line number of the new top row.
	 */
	public void setTopRow(int topRow) {
		this.topRow = topRow;
		if (contentPane != null) {
			contentPane.setTopRow(topRow);
		}
	}

	/**
	 * Makes sure that the focused row is visible
	 * 
	 * @return true if the display needed to be scrolled; false otherwise
	 */
	public boolean makeFocusedRowVisible() {
		if (contentPane != null) {
			return contentPane.makeFocusedRowVisible();
		}
		return false;
	}
	
	/**
	 * Method refreshAllRows. Refresh all visible rows in the CompositeTable
	 * from the original data.
	 */
	public void refreshAllRows() {
		if (contentPane != null) {
			contentPane.updateVisibleRows();
			contentPane.refreshAllRows();
		}
	}

	/**
	 * Method getCurrentColumn. Returns the column number of the
	 * currently-focused column (0-based).
	 * 
	 * @return the column number of the currently-focused column.
	 */
	public int getCurrentColumn() {
		if (contentPane == null) {
			return -1;
		}
		return getSelection().x;
	}

	/**
	 * Method setCurrentColumn. Sets the column number of the currently-focused
	 * column (0-based).
	 * 
	 * @param column
	 *            The new column to focus.
	 */
	public void setCurrentColumn(int column) {
		setSelection(column, getCurrentRow());
	}

	/**
	 * Method getCurrentRow. Returns the current row number as an offset from
	 * the top of the table window. In order to get the current row in the
	 * underlying data structure, compute getTopRow() + getCurrentRow().
	 * 
	 * @return the current row number as an offset from the top of the table
	 *         window.
	 */
	public int getCurrentRow() {
		if (contentPane == null) {
			return -1;
		}
		return getSelection().y;
	}

	/**
	 * Method setCurrentRow. Sets the current row number as an offset from the
	 * top of the table window. In order to get the current row in the
	 * underlying data structure, compute getTopRow() + getCurrentRow().
	 * 
	 * @param row
	 *            the current row number as an offset from the top of the table
	 *            window.
	 */
	public void setCurrentRow(int row) {
		setSelection(getCurrentColumn(), row);
	}

	/**
	 * Method getCurrentRowControl. Returns the SWT control that displays the
	 * current row.
	 * 
	 * @return Control the current row control.
	 */
	public Control getCurrentRowControl() {
		return contentPane.getCurrentRowControl();
	}
	
	/**
	 * Method getRowControls. Returns an array of SWT controls where each
	 * control represents a row control in the CompositeTable's current scrolled
	 * position. If CompositeTable is resized, scrolled, such that the rows that
	 * the CompositeTable control is displaying change in any way, the array
	 * that is returned by this method will become out of date and need to be
	 * retrieved again.
	 * 
	 * @return Control[] An array of SWT Control objects, each representing an
	 *         SWT row object.
	 */
	public Control[] getRowControls() {
		return contentPane.getRowControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setMenu(org.eclipse.swt.widgets.Menu)
	 */
	public void setMenu(Menu menu) {
		super.setMenu(menu);
		if (contentPane != null) {
			contentPane.setMenu(menu);
		}
	}
	
	/**
	 * Method getControlRow.  Given a row control, returns its row number
	 * relative to the topRow.
	 * 
	 * @param rowControl The row object to find
	 * @return The row number of the rowControl relative to the topRow (0-based)
	 * @throws IllegalArgumentException if rowControl is not currently visible
	 */
	public int getControlRow(Control rowControl) {
		return contentPane.getControlRow(rowControl);
	}

	/**
	 * Method getSelection. Returns the currently-selected (column, row) pair
	 * where the row specifies the offset from the top of the table window. In
	 * order to get the current row in the underlying data structure, use
	 * getSelection().y + getTopRow().
	 * 
	 * @return the currently-selected (column, row) pair where the row specifies
	 *         the offset from the top of the table window.
	 */
	public Point getSelection() {
		if (contentPane == null) {
			return null;
		}
		return contentPane.getSelection();
	}

	/**
	 * Method setSelection. Sets the currently-selected (column, row) pair where
	 * the row specifies the offset from the top of the table window. In order
	 * to get the current row in the underlying data structure, use
	 * getSelection().y + getCurrentRow().
	 * 
	 * @param selection
	 *            the (column, row) to select
	 */
	public void setSelection(Point selection) {
		setSelection(selection.x, selection.y);
	}

	/**
	 * Method setSelection. Sets the currently-selected (column, row) pair where
	 * the row specifies the offset from the top of the table window. In order
	 * to get the current row in the underlying data structure, use
	 * getSelection().y + getCurrentRow().
	 * 
	 * @param column
	 *            the column to select
	 * @param row
	 *            the row to select as an offset from the top of the window
	 */
	public void setSelection(int column, int row) {
		if (contentPane == null) {
			return;
		}
		contentPane.setSelection(column, row);
	}

	/**
	 * (non-API) Method getHeaderConstructor. Returns the Constructor object
	 * used internally to construct the table's header or null if there is none.
	 * 
	 * @return the header's constructor.
	 */
	public Constructor getHeaderConstructor() {
		return headerConstructor;
	}

	/**
	 * (non-API) Method getRowConstructor. Returns the Constructor object used
	 * internally to construct each row object.
	 * 
	 * @return the rows' constructor
	 */
	public Constructor getRowConstructor() {
		return rowConstructor;
	}

	/**
	 * (non-API) Method getHeaderControl. Returns the prototype header control.
	 * 
	 * @return the prototype header control.
	 */
	public Control getHeaderControl() {
		return headerControl;
	}

	/**
	 * (non-API) Method getRowControl. Returns the prototype row control.
	 * 
	 * @return the prototype row control.
	 */
	public Control getRowControl() {
		return rowControl;
	}

	LinkedList contentProviders = new LinkedList();

	/**
	 * Method addRowContentProvider. Adds the specified content provider to the
	 * list of content providers that will be called when a row needs to be
	 * filled with data. Most of the time it only makes sense to add a single
	 * one.
	 * 
	 * @param contentProvider
	 *            The content provider to add.
	 */
	public void addRowContentProvider(IRowContentProvider contentProvider) {
		contentProviders.add(contentProvider);
	}

	/**
	 * Method removeRowContentProvider. Removes the specified content provider
	 * from the list of content providers that will be called when a row needs
	 * to be filled with data.
	 * 
	 * @param contentProvider
	 *            The content provider to remove.
	 */
	public void removeRowContentProvider(IRowContentProvider contentProvider) {
		contentProviders.remove(contentProvider);
	}

	LinkedList rowFocusListeners = new LinkedList();

	/**
	 * Method addRowListener. Adds the specified listener to the set of
	 * listeners that will be notified when the user wishes to leave a row and
	 * when the user has already left a row. If any listener vetos leaving a
	 * row, the focus remains in the row.
	 * 
	 * @param rowListener
	 *            The listener to add.
	 */
	public void addRowFocusListener(IRowFocusListener rowListener) {
		rowFocusListeners.add(rowListener);
	}

	/**
	 * Method removeRowListener. Removes the specified listener from the set of
	 * listeners that will be notified when the user wishes to leave a row and
	 * when the user has already left a row. If any listener vetos leaving a
	 * row, the focus remains in the row.
	 * 
	 * @param listener
	 *            The listener to remove.
	 */
	public void removeRowFocusListener(IRowFocusListener listener) {
		rowFocusListeners.remove(listener);
	}

	LinkedList insertHandlers = new LinkedList();

	/**
	 * Method addInsertHandler. Adds the specified insertHandler to the set of
	 * objects that will be used to handle insert requests.
	 * 
	 * @param insertHandler
	 *            the insertHandler to add.
	 */
	public void addInsertHandler(IInsertHandler insertHandler) {
		insertHandlers.add(insertHandler);
	}

	/**
	 * Method removeInsertHandler. Removes the specified insertHandler from the
	 * set of objects that will be used to handle insert requests.
	 * 
	 * @param insertHandler
	 *            the insertHandler to remove.
	 */
	public void removeInsertHandler(IInsertHandler insertHandler) {
		insertHandlers.remove(insertHandler);
	}

	LinkedList deleteHandlers = new LinkedList();

	/**
	 * Method addDeleteHandler. Adds the specified deleteHandler to the set of
	 * objects that will be used to handle delete requests.
	 * 
	 * @param deleteHandler
	 *            the deleteHandler to add.
	 */
	public void addDeleteHandler(IDeleteHandler deleteHandler) {
		deleteHandlers.add(deleteHandler);
	}

	/**
	 * Method removeDeleteHandler. Removes the specified deleteHandler from the
	 * set of objects that will be used to handle delete requests.
	 * 
	 * @param deleteHandler
	 *            the deleteHandler to remove.
	 */
	public void removeDeleteHandler(IDeleteHandler deleteHandler) {
		deleteHandlers.remove(deleteHandler);
	}

	LinkedList rowConstructionListeners = new LinkedList();

	/**
	 * Method addRowConstructionListener. Adds the specified
	 * rowConstructionListener to the set of objects that will be used to listen
	 * to row construction events.
	 * 
	 * @param rowConstructionListener
	 *            the rowConstructionListener to add.
	 */
	public void addRowConstructionListener(
			RowConstructionListener rowConstructionListener) {
		rowConstructionListeners.add(rowConstructionListener);
	}

	/**
	 * Method removeRowConstructionListener. Removes the specified
	 * rowConstructionListener from the set of objects that will be used to
	 * listen to row construction events.
	 * 
	 * @param rowConstructionListener
	 *            the rowConstructionListener to remove.
	 */
	public void removeRowConstructionListener(
			RowConstructionListener rowConstructionListener) {
		rowConstructionListeners.remove(rowConstructionListener);
	}

	boolean deleteEnabled = true;

	/**
	 * Method isDeleteEnabled. Returns if delete is enabled. Deletions are only
	 * processed if the DeleteEnabled property is true and at least one delete
	 * handler has been registered.
	 * <p>
	 * 
	 * The default value is true.
	 * 
	 * @return true if delete is enabled. false otherwise.
	 */
	public boolean isDeleteEnabled() {
		return deleteEnabled;
	}

	/**
	 * Method setDeleteEnabled. Sets if delete is enabled. Deletions are only
	 * processed if the DeleteEnabled property is true and at least one delete
	 * handler has been registered.
	 * <p>
	 * 
	 * The default value is true.
	 * 
	 * @param deleteEnabled
	 *            true if delete should be enabled. false otherwise.
	 */
	public void setDeleteEnabled(boolean deleteEnabled) {
		this.deleteEnabled = deleteEnabled;
	}
	
	LinkedList scrollListeners = new LinkedList();
	
	/**
	 * Method addScrollListener.  Adds the specified scroll listener to the
	 * list of listeners that will be notified when this CompositeTable control
	 * scrolls the top-visible row.  This event is not fired when the 
	 * CompositeTable is resized.
	 * 
	 * @param scrollListener the ScrollListener to add.
	 */
	public void addScrollListener(ScrollListener scrollListener) {
		scrollListeners.add(scrollListener);
	}
	
	/**
	 * Method removeScrollListener.  Removes the specified scroll listener from the
	 * list of listeners that will be notified when this CompositeTable control
	 * scrolls the top-visible row.
	 * 
	 * @param scrollListener the ScrollListener to remove.
	 */
	public void removeScrollListener(ScrollListener scrollListener) {
		scrollListeners.remove(scrollListener);
	}

	private boolean traverseOnTabsEnabled = true;
	
	/**
	 * Method isTraverseOnTabsEnabled. Returns true if Tab and Shift-tab cause
	 * the focus to wrap from the end of the table back to the beginning and
	 * Enter causes the focus to advance. Returns false otherwise.
	 * <p>
	 * This property defaults to true.
	 * 
	 * @return true if CompositeTable is handling Tab, Shift-tab, and Enter key
	 *         behavior; false otherwise.
	 */
	public boolean isTraverseOnTabsEnabled() {
		return traverseOnTabsEnabled;
	}

	/**
	 * Method setTraverseOnTabsEnabled. Sets if Tab and Shift-tab cause
	 * the focus to wrap from the end of the table back to the beginning and
	 * Enter causes the focus to advance.
	 * <p>
	 * This property defaults to true.
	 * 
	 * @param enabled true if CompositeTable is handling Tab, Shift-tab, and Enter key
	 *         behavior; false otherwise.
	 */
	public void setTraverseOnTabsEnabled(boolean enabled) {
		this.traverseOnTabsEnabled = enabled;
	}

} // @jve:decl-index=0:visual-constraint="10,10"

