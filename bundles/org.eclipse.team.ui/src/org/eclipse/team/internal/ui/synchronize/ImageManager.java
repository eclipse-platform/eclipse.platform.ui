/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.util.*;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.ICache;
import org.eclipse.team.core.ICacheListener;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIPlugin;

public class ImageManager {
	
	private static final String PROP_IMAGE_MANAGER = TeamUIPlugin.ID + ".imageManager"; //$NON-NLS-1$
	
	// Cache for images that have been overlayed
	private Map fgImageCache = new HashMap(10);
	
	// Contains direction images
	private CompareConfiguration compareConfig = new CompareConfiguration();
	
	private boolean disposed = false;
	
	public synchronized static ImageManager getImageManager(ISynchronizationContext context) {
		ImageManager manager = (ImageManager)context.getCache().get(PROP_IMAGE_MANAGER);
		if (manager == null) {
			final ImageManager newRegistry = new ImageManager();
			context.getCache().put(PROP_IMAGE_MANAGER, newRegistry);
			context.getCache().addCacheListener(new ICacheListener() {
				public void cacheDisposed(ICache cache) {
					newRegistry.dispose();
				}
			});
			manager = newRegistry;
		}
		return manager;
	}
	
	public Image getImage(ImageDescriptor descriptor) {
		if (descriptor == null || disposed)
			return null;
		Image image = (Image)fgImageCache.get(descriptor);
		if (image == null) {
			image = descriptor.createImage();
			fgImageCache.put(descriptor, image);
		}
		return image;
	}
	
	public void dispose() {
		disposed = true;
		compareConfig.dispose();
		if (fgImageCache != null) {
			Iterator it = fgImageCache.values().iterator();
			while (it.hasNext()) {
				Image element = (Image) it.next();
				element.dispose();
			}
		}
	}

	public Image getImage(Image base, int compareKind) {
		if (disposed)
			return null;
		return compareConfig.getImage(base, compareKind);
	}
}