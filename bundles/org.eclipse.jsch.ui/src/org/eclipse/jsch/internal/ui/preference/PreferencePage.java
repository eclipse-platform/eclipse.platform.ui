/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.jsch.internal.ui.preference;

import java.io.*;
import java.util.Iterator;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.jsch.core.IJSchService;
import org.eclipse.jsch.internal.core.*;
import org.eclipse.jsch.internal.ui.JSchUIPlugin;
import org.eclipse.jsch.internal.ui.Messages;
import org.eclipse.jsch.ui.UserInfoPrompter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

import com.jcraft.jsch.*;

public class PreferencePage extends org.eclipse.jface.preference.PreferencePage
    implements IWorkbenchPreferencePage{

  private static final String SSH2_PREFERENCE_PAGE_CONTEXT="org.eclipse.jsch.ui.ssh2_preference_page_context"; //$NON-NLS-1$

  private Label ssh2HomeLabel;
  private Label privateKeyLabel;
  Text ssh2HomeText;
  Text privateKeyText;
  private Button privateKeyAdd;

  private Button ssh2HomeBrowse;
  Button keyGenerateDSA;
  Button keyGenerateRSA;
  private Button keyLoad;
  private Button keyExport;
  Button saveKeyPair;
  private Label keyCommentLabel;
  Text keyCommentText;
  private Label keyFingerPrintLabel;
  Text keyFingerPrintText;
  private Label keyPassphrase1Label;
  Text keyPassphrase1Text;
  private Label keyPassphrase2Label;
  Text keyPassphrase2Text;
  private Label publicKeylabel;
  Text publicKeyText;
  KeyPair kpair=null;
  String kpairComment;

  public static final String AUTH_SCHEME="";//$NON-NLS-1$ 

  public PreferencePage(){
    setDescription(Messages.CVSSSH2PreferencePage_18);
  }

  protected Control createContents(Composite parent){
    Composite container=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
    container.setLayout(layout);
    initializeDialogUnits(container);

    TabFolder tabFolder=new TabFolder(container, SWT.NONE);
    tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabItem tabItem=new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(Messages.CVSSSH2PreferencePage_19);
    tabItem.setControl(createGeneralPage(tabFolder));

    tabItem=new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(Messages.CVSSSH2PreferencePage_21);
    tabItem.setControl(createKeyManagementPage(tabFolder));

    tabItem=new TabItem(tabFolder, SWT.NONE);
    tabItem.setText(Messages.CVSSSH2PreferencePage_133);
    tabItem.setControl(createHostKeyManagementPage(tabFolder));

    initControls();

    Dialog.applyDialogFont(parent);
    PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
        SSH2_PREFERENCE_PAGE_CONTEXT);
    return container;
  }

  private Control createGeneralPage(Composite parent){
    Composite group=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    layout.marginHeight=convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth=convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing=convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing=convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    group.setLayout(layout);
    GridData data=new GridData();
    data.horizontalAlignment=GridData.FILL;
    group.setLayoutData(data);

    ssh2HomeLabel=new Label(group, SWT.NONE);
    ssh2HomeLabel.setText(Messages.CVSSSH2PreferencePage_23);

    ssh2HomeText=new Text(group, SWT.SINGLE|SWT.BORDER);
    ssh2HomeText.setFont(group.getFont());
    GridData gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=1;
    ssh2HomeText.setLayoutData(gd);

    ssh2HomeBrowse=new Button(group, SWT.NULL);
    ssh2HomeBrowse.setText(Messages.CVSSSH2PreferencePage_24);
    gd=new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gd.horizontalSpan=1;
    ssh2HomeBrowse.setLayoutData(gd);

    createSpacer(group, 3);

    privateKeyLabel=new Label(group, SWT.NONE);
    privateKeyLabel.setText(Messages.CVSSSH2PreferencePage_25);

    privateKeyText=new Text(group, SWT.SINGLE|SWT.BORDER);
    privateKeyText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=1;
    privateKeyText.setLayoutData(gd);

    privateKeyAdd=new Button(group, SWT.NULL);
    privateKeyAdd.setText(Messages.CVSSSH2PreferencePage_26);
    gd=new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    gd.horizontalSpan=1;
    privateKeyAdd.setLayoutData(gd);

    ssh2HomeBrowse.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        String home=ssh2HomeText.getText();

        if(!new File(home).exists()){
          while(true){
            int foo=home.lastIndexOf(java.io.File.separator, home.length());
            if(foo==-1)
              break;
            home=home.substring(0, foo);
            if(new File(home).exists())
              break;
          }
        }

        DirectoryDialog dd=new DirectoryDialog(getShell());
        dd.setFilterPath(home);
        dd.setMessage(Messages.CVSSSH2PreferencePage_27);
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
        if(dir.equals(home)){
          dir="";} //$NON-NLS-1$
        else{
          dir+=java.io.File.separator;
        }

        for(int i=0; i<files.length; i++){
          String foo=files[i];
          if(keys.length()!=0)
            keys=keys+","; //$NON-NLS-1$
          keys=keys+dir+foo;
        }
        privateKeyText.setText(keys);
      }
    });

    return group;
  }

  private Control createKeyManagementPage(Composite parent){
    int columnSpan=3;
    Composite group=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
    layout.numColumns=3;
    layout.marginHeight=convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth=convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing=convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing=convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    group.setLayout(layout);
    GridData gd=new GridData();
    gd.horizontalAlignment=GridData.FILL;
    group.setLayoutData(gd);

    keyGenerateDSA=new Button(group, SWT.NULL);
    keyGenerateDSA.setText(Messages.CVSSSH2PreferencePage_131);
    gd=new GridData();
    gd.horizontalSpan=1;
    keyGenerateDSA.setLayoutData(gd);

    keyGenerateRSA=new Button(group, SWT.NULL);
    keyGenerateRSA.setText(Messages.CVSSSH2PreferencePage_132);
    gd=new GridData();
    gd.horizontalSpan=1;
    keyGenerateRSA.setLayoutData(gd);

    keyLoad=new Button(group, SWT.NULL);
    keyLoad.setText(Messages.CVSSSH2PreferencePage_128);
    gd=new GridData();
    gd.horizontalSpan=1;
    keyLoad.setLayoutData(gd);

    publicKeylabel=new Label(group, SWT.NONE);
    publicKeylabel.setText(Messages.CVSSSH2PreferencePage_39);
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    publicKeylabel.setLayoutData(gd);

    publicKeyText=new Text(group, SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.WRAP|SWT.LEFT_TO_RIGHT);
    publicKeyText.setText(""); //$NON-NLS-1$
    publicKeyText.setEditable(false);
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment=GridData.FILL;
    gd.verticalAlignment=GridData.FILL;
    gd.grabExcessHorizontalSpace=true;
    gd.grabExcessVerticalSpace=true;
    publicKeyText.setLayoutData(gd);

    keyFingerPrintLabel=new Label(group, SWT.NONE);
    keyFingerPrintLabel.setText(Messages.CVSSSH2PreferencePage_41);
    keyFingerPrintText=new Text(group, SWT.SINGLE|SWT.BORDER|SWT.LEFT_TO_RIGHT);
    keyFingerPrintText.setFont(group.getFont());
    keyFingerPrintText.setEditable(false);
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyFingerPrintText.setLayoutData(gd);

    keyCommentLabel=new Label(group, SWT.NONE);
    keyCommentLabel.setText(Messages.CVSSSH2PreferencePage_42);
    keyCommentText=new Text(group, SWT.SINGLE|SWT.BORDER);
    keyCommentText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyCommentText.setLayoutData(gd);

    keyCommentText.addModifyListener(new ModifyListener(){
      public void modifyText(ModifyEvent e){
        if(kpair==null)
          return;
        try{
          ByteArrayOutputStream out=new ByteArrayOutputStream();
          kpair.writePublicKey(out, keyCommentText.getText());
          out.close();
          publicKeyText.setText(out.toString());
        }
        catch(IOException ee){
          // Ignore
        }
      }
    });

    keyPassphrase1Label=new Label(group, SWT.NONE);
    keyPassphrase1Label.setText(Messages.CVSSSH2PreferencePage_43);
    keyPassphrase1Text=new Text(group, SWT.SINGLE|SWT.BORDER);
    keyPassphrase1Text.setFont(group.getFont());
    keyPassphrase1Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyPassphrase1Text.setLayoutData(gd);

    keyPassphrase2Label=new Label(group, SWT.NONE);
    keyPassphrase2Label.setText(Messages.CVSSSH2PreferencePage_44);
    keyPassphrase2Text=new Text(group, SWT.SINGLE|SWT.BORDER);
    keyPassphrase2Text.setFont(group.getFont());
    keyPassphrase2Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan=2;
    keyPassphrase2Text.setLayoutData(gd);

    keyPassphrase1Text.addModifyListener(new ModifyListener(){
      public void modifyText(ModifyEvent e){
        String pass1=keyPassphrase1Text.getText();
        String pass2=keyPassphrase2Text.getText();
        if(kpair!=null&&pass1.equals(pass2)){
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
          setErrorMessage(Messages.CVSSSH2PreferencePage_48);
        }
      }
    });

    keyPassphrase2Text.addModifyListener(new ModifyListener(){
      public void modifyText(ModifyEvent e){
        String pass1=keyPassphrase1Text.getText();
        String pass2=keyPassphrase2Text.getText();
        if(kpair!=null&&pass1.equals(pass2)){
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
            setErrorMessage(Messages.CVSSSH2PreferencePage_48);
          }
          return;
        }
        if(pass1.equals(pass2)){
          setErrorMessage(null);
        }
        else{
          setErrorMessage(Messages.CVSSSH2PreferencePage_48);
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
            setErrorMessage(Messages.CVSSSH2PreferencePage_48);
          }
          return;
        }
        if(pass1.equals(pass2)){
          setErrorMessage(null);
        }
        else{
          setErrorMessage(Messages.CVSSSH2PreferencePage_48);
        }
      }

      public void focusLost(FocusEvent e){
        String pass1=keyPassphrase1Text.getText();
        String pass2=keyPassphrase2Text.getText();
        if(pass1.equals(pass2)){
          setErrorMessage(null);
        }
        else{
          setErrorMessage(Messages.CVSSSH2PreferencePage_48);
        }
      }
    });

    Composite buttons=new Composite(group, SWT.NONE);
    layout=new GridLayout(2, true);
    layout.marginWidth=0;
    layout.marginHeight=0;
    layout.horizontalSpacing=convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    buttons.setLayout(layout);
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    buttons.setLayoutData(gd);

    keyExport=new Button(buttons, SWT.NULL);
    keyExport.setText(Messages.CVSSSH2PreferencePage_105);
    gd=new GridData(GridData.FILL_BOTH);
    keyExport.setLayoutData(gd);

    saveKeyPair=new Button(buttons, SWT.NULL);
    saveKeyPair.setText(Messages.CVSSSH2PreferencePage_45);
    gd=new GridData(GridData.FILL_BOTH);
    saveKeyPair.setLayoutData(gd);

    SelectionAdapter keygenadapter=new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        boolean ok=true;
        String _type=""; //$NON-NLS-1$

        try{
          int type=0;
          if(e.widget==keyGenerateDSA){
            type=KeyPair.DSA;
            _type=IConstants.DSA;
          }
          else if(e.widget==keyGenerateRSA){
            type=KeyPair.RSA;
            _type=IConstants.RSA;
          }
          else{
            return;
          }

          final KeyPair[] _kpair=new KeyPair[1];
          final int __type=type;
          final JSchException[] _e=new JSchException[1];
          BusyIndicator.showWhile(getShell().getDisplay(), new Runnable(){
            public void run(){
              try{
                _kpair[0]=KeyPair.genKeyPair(getJSch(), __type);
              }
              catch(JSchException e){
                _e[0]=e;
              }
            }
          });
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
              Messages.CVSSSH2PreferencePage_error,
              Messages.CVSSSH2PreferencePage_47);
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
          KeyPair _kpair=KeyPair.load(getJSch(), pkeyab);
          PassphrasePrompt prompt=null;
          while(_kpair.isEncrypted()){
            if(prompt==null){
              prompt=new PassphrasePrompt(NLS.bind(
                  Messages.CVSSSH2PreferencePage_126, new String[] {pkey}));
            }
            Display.getDefault().syncExec(prompt);
            String passphrase=prompt.getPassphrase();
            if(passphrase==null)
              break;
            if(_kpair.decrypt(passphrase)){
              break;
            }
            MessageDialog.openError(getShell(),
                Messages.CVSSSH2PreferencePage_error, NLS.bind(
                    Messages.CVSSSH2PreferencePage_129, new String[] {pkey}));
          }
          if(_kpair.isEncrypted()){
            return;
          }
          kpair=_kpair;
          String _type=(kpair.getKeyType()==KeyPair.DSA) ? IConstants.DSA
              : IConstants.RSA;
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
              Messages.CVSSSH2PreferencePage_error,
              Messages.CVSSSH2PreferencePage_130);
        }
      }
    });

    keyExport.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        if(kpair==null)
          return;

        setErrorMessage(null);

        final String[] target=new String[1];
        final String title=Messages.CVSSSH2PreferencePage_106;
        final String message=Messages.CVSSSH2PreferencePage_107;
        Display.getDefault().syncExec(new Runnable(){
          public void run(){
            Display display=Display.getCurrent();
            Shell shell=new Shell(display);
            ExportDialog dialog=new ExportDialog(shell, title, message);
            dialog.open();
            shell.dispose();
            target[0]=dialog.getTarget();
          }
        });
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
          try{
            port=Integer.parseInt(host.substring(host.indexOf(':')+1));
          }
          catch(NumberFormatException ee){
            port=-1;
          }
          host=host.substring(0, host.indexOf(':'));
        }

        if(user.length()==0||host.length()==0||port==-1){
          setErrorMessage(NLS.bind(Messages.CVSSSH2PreferencePage_108,
              new String[] {target[0]}));
          return;
        }

        String options=""; //$NON-NLS-1$
        try{
          ByteArrayOutputStream bos=new ByteArrayOutputStream();
          if(options.length()!=0){
            try{
              bos.write((options+" ").getBytes());} //$NON-NLS-1$
            catch(IOException eeee){
              // Ignore
            }
          }
          kpair.writePublicKey(bos, kpairComment);
          bos.close();
          export_via_sftp(user, host, port, /* ".ssh/authorized_keys", //$NON-NLS-1$ */
              bos.toByteArray());
        }
        catch(IOException ee){
       // Ignore
        }
        catch(JSchException ee){
          setErrorMessage(Messages.CVSSSH2PreferencePage_111);
        }
      }
    });

    saveKeyPair.addSelectionListener(new SelectionAdapter(){
      public void widgetSelected(SelectionEvent e){
        if(kpair==null)
          return;

        String pass=keyPassphrase1Text.getText();
        /*
         * if(!pass.equals(keyPassphrase2Text.getText())){
         * setErrorMessage(Policy.bind("CVSSSH2PreferencePage.48"));
         * //$NON-NLS-1$ return; }
         */
        if(pass.length()==0){
          if(!MessageDialog.openConfirm(getShell(),
              Messages.CVSSSH2PreferencePage_confirmation,
              Messages.CVSSSH2PreferencePage_49)){
            return;
          }
        }

        kpair.setPassphrase(pass);

        String home=ssh2HomeText.getText();

        File _home=new File(home);

        if(!_home.exists()){
          if(!MessageDialog.openConfirm(getShell(),
              Messages.CVSSSH2PreferencePage_confirmation, NLS.bind(
                  Messages.CVSSSH2PreferencePage_50, new String[] {home}))){
            return;
          }
          if(!_home.mkdirs()){
            setErrorMessage(Messages.CVSSSH2PreferencePage_100+home);
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
              Messages.CVSSSH2PreferencePage_confirmation, // 
              NLS.bind(Messages.CVSSSH2PreferencePage_53, new String[] {file}))){
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
              Messages.CVSSSH2PreferencePage_information,
              Messages.CVSSSH2PreferencePage_55+"\n"+ //$NON-NLS-1$
                  Messages.CVSSSH2PreferencePage_57+file+"\n"+ //$NON-NLS-1$
                  Messages.CVSSSH2PreferencePage_59+file+".pub"); //$NON-NLS-1$
        }
        else{
          return;
        }

        // The generated key should be added to privateKeyText.

        String dir=fd.getFilterPath();
        File mypkey=new java.io.File(dir, fd.getFileName());
        String pkeys=privateKeyText.getText();

        // Check if the generated key has been included in pkeys?
        String[] pkeysa=pkeys.split(","); //$NON-NLS-1$
        for(int i=0; i<pkeysa.length; i++){
          File pkey=new java.io.File(pkeysa[i]);
          if(!pkey.isAbsolute()){
            pkey=new java.io.File(home, pkeysa[i]);
          }
          if(pkey.equals(mypkey))
            return;
        }

        if(dir.equals(home)){
          dir="";} //$NON-NLS-1$
        else{
          dir+=java.io.File.separator;
        }
        if(pkeys.length()>0)
          pkeys+=","; //$NON-NLS-1$
        pkeys=pkeys+dir+fd.getFileName();
        privateKeyText.setText(pkeys);
      }
    });

    return group;
  }

  private TableViewer viewer;
  private Button removeHostKeyButton;

  class TableLabelProvider extends LabelProvider implements ITableLabelProvider{
    public String getColumnText(Object element, int columnIndex){
      HostKey entry=(HostKey)element;
      switch(columnIndex){
        case 0:
          return entry.getHost();
        case 1:
          return entry.getType();
        case 2:
          return entry.getFingerPrint(getJSch());
        default:
          return null;
      }
    }

    public Image getColumnImage(Object element, int columnIndex){
      return null;
    }
  }

  private Control createHostKeyManagementPage(Composite parent){
    Composite group=new Composite(parent, SWT.NULL);
    GridLayout layout=new GridLayout();
    layout.marginHeight=convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth=convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing=convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing=convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    layout.numColumns=2;
    group.setLayout(layout);
    GridData gd=new GridData();
    gd.horizontalAlignment=GridData.FILL;
    gd.verticalAlignment=GridData.FILL;
    group.setLayoutData(gd);

    Label label=new Label(group, SWT.NONE);
    label.setText(Messages.CVSSSH2PreferencePage_139);
    gd=new GridData();
    gd.horizontalSpan=2;
    label.setLayoutData(gd);

    viewer=new TableViewer(group, SWT.MULTI|SWT.FULL_SELECTION|SWT.H_SCROLL
        |SWT.V_SCROLL|SWT.BORDER);
    Table table=viewer.getTable();
    new TableEditor(table);
    table.setHeaderVisible(true);
    table.setLinesVisible(true);
    gd=new GridData(GridData.FILL_BOTH);
    gd.widthHint=convertWidthInCharsToPixels(30);
    /*
     * The hardcoded hint does not look elegant, but in reality it does not make
     * anything bound to this 100-pixel value, because in any case the tree on
     * the left is taller and that's what really determines the height.
     */
    gd.heightHint=100;
    table.setLayoutData(gd);
    table.addListener(SWT.Selection, new Listener(){
      public void handleEvent(Event e){
        handleSelection();
      }
    });
    // Create the table columns
    new TableColumn(table, SWT.NULL);
    new TableColumn(table, SWT.NULL);
    new TableColumn(table, SWT.NULL);
    TableColumn[] columns=table.getColumns();
    columns[0].setText(Messages.CVSSSH2PreferencePage_134);
    columns[1].setText(Messages.CVSSSH2PreferencePage_135);
    columns[2].setText(Messages.CVSSSH2PreferencePage_136);
    viewer.setColumnProperties(new String[] {
        Messages.CVSSSH2PreferencePage_134, // 
        Messages.CVSSSH2PreferencePage_135, // 
        Messages.CVSSSH2PreferencePage_136});
    viewer.setLabelProvider(new TableLabelProvider());
    viewer.setContentProvider(new IStructuredContentProvider(){
      public void dispose(){
        // nothing to do
      }

      public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
        // nothing to do
      }

      public Object[] getElements(Object inputElement){
        if(inputElement==null)
          return null;
        return (Object[])inputElement;
      }
    });
    TableLayout tl=new TableLayout();
    tl.addColumnData(new ColumnWeightData(30));
    tl.addColumnData(new ColumnWeightData(20));
    tl.addColumnData(new ColumnWeightData(70));
    table.setLayout(tl);

    Composite buttons=new Composite(group, SWT.NULL);
    buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    layout=new GridLayout();
    layout.marginHeight=0;
    layout.marginWidth=0;
    buttons.setLayout(layout);

    removeHostKeyButton=new Button(buttons, SWT.PUSH);
    removeHostKeyButton.setText(Messages.CVSSSH2PreferencePage_138);
    int buttonWidth=SWTUtils
        .calculateControlSize(SWTUtils.createDialogPixelConverter(parent),
            new Button[] {removeHostKeyButton});
    removeHostKeyButton.setLayoutData(SWTUtils.createGridData(buttonWidth,
        SWT.DEFAULT, SWT.END, SWT.CENTER, false, false));
    removeHostKeyButton.setEnabled(false);
    removeHostKeyButton.addListener(SWT.Selection, new Listener(){
      public void handleEvent(Event e){
        removeHostKey();
      }
    });

    Dialog.applyDialogFont(parent);

    // JSchSession.loadKnownHosts(JSchContext.getDefaultContext().getJSch());
    JSchCorePlugin.getPlugin().loadKnownHosts();
    HostKeyRepository hkr=getJSch().getHostKeyRepository();
    viewer.setInput(hkr.getHostKey());
    handleSelection();

    return group;
  }

  void handleSelection(){
    boolean empty=viewer.getSelection().isEmpty();
    removeHostKeyButton.setEnabled(!empty);
  }

  void removeHostKey(){
    IStructuredSelection selection=(IStructuredSelection)viewer.getSelection();
    HostKeyRepository hkr=getJSch().getHostKeyRepository();
    for(Iterator iterator=selection.iterator(); iterator.hasNext();){
      HostKey hostkey=(HostKey)iterator.next();
      hkr.remove(hostkey.getHost(), hostkey.getType());
      viewer.remove(hostkey);
    }
  }

  void export_via_sftp(String user, String host, int port,
      /* String target, */ byte[] pkey) throws JSchException{
    try{

      /*
       * int i=0; String authorized_keys=target; String dir=""; String
       * separator="/"; i=target.lastIndexOf("/"); if(i<0){
       * i=target.lastIndexOf("\\"); if(i>=0){ separator="\\"; } } else{ }
       * if(i>=0){ authorized_keys=target.substring(i+1);
       * dir=target.substring(0, i+1); }
       */

      int timeout = 60000;
      IJSchService service = JSchUIPlugin.getPlugin().getJSchService();
      if (service == null) {
        MessageDialog.openInformation(getShell(), Messages.PreferencePage_0, Messages.PreferencePage_1);
        return;
      }
      Session session = service.createSession(host, port, user);
      new UserInfoPrompter(session);
      session.setTimeout(timeout);
      try {
        service.connect(session, timeout, new NullProgressMonitor());
	      if(session.getServerVersion().indexOf("OpenSSH")==-1){ //$NON-NLS-1$
	        setErrorMessage(Messages.CVSSSH2PreferencePage_110);
	        return;
	      }
	      Channel channel=session.openChannel("sftp"); //$NON-NLS-1$
	      channel.connect();
	      ChannelSftp c=(ChannelSftp)channel;

	      /* String pwd= */c.pwd(); // Read off the channel
	      SftpATTRS attr=null;
	
	      try{
	        attr=c.stat(".ssh");} //$NON-NLS-1$
	      catch(SftpException ee){
          // Ignore
	      }
	      if(attr==null){
	        try{
	          c.mkdir(".ssh");} //$NON-NLS-1$
	        catch(SftpException ee){
	          setErrorMessage(ee.getMessage());
	          return;
	        }
	      }
	      try{
	        c.cd(".ssh");} //$NON-NLS-1$
	      catch(SftpException ee){
	        setErrorMessage(ee.getMessage());
	        return;
	      }

	      try{
	        ByteArrayInputStream bis=new ByteArrayInputStream(pkey);
	        c.put(bis, "authorized_keys", null, ChannelSftp.APPEND); //$NON-NLS-1$
	        bis.close();
	        checkPermission(c, "authorized_keys"); //$NON-NLS-1$
	        checkPermission(c, "."); // .ssh //$NON-NLS-1$
	        c.cd(".."); //$NON-NLS-1$
	        checkPermission(c, "."); // home directory //$NON-NLS-1$
	      }
	      catch(SftpException ee){
	        // setErrorMessage(debug+ee.message);
	      }

	      MessageDialog.openInformation(getShell(),
	          Messages.CVSSSH2PreferencePage_information,
	          NLS.bind(Messages.CVSSSH2PreferencePage_109, (user
	              +"@"+host+(port==22 ? "" : ":"+port)+":~/.ssh/authorized_keys"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	
	      c.disconnect();
      } finally {
        session.disconnect();
      }
    } catch(IOException eee){
      setErrorMessage(eee.toString());
    } 
  }

  private void checkPermission(ChannelSftp c, String path) throws SftpException{
    SftpATTRS attr=c.stat(path);
    int permissions=attr.getPermissions();
    if((permissions&00022)!=0){
      permissions&=~00022;
      c.chmod(permissions, path);
    }
  }

  void updateControls(){
    boolean enable=(kpair!=null);
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

  public void init(IWorkbench workbench){
    // super.init(workbench);
    // initControls();
  }

  public void initialize(){
    initControls();
  }

  private void initControls(){
    // Preferences preferences=JSchCorePlugin.getPlugin().getPreferences();
    Preferences preferences=JSchCorePlugin.getPlugin().getPluginPreferences();
    ssh2HomeText.setText(preferences
        .getString(org.eclipse.jsch.internal.core.IConstants.KEY_SSH2HOME));
    privateKeyText.setText(preferences
        .getString(org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY));
    updateControls();
  }

  public boolean performOk(){
    boolean result=super.performOk();
    if(result){
      setErrorMessage(null);
      String home=ssh2HomeText.getText();
      File _home=new File(home);
      if(!_home.exists()){
        if(MessageDialog.openQuestion(getShell(),
            Messages.CVSSSH2PreferencePage_question, NLS.bind(
                Messages.CVSSSH2PreferencePage_99, new String[] {home}))){
          if(!(_home.mkdirs())){
            setErrorMessage(Messages.CVSSSH2PreferencePage_100+home);
            return false;
          }
        }
      }

      // Preferences preferences=JSchCorePlugin.getPlugin().getPreferences();
      Preferences preferences=JSchCorePlugin.getPlugin().getPluginPreferences();
      preferences.setValue(
          org.eclipse.jsch.internal.core.IConstants.KEY_SSH2HOME, home);
      preferences.setValue(
          org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY,
          privateKeyText.getText());
    }
    JSchCorePlugin.getPlugin().setNeedToLoadKnownHosts(true);
    JSchCorePlugin.getPlugin().setNeedToLoadKeys(true);
    JSchCorePlugin.getPlugin().savePluginPreferences();
    return result;
  }

  public void performApply(){
    performOk();
  }

  protected void performDefaults(){
    super.performDefaults();
    // Preferences preferences=JSchCorePlugin.getPlugin().getPreferences();
    Preferences preferences=JSchCorePlugin.getPlugin().getPluginPreferences();
    ssh2HomeText
        .setText(preferences
            .getDefaultString(org.eclipse.jsch.internal.core.IConstants.KEY_SSH2HOME));
    privateKeyText
        .setText(preferences
            .getDefaultString(org.eclipse.jsch.internal.core.IConstants.KEY_PRIVATEKEY));
    updateControls();
  }

  protected void createSpacer(Composite composite, int columnSpan){
    Label label=new Label(composite, SWT.NONE);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    label.setLayoutData(gd);
  }

  JSch getJSch(){
    return JSchCorePlugin.getPlugin().getJSch();
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
}
