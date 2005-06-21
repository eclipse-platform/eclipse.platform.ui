/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.core;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;

public class HttpResponse implements Response {
	/**
	 * Monitored InputStream.  Upon IOException, discards
	 * connection so it is not resused.
	 *
	 */
	private class MonitoringInputStream extends FilterInputStream {
		InputStream in;

		public MonitoringInputStream(InputStream in) {
			super(in);
			this.in = in;
		}

		public int available() throws IOException {
			try {
				return super.available();
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
		}

		public void close() throws IOException {
			try {
				super.close();
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
		}

		public int read() throws IOException {
			try {
				return super.read();
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
		}

		public synchronized void reset() throws IOException {
			try {
				super.reset();
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
		}

		public int read(byte[] b) throws IOException {
			try {
				return super.read(b);
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
		}

		public int read(byte[] b, int off, int len) throws IOException {
			try {
				return super.read(b, off, len);
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
		}

		public long skip(long n) throws IOException {
			try {
				return super.skip(n);
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
		}

	}
	
	private static final long POLLING_INTERVAL = 200;
//    private static final String ECLIPSE_DOWNLOADS = "http://www.eclipse.org/downloads/download.php?r=1&file=/eclipse";
//    private static final String ECLIPSE_UPDATES="http://update.eclipse.org";
	protected URL url;
//    protected URL originalURL;
	protected InputStream in;
	protected URLConnection connection;
	protected long lastModified;
	protected long offset;

	public HttpResponse(URL url) {
//        this.originalURL = url;
        this.url = url;
//        String urlstring = url.toExternalForm();
//        if (urlstring.startsWith(ECLIPSE_UPDATES))
//            try {
//                this.url = new URL(ECLIPSE_DOWNLOADS + urlstring.substring(ECLIPSE_UPDATES.length()));
//            } catch (MalformedURLException e) {
//               this.url = url;
//            }
	}

	public InputStream getInputStream() throws IOException {
		if (in == null && url != null) {
			if (connection == null || offset > 0)
				connection = url.openConnection();
			if (offset > 0)
				connection.setRequestProperty("Range", "bytes=" + offset + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			try {
				in = new MonitoringInputStream(connection.getInputStream());
			} catch (IOException ioe) {
				connection = null;
				throw ioe;
			}
			checkOffset();
		}
		return in;
	}
	/**
	 * @see Response#getInputStream(IProgressMonitor)
	 */
	public InputStream getInputStream(IProgressMonitor monitor)
		throws IOException, CoreException {
		if (in == null && url != null) {
			if (connection == null || offset > 0)
				connection = url.openConnection();
			if (offset > 0)
				connection.setRequestProperty("Range", "bytes=" + offset + "-"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			if (monitor != null) {
				try {
					this.in = new MonitoringInputStream(openStreamWithCancel(
							connection, monitor));
				} catch (IOException ioe) {
					connection = null;
					throw ioe;
				}
			} else {
				try {
					this.in = new MonitoringInputStream(connection
							.getInputStream());
				} catch (IOException ioe) {
					connection = null;
					throw ioe;
				}
			}
			// this can also be run inside a monitoring thread, but it is safe
			// to
			// just call it now, if the input stream has already been obtained
			checkOffset();
			if (in != null) {
				this.lastModified = connection.getLastModified();
			}
		}
		return in;
	}

	public long getContentLength() {
		if (connection != null)
			return connection.getContentLength();
		return 0;
	}

	public int getStatusCode() {
		if (connection == null)
			try {
				connection = url.openConnection();
			} catch (IOException e) {
			}
		if (connection != null) {
			try {
				return ((HttpURLConnection) connection).getResponseCode();
			} catch (IOException e) {
				UpdateCore.warn("", e); //$NON-NLS-1$
			}
		}
		return IStatusCodes.HTTP_OK;
	}

	public String getStatusMessage() {
		if (connection != null) {
			try {
				return ((HttpURLConnection) connection).getResponseMessage();
			} catch (IOException e) {
				UpdateCore.warn("", e); //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
	}

	public long getLastModified() {
		if (lastModified == 0) {
			if (connection == null)
				try {
					connection = url.openConnection();
				} catch (IOException e) {
				}
			if (connection != null)
				lastModified = connection.getLastModified();
		}
		return lastModified;
	}

	private InputStream openStreamWithCancel(
		URLConnection urlConnection,
		IProgressMonitor monitor)
		throws IOException, CoreException {
		ConnectionThreadManager.StreamRunnable runnable =
			new ConnectionThreadManager.StreamRunnable(urlConnection);
		Thread t =
			UpdateCore.getPlugin().getConnectionManager().createThread(
				runnable);
		t.start();
		InputStream is = null;
		try {
			for (;;) {
				if (monitor.isCanceled()) {
					runnable.disconnect();
                    connection = null;
					break;
				}
				if (runnable.getInputStream() != null) {
					is = runnable.getInputStream();
					break;
				}
				if (runnable.getException() != null) {
					if (runnable.getException() instanceof IOException)
						throw (IOException) runnable.getException();
					else
						throw new CoreException(new Status(IStatus.ERROR,
								UpdateCore.getPlugin().getBundle()
										.getSymbolicName(), IStatus.OK,
								runnable.getException().getMessage(), runnable
										.getException()));
				}
				t.join(POLLING_INTERVAL);
			}
		} catch (InterruptedException e) {
		}
		return is;
	}
	public void setOffset(long offset) {
		this.offset = offset;
	}
	private void checkOffset() throws IOException {
		if (offset == 0)
			return;
		String range = connection.getHeaderField("Content-Range"); //$NON-NLS-1$
		//System.out.println("Content-Range=" + range);
		if (range == null) {
			//System.err.println("Server does not support ranges");
			throw new IOException(Messages.HttpResponse_rangeExpected); 
		} else if (!range.startsWith("bytes " + offset + "-")) { //$NON-NLS-1$ //$NON-NLS-2$
			//System.err.println("Server returned wrong range");
			throw new IOException(Messages.HttpResponse_wrongRange); 
		}
	}
}
