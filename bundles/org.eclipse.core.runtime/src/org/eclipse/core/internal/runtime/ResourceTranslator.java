package org.eclipse.core.internal.runtime;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.*;

public class ResourceTranslator {
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$	
	private static ServiceReference localizationServiceReference;
	private static BundleLocalization localizationService;

	public static String getResourceString(Bundle bundle, String value) {
		return getResourceString(bundle, value, null);
	}

	public static String getResourceString(Bundle bundle, String value, ResourceBundle resourceBundle) {
		String s = value.trim();
		if (!s.startsWith(KEY_PREFIX))
			return s;
		if (s.startsWith(KEY_DOUBLE_PREFIX))
			return s.substring(1);

		int ix = s.indexOf(' ');
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (resourceBundle == null) {
			try {
				resourceBundle = getResourceBundle(bundle);
			} catch (MissingResourceException e) {
				// just return the default (dflt)
			}
		}

		if (resourceBundle == null)
			return dflt;

		try {
			return resourceBundle.getString(key.substring(1));
		} catch (MissingResourceException e) {
			//this will avoid requiring a bundle access on the next lookup
			return '%' + dflt; //$NON-NLS-1$
		}
	}

	public static void start() {
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		localizationServiceReference = InternalPlatform.getDefault().getBundleContext().getServiceReference(BundleLocalization.class.getName());
		if (localizationServiceReference == null)
			return;
		localizationService = (BundleLocalization) context.getService(localizationServiceReference);
	}

	public static void stop() {
		if (localizationServiceReference == null)
			return;
		localizationService = null;
		InternalPlatform.getDefault().getBundleContext().ungetService(localizationServiceReference);
		localizationServiceReference = null;
	}

	public static ResourceBundle getResourceBundle(Bundle bundle) {
		return localizationService.getLocalization(bundle, null);
	}
}