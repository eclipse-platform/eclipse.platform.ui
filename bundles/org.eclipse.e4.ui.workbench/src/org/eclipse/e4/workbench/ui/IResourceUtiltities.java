/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;

/**
 *
 */
public interface IResourceUtiltities<ColorDesc, ImageDesc> {

	public static class Gradient<ColorDesc> {
		private List<ColorDesc> colors;
		private int[] percents;
		private boolean vertical;

		public Gradient(List<ColorDesc> colors, int[] percents, boolean vertical) {
			this.colors = new ArrayList<ColorDesc>(colors);
			this.percents = percents;
			this.vertical = vertical;
		}

		public List<ColorDesc> getColors() {
			return Collections.unmodifiableList(colors);
		}

		public int[] getPercents() {
			return percents;
		}

		public boolean isVertical() {
			return vertical;
		}
	}

	public ImageDesc resolveIconResource(String resource);

	public ImageDesc imageDescriptorFromPlugin(String pluginId,
			String imageFilePath);

	public ColorDesc getColor(String colorDefinition);

	public Gradient<ColorDesc> getGradientColors(String gradientDefinition);

	public ImageDesc imageDescriptorFromURI(URI iconPath);

}