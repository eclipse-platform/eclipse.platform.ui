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
package org.eclipse.tips.examples.tipsframework;

import java.util.Date;

import org.eclipse.tips.core.IUrlTip;
import org.eclipse.tips.core.Tip;
import org.eclipse.tips.examples.DateUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class MatrixRainTip extends Tip implements IUrlTip {

	public MatrixRainTip(String providerId) {
		super(providerId);
	}

	@Override
	public Date getCreationDate() {
		return DateUtil.getDateFromYYMMDD("09/01/2018");
	}

	@Override
	public String getSubject() {
		return "Welcome to the Matrix";
	}

	@Override
	public String getURL() {
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		return bundle.getEntry("matrixrain/index.html").toString();
	}
}