/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.ui;

import org.eclipse.help.ui.internal.views.ScopeSet;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class RootScopePage extends PreferencePage implements ISearchScopePage {
	private String engineId;
	private Button masterButton;
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
		Control scopeContents = createScopeContents(container);
		if (scopeContents!=null)
			scopeContents.setLayoutData(new GridData(GridData.FILL_BOTH));
		return container;
	}
	
	protected void masterValueChanged(boolean value) {
	}
	
    public boolean performOk() {
    	getPreferenceStore().setValue(ScopeSet.getMasterKey(engineId), masterButton.getSelection()); 
        return true;
    }	
	protected abstract Control createScopeContents(Composite parent);
}