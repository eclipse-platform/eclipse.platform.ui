package org.eclipse.core.internal.runtime;

import java.io.*;
import java.net.URL;
import org.eclipse.core.internal.boot.PlatformURLConnection;
import org.eclipse.core.internal.boot.PlatformURLHandler;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;

public class PlatformURLConfigConnection extends PlatformURLConnection {
	private static boolean isRegistered = false;
	public static final String CONFIG = "config"; //$NON-NLS-1$

	/**
	 * @param url
	 */
	public PlatformURLConfigConnection(URL url) {
		super(url);
	}

	protected URL resolve() throws IOException {
		String spec = url.getFile().trim();
		if (spec.startsWith("/")) //$NON-NLS-1$
			spec = spec.substring(1);
		if (!spec.startsWith(CONFIG))
			throw new IOException(NLS.bind(Messages.url_badVariant, url.toString()));
		String path = spec.substring(CONFIG.length() + 1);
		return new URL(Platform.getConfigurationLocation().getURL(), path);
	}

	public static void startup() {
		// register connection type for platform:/config handling
		if (isRegistered)
			return;
		PlatformURLHandler.register(CONFIG, PlatformURLConfigConnection.class);
		isRegistered = true;
	}

	/* (non-Javadoc)
	 * @see java.net.URLConnection#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		//This is not optimal but connection is a private ivar in super.
		URL resolved = getResolvedURL();
		if (resolved != null) {
			String fileString = resolved.getFile();
			if (fileString != null) {
				File file = new File(fileString);
				String parent = file.getParent();
				if (parent != null)
					new File(parent).mkdirs();
				return new FileOutputStream(file);
			}
		}
		return null;
	}
}
