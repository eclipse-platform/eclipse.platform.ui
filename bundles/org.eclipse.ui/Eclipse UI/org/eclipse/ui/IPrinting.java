package org.eclipse.ui;
public interface IPrinting {	
/*
 * Return true if the contents can be printed
 * Otherwise, return false.
 *
 * NOTE: Only contents of a StyledText widget
 * can be printed at this time
 */
public boolean isPrintable();

}

