package org.eclipse.update.internal.core;

import java.io.IOException;

public class FatalIOException extends IOException {
	private static final long serialVersionUID = 7690318087505479039L;

	public FatalIOException(String string) {
		super(string);
	}
}
