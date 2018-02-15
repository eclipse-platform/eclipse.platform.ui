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
package org.eclipse.tips.core.internal;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.tips.core.IHtmlTip;
import org.eclipse.tips.core.Tip;
import org.eclipse.tips.core.TipImage;
import org.eclipse.tips.core.TipProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Special generic tip that tells the user that there are no more tips.
 *
 */
public class FinalTip extends Tip implements IHtmlTip {

	/**
	 * Constructor.
	 */
	public FinalTip(String providerId) {
		super(providerId);
	}

	@Override
	public Date getCreationDate() {
		Calendar instance = Calendar.getInstance();
		return instance.getTime();
	}

	@Override
	public String getSubject() {
		return "No more tips";
	}

	@Override
	public String getHTML() {
		return "<h1>There are no more tips</h1>" //
				+ "This provider has no more new tips. You can toggle the " //
				+ "<b>Unread</b> checkbox below or select another provider.";
	}

	@Override
	public TipImage getImage() {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		try {
			return new TipImage(bundle.getEntry("images/nomoretips.png")).setAspectRatio(417, 640, false);
		} catch (IOException e) {
//			getManager().log(LogUtil.error(getClass(), e));
		}
		return null;
	}
}