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
package org.eclipse.team.cvs.ssh2;

import java.util.Enumeration;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.team.internal.ccvs.core.util.Util;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import com.jcraft.jsch.*;

class JSchSession{
  private static final int SSH_DEFAULT_PORT=22;
  private static JSch jsch;
  private static java.util.Hashtable pool=new java.util.Hashtable();

  static String default_ssh_home=null;
  static {
    default_ssh_home=System.getProperty("user.home");
    if(default_ssh_home!=null){
      default_ssh_home=default_ssh_home+java.io.File.separator+".ssh";
    }
  }

  private static String current_ssh_home=null;

  static Session getSession(String username, String password, 
			    String hostname, int port,
			    final IProgressMonitor monitor) throws JSchException{
    if(port==0) port=SSH_DEFAULT_PORT;
    if(jsch==null){
      jsch=new JSch();
    }

    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    String ssh_home=store.getString(CVSSSH2PreferencePage.KEY_SSH2HOME);

    if(current_ssh_home==null ||
       !current_ssh_home.equals(ssh_home)){
      current_ssh_home=ssh_home;
      if(ssh_home.length()==0)
	ssh_home=default_ssh_home;

      try{
	java.io.File file;
	file=new java.io.File(ssh_home, "id_dsa");
	if(file.exists()) jsch.addIdentity(file.getPath());
	file=new java.io.File(ssh_home, "id_rsa");
	if(file.exists()) jsch.addIdentity(file.getPath());
	file=new java.io.File(ssh_home, "known_hosts");
	jsch.setKnownHosts(file.getPath());
      }
      catch(Exception e){
      }
    }

    String key=username+"@"+hostname+":"+port;

    try{
      Session session=(Session)pool.get(key);
      if(session!=null && !session.isConnected()){
	pool.remove(key);
	session=null;
      }

      if(session==null){
	session=jsch.getSession(username, hostname, port);

	boolean useProxy=store.getString(CVSSSH2PreferencePage.KEY_PROXY).equals("true");
	if(useProxy){
	  String _type=store.getString(CVSSSH2PreferencePage.KEY_PROXY_TYPE);
	  String _host=store.getString(CVSSSH2PreferencePage.KEY_PROXY_HOST);
	  String _port=store.getString(CVSSSH2PreferencePage.KEY_PROXY_PORT);

	  boolean useAuth=store.getString(CVSSSH2PreferencePage.KEY_PROXY_AUTH).equals("true");
	  String _user=store.getString(CVSSSH2PreferencePage.KEY_PROXY_USER);
	  String _pass=store.getString(CVSSSH2PreferencePage.KEY_PROXY_PASS);

	  Proxy proxy=null;
          String proxyhost=_host+":"+_port;
          if(_type.equals(CVSSSH2PreferencePage.HTTP)){
	    proxy=new ProxyHTTP(proxyhost);
	    if(useAuth){
	      ((ProxyHTTP)proxy).setUserPasswd(_user, _pass);
	    }
	  }
	  else if(_type.equals(CVSSSH2PreferencePage.SOCKS5)){
	    proxy=new ProxySOCKS5(proxyhost);
	    if(useAuth){
	      ((ProxySOCKS5)proxy).setUserPasswd(_user, _pass);
	    }
	  }
	  else{
	    proxy=null;
	  }
	  if(proxy!=null){
	    session.setProxy(proxy);
	  }
	}

	session.setPassword(password);

	UserInfo ui=new MyUserInfo(username);
	session.setUserInfo(ui);

	session.setSocketFactory(new SocketFactory(){
	    InputStream in=null;
	    OutputStream out=null;
	    public Socket createSocket(String host, int port) throws IOException,
								     UnknownHostException{
	      Socket socket=null;
	      socket=Util.createSocket(host, port, monitor);
	      return socket;
	    }
	    public InputStream getInputStream(Socket socket) throws IOException{
	      if(in==null) in=socket.getInputStream();
	      return in;
	    }
	    public OutputStream getOutputStream(Socket socket) throws IOException{
	      if(out==null) out=socket.getOutputStream();
	      return out;
	    }
	  });

	session.connect();
	pool.put(key, session);
      }
      return session;
    }
    catch(JSchException e){
      //e.printStackTrace();
      pool.remove(key);
      throw e;
    }
  }

