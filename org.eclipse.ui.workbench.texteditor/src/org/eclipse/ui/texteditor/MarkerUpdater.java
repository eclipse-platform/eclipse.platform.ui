package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;

import org.eclipse.core.resources.IMarker;

/**
 * Updates a marker's positional attributes which are start position, end position, 
 * and line number.
 */
class MarkerUpdater implements IMarkerUpdater {

	private final static String[] ATTRIBUTES= {IMarker.CHAR_START, IMarker.CHAR_END, IMarker.LINE_NUMBER};

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

		if (position.isDeleted())
			return false;

		if (MarkerUtilities.getCharStart(marker) != -1 && MarkerUtilities.getCharEnd(marker) != -1) {
			MarkerUtilities.setCharStart(marker, position.getOffset());
			MarkerUtilities.setCharEnd(marker, position.getOffset() + position.getLength());
		}
		if (MarkerUtilities.getLineNumber(marker) != -1) {
			try {
				// marker line numbers are 1-based
				MarkerUtilities.setLineNumber(marker, document.getLineOfOffset(position.getOffset()) + 1);
			} catch (BadLocationException x) {
			}
		}

		return true;
	}

}
