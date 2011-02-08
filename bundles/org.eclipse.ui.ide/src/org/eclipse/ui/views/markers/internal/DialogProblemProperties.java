/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 77332 - [Markers] Add task dialog improvements
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DialogProblemProperties extends DialogMarkerProperties {

	private Text severityLabel;

	private Label severityImage;

	public DialogProblemProperties(Shell parentShell) {
		super(parentShell);
		setType(IMarker.PROBLEM);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.MarkerPropertiesDialog#createAttributesArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createAttributesArea(Composite parent) {
		createSeperator(parent);
		super.createAttributesArea(parent);

		new Label(parent, SWT.NONE)
				.setText(MarkerMessages.propertiesDialog_severityLabel);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);

		severityImage = new Label(composite, SWT.NONE);
		severityLabel = new Text(composite, SWT.SINGLE | SWT.READ_ONLY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markerview.MarkerPropertiesDialog#updateDialogFromMarker()
	 */
	protected void updateDialogFromMarker() {
		super.updateDialogFromMarker();
		IMarker marker = getMarker();
		if (marker == null) {
			return;
		}

		severityImage.setImage(Util.getImage(marker.getAttribute(
				IMarker.SEVERITY, -1)));
		int severity = marker.getAttribute(IMarker.SEVERITY, -1);
		if (severity == IMarker.SEVERITY_ERROR) {
			severityLabel.setText(MarkerMessages.propertiesDialog_errorLabel);
		} else if (severity == IMarker.SEVERITY_WARNING) {
			severityLabel.setText(MarkerMessages.propertiesDialog_warningLabel);
		} else if (severity == IMarker.SEVERITY_INFO) {
			severityLabel.setText(MarkerMessages.propertiesDialog_infoLabel);
		} else {
			severityLabel
					.setText(MarkerMessages.propertiesDialog_noseverityLabel);
		}
	}
}
