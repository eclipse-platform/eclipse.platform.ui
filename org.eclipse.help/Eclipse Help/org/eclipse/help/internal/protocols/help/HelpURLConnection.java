package org.eclipse.help.internal.protocols.help;

import java.io.*;
import java.net.*;
import java.util.*;
import org.eclipse.help.internal.server.*;
import org.eclipse.help.internal.util.*;

public class HelpURLConnection extends URLConnection {
	private HelpURL helpURL = null;

	/**
	 * Constructor for HelpURLConnection
	 */
	public HelpURLConnection(URL url) {
		super(url);
		helpURL = HelpURLFactory.createHelpURL(url.getFile());
		setDefaultUseCaches(helpURL.isCacheable());
		if (Logger.DEBUG)
			Logger.logDebugMessage("HelpURLConnection", "url= " + url);
	}

	/**
	 * @see URLConnection#connect()
	 */
	public void connect() throws IOException {
		return;
	}

	public InputStream getInputStream() throws IOException {
		// must override parent implementation, since it does nothing.
		return helpURL.openStream();
	}

	public String getContentType()
	{
		return helpURL.getContentType();
	}
	
	public long getExpiration()
	{
		Date now = new Date();
		return now.getTime() + 10000;
	}
}