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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.custom.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.eclipse.team.ccvs.ssh2.Policy;

public class CVSSSH2PreferencePage extends PreferencePage
  implements IWorkbenchPreferencePage {

  public static String KEY_PROXY="CVSSSH2PreferencePage.PROXY"; //$NON-NLS-1$
  public static String KEY_PROXY_TYPE="CVSSSH2PreferencePage.PROXY_TYPE"; //$NON-NLS-1$
  public static String KEY_PROXY_HOST="CVSSSH2PreferencePage.PROXY_HOST"; //$NON-NLS-1$
  public static String KEY_PROXY_PORT="CVSSSH2PreferencePage.PROXY_PORT"; //$NON-NLS-1$
  public static String KEY_PROXY_AUTH="CVSSSH2PreferencePage.PROXY_AUTH"; //$NON-NLS-1$
  public static String KEY_PROXY_USER="CVSSSH2PreferencePage.PROXY_USER"; //$NON-NLS-1$
  public static String KEY_PROXY_PASS="CVSSSH2PreferencePage.PROXY_PASS"; //$NON-NLS-1$
  public static String KEY_SSH2HOME="CVSSSH2PreferencePage.SSH2HOME"; //$NON-NLS-1$
  public static String KEY_KEYFILE="CVSSSH2PreferencePage.KEYFILE"; //$NON-NLS-1$
  public static String KEY_PRIVATEKEY="CVSSSH2PreferencePage.PRIVATEKEY"; //$NON-NLS-1$
  
  // Temporary preference for using ssh2 instead of ssh1
  public static String KEY_USE_SSH2="CVSSSH2PreferencePage.SSH2_USE_SSH2"; //$NON-NLS-1$

  static String SOCKS5="SOCKS5"; //$NON-NLS-1$
  static String HTTP="HTTP"; //$NON-NLS-1$
  private static String HTTP_DEFAULT_PORT="80"; //$NON-NLS-1$
  private static String SOCKS5_DEFAULT_PORT="1080"; //$NON-NLS-1$
  private static String privatekeys="id_dsa,id_rsa"; //$NON-NLS-1$

  static String DSA="DSA"; //$NON-NLS-1$
  static String RSA="RSA"; //$NON-NLS-1$

//  private DirectoryFieldEditor ssh2homeEditor;

  private Label ssh2HomeLabel;
  private Label proxyTypeLabel;
  private Label proxyHostLabel;
  private Label proxyPortLabel;
  private Label proxyUserLabel;
  private Label proxyPassLabel;
  private Label privateKeyLabel;
  private Combo proxyTypeCombo;
  private Text ssh2HomeText;
  private Text proxyHostText;
  private Text proxyPortText;
  private Text proxyUserText;
  private Text proxyPassText;
  private Text privateKeyText;
  private Button enableProxy;
  private Button enableAuth;
  private Button enableSSH2;
  private Button privateKeyAdd;
  private boolean useProxy;
  private boolean useAuth;

  private Label keyTypeLabel;
  private Combo keyTypeCombo;
  private Button ssh2HomeBrowse;
  private Button keyGenerate;
  private Button saveKeyPair;
  private Label keyCommentLabel;
  private Text keyCommentText;
  private Label keyFingerPrintLabel;
  private Text keyFingerPrintText;
  private Label keyPassphrase1Label;
  private Text keyPassphrase1Text;
  private Label keyPassphrase2Label;
  private Text keyPassphrase2Text;
  private Label publicKeylabel;
  private Text publicKeyText;
  private KeyPair kpair=null;
  private String kpairComment;

  public CVSSSH2PreferencePage() {
//    super(GRID);
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    setPreferenceStore(store);
    setDescription(Policy.bind("CVSSSH2PreferencePage.18")); //$NON-NLS-1$
  }

  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);

    TabFolder tabFolder = new TabFolder(container, SWT.NONE);
    tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(Policy.bind("CVSSSH2PreferencePage.19")); //$NON-NLS-1$
    tabItem.setControl(createGeneralPage(tabFolder));

    tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(Policy.bind("CVSSSH2PreferencePage.20")); //$NON-NLS-1$
    tabItem.setControl(createProxyPage(tabFolder));

    tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(Policy.bind("CVSSSH2PreferencePage.21")); //$NON-NLS-1$
    tabItem.setControl(createKeyManagementPage(tabFolder));

    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    initDefaults(store);
    initControls();

    Dialog.applyDialogFont(parent);
    return container;
  }

  private Control createGeneralPage(Composite parent) {
    Composite group=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    group.setLayout(layout);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    group.setLayoutData(data);

    enableSSH2=new Button(group, SWT.CHECK);
    enableSSH2.setText(Policy.bind("CVSSSH2PreferencePage.22")); //$NON-NLS-1$
    GridData gd=new GridData();
    gd.horizontalSpan=3;
    enableSSH2.setLayoutData(gd);

    createSpacer(group, 3);

    ssh2HomeLabel=new Label(group, SWT.NONE);
    ssh2HomeLabel.setText(Policy.bind("CVSSSH2PreferencePage.23")); //$NON-NLS-1$

    ssh2HomeText=new Text(group, SWT.SINGLE | SWT.BORDER);
    ssh2HomeText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=1;
    ssh2HomeText.setLayoutData(gd);

    ssh2HomeBrowse=new Button(group, SWT.NULL);
    ssh2HomeBrowse.setText(Policy.bind("CVSSSH2PreferencePage.24")); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=1;
    ssh2HomeBrowse.setLayoutData(gd);


    createSpacer(group, 3);

    privateKeyLabel=new Label(group, SWT.NONE);
    privateKeyLabel.setText(Policy.bind("CVSSSH2PreferencePage.25")); //$NON-NLS-1$

    privateKeyText=new Text(group, SWT.SINGLE | SWT.BORDER);
    privateKeyText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=1;
    privateKeyText.setLayoutData(gd);

    privateKeyAdd=new Button(group, SWT.NULL);
    privateKeyAdd.setText(Policy.bind("CVSSSH2PreferencePage.26")); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=1;
    privateKeyAdd.setLayoutData(gd);

    ssh2HomeBrowse.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  String home=ssh2HomeText.getText();

	  if(!new File(home).exists()){
	    while(true){
	      int foo=home.lastIndexOf(java.io.File.separator, home.length());
	      if(foo==-1)break;
	      home=home.substring(0, foo);
	      if(new File(home).exists())break;
	    }
	  }

	  DirectoryDialog dd=new DirectoryDialog(getShell());
	  dd.setFilterPath(home);
	  dd.setMessage(Policy.bind("CVSSSH2PreferencePage.27")); //$NON-NLS-1$
	  String dir=dd.open();
	  if(dir==null){ // cancel
	    return;
	  }
	  ssh2HomeText.setText(dir);
	}
      });

    privateKeyAdd.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  String home=ssh2HomeText.getText();

	  FileDialog fd=new FileDialog(getShell(), SWT.OPEN|SWT.MULTI);
	  fd.setFilterPath(home);
	  Object o=fd.open();
	  if(o==null){ // cancel
	    return;
	  }
	  String[] files=fd.getFileNames();
	  String keys=privateKeyText.getText();
	  String dir=fd.getFilterPath();
	  if(dir.equals(home)){dir="";} //$NON-NLS-1$
	  else{dir+=java.io.File.separator;}

	  for(int i=0; i<files.length; i++){
	    String foo=files[i];
	    if(keys.length()!=0)keys=keys+",";
	    keys=keys+dir+foo;
	  }
	  privateKeyText.setText(keys);
	}
      });

    return group;
  }

  private Control createProxyPage(Composite parent) {
    Composite group=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    group.setLayout(layout);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    group.setLayoutData(data);

    enableProxy=new Button(group, SWT.CHECK);
    enableProxy.setText(Policy.bind("CVSSSH2PreferencePage.30")); //$NON-NLS-1$
    GridData gd=new GridData();
    gd.horizontalSpan=3;
    enableProxy.setLayoutData(gd);

    proxyTypeLabel=new Label(group, SWT.NONE);
    proxyTypeLabel.setText(Policy.bind("CVSSSH2PreferencePage.31")); //$NON-NLS-1$
    proxyTypeCombo=new Combo(group, SWT.READ_ONLY);
    proxyTypeCombo.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
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
    proxyHostLabel.setText(Policy.bind("CVSSSH2PreferencePage.32")); //$NON-NLS-1$

    proxyHostText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyHostText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    proxyHostText.setLayoutData(gd);

    proxyPortLabel=new Label(group, SWT.NONE);
    proxyPortLabel.setText(Policy.bind("CVSSSH2PreferencePage.33")); //$NON-NLS-1$

    proxyPortText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPortText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    proxyPortText.setLayoutData(gd);
    
    createSpacer(group, 3);

    enableAuth=new Button(group, SWT.CHECK);
    enableAuth.setText(Policy.bind("CVSSSH2PreferencePage.34")); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=3;
    enableAuth.setLayoutData(gd);

    proxyUserLabel=new Label(group, SWT.NONE);
    proxyUserLabel.setText(Policy.bind("CVSSSH2PreferencePage.35")); //$NON-NLS-1$

    proxyUserText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyUserText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    proxyUserText.setLayoutData(gd);

    proxyPassLabel=new Label(group, SWT.NONE);
    proxyPassLabel.setText(Policy.bind("CVSSSH2PreferencePage.36")); //$NON-NLS-1$

    proxyPassText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPassText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
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
    return group;
  }

  private Control createKeyManagementPage(Composite parent) {
    int columnSpan=3;
    Composite group=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    group.setLayout(layout);
    GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    group.setLayoutData(data);

    keyTypeLabel=new Label(group, SWT.NONE);
    keyTypeLabel.setText(Policy.bind("CVSSSH2PreferencePage.37")); //$NON-NLS-1$
    keyTypeCombo=new Combo(group, SWT.READ_ONLY);
    keyTypeCombo.setFont(group.getFont());
    GridData gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyTypeCombo.setLayoutData(gd);

    keyGenerate=new Button(group, SWT.NULL);
    keyGenerate.setText(Policy.bind("CVSSSH2PreferencePage.38")); //$NON-NLS-1$
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    keyGenerate.setLayoutData(gd);

/*
    keyLoad=new Button(group, SWT.NULL);
    keyLoad.setText("Load(not implemented)");
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    keyLoad.setLayoutData(gd);
*/

    publicKeylabel=new Label(group, SWT.NONE);
    publicKeylabel.setText(Policy.bind("CVSSSH2PreferencePage.39")); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    publicKeylabel.setLayoutData(gd);

    publicKeyText=new Text(group,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.WRAP);
    publicKeyText.setText(""); //$NON-NLS-1$
    publicKeyText.setEditable(false);
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    publicKeyText.setLayoutData(gd);

    keyFingerPrintLabel=new Label(group, SWT.NONE);
    keyFingerPrintLabel.setText(Policy.bind("CVSSSH2PreferencePage.41")); //$NON-NLS-1$
    keyFingerPrintText=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyFingerPrintText.setFont(group.getFont());
    keyFingerPrintText.setEditable(false);
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyFingerPrintText.setLayoutData(gd);

    keyCommentLabel=new Label(group, SWT.NONE);
    keyCommentLabel.setText(Policy.bind("CVSSSH2PreferencePage.42")); //$NON-NLS-1$
    keyCommentText=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyCommentText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyCommentText.setLayoutData(gd);
    
    keyCommentText.addModifyListener(new ModifyListener(){
    	public void modifyText(ModifyEvent e){
    		if(kpair==null)return;
    		try{
    			ByteArrayOutputStream out=new ByteArrayOutputStream();
    			kpair.writePublicKey(out, keyCommentText.getText());
    			out.close();
    			publicKeyText.setText(out.toString());
    		}
    		catch(IOException ee){}
    }});

    keyPassphrase1Label=new Label(group, SWT.NONE);
    keyPassphrase1Label.setText(Policy.bind("CVSSSH2PreferencePage.43")); //$NON-NLS-1$
    keyPassphrase1Text=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyPassphrase1Text.setFont(group.getFont());
    keyPassphrase1Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyPassphrase1Text.setLayoutData(gd);

    keyPassphrase2Label=new Label(group, SWT.NONE);
    keyPassphrase2Label.setText(Policy.bind("CVSSSH2PreferencePage.44")); //$NON-NLS-1$
    keyPassphrase2Text=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyPassphrase2Text.setFont(group.getFont());
    keyPassphrase2Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyPassphrase2Text.setLayoutData(gd);

    keyPassphrase1Text.addModifyListener(new ModifyListener(){
    	public void modifyText(ModifyEvent e){
    		String pass1=keyPassphrase1Text.getText();
    		String pass2=keyPassphrase2Text.getText();
    		if(pass2.length()==0){
    			setErrorMessage(null);
    			return;
    		}
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
    		}
    	}
    });	

    keyPassphrase2Text.addModifyListener(new ModifyListener(){
    	public void modifyText(ModifyEvent e){
    		String pass1=keyPassphrase1Text.getText();
    		String pass2=keyPassphrase2Text.getText();
    		if(pass2.length()<pass1.length()){
    			if(pass1.startsWith(pass2)){
    				setErrorMessage(null);
    			}
    			else{
    				setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
    			}
    			return;
    		}
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
    		}
    	}
    });
    
    keyPassphrase2Text.addFocusListener(new FocusListener(){
    	public void focusGained(FocusEvent e){
    		String pass1=keyPassphrase1Text.getText();
    		String pass2=keyPassphrase2Text.getText();
    		if(pass2.length()<pass1.length()){
    			if(pass1.startsWith(pass2)){
    				setErrorMessage(null);
    			}
    			else{
    				setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
    			}
    			return;
    		}
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
    		}   	
    	}
    	public void focusLost(FocusEvent e){
    		String pass1=keyPassphrase1Text.getText();
    		String pass2=keyPassphrase2Text.getText();
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
    		}
    	}
    });

    saveKeyPair=new Button(group, SWT.NULL);
    saveKeyPair.setText(Policy.bind("CVSSSH2PreferencePage.45")); //$NON-NLS-1$
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    saveKeyPair.setLayoutData(gd);

    keyGenerate.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  JSch jsch=JSchSession.getJSch();
	  boolean ok=true;
	  String _type=keyTypeCombo.getText();

	  try{
	    int type=0;
	    if(_type.equals(DSA)){
	      type=KeyPair.DSA;
	    }
	    else if(_type.equals(RSA)){
	      type=KeyPair.RSA;
	    }
	    else{
	      return;
	    }

	    final KeyPair[] _kpair=new KeyPair[1];
	    final JSch _jsch=jsch;
	    final int __type=type;
	    final JSchException[] _e=new JSchException[1];
	    BusyIndicator.showWhile(getShell().getDisplay(),
	    		new Runnable(){
	    	public void run(){
	    		try{
	    		  _kpair[0]=KeyPair.genKeyPair(_jsch, __type);
	    		}catch(JSchException e){
	    		  _e[0]=e;
	    		}
	    	}}
	    		);
	    if(_e[0]!=null){
	    	throw _e[0];
	    }
	    kpair=_kpair[0];
	    
	    ByteArrayOutputStream out=new ByteArrayOutputStream();
	    kpairComment=_type+"-1024"; //$NON-NLS-1$
	    kpair.writePublicKey(out, kpairComment);
	    out.close();
	    publicKeyText.setText(out.toString());
	    keyFingerPrintText.setText(kpair.getFingerPrint());
	    keyCommentText.setText(kpairComment);
	    updateControls();
	  }
	  catch(IOException ee){
	    ok=false;
	  }
	  catch(JSchException ee){
	    ok=false;
	  }
	  if(!ok){
	    MessageBox mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_ERROR);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.error")); //$NON-NLS-1$
	    mb.setMessage(Policy.bind("CVSSSH2PreferencePage.47")); //$NON-NLS-1$
	    mb.open();
	  }
	}
      });

    saveKeyPair.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  if(kpair==null)return;

	  MessageBox mb;
	  String pass=keyPassphrase1Text.getText();
	  if(!pass.equals(keyPassphrase2Text.getText())){
	    setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
	    return;
	  }
	  if(pass.length()==0){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_WARNING);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.warning")); //$NON-NLS-1$
	    mb.setMessage(Policy.bind("CVSSSH2PreferencePage.49")); //$NON-NLS-1$
	    if(mb.open()==SWT.NO){
	      return;
	    }
	  }

	  kpair.setPassphrase(pass);

	  IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
	  String home=ssh2HomeText.getText();

	  File _home=new File(home);

	  if(!_home.exists()){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_QUESTION);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.question")); //$NON-NLS-1$
	    mb.setMessage(home+Policy.bind("CVSSSH2PreferencePage.50")); //$NON-NLS-1$
	    if(mb.open()==SWT.NO){
	      return;
	    }
	    if(!_home.mkdirs()){
	      return;
	    }
	  }

	  FileDialog fd=new FileDialog(getShell(), SWT.SAVE);
	  fd.setFilterPath(home);
	  String file=(kpair.getKeyType()==KeyPair.RSA) ? "id_rsa" : "id_dsa"; //$NON-NLS-1$ //$NON-NLS-2$
	  fd.setFileName(file);
	  file=fd.open();
	  if(file==null){ // cancel
	    return;
	  }

	  if(new File(file).exists()){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_WARNING);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.warning")); //$NON-NLS-1$
	    mb.setMessage(file+Policy.bind("CVSSSH2PreferencePage.53")); //$NON-NLS-1$
	    if(mb.open()==SWT.NO){
	      return;
	    }
	  }

	  boolean ok=true;
	  try{
	    kpair.writePrivateKey(file);
	    kpair.writePublicKey(file+".pub", kpairComment); //$NON-NLS-1$
	  }
	  catch(Exception ee){
	    ok=false;
	  }

	  if(ok){
	    mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_INFORMATION);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.information")); //$NON-NLS-1$
	    mb.setMessage(Policy.bind("CVSSSH2PreferencePage.55")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
			  Policy.bind("CVSSSH2PreferencePage.57")+file+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
			  Policy.bind("CVSSSH2PreferencePage.59")+file+".pub"); //$NON-NLS-1$ //$NON-NLS-2$
	    mb.open();
	  }
	}
      });

    keyTypeCombo.add(DSA);
    keyTypeCombo.add(RSA);
    keyTypeCombo.select(0);

    return group;
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

    enable=(kpair!=null);
    publicKeylabel.setEnabled(enable);
    publicKeyText.setEnabled(enable);
    keyFingerPrintLabel.setEnabled(enable);
    keyFingerPrintText.setEnabled(enable);
    keyCommentLabel.setEnabled(enable);
    keyCommentText.setEnabled(enable);
    keyPassphrase1Label.setEnabled(enable);
    keyPassphrase1Text.setEnabled(enable);
    keyPassphrase2Label.setEnabled(enable);
    keyPassphrase2Text.setEnabled(enable);
    saveKeyPair.setEnabled(enable);
  }

  public void init(IWorkbench workbench) {
//    super.init(workbench);
//    initControls();
  }

  public void initialize() {
    initControls();
  }

  public static void initDefaults(IPreferenceStore store) {
    setDefault(store, KEY_SSH2HOME, JSchSession.default_ssh_home);
    setDefault(store, KEY_PRIVATEKEY, privatekeys);
    setDefault(store, KEY_PROXY_TYPE, HTTP);
    setDefault(store, KEY_PROXY_PORT, HTTP_DEFAULT_PORT);
    setDefault(store, KEY_PROXY_AUTH, "false"); //$NON-NLS-1$
    setDefault(store, KEY_PROXY_USER, ""); //$NON-NLS-1$
    setDefault(store, KEY_PROXY_PASS, ""); //$NON-NLS-1$
    store.setDefault(KEY_USE_SSH2, false);
  }

  private static void setDefault(IPreferenceStore store, String key, String value){
    store.setDefault(key, value);
    if(store.getString(key).length()==0)
      store.setValue(key, value);
  }

  private void initControls(){
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    ssh2HomeText.setText(store.getString(KEY_SSH2HOME));
    privateKeyText.setText(store.getString(KEY_PRIVATEKEY));
    enableSSH2.setSelection(store.getBoolean(KEY_USE_SSH2));
    useProxy=store.getString(KEY_PROXY).equals("true"); //$NON-NLS-1$
    enableProxy.setSelection(useProxy);
    proxyHostText.setText(store.getString(KEY_PROXY_HOST));
    proxyPortText.setText(store.getString(KEY_PROXY_PORT));
    proxyTypeCombo.select(store.getString(KEY_PROXY_TYPE).equals(HTTP)?0:1);
    useAuth=store.getString(KEY_PROXY_AUTH).equals("true"); //$NON-NLS-1$
    enableAuth.setSelection(useAuth);
    proxyUserText.setText(store.getString(KEY_PROXY_USER));
    proxyPassText.setText(store.getString(KEY_PROXY_PASS));
    proxyPassText.setEchoChar('*');
    updateControls();
  }
  protected void createProxy(Composite composite, int columnSpan) {
    Group group=new Group(composite, SWT.NONE);
    group.setText(Policy.bind("CVSSSH2PreferencePage.66")); //$NON-NLS-1$
    GridLayout layout=new GridLayout();
    layout.numColumns=2;
    group.setLayout(layout);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment=GridData.FILL;
    group.setLayoutData(gd);
    group.setFont(composite.getFont());
    
    enableProxy=new Button(group, SWT.CHECK);
    enableProxy.setText(Policy.bind("CVSSSH2PreferencePage.67")); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=2;
    enableProxy.setLayoutData(gd);

    proxyTypeLabel=new Label(group, SWT.NONE);
    proxyTypeLabel.setText(Policy.bind("CVSSSH2PreferencePage.68")); //$NON-NLS-1$
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
    proxyHostLabel.setText(Policy.bind("CVSSSH2PreferencePage.69")); //$NON-NLS-1$

    proxyHostText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyHostText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyHostText.setLayoutData(gd);

    proxyPortLabel=new Label(group, SWT.NONE);
    proxyPortLabel.setText(Policy.bind("CVSSSH2PreferencePage.70")); //$NON-NLS-1$

    proxyPortText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPortText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyPortText.setLayoutData(gd);

    createSpacer(group, 3);

    enableAuth=new Button(group, SWT.CHECK);
    enableAuth.setText(Policy.bind("CVSSSH2PreferencePage.71")); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=2;
    enableAuth.setLayoutData(gd);

    proxyUserLabel=new Label(group, SWT.NONE);
    proxyUserLabel.setText(Policy.bind("CVSSSH2PreferencePage.72")); //$NON-NLS-1$

    proxyUserText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyUserText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    proxyUserText.setLayoutData(gd);

    proxyPassLabel=new Label(group, SWT.NONE);
    proxyPassLabel.setText(Policy.bind("CVSSSH2PreferencePage.73")); //$NON-NLS-1$

    proxyPassText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPassText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
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

  protected void createKeyGeneration(Composite composite, int columnSpan) {
    Group group=new Group(composite, SWT.NONE);
    group.setText(Policy.bind("CVSSSH2PreferencePage.74")); //$NON-NLS-1$
    GridLayout layout=new GridLayout();
    layout.numColumns=2;
    group.setLayout(layout);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment=GridData.FILL;
    group.setLayoutData(gd);
    group.setFont(composite.getFont());
    
    keyTypeLabel=new Label(group, SWT.NONE);
    keyTypeLabel.setText(Policy.bind("CVSSSH2PreferencePage.75")); //$NON-NLS-1$
    keyTypeCombo=new Combo(group, SWT.READ_ONLY);
    keyTypeCombo.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    keyTypeCombo.setLayoutData(gd);

    keyGenerate=new Button(group, SWT.NULL);
    keyGenerate.setText(Policy.bind("CVSSSH2PreferencePage.76")); //$NON-NLS-1$
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    keyGenerate.setLayoutData(gd);

    /*
    keyLoad=new Button(group, SWT.NULL);
    keyLoad.setText("Load(not implemented)");
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    keyLoad.setLayoutData(gd);
    */

    publicKeylabel=new Label(group, SWT.NONE);
    publicKeylabel.setText(Policy.bind("CVSSSH2PreferencePage.77")); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    publicKeylabel.setLayoutData(gd);

    publicKeyText=new Text(group,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
    publicKeyText.setText(""); //$NON-NLS-1$
    publicKeyText.setEditable(false);
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    publicKeyText.setLayoutData(gd);

    keyFingerPrintLabel=new Label(group, SWT.NONE);
    keyFingerPrintLabel.setText(Policy.bind("CVSSSH2PreferencePage.79")); //$NON-NLS-1$
    keyFingerPrintText=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyFingerPrintText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyFingerPrintText.setLayoutData(gd);

    keyCommentLabel=new Label(group, SWT.NONE);
    keyCommentLabel.setText(Policy.bind("CVSSSH2PreferencePage.80")); //$NON-NLS-1$
    keyCommentText=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyCommentText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyCommentText.setLayoutData(gd);

    keyPassphrase1Label=new Label(group, SWT.NONE);
    keyPassphrase1Label.setText(Policy.bind("CVSSSH2PreferencePage.81")); //$NON-NLS-1$
    keyPassphrase1Text=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyPassphrase1Text.setFont(group.getFont());
    keyPassphrase1Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyPassphrase1Text.setLayoutData(gd);

    keyPassphrase2Label=new Label(group, SWT.NONE);
    keyPassphrase2Label.setText(Policy.bind("CVSSSH2PreferencePage.82")); //$NON-NLS-1$
    keyPassphrase2Text=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyPassphrase2Text.setFont(group.getFont());
    keyPassphrase2Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyPassphrase2Text.setLayoutData(gd);

    saveKeyPair=new Button(group, SWT.NULL);
    saveKeyPair.setText(Policy.bind("CVSSSH2PreferencePage.83")); //$NON-NLS-1$
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    saveKeyPair.setLayoutData(gd);

    keyGenerate.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  JSch jsch=JSchSession.getJSch();
	  boolean ok=true;
	  String _type=keyTypeCombo.getText();

	  try{
	    int type=0;
	    if(_type.equals(DSA)){
	      type=KeyPair.DSA;
	    }
	    else if(_type.equals(RSA)){
	      type=KeyPair.RSA;
	    }
	    else{
	      return;
	    }

	    kpair=KeyPair.genKeyPair(jsch, type);
	    ByteArrayOutputStream out=new ByteArrayOutputStream();
	    kpairComment=_type+"-1024"; //$NON-NLS-1$
	    kpair.writePublicKey(out, kpairComment);
	    out.close();
	    publicKeyText.setText(out.toString());
	    keyFingerPrintText.setText(kpair.getFingerPrint());
	    keyCommentText.setText(kpairComment);
	    updateControls();
	  }
	  catch(IOException ee){
	    ok=false;
	  }
	  catch(JSchException ee){
	    ok=false;
	  }
	  MessageBox mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_INFORMATION);
	  mb.setText(Policy.bind("CVSSSH2PreferencePage.information")); //$NON-NLS-1$
	  mb.setMessage(_type+Policy.bind("CVSSSH2PreferencePage.85")); //$NON-NLS-1$
	  mb.open();
	}
      });

    saveKeyPair.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  if(kpair==null)return;

	  MessageBox mb;
	  String pass=keyPassphrase1Text.getText();
	  if(!pass.equals(keyPassphrase2Text.getText())){
	    mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_ERROR);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.error")); //$NON-NLS-1$
	    mb.setMessage(Policy.bind("CVSSSH2PreferencePage.86")); //$NON-NLS-1$
	    mb.open();
	    return;
	  }
	  if(pass.length()==0){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_WARNING);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.awrning")); //$NON-NLS-1$
	    mb.setMessage(Policy.bind("CVSSSH2PreferencePage.87")); //$NON-NLS-1$
	    if(mb.open()==SWT.NO){
	      return;
	    }
	  }

	  kpair.setPassphrase(pass);

	  IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
	  String home=ssh2HomeText.getText();

	  File _home=new File(home);

	  if(!_home.exists()){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_QUESTION);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.question")); //$NON-NLS-1$
	    mb.setMessage(home+Policy.bind("CVSSSH2PreferencePage.88")); //$NON-NLS-1$
	    if(mb.open()==SWT.NO){
	      return;
	    }
	    if(!_home.mkdirs()){
	      return;
	    }
	  }

	  FileDialog fd=new FileDialog(getShell(), SWT.SAVE);
	  fd.setFilterPath(home);
	  String file=(kpair.getKeyType()==KeyPair.RSA) ? "id_rsa" : "id_dsa"; //$NON-NLS-1$ //$NON-NLS-2$
	  fd.setFileName(file);
	  file=fd.open();
	  if(file==null){ // cancel
	    return;
	  }

	  if(new File(file).exists()){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_WARNING);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.warning")); //$NON-NLS-1$
	    mb.setMessage(file+Policy.bind("CVSSSH2PreferencePage.91")); //$NON-NLS-1$
	    if(mb.open()==SWT.NO){
	      return;
	    }
	  }

	  boolean ok=true;
	  try{
	    kpair.writePrivateKey(file);
	    kpair.writePublicKey(file+".pub", kpairComment); //$NON-NLS-1$
	  }
	  catch(Exception ee){
	    ok=false;
	  }

	  if(ok){
	    mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_INFORMATION);
	    mb.setText(Policy.bind("CVSSSH2PreferencePage.information")); //$NON-NLS-1$
	    mb.setMessage(Policy.bind("CVSSSH2PreferencePage.93")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
			  Policy.bind("CVSSSH2PreferencePage.95")+file+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
			  Policy.bind("CVSSSH2PreferencePage.97")+file+".pub"); //$NON-NLS-1$ //$NON-NLS-2$
	    mb.open();
	  }
	}
      });

    keyTypeCombo.add(DSA);
    keyTypeCombo.add(RSA);
    keyTypeCombo.select(0);
  }

  public boolean performOk() {
    boolean result=super.performOk();
    if(result){
    	setErrorMessage(null);
      String home=ssh2HomeText.getText();
      File _home=new File(home);
      if(!_home.exists()){
	MessageBox mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_QUESTION);
	mb.setText(Policy.bind("CVSSSH2PreferencePage.question")); //$NON-NLS-1$
	mb.setMessage(home+Policy.bind("CVSSSH2PreferencePage.99")); //$NON-NLS-1$
	if(mb.open()==SWT.YES){
	  if(!(_home.mkdirs())){
	  setErrorMessage(Policy.bind("CVSSSH2PreferencePage.100")+home); //$NON-NLS-1$
	  return false;
	  }
	}
      }

      {
      int i=-1;
      try {	
      	i=Integer.parseInt(proxyPortText.getText());
      }
      catch (NumberFormatException ee) {
      	setErrorMessage(Policy.bind("CVSSSH2PreferencePage.103")); //$NON-NLS-1$
      	return false;
      }
      if((i < 0) || (i > 65535)){
      	setErrorMessage(Policy.bind("CVSSSH2PreferencePage.104")); //$NON-NLS-1$
      	return false;
      }
      }
      
      IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
      store.setValue(KEY_SSH2HOME, home);
      store.setValue(KEY_PRIVATEKEY, privateKeyText.getText());
      store.setValue(KEY_PROXY, enableProxy.getSelection());
      store.setValue(KEY_PROXY_TYPE, proxyTypeCombo.getText());
      store.setValue(KEY_PROXY_HOST, proxyHostText.getText());
      store.setValue(KEY_PROXY_PORT, proxyPortText.getText());

      store.setValue(KEY_PROXY_AUTH, enableAuth.getSelection());
      store.setValue(KEY_PROXY_USER, proxyUserText.getText());
      store.setValue(KEY_PROXY_PASS, proxyPassText.getText());
      
      store.setValue(KEY_USE_SSH2, enableSSH2.getSelection());
    }
    CVSSSH2Plugin.getDefault().savePluginPreferences();
    return result;
  }

  public void performApply() {
    super.performApply();

    setErrorMessage(null);
    
    String home=ssh2HomeText.getText();
    File _home=new File(home);
    if(!_home.exists()){
      MessageBox mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_QUESTION);
      mb.setText(Policy.bind("CVSSSH2PreferencePage.question")); //$NON-NLS-1$      
      mb.setMessage(home+Policy.bind("CVSSSH2PreferencePage.101")); //$NON-NLS-1$
      if(mb.open()==SWT.YES){
	if(!(_home.mkdirs())){
		setErrorMessage(Policy.bind("CVSSSH2PreferencePage.102")+home); //$NON-NLS-1$
		return;
	}
      }
    }

    {
    	int i=-1;
    	try {	
    		i=Integer.parseInt(proxyPortText.getText());
    	}
    	catch (NumberFormatException ee) {
    		setErrorMessage(Policy.bind("CVSSSH2PreferencePage.103")); //$NON-NLS-1$
    		return;
    	}
    	if((i < 0) || (i > 65535)){
    		setErrorMessage(Policy.bind("CVSSSH2PreferencePage.104")); //$NON-NLS-1$
    		return;
    	}
    }
    
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    store.setValue(KEY_SSH2HOME, ssh2HomeText.getText());
    store.setValue(KEY_PRIVATEKEY, privateKeyText.getText());
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
    proxyHostText.setText(""); //$NON-NLS-1$
    proxyPortText.setText(HTTP_DEFAULT_PORT);
    proxyTypeCombo.select(0);
    enableAuth.setSelection(false);
    proxyUserText.setText(""); //$NON-NLS-1$
    proxyPassText.setText(""); //$NON-NLS-1$
    updateControls();
  }

  protected void createSpacer(Composite composite, int columnSpan) {
    Label label=new Label(composite, SWT.NONE);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    label.setLayoutData(gd);
  }
}
