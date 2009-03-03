/*******************************************************************************
 * Copyright (c) 2008 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtiltities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

public class ResourceUtility implements
		ISWTResourceUtiltities {
	public static final String PROTOCOL = "bundle-resource://"; //$NON-NLS-1$

	private ImageRegistry IMAGE_REGISTRY = new ImageRegistry();

	private ColorRegistry COLOR_REGISTRY = new ColorRegistry();

	private final PackageAdmin packageAdmin;

	public ResourceUtility(PackageAdmin packageAdmin) {
		this.packageAdmin = packageAdmin;
	}

	public ImageDescriptor resolveIconResource(String resource) {
		Image rv;

		String bundle = resource.substring(PROTOCOL.length(), resource.indexOf(
				"/", PROTOCOL.length())); //$NON-NLS-1$
		String path = resource.substring(bundle.length() + PROTOCOL.length());

		if ((rv = IMAGE_REGISTRY.get(resource)) == null) {
			ImageDescriptor desc = imageDescriptorFromPlugin(bundle, path);
			rv = desc.createImage();
			IMAGE_REGISTRY.put(resource, rv);
		}

		return ImageDescriptor.createFromImage(rv);
	}

	private Bundle getBundle(String bundleName) {
		Bundle[] bundles = packageAdmin.getBundles(bundleName, null);
		if (bundles == null)
			return null;
		// Return the first bundle that is not installed or uninstalled
		for (int i = 0; i < bundles.length; i++) {
			if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
				return bundles[i];
			}
		}
		return null;
	}

	public ImageDescriptor imageDescriptorFromPlugin(String pluginId,
			String imageFilePath) {
		if (pluginId == null || imageFilePath == null) {
			throw new IllegalArgumentException();
		}

		// if the bundle is not ready then there is no image
		Bundle bundle = getBundle(pluginId);
		if ((bundle.getState() & (Bundle.RESOLVED | Bundle.STARTING
				| Bundle.ACTIVE | Bundle.STOPPING)) == 0) {
			return null;
		}

		// look for the image (this will check both the plugin and fragment
		// folders
		URL fullPathString = FileLocator.find(bundle, new Path(imageFilePath),
				null);
		if (fullPathString == null) {
			try {
				fullPathString = new URL(imageFilePath);
			} catch (MalformedURLException e) {
				return null;
			}
		}

		if (fullPathString == null) {
			return null;
		}

		return ImageDescriptor.createFromURL(fullPathString);
	}

	public ColorDescriptor getColor(String colorDefinition) {
		RGB rgb = new RGB(255, 255, 255);

		if (colorDefinition.indexOf("rgb") != -1) { //$NON-NLS-1$
			Pattern p = Pattern.compile("\\d+"); //$NON-NLS-1$
			Matcher m = p.matcher(colorDefinition);
			int valCount = 0;
			while (m.find()) {
				int val = 0;
				try {
					val = Integer.parseInt(m.group());
				} catch (NumberFormatException e) {
				}

				// force into 0-255
				if (val < 0)
					val = 0;
				if (val > 255)
					val = 255;

				if (valCount == 0) {
					rgb.red = val;
				} else if (valCount == 1) {
					rgb.green = val;
				} else {
					rgb.blue = val;
				}

				valCount++;
				if (valCount == 3)
					break;
			}
		}

		ColorDescriptor desc;
		if (!COLOR_REGISTRY.hasValueFor(rgb.toString())) {
			COLOR_REGISTRY.put(rgb.toString(), rgb);
			desc = COLOR_REGISTRY.getColorDescriptor(rgb.toString());
		} else {
			desc = COLOR_REGISTRY.getColorDescriptor(rgb.toString());
		}

		return desc;
	}

	public Gradient<ColorDescriptor> getGradientColors(String gradientDefinition) {
		String[] gradientparts = gradientDefinition.split(";"); //$NON-NLS-1$

		String[] colors = gradientparts[0].split("-"); //$NON-NLS-1$
		List<ColorDescriptor> descs = new ArrayList<ColorDescriptor>(
				colors.length);
		int i = 0;
		for (String color : colors) {
			descs.add(getColor(color));
		}

		Pattern p = Pattern.compile("\\d+"); //$NON-NLS-1$
		Matcher m = p.matcher(gradientparts[1]);

		ArrayList<Integer> vals = new ArrayList<Integer>();
		while (m.find()) {
			int val = 0;
			try {
				val = Integer.parseInt(m.group());
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}

			if (val > 100) {
				val = 100;
			}

			vals.add(val);
		}

		int[] iVals = new int[vals.size()];
		i = 0;
		for (Integer v : vals) {
			iVals[i++] = v;
		}

		return new Gradient(descs, iVals, gradientparts.length == 3
				&& gradientparts[2].indexOf("true") != -1); //$NON-NLS-1$
	}

	public ImageDescriptor imageDescriptorFromURI(URI iconPath) {
		try {
			return ImageDescriptor.createFromURL(new URL(iconPath.toString()));
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
