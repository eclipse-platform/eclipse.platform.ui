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
		this.commandId = commandId;
		this.imageStyle = imageStyle;
		this.imageUri = imageUri;
		this.locale = locale;
		this.platform = platform;
		this.pluginId = pluginId;
	}
	
	public int compareTo(Object object) {
		ImageBindingDefinition imageBindingDefinition = (ImageBindingDefinition) object;
		int compareTo = Util.compare(commandId, imageBindingDefinition.commandId);
		
		if (compareTo == 0) {		
			compareTo = Util.compare(imageStyle, imageBindingDefinition.imageStyle);			

			if (compareTo == 0) {		
				compareTo = Util.compare(imageUri, imageBindingDefinition.imageUri);			

				if (compareTo == 0) {		
					compareTo = Util.compare(locale, imageBindingDefinition.locale);			

					if (compareTo == 0) {		
						compareTo = Util.compare(platform, imageBindingDefinition.platform);			
		
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
		equals &= Util.equals(commandId, imageBindingDefinition.commandId);
		equals &= Util.equals(imageStyle, imageBindingDefinition.imageStyle);
		equals &= Util.equals(imageUri, imageBindingDefinition.imageUri);
		equals &= Util.equals(locale, imageBindingDefinition.locale);
		equals &= Util.equals(platform, imageBindingDefinition.platform);
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
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(imageStyle);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(imageUri);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(locale);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(platform);
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
