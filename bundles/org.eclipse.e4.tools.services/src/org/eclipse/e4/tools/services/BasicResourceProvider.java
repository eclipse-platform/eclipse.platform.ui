package org.eclipse.e4.tools.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
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
	
	public Image getImage(Display display, String key) {
		URL url = FileLocator.find(context.getBundle(),new Path(properties.get(key)),null);
		
		if( url != null ) {
			InputStream stream = null;
			try {
				stream = url.openStream();
				return new Image(display, stream);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if( stream != null ) {
					try {
						stream.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
		// TODO Auto-generated method stub
		return null;
	}
	
	public Font getFont(Display display, String key) {
		return null;
	}

	public Color getColor(Display display, String key) {
		String color = properties.get(key);
		if (color.startsWith("rgb")) {
			String rgb = color.substring(color.indexOf('(')+1, color.indexOf(')'));
			String[] cols = rgb.split(",");
			int r = Integer.parseInt(cols[0].trim());
			int g = Integer.parseInt(cols[1].trim());
			int b = Integer.parseInt(cols[2].trim());
			return new Color(display, new RGB(r,g,b));
		}
		return null;
	}
}