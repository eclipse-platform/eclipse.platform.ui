/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IDetailsPage {
/**
 * 
 * @param form
 */
	void initialize(IManagedForm form);
/**
 * 
 * @param parent
 */
	void createContents(Composite parent);
/**
 * 
 * @param selection
 */
	void inputChanged(IStructuredSelection selection);
/**
 * 
 *
 */
	void commit();
/**
 * 
 *
 */
	void setFocus();
/**
 * 
 *
 */
	void dispose();
/**
 * 
 * @return
 */
	boolean isDirty();
	boolean isStale();
/**
 * 
 *
 */
	void refresh();
}