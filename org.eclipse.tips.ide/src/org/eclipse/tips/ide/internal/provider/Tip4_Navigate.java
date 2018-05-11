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
package org.eclipse.tips.ide.internal.provider;

import java.util.Date;

import org.eclipse.tips.core.IHtmlTip;
import org.eclipse.tips.core.Tip;
import org.eclipse.tips.core.TipImage;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class Tip4_Navigate extends Tip implements IHtmlTip {

	private TipImage fImage;

	@Override
	public TipImage getImage() {
		if (fImage == null) {
			try {
				Bundle bundle = FrameworkUtil.getBundle(getClass());
				fImage = new TipImage(bundle.getEntry("images/tips/navigate1.png")).setAspectRatio(570, 91, true);
			} catch (Exception e) {
			}
		}
		return fImage;
	}

	public Tip4_Navigate(String providerId) {
		super(providerId);
	}

	@Override
	public Date getCreationDate() {
		return TipsTipProvider.getDateFromYYMMDD("09/01/2019");
	}

	@Override
	public String getSubject() {
		return "Navigate Tip 1";
	}

	@Override
	public String getHTML() {
		return "<h2>Navigating Tips</h2>You can navigate tips by using the button bar."
				+ "<br><br><b>Show tips at startup</b><br>A toggle to show this dialog when you start Eclipse."
				+ "<br><b>Unread only</b><br>Shows all Tips when unchecked or unread Tips when checked."
				+ "<br><b>Next Tip</b><br>Navigates to the next tip."
				+ "<br><b>Previous Tip</b><br>Navigates to the previous tip <i>if reading all tips</i>."
				+ "<br><b>Close</b><br>Closes the Dialog (<b>Escape</b> does the same)." + "<br><br><br>";
	}
}