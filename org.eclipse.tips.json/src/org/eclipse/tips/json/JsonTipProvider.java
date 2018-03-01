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
package org.eclipse.tips.json;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.tips.core.Tip;
import org.eclipse.tips.core.TipImage;
import org.eclipse.tips.core.TipProvider;
import org.eclipse.tips.core.internal.LogUtil;
import org.eclipse.tips.json.internal.JsonConstants;
import org.eclipse.tips.json.internal.JsonHTMLTip;
import org.eclipse.tips.json.internal.JsonUrlTip;
import org.eclipse.tips.json.internal.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A special TipProvider that gets instantiated from a JSon file.
 *
 */
@SuppressWarnings("restriction")
public abstract class JsonTipProvider extends TipProvider {

	private URL fJsonUrl;
	private String fDescription;
	private String fImage;

	/**
	 * A method to set the a url containing a JSon file that describes this tip
	 * provider.
	 *
	 * @param jsonUrl
	 *            the uRL to the Json file describing the provider and tips
	 * @throws MalformedURLException
	 *             in case of an incorrect URL
	 */
	public void setJsonUrl(String jsonUrl) throws MalformedURLException {
		fJsonUrl = new URL(jsonUrl);
	}

	@Override
	public IStatus loadNewTips(IProgressMonitor monitor) {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		ArrayList<Tip> result = new ArrayList<>();
		try {
			subMonitor.beginTask(getDescription() + " Loading Tips", -1);
			JsonObject value = (JsonObject) new JsonParser().parse(new InputStreamReader(fJsonUrl.openStream()));
			JsonObject provider = value.getAsJsonObject(JsonConstants.P_PROVIDER);
			fDescription = Util.getValueOrDefault(provider, JsonConstants.P_DESCRIPTION, "not set");
			fImage = Util.getValueOrDefault(provider, JsonConstants.P_IMAGE, null);
			setExpression(Util.getValueOrDefault(provider, JsonConstants.P_EXPRESSION, null));
			JsonArray tips = provider.getAsJsonArray(JsonConstants.P_TIPS);
			subMonitor.beginTask(getDescription() + " Creating Tips", -1);
			tips.forEach(parm -> result.add(createJsonTip(parm)));
		} catch (Exception e) {
			Status status = new Status(IStatus.ERROR, "org.eclipse.tips.json", e.getMessage(), e);
			getManager().log(status);
			return status;
		}
		setTips(result);
		subMonitor.done();
		return Status.OK_STATUS;
	}

	@Override
	public TipImage getImage() {
		if (fImage == null) {
			return null;
		}
		return new TipImage(fImage);

	}

	@Override
	public String getDescription() {
		return fDescription;
	}

	private Tip createJsonTip(JsonElement parm) {
		JsonObject json = (JsonObject) parm;
		try {
			if (json.get(JsonConstants.T_URL) == null) {
				return new JsonHTMLTip(getID(), json);
			} else {
				return new JsonUrlTip(getID(), json);
			}
		} catch (ParseException e) {
			getManager().log(LogUtil.error(getClass(), e));
			throw new RuntimeException(e);
		}
	}

	@Override
	public void dispose() {
	}
}