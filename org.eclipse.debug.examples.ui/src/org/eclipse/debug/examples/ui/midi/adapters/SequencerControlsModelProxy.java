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
package org.eclipse.debug.examples.ui.midi.adapters;

import javax.sound.midi.Sequencer;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.examples.core.midi.launcher.MidiLaunch;
import org.eclipse.debug.examples.core.midi.launcher.SequencerControl;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;
import org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy;
import org.eclipse.jface.viewers.Viewer;

/**
 * Model proxy for a sequencer in the variables view. Listens
 * to events from controls to update the viewer when the user
 * changes a control's value.
 * 
 * @since 1.0
 */
public class SequencerControlsModelProxy extends EventHandlerModelProxy {

	/**
	 * Associated launch
	 */
	private MidiLaunch fLaunch;
	
	/**
	 * Event handler
	 */
	private ControlEventHandler fHandler;
	
	/**
	 * Constructs a model proxy to update based on changes in controls
	 * for the associated sequencer.
	 * 
	 * @param launch MIDI launch
	 */
	public SequencerControlsModelProxy(MidiLaunch launch) {
		fLaunch = launch;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	@Override
	public void installed(Viewer viewer) {
		super.installed(viewer);
		fHandler.init();
	}

	/**
	 * Returns the launch assocaited with this proxy.
	 * 
	 * @return MIDI launch
	 */
	protected MidiLaunch getMidiLaunch() {
		return fLaunch;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#createEventHandlers()
	 */
	@Override
	protected DebugEventHandler[] createEventHandlers() {
		fHandler = new ControlEventHandler(this);
		return new DebugEventHandler[]{fHandler};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#containsEvent(org.eclipse.debug.core.DebugEvent)
	 */
	@Override
	protected boolean containsEvent(DebugEvent event) {
		if (event.getSource() instanceof SequencerControl) {
			return ((SequencerControl)event.getSource()).getLaunch().equals(fLaunch);
		}
		if (event.getSource() instanceof Sequencer) {
			return fLaunch.getSequencer().equals(event.getSource());
		}
		if (event.getSource().equals(fLaunch)) {
			return true;
		}
		return false;
	}
	
	
	
	

}
