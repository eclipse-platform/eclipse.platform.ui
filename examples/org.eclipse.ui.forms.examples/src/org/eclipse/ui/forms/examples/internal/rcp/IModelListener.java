/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.examples.internal.rcp;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IModelListener {
	String ADDED="__added";
	String REMOVED="__removed";
	String CHANGED = "__changed";
	void modelChanged(Object[] objects, String type, String property);
}