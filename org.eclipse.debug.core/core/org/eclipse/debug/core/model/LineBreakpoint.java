package org.eclipse.debug.core.model;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;


/**
 * Abstract implementation of a line breakpoint. This class is
 * intended to be subclassed by debug model specific implementations
 * of line breakpoints.
 * 
 * @see ILineBreakpoint
 */

public abstract class LineBreakpoint extends Breakpoint implements ILineBreakpoint {
	

	/**
	 * @see ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		IMarker m = getMarker();
		if (m != null) {
			return m.getAttribute(IMarker.LINE_NUMBER, -1);
		}
		return -1;
	}

	/**
	 * @see ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		IMarker m = getMarker();
		if (m != null) {
			return m.getAttribute(IMarker.CHAR_START, -1);
		}
		return -1;
	}

	/**
	 * @see ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		IMarker m = getMarker();
		if (m != null) {
			return m.getAttribute(IMarker.CHAR_END, -1);
		}
		return -1;
	}
}

