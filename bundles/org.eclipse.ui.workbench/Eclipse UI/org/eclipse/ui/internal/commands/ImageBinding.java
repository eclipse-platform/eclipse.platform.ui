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

	private final static int HASH_FACTOR = 89;
	private final static int HASH_INITIAL = ImageBinding.class.getName().hashCode();

	private String imageStyle;
	private String imageUri;
	private int match;	

	private transient int hashCode;
	private transient boolean hashCodeComputed;
	private transient String string;
	
	ImageBinding(String imageStyle, String imageUri, int match) {	
		if (imageStyle == null || imageUri == null)
			throw new NullPointerException();

		if (match < 0)
			throw new IllegalArgumentException();
			
		this.imageStyle = imageStyle;
		this.imageUri = imageUri;
		this.match = match;
	}

	public int compareTo(Object object) {
		ImageBinding imageBinding = (ImageBinding) object;
		int compareTo = match - imageBinding.match;		

		if (compareTo == 0) {
			compareTo = imageStyle.compareTo(imageBinding.imageStyle);	
		
			if (compareTo == 0)
				compareTo = imageStyle.compareTo(imageBinding.imageUri);
		}
		
		return compareTo;	
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof ImageBinding))
			return false;

		ImageBinding imageBinding = (ImageBinding) object;	
		boolean equals = true;
		equals &= imageStyle.equals(imageBinding.imageStyle);
		equals &= imageUri.equals(imageBinding.imageUri);
		equals &= match == imageBinding.match;
		return equals;
	}

	public String getImageStyle() {
		return imageStyle;
	}

	public String getImageUri() {
		return imageUri;
	}
	
	public int getMatch() {
		return match;	
	}	
	
	public int hashCode() {
		if (!hashCodeComputed) {
			hashCode = HASH_INITIAL;
			hashCode = hashCode * HASH_FACTOR + imageStyle.hashCode();
			hashCode = hashCode * HASH_FACTOR + imageUri.hashCode();
			hashCode = hashCode * HASH_FACTOR + match;				
			hashCodeComputed = true;
		}
			
		return hashCode;		
	}

	public String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append('[');
			stringBuffer.append(imageStyle);
			stringBuffer.append(',');
			stringBuffer.append(imageUri);
			stringBuffer.append(',');
			stringBuffer.append(match);
			stringBuffer.append(']');
			string = stringBuffer.toString();
		}
	
		return string;		
	}
}
