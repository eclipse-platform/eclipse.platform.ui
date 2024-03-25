/*******************************************************************************
* Copyright (c) 2022 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;

/**
 * This class provides a convenient shorthand for creating and initializing
 * {@link Browser}. This offers several benefits over creating Browser normal
 * way:
 *
 * <ul>
 * <li>The same factory can be used many times to create several Browser
 * instances</li>
 * <li>The setters on BrowserFactory all return "this", allowing them to be
 * chained</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * Browser browser = BrowserFactory.newBrowser(SWT.NONE)//
 * 		.url("http://www.eclipse.org") //
 * 		.layoutData(gridData) //
 * 		.create(parent);
 * </pre>
 * <p>
 * The above example creates a Browser for the eclipse website. Finally the
 * Browser is created in "parent".
 * </p>
 *
 * <pre>
 * BrowserFactory browserFactory = BrowserFactory.newGroup(SWT.NONE);
 * browserFactory.url("http://www.eclipse.org").create(parent);
 * browserFactory.url("https://www.eclipse.org/projects/").create(parent);
 * browserFactory.url("https://www.eclipse.org/donate/").create(parent);
 * </pre>
 * <p>
 * The above example creates three browser using the same instance of
 * BrowserFactory.
 * </p>
 *
 * @since 3.25
 */
public final class BrowserFactory extends AbstractCompositeFactory<BrowserFactory, Browser> {

	private BrowserFactory(int style) {
		super(BrowserFactory.class, (Composite parent) -> new Browser(parent, style));
	}

	/**
	 * Creates a new BrowserFactory with the given style. Refer to
	 * {@link Browser#Browser(Composite, int)} for possible styles.
	 *
	 * @return a new BrowserFactory instance
	 */
	public static BrowserFactory newBrowser(int style) {
		return new BrowserFactory(style);
	}

	/**
	 * Renders a string containing HTML. The rendering of the content occurs
	 * asynchronously. The rendered page will be given trusted permissions; to
	 * render the page with untrusted permissions use
	 * <code>setText(String html, boolean trusted)</code> instead.
	 * <p>
	 * The html parameter is Unicode-encoded since it is a java <code>String</code>.
	 * As a result, the HTML meta tag charset should not be set. The charset is
	 * implied by the <code>String</code> itself.
	 *
	 * @param html the html
	 * @return this
	 *
	 * @see Browser#setText(String)
	 */
	public BrowserFactory html(String html) {
		addProperty(g -> g.setText(html));
		return this;
	}

	/**
	 * Renders a string containing HTML. The rendering of the content occurs
	 * asynchronously. The rendered page can be given either trusted or untrusted
	 * permissions.
	 * <p>
	 * The <code>html</code> parameter is Unicode-encoded since it is a java
	 * <code>String</code>. As a result, the HTML meta tag charset should not be
	 * set. The charset is implied by the <code>String</code> itself.
	 * <p>
	 * The <code>trusted</code> parameter affects the permissions that will be
	 * granted to the rendered page. Specifying <code>true</code> for trusted gives
	 * the page permissions equivalent to a page on the local file system, while
	 * specifying <code>false</code> for trusted gives the page permissions
	 * equivalent to a page from the internet. Page content should be specified as
	 * trusted if the invoker created it or trusts its source, since this would
	 * allow (for instance) style sheets on the local file system to be referenced.
	 * Page content should be specified as untrusted if its source is not trusted or
	 * is not known.
	 *
	 * @param html    the html
	 * @param trusted <code>false</code> if the rendered page should be granted
	 *                restricted permissions and <code>true</code> otherwise
	 * @return this
	 *
	 * @see Browser#setText(String, boolean)
	 */
	public BrowserFactory html(String html, boolean trusted) {
		addProperty(g -> g.setText(html, trusted));
		return this;
	}

	/**
	 * Begins loading a URL. The loading of its content occurs asynchronously.
	 *
	 * @param url the URL to be loaded
	 * @return this
	 *
	 * @see Browser#setUrl(String)
	 */
	public BrowserFactory url(String url) {
		addProperty(g -> g.setUrl(url));
		return this;
	}

	/**
	 * Begins loading a URL. The loading of its content occurs asynchronously.
	 * <p>
	 * If the URL causes an HTTP request to be initiated then the provided
	 * <code>postData</code> and <code>header</code> arguments, if any, are sent
	 * with the request. A value in the <code>headers</code> argument must be a
	 * name-value pair with a colon separator in order to be sent (for example:
	 * "<code>user-agent: custom</code>").
	 *
	 * @param url      the URL to be loaded
	 * @param postData post data to be sent with the request, or <code>null</code>
	 * @param headers  header lines to be sent with the request, or
	 *                 <code>null</code>
	 * @return this
	 *
	 * @see Browser#setUrl(String, String, String[])
	 */
	public BrowserFactory url(String url, String postData, String[] headers) {
		addProperty(g -> g.setUrl(url, postData, headers));
		return this;
	}

	/**
	 * Sets that javascript will not be allowed to run in pages subsequently viewed
	 * in the receiver. Note that setting this value does not affect the running of
	 * javascript in the current page.
	 *
	 * @return this
	 *
	 * @see Browser#setJavascriptEnabled(boolean)
	 */
	public BrowserFactory disableJS() {
		addProperty(g -> g.setJavascriptEnabled(false));
		return this;
	}
}