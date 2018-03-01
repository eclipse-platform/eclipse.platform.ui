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
package org.eclipse.tips.json.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.tips.core.IUrlTip;
import org.eclipse.tips.core.Tip;
import org.eclipse.tips.json.IJsonTip;

import com.google.gson.JsonObject;

/**
 * Internal implementation for a Json generated {@link IUrlTip}.
 */
public class JsonUrlTip extends Tip implements IUrlTip, IJsonTip {

	private String fSubject;
	private Date fDate;
	private String fUrl;
	private String fJsonObject;

	/**
	 * Creates the tip out of the passed Json object.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param provider
	 *            the provider
	 * @throws ParseException
	 *             when the json object could not be parsed.
	 */
	public JsonUrlTip(String providerId, JsonObject jsonObject) throws ParseException {
		super(providerId);
		fJsonObject = jsonObject.toString();
		fSubject = Util.getValueOrDefault(jsonObject, JsonConstants.T_SUBJECT, "Not set");
		fDate = getDate(jsonObject);
		fUrl = Util.getValueOrDefault(jsonObject, JsonConstants.T_URL, null);
	}

	private static Date getDate(JsonObject jsonObject) throws ParseException {
		String date = Util.getValueOrDefault(jsonObject, JsonConstants.T_DATE, "1970-01-01");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.parse(date);
	}

	@Override
	public Date getCreationDate() {
		return fDate;
	}

	@Override
	public URL getURL() {
		if (fUrl == null) {
			return null;
		}
		try {
			return new URL(fUrl);
		} catch (MalformedURLException e) {
			// TODO shoud thrown exeception and calling code should log
//			getProvider().getManager().log(LogUtil.error(getClass(), e));
			return null;
		}
	}

	@Override
	public String getSubject() {
		return fSubject;
	}

	@Override
	public String getJsonObject() {
		return fJsonObject;
	}
}