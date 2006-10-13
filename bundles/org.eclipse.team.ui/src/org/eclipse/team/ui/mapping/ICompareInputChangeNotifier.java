/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import org.eclipse.compare.structuremergeviewer.ICompareInput;

/**
 * Interface that allows clients to react to changes in the state
 * of a compare input.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * can instead subclass {@link CompareInputChangeNotifier}.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see CompareInputChangeNotifier
 * 
 * @since 3.3
 */
public interface ICompareInputChangeNotifier {

	/**
	 * Connect the input to this notifier. Clients can expect to receive change
	 * notification for all connected inputs.
	 * @param input a compare input
	 */
	void connect(ICompareInput input);
	
	/**
	 * disconnect the input from this notifier. Once disconnected, change notification
	 * will not be issued for an input.
	 * @param input a compare input
	 */
	void disconnect(ICompareInput input);
	
	/**
	 * Add a change listener that will get notified of changes in any connected
	 * inputs. Registering a listener multiple times will have no affect.
	 * @param listener a change listener
	 */
	void addChangeListener(ICompareInputChangeListener listener);
	
	/**
	 * Remove the given listener. Removing a non-existent listener has no effect.
	 * @param listener a change listener
	 */
	void removeChangeListener(ICompareInputChangeListener listener);
	
}
