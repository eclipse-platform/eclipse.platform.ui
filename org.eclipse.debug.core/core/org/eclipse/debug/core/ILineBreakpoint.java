package org.eclipse.debug.core;
public interface ILineBreakpoint extends IBreakpoint {

/**
 * Returns the value of the <code>LINE_NUMBER</code> attribute of the
 * given breakpoint or -1 if the attribute is not present or
 * an exception occurs while accessing the attribute. This is a
 * convenience method for <code>IMarker.getAttribute(String, int)</code>.
 *
 * @param breakpoint the breakpoint
 * @return the breakpoint's line number, or -1 if unknown
 */
public int getLineNumber();
/**
 * Returns the value of the <code>CHAR_START</code> attribute of the
 * given breakpoint or -1 if the attribute is not present, or
 * an exception occurs while accessing the attribute. This is a
 * convenience method for <code>IMarker.getAttribute(String, int)</code>
 * 
 * @param breakpoint the breakpoint
 * @return the breakpoint's char start value, or -1 if unknown
 */
public int getCharStart();
/**
 * Returns the value of the <code>CHAR_END</code> attribute of the
 * given breakpoint or -1 if the attribute is not present or
 * an exception occurs while accessing the attribute.
 * This is a convenience method for <code>IMarker.getAttribute(String, int)</code>.
 *
 * @param breakpoint the breakpoint
 * @return the breakpoint's char end value, or -1 if unknown
 */
public int getCharEnd();
}

