package org.eclipse.core.internal.plugins;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */import java.net.MalformedURLException;import java.net.URL;import org.eclipse.core.runtime.model.PluginFragmentModel;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.internal.runtime.PlatformURLFragmentConnection;

public class FragmentDescriptor extends PluginFragmentModel {

	// constants
	static final String FRAGMENT_URL = PlatformURLHandler.PROTOCOL + PlatformURLHandler.PROTOCOL_SEPARATOR + "/" + PlatformURLFragmentConnection.FRAGMENT + "/";

public String toString() {
	return getId() + PluginDescriptor.VERSION_SEPARATOR + getVersion();
}
public URL getInstallURL() {	try {		return new URL(FRAGMENT_URL + toString() + "/");	} catch (MalformedURLException e) {		throw new IllegalStateException(); // unchecked	}}}
