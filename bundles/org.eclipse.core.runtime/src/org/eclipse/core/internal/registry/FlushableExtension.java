package org.eclipse.core.internal.registry;

import java.lang.ref.SoftReference;
import org.eclipse.core.runtime.IConfigurationElement;

public class FlushableExtension extends Extension {
	public FlushableExtension() {
	}

	public IConfigurationElement[] getConfigurationElements() {
		synchronized (this) {
			if (!fullyLoaded) {
				fullyLoaded = true;
				RegistryCacheReader reader = getRegistry().getCacheReader();
				if (reader != null)
					elements = new SoftReference(reader.loadConfigurationElements(this, subElementsCacheOffset));
			}
			if (elements == null)
				elements = new IConfigurationElement[0];

			if (((SoftReference) elements).get() == null) {
				RegistryCacheReader reader = getRegistry().getCacheReader();
				if (reader != null)
					elements = new SoftReference(reader.loadConfigurationElements(this, subElementsCacheOffset));
			}
		}
		return (IConfigurationElement[]) ((SoftReference) elements).get();
	}

	public void setSubElements(IConfigurationElement[] value) {
		elements = new SoftReference(value);
	}
}