package org.eclipse.update.internal.core;

import java.io.PrintWriter;

/**
 * Called when we want the model to persist as XML
 */

public interface IWritable {
	
	public static final int INDENT = 3;

	void write(int indent, PrintWriter w);


}

