/* -*-mode:java; c-basic-offset:2; -*- */
/**********************************************************************
Copyright (c) 2003, Atsuhiko Yamanaka, JCraft,Inc. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
**********************************************************************/
package org.eclipse.team.ccvs.ssh2;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class CVSSSH2PreferencePage extends FieldEditorPreferencePage
  implements IWorkbenchPreferencePage {

  public static String KEY_PROXY="CVSSSH2PreferencePage.PROXY";
  public static String KEY_PROXY_TYPE="CVSSSH2PreferencePage.PROXY_TYPE";
  public static String KEY_PROXY_HOST="CVSSSH2PreferencePage.PROXY_HOST";
  public static String KEY_PROXY_PORT="CVSSSH2PreferencePage.PROXY_PORT";
  public static String KEY_PROXY_AUTH="CVSSSH2PreferencePage.PROXY_AUTH";
  public static String KEY_PROXY_USER="CVSSSH2PreferencePage.PROXY_USER";
  public static String KEY_PROXY_PASS="CVSSSH2PreferencePage.PROXY_PASS";
  public static String KEY_SSH2HOME="CVSSSH2PreferencePage.SSH2HOME";

  static String SOCKS5="SOCKS5";
  static String HTTP="HTTP";
  private static String HTTP_DEFAULT_PORT="80";
  private static String SOCKS5_DEFAULT_PORT="1080";

  private DirectoryFieldEditor ssh2homeEditor;

  private Label proxyTypeLabel;
  private Label proxyHostLabel;
  private Label proxyPortLabel;
  private Label proxyUserLabel;
  private Label proxyPassLabel;
  private Combo proxyTypeCombo;
  private Text proxyHostText;
  private Text proxyPortText;
  private Text proxyUserText;
  private Text proxyPassText;
  private Button enableProxy;
  private Button enableAuth;
  private boolean useProxy;
  private boolean useAuth;

  public CVSSSH2PreferencePage() {
    super(GRID);
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    setPreferenceStore(store);
    setDescription("General CVSSSH2 Settings:");
  }

  protected void createFieldEditors() {
    ssh2homeEditor=
      new DirectoryFieldEditor(KEY_SSH2HOME, 
			       "SSH2 Home", 
			       getFieldEditorParent());
    addField(ssh2homeEditor);

    createSpacer(getFieldEditorParent(), 3);

    createProxy(getFieldEditorParent(), 3);
  }

  private void updateControls() {
    boolean enable=enableProxy.getSelection();
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

  public void init(IWorkbench workbench) {
  }

  protected void initialize() {
    super.initialize();
    initControls();
  }

  public static void initDefaults(IPreferenceStore store) {
    setDefault(store, KEY_SSH2HOME, JSchSession.default_ssh_home);
    setDefault(store, KEY_PROXY_TYPE, HTTP);
    setDefault(store, KEY_PROXY_PORT, HTTP_DEFAULT_PORT);
    setDefault(store, KEY_PROXY_AUTH, "false");
    setDefault(store, KEY_PROXY_USER, "");
    setDefault(store, KEY_PROXY_PASS, "");
  }

  private static void setDefault(IPreferenceStore store, String key, String value){
    store.setDefault(key, value);
    if(store.getString(key).length()==0)
      store.setValue(key, value);
  }

  private void initControls(){
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    useProxy=store.getString(KEY_PROXY).equals("true");
    enableProxy.setSelection(useProxy);
    proxyHostText.setText(store.getString(KEY_PROXY_HOST));
    proxyPortText.setText(store.getString(KEY_PROXY_PORT));
    proxyTypeCombo.select(store.getString(KEY_PROXY_TYPE).equals(HTTP)?0:1);
    useAuth=store.getString(KEY_PROXY_AUTH).equals("true");
    enableAuth.setSelection(useAuth);
    proxyUserText.setText(store.getString(KEY_PROXY_USER));
    proxyPassText.setText(store.getString(KEY_PROXY_PASS));
    proxyPassText.setEchoChar('*');
    updateControls();
  }
  protected void createProxy(Composite composite, int columnSpan) {
    Group group=new Group(composite, SWT.NONE);
    group.setText("Proxy settings");
    GridLayout layout=new GridLayout();
    layout.numColumns=2;
    group.setLayout(layout);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment=GridData.FILL;
    group.setLayoutData(gd);
    group.setFont(composite.getFont());

    enableProxy=new Button(group, SWT.CHECK);
    enableProxy.setText("Enable proxy connection");
    gd=new GridData();
    gd.horizontalSpan=2;
    enableProxy.setLayoutData(gd);

    proxyTypeLabel=new Label(group, SWT.NONE);
    proxyTypeLabel.setText("Proxy type");
    proxyTypeCombo=new Combo(group, SWT.READ_ONLY);
    proxyTypeCombo.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyTypeCombo.setLayoutData(gd);
    proxyTypeCombo.addModifyListener(new ModifyListener () {
	public void modifyText(ModifyEvent e){
	  if(proxyPortText==null) return;
	  Combo combo=(Combo)(e.getSource());
	  String foo=combo.getText();
	  if(foo.equals(HTTP)){ 
	    proxyPortText.setText(HTTP_DEFAULT_PORT); 
	  }
	  else if(foo.equals(SOCKS5)){
	    proxyPortText.setText(SOCKS5_DEFAULT_PORT);
	  }
	} 
      });
    proxyTypeCombo.add(HTTP);
    proxyTypeCombo.add(SOCKS5);
    proxyTypeCombo.select(0);

    proxyHostLabel=new Label(group, SWT.NONE);
    proxyHostLabel.setText("Proxy host address");

    proxyHostText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyHostText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyHostText.setLayoutData(gd);

    proxyPortLabel=new Label(group, SWT.NONE);
    proxyPortLabel.setText("Proxy host port");

    proxyPortText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPortText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyPortText.setLayoutData(gd);

    enableAuth=new Button(group, SWT.CHECK);
    enableAuth.setText("Enable proxy authentication");
    gd=new GridData();
    gd.horizontalSpan=2;
    enableAuth.setLayoutData(gd);

    proxyUserLabel=new Label(group, SWT.NONE);
    proxyUserLabel.setText("Proxy user name");

    proxyUserText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyUserText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyUserText.setLayoutData(gd);

    proxyPassLabel=new Label(group, SWT.NONE);
    proxyPassLabel.setText("Proxy password");

    proxyPassText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPassText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyPassText.setLayoutData(gd);


    //  performDefaults();

    enableProxy.addSelectionListener(new SelectionListener() {
	public void widgetSelected(SelectionEvent e) {
	  updateControls();
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}
      });

    enableAuth.addSelectionListener(new SelectionListener() {
	public void widgetSelected(SelectionEvent e) {
	  updateControls();
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}
      });
  }

  public boolean performOk() {
    boolean result=super.performOk();
    if(result){
      IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
      store.setValue(KEY_PROXY, enableProxy.getSelection());
      store.setValue(KEY_PROXY_TYPE, proxyTypeCombo.getText());
      store.setValue(KEY_PROXY_HOST, proxyHostText.getText());
      store.setValue(KEY_PROXY_PORT, proxyPortText.getText());

      store.setValue(KEY_PROXY_AUTH, enableAuth.getSelection());
      store.setValue(KEY_PROXY_USER, proxyUserText.getText());
      store.setValue(KEY_PROXY_PASS, proxyPassText.getText());
    }
    CVSSSH2Plugin.getDefault().savePluginPreferences();
    return result;
  }

  public void performApply() {
    super.performApply();
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    store.setValue(KEY_PROXY, enableProxy.getSelection());
    store.setValue(KEY_PROXY_TYPE, proxyTypeCombo.getText());
    store.setValue(KEY_PROXY_HOST, proxyHostText.getText());
    store.setValue(KEY_PROXY_PORT, proxyPortText.getText());

    store.setValue(KEY_PROXY_AUTH, enableAuth.getSelection());
    store.setValue(KEY_PROXY_USER, proxyUserText.getText());
    store.setValue(KEY_PROXY_PASS, proxyPassText.getText());
  }

  protected void performDefaults(){
    super.performDefaults();
    enableProxy.setSelection(false);
    proxyHostText.setText("");
    proxyPortText.setText(HTTP_DEFAULT_PORT);
    proxyTypeCombo.select(0);
    enableAuth.setSelection(false);
    proxyUserText.setText("");
    proxyPassText.setText("");
    updateControls();
  }

  protected void createSpacer(Composite composite, int columnSpan) {
    Label label=new Label(composite, SWT.NONE);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    label.setLayoutData(gd);
  }
}
