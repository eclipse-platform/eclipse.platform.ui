package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * An abstract column layout data describing the information needed 
 * (by <code>TableLayout</code>) to properly lay out a table. 
 * <p>
 * This class is not intended to be subclassed outside the framework.
 * </p>
 */
public abstract class ColumnLayoutData {

	/** 
	 * Default width of a column (in pixels).
	 */
	public static final int MINIMUM_WIDTH = 20;
	
	/**
	 * The column's minimum width in pixels.
	 */
	public int minimumWidth;
		
	/**
	 * Indicates whether the column is resizable.
	 */
	public boolean resizable;
/**
 * Creates a new column layout data object.
 *
 * @param resizable <code>true</code> if the column is resizable, and <code>false</code> if not
 */
protected ColumnLayoutData(boolean resizable) {
	this.resizable = resizable;
}
}
