package org.eclipse.jface.util;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.swt.events.SelectionEvent;
/**
 * Listener for open events which are generated on selection
 * of default selection depending on the user preferences.
 * 
 * <p>
 * Usage:
 * <pre>
 *	OpenStrategy handler = new OpenStrategy(control);
 *	handler.addOpenListener(new IOpenEventListener() {
 *		public void handleOpen(SelectionEvent e) {
 *			... // code to handle the open event.
 *		}
 *	});
 * </pre>
 * </p>
 *
 * @see OpenStrategy
 */
public interface IOpenEventListener {
	/**
	 * Called when a selection or default selection occurs 
	 * depending on the user preference. 
	 */
	public void handleOpen(SelectionEvent e);
}
