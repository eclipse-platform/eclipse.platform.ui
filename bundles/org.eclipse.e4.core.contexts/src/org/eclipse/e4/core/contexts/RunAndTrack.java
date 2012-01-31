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

import java.util.Stack;
import org.eclipse.e4.core.internal.contexts.Computation;
import org.eclipse.e4.core.internal.contexts.EclipseContext;

/**
 * Extended version of a runnable that can be used with the
 * {@link IEclipseContext#runAndTrack(RunAndTrack)} version gets more detailed
 * information on the change, such as the service name and the event type.
 */
abstract public class RunAndTrack {

	private boolean isRecordingPaused = false;

	/**
	 * Creates a new instance of trackable computation
	 */
	public RunAndTrack() {
		// placeholder
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

	/**
	 * This method can be called to pause dependency recording while {@link #changed(IEclipseContext)}
	 * does its processing. This can be especially useful if external code
	 * has to be called. The method {@link #resumeRecoding()} must be called before RunAndTrack
	 * returns control to its caller.  
	 */
	synchronized protected void pauseRecording() {
		if (isRecordingPaused)
			return;
		Stack<Computation> current = EclipseContext.getCalculatedComputations();
		current.push(null);
		isRecordingPaused = true;
	}

	/**
	 * Call this method to resume dependency recording previously paused by 
	 * the {@link #pauseRecording()}.
	 */
	synchronized protected void resumeRecoding() {
		if (!isRecordingPaused)
			return;
		Stack<Computation> current = EclipseContext.getCalculatedComputations();
		Computation plug = current.pop();
		if (plug != null)
			throw new IllegalArgumentException("Internal error in nested computation processing"); //$NON-NLS-1$
		isRecordingPaused = false;
	}

}
