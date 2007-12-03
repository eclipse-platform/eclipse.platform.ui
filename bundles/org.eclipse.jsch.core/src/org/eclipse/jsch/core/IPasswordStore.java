/*******************************************************************************
 * Copyright (c) 2007 JCraft,Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     JCraft,Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jsch.core;

/**
 * This interface abstracts the password store.  The given password
 * will be stored to via this interface.
 * 
 * This interface is intended to be implemented by clients.
 * @since 1.1
 */
public interface IPasswordStore{
  /**
   * The cached password should be flushed.
   * @param location
   */
  public void clear(IJSchLocation location);
  
  /**
   * This method will check if the password is cached or not.
   * @param location
   * @return whether the password is cached.
   */
  public boolean isCached(IJSchLocation location);
  
  /*
   * The new password "location.getPassword()" will be
   * cached.
   */
  public void update(IJSchLocation location);
}
