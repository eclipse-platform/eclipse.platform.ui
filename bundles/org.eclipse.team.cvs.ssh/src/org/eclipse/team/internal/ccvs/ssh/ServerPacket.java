/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ssh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class ServerPacket extends Packet {
	private PacketInputStream pis = null;

	private static class PacketInputStream extends FilterInputStream {
		private static int MAX_BUFFER_SIZE = 1024;

		private byte[] buffer = new byte[MAX_BUFFER_SIZE];
		private int bufpos = 0;
		private int buflen = 0;
		private int bufrem = 0;

		private long remaining = 0;
		private Cipher cipher = null;

		private long crc = 0;
		private boolean closed = false;

		public PacketInputStream(InputStream in, long length, Cipher cipher) {
			super(in);

			this.remaining = length;
			this.cipher = cipher;
		}

		public int available() throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed);
			}

			return (int) Math.min(remaining - 4, Integer.MAX_VALUE);
		}

		public void close() throws IOException {
			close(true);
		}
		
		public void close(boolean doCrcCheck) throws IOException {
			if (!closed) {
				try {					
					long toRead = doCrcCheck ? remaining - 4 : remaining;			
					
					try {
						Misc.skipFully(this, toRead);
					} catch(IOException e) {
						// we tried our best, keep going
					}
					
					if(doCrcCheck) {
						if ((int) crc != Misc.readInt(buffer, bufpos)) {
							throw new IOException(CVSSSHMessages.ServerPacket_crc);
						}
					}
				} finally {
					closed = true;
				}
			}
		}
		
		private void fill() throws IOException {
			if (bufrem > 0) {
				System.arraycopy(buffer, bufpos, buffer, 0, bufrem);
			}

			int totalBytesRead = bufrem;
			int read = 0;
			int toRead = (int)Math.min(remaining - totalBytesRead, MAX_BUFFER_SIZE - totalBytesRead);

			while (toRead > 0) {
				read = in.read(buffer, totalBytesRead, toRead);

				if (read == -1) {
					throw new IOException(CVSSSHMessages.stream);
				}

				totalBytesRead += read;
				toRead -= read;
			}

			bufpos = 0;
			
			buflen = (totalBytesRead / 8) * 8;
			bufrem = totalBytesRead - buflen;

			if (cipher != null) {
				cipher.decipher(buffer, 0, buffer, 0, buflen);
			}
			
			crc = Misc.crc32(buffer, 0, buflen == remaining ? buflen - 4 : buflen, crc);
		}

		public int read() throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed);
			}

			if (remaining - 4 == 0) {
				return -1;
			}

			if (bufpos == buflen) {
				fill();
			}

			int b = buffer[bufpos] & 0xff;

			++bufpos;
			--remaining;

			return b;
		}

		public int read(byte b[], int off, int len) throws IOException {
			if (closed) {
				throw new IOException(CVSSSHMessages.closed);
			}

			if (remaining - 4 == 0) {
				return -1;
			}

			if (bufpos == buflen) {
				fill();
			}

			len = Math.min(len, (buflen == remaining + bufpos ? buflen - 4 : buflen) - bufpos);

			System.arraycopy(buffer, bufpos, b, off, len);

			bufpos += len;
			remaining -= len;

			return len;
		}
	}
public ServerPacket(InputStream is, Cipher cipher) throws java.io.IOException {
	packetLength = Misc.readInt(is);
	paddingLength = 8 - (packetLength % 8);
	pis = new PacketInputStream(is, packetLength + paddingLength, cipher);
	Misc.skipFully(pis, paddingLength);
	packetType = (byte) pis.read();
}
public void close(boolean doCrcCheck) throws IOException {
	pis.close(doCrcCheck);
}
public InputStream getInputStream() {
	return pis;
}
}
