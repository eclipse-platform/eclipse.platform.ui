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

import org.eclipse.ui.internal.util.Util;

final class ImageBindingDefinition implements IImageBindingDefinition {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ImageBindingDefinition.class.getName().hashCode();

	private String commandId;
	private String imageStyle;
	private String imageUri;
	private String locale;
	private String platform;
	private String pluginId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;

	ImageBindingDefinition(String commandId, String imageStyle, String imageUri, String locale, String platform, String pluginId) {
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
		boolean equals = true;
		equals &= commandId.equals(imageBindingDefinition.commandId);
		equals &= imageStyle.equals(imageBindingDefinition.imageStyle);
		equals &= imageUri.equals(imageBindingDefinition.imageUri);
		equals &= locale.equals(imageBindingDefinition.locale);
		equals &= platform.equals(imageBindingDefinition.platform);
		equals &= Util.equals(pluginId, imageBindingDefinition.pluginId);
		return equals;
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
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + commandId.hashCode();
			hashCode = hashCode * HASH_FACTOR + imageStyle.hashCode();
			hashCode = hashCode * HASH_FACTOR + imageUri.hashCode();
			hashCode = hashCode * HASH_FACTOR + locale.hashCode();
			hashCode = hashCode * HASH_FACTOR + platform.hashCode();
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(pluginId);
			hashCodeComputed = true;
		}
			
		return hashCode;
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(commandId);
			stringBuffer.append(',');
			stringBuffer.append(imageStyle);
			stringBuffer.append(',');
			stringBuffer.append(imageUri);
			stringBuffer.append(',');
			stringBuffer.append(locale);
			stringBuffer.append(',');
			stringBuffer.append(platform);
			stringBuffer.append(',');
			stringBuffer.append(pluginId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;
	}
}
