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

package org.eclipse.ui.internal.browser;

import java.util.List;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.*;

public class ExtendedBrowserSupportImpl extends ExtendedBrowserSupport {
  private DefaultBrowserSupport defaultBrowserSupportInstance;

  public ExtendedBrowserSupportImpl() {
    // make sure browser support is loaded
    PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable();
  }

  public DefaultBrowserSupport getDefaultBrowserSupportInstance() throws PartInitException {
    if (defaultBrowserSupportInstance == null)
      defaultBrowserSupportInstance = DefaultBrowserSupport.getInstance();

    if (defaultBrowserSupportInstance == null)
      throw new PartInitException(Messages.errorNoBrowser);

    return defaultBrowserSupportInstance;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.browser.ExtendedBrowserSupport#getWebBrowsers()
   */
  public List getWebBrowsers() {
    return BrowserManager.getInstance().getWebBrowsers();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.browser.ExtendedBrowserSupport#createExternalBrowser(org.eclipse.ui.browser.IBrowserDescriptor, java.lang.String)
   */
  public IWebBrowser createExternalBrowser(IBrowserDescriptor descriptor, String browserId) throws PartInitException {
    return getDefaultBrowserSupportInstance().createExternalBrowser(descriptor, browserId);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.browser.ExtendedBrowserSupport#createInternalBrowser(int, java.lang.String, java.lang.String, java.lang.String)
   */
  public IWebBrowser createInternalBrowser(int style, String browserId, String name, String tooltip)
  throws PartInitException {
    return getDefaultBrowserSupportInstance().createInternalBrowser(style, browserId, name, tooltip);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.browser.ExtendedBrowserSupport#internalBrowserAvailable()
   */
  public boolean internalBrowserAvailable() {
    return WebBrowserUtil.canUseInternalWebBrowser();
  }
}
