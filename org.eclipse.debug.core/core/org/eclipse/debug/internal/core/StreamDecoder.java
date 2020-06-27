/*******************************************************************************
 * Copyright (c) 2017 Andreas Loth and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andreas Loth - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.core;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * Wraps CharsetDecoder to decode a byte stream statefully to characters.
 *
 * @since 3.7 org.eclipse.ui.console
 */
public class StreamDecoder {
	// For more context see https://bugs.eclipse.org/bugs/show_bug.cgi?id=507664

	static private final int BUFFER_SIZE = 4096;

	private final CharsetDecoder decoder;
	private final ByteBuffer inputBuffer;
	private final CharBuffer outputBuffer;
	private boolean finished;

	public StreamDecoder(Charset charset) {
		this.decoder = charset.newDecoder();
		this.decoder.onMalformedInput(CodingErrorAction.REPLACE);
		this.decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
		this.inputBuffer = ByteBuffer.allocate(StreamDecoder.BUFFER_SIZE);
		this.inputBuffer.flip();
		this.outputBuffer = CharBuffer.allocate(StreamDecoder.BUFFER_SIZE);
		this.finished = false;
	}

	private void consume(StringBuilder consumer) {
		this.outputBuffer.flip();
		consumer.append(this.outputBuffer);
		this.outputBuffer.clear();
	}

	private void internalDecode(StringBuilder consumer, byte[] buffer, int offset, int length) {
		assert (offset >= 0);
		assert (length >= 0);
		int position = offset;
		int end = offset + length;
		assert (end <= buffer.length);
		boolean finishedReading = false;
		do {
			CoderResult result = this.decoder.decode(this.inputBuffer, this.outputBuffer, false);
			if (result.isOverflow()) {
				this.consume(consumer);
			} else if (result.isUnderflow()) {
				this.inputBuffer.compact();
				int remaining = this.inputBuffer.remaining();
				assert (remaining > 0);
				int read = Math.min(remaining, end - position);
				if (read > 0) {
					this.inputBuffer.put(buffer, position, read);
					position += read;
				} else {
					finishedReading = true;
				}
				this.inputBuffer.flip();
			} else {
				assert false;
			}
		} while (!finishedReading);
	}

	public void decode(StringBuilder consumer, byte[] buffer, int offset, int length) {
		this.internalDecode(consumer, buffer, offset, length);
		this.consume(consumer);
	}

	public void finish(StringBuilder consumer) {
		if (this.finished) {
			return;
		}
		this.finished = true;
		CoderResult result;
		result = this.decoder.decode(this.inputBuffer, this.outputBuffer, true);
		assert (result.isOverflow() || result.isUnderflow());
		do {
			result = this.decoder.flush(this.outputBuffer);
			if (result.isOverflow()) {
				this.consume(consumer);
			} else {
				assert result.isUnderflow();
			}
		} while (!result.isUnderflow());
		this.consume(consumer);
	}

}
