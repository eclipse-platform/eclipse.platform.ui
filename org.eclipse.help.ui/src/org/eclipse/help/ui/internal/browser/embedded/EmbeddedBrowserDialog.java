/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.browser.embedded;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * Application providing embeded Internet Explorer The controlling commands are
 * read from standard input Commands and their parameters are separated using
 * spaced and should be provided one command per line.
 */
public class EmbeddedBrowserDialog {
	private String windowTitle;
	private Image shellImg;
	Shell shell;
	Browser webBrowser;
	int x, y, w, h;
	/**
	 * Constructor used for launching in process embeded IE (for debugging)
	 */
	public EmbeddedBrowserDialog(
		Shell parent,
		String windowTitle,
		Image shellImage,
		int xPosition,
		int yPosition,
		int width,
		int height) {
		this.windowTitle = windowTitle;
		this.shellImg = shellImage;
		this.x = xPosition;
		this.y = yPosition;
		this.w = width;
		this.h = height;
		createShell(parent);
	}
	/**
	 * Creates hosting shell.
	 */
	private void createShell(Shell parent) {
		shell = new Shell(parent, SWT.DIALOG_TRIM);
		if (shellImg != null)
			shell.setImage(shellImg);
		shell.setText(windowTitle);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		shell.setLayout(layout);

		webBrowser = new Browser(shell, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.widthHint = w;
		data.heightHint = h;
		webBrowser.setLayoutData(data);
		shell.pack();
		shell.setLocation(x, y);
		webBrowser.addVisibilityListener(new VisibilityListener() {
			public void hide(VisibilityEvent event) {
				shell.setVisible(false);
			}
			public void show(VisibilityEvent event) {
				shell.open();
			}
		});
		webBrowser.addOpenWindowListener(new OpenWindowListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.browser.NewWindowListener#newWindow(org.eclipse.swt.browser.NewWindowEvent)
			 */
			public void open(OpenWindowEvent event) {
				int dw = 300;
				int dh = 500;
				int dx = x + (w - dw) / 2;
				int dy = y + (h - dh) / 2;
				dx += 10;
				if (dy > 30)
					dy -= 30;
				EmbeddedBrowserDialog workingSetDialog =
					new EmbeddedBrowserDialog(
						shell,
						windowTitle,
						shellImg,
						dx,
						dy,
						dw,
						dh);
				event.browser = workingSetDialog.getBrowser();
			}

		});
		webBrowser.addCloseWindowListener(new CloseWindowListener() {
			public void close(CloseWindowEvent event) {
				Browser browser = (Browser) event.widget;
				browser.getShell().close();
			}
		});
	}
	public boolean isDisposed() {
		return shell.isDisposed();
	}
	public Browser getBrowser() {
		return webBrowser;
	}
}
