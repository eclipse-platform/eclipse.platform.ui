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

import javax.sound.midi.MidiEvent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

/**
 * Provides labels for MIDI tracks.
 * 
 * @since 1.0
 */
public class MidiEventLabelProvider extends ElementLabelProvider {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.model.elements.ElementLabelProvider#getLabel(org.eclipse.jface.viewers.TreePath, org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.String)
	 */
	protected String getLabel(TreePath elementPath, IPresentationContext presentationContext, String columnId) throws CoreException {
		MidiEvent event = (MidiEvent) elementPath.getLastSegment();
		if (TrackColumnPresentation.COL_TICK.equals(columnId)) {
			return Long.toString(event.getTick());
		} else if (TrackColumnPresentation.COL_MESSAGE.equals(columnId)) {
			byte[] bytes = event.getMessage().getMessage();
			StringBuffer buffer = new StringBuffer();
			int status = event.getMessage().getStatus();
			appendByte(buffer, status);
			for (int i = 1; i < bytes.length; i++) {
				buffer.append(' ');
				appendByte(buffer, bytes[i]);
			}
			return buffer.toString();
		}
		return "";
	}
	
	/**
	 * Appends a byte to the buffer with 2 hex characters.
	 * 
	 * @param buffer
	 * @param b
	 */
	private void appendByte(StringBuffer buffer, int b) { 
		String hex = Integer.toHexString(b).toUpperCase();
		for (int i = hex.length(); i < 2; i++) {
			buffer.append('0');
		}
		buffer.append(hex);
	}

}
