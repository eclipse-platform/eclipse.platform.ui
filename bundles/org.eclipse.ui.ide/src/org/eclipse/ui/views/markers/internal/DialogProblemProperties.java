/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

class DialogProblemProperties extends DialogMarkerProperties {

    private Label severityLabel;

    DialogProblemProperties(Shell parentShell) {
        super(parentShell);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markerview.MarkerPropertiesDialog#createAttributesArea(org.eclipse.swt.widgets.Composite)
     */
    protected void createAttributesArea(Composite parent) {
        super.createAttributesArea(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());

        severityLabel = new Label(composite, SWT.NONE);
        severityLabel.setFont(composite.getFont());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markerview.MarkerPropertiesDialog#updateDialogFromMarker()
     */
    protected void updateDialogFromMarker() {
        super.updateDialogFromMarker();
        IMarker marker = getMarker();
        if (marker == null) {
            return;
        }
        //TODO display image rather than text
        FieldSeverity type = new FieldSeverity();
        severityLabel.setImage(type.getImage(marker));
        int severity = marker.getAttribute(IMarker.SEVERITY, -1);
        if (severity == IMarker.SEVERITY_ERROR) {
            severityLabel.setText(Messages.format(
                    "propertiesDialog.severityLabel", //$NON-NLS-1$
                    new String[] { Messages
                            .getString("propertiesDialog.errorLabel") })); //$NON-NLS-1$
        } else if (severity == IMarker.SEVERITY_WARNING) {
            severityLabel.setText(Messages.format(
                    "propertiesDialog.severityLabel", //$NON-NLS-1$
                    new String[] { Messages
                            .getString("propertiesDialog.warningLabel") })); //$NON-NLS-1$
        } else if (severity == IMarker.SEVERITY_INFO) {
            severityLabel.setText(Messages.format(
                    "propertiesDialog.severityLabel", //$NON-NLS-1$
                    new String[] { Messages
                            .getString("propertiesDialog.infoLabel") })); //$NON-NLS-1$
        } else {
            severityLabel.setText(Messages.format(
                    "propertiesDialog.severityLabel", //$NON-NLS-1$
                    new String[] { Messages
                            .getString("propertiesDialog.noseverityLabel") })); //$NON-NLS-1$
        }
    }

}