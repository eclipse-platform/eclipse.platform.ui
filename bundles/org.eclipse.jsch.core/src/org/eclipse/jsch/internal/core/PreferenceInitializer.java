/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class PreferenceInitializer extends AbstractPreferenceInitializer{

  public static String SSH_HOME_DEFAULT=null;
  static{
    SSH_HOME_DEFAULT=System.getProperty(IConstants.SYSTEM_PROPERTY_USER_HOME);
    if(SSH_HOME_DEFAULT!=null){
      SSH_HOME_DEFAULT=SSH_HOME_DEFAULT+File.separator
          +IConstants.SSH_DEFAULT_HOME;
    }
  }

  public void initializeDefaultPreferences(){
    IEclipsePreferences defaultNode=DefaultScope.INSTANCE
        .getNode(JSchCorePlugin.ID);
    defaultNode.put(IConstants.KEY_SSH2HOME, SSH_HOME_DEFAULT);
    defaultNode.put(IConstants.KEY_PRIVATEKEY, IConstants.PRIVATE_KEYS_DEFAULT);
    Utils.migrateSSH2Preferences();
    changeDefaultWin32SshHome();
  }

  private void changeDefaultWin32SshHome(){
    if(!Platform.getOS().equals(Platform.OS_WIN32))
      return;

    IEclipsePreferences preferences=InstanceScope.INSTANCE
        .getNode(JSchCorePlugin.ID);
    boolean verified=preferences.getBoolean(
        IConstants.PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME, false);
    if(!verified){
      if(null==preferences.get(IConstants.KEY_SSH2HOME, null)){
        String userHome=System
            .getProperty(IConstants.SYSTEM_PROPERTY_USER_HOME);
        if(userHome!=null){
          String oldSshHome=userHome+File.separator
              +IConstants.SSH_OLD_DEFAULT_WIN32_HOME;
          File file=new File(oldSshHome);
          if(file.exists())
            preferences.put(IConstants.KEY_SSH2HOME, oldSshHome);
        }
        preferences.putBoolean(
            IConstants.PREF_HAS_CHANGED_DEFAULT_WIN32_SSH_HOME, true);
        try{
          preferences.flush();
        }
        catch(BackingStoreException e){
          JSchCorePlugin.log(new Status(IStatus.INFO, JSchCorePlugin.ID,
              "Could not flush preferences.", e)); //$NON-NLS-1$
        }
      }
    }
  }
}
