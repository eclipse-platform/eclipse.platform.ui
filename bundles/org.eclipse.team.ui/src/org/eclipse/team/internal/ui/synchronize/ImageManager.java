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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.resource.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.core.ICache;
import org.eclipse.team.core.ICacheListener;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.TeamUIPlugin;

public class ImageManager {
	
	private static final String PROP_IMAGE_MANAGER = TeamUIPlugin.ID + ".imageManager"; //$NON-NLS-1$
	
	private LocalResourceManager imageManager;
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
		ResourceManager manager = getResourceManager();
		Image image = manager.createImage(descriptor);
		return image;
	}
	
	private synchronized ResourceManager getResourceManager() {
		if (imageManager == null) {
			imageManager = new LocalResourceManager(JFaceResources.getResources());
		}
		return imageManager;
	}

	public void dispose() {
		disposed = true;
		compareConfig.dispose();
		if (imageManager != null) {
			imageManager.dispose();
		}
	}

	public Image getImage(Image base, int compareKind) {
		if (disposed)
			return null;
		return compareConfig.getImage(base, compareKind);
	}
}