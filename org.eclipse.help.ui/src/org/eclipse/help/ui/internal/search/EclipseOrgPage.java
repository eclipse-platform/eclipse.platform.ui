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
public class EclipseOrgPage extends RootScopePage {
    private Button searchAll;
    private Button searchArticles;
    private Button searchDoc;
    private Button searchMail;
    private Button searchNewsgroups;
    private String searchType;
    
    /**
     * Default constructor.
     */
    public EclipseOrgPage() {
    }
    
    protected Control createScopeContents(Composite parent) {
        Font font = parent.getFont();
        initializeDialogUnits(parent);

        searchType = getPreferenceStore().getString(getEngineId());
        
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

        searchAll = new Button(composite, SWT.RADIO);
        searchAll.setText("Search entire site"); 
        GridData gd = new GridData();
        searchAll.setLayoutData(gd);
        searchAll.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchType = EclipseOrg.ALL;
            }
        });
        
        searchArticles = new Button(composite, SWT.RADIO);
        searchArticles.setText("Search articles"); 
        gd = new GridData();
        searchArticles.setLayoutData(gd);
        searchArticles.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchType = EclipseOrg.ARTICLES;
            }
        });

        searchDoc = new Button(composite, SWT.RADIO);
        searchDoc.setText("Search documentation"); 
        gd = new GridData();
        searchDoc.setLayoutData(gd);
        searchDoc.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchType = EclipseOrg.DOC;
            }
        });

        searchMail = new Button(composite, SWT.RADIO);
        searchMail.setText("Search mailing lists"); 
        gd = new GridData();
        searchMail.setLayoutData(gd);
        searchMail.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchType = EclipseOrg.MAIL;
            }
        });

        searchNewsgroups = new Button(composite, SWT.RADIO);
        searchNewsgroups.setText("Search newsgroups"); 
        gd = new GridData();
        searchNewsgroups.setLayoutData(gd);
        searchNewsgroups.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                searchType = EclipseOrg.NEWS;
            }
        });
      
        if (EclipseOrg.ARTICLES.equals(searchType))
            searchArticles.setSelection(true);
        else if (EclipseOrg.DOC.equals(searchType))
            searchDoc.setSelection(true);
        else if (EclipseOrg.MAIL.equals(searchType))
            searchMail.setSelection(true);
        else if (EclipseOrg.NEWS.equals(searchType))
            searchNewsgroups.setSelection(true);
        else
            searchAll.setSelection(true);
        
        return composite;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        if (searchAll.getSelection())
            getPreferenceStore().setValue(getEngineId(), EclipseOrg.ALL);
        else if (searchArticles.getSelection())
            getPreferenceStore().setValue(getEngineId(), EclipseOrg.ARTICLES);
        else if (searchDoc.getSelection())
            getPreferenceStore().setValue(getEngineId(), EclipseOrg.DOC);
        else if (searchMail.getSelection())
            getPreferenceStore().setValue(getEngineId(), EclipseOrg.MAIL);
        else if (searchNewsgroups.getSelection())
            getPreferenceStore().setValue(getEngineId(), EclipseOrg.NEWS);
        return super.performOk();
    }
}
