/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui;

import org.eclipse.help.ui.internal.views.ScopeSet;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Clients that contribute search scope root page to the 
 * search engine definition must extend this class and
 * implement <code>createScopeContents</code> method.
 * The page will come preset with the engine name, 
 * image and description, as well as the master switch
 * that turns the engine on or off. When the engins master
 * switch is set to false, the entire client composite
 * will be hidden.
 */
public abstract class RootScopePage extends PreferencePage implements ISearchScopePage {
	private String engineId;
	private Button masterButton;
	private Control scopeContents;
	/**
	 * 
	 */
	public RootScopePage() {
	}
	
	public void setEngineIdentifier(String id) {
		this.engineId = id;
	}

	protected final Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		masterButton = new Button(container, SWT.CHECK);
		masterButton.setText("Enable search engine");
		boolean masterValue = getPreferenceStore().getBoolean(ScopeSet.getMasterKey(engineId));
		masterButton.setSelection(masterValue);
		masterButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				masterValueChanged(masterButton.getSelection());
			}
		});
		scopeContents = createScopeContents(container);
		if (scopeContents!=null)
			scopeContents.setLayoutData(new GridData(GridData.FILL_BOTH));
		masterValueChanged(masterValue);
		return container;
	}
	
	protected void masterValueChanged(boolean value) {
		scopeContents.setVisible(value);
	}
	
    public boolean performOk() {
    	getPreferenceStore().setValue(ScopeSet.getMasterKey(engineId), masterButton.getSelection()); 
        return true;
    }	
	protected abstract Control createScopeContents(Composite parent);
}