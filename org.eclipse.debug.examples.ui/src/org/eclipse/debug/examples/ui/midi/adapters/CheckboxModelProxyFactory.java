/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 286310)
 *****************************************************************/
package org.eclipse.debug.examples.ui.midi.adapters;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

import org.eclipse.debug.examples.ui.pda.views.CheckboxView;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

public class CheckboxModelProxyFactory implements IModelProxyFactory {
	private MidiEventModelProxy fMidiEventProxy = new MidiEventModelProxy();
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory#createModelProxy(java.lang.Object, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext)
	 */
	@Override
	public IModelProxy createModelProxy(Object element, IPresentationContext context) {
		if (CheckboxView.ID.equals(context.getId())) {
			if (element instanceof Track) {
				return new TrackModelProxy((Track) element);
			} else if (element instanceof MidiEvent) {
				return fMidiEventProxy;
			}
		}
		
		return null;
	}

}
