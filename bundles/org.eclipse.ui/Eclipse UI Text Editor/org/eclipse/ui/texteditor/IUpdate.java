package org.eclipse.ui.texteditor;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


/**
 * Indicates the support of an update method.
 */
public interface IUpdate {
	
	/**
	 * Requests that this object update itself.
	 */
	void update();
}
