/*******************************************************************************
 * Copyright (c) 2011 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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