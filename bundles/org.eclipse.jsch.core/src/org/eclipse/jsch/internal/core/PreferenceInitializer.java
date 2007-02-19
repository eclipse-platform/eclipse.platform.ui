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
package org.eclipse.jsch.internal.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.InstanceScope;

public class PreferenceInitializer extends AbstractPreferenceInitializer{

  public static String SSH_HOME_DEFAULT=null;
  static{
    String ssh_dir_name=".ssh"; //$NON-NLS-1$

    // Windows doesn't like files or directories starting with a dot.
    if(Platform.getOS().equals(Platform.OS_WIN32)){
      ssh_dir_name="ssh"; //$NON-NLS-1$
    }
    SSH_HOME_DEFAULT=System.getProperty("user.home"); //$NON-NLS-1$
    if(SSH_HOME_DEFAULT!=null){
      SSH_HOME_DEFAULT=SSH_HOME_DEFAULT+java.io.File.separator+ssh_dir_name;
    }
  }
  
  private IEclipsePreferences[] getOldPreferences() {
    return new IEclipsePreferences[]{
        new InstanceScope().getNode("org.eclipse.team.cvs.ssh2"), //$NON-NLS-1$
        new DefaultScope().getNode("org.eclipse.team.cvs.ssh2") //$NON-NLS-1$
    };
  }
  
  public void initializeDefaultPreferences(){
    Preferences preferences=JSchCorePlugin.getPlugin().getPluginPreferences();

    preferences.setDefault(IConstants.KEY_SSH2HOME, SSH_HOME_DEFAULT);
    preferences.setDefault(IConstants.KEY_PRIVATEKEY, IConstants.PRIVATE_KEYS_DEFAULT);
 
    if(!preferences.contains(IConstants.PREF_FIRST_STARTUP)){
      
      IEclipsePreferences[] oldPreferences=getOldPreferences();
      IPreferencesService ps=Platform.getPreferencesService();
      if(ps.get(IConstants.KEY_OLD_SSH2HOME, null, oldPreferences)!=null){
        preferences.setValue(IConstants.KEY_SSH2HOME, ps.get(
            IConstants.KEY_OLD_SSH2HOME, null, oldPreferences));
        preferences.setValue(IConstants.KEY_PRIVATEKEY, ps.get(
            IConstants.KEY_OLD_PRIVATEKEY, null, oldPreferences));
      }
      preferences.setValue(IConstants.PREF_FIRST_STARTUP, true);
      JSchCorePlugin.getPlugin().savePluginPreferences();
    }
  }

}
