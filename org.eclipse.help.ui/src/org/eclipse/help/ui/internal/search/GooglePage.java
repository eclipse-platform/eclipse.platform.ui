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

package org.eclipse.help.ui.internal.search;

import org.eclipse.help.ui.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Google participant in the federated search.
 */
public class GooglePage extends RootScopePage {
    private Button searchWeb;
    private Button searchNewsgroups;
    private String searchType;
    
    /**
     * Default constructor.
     */
    public GooglePage() {
    }
    
    protected Control createScopeContents(Composite parent) {
        Font font = parent.getFont();
        initializeDialogUnits(parent);

        searchType = getPreferenceStore().getString(getEngineId());
        
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        searchWeb = new Button(composite, SWT.RADIO);
        searchWeb.setText("Search web pages"); 
        GridData gd = new GridData();
        searchWeb.setLayoutData(gd);
        searchWeb.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchType = Google.WEB;
            }
        });
        
        searchNewsgroups = new Button(composite, SWT.RADIO);
        searchNewsgroups.setText("Search newsgroups"); 
        gd = new GridData();
        searchNewsgroups.setLayoutData(gd);
        searchNewsgroups.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchType = Google.NEWS;
            }
        });
      
        if (Google.NEWS.equals(searchType))
            searchNewsgroups.setSelection(true);
        else
            searchWeb.setSelection(true);

        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        if (searchWeb.getSelection())
            getPreferenceStore().setValue(getEngineId(), Google.WEB);
        else if (searchNewsgroups.isEnabled())
            getPreferenceStore().setValue(getEngineId(), Google.NEWS);
        return super.performOk();
    }
}
