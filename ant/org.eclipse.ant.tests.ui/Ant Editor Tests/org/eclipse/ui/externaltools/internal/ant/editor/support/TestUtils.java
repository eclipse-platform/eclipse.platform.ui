package org.eclipse.ui.externaltools.internal.ant.editor.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;

/**********************************************************************
Copyright (c) 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

public class TestUtils {

	public static String getStreamContentAsString(InputStream inputStream) {
		InputStreamReader reader;
		try {
			reader = new InputStreamReader(inputStream, ResourcesPlugin.getEncoding());
		} catch (UnsupportedEncodingException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return ""; //$NON-NLS-1$
		}
		BufferedReader tempBufferedReader = new BufferedReader(reader);

		return getReaderContentAsString(tempBufferedReader);
	}
	
	protected static String getReaderContentAsString(BufferedReader tempBufferedReader) {
		StringBuffer tempResult = new StringBuffer();
		try {
			String tempLine= tempBufferedReader.readLine();
    
			while(tempLine != null) {
				if(tempResult.length() != 0) {
					tempResult.append("\n"); //$NON-NLS-1$
				}
				tempResult.append(tempLine);
				tempLine = tempBufferedReader.readLine();
			}
		} catch (IOException e) {
			ExternalToolsPlugin.getDefault().log(e);
			return null;
		}

		return tempResult.toString();
	}
}
