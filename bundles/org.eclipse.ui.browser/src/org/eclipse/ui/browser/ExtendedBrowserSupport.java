/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.browser;

import java.util.List;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.browser.ExtendedBrowserSupportImpl;

/**
 * @since 3.3
 */
public abstract class ExtendedBrowserSupport {

  private static ExtendedBrowserSupport extendedBrowserSupport;
  
  public static ExtendedBrowserSupport getInstance() {
    if (extendedBrowserSupport == null)
      extendedBrowserSupport = new ExtendedBrowserSupportImpl();
    
    return extendedBrowserSupport;
  }
  
  /**
   * Returns the list of available external web browsers.
   * 
   * @return a list of IBrowserDescriptor's
   */
  public abstract List getWebBrowsers();
  
  /**
   * Creates an external browser given a descriptor.
   * 
   * @param descriptor the descriptor of the external browser to be created
   * @param browserId 
   * @return
   * @throws PartInitException
   */
  public abstract IWebBrowser createExternalBrowser(IBrowserDescriptor descriptor, String browserId) throws PartInitException;
  

  /**
   * Creates an internal browser. 
   * 
   * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(int,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public abstract IWebBrowser createInternalBrowser(int style, String browserId, String name,
      String tooltip) throws PartInitException; 
  
  /**
   * Returns whether the internal browser is available.
   * 
   * @return <code>true</code> if the internal browser is available, <code>false</code> otherwise.
   */
  public abstract boolean internalBrowserAvailable();

  /**
   * Returns whether the internal browser is the default browser.
   * 
   * @see #getDefaultWebBrowser()
   * @return <code>true</code> if the internal browser is the current default (or is
   *    not available), and <code>false</code> if there is no default or an
   *    external browser is the default
   */
  public abstract boolean isInternalBrowserDefault();

  /**
   * Sets whether the internal browser is the default browser.
   * 
   * @param useInternalBrowser <code>true</code> to set the internal browser as the
   *    default (has no effect if the internal browser is not available),
   *    and <code>false</code> to set an external browser as the default
   */
  public abstract void setInternalBrowserDefault(boolean useInternalBrowser);

  /**
   * Returns which external web browser is the default browser when using an
   * external browser. The default browser may still be the internal browser,
   * which can be verified using isInternalBrowserDefault().
   * 
   * @see #isInternalBrowserDefault()
   * @return the current default external browser
   */
  public abstract IBrowserDescriptor getDefaultWebBrowser();

  /**
   * Sets the given external web browser as the default.
   * 
   * @param browser the default browser for launching 
   */
  public abstract void setDefaultWebBrowser(IBrowserDescriptor browser);
}