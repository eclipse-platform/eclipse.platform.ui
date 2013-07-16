/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.midi.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.examples.core.pda.DebugCorePlugin;

/**
 * Controls the tempo of a sequencer.
 * 
 * @since 1.0
 */
public class TempoControl extends SequencerControl {

	/**
	 * Constructs a tempo control for the given launch.
	 */
	public TempoControl(MidiLaunch launch) {
		super("Tempo (BPM)", launch); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#getValue()
	 */
	public String getValue() {
		float bpm = getSequencer().getTempoInBPM();
		return Float.toString(bpm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#isEditable()
	 */
	public boolean isEditable() {
		return getSequencer().isOpen();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#setValue(java.lang.String)
	 */
	public IStatus setValue(String newValue) {
		try {
			float value = getFloat(newValue);
			getSequencer().setTempoInBPM(value);
			fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#validateValue(java.lang.String)
	 */
	public IStatus validateValue(String value) {
		try {
			getFloat(value);
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

	/**
	 * Returns a float for the string.
	 * 
	 * @param value string
	 * @return float
	 * @throws CoreException if not a valid value 
	 */
	protected float getFloat(String value) throws CoreException {
		try {
			return Float.parseFloat(value); 
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, DebugCorePlugin.PLUGIN_ID, "Tempo must be a number", e)); //$NON-NLS-1$
		}
	}

}
