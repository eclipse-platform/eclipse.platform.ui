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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
		
	private SashForm fSash;
	/**
	 * Top level composite that remains as orientation changes.
	 */
	private Composite fDetailsContainer;
	/**
	 * Inner composite containing separator and editor composite that gets
	 * disposed/created as orientation changes with *no* margins (so separator
	 * spans entire width/height of the pane).
	 */
	private Composite fSeparatorContainer; 
	/**
	 * Cached orientation currently being displayed
	 */
	private int fOrientation = -1;
	/**
	 * Composite that contains the editor that has margins.
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
		fSash = null;
		fDetailsContainer.dispose();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		fDetailsContainer = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
		if (parent instanceof SashForm) {
			fSash = (SashForm) parent;
		}
		createDetails();
		return fDetailsContainer;
	}
	
	/**
	 * Creates the details area with a separator based on orientation.
	 */
	protected void createDetails() {
		int parentOrientation = SWT.HORIZONTAL;
		if (fSash != null) {
			parentOrientation = fSash.getOrientation();
		}
		String message = ""; //$NON-NLS-1$
		if (fLabel != null) {
			message = fLabel.getText();
		}
		if (parentOrientation == fOrientation) {
			return;
		}
		if (fSeparatorContainer != null) {
			fSeparatorContainer.dispose();
		}
		if (parentOrientation == SWT.VERTICAL) {
			fSeparatorContainer = SWTFactory.createComposite(fDetailsContainer, fDetailsContainer.getFont(), 1, 1, GridData.FILL_BOTH, 0, 0);
			Label sep = new Label(fSeparatorContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
			GridLayout layout= (GridLayout)fSeparatorContainer.getLayout();
			layout.marginHeight= 0;
			layout.marginWidth= 0;
			fControlParent = SWTFactory.createComposite(fSeparatorContainer, 1, 1, GridData.FILL_BOTH);
		} else {
			fSeparatorContainer = SWTFactory.createComposite(fDetailsContainer, fDetailsContainer.getFont(), 2, 1, GridData.FILL_BOTH, 0, 0);
			Label sep= new Label(fSeparatorContainer, SWT.SEPARATOR | SWT.VERTICAL);
			sep.setLayoutData(new GridData(SWT.TOP, SWT.FILL, false, true));
			GridLayout layout= (GridLayout)fSeparatorContainer.getLayout();
			layout.marginHeight= 0;
			layout.marginWidth= 0;
			fControlParent = SWTFactory.createComposite(fSeparatorContainer, 1, 1, GridData.FILL_BOTH);
		}
		fOrientation = parentOrientation;
		fLabel = SWTFactory.createWrapLabel(fControlParent, message, 1);
		fDetailsContainer.layout(true);
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
				createDetails();
				String message = (String) input;
				fLabel.setText(message);
				fDetailsContainer.layout(true);
			}
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
