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
package org.eclipse.tips.examples.tips;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.eclipse.tips.core.IUrlTip;
import org.eclipse.tips.core.Tip;
import org.eclipse.tips.core.TipProvider;
import org.eclipse.tips.core.internal.LogUtil;

/**
 * Specialisation of Tip that receives an URL of a mediawiki page (e.g. Eclipse
 * wiki) and renders this URL as a tip.
 * <p>
 * For an example see:
 *
 * <a href=
 * "https://wiki.eclipse.org/Tip_of_the_Day/Eclipse_Tips/Now_where_was_I">https://wiki.eclipse.org/Tip_of_the_Day/Eclipse_Tips/Now_where_was_I</a>
 *
 */
public class MediaWikiTip extends Tip implements IUrlTip {

	private String fPageUrl;
	private Date fCreationDate;
	private String fSubject;

	/**
	 * Constructor.
	 *
	 * <p>
	 * For an example see:
	 *
	 * <a href=
	 * "https://wiki.eclipse.org/Tip_of_the_Day/Eclipse_Tips/Now_where_was_I">https://wiki.eclipse.org/Tip_of_the_Day/Eclipse_Tips/Now_where_was_I</a>
	 * </p>
	 *
	 * @param pProvider
	 *            the {@link TipProvider} that created this Tip
	 * @param pPageUrl
	 *            the Eclipse wiki page containing a simple tip
	 * @param pCreationDate
	 *            creation date
	 * @param pSubject
	 *            the tips' subject
	 */
	public MediaWikiTip(String providerId, String pPageUrl, Date pCreationDate, String pSubject) {
		super(providerId);
		fPageUrl = pPageUrl;
		fCreationDate = pCreationDate;
		fSubject = pSubject;
	}

	@Override
	public Date getCreationDate() {
		return fCreationDate;
	}

	@Override
	public String getSubject() {
		return fSubject;
	}

	@Override
	public URL getURL() {
		try {
			return new URL(fPageUrl.trim() + "?action=render");
		} catch (MalformedURLException e) {
//			getProvider().getManager().log(LogUtil.error(getClass(), e));
		}
		return null;
	}
}