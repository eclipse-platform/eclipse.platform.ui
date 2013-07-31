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


/**
 * Describes the length of the sequence in microseconds.
 * 
 * @since 1.0
 */
public class LengthControl extends TimeControl {

	/**
	 * @param launch
	 */
	public LengthControl(MidiLaunch launch) {
		super("Duration" , launch); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.examples.core.midi.launcher.TimeControl#getTimeValue()
	 */
	@Override
	protected long getTimeValue() {
		return getSequencer().getMicrosecondLength();
	}


}
