/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh2;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.ui.*;

import com.jcraft.jsch.*;

public class CVSSSH2PreferencePage extends PreferencePage
  implements IWorkbenchPreferencePage {

//  private DirectoryFieldEditor ssh2homeEditor;

  private static final String SSH2_PREFERENCE_PAGE_CONTEXT = "org.eclipse.team.cvs.ui.ssh2_preference_page_context"; //$NON-NLS-1$
  
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
  private Button privateKeyAdd;
  private boolean useProxy;
  private boolean useAuth;

  private Button ssh2HomeBrowse;
  private Button keyGenerateDSA;
  private Button keyGenerateRSA;
  private Button keyLoad;
  private Button keyExport;
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
 
  public static final String AUTH_SCHEME = "";//$NON-NLS-1$ 
  public static final URL FAKE_URL;

  static {
    URL temp = null;
    try{
      temp = new URL("http://org.eclipse.team.cvs.ssh2");//$NON-NLS-1$ 
    }catch (MalformedURLException e){}
    FAKE_URL = temp;
  } 

  public CVSSSH2PreferencePage() {
//    super(GRID);
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    setPreferenceStore(store);
    setDescription(CVSSSH2Messages.CVSSSH2PreferencePage_18); //$NON-NLS-1$
  }
  
  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    initializeDialogUnits(container);

    TabFolder tabFolder = new TabFolder(container, SWT.NONE);
    tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(CVSSSH2Messages.CVSSSH2PreferencePage_19); //$NON-NLS-1$
    tabItem.setControl(createGeneralPage(tabFolder));

    tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(CVSSSH2Messages.CVSSSH2PreferencePage_20); //$NON-NLS-1$
    tabItem.setControl(createProxyPage(tabFolder));

    tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(CVSSSH2Messages.CVSSSH2PreferencePage_21); //$NON-NLS-1$
    tabItem.setControl(createKeyManagementPage(tabFolder));
    
    tabItem = new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(CVSSSH2Messages.CVSSSH2PreferencePage_133); //$NON-NLS-1$
    tabItem.setControl(createHostKeyManagementPage(tabFolder));

    initControls();

    Dialog.applyDialogFont(parent);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), SSH2_PREFERENCE_PAGE_CONTEXT);
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

    createSpacer(group, 3);

    ssh2HomeLabel=new Label(group, SWT.NONE);
    ssh2HomeLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_23); //$NON-NLS-1$

    ssh2HomeText=new Text(group, SWT.SINGLE | SWT.BORDER);
    ssh2HomeText.setFont(group.getFont());
    GridData gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=1;
    ssh2HomeText.setLayoutData(gd);

    ssh2HomeBrowse=new Button(group, SWT.NULL);
    ssh2HomeBrowse.setText(CVSSSH2Messages.CVSSSH2PreferencePage_24); //$NON-NLS-1$
    gd=new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gd.horizontalSpan=1;
    ssh2HomeBrowse.setLayoutData(gd);

    createSpacer(group, 3);

    privateKeyLabel=new Label(group, SWT.NONE);
    privateKeyLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_25); //$NON-NLS-1$

    privateKeyText=new Text(group, SWT.SINGLE | SWT.BORDER);
    privateKeyText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=1;
    privateKeyText.setLayoutData(gd);

    privateKeyAdd=new Button(group, SWT.NULL);
    privateKeyAdd.setText(CVSSSH2Messages.CVSSSH2PreferencePage_26); //$NON-NLS-1$
    gd=new GridData(GridData.HORIZONTAL_ALIGN_FILL);
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
	  dd.setMessage(CVSSSH2Messages.CVSSSH2PreferencePage_27); //$NON-NLS-1$
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
	    if(keys.length()!=0)keys=keys+","; //$NON-NLS-1$
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
    enableProxy.setText(CVSSSH2Messages.CVSSSH2PreferencePage_30); //$NON-NLS-1$
    GridData gd=new GridData();
    gd.horizontalSpan=3;
    enableProxy.setLayoutData(gd);

    proxyTypeLabel=new Label(group, SWT.NONE);
    proxyTypeLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_31); //$NON-NLS-1$
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
		  if(foo.equals(ISSHContants.HTTP)){ 
		    proxyPortText.setText(ISSHContants.HTTP_DEFAULT_PORT); 
		  }
		  else if(foo.equals(ISSHContants.SOCKS5)){
		    proxyPortText.setText(ISSHContants.SOCKS5_DEFAULT_PORT);
		  }
		} 
     });
    proxyTypeCombo.add(ISSHContants.HTTP);
    proxyTypeCombo.add(ISSHContants.SOCKS5);
    proxyTypeCombo.select(0);

    proxyHostLabel=new Label(group, SWT.NONE);
    proxyHostLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_32); //$NON-NLS-1$

    proxyHostText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyHostText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    proxyHostText.setLayoutData(gd);

    proxyPortLabel=new Label(group, SWT.NONE);
    proxyPortLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_33); //$NON-NLS-1$

    proxyPortText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPortText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    proxyPortText.setLayoutData(gd);
    
    proxyPortText.addModifyListener(new ModifyListener(){
    	public void modifyText(ModifyEvent e){
    			if(isValidPort(proxyPortText.getText())){
    				setErrorMessage(null);
    			}
    	}
    });

    
    createSpacer(group, 3);

    enableAuth=new Button(group, SWT.CHECK);
    enableAuth.setText(CVSSSH2Messages.CVSSSH2PreferencePage_34); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=3;
    enableAuth.setLayoutData(gd);

    proxyUserLabel=new Label(group, SWT.NONE);
    proxyUserLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_35); //$NON-NLS-1$

    proxyUserText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyUserText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    proxyUserText.setLayoutData(gd);

    proxyPassLabel=new Label(group, SWT.NONE);
    proxyPassLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_36); //$NON-NLS-1$

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
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
    group.setLayoutData(gd);

    keyGenerateDSA=new Button(group, SWT.NULL);
    keyGenerateDSA.setText(CVSSSH2Messages.CVSSSH2PreferencePage_131); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=1;
    keyGenerateDSA.setLayoutData(gd);

    keyGenerateRSA=new Button(group, SWT.NULL);
    keyGenerateRSA.setText(CVSSSH2Messages.CVSSSH2PreferencePage_132); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=1;
    keyGenerateRSA.setLayoutData(gd);

    keyLoad=new Button(group, SWT.NULL);
    keyLoad.setText(CVSSSH2Messages.CVSSSH2PreferencePage_128);  //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=1;
    keyLoad.setLayoutData(gd);

    publicKeylabel=new Label(group, SWT.NONE);
    publicKeylabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_39); //$NON-NLS-1$
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
    keyFingerPrintLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_41); //$NON-NLS-1$
    keyFingerPrintText=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyFingerPrintText.setFont(group.getFont());
    keyFingerPrintText.setEditable(false);
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyFingerPrintText.setLayoutData(gd);

    keyCommentLabel=new Label(group, SWT.NONE);
    keyCommentLabel.setText(CVSSSH2Messages.CVSSSH2PreferencePage_42); //$NON-NLS-1$
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
    keyPassphrase1Label.setText(CVSSSH2Messages.CVSSSH2PreferencePage_43); //$NON-NLS-1$
    keyPassphrase1Text=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyPassphrase1Text.setFont(group.getFont());
    keyPassphrase1Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyPassphrase1Text.setLayoutData(gd);

    keyPassphrase2Label=new Label(group, SWT.NONE);
    keyPassphrase2Label.setText(CVSSSH2Messages.CVSSSH2PreferencePage_44); //$NON-NLS-1$
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
    		if(kpair!=null && pass1.equals(pass2)){
    			saveKeyPair.setEnabled(true);
    		}
    		else{
    			saveKeyPair.setEnabled(false);
    		}
    		if(pass2.length()==0){
    			setErrorMessage(null);
    			return;
    		}
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_48); //$NON-NLS-1$
    		}
    	}
    });	

    keyPassphrase2Text.addModifyListener(new ModifyListener(){
    	public void modifyText(ModifyEvent e){
    		String pass1=keyPassphrase1Text.getText();
    		String pass2=keyPassphrase2Text.getText();
    		if(kpair!=null && pass1.equals(pass2)){
    			saveKeyPair.setEnabled(true);
    		}
    		else{
    			saveKeyPair.setEnabled(false);
    		}
    		if(pass2.length()<pass1.length()){
    			if(pass1.startsWith(pass2)){
    				setErrorMessage(null);
    			}
    			else{
    				setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_48); //$NON-NLS-1$
    			}
    			return;
    		}
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_48); //$NON-NLS-1$
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
    				setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_48); //$NON-NLS-1$
    			}
    			return;
    		}
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_48); //$NON-NLS-1$
    		}   	
    	}
    	public void focusLost(FocusEvent e){
    		String pass1=keyPassphrase1Text.getText();
    		String pass2=keyPassphrase2Text.getText();
    		if(pass1.equals(pass2)){
    			setErrorMessage(null);
    		}
    		else{
    			setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_48); //$NON-NLS-1$
    		}
    	}
    });

    Composite buttons=new Composite(group, SWT.NONE);
    layout=new GridLayout(2, true);
    layout.marginWidth=0;
    buttons.setLayout(layout);
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    buttons.setLayoutData(gd);
    
    keyExport=new Button(buttons, SWT.NULL);
    keyExport.setText(CVSSSH2Messages.CVSSSH2PreferencePage_105); //$NON-NLS-1$
    gd=new GridData(GridData.FILL_BOTH);
    keyExport.setLayoutData(gd);
    
    saveKeyPair=new Button(buttons, SWT.NULL);
    saveKeyPair.setText(CVSSSH2Messages.CVSSSH2PreferencePage_45); //$NON-NLS-1$
    gd=new GridData(GridData.FILL_BOTH);
    saveKeyPair.setLayoutData(gd);
    
    SelectionAdapter keygenadapter=new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  JSch jsch=JSchSession.getJSch();
	  boolean ok=true;
	  String _type=""; //$NON-NLS-1$

	  try{
	    int type=0;
	    if(e.widget==keyGenerateDSA){
	      type=KeyPair.DSA;
	      _type=ISSHContants.DSA;
	    }
	    else if(e.widget==keyGenerateRSA){
	      type=KeyPair.RSA;
	      _type=ISSHContants.RSA;
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
	    keyPassphrase1Text.setText(""); //$NON-NLS-1$
	    keyPassphrase2Text.setText(""); //$NON-NLS-1$
	    updateControls();
	  }
	  catch(IOException ee){
	    ok=false;
	  }
	  catch(JSchException ee){
	    ok=false;
	  }
	  if(!ok){
 	    MessageDialog.openError(getShell(),
				    CVSSSH2Messages.CVSSSH2PreferencePage_error,  //$NON-NLS-1$
				    CVSSSH2Messages.CVSSSH2PreferencePage_47);  //$NON-NLS-1$
	  }
	}
      };
    keyGenerateDSA.addSelectionListener(keygenadapter);
    keyGenerateRSA.addSelectionListener(keygenadapter);

    keyLoad.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  boolean ok=true;
	  String home=ssh2HomeText.getText();
	  FileDialog fd=new FileDialog(getShell(), SWT.OPEN);
	  fd.setFilterPath(home);
	  Object o=fd.open();
	  if(o==null){ // cancel
	    return;
	  }
	  String pkey=fd.getFileName();
	  String pkeyab=(new File(fd.getFilterPath(), pkey)).getAbsolutePath();
	  try{
	    JSch jsch=JSchSession.getJSch();
	    KeyPair _kpair=KeyPair.load(jsch, pkeyab);
	    PassphrasePrompt prompt=null;
	    while(_kpair.isEncrypted()){
	      if(prompt==null){
		prompt=new PassphrasePrompt(NLS.bind(CVSSSH2Messages.CVSSSH2PreferencePage_126, new String[] { pkey }));   //$NON-NLS-1$
	      }
	      Display.getDefault().syncExec(prompt);
	      String passphrase=prompt.getPassphrase();
	      if(passphrase==null) break;
	      if(_kpair.decrypt(passphrase)){
	      	break;
	      }
	      MessageDialog.openError(getShell(),
				      CVSSSH2Messages.CVSSSH2PreferencePage_error,  //$NON-NLS-1$
				      NLS.bind(CVSSSH2Messages.CVSSSH2PreferencePage_129, new String[] { pkey }));  //$NON-NLS-1$
	    }
	    if(_kpair.isEncrypted()){
	      return;
	    }
	    kpair=_kpair;
	    String _type=(kpair.getKeyType()==KeyPair.DSA)?ISSHContants.DSA:ISSHContants.RSA;
	    ByteArrayOutputStream out=new ByteArrayOutputStream();
	    kpairComment=_type+"-1024"; //$NON-NLS-1$
	    kpair.writePublicKey(out, kpairComment);
	    out.close();
	    publicKeyText.setText(out.toString());
	    keyFingerPrintText.setText(kpair.getFingerPrint());
	    keyCommentText.setText(kpairComment);
	    keyPassphrase1Text.setText(""); //$NON-NLS-1$
	    keyPassphrase2Text.setText(""); //$NON-NLS-1$
	    updateControls();
	  }
	  catch(IOException ee){
	    ok=false;
	  }
	  catch(JSchException ee){
	    ok=false;
	  }
	  if(!ok){
	    MessageDialog.openError(getShell(),
				    CVSSSH2Messages.CVSSSH2PreferencePage_error,  //$NON-NLS-1$
				    CVSSSH2Messages.CVSSSH2PreferencePage_130);  //$NON-NLS-1$
	  }
	}
      });

    keyExport.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  if(kpair==null)return;

          setErrorMessage(null);

	  final String[] target=new String[1];
	  final String title=CVSSSH2Messages.CVSSSH2PreferencePage_106;  //$NON-NLS-1$
	  final String message=CVSSSH2Messages.CVSSSH2PreferencePage_107;  //$NON-NLS-1$
	  Display.getDefault().syncExec(new Runnable(){
	      public void run(){
		Display display=Display.getCurrent();
		Shell shell=new Shell(display);
		ExportDialog dialog=new ExportDialog(shell, title, message);
		dialog.open();
		shell.dispose();
		target[0]=dialog.getTarget();
	      }});
	  if(target[0]==null){
	    return;
	  }
	  String user=""; //$NON-NLS-1$
	  String host=""; //$NON-NLS-1$
	  int port=22;
  
	  if(target[0].indexOf('@')>0){
	    user=target[0].substring(0, target[0].indexOf('@'));
	    host=target[0].substring(target[0].indexOf('@')+1);
	  }
          if(host.indexOf(':')>0){
	    try{port=Integer.parseInt(host.substring(host.indexOf(':')+1));}
	    catch(NumberFormatException ee) {
	      port=-1;
	    }
	    host=host.substring(0, host.indexOf(':'));
	  }

	  if(user.length()==0 || 
	     host.length()==0 ||
	     port==-1){
	    setErrorMessage(NLS.bind(CVSSSH2Messages.CVSSSH2PreferencePage_108, new String[] { target[0] })); //$NON-NLS-1$
	    return;
	  }

	  String options=""; //$NON-NLS-1$
	  try{
	    ByteArrayOutputStream bos=new ByteArrayOutputStream();
	    if(options.length()!=0){
	      try{bos.write((options+" ").getBytes());} //$NON-NLS-1$
	      catch(IOException eeee){}
	    }
	    kpair.writePublicKey(bos, kpairComment);
	    bos.close();
	    export_via_sftp(user, host, port, 
			    ".ssh/authorized_keys", //$NON-NLS-1$
			    bos.toByteArray());
	  }
	  catch(IOException ee){
	  }
	  catch(JSchException ee){
	  	setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_111); //$NON-NLS-1$
	  }
	}});

    saveKeyPair.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  if(kpair==null)return;

	  String pass=keyPassphrase1Text.getText();
	  /*
	  if(!pass.equals(keyPassphrase2Text.getText())){
	    setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48")); //$NON-NLS-1$
	    return;
	  }
	  */
	  if(pass.length()==0){
	    if(!MessageDialog.openConfirm(getShell(),
					  CVSSSH2Messages.CVSSSH2PreferencePage_confirmation, //$NON-NLS-1$
					  CVSSSH2Messages.CVSSSH2PreferencePage_49
					  )){
	      return ;
	    }
	  }

	  kpair.setPassphrase(pass);

	  IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
	  String home=ssh2HomeText.getText();

	  File _home=new File(home);

	  if(!_home.exists()){
	    if(!MessageDialog.openConfirm(getShell(),
					  CVSSSH2Messages.CVSSSH2PreferencePage_confirmation, //$NON-NLS-1$
					  NLS.bind(CVSSSH2Messages.CVSSSH2PreferencePage_50, new String[] { home }) //$NON-NLS-1$
					  )){
	      return ;
	    }
	    if(!_home.mkdirs()){
	      setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_100+home); //$NON-NLS-1$
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
	    if(!MessageDialog.openConfirm(getShell(),
					  CVSSSH2Messages.CVSSSH2PreferencePage_confirmation, //$NON-NLS-1$ 
					  NLS.bind(CVSSSH2Messages.CVSSSH2PreferencePage_53, new String[] { file }) //$NON-NLS-1$
					  )){
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
 	    MessageDialog.openInformation(getShell(),
					  CVSSSH2Messages.CVSSSH2PreferencePage_information, //$NON-NLS-1$
					  CVSSSH2Messages.CVSSSH2PreferencePage_55+ //$NON-NLS-1$
					  "\n"+ //$NON-NLS-1$
					  CVSSSH2Messages.CVSSSH2PreferencePage_57+file+ //$NON-NLS-1$
					  "\n"+ //$NON-NLS-1$
					  CVSSSH2Messages.CVSSSH2PreferencePage_59+ //$NON-NLS-1$
					  file+
					  ".pub"); //$NON-NLS-1$
	  }
	}
      });

    return group;
  }

	private TableViewer viewer;
	private Button addHostKeyButton;
	private Button removeHostKeyButton;
	class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			HostKey entry = (HostKey)element;
			switch (columnIndex) {
				case 0:
					return entry.getHost();
				case 1:
					return entry.getType();
				case 2:
					return entry.getFingerPrint(JSchSession.getJSch());
				default:
					return null;
			}
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	};
	
  private Control createHostKeyManagementPage(Composite parent) {
    int columnSpan=3;
    Composite group=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	layout.numColumns = 2;
    group.setLayout(layout);
    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
	gd.verticalAlignment = GridData.FILL;
    group.setLayoutData(gd);

    Label label=new Label(group, SWT.NONE);
    label.setText(CVSSSH2Messages.CVSSSH2PreferencePage_139); //$NON-NLS-1$
    gd=new GridData();
    gd.horizontalSpan=2;
    label.setLayoutData(gd);
    
	viewer = new TableViewer(group, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	Table table = viewer.getTable();
	new TableEditor(table);
	table.setHeaderVisible(true);
	table.setLinesVisible(true);
	gd = new GridData(GridData.FILL_BOTH);
	gd.widthHint = convertWidthInCharsToPixels(30);
	/*
	 * The hardcoded hint does not look elegant, but in reality
	 * it does not make anything bound to this 100-pixel value,
	 * because in any case the tree on the left is taller and
	 * that's what really determines the height.
	 */
	gd.heightHint = 100;
	table.setLayoutData(gd);
	table.addListener(SWT.Selection, new Listener() {
		public void handleEvent(Event e) {
			handleSelection();
		}
	});
	// Create the table columns
	new TableColumn(table, SWT.NULL);
	new TableColumn(table, SWT.NULL);
	new TableColumn(table, SWT.NULL);
	TableColumn[] columns = table.getColumns();
	columns[0].setText(CVSSSH2Messages.CVSSSH2PreferencePage_134);  //$NON-NLS-1$
	columns[1].setText(CVSSSH2Messages.CVSSSH2PreferencePage_135);  //$NON-NLS-1$
	columns[2].setText(CVSSSH2Messages.CVSSSH2PreferencePage_136);  //$NON-NLS-1$
	viewer.setColumnProperties(new String[] {
				CVSSSH2Messages.CVSSSH2PreferencePage_134,  //$NON-NLS-1$ 
				CVSSSH2Messages.CVSSSH2PreferencePage_135,  //$NON-NLS-1$ 
				CVSSSH2Messages.CVSSSH2PreferencePage_136}); //$NON-NLS-1$
	viewer.setLabelProvider(new TableLabelProvider());
	viewer.setContentProvider(new IStructuredContentProvider() {
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public Object[] getElements(Object inputElement) {
			if (inputElement == null) return null;
			return (Object[])inputElement;
		}
	});
	TableLayout tl = new TableLayout();
	tl.addColumnData(new ColumnWeightData(20));
	tl.addColumnData(new ColumnWeightData(10));
	tl.addColumnData(new ColumnWeightData(70));
	table.setLayout(tl);
	
	Composite buttons = new Composite(group, SWT.NULL);
	buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	buttons.setLayout(layout);

	addHostKeyButton = new Button(buttons, SWT.PUSH);
	addHostKeyButton.setText(CVSSSH2Messages.CVSSSH2PreferencePage_137);  //$NON-NLS-1$
	gd = new GridData();
	gd.horizontalAlignment = GridData.FILL;
	addHostKeyButton.setLayoutData(gd);
	addHostKeyButton.setEnabled(false);
	addHostKeyButton.addListener(SWT.Selection, new Listener() {
		public void handleEvent(Event e) {
			//addHostKey();
		}
	});
	removeHostKeyButton = new Button(buttons, SWT.PUSH);
	removeHostKeyButton.setText(CVSSSH2Messages.CVSSSH2PreferencePage_138);  //$NON-NLS-1$
	gd = new GridData();
	gd.horizontalAlignment = GridData.FILL;
	removeHostKeyButton.setLayoutData(gd);
	removeHostKeyButton.setEnabled(false);
	removeHostKeyButton.addListener(SWT.Selection, new Listener() {
		public void handleEvent(Event e) {
			removeHostKey();
		}
	});
	    
	Dialog.applyDialogFont(parent);
	
	JSchSession.loadKnownHosts();
	HostKeyRepository hkr=JSchSession.getJSch().getHostKeyRepository();
	viewer.setInput(hkr.getHostKey());
	handleSelection();

    return group;
  }
  
	private void handleSelection() {
		boolean empty = viewer.getSelection().isEmpty();
		removeHostKeyButton.setEnabled(!empty);
	}
	private void removeHostKey(){
		JSch jsch=JSchSession.getJSch();
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		HostKeyRepository hkr=JSchSession.getJSch().getHostKeyRepository();
		for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
			HostKey hostkey = (HostKey) iterator.next();
			hkr.remove(hostkey.getHost(), hostkey.getType());
			viewer.remove(hostkey);
         }		
	}
  private void export_via_sftp(String user, String host, int port, String target, byte[] pkey) throws JSchException{
    try{

      /*
      int i=0;
      String authorized_keys=target;
      String dir="";
      String separator="/";
      i=target.lastIndexOf("/");
      if(i<0){
	i=target.lastIndexOf("\\");
	if(i>=0){ separator="\\"; }
      }
      else{
      }
      if(i>=0){
	authorized_keys=target.substring(i+1);
	dir=target.substring(0, i+1);
      }
      */

      IProgressMonitor pm=new org.eclipse.core.runtime.NullProgressMonitor();
      Session session=JSchSession.getSession(null, user, "", host, port, pm).getSession(); //$NON-NLS-1$
      if(session.getServerVersion().indexOf("OpenSSH")==-1){ //$NON-NLS-1$
      	setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_110); //$NON-NLS-1$
    	return;
      }
      Channel channel=session.openChannel("sftp"); //$NON-NLS-1$
      channel.connect();
      ChannelSftp c=(ChannelSftp)channel;

      String pwd=c.pwd();
      SftpATTRS attr=null;

      try{ attr=c.stat(".ssh"); } //$NON-NLS-1$
      catch(SftpException ee){ }
      if(attr==null){
        try{ c.mkdir(".ssh"); } //$NON-NLS-1$
	catch(SftpException ee){
	  setErrorMessage(ee.message);
	  return;
	}
      }
      try{ c.cd(".ssh"); } //$NON-NLS-1$
      catch(SftpException ee){
	setErrorMessage(ee.message);
	return;
      }

      try{
	ByteArrayInputStream bis=new ByteArrayInputStream(pkey);
	c.put(bis, "authorized_keys", null, ChannelSftp.APPEND); //$NON-NLS-1$
	bis.close();
	checkPermission(c, "authorized_keys"); //$NON-NLS-1$
	checkPermission(c, ".");                // .ssh //$NON-NLS-1$
	c.cd("..");                              //$NON-NLS-1$
	checkPermission(c, ".");                //  home directory //$NON-NLS-1$
      }
      catch(SftpException ee){
	//setErrorMessage(debug+ee.message);
      }

      MessageDialog.openInformation(getShell(),
				    CVSSSH2Messages.CVSSSH2PreferencePage_information,  //$NON-NLS-1$
				    CVSSSH2Messages.CVSSSH2PreferencePage_109+ //$NON-NLS-1$
				    (user+"@"+host+(port==22 ? "" : ":"+port)+":~/.ssh/authorized_keys")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

      c.disconnect();
      //session.disconnect();
    }
    catch(IOException eee){
      setErrorMessage(eee.toString());
    }
  }

  private void checkPermission(ChannelSftp c, String path) throws SftpException{
    SftpATTRS attr=c.stat(path);
    int permissions=attr.getPermissions();
    if((permissions&00022)!=0){
      permissions&=~00022;
      c.chmod(permissions,path);
    } 	
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
    keyExport.setEnabled(enable);
    saveKeyPair.setEnabled(enable);
  }

  public void init(IWorkbench workbench) {
//    super.init(workbench);
//    initControls();
  }

  public void initialize() {
    initControls();
  }

  private void initControls(){
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    ssh2HomeText.setText(store.getString(ISSHContants.KEY_SSH2HOME));
    privateKeyText.setText(store.getString(ISSHContants.KEY_PRIVATEKEY));
    useProxy=store.getString(ISSHContants.KEY_PROXY).equals("true"); //$NON-NLS-1$
    enableProxy.setSelection(useProxy);
    proxyHostText.setText(store.getString(ISSHContants.KEY_PROXY_HOST));
    proxyTypeCombo.select(store.getString(ISSHContants.KEY_PROXY_TYPE).equals(ISSHContants.HTTP)?0:1);
    proxyPortText.setText(store.getString(ISSHContants.KEY_PROXY_PORT));
    useAuth=store.getString(ISSHContants.KEY_PROXY_AUTH).equals("true"); //$NON-NLS-1$
    enableAuth.setSelection(useAuth);
    
    Map map = Platform.getAuthorizationInfo(FAKE_URL, "proxy", AUTH_SCHEME); //$NON-NLS-1$
    if(map!=null){
      String username=(String) map.get(ISSHContants.KEY_PROXY_USER);
      if(username!=null) proxyUserText.setText(username);
      String password=(String) map.get(ISSHContants.KEY_PROXY_PASS);
      if(password!=null) proxyPassText.setText(password);
    }

    proxyPassText.setEchoChar('*');
    updateControls();
  }
  public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			setErrorMessage(null);
			String home = ssh2HomeText.getText();
			File _home = new File(home);
			if (!_home.exists()) {
				if (MessageDialog.openQuestion(getShell(), CVSSSH2Messages.CVSSSH2PreferencePage_question, //$NON-NLS-1$
						NLS.bind(CVSSSH2Messages.CVSSSH2PreferencePage_99, new String[] { home }) //$NON-NLS-1$
						)) {
					if (!(_home.mkdirs())) {
						setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_100 + home); //$NON-NLS-1$
						return false;
					}
				}
			}
			if (enableProxy.getSelection() && !isValidPort(proxyPortText.getText())) {
				return false;
			}
			IPreferenceStore store = CVSSSH2Plugin.getDefault().getPreferenceStore();
			store.setValue(ISSHContants.KEY_SSH2HOME, home);
			store.setValue(ISSHContants.KEY_PRIVATEKEY, privateKeyText.getText());
			store.setValue(ISSHContants.KEY_PROXY, enableProxy.getSelection());
			store.setValue(ISSHContants.KEY_PROXY_TYPE, proxyTypeCombo.getText());
			store.setValue(ISSHContants.KEY_PROXY_HOST, proxyHostText.getText());
			store.setValue(ISSHContants.KEY_PROXY_PORT, proxyPortText.getText());
			store.setValue(ISSHContants.KEY_PROXY_AUTH, enableAuth.getSelection());
			
			// Store proxy username and password in the keyring file for now. This is
			// not ultra secure, but at least it will be saved between sessions.
			Map map = Platform.getAuthorizationInfo(FAKE_URL, "proxy", AUTH_SCHEME); //$NON-NLS-1$
			if (map == null)
				map = new java.util.HashMap(10);
			map.put(ISSHContants.KEY_PROXY_USER, proxyUserText.getText());
			map.put(ISSHContants.KEY_PROXY_PASS, proxyPassText.getText());
			try {
				Platform.addAuthorizationInfo(FAKE_URL, "proxy", AUTH_SCHEME, map); //$NON-NLS-1$
			} catch (CoreException e) {
				Util.logError("Cannot save ssh2 proxy authentication information to keyring file", e); //$NON-NLS-1$
			}
		}
		CVSSSH2Plugin.getDefault().savePluginPreferences();
		return result;
	}

  private boolean isValidPort(String port){
  	int i=-1;
  	try {	
  		i=Integer.parseInt(port);
  	}
  	catch (NumberFormatException ee) {
//  		setErrorMessage(Policy.bind("CVSSSH2PreferencePage.103")); //$NON-NLS-1$
//  		return false;
  	}
  	if((i < 0) || (i > 65535)){
  		setErrorMessage(CVSSSH2Messages.CVSSSH2PreferencePage_104); //$NON-NLS-1$
  		return false;
  	}
  	return true;
  }
  
  public void performApply() {
    performOk();
  }

  protected void performDefaults(){
    super.performDefaults();
    enableProxy.setSelection(false);
    proxyHostText.setText(""); //$NON-NLS-1$
    proxyPortText.setText(ISSHContants.HTTP_DEFAULT_PORT);
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

class ExportDialog extends Dialog {
  protected Text field;
  protected String target=null;
  protected String title=null;
  protected String message=null;

  public ExportDialog(Shell parentShell, String title, String message) {
    super(parentShell);
    this.title=title;
    this.message=message;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(title);
  }

  public void create() {
    super.create();
    field.setFocus();
  }

  protected Control createDialogArea(Composite parent) {
  	
  	parent = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;	
	parent.setLayout(layout);
	parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
    Composite main=new Composite(parent, SWT.NONE);
    layout=new GridLayout();
    layout.numColumns=3;
    main.setLayout(layout);
    main.setLayoutData(new GridData(GridData.FILL_BOTH));
	
    if (message!=null) {
      Label messageLabel=new Label(main, SWT.WRAP);
      messageLabel.setText(message);
      GridData data=new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan=3;
      messageLabel.setLayoutData(data);
    }

    createTargetFields(main);
    Dialog.applyDialogFont(main);
    return main;
  }

  protected void createTargetFields(Composite parent) {
    new Label(parent, SWT.NONE).setText(CVSSSH2Messages.CVSSSH2PreferencePage_125); //$NON-NLS-1$
		
    field=new Text(parent, SWT.BORDER);
    GridData data=new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint=convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
    data.horizontalSpan=2;
    field.setLayoutData(data);
  }

  public String getTarget() {
    return target;
  }

  protected void okPressed() {
    String _target=field.getText();
    if(_target==null || _target.length()==0){
      return;
    }
    target=_target;
    super.okPressed();
  }

  protected void cancelPressed() {
    target=null;
    super.cancelPressed();
  }
}

class PassphrasePrompt implements Runnable{
  private String message;
  private String passphrase;
  PassphrasePrompt(String message){
    this.message=message;
  }
  public void run(){
    Display display=Display.getCurrent();
    Shell shell=new Shell(display);
    PassphraseDialog dialog=new PassphraseDialog(shell, message);
    dialog.open();
    shell.dispose();
    passphrase=dialog.getPassphrase();
  }
  public String getPassphrase(){
    return passphrase;
  }
}

class PassphraseDialog extends Dialog {
  protected Text passphraseField;
  protected String passphrase = null;
  protected String message = null;

  public PassphraseDialog(Shell parentShell, String message) {
    super(parentShell);
    this.message = message;
  }

  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(message);
  }

  public void create() {
    super.create();
    passphraseField.setFocus();
  }

  protected Control createDialogArea(Composite parent) {
    Composite main=new Composite(parent, SWT.NONE);

    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    main.setLayout(layout);
    main.setLayoutData(new GridData(GridData.FILL_BOTH));

    if (message!=null) {
      Label messageLabel=new Label(main, SWT.WRAP);
      messageLabel.setText(message);
      GridData data=new GridData(GridData.FILL_HORIZONTAL);
      data.horizontalSpan=3;
      messageLabel.setLayoutData(data);
    }

    createPassphraseFields(main);
    return main;
  }

  protected void createPassphraseFields(Composite parent) {
    new Label(parent, SWT.NONE).setText(CVSSSH2Messages.CVSSSH2PreferencePage_127); //$NON-NLS-1$
    passphraseField=new Text(parent, SWT.BORDER);
    GridData data=new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint=convertHorizontalDLUsToPixels(IDialogConstants.ENTRY_FIELD_WIDTH);
    passphraseField.setLayoutData(data);
    passphraseField.setEchoChar('*');

    new Label(parent, SWT.NONE);
  }

  public String getPassphrase() {
    return passphrase;
  }

  protected void okPressed() {
    String _passphrase = passphraseField.getText();
    if(_passphrase==null || _passphrase.length()==0){
      return;
    }
    passphrase=_passphrase;
    super.okPressed();
  }
  protected void cancelPressed() {
    passphrase=null;
    super.cancelPressed();
  }
}
