package org.eclipse.team.tests.ccvs.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.OutputStream;

/**
 * @version 	1.0
 * @author 	${user}
 */
public class NullOutputStream extends OutputStream {

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
