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
 * Controls the location of the sequencer in microseconds.
 * 
 * @since 1.0
 */
public class ClockControl extends TimeControl {

	/**
	 * @param launch
	 */
	public ClockControl(MidiLaunch launch) {
		super("Time" , launch); //$NON-NLS-1$
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.TimeControl#getTimeValue()
	 */
	protected long getTimeValue() {
		return getSequencer().getMicrosecondPosition();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#isEditable()
	 */
	public boolean isEditable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#setValue(java.lang.String)
	 */
	public IStatus setValue(String newValue) {
		try {
			long value = getLong(newValue);
			getSequencer().setMicrosecondPosition(value);
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
			getLong(value);
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
	protected long getLong(String value) throws CoreException {
		try {
			if (value.indexOf(':') == -1) {
				long secs = Long.parseLong(value);
				return secs * 1000000;
			}
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, DebugCorePlugin.PLUGIN_ID, "Time must be an integer (seconds) or 00:00 (minutes:seconds) format", e)); //$NON-NLS-1$
		}
		return 0L;
	}
	

}
