/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import org.eclipse.debug.examples.core.pda.DebugCorePlugin;

/**
 * Controls the location of the sequencer in microseconds.
 * 
 * @since 1.0
 */
public class TimeControl extends SequencerControl {

	/**
	 * @param name
	 * @param launch
	 */
	public TimeControl(MidiLaunch launch) {
		super("Time" , launch);
	}

	/**
	 * Returns a long for the string.
	 * 
	 * @param value string
	 * @return long
	 * @throws CoreException if not a valid value 
	 */
	protected long getLong(String value) throws CoreException {
		try {
			return Long.parseLong(value); 
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, DebugCorePlugin.PLUGIN_ID, "Time must be a number", e));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#getValue()
	 */
	public String getValue() {
		long position = getSequencer().getMicrosecondPosition();
		int milli = (int) (position & 0x3F);
		int sec = (int) (position / 1000000);
		StringBuffer clock = new StringBuffer();
		clock.append(sec);
		clock.append(':');
		clock.append(milli);
		return clock.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#isEditable()
	 */
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#setValue(java.lang.String)
	 */
	public IStatus setValue(String newValue) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#validateValue(java.lang.String)
	 */
	public IStatus validateValue(String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
