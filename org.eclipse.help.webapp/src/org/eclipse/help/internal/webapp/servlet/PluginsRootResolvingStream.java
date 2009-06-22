/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.IOException;
import java.io.OutputStream;

public class PluginsRootResolvingStream extends OutputStream {
	
	private OutputStream out;
	
	private int state = INITIAL_STATE;
	private static final int  INITIAL_STATE = 0;
	private static final int  IN_TAG = 1;
	private static final int  IN_QUOTE = 3;  // IN_QUOTE + n means n characters from PLUGIN_ROOT matched
	private static final int  IN_QUOTE_NOT_PLUGINS_ROOT = 2;
	private static final String PLUGINS_ROOT = "PLUGINS_ROOT/"; //$NON-NLS-1$
	private static final int  IN_QUOTE_PLUGINS_ROOT = IN_QUOTE + 13; // 13 is PLUGINS_ROOT.length();

	private String pathPrefix;
	
	private void flushCachedCharacters(int charsMatched) throws IOException {
		out.write(PLUGINS_ROOT.substring(0, charsMatched).getBytes());
	}

	public PluginsRootResolvingStream(OutputStream out, String prefix) {
		this.out = out;
		this.pathPrefix = prefix;
	}

	public void write(int b) throws IOException {
		switch(state) {
	    case INITIAL_STATE: 
	    	if (b == '<') {
	    		state = IN_TAG;
	    	}
	    	out.write(b);
	    	break;
	    case IN_TAG:
	    	if (b == '>') {
	    		state = INITIAL_STATE;
	    	} else if (b == '"') {
	    		state = IN_QUOTE;
	    	}
	    	out.write(b);
	    	break;
	    case IN_QUOTE_NOT_PLUGINS_ROOT:
	    	if (b == '>') {
	    		state = INITIAL_STATE;
	    	} else if (b == '"') {
	    		state = IN_TAG;
	    	}
	    	out.write(b);
	    	break;
	    default:
	    	// In a quote which may start with PLUGINS_ROOT
	    	int charsMatched = state - IN_QUOTE;
	    	if (b == PLUGINS_ROOT.charAt(charsMatched)) {
	    		state++;
	    		if (state == IN_QUOTE_PLUGINS_ROOT) {
	    			out.write(pathPrefix.getBytes());
	    			state = IN_QUOTE_NOT_PLUGINS_ROOT;
	    		}
	    	} else {
	    		// We just discovered that this is not "PLUGINS_ROOT/  
	    		// flush out the characters
	    		state = IN_QUOTE_NOT_PLUGINS_ROOT;
	    		flushCachedCharacters(charsMatched);
	    		out.write(b);
	    	}
		}
	}
	
	public void close() throws IOException {
		if (state > IN_QUOTE && state < IN_QUOTE_PLUGINS_ROOT) {
			flushCachedCharacters(state - IN_QUOTE);
		}
		out.close();
		super.close();
	}	
}