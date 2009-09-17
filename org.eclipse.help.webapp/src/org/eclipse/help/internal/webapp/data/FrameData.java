/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.help.webapp.AbstractFrame;

public class FrameData extends RequestData {
	
	private static final String FRAME_EXTENSION_POINT = "org.eclipse.help.webapp.frame"; //$NON-NLS-1$
	private List allFrames;

	public FrameData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
	}
	
	public AbstractFrame[] getFrames(int location) {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = registry
				.getConfigurationElementsFor(FRAME_EXTENSION_POINT);
		if (allFrames == null) {
			allFrames = new ArrayList();
			for (int i = 0; i < elements.length; i++) {
				Object obj = null;
				try {
					obj = elements[i].createExecutableExtension("class"); //$NON-NLS-1$
				} catch (CoreException e) {
					HelpWebappPlugin.logError("Create extension failed:[" //$NON-NLS-1$
							+ FRAME_EXTENSION_POINT + "].", e); //$NON-NLS-1$
				}
				if (obj instanceof AbstractFrame) {
					allFrames.add(obj);
				}
			}
			Collections.sort(allFrames);
		}

		List frameList = new ArrayList();
		for (Iterator iter = allFrames.iterator(); iter.hasNext();) {
			AbstractFrame frame = (AbstractFrame) iter.next();
			if (frame.isVisible() && frame.getLocation() == location) {
				frameList.add(frame);
			}
		}			
		AbstractFrame[] frames = (AbstractFrame[]) frameList.toArray(new AbstractFrame[frameList.size()]);
		return frames;		
	}
	
	public String getUrl(AbstractFrame frame) {
		return request.getContextPath() + frame.getURL();
	}
	
	public String getContentAreaFrameSizes() {
		String size = "24,*"; //$NON-NLS-1$
		AbstractFrame[] frames = getFrames(AbstractFrame.BELOW_CONTENT);
		for (int f = 0; f < frames.length; f++) {
			size += ',';
			size += frames[f].getSize();
		}
		return size;
	}

}
