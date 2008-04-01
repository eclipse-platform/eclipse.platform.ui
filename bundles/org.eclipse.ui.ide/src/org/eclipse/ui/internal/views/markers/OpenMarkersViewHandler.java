/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.markers.MarkerViewHandler;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * OpenMarkersViewHandler is used to open another markers view.
 * 
 * @since 3.4
 * 
 */
public class OpenMarkersViewHandler extends MarkerViewHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ExtendedMarkersView part = getView(event);
		if (part == null)
			return null;
		try {
			String count = ExtendedMarkersView.newSecondaryID(part);
				String defaultName = NLS.bind(MarkerMessages.newViewTitle,
					new Object[] { part.getSite().getRegisteredName(), count });
			InputDialog dialog = new InputDialog(part.getSite().getShell(),
					NLS
							.bind(MarkerMessages.NewViewHandler_dialogTitle,
									new String[] { part.getSite()
											.getRegisteredName() }),
					MarkerMessages.NewViewHandler_dialogMessage, defaultName,
					getValidator());

			if (dialog.open() != Window.OK)
				return this;

			IViewPart newPart = part.getSite().getPage()
					.showView(part.getSite().getId(), count,
							IWorkbenchPage.VIEW_ACTIVATE);
			if (newPart instanceof ExtendedMarkersView) {
				((ExtendedMarkersView) newPart).initializeTitle(dialog
						.getValue());
			}
		} catch (PartInitException e) {
			throw new ExecutionException(e.getLocalizedMessage(), e);
		}
		return this;

	}

	/**
	 * Get the input validator for the receiver.
	 * 
	 * @return IInputValidator
	 */
	private IInputValidator getValidator() {
		return new IInputValidator() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
			 */
			public String isValid(String newText) {
				if (newText.length() > 0)
					return null;
				return MarkerMessages.MarkerFilterDialog_emptyMessage;
			}
		};
	}
}
