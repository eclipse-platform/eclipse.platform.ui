package org.eclipse.debug.internal.ui.views;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
 
/**
 * A plugable  exception handler.
 */
public interface IDebugExceptionHandler {
	
	/**
	 * Handles the given debug exception.
	 * 
	 * @param e debug exception
	 */
	public abstract void handleException(DebugException e);

}
