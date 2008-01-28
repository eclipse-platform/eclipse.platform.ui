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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.examples.core.midi.launcher.MidiLaunch;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler;

/**
 * Listens to events from sequencer controls and fires corresponding
 * deltas to update the viewer.
 * 
 * @since 1.0
 */
public class ControlEventHandler extends DebugEventHandler {

	private MidiLaunch fLaunch;
	
	/**
	 * @param proxy
	 */
	public ControlEventHandler(SequencerControlsModelProxy proxy) {
		super(proxy);
		fLaunch = proxy.getMidiLaunch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DebugEventHandler#handlesEvent(org.eclipse.debug.core.DebugEvent)
	 */
	protected boolean handlesEvent(DebugEvent event) {
		return true;
	}

	protected void refreshRoot(DebugEvent event) {
		ModelDelta delta = new ModelDelta(fLaunch, IModelDelta.CONTENT);
		fireDelta(delta);
	}
	
}
