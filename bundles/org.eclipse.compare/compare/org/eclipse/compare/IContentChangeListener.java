/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare;

/**
 * An <code>IContentChangeListener</code> is informed about content changes of a 
 * <code>IContentChangeNotifier</code>.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IContentChangeNotifier
 */
public interface IContentChangeListener {

	/**
	 * Called whenever the content of the given source has changed.
	 *
	 * @param source the source whose contents has changed
	 */
	void contentChanged(IContentChangeNotifier source);
}
