/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.services;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public interface IResourceService {
	public interface IPooledResource<T> {
		public T getResource();
		public String getId();
		public void dispose();
	}
	
	public interface IDiposeableResourcePool extends IResourcePool {
		public void dispose();
	}
	
	public IPooledResource<Image> getImage(Display display, String key);
	public IPooledResource<Color> getColor(Display display, String key);
	public IPooledResource<Font> getFont(Display display, String key);
	
	public IDiposeableResourcePool getResourcePool(Display display);
	public IResourcePool getControlPool(Control control);
}