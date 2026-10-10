package org.eclipse.ui.examples.urlmodifier;

import java.net.URL;

import org.eclipse.jface.resource.IImageURLModifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Replaces any icon whose file name exists in iconpacks/&lt;pack&gt;/ of this
 * bundle.
 */
@Component
public class IconPackModifier implements IImageURLModifier {

	private Bundle bundle;
	private String pack;

	@Activate
	void activate(BundleContext context) {
		bundle = context.getBundle();
		pack = IconPacks.selected();
	}

	@Override
	public URL modifyURL(URL url) {
		if (pack == null) {
			return null;
		}
		String path = url.getPath();
		String name = path.substring(path.lastIndexOf('/') + 1);
		int dot = name.lastIndexOf('.');
		String baseName = dot < 0 ? name : name.substring(0, dot);
		// null keeps the original icon
		return bundle.getEntry("iconpacks/" + pack + "/" + baseName + ".png");
	}
}
