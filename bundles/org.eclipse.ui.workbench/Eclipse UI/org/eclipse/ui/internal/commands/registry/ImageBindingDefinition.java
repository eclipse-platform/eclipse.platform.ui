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

package org.eclipse.ui.internal.commands.registry;

import org.eclipse.ui.commands.IImageBindingDefinition;
import org.eclipse.ui.internal.util.Util;

final class ImageBindingDefinition implements Comparable, IImageBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ImageBindingDefinition.class.getName().hashCode();

	private String commandId;
	private String imageStyle;
	private String imageUri;
	private String locale;
	private String platform;
	private String pluginId;

	ImageBindingDefinition(String commandId, String imageStyle, String imageUri, String locale, String platform, String pluginId) {
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
		ImageBindingDefinition imageBindingDefinition = (ImageBindingDefinition) object;
		int compareTo = commandId.compareTo(imageBindingDefinition.commandId);
		
		if (compareTo == 0) {		
			compareTo = imageStyle.compareTo(imageBindingDefinition.imageStyle);			

			if (compareTo == 0) {		
				compareTo = imageUri.compareTo(imageBindingDefinition.imageUri);			

				if (compareTo == 0) {		
					compareTo = locale.compareTo(imageBindingDefinition.locale);			

					if (compareTo == 0) {		
						compareTo = platform.compareTo(imageBindingDefinition.platform);			
		
						if (compareTo == 0)
							compareTo = Util.compare(pluginId, imageBindingDefinition.pluginId);								
					}
				}
			}
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ImageBindingDefinition))
			return false;

		ImageBindingDefinition imageBindingDefinition = (ImageBindingDefinition) object;	
		return commandId.equals(imageBindingDefinition.commandId) && imageStyle.equals(imageBindingDefinition.imageStyle) && imageUri.equals(imageBindingDefinition.imageUri) && locale.equals(imageBindingDefinition.locale) && platform.equals(imageBindingDefinition.platform) && Util.equals(pluginId, imageBindingDefinition.pluginId);
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
