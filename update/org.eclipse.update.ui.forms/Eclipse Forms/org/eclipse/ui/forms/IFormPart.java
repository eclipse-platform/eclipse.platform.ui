/*
 * Created on Dec 4, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms;

/**
 * Classes that take part 
 */
public interface IFormPart {
/**
 * Initializes the part.
 * @param form
 */
	void initialize(ManagedForm form);
/**
 *
 */
	void dispose();
/**
 * 
 * @param onSave
 */
	void commit(boolean onSave);
}