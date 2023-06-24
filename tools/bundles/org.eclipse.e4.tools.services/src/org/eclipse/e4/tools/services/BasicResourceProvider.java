package org.eclipse.e4.tools.services;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;

public abstract class BasicResourceProvider implements IResourceProviderService {
	protected Map<String, String> properties;
	protected BundleContext context;

	public void activate(BundleContext context, Map<String, String> properties) {
		this.properties = properties;
		this.context = context;
	}

	@Override
	public Image getImage(Display display, String key) {
		final URL url = FileLocator.find(context.getBundle(), IPath.fromOSString(properties.get(key)), null);

		if (url != null) {
			try (InputStream stream = url.openStream()) {
				return new Image(display, stream);
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font getFont(Display display, String key) {
		return null;
	}

	@Override
	public Color getColor(Display display, String key) {
		final String color = properties.get(key);
		if (color.startsWith("rgb")) { //$NON-NLS-1$
			final String rgb = color.substring(color.indexOf('(') + 1, color.indexOf(')'));
			final String[] cols = rgb.split(","); //$NON-NLS-1$
			final int r = Integer.parseInt(cols[0].trim());
			final int g = Integer.parseInt(cols[1].trim());
			final int b = Integer.parseInt(cols[2].trim());
			return new Color(display, new RGB(r, g, b));
		}
		return null;
	}
}