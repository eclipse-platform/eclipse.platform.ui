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
import org.eclipse.ui.internal.util.Util;

final class ImageBinding implements Comparable, IImageBinding {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ImageBinding.class.getName().hashCode();

	private String commandId;
	private String imageStyle;
	private String imageUri;
	private String locale;
	private String platform;
	private String pluginId;

	ImageBinding(String commandId, String imageStyle, String imageUri, String locale, String platform, String pluginId) {
		super();
		
		if (commandId == null || imageStyle == null || imageUri == null || locale == null || platform == null)
			throw new NullPointerException();
		
		this.commandId = commandId;
		this.imageStyle = imageStyle;
		this.imageUri = imageUri;
		this.locale = locale;
		this.platform = platform;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ImageBinding imageBinding = (ImageBinding) object;
		int compareTo = commandId.compareTo(imageBinding.commandId);
		
		if (compareTo == 0) {		
			compareTo = imageStyle.compareTo(imageBinding.imageStyle);			

			if (compareTo == 0) {		
				compareTo = imageUri.compareTo(imageBinding.imageUri);			

				if (compareTo == 0) {		
					compareTo = locale.compareTo(imageBinding.locale);			

					if (compareTo == 0) {		
						compareTo = platform.compareTo(imageBinding.platform);			
		
						if (compareTo == 0)
							compareTo = Util.compare(pluginId, imageBinding.pluginId);								
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ImageBinding))
			return false;

		ImageBinding imageBinding = (ImageBinding) object;	
		return commandId.equals(imageBinding.commandId) && imageStyle.equals(imageBinding.imageStyle) && imageUri.equals(imageBinding.imageUri) && locale.equals(imageBinding.locale) && platform.equals(imageBinding.platform) && Util.equals(pluginId, imageBinding.pluginId);
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

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + commandId.hashCode();
		result = result * HASH_FACTOR + imageStyle.hashCode();
		result = result * HASH_FACTOR + imageUri.hashCode();
		result = result * HASH_FACTOR + locale.hashCode();
		result = result * HASH_FACTOR + platform.hashCode();
		result = result * HASH_FACTOR + Util.hashCode(pluginId);
		return result;
	}

	public String toString() {
		return '[' + commandId + ',' + imageStyle + ',' + imageUri + ',' + locale + ',' + platform + ',' + pluginId + ']';
	}
}
