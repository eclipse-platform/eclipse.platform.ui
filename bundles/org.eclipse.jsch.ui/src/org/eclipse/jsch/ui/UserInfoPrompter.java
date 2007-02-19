/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jsch.ui;

import com.jcraft.jsch.UserInfo;

public class UserInfoPrompter implements UserInfo{

  /* (non-Javadoc)
   * @see com.jcraft.jsch.UserInfo#getPassphrase()
   */
  public String getPassphrase(){
    // XXX Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.jcraft.jsch.UserInfo#getPassword()
   */
  public String getPassword(){
    // XXX Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
   */
  public boolean promptPassphrase(String arg0){
    // XXX Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
   */
  public boolean promptPassword(String arg0){
    // XXX Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
   */
  public boolean promptYesNo(String arg0){
    // XXX Auto-generated method stub
    return false;
  }

  /* (non-Javadoc)
   * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
   */
  public void showMessage(String arg0){
    // XXX Auto-generated method stub

  }

}
