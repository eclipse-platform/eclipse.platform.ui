package org.eclipse.help.internal.server;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.io.*;
import java.util.*;

/**
 * Handles a request
 */
public class HelpContentManager {
	private static final int DATA_READ_SIZE = 8192; // buffer size
	/**
	 * Creates output processors, required
	 * @return vector of OutputProcessor;
	 */
	private static Vector createOutputProcessor(HelpURL url) {
		Vector processors = new Vector(2);
		// only deal with html files
		if (!"text/html".equals(url.getContentType()))
			return processors;

		// search result
		if (url.getValue("resultof") != null)
			processors.add(new HighlightProcessor(url));

		// add the CSS to all html files
		processors.add(new CSSEmbedProcessor());
		
		return processors;
	}
	public static void fillInResponse(
		HelpURL helpURL,
		InputStream inputStream,
		OutputStream out)
		throws IOException {
		Vector outputProcessors = createOutputProcessor(helpURL);
		if ((outputProcessors == null) || (outputProcessors.size() <= 0)) {
			// Read from inputStream and write to out
			transferContent(inputStream, out);
		} else {
			ByteArrayOutputStream tempOut = new ByteArrayOutputStream(DATA_READ_SIZE);
			transferContent(inputStream, tempOut);
			byte[] tempBuffer = tempOut.toByteArray();
			for (int i = 0; i < outputProcessors.size(); i++) {
				tempBuffer =
					((OutputProcessor) outputProcessors.elementAt(i)).processOutput(tempBuffer);
			}
			ByteArrayInputStream tempIn = new ByteArrayInputStream(tempBuffer);
			transferContent(tempIn, out);
		}

	}
	/**
	 * Write the body to the response
	 */
	private static void transferContent(InputStream inputStream, OutputStream out)
		throws IOException {

		// Prepare the input stream for reading
		BufferedInputStream dataStream = new BufferedInputStream(inputStream);

		// Create a fixed sized buffer for reading.
		// We could create one with the size of availabe data...
		byte[] temp = new byte[DATA_READ_SIZE];
		int len = 0;
		while (true) {
			//int    count = 5000; //dataStream.available();
			//if (count == 0) break;
			//byte[] temp = new byte [ count ]; 
			len = dataStream.read(temp); // Read file into the byte array
			if (len == -1)
				break;
			out.write(temp, 0, len);
		}
	}
}
