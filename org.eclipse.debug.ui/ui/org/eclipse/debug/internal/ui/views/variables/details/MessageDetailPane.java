/*******************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables.details;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * A detail pane that displays a message in a wrapped label. Not contributed by an extension
 * point - used internally to display messages.
 * 
 * @since 3.6
 */
public class MessageDetailPane implements IDetailPane {
	
	public static final String ID = IDebugUIConstants.PLUGIN_ID + ".detailpanes.message"; //$NON-NLS-1$
	public static final String NAME = DetailMessages.MessageDetailPane_0;
	public static final String DESCRIPTION = DetailMessages.MessageDetailPane_1;
		
	/**
	 * Composite that contains the label that has margins.
	 */
	private Composite fControlParent;
	
	/**
	 * Label control
	 */
	private Label fLabel;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#init(org.eclipse.ui.IWorkbenchPartSite)
	 */
	public void init(IWorkbenchPartSite partSite) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#dispose()
	 */
	public void dispose() {
		fControlParent.dispose();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		fControlParent = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		fLabel = SWTFactory.createWrapLabel(fControlParent, "", 1); //$NON-NLS-1$
		return fControlParent;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#setFocus()
	 */
	public boolean setFocus() {
		return false;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#display(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void display(IStructuredSelection selection) {
		// re-create controls if the layout has changed
		if (selection != null && selection.size() == 1) {
			Object input = selection.getFirstElement();
			if (input instanceof String) {
				String message = (String) input;
				fLabel.setText(message);
				fControlParent.layout(true);
			}
		} else if (selection == null || selection.isEmpty()) {
			// clear the message
			fLabel.setText(""); //$NON-NLS-1$
			fControlParent.layout(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#getID()
	 */
	public String getID() {
		return ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#getName()
	 */
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#getDescription()
	 */
	public String getDescription() {
		return DESCRIPTION;
	}

}
