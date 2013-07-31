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

import org.eclipse.core.runtime.IStatus;

/**
 * Displays a time value based on underlying microsecond value
 * 
 * @since 1.0
 */
public abstract class TimeControl extends SequencerControl {

	/**
	 * Constructs a time control with the given name for the
	 * given launch.
	 * 
	 * @param name
	 * @param launch
	 */
	public TimeControl(String name, MidiLaunch launch) {
		super(name, launch);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#getValue()
	 */
	@Override
	public String getValue() {
		long position = getTimeValue();
		int milli = (int) (position & 0x3F);
		int sec = (int) (position / 1000000);
		int min = sec / 60;
		sec = sec % 60;
		StringBuffer clock = new StringBuffer();
		clock.append(min);
		while (clock.length() < 2) {
			clock.insert(0, 0);
		}
		clock.append(':');
		clock.append(sec);
		while (clock.length() < 5) {
			clock.insert(3, 0);
		}
		clock.append(':');
		clock.append(milli);
		while (clock.length() < 8) {
			clock.insert(6, 0);
		}
		return clock.toString();
	}
	
	/**
	 * Provided by subclasses for the control.
	 * 
	 * @return time in microseconds
	 */
	protected abstract long getTimeValue();

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#isEditable()
	 */
	@Override
	public boolean isEditable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#setValue(java.lang.String)
	 */
	@Override
	public IStatus setValue(String newValue) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.SequencerControl#validateValue(java.lang.String)
	 */
	@Override
	public IStatus validateValue(String value) {
		return null;
	}

}
