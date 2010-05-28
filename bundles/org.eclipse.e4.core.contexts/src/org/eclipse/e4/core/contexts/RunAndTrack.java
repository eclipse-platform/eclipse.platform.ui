/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.contexts;

/**
 * Extended version of a runnable that can be used with the
 * {@link IEclipseContext#runAndTrack(RunAndTrack)} version gets more detailed
 * information on the change, such as the service name and the event type.
 */
abstract public class RunAndTrack {

	/**
	 * Creates a new instance of trackable computation
	 */
	public RunAndTrack() {
		// placeholder
	}

	/**
	 * The method will be called before the associated context is disposed of.
	 * <p>
	 * Subclasses may override this method. Overrides should finish the processing
	 * by calling this method in the superclass.
	 * </p>
	 * @param context the context being disposed of
	 * @deprecated this method will be removed; use IContextDisposalListener instead
	 */
	public void disposed(IEclipseContext context) {
		// subclasses may override
	}

	/**
	 * This method is initially called by the framework when an instance of this
	 * class is associated with the context via {@link IEclipseContext#runAndTrack(RunAndTrack)}.
	 * <p>
	 * After the initial call this method is executed when one or more values it retrieved 
	 * from the context change.
	 * </p>
	 * @param context modified context
	 * @return <code>true</code> to continue to be called on updates; <code>false</code> otherwise
	 */
	abstract public boolean changed(IEclipseContext context);
}
