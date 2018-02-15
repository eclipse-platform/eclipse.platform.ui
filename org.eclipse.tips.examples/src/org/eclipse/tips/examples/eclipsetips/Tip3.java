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
package org.eclipse.tips.examples.eclipsetips;

import java.io.IOException;
import java.util.Date;

import org.eclipse.tips.core.IHtmlTip;
import org.eclipse.tips.core.Tip;
import org.eclipse.tips.core.TipImage;
import org.eclipse.tips.core.TipProvider;
import org.eclipse.tips.core.internal.LogUtil;
import org.eclipse.tips.examples.DateUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class Tip3 extends Tip implements IHtmlTip {


	public Tip3(String providerId) {
		super(providerId);
	}

	@Override
	public Date getCreationDate() {
		return DateUtil.getDateFromYYMMDD("10/01/2018");
	}

	@Override
	public String getSubject() {
		return "Auto Save";
	}

	@Override
	public String getHTML() {
		return "<h1>Automatic Save of dirty editors</h1>You can configure the automatic save of dirty editors in Eclipse via the <b>General > Editors > Autosave</b> preference page which allows you to enable/disable the autosave and change the interval of autosave.";
	}

	@Override
	public TipImage getImage() {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		try {
			return new TipImage(bundle.getEntry("images/eclipsetips/tip3.png")).setAspectRatio(658.0 / 581.0);
		} catch (IOException e) {
//			getProvider().getManager().log(LogUtil.error(getClass(), e));
		}
		return null;

	}
}