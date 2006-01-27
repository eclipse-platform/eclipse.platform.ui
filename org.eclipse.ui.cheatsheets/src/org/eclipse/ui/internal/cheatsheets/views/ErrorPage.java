/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.internal.cheatsheets.*;

public class ErrorPage extends Page {

	private String message;
	
	public ErrorPage() {
	}

	public ErrorPage(String errorMessage) {
		this.message = errorMessage;
	}

	public void createPart(Composite parent) {
		super.createPart(parent);
		String errorString = null;
		if(message == null) {
			errorString = Messages.ERROR_PAGE_MESSAGE;
		} else {
			errorString = message;
		}
		Label errorLabel = toolkit.createLabel(form.getBody(), errorString, SWT.WRAP);
		errorLabel.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
	}

	/**
	 * Creates the cheatsheet's title areawhich will consists
	 * of a title and image.
	 *
	 * @param parent the SWT parent for the title area composite
	 */
	protected String getTitle() {
		return Messages.ERROR_LOADING_CHEATSHEET_CONTENT;
	}

	public void initialized() {
		// No initialization required
	}
}