/*******************************************************************************
 * Copyright (c) 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     wim.jongman@remainsoftware.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.tips.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tips.core.TipManager;

public class TipDialog extends Shell {

	/**
	 * When passed as style, the default style will be used.
	 */
	public static final int DEFAULT_STYLE = -1;
	private TipManager fTipManager;
	private TipComposite fTipComposite;

	/**
	 * Creates the Tip Dialog.
	 *
	 * @param display
	 *            the {@link Display}
	 * @param tipManager
	 *            the {@link TipManager}
	 * @param style
	 *            the {@link Shell} style or {@link #DEFAULT_STYLE} for
	 *            <code>SWT.DIALOG_TRIM | SWT.RESIZE | SWT.CLOSE</code>
	 */
	public TipDialog(Shell display, TipManager tipManager, int style) {
		super(display, style == DEFAULT_STYLE ? SWT.DIALOG_TRIM | SWT.RESIZE | SWT.CLOSE : style);
		fTipManager = tipManager;
		setLayout(new FillLayout(SWT.HORIZONTAL));
		fTipComposite = new TipComposite(this, SWT.NONE);
		pack();
		setLocation(getShell().getMonitor().getClientArea().width / 2 - getSize().x / 2,
				getShell().getMonitor().getClientArea().height / 2 - getSize().y / 2);
		setText("Tip of the Day");
	}

	@Override
	public void open() {
		super.open();
		fTipComposite.setTipManager(fTipManager);
	}

	@Override
	protected void checkSubclass() {
	}
}