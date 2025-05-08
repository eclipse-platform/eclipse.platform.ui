/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 207344
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 218648
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import static org.eclipse.ui.internal.views.log.LogEntry.GREGORIAN_SDF;
import static org.eclipse.ui.internal.views.log.LogEntry.LOCAL_SDF;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.Date;

/**
 * Group of entries with additional Session data.
 */
public class LogSession extends Group {

	/**
	 * Describes the !SESSION header name
	 *
	 * @since 3.5
	 */
	public static final String SESSION = "!SESSION"; //$NON-NLS-1$

	private String sessionData;
	private Date date;
	private String fDateString;

	public LogSession() {
		super(Messages.LogViewLabelProvider_Session);
	}

	public Date getDate() {
		return date;
	}

	/**
	 * Returns a pretty-print formatting for the date for this entry
	 * 
	 * @return the formatted date for this entry
	 */
	public String getFormattedDate() {
		if (fDateString == null) {
			Date tmpdate = getDate();
			if (tmpdate != null) {
				fDateString = LOCAL_SDF.format(tmpdate.toInstant());
			}
		}
		return fDateString;
	}

	public void setDate(String dateString) {
		try {
			Date parsed = Date.from(Instant.from(GREGORIAN_SDF.parse(dateString)));
			if (parsed != null) {
				this.date = parsed;
				fDateString = LOCAL_SDF.format(parsed.toInstant());
			}
		} catch (Exception e) {
			// do nothing
		}
	}

	public String getSessionData() {
		return sessionData;
	}

	void setSessionData(String data) {
		this.sessionData = data;
	}

	public void processLogLine(String line) {
		// process "!SESSION <dateUnknownFormat> ----------------------------"
		if (line.startsWith(SESSION)) {
			line = line.substring(SESSION.length()).trim(); // strip "!SESSION "
			int delim = line.indexOf("----"); //$NON-NLS-1$ // single "-" may be in date, so take few for sure
			if (delim == -1) {
				return;
			}
			String dateBuffer = line.substring(0, delim).trim();
			setDate(dateBuffer);
		}
	}

	@Override
	public void write(PrintWriter writer) {
		writer.write(sessionData);
		writer.println();
		super.write(writer);
	}
}
