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
 * Specialisation of Tip that receives an URL of a tweet in the constructor. The
 * URL points directly to the tweet:
 * <p>
 * For an example see: <a href=
 * "https://twitter.com/EclipseJavaIDE/status/919915440041840641">https://twitter.com/EclipseJavaIDE/status/919915440041840641</a>
 * </p>
 */
public class TwitterTip extends Tip implements IUrlTip {

	private String fPageUrl;
	private Date fCreationDate;
	private String fSubject;

	/**
	 * Constructor.
	 *
	 * <p>
	 * For an example see: <a href=
	 * "https://twitter.com/EclipseJavaIDE/status/919915440041840641">https://twitter.com/EclipseJavaIDE/status/919915440041840641</a>
	 * </p>
	 *
	 * @param pProvider
	 *            the {@link TipProvider} that created this Tip
	 * @param pTweetUrl
	 *            the URL of the tweet
	 * @param pCreationDate
	 *            creation date
	 * @param pSubject
	 *            the tips' subject
	 */
	public TwitterTip(String providerId, String pTweetUrl, Date pCreationDate, String pSubject) {
		super(providerId);
		fPageUrl = pTweetUrl;
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
			return new URL(fPageUrl);
		} catch (MalformedURLException e) {
//			getProvider().getManager().log(LogUtil.error(getClass(), e));
		}
		return null;
	}
}