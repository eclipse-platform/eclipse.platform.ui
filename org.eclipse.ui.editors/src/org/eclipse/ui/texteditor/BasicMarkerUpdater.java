/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;


/**
 * Updates a marker's positional attributes which are
 * start position, end position, and line number.
 */
public final class BasicMarkerUpdater implements IMarkerUpdater {

	private final static String[] ATTRIBUTES= {
		IMarker.CHAR_START,
		IMarker.CHAR_END,
		IMarker.LINE_NUMBER
	};

	/**
	 * Creates a new basic marker updater.
	 */
	public BasicMarkerUpdater() {
		super();
	}

	/*
	 * @see IMarkerUpdater#getAttribute()
	 */
	public String[] getAttribute() {
		return ATTRIBUTES;
	}

	/*
	 * @see IMarkerUpdater#getMarkerType()
	 */
	public String getMarkerType() {
		return null;
	}

	/*
	 * @see IMarkerUpdater#updateMarker(IMarker, IDocument, Position)
	 */
	public boolean updateMarker(IMarker marker, IDocument document, Position position) {

		if (position == null)
			return true;

		if (position.isDeleted())
			return false;

		boolean offsetsInitialized= false;
		boolean offsetsChanged= false;
		int markerStart= MarkerUtilities.getCharStart(marker);
		int markerEnd= MarkerUtilities.getCharEnd(marker);

		if (markerStart != -1 && markerEnd != -1) {

			offsetsInitialized= true;

			int offset= position.getOffset();
			if (markerStart != offset) {
				MarkerUtilities.setCharStart(marker, offset);
				offsetsChanged= true;
			}

			offset += position.getLength();
			if (markerEnd != offset) {
				MarkerUtilities.setCharEnd(marker, offset);
				offsetsChanged= true;
			}
		}

		if (!offsetsInitialized || (offsetsChanged && MarkerUtilities.getLineNumber(marker) != -1)) {
			try {
				// marker line numbers are 1-based
				MarkerUtilities.setLineNumber(marker, document.getLineOfOffset(position.getOffset()) + 1);
			} catch (BadLocationException x) {
			}
		}

		return true;
	}
}
