/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.browser;
import org.eclipse.help.ui.browser.*;

/**
 * Creates browser by delegating
 * to appropriate browser adapter
 * @deprecated Use org.eclipse.help.interna
 */
public class BrowserManager {
	private static BrowserManager instance;
	
	/**
	 * Private Constructor
	 */
	private BrowserManager() {
	}
	/**
	 * Obtains singleton instance.
	 */
	public static BrowserManager getInstance() {
		if (instance == null)
			instance = new BrowserManager();
		return instance;
	}

	/**
	 * Creates web browser
	 */
	public IBrowser createBrowser() {
		return new Browser(org.eclipse.help.internal.browser.BrowserManager.getInstance().createBrowser());
	}
	
	/**
	 * Closes all browsers created
	 */
	public void closeAll() {
	}
	
	class Browser implements IBrowser {
		private org.eclipse.help.browser.IBrowser browser;
		public Browser(org.eclipse.help.browser.IBrowser browser) {
			this.browser = browser;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.help.browser.IBrowser#close()
		 */
		public void close() {
			browser.close();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.help.browser.IBrowser#displayURL(java.lang.String)
		 */
		public void displayURL(String url) throws Exception {
			browser.displayURL(url);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.help.browser.IBrowser#isCloseSupported()
		 */
		public boolean isCloseSupported() {
			return browser.isCloseSupported();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.help.browser.IBrowser#isSetLocationSupported()
		 */
		public boolean isSetLocationSupported() {
			return browser.isSetLocationSupported();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.help.browser.IBrowser#isSetSizeSupported()
		 */
		public boolean isSetSizeSupported() {
			return browser.isSetSizeSupported();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.help.browser.IBrowser#setLocation(int, int)
		 */
		public void setLocation(int x, int y) {
			browser.setLocation(x,y);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.help.browser.IBrowser#setSize(int, int)
		 */
		public void setSize(int width, int height) {
			browser.setSize(width,height);
		}

	}
}