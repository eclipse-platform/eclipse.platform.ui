/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.css.core.resources.AbstractResourcesRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * SWT Resources Registry to cache SWT Resource like Color, Cursor and Font and
 * dispose it.
 */
public class SWTResourcesRegistry extends AbstractResourcesRegistry {

	public SWTResourcesRegistry(Display display) {
		if (display == null) {
			return;
		}
		// When SWT Display will dispose, all SWT resources stored
		// into cache will be dispose it too.
		display.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				dispose();
			}
		});
	}

	@Override
	public Object getResource(Object type, Object key) {
		Object resource = super.getResource(type, key);
		if (resource != null) {
			// test if resource is disposed
			if (isDisposed(resource)) {
				// SWT Resource is disposed
				// unregister it.
				super.unregisterResource(resource);
				return null;
			}
		}
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.resources.AbstractResourcesRegistry#registerResource(java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	@Override
	public void registerResource(Object type, Object key, Object resource) {
		if (resource == null)
		{
			return;
			//		String hit = getResource(type, key) != null
			//			? " hit "
			//			: " ";
			//TODO replace with eclipse logging
			//		if (resource instanceof Color) {
			//			System.out.println("key class = " + key.getClass());
			//			System.out.println("Cache " + hit + "SWT Color key= " + key);
			//		} else if (resource instanceof Cursor) {
			//			System.out.println("Cache" + hit + "SWT Cursor key=" + key);
			//		} else if (resource instanceof Font) {
			//			System.out.println("Cache" + hit + "SWT Font key=" + key);
			//		} else if (resource instanceof Image) {
			//			System.out.println("Cache" + hit + "SWT Image key=" + key);
			//		} else
			//			System.out.println("Cache" + hit + "Resource key=" + key);
		}

		super.registerResource(type, key, resource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.core.css.resources.AbstractResourcesRegistry#disposeResource(java.lang.Object,
	 *      java.lang.String, java.lang.Object)
	 */
	@Override
	public void disposeResource(Object type, Object key, Object resource) {
		// Dispose SWT Resource
		if (resource instanceof Color) {
			((Color)resource).dispose();
			//TODO replace with eclipse logging
			//			if (logger.isDebugEnabled())
			//				logger.debug("Dispose SWT Color key=" + key);
		} else if (resource instanceof Cursor) {
			((Cursor)resource).dispose();
			//TODO replace with eclipse logging
			//			if (logger.isDebugEnabled())
			//				logger.debug("Dispose SWT Cursor key=" + key);
		} else if (resource instanceof Font) {
			((Font)resource).dispose();
			//TODO replace with eclipse logging
			//			if (logger.isDebugEnabled())
			//				logger.debug("Dispose SWT Font key=" + key);
		} else if (resource instanceof Image) {
			((Image) resource).dispose();
			//TODO replace with eclipse logging
			//			if (logger.isDebugEnabled())
			//				logger.debug("Dispose SWT Image key=" + key);
		}
		//TODO replace with eclipse logging
		//		else if (logger.isDebugEnabled())
		//			logger.debug("Dispose Resource key=" + key);
	}

	protected boolean isDisposed(Object resource) {
		if (resource instanceof Color) {
			return ((Color) resource).isDisposed();
		} else if (resource instanceof Font) {
			return ((Font) resource).isDisposed();
		} else if (resource instanceof Image) {
			return ((Image) resource).isDisposed();
		} else if (resource instanceof Cursor) {
			return ((Cursor) resource).isDisposed();
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<Object> removeResourcesByKeyTypeAndType(Class<?> keyType,
			Class<?>... types) {
		List<Object> removedResources = new ArrayList<Object>();
		for (Class<?> cls : types) {
			Iterator<Map.Entry<?, ?>> iter = getCacheByType(cls).entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<?, ?> entry = iter.next();
				if (keyType.isAssignableFrom(entry.getKey().getClass())) {
					removedResources.add(entry.getValue());
					iter.remove();
				}
			}
		}
		return removedResources;
	}
}
