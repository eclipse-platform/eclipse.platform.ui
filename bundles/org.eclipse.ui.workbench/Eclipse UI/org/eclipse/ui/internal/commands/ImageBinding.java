/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.commands;

import org.eclipse.ui.commands.IImageBinding;

final class ImageBinding implements IImageBinding {

	private String commandId;
	private String imageStyle;
	private String imageUri;
	private String locale;
	private String platform;
	private String pluginId;

	ImageBinding(String commandId, String imageStyle, String imageUri, String locale, String platform, String pluginId) {
		super();
		this.commandId = commandId;
		this.imageStyle = imageStyle;
		this.imageUri = imageUri;
		this.locale = locale;
		this.platform = platform;
		this.pluginId = pluginId;
	}

	public String getCommandId() {
		return commandId;
	}

	public String getImageStyle() {
		return imageStyle;
	}

	public String getImageUri() {
		return imageUri;
	}

	public String getLocale() {
		return locale;
	}

	public String getPlatform() {
		return platform;
	}
	
	public String getPluginId() {
		return pluginId;
	}	
}
