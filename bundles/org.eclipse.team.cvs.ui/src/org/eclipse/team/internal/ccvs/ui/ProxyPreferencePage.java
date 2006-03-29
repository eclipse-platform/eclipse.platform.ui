/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.ui.*;

public class ProxyPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private Label proxyTypeLabel;
    private Label proxyHostLabel;
    private Label proxyPortLabel;
    private Label proxyUserLabel;
    private Label proxyPassLabel;

    private Button enableProxy;
    private Combo proxyTypeCombo;
    private Text proxyHostText;
    private Text proxyPortText;
    private Button enableAuth;
    private Text proxyUserText;
    private Text proxyPassText;

  /*
   * @see PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent) {
      Composite composite = new Composite(parent, SWT.NULL);
      GridLayout layout = new GridLayout();
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      layout.numColumns = 2;
      composite.setLayout(layout);
      composite.setLayoutData(new GridData());
    
      GridData data = new GridData();
      data.horizontalAlignment = GridData.FILL;
      composite.setLayoutData(data);
      
      createProxyPage(composite);
       
      initializeDefaults();
      PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IHelpContextIds.PROXY_PREFERENCE_PAGE);
      Dialog.applyDialogFont(parent);
      return composite;
    }
  
  
    private void initializeDefaults() {
        IPreferenceStore store = getPreferenceStore();

        enableProxy.setSelection(store.getBoolean(ICVSUIConstants.PREF_USE_PROXY));

        proxyTypeCombo.select(store.getString(ICVSUIConstants.PREF_PROXY_TYPE).equals(CVSProviderPlugin.PROXY_TYPE_HTTP)? 0:1); 
        proxyHostText.setText(store.getString(ICVSUIConstants.PREF_PROXY_HOST));
        proxyPortText.setText(store.getString(ICVSUIConstants.PREF_PROXY_PORT));
        
        enableAuth.setSelection(store.getBoolean(ICVSUIConstants.PREF_PROXY_AUTH));        
        proxyUserText.setText(CVSProviderPlugin.getPlugin().getProxyUser());
        proxyPassText.setText(CVSProviderPlugin.getPlugin().getProxyPassword());

        // FIXME
        updateControls();
    }
  
    /*
     * @see IWorkbenchPreferencePage#init(IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }
  
    /*
     * @see IPreferencePage#performOk()
     */
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        
        store.setValue(ICVSUIConstants.PREF_USE_PROXY, enableProxy.getSelection());
        
        store.setValue(ICVSUIConstants.PREF_PROXY_TYPE, proxyTypeCombo.getText());
        store.setValue(ICVSUIConstants.PREF_PROXY_HOST, proxyHostText.getText());
        store.setValue(ICVSUIConstants.PREF_PROXY_PORT, proxyPortText.getText());

        store.setValue(ICVSUIConstants.PREF_PROXY_AUTH, enableAuth.getSelection());
        
        CVSProviderPlugin plugin = CVSProviderPlugin.getPlugin();
        
        plugin.setUseProxy(enableProxy.getSelection());

        plugin.setProxyType(proxyTypeCombo.getText());
        plugin.setProxyHost(proxyHostText.getText());
        plugin.setProxyPort(proxyPortText.getText());

        plugin.setUseProxyAuth(enableAuth.getSelection());
        plugin.setProxyAuth(proxyUserText.getText(), proxyPassText.getText());
                
        CVSUIPlugin.getPlugin().savePluginPreferences();
        return super.performOk();
    }
    
    /* 
     * @see PreferencePage#performDefaults()
     */
    protected void performDefaults() {
    	super.performDefaults();
        IPreferenceStore store = getPreferenceStore();
        store.setToDefault(ICVSUIConstants.PREF_USE_PROXY);
        store.setToDefault(ICVSUIConstants.PREF_PROXY_TYPE);
        store.setToDefault(ICVSUIConstants.PREF_PROXY_HOST);
        store.setToDefault(ICVSUIConstants.PREF_PROXY_PORT);
        store.setToDefault(ICVSUIConstants.PREF_PROXY_AUTH);
        CVSProviderPlugin.getPlugin().setProxyAuth("",""); //$NON-NLS-1$ //$NON-NLS-2$
        
        initializeDefaults();
    }
    

    /*
     * @see PreferencePage#doGetPreferenceStore()
     */
    protected IPreferenceStore doGetPreferenceStore() {
      return CVSUIPlugin.getPlugin().getPreferenceStore();
    }
  
    private void createProxyPage(Composite group) {
    
        enableProxy = new Button(group, SWT.CHECK);
        enableProxy.setText(CVSUIMessages.CVSProxyPreferencePage_enableProxy);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        enableProxy.setLayoutData(gd);
        enableProxy.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            updateControls();
          }
        });
    
        proxyTypeLabel = new Label(group, SWT.NONE);
        proxyTypeLabel.setText(CVSUIMessages.CVSProxyPreferencePage_proxyTpe);
        
        proxyTypeCombo = new Combo(group, SWT.READ_ONLY);
        proxyTypeCombo.setFont(group.getFont());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        proxyTypeCombo.setLayoutData(gd);
        proxyTypeCombo.add(CVSProviderPlugin.PROXY_TYPE_HTTP);
        proxyTypeCombo.add(CVSProviderPlugin.PROXY_TYPE_SOCKS5);
        proxyTypeCombo.select(0);
        proxyTypeCombo.addModifyListener(new ModifyListener() {
          public void modifyText(ModifyEvent e) {
            if(proxyPortText == null)
              return;
            Combo combo = (Combo) (e.getSource());
            String foo = combo.getText();
            if(foo.equals(CVSProviderPlugin.PROXY_TYPE_HTTP)) {
              proxyPortText.setText(CVSProviderPlugin.HTTP_DEFAULT_PORT);
            } else if(foo.equals(CVSProviderPlugin.PROXY_TYPE_SOCKS5)) {
              proxyPortText.setText(CVSProviderPlugin.SOCKS5_DEFAULT_PORT);
            }
          }
        });
    
        proxyHostLabel = new Label(group, SWT.NONE);
        proxyHostLabel.setText(CVSUIMessages.CVSProxyPreferencePage_proxyHost);
    
        proxyHostText = new Text(group, SWT.SINGLE | SWT.BORDER);
        proxyHostText.setFont(group.getFont());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        proxyHostText.setLayoutData(gd);
    
        proxyPortLabel = new Label(group, SWT.NONE);
        proxyPortLabel.setText(CVSUIMessages.CVSProxyPreferencePage_proxyPort);
    
        proxyPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
        proxyPortText.setFont(group.getFont());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        proxyPortText.setLayoutData(gd);
    
        proxyPortText.addModifyListener(new ModifyListener() {
          public void modifyText(ModifyEvent e) {
            if(isValidPort(proxyPortText.getText())) {
              setErrorMessage(null);
            }
          }
        });
    
        createSpacer(group, 2);
    
        enableAuth = new Button(group, SWT.CHECK);
        enableAuth.setText(CVSUIMessages.CVSProxyPreferencePage_enableProxyAuth);
        gd = new GridData();
        gd.horizontalSpan = 2;
        enableAuth.setLayoutData(gd);
        enableAuth.addSelectionListener(new SelectionAdapter() {
          public void widgetSelected(SelectionEvent e) {
            updateControls();
          }
        });
    
        proxyUserLabel = new Label(group, SWT.NONE);
        proxyUserLabel.setText(CVSUIMessages.CVSProxyPreferencePage_proxyUser);
    
        proxyUserText = new Text(group, SWT.SINGLE | SWT.BORDER);
        proxyUserText.setFont(group.getFont());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        proxyUserText.setLayoutData(gd);
    
        proxyPassLabel = new Label(group, SWT.NONE);
        proxyPassLabel.setText(CVSUIMessages.CVSProxyPreferencePage_proxyPass);
    
        proxyPassText = new Text(group, SWT.SINGLE | SWT.BORDER);
        proxyPassText.setEchoChar('*');
        proxyPassText.setFont(group.getFont());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        proxyPassText.setLayoutData(gd);
    
        //  performDefaults();
    }

    private boolean isValidPort(String port){
        int i = -1;
        try {   
            i = Integer.parseInt(port);
        } catch (NumberFormatException ee) {
//          setErrorMessage(Policy.bind("CVSSSH2PreferencePage.103")); //$NON-NLS-1$
//          return false;
        }
        if(i < 0 || i > 65535){
            setErrorMessage(CVSUIMessages.CVSProxyPreferencePage_proxyPortError); 
            return false;
        }
        return true;
    }
    
    protected void createSpacer(Composite composite, int columnSpan) {
      Label label = new Label(composite, SWT.NONE);
      GridData gd = new GridData();
      gd.horizontalSpan = columnSpan;
      label.setLayoutData(gd);
    }
    
    private void updateControls() {
      boolean enable = enableProxy.getSelection();
      proxyTypeLabel.setEnabled(enable);
      proxyTypeCombo.setEnabled(enable);
      proxyPortLabel.setEnabled(enable);
      proxyPortText.setEnabled(enable);
      proxyHostLabel.setEnabled(enable);
      proxyHostText.setEnabled(enable);

      enableAuth.setEnabled(enable);
      enable&=enableAuth.getSelection();
      proxyUserLabel.setEnabled(enable);
      proxyUserText.setEnabled(enable);
      proxyPassLabel.setEnabled(enable);
      proxyPassText.setEnabled(enable);
    }
    
}
