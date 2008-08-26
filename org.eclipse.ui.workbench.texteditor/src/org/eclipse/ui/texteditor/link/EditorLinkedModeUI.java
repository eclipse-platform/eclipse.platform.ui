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
package org.eclipse.ui.texteditor.link;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * A <code>LinkedModeUI</code> that takes care of updating the focus editor's
 * navigation history.
 *
 * @since 3.0
 */
public class EditorLinkedModeUI extends LinkedModeUI {

	/**
	 * Updates the editor navigation history upon linked focus changes.
	 *
	 * @since 3.0
	 */
	private static class EditorHistoryUpdater implements ILinkedModeUIFocusListener {

		/*
		 * @see org.eclipse.jface.text.link.LinkedModeUI.ILinkedModeUIFocusListener#linkedFocusLost(org.eclipse.jface.text.link.LinkedPosition, org.eclipse.jface.text.link.LinkedModeUI.LinkedModeUITarget)
		 */
		public void linkingFocusLost(LinkedPosition position, LinkedModeUITarget target) {
			// mark navigation history
			IWorkbenchWindow win= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (win != null) {
				IWorkbenchPage page= win.getActivePage();
				if (page != null) {
					IEditorPart part= page.getActiveEditor();
					page.getNavigationHistory().markLocation(part);
				}
			}
		}

		/*
		 * @see org.eclipse.jface.text.link.LinkedModeUI.ILinkedModeUIFocusListener#linkedFocusGained(org.eclipse.jface.text.link.LinkedPosition, org.eclipse.jface.text.link.LinkedModeUI.LinkedModeUITarget)
		 */
		public void linkingFocusGained(LinkedPosition position, LinkedModeUITarget target) {
			// does nothing
		}
	}


	public EditorLinkedModeUI(LinkedModeModel model, ITextViewer viewer) {
		super(model, viewer);
		setPositionListener(new EditorHistoryUpdater());
	}

	public EditorLinkedModeUI(LinkedModeModel model, ITextViewer[] viewers) {
		super(model, viewers);
		setPositionListener(new EditorHistoryUpdater());
	}

	public EditorLinkedModeUI(LinkedModeModel model, LinkedModeUITarget target) {
		super(model, target);
		setPositionListener(new EditorHistoryUpdater());
	}

	public EditorLinkedModeUI(LinkedModeModel model, LinkedModeUITarget[] targets) {
		super(model, targets);
		setPositionListener(new EditorHistoryUpdater());
	}
}
