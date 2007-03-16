/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;

/**
 * This class is responsible to determin if a cell selection event is triggers
 * an editor activation
 * 
 * @since 3.3
 */
public class ColumnViewerEditorActivationStrategy {
	private ColumnViewer viewer;
	
	private KeyListener keyboardActivationListener;
	
	/**
	 * @param viewer the viewer the editor support is attached to
	 */
	public ColumnViewerEditorActivationStrategy(ColumnViewer viewer) {
		this.viewer = viewer;
	}
	
	/**
	 * @param event
	 * @return bla bl
	 */
	protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
		return event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
			|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC
			|| event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL;
		}

	/**
	 * @return the cell holding the current focus
	 */
	private ViewerCell getFocusCell() {
		return viewer.getColumnViewerEditor().getFocusCell();
	}
	
	/**
	 * @return the viewer
	 */
	public ColumnViewer getViewer() {
		return viewer;
	}
	
	/**
	 * Enable activation of cell editors by keyboard
	 * @param enable <code>true</code> to enable
	 */
	public void setEnableEditorActivationWithKeyboard(boolean enable) {
		if( enable ) {
			if( keyboardActivationListener == null ) {
				keyboardActivationListener = new KeyListener() {

					public void keyPressed(KeyEvent e) {
						ViewerCell cell = getFocusCell();
						
						if( cell != null ) {
							viewer.triggerEditorActivationEvent(new ColumnViewerEditorActivationEvent(cell,e));
						}
					}

					public void keyReleased(KeyEvent e) {
						
					}
					
				};
				viewer.getControl().addKeyListener(keyboardActivationListener);
			}
		} else {
			if( keyboardActivationListener != null ) {
				viewer.getControl().removeKeyListener(keyboardActivationListener);
				keyboardActivationListener = null;
			}
		}
	}

}
