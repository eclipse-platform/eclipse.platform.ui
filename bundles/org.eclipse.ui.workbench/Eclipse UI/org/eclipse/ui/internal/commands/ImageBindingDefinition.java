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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ui.internal.util.Util;

public final class ImageBindingDefinition implements Comparable {

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL =
		ImageBindingDefinition.class.getName().hashCode();

	static Map imageBindingDefinitionsByCommandId(Collection imageBindingDefinitions) {
		if (imageBindingDefinitions == null)
			throw new NullPointerException();

		Map map = new HashMap();
		Iterator iterator = imageBindingDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			Util.assertInstance(object, ImageBindingDefinition.class);
			ImageBindingDefinition imageBindingDefinition =
				(ImageBindingDefinition) object;
			String commandId = imageBindingDefinition.getCommandId();

			if (commandId != null) {
				Collection imageBindingDefinitions2 =
					(Collection) map.get(commandId);

				if (imageBindingDefinitions2 == null) {
					imageBindingDefinitions2 = new ArrayList();
					map.put(commandId, imageBindingDefinitions2);
				}

				imageBindingDefinitions2.add(imageBindingDefinition);
			}
		}

		return map;
	}

	private String commandId;

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private String imageStyle;
	private String imageUri;
	private String locale;
	private String platform;
	private String sourceId;
	private transient String string;

	public ImageBindingDefinition(
		String commandId,
		String imageStyle,
		String imageUri,
		String locale,
		String platform,
		String sourceId) {
		this.commandId = commandId;
		this.imageStyle = imageStyle;
		this.imageUri = imageUri;
		this.locale = locale;
		this.platform = platform;
		this.sourceId = sourceId;
	}

	public int compareTo(Object object) {
		ImageBindingDefinition castedObject = (ImageBindingDefinition) object;
		int compareTo = Util.compare(commandId, castedObject.commandId);

		if (compareTo == 0) {
			compareTo = Util.compare(imageStyle, castedObject.imageStyle);

			if (compareTo == 0) {
				compareTo = Util.compare(imageUri, castedObject.imageUri);

				if (compareTo == 0) {
					compareTo = Util.compare(locale, castedObject.locale);

					if (compareTo == 0) {
						compareTo =
							Util.compare(platform, castedObject.platform);

						if (compareTo == 0)
							compareTo =
								Util.compare(sourceId, castedObject.sourceId);
					}
				}
			}
		}

		return compareTo;
	}

	public boolean equals(Object object) {
		if (!(object instanceof ImageBindingDefinition))
			return false;

		ImageBindingDefinition castedObject = (ImageBindingDefinition) object;
		boolean equals = true;
		equals &= Util.equals(commandId, castedObject.commandId);
		equals &= Util.equals(imageStyle, castedObject.imageStyle);
		equals &= Util.equals(imageUri, castedObject.imageUri);
		equals &= Util.equals(locale, castedObject.locale);
		equals &= Util.equals(platform, castedObject.platform);
		equals &= Util.equals(sourceId, castedObject.sourceId);
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

	public String getSourceId() {
		return sourceId;
	}

	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(commandId);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(imageStyle);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(imageUri);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(locale);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(platform);
			hashCode = hashCode * HASH_FACTOR + Util.hashCode(sourceId);
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
			stringBuffer.append(sourceId);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}

		return string;
	}
}
