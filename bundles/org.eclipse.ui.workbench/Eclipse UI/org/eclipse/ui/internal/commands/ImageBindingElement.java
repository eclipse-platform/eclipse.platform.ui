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

import org.eclipse.ui.internal.util.Util;

final class ImageBindingElement implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ImageBindingElement.class.getName().hashCode();
	
	static ImageBindingElement create(String commandId, String imageStyle, String imageUri, String locale, String platform, String pluginId)
		throws IllegalArgumentException {
		return new ImageBindingElement(commandId, imageStyle, imageUri, locale, platform, pluginId);
	}

	private String commandId;
	private String imageStyle;
	private String imageUri;
	private String locale;
	private String platform;
	private String pluginId;
	
	private ImageBindingElement(String commandId, String imageStyle, String imageUri, String locale, String platform, String pluginId)
		throws IllegalArgumentException {
		super();
		
		if (commandId == null || imageStyle == null || imageUri == null || locale == null || platform == null)
			throw new IllegalArgumentException();
		
		this.commandId = commandId;
		this.imageStyle = imageStyle;
		this.imageUri = imageUri;
		this.locale = locale;
		this.platform = platform;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ImageBindingElement imageBindingElement = (ImageBindingElement) object;
		int compareTo = commandId.compareTo(imageBindingElement.commandId);

		if (compareTo == 0) {	
			compareTo = imageStyle.compareTo(imageBindingElement.imageStyle);		
		
			if (compareTo == 0) {	
				compareTo = imageUri.compareTo(imageBindingElement.imageUri);			

				if (compareTo == 0) {	
					compareTo = locale.compareTo(imageBindingElement.locale);		
					
					if (compareTo == 0) {	
						compareTo = platform.compareTo(imageBindingElement.platform);		
			
						if (compareTo == 0)
							compareTo = Util.compare(pluginId, imageBindingElement.pluginId);		
					}
				}							
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ImageBindingElement))
			return false;

		ImageBindingElement imageBindingElement = (ImageBindingElement) object;	
		return commandId.equals(imageBindingElement.commandId) && imageStyle.equals(imageBindingElement.imageStyle) && imageUri.equals(imageBindingElement.imageUri) && locale.equals(imageBindingElement.locale) && platform.equals(imageBindingElement.platform) && Util.equals(pluginId, imageBindingElement.pluginId);
	}

	String getCommandId() {
		return commandId;	
	}

	String getImageStyle() {
		return imageStyle;	
	}
	
	String getImageUri() {
		return imageUri;	
	}

	String getLocale() {
		return locale;	
	}

	String getPlatform() {
		return platform;	
	}
	
	String getPluginId() {
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
