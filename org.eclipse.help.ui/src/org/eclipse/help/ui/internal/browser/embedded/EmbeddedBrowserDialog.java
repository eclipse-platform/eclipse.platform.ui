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
	private Image[] shellImgs;
	Shell shell;
	Browser webBrowser;
	/**
	 * Constructor
	 */
	public EmbeddedBrowserDialog(
		Shell parent,
		String windowTitle,
		Image[] shellImages) {
		this.windowTitle = windowTitle;
		this.shellImgs = shellImages;
		createShell(parent);
	}
	/**
	 * Creates hosting shell.
	 */
	private void createShell(Shell parent) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		if (shellImgs != null)
			shell.setImages(shellImgs);
		shell.setText(windowTitle);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		shell.setLayout(layout);

		webBrowser = new Browser(shell, SWT.NONE);
		final GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.widthHint = parent.getSize().x;
		data.heightHint = parent.getSize().y;
		webBrowser.setLayoutData(data);
		shell.pack();
		shell.setLocation(parent.getLocation());
		webBrowser.addVisibilityWindowListener(new VisibilityWindowListener() {
			public void hide(WindowEvent event) {
				shell.setVisible(false);
			}
			public void show(WindowEvent event) {
				if(event.location!=null){
					shell.setLocation(event.location);
				}
				if(event.size!=null){
					data.widthHint = event.size.x;
					data.heightHint = event.size.y;
					shell.pack();
				}
				shell.open();

			}
		});
		webBrowser.addOpenWindowListener(new OpenWindowListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.browser.NewWindowListener#newWindow(org.eclipse.swt.browser.NewWindowEvent)
			 */
			public void open(WindowEvent event) {
				EmbeddedBrowserDialog workingSetDialog =
					new EmbeddedBrowserDialog(
						shell,
						windowTitle,
						shellImgs);
				event.browser = workingSetDialog.getBrowser();
			}

		});
		webBrowser.addCloseWindowListener(new CloseWindowListener() {
			public void close(WindowEvent event) {
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
