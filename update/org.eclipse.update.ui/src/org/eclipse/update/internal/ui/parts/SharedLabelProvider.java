/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.parts;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.update.internal.ui.*;

/**
 * @version 	1.0
 * @author
 */
public class SharedLabelProvider
	extends LabelProvider
	implements ITableLabelProvider {
	public static final int F_ERROR = 1;
	public static final int F_WARNING = 2;
	public static final int F_CURRENT = 4;
	public static final int F_INSTALLABLE = 8;
	public static final int F_LINKED = 16;
	public static final int F_MOD = 32;
	public static final int F_UPDATED = 64;
	public static final int F_UNCONFIGURED = 128;
	public static final int F_ADD = 256;
	public static final int F_DEL = 512;

	Hashtable images = new Hashtable();
	ArrayList consumers = new ArrayList();

	public SharedLabelProvider() {
	}

	public void connect(Object consumer) {
		if (!consumers.contains(consumer))
			consumers.add(consumer);
	}

	public void disconnect(Object consumer) {
		consumers.remove(consumer);
		if (consumers.size() == 0) {
			reset();
		}
	}
	
	public void dispose() {
		reset();
		super.dispose();
	}

	private void reset() {
		for (Enumeration iterator = images.elements(); iterator.hasMoreElements();) {
			Image image = (Image) iterator.nextElement();
			image.dispose();
		}
		images.clear();
	}

	public Image get(ImageDescriptor desc) {
		return get(desc, 0);
	}

	public Image get(ImageDescriptor desc, int flags) {
		Object key = desc;

		if (flags != 0) {
			key = getKey(desc.hashCode(), flags);
		}
		Image image = (Image) images.get(key);
		if (image == null) {
			image = createImage(desc, flags);
			images.put(key, image);
		}
		return image;
	}
	
	public Image get(Image image, int flags) {
		if (flags==0) return image;
		String key = getKey(image.hashCode(), flags);
		Image resultImage = (Image)images.get(key);
		if (resultImage == null) {
			resultImage = createImage(image, flags);
			images.put(key, resultImage);
		}
		return resultImage;
	}

	private String getKey(long hashCode, int flags) {
		return (""+hashCode) + ":"+flags; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Image createImage(ImageDescriptor baseDesc, int flags) {
		if (flags == 0) {
			return baseDesc.createImage();
		}
		ImageDescriptor[] lowerLeft = getLowerLeftOverlays(flags);
		ImageDescriptor[] upperRight = getUpperRightOverlays(flags);
		ImageDescriptor[] lowerRight = getLowerRightOverlays(flags);
		ImageDescriptor[] upperLeft = getUpperLeftOverlays(flags);
		OverlayIcon compDesc =
			new OverlayIcon(
				baseDesc,
				new ImageDescriptor[][] { upperRight, lowerRight, lowerLeft, upperLeft });
		return compDesc.createImage();
	}
	
	private Image createImage(Image baseImage, int flags) {
		if (flags == 0) {
			return baseImage;
		}
		ImageDescriptor[] lowerLeft = getLowerLeftOverlays(flags);
		ImageDescriptor[] upperRight = getUpperRightOverlays(flags);
		ImageDescriptor[] lowerRight = getLowerRightOverlays(flags);
		ImageDescriptor[] upperLeft = getUpperLeftOverlays(flags);
		ImageOverlayIcon compDesc =
			new ImageOverlayIcon(
				baseImage,
				new ImageDescriptor[][] { upperRight, lowerRight, lowerLeft, upperLeft });
		return compDesc.createImage();
	}

	private ImageDescriptor[] getLowerLeftOverlays(int flags) {
		if ((flags & F_ERROR) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_ERROR_CO };
		if ((flags & F_WARNING) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_WARNING_CO };
		return null;
	}

	private ImageDescriptor[] getUpperRightOverlays(int flags) {
		if ((flags & F_ADD) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_ADD_CO };
		if ((flags & F_DEL) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_DEL_CO };
		if ((flags & F_UNCONFIGURED) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_UNCONF_CO };
		if ((flags & F_LINKED) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_LINKED_CO };
		return null;
	}
	
	private ImageDescriptor[] getLowerRightOverlays(int flags) {
		if ((flags & F_CURRENT) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_CURRENT_CO };
		if ((flags & F_MOD) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_MOD_CO };
		/*
		if ((flags & F_ADD) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_ADD_CO };
		if ((flags & F_DEL) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_DEL_CO };
		if ((flags & F_UNCONFIGURED) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_UNCONF_CO };
		*/
		return null;
	}
	
	private ImageDescriptor[] getUpperLeftOverlays(int flags) {
		if ((flags & F_UPDATED) != 0)
			return new ImageDescriptor[] { UpdateUIImages.DESC_UPDATED_CO };
		return null;
	}

	public String getColumnText(Object obj, int index) {
		return getText(obj);
	}
	public Image getColumnImage(Object obj, int index) {
		return getImage(obj);
	}

	public Image getImageFromURL(
		URL installURL,
		String subdirectoryAndFilename) {
		Image image = null;
		try {
			URL newURL = new URL(installURL, subdirectoryAndFilename);
			String key = newURL.toString();
			image = (Image)images.get(key);
			if (image == null) {
				ImageDescriptor desc = ImageDescriptor.createFromURL(newURL);
				image = desc.createImage();
				images.put(key, image);
			}
		} catch (MalformedURLException e) {
		} catch (SWTException e) {
		}
		return image;
	}
}
