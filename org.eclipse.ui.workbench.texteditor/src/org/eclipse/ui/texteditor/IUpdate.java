package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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