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

import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.Sequencer;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ISuspendResume;

/**
 * A launch containing a MIDI sequencer.
 * 
 * @since 1.0
 */
public class MidiLaunch extends Launch implements ISuspendResume {
	
	/**
	 * MIDI Sequencer
	 */
	private Sequencer fSequencer;
	
	/**
	 * MIDI file format
	 */
	private MidiFileFormat fFormat;

	/**
	 * Constructs a new MIDI launch.
	 * 
	 * @param launchConfiguration configuration to play
	 * @param mode mode to play in
	 */
	public MidiLaunch(ILaunchConfiguration launchConfiguration, String mode) {
		super(launchConfiguration, mode, null);
	}
	
	/**
	 * Sets the sequencer used to play MIDI files.
	 * 
	 * @param sequencer
	 */
	public void setSequencer(Sequencer sequencer) {
		fSequencer = sequencer;
		fireChanged();
	}
	
	/**
	 * Sets the format of the sequence
	 * @param format
	 */
	public void setFormat(MidiFileFormat format) {
		fFormat = format;
	}

	/**
	 * Returns the file format of the sequence.
	 * 
	 * @return file format
	 */
	public MidiFileFormat getFormat() {
		return fFormat;
	}
	/**
	 * Returns the sequencer used to play MIDI files.
	 * 
	 * @return the sequencer used to play MIDI files
	 */
	public Sequencer getSequencer() {
		return fSequencer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#canTerminate()
	 */
	@Override
	public boolean canTerminate() {
		return getSequencer().isOpen();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#isTerminated()
	 */
	@Override
	public boolean isTerminated() {
		if (fSequencer != null) {
			return !fSequencer.isOpen();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.Launch#terminate()
	 */
	@Override
	public void terminate() throws DebugException {
		getSequencer().stop();
		getSequencer().close();
		fireTerminate();
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[]{new DebugEvent(getSequencer(), DebugEvent.TERMINATE)});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	@Override
	public boolean canResume() {
		return isSuspended();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	@Override
	public boolean canSuspend() {
		if (fSequencer != null) {
			return fSequencer.isRunning();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	@Override
	public boolean isSuspended() {
		if (fSequencer != null) {
			return fSequencer.isOpen() & !fSequencer.isRunning();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	@Override
	public void resume() throws DebugException {
		getSequencer().start();
		fireChanged();
		fireEvent(new DebugEvent(getSequencer(), DebugEvent.RESUME, DebugEvent.CLIENT_REQUEST));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	@Override
	public void suspend() throws DebugException {
		getSequencer().stop();
		fireChanged();
		fireEvent(new DebugEvent(getSequencer(), DebugEvent.SUSPEND, DebugEvent.CLIENT_REQUEST));
	}
	
	/**
	 * Fires a debug event.
	 * 
	 * @param event debug event to fire
	 */
	protected void fireEvent(DebugEvent event) {
		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] {event});
	} 
}
