/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
 *     IBM Corporation - ongoing maintenance
 *******************************************************************************/
package org.eclipse.jsch.internal.core;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceInitializer extends AbstractPreferenceInitializer{

  public static String SSH_HOME_DEFAULT=null;
  static{
    String ssh_dir_name=".ssh"; //$NON-NLS-1$

    SSH_HOME_DEFAULT=System.getProperty("user.home"); //$NON-NLS-1$
    if(SSH_HOME_DEFAULT!=null){
      SSH_HOME_DEFAULT=SSH_HOME_DEFAULT+java.io.File.separator+ssh_dir_name;
    }
  }
  
  public void initializeDefaultPreferences(){
    IEclipsePreferences defaultNode=new DefaultScope().getNode(JSchCorePlugin.ID);
    defaultNode.put(IConstants.KEY_SSH2HOME, SSH_HOME_DEFAULT);
    defaultNode.put(IConstants.KEY_PRIVATEKEY, IConstants.PRIVATE_KEYS_DEFAULT);
    Utils.migrateSSH2Preferences();
  }

}
