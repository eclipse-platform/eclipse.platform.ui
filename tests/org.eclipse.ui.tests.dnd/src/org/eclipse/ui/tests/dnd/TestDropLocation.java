package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * This is an interface intended for use in test suites. Objects can implement
 * this interface to force any dragged object to be dropped at a particular
 * location.
 *
 */
public interface TestDropLocation {

	/**
	 * Location where the object should be dropped, in display coordinates
	 *
	 * @return a location in display coordinates
	 */
	Point getLocation();

	/**
	 * The drop code will pretend that only the given shells are open,
	 * and that they have the specified Z-order.
	 *
	 * @return the shells to check for drop targets, from bottom to top.
	 */
	Shell[] getShells();
}