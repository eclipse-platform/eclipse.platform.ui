package org.eclipse.update.internal.core.connection;

import java.io.IOException;
import java.net.URL;

public class ConnectionFactory {

	
	public static IResponse get(URL url) throws IOException {
		//Request request = null;
		IResponse response = null;
		
		if ("file".equals(url.getProtocol())) { //$NON-NLS-1$
			response = new FileResponse(url);
		} else if (url != null && url.getProtocol().startsWith("http")) { //$NON-NLS-1$
			response = new HttpResponse(url);
		} else {
			response = new OtherResponse(url);
		}

		return response;
	}
}
