package org.eclipse.jface.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
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
