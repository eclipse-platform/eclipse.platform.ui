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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Clients that contribute search scope root page to the search engine
 * definition must extend this class and implement
 * <code>createScopeContents</code> method. The page will come preset with the
 * engine name, image and description, as well as the master switch that turns
 * the engine on or off. When the engins master switch is set to false, the
 * entire client composite will be disabled.
 * 
 * @since 3.1
 */
public abstract class RootScopePage extends PreferencePage implements
		ISearchScopePage {
	private String engineId;

	private String scopeSetName;

	private Button masterButton;

	private Control scopeContents;

	/**
	 * The default constructor.
	 */
	public RootScopePage() {
	}

	/**
	 * Implements ISearchScopePage
	 * 
	 * @param engineId
	 * @param scopeSetName
	 */
	public void init(String engineId, String scopeSetName) {
		this.engineId = engineId;
		this.scopeSetName = scopeSetName;
	}

	/**
	 * Creates the initial contents of the page and allocates the area for the
	 * clients. Classes that extend this class should implement
	 * <code>createScopeContents(Composite)</code> instead.
	 * 
	 * @param parent
	 *            the page parent
	 * @return the page client control
	 */
	protected final Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		masterButton = new Button(container, SWT.CHECK);
		masterButton.setText("Enable search engine");
		boolean masterValue = getPreferenceStore().getBoolean(
				ScopeSet.getMasterKey(engineId));
		masterButton.setSelection(masterValue);
		masterButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				masterValueChanged(masterButton.getSelection());
			}
		});
		scopeContents = createScopeContents(container);
		if (scopeContents != null)
			scopeContents.setLayoutData(new GridData(GridData.FILL_BOTH));
		masterValueChanged(masterValue);
		return container;
	}

	/**
	 * Called when the value of the master switch has changed. The default
	 * implementation disables the scope contents control when the master switch
	 * is off. Subclass can override this behaviour.
	 * 
	 * @param value
	 *            <code>true</code> if the master switch is on,
	 *            <code>false</code> otherwise.
	 */

	protected void masterValueChanged(boolean value) {
		scopeContents.setEnabled(value);
	}

	/**
	 * Returns the scope set name passed to the page during initialization.
	 * 
	 * @return the name of the current scope set
	 */

	protected String getScopeSetName() {
		return scopeSetName;
	}

	/**
	 * Returns the identifier of the engine associated with this page.
	 * 
	 * @return the engine identifier
	 */
	protected String getEngineId() {
		return engineId;
	}

	/**
	 * Tests whether the search engine has been selected to participate in the
	 * search.
	 * 
	 * @return <code>true</code> if the search engine is enabled, </code>false</code>
	 *         otherwise.
	 */

	protected boolean isEngineEnabled() {
		return masterButton.getSelection();
	}

	/**
	 * Stores the value of the master switch in the preference store. Subclasses
	 * may override but must call 'super'.
	 * 
	 * @return <code>true</code> if the wizard can be closed,
	 *         <code>false</code> otherwise.
	 */

	public boolean performOk() {
		getPreferenceStore().setValue(ScopeSet.getMasterKey(engineId),
				masterButton.getSelection());
		return true;
	}

	/**
	 * Abstract method that subclasses must implement in order to provide root
	 * page content.
	 * 
	 * @param parent
	 *            the page parent
	 * @return the control of the scope contents
	 */
	protected abstract Control createScopeContents(Composite parent);
}