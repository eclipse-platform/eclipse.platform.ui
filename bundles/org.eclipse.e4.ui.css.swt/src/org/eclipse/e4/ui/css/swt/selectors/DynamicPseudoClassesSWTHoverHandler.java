/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.selectors;

import org.eclipse.e4.ui.css.core.dom.selectors.IDynamicPseudoClassesHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Control;

/**
 * SWT class to manage dynamic pseudo classes handler ...:hover with SWT
 * Control.
 */
public class DynamicPseudoClassesSWTHoverHandler extends
		AbstractDynamicPseudoClassesControlHandler {

	public static final IDynamicPseudoClassesHandler INSTANCE = new DynamicPseudoClassesSWTHoverHandler();

	private static String MOUSE_HOVER_LISTENER = "org.eclipse.e4.ui.core.css.swt.selectors.MOUSE_HOVER_LISTENER";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.swt.selectors.AbstractDynamicPseudoClassesControlHandler#intialize(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	protected void intialize(final Control control, final CSSEngine engine) {
		// Create SWT MouseTrack listener
		MouseTrackAdapter mouseHoverListener = new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e) {
				try {
					// mouse hover, apply styles
					// into the SWT control
					control.setData("mouseHover", Boolean.TRUE);
					engine.applyStyles(control, false, true);
				} catch (Exception ex) {
					engine.handleExceptions(ex);
				} finally {
					control.setData("mouseHover", null);
				}
			}

			public void mouseExit(MouseEvent e) {
				// mouse exit, apply styles
				// into the SWT control
				try {
					control.setData("mouseHover", null);
					engine.applyStyles(control, false, true);
				} catch (Exception ex) {
					engine.handleExceptions(ex);
				}
			}
		};
		control.setData(MOUSE_HOVER_LISTENER, mouseHoverListener);
		control.addMouseTrackListener(mouseHoverListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.swt.selectors.AbstractDynamicPseudoClassesControlHandler#dispose(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.e4.ui.core.css.engine.CSSEngine)
	 */
	protected void dispose(Control control, CSSEngine engine) {
		// Get the MouseTrack listener registered into control data
		MouseTrackAdapter mouseHoverListener = (MouseTrackAdapter) control
				.getData(MOUSE_HOVER_LISTENER);
		if (mouseHoverListener != null)
			// remove the MouseTrack listener to the control
			control.removeMouseTrackListener(mouseHoverListener);
		control.setData(MOUSE_HOVER_LISTENER, null);
	}

}
