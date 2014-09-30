/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.tools.services.IResourceProviderService;
import org.eclipse.e4.tools.services.IResourceService;
import org.eclipse.e4.tools.services.ToolsServicesActivator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ResourceService implements IResourceService {
	private enum Type {
		IMAGE, FONT, COLOR
	}

	static class PooledResource<T extends Resource> implements
		IPooledResource<T> {
		private final Display display;
		private int count;
		private T resource;
		private String id;
		private ResourceService resourceService;

		PooledResource(Display display, ResourceService resourceService,
			String id, T resource) {
			this.display = display;
			this.id = id;
			this.count = 1;
			this.resourceService = resourceService;
			this.resource = resource;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public T getResource() {
			return resource;
		}

		@Override
		public void dispose() {
			this.count--;
			if (this.count == 0) {
				resourceService.removePooledResource(this);
				if (resource != null) {
					resource.dispose();
				}
				resource = null;
				id = null;
				resourceService = null;
			}
		}
	}

	public static class ResourcePool implements IDiposeableResourcePool {
		private IResourceService resourceService;

		private List<IPooledResource<Image>> pooledImages = new ArrayList<IPooledResource<Image>>();
		private List<IPooledResource<Font>> pooledFonts = new ArrayList<IPooledResource<Font>>();
		private List<IPooledResource<Color>> pooledColors = new ArrayList<IPooledResource<Color>>();
		private final Display display;

		@Inject
		public ResourcePool(IResourceService resourceService, Display display) {
			this.display = display;
			this.resourceService = resourceService;
		}

		@Override
		public Image getImage(String key) throws CoreException {
			if (resourceService == null) {
				throw new CoreException(
					new Status(IStatus.ERROR,
						ToolsServicesActivator.PLUGIN_ID,
						Messages.ResourceService_PoolDisposed));
			}
			IPooledResource<Image> image = null;

			for (final IPooledResource<Image> img : pooledImages) {
				if (img.getId().equals(key)) {
					image = img;
				}
			}
			if (image == null) {
				image = resourceService.getImage(display, key);
				pooledImages.add(image);
			}

			return image.getResource();
		}

		@Override
		public Font getFont(String key) throws CoreException {
			if (resourceService == null) {
				throw new CoreException(
					new Status(IStatus.ERROR,
						ToolsServicesActivator.PLUGIN_ID,
						Messages.ResourceService_PoolDisposed));
			}

			IPooledResource<Font> font = null;
			for (final IPooledResource<Font> fon : pooledFonts) {
				if (fon.getId().equals(key)) {
					font = fon;
				}
			}
			if (font == null) {
				font = resourceService.getFont(display, key);
				pooledFonts.add(font);
			}
			return font.getResource();
		}

		@Override
		public Color getColor(String key) throws CoreException {
			if (resourceService == null) {
				throw new CoreException(
					new Status(IStatus.ERROR,
						ToolsServicesActivator.PLUGIN_ID,
						Messages.ResourceService_PoolDisposed));
			}
			IPooledResource<Color> color = null;

			for (final IPooledResource<Color> col : pooledColors) {
				if (col.getId().equals(key)) {
					color = col;
				}
			}

			if (color == null) {
				color = resourceService.getColor(display,
					key);
				pooledColors.add(color);
			}
			return color.getResource();
		}

		@Override
		public Image getImageUnchecked(String key) {
			try {
				return getImage(key);
			} catch (final CoreException e) {
				return null;
			}
		}

		@Override
		public Font getFontUnchecked(String key) {
			try {
				return getFont(key);
			} catch (final CoreException e) {
				return null;
			}
		}

		@Override
		public Color getColorUnchecked(String key) {
			try {
				return getColor(key);
			} catch (final CoreException e) {
				return null;
			}
		}

		@Override
		@PreDestroy
		public void dispose() {
			for (final IPooledResource<Image> img : pooledImages) {
				img.dispose();
			}
			for (final IPooledResource<Font> font : pooledFonts) {
				font.dispose();
			}
			for (final IPooledResource<Color> col : pooledColors) {
				col.dispose();
			}
			resourceService = null;
			pooledImages = null;
			pooledFonts = null;
			pooledColors = null;
		}
	}

	static class DisplayPool {
		private Map<String, PooledResource<Image>> imagePool;
		private Map<String, PooledResource<Color>> colorPool;
		private Map<String, PooledResource<Font>> fontPool;

		public Map<String, PooledResource<Color>> getColorPool() {
			if (colorPool == null) {
				colorPool = new HashMap<String, ResourceService.PooledResource<Color>>();
			}
			return colorPool;
		}

		public Map<String, PooledResource<Image>> getImagePool() {
			if (imagePool == null) {
				imagePool = new HashMap<String, ResourceService.PooledResource<Image>>();
			}
			return imagePool;
		}

		public Map<String, PooledResource<Font>> getFontPool() {
			if (fontPool == null) {
				fontPool = new HashMap<String, ResourceService.PooledResource<Font>>();
			}
			return fontPool;
		}
	}

	private final Map<Display, DisplayPool> displayPool = new HashMap<Display, ResourceService.DisplayPool>();
	// private Map<String, IResourceProviderService> imagekey2providers = new
	// HashMap<String, IResourceProviderService>();
	// private Map<String, IResourceProviderService> fontkey2providers = new
	// HashMap<String, IResourceProviderService>();
	// private Map<String, IResourceProviderService> colorkey2providers = new
	// HashMap<String, IResourceProviderService>();
	private final BundleContext context;

	public ResourceService() {
		final Bundle b = FrameworkUtil.getBundle(ResourceService.class);
		context = b.getBundleContext();
	}

	protected void removePooledResource(PooledResource<?> resource) {
		if (resource.getResource() instanceof Image) {
			displayPool.get(resource.display).getImagePool().remove(resource.getId());
		} else if (resource.getResource() instanceof Color) {
			displayPool.get(resource.display).getColorPool().remove(resource.getId());
		} else if (resource.getResource() instanceof Font) {
			displayPool.get(resource.display).getFontPool().remove(resource.getId());
		}
	}

	@SuppressWarnings("unchecked")
	private <R extends Resource> PooledResource<R> loadResource(
		Display display, String key, Type type) {
		DisplayPool p = displayPool.get(display);
		PooledResource<R> resource = null;

		if (p != null) {
			if (type == Type.IMAGE) {
				resource = (PooledResource<R>) p.getImagePool().get(key);
			} else if (type == Type.COLOR) {
				resource = (PooledResource<R>) p.getColorPool().get(key);
			} else {
				resource = (PooledResource<R>) p.getFontPool().get(key);
			}
		}

		if (resource != null && resource.getResource() != null) {
			resource.count++;
		} else {
			resource = new PooledResource<R>(display, this, key,
				(R) lookupResource(display, key, type));

			if (p == null) {
				p = new DisplayPool();
				displayPool.put(display, p);
			}

			if (type == Type.IMAGE) {
				p.getImagePool().put(key, (PooledResource<Image>) resource);
			} else if (type == Type.COLOR) {
				p.getColorPool().put(key, (PooledResource<Color>) resource);
			} else {
				p.getFontPool().put(key, (PooledResource<Font>) resource);
			}

		}

		return resource;
	}

	@SuppressWarnings("unchecked")
	private <R> R lookupResource(Display display, String key, Type type) {

		if (type == Type.IMAGE) {
			final IResourceProviderService provider = lookupOSGI(key);
			if (provider != null) {
				return (R) provider.getImage(display, key);
			}
		} else if (type == Type.COLOR) {
			final IResourceProviderService provider = lookupOSGI(key);
			if (provider != null) {
				return (R) provider.getColor(display, key);
			}

		} else {
			final IResourceProviderService provider = lookupOSGI(key);
			if (provider != null) {
				return (R) provider.getFont(display, key);
			}
		}
		throw new IllegalArgumentException(Messages.ResourceService_NoProvider + key
			+ "'."); //$NON-NLS-1$
	}

	private IResourceProviderService lookupOSGI(String key) {
		try {
			final Collection<ServiceReference<IResourceProviderService>> refs = context
				.getServiceReferences(IResourceProviderService.class, "(" //$NON-NLS-1$
					+ key + "=*)"); //$NON-NLS-1$
			if (!refs.isEmpty()) {
				final ServiceReference<IResourceProviderService> ref = refs
					.iterator().next();
				return context.getService(ref);
			}
		} catch (final InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// public void addProvider(IResourceProviderService provider,
	// Map<String, String> map) {
	// for (Entry<String, String> e : map.entrySet()) {
	// if( e.getKey().startsWith("IMAGE") ) {
	// imagekey2providers.put(e.getKey(), provider);
	// } else if( e.getKey().startsWith("FONT") ) {
	// fontkey2providers.put(e.getKey(), provider);
	// } else if( e.getKey().startsWith("COLOR") ) {
	// colorkey2providers.put(e.getKey(), provider);
	// }
	// }
	// }
	//
	// public void removeProvider(IResourceProviderService provider,
	// Map<String, String> map) {
	// for (Entry<String, String> e : map.entrySet()) {
	// if( e.getKey().startsWith("IMAGE") ) {
	// imagekey2providers.remove(e.getKey());
	// } else if( e.getKey().startsWith("FONT") ) {
	// fontkey2providers.remove(e.getKey());
	// } else if( e.getKey().startsWith("COLOR") ) {
	// colorkey2providers.remove(e.getKey());
	// }
	// }
	// }

	@Override
	public PooledResource<Image> getImage(Display display, String key) {
		return loadResource(display, key, Type.IMAGE);
	}

	@Override
	public PooledResource<Color> getColor(Display display, String key) {
		return loadResource(display, key, Type.COLOR);
	}

	@Override
	public PooledResource<Font> getFont(Display display, String key) {
		return loadResource(display, key, Type.FONT);
	}

	@Override
	public IDiposeableResourcePool getResourcePool(Display display) {
		return new ResourcePool(this, display);
	}

	@Override
	public IResourcePool getControlPool(Control control) {
		final ResourcePool pool = new ResourcePool(this, control.getDisplay());
		control.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				pool.dispose();
			}
		});
		return pool;
	}

}
