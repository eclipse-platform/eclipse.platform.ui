package org.eclipse.team.internal.ccvs.core.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @version 	1.0
 * @author 	${user}
 */
public class NullOutputStream extends OutputStream {

	public static PrintStream DEFAULT = new PrintStream(new NullOutputStream());
	/**
	 * Constructor for NullOutputStream.
	 */
	public NullOutputStream() {
		super();
	}

	/*
	 * @see OutputStream#write(int)
	 */
	public void write(int arg0) throws IOException {
	}

}
