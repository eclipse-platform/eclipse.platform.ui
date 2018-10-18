package org.eclipse.urischeme.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.urischeme.IUriSchemeHandler;

public class UriSchemeHandlerSpy implements IUriSchemeHandler {

	public Collection<String> uris = new ArrayList<>();

		@Override
		public void handle(String uri) {
			uris.add(uri);
		}
}