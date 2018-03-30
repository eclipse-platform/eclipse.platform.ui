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

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tips.core.ITipManager;
import org.eclipse.tips.core.TipProvider;
import org.eclipse.tips.core.internal.LogUtil;
import org.eclipse.tips.core.internal.TipManager;

/**
 * Class to manage the tip providers and start the tip of the day UI.
 *
 */
@SuppressWarnings("restriction")
public abstract class DefaultTipManager extends TipManager {

	private TipDialog fTipDialog;

	/**
	 * Opens the Tip of the Day dialog. Subclasses may override if they want to
	 * present the Tips in a different way, e.g. in a view.
	 *
	 * @param startUp When called from a startup situation, true must be passed for
	 *                <code>pStartup</code>. If in a manual starting situation,
	 *                false must be passed. This enables the manager to decide to
	 *                skip opening the dialog at startup (e.g., no new tip items).
	 *
	 * @see #isOpen()
	 */
	@Override
	public ITipManager open(boolean startUp) {
		try {
			Assert.isTrue(!isOpen(), "Tip of the Day already open.");
		} catch (Exception e) {
			log(LogUtil.error(getClass(), e));
			throw e;
		}
		if (!mustOpen(startUp)) {
			return this;
		}

		setOpen(true);

		fTipDialog = new TipDialog(Display.getCurrent().getActiveShell(), this, TipDialog.DEFAULT_STYLE);
		fTipDialog.open();
		fTipDialog.getShell().addDisposeListener(pE -> {
			dispose();
		});
		return this;
	}

	// Open if not a startup call or if there are unread tips.
	private boolean mustOpen(boolean startUp) {
		if (!startUp) {
			return true;
		}
		if (startUp && isRunAtStartup()) {
			for (TipProvider provider : getProviders()) {
				if (provider.isReady() && !provider.getTips().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

}