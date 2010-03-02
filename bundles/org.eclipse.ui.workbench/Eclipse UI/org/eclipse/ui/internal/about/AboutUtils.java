/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.about;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Manages links in styled text.
 */

public class AboutUtils {

	private final static String ERROR_LOG_COPY_FILENAME = "log"; //$NON-NLS-1$

	/**
	 * Scan the contents of the about text
	 * 
	 * @param s
	 * @return
	 */
	public static AboutItem scan(String s) {
		ArrayList linkRanges = new ArrayList();
		ArrayList links = new ArrayList();

		// slightly modified version of jface url detection
		// see org.eclipse.jface.text.hyperlink.URLHyperlinkDetector

		int urlSeparatorOffset = s.indexOf("://"); //$NON-NLS-1$
		while (urlSeparatorOffset >= 0) {

			boolean startDoubleQuote = false;

			// URL protocol (left to "://")
			int urlOffset = urlSeparatorOffset;
			char ch;
			do {
				urlOffset--;
				ch = ' ';
				if (urlOffset > -1)
					ch = s.charAt(urlOffset);
				startDoubleQuote = ch == '"';
			} while (Character.isUnicodeIdentifierStart(ch));
			urlOffset++;

			// Right to "://"
			StringTokenizer tokenizer = new StringTokenizer(s
					.substring(urlSeparatorOffset + 3), " \t\n\r\f<>", false); //$NON-NLS-1$
			if (!tokenizer.hasMoreTokens())
				return null;

			int urlLength = tokenizer.nextToken().length() + 3
					+ urlSeparatorOffset - urlOffset;

			if (startDoubleQuote) {
				int endOffset = -1;
				int nextDoubleQuote = s.indexOf('"', urlOffset);
				int nextWhitespace = s.indexOf(' ', urlOffset);
				if (nextDoubleQuote != -1 && nextWhitespace != -1)
					endOffset = Math.min(nextDoubleQuote, nextWhitespace);
				else if (nextDoubleQuote != -1)
					endOffset = nextDoubleQuote;
				else if (nextWhitespace != -1)
					endOffset = nextWhitespace;
				if (endOffset != -1)
					urlLength = endOffset - urlOffset;
			}

			linkRanges.add(new int[] { urlOffset, urlLength });
			links.add(s.substring(urlOffset, urlOffset + urlLength));

			urlSeparatorOffset = s.indexOf("://", urlOffset + urlLength + 1); //$NON-NLS-1$
		}
		return new AboutItem(s, (int[][]) linkRanges.toArray(new int[linkRanges
				.size()][2]), (String[]) links
				.toArray(new String[links.size()]));
	}

	/**
	 * Open a browser with the argument title on the argument url. If the url
	 * refers to a resource within a bundle, then a temp copy of the file will
	 * be extracted and opened.
	 * 
	 * @see <code>Platform.asLocalUrl</code>
	 * @param url
	 *            The target url to be displayed, null will be safely ignored
	 * @return true if the url was successfully displayed and false otherwise
	 */
	public static boolean openBrowser(Shell shell, URL url) {
		if (url != null) {
			try {
				url = Platform.asLocalURL(url);
			} catch (IOException e) {
				return false;
			}
		}
		if (url == null) {
			return false;
		}
		openLink(shell, url.toString());
		return true;
	}

	/**
	 * Open a link
	 */
	public static void openLink(Shell shell, String href) {
		// format the href for an html file (file:///<filename.html>
		// required for Mac only.
		if (href.startsWith("file:")) { //$NON-NLS-1$
			href = href.substring(5);
			while (href.startsWith("/")) { //$NON-NLS-1$
				href = href.substring(1);
			}
			href = "file:///" + href; //$NON-NLS-1$
		}
		IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
				.getBrowserSupport();
		try {
			IWebBrowser browser = support.getExternalBrowser();
			browser.openURL(new URL(urlEncodeForSpaces(href.toCharArray())));
		} catch (MalformedURLException e) {
			openWebBrowserError(shell, href, e);
		} catch (PartInitException e) {
			openWebBrowserError(shell, href, e);
		}
	}

	/**
	 * This method encodes the url, removes the spaces from the url and replaces
	 * the same with <code>"%20"</code>. This method is required to fix Bug
	 * 77840.
	 * 
	 * @since 3.0.2
	 */
	private static String urlEncodeForSpaces(char[] input) {
		StringBuffer retu = new StringBuffer(input.length);
		for (int i = 0; i < input.length; i++) {
			if (input[i] == ' ') {
				retu.append("%20"); //$NON-NLS-1$
			} else {
				retu.append(input[i]);
			}
		}
		return retu.toString();
	}

	/**
	 * display an error message
	 */
	private static void openWebBrowserError(Shell shell, final String href,
			final Throwable t) {
		String title = WorkbenchMessages.ProductInfoDialog_errorTitle;
		String msg = NLS.bind(
				WorkbenchMessages.ProductInfoDialog_unableToOpenWebBrowser,
				href);
		IStatus status = WorkbenchPlugin.getStatus(t);
		StatusUtil.handleStatus(status, title + ": " + msg, StatusManager.SHOW, //$NON-NLS-1$
				shell);
	}

	public static void openErrorLogBrowser(Shell shell) {
		String filename = Platform.getLogFileLocation().toOSString();

		File log = new File(filename);
		if (log.exists()) {
			// Make a copy of the file with a temporary name.
			// Working around an issue with windows file associations/browser
			// malfunction whereby the browser doesn't open on ".log" and we
			// aren't returned an error.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=97783
			File logCopy = makeDisplayCopy(log);
			if (logCopy != null) {
				AboutUtils.openLink(shell,
						"file:///" + logCopy.getAbsolutePath()); //$NON-NLS-1$
				return;
			}
			// Couldn't make copy, try to open the original log.
			// We try the original in this case rather than putting up an error,
			// because the copy could fail due to an I/O or out of space
			// problem.
			// In that case we may still be able to show the original log,
			// depending on the platform. The risk is that users with
			// configurations that have bug #97783 will still get nothing
			// (vs. an error) but we'd rather
			// try again than put up an error dialog on platforms where the
			// ability to view the original log works just fine.
			AboutUtils.openLink(shell, "file:///" + filename); //$NON-NLS-1$
			return;
		}
		MessageDialog.openInformation(shell,
				WorkbenchMessages.AboutSystemDialog_noLogTitle, NLS.bind(
						WorkbenchMessages.AboutSystemDialog_noLogMessage,
						filename));
	}

	/**
	 * Returns a copy of the given file to be used for display in a browser.
	 * 
	 * @return the file, or <code>null</code>
	 */
	private static File makeDisplayCopy(File file) {
		IPath path = WorkbenchPlugin.getDefault().getDataLocation();
		if (path == null) {
			return null;
		}
		path = path.append(ERROR_LOG_COPY_FILENAME);
		File copy = path.toFile();
		FileReader in = null;
		FileWriter out = null;
		try {
			in = new FileReader(file);
			// don't append data, overwrite what was there
			out = new FileWriter(copy);
			char buffer[] = new char[4096];
			int count;
			while ((count = in.read(buffer, 0, buffer.length)) > 0) {
				out.write(buffer, 0, count);
			}
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				return null;
			}
		}
		return copy;

	}

}
