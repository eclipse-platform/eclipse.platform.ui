/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation]
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.DOMException;
import org.w3c.dom.stylesheets.MediaList;

public class MediaListImpl implements MediaList, Serializable {

	private List mediaList = null;
	
	public MediaListImpl(SACMediaList media) {
		mediaList = new ArrayList();
		for (int i = 0; i < media.getLength(); i++) {
			mediaList.add(media.item(i));
		}
		
	}

	public void appendMedium(String newMedium) throws DOMException {
		if (mediaList.contains(newMedium)) mediaList.remove(newMedium);
		mediaList.add(newMedium);
	}

	public void deleteMedium(String oldMedium) throws DOMException {
		mediaList.remove(oldMedium);
	}

	public int getLength() {
		return (mediaList != null) ? mediaList.size() : 0;
	}

	public String getMediaText() {
		String media = "";
		int size = mediaList.size();
		if (size > 0) {
			media += mediaList.get(0);
			for (int i = 1; i < mediaList.size(); i++) {
					media += ", " + mediaList.get(i);
			}
		}
		return media;
	}

	public String item(int index) {
		if (index > mediaList.size()) return null;
		return (String) mediaList.get(index);
	}

	public void setMediaText(String mediaText) throws DOMException {
		while (mediaText.length() > 0) {
			int next = mediaText.indexOf(',');
			if (next == -1) next = mediaText.length();
			String media = mediaText.substring(0, next);
			appendMedium(media.trim());
			if (next + 1 < mediaText.length()) {
				mediaText = mediaText.substring(next + 1, mediaText.length());
			} else {
				break;
			}
		}
	}

}