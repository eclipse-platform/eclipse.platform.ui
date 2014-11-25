/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A manager for images. Features include:
 * <ul>
 * <li>Eliminates the need to pass in Display when creating images
 * <li>Loading an image from a bundle
 * <li>Cleanup of cache
 * </ul>
 *
 * Images returned by this method must never be disposed of outside of this
 * class, as placeholders are used.
 *
 * @author "Steven Spungin"
 *
 */
public class BundleImageCache {

	private final Display display;
	private final ClassLoader classloader;
	ArrayList<Image> images;
	static private Image imgPlaceholder;
	private final IEclipseContext context;

	public BundleImageCache(Display display, ClassLoader classloader) {
		this(display, classloader, null);
	}

	public BundleImageCache(Display display, ClassLoader classloader, IEclipseContext context) {
		this.display = display;
		this.classloader = classloader;
		this.context = context;
		images = new ArrayList<Image>();
	}

	/**
	 * Creates the image, and tracks it for a bulk dispose operation.
	 *
	 * @param path
	 * @return the {@link Image}
	 */
	public Image create(String path) {
		Image img;
		try {
			img = new Image(display, classloader.getResourceAsStream(path));
			images.add(img);
		} catch (final Exception e) {
			e.printStackTrace();
			img = getPlaceholder();
		}
		return img;
	}

	/**
	 * Uses IResourcePool to obtain image. Does not track object.
	 *
	 * @param key
	 * @return The {@link Image}
	 */
	public Image loadFromKey(String key) {
		Image ret = null;
		try {
			ret = context.get(IResourcePool.class).getImageUnchecked(key);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		if (ret == null) {
			ret = getPlaceholder();
		}
		return ret;
	}

	protected Image getPlaceholder() {
		if (imgPlaceholder == null) {
			imgPlaceholder = new Image(Display.getDefault(),
				classloader.getResourceAsStream("/icons/full/obj16/missing_image_placeholder.png")); //$NON-NLS-1$
		}
		return imgPlaceholder;
	}

	@Override
	protected void finalize() throws Throwable {
		if (imgPlaceholder != null && imgPlaceholder.isDisposed() == false) {
			imgPlaceholder.dispose();
		}
	}

	public void dispose() {
		for (final Iterator<Image> it = images.iterator(); it.hasNext();) {
			final Image image = it.next();
			if (image.isDisposed() == false) {
				image.dispose();
			}
			it.remove();
		}
	}

	public Image create(String bundleId, String path) {
		if (path.startsWith("/") == false) { //$NON-NLS-1$
			path = "/" + path; //$NON-NLS-1$
		}
		Image img;
		try {
			img = new Image(display, new URL("platform:/plugin/" + bundleId + path).openStream()); //$NON-NLS-1$
			images.add(img);
		} catch (final Exception e) {
			e.printStackTrace();
			img = getPlaceholder();
		}
		return img;
	}

}
