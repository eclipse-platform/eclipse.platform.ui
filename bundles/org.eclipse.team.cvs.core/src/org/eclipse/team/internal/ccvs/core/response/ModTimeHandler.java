package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;
import java.text.ParseException;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.util.FileDateFormat;
import org.eclipse.team.internal.ccvs.core.util.ServerDateFormat;

/**
 * The ModTimeHandler saves the modification time given from the
 * server.<br>
 * The last modification time can be asked by other handlers through
 * pullLastTime(), which nulls the buffer.<br>
 * (The server does not need to send timestamps, then we want to use 
 * the local time rather than the last time send)
 */
class ModTimeHandler extends ResponseHandler {
	
	private String modTime;
	
	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		return "Mod-time";
	}

	/**
	 * @see IResponseHandler#handle(Connection, OutputStream, IManagedFolder)
	 */
	public void handle(
		Connection connection,
		PrintStream messageOutput,
		IManagedFolder mRoot)
		throws CVSException {
		
		String unConverted = connection.readLine();
		
		modTime = convertStamp(unConverted,true);
	}
	
	/**
	 * Returns the last modification-time that it got from the server.
	 * 
	 * @return null, if somebody else pulled the time before
	 */
	public String pullLastTime() {
		String oldTime = modTime;
		modTime = null;
		return oldTime;
	}
	
	/**
	 * Converts Timestamps between: <br>
	 *   the server used format ("18 Oct 2001 20:21:13 -0350")<br>
	 *   the format in the filesystem ("Thu Oct 18 20:21:13 2001")
	 */
	private static String convertStamp(String stamp, boolean toFile) throws CVSException {
		
		long dateInMsec;
		ServerDateFormat serverFormater = new ServerDateFormat();
		FileDateFormat fileFormater = new FileDateFormat();
		
		try {
			if (toFile) {
				dateInMsec = serverFormater.parseMill(stamp);
				return fileFormater.formatMill(dateInMsec);
			} else {
				dateInMsec = fileFormater.parseMill(stamp);
				return serverFormater.formatMill(dateInMsec);
			}
		} catch (ParseException e) {

			throw new CVSException(Policy.bind("ModTimeHandler.invalidFormat", stamp),e);
			
			// if the timestamp is not parseable we have got something of the
			// kind we properbly do not want to parse, so we just return the
			// text it was before
			// return stamp;
		}
	}
}

