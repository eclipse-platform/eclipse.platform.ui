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
package org.eclipse.jsch.internal.core;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PreferenceModifyListener extends
    org.eclipse.core.runtime.preferences.PreferenceModifyListener{

  public PreferenceModifyListener(){
    // Nothing to do
  }
  
  public IEclipsePreferences preApply(IEclipsePreferences node){
    Utils.migrateSSH2Preferences(node.node("instance")); //$NON-NLS-1$
    return super.preApply(node);
  }

}