  private static class MyUserInfo implements UserInfo, UIKeyboardInteractive{
    private String username;
    private String password;
    private String passphrase;
    MyUserInfo(String username){
      this.username=username;
    }
    public String getPassword(){ return password; }
    public String getPassphrase(){ return passphrase; }
    public boolean promptYesNo(String str){
      YesNoPrompt prompt=new YesNoPrompt(str);
      Display.getDefault().syncExec(prompt);
      return prompt.getResult()==SWT.YES;
    }
    public boolean promptPassphrase(String message){
      PassphrasePrompt prompt=new PassphrasePrompt(message);
      Display.getDefault().syncExec(prompt);
      String _passphrase=prompt.getPassphrase();
      if(_passphrase!=null)passphrase=_passphrase;
      return _passphrase!=null;
    }
    public boolean promptPassword(String message){
      PasswordPrompt prompt=new PasswordPrompt(message);
      Display.getDefault().syncExec(prompt);
      String _password=prompt.getPassword();
      if(_password!=null)password=_password;
      return _password!=null;
    }
    public void showMessage(final String foo){
      final Display display=Display.getCurrent();
      display.syncExec(new Runnable(){
	  public void run(){
	    Shell shell=new Shell(display);
	    MessageBox box=new MessageBox(shell,SWT.OK);
	    box.setMessage(foo);
	    box.open();
	    shell.dispose();
	  }
	});
    }
    public String[] promptKeyboardInteractive(String destination,
					      String name,
					      String instruction,
					      String lang,
					      String[] prompt,
					      boolean[] echo){
      KeyboardInteractivePrompt _prompt=new KeyboardInteractivePrompt(destination,
								      name,
								      instruction,
								      lang,
								      prompt,
								      echo);
      Display.getDefault().syncExec(_prompt);
      String[] result=_prompt.getResult();
      return result;
    }

    private class YesNoPrompt implements Runnable{
      private String prompt;
      private int result;
      YesNoPrompt(String prompt){
	this.prompt=prompt;
      }
      public void run(){
	Display display=Display.getCurrent();
	Shell shell=new Shell(display);
	MessageBox box=new MessageBox(shell,SWT.YES|SWT.NO);
	box.setMessage(prompt);
	result=box.open();
	shell.dispose();
      }
      public int getResult(){ return result; }
    }
    private class PasswordPrompt implements Runnable{
      private String message;
      private String password;
      PasswordPrompt(String message){
	this.message=message;
      }
      public void run(){
	Display display=Display.getCurrent();
	Shell shell=new Shell(display);
	PasswordDialog dialog=new PasswordDialog(shell, message);
	dialog.open();
	shell.dispose();
	password=dialog.getPassword();
      }
      public String getPassword(){
	return password;
      }
    }
    private class PassphrasePrompt implements Runnable{
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
    private class KeyboardInteractivePrompt implements Runnable{
      private String destination;
      private String name;
      private String instruction;
      private String lang;
      private String[] prompt;
      private boolean[] echo;
      private String[] result;
      KeyboardInteractivePrompt(String destination,
				String name,
				String instruction,
				String lang,
				String[] prompt,
				boolean[] echo){
	this.destination=destination;
	this.name=name;
	this.instruction=instruction;
	this.lang=lang;
	this.prompt=prompt;
	this.echo=echo;
      }
      public void run(){
	Display display=Display.getCurrent();
	Shell shell=new Shell(display);
	KeyboardInteractiveDialog dialog=new KeyboardInteractiveDialog(shell,
								       destination,
								       name,
								       instruction,
								       lang,
								       prompt,
								       echo);
	dialog.open();
	shell.dispose();
	result=dialog.getResult();
      }
      public String[] getResult(){
	return result;
      }
    }
  }

  static void shutdown(){
    if(jsch!=null && pool.size()>0){
      for(Enumeration e=pool.elements(); e.hasMoreElements();){
        Session session=(Session)(e.nextElement());
	try{ session.disconnect(); }
	catch(Exception ee){}
      }
      pool.clear();
    }
  }
}
