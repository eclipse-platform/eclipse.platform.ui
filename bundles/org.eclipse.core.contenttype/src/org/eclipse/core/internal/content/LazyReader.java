/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import java.io.IOException;
import java.io.Reader;

public class LazyReader extends Reader implements ILazySource {
	private int blockCapacity;
	char[][] blocks = {};
	private int bufferSize;
	private Reader in;
	private int mark;
	private int offset;

	public LazyReader(Reader in, int blockCapacity) {
		this.in = in;
		this.blockCapacity = blockCapacity;
	}

	public void close() {
		// we don't close the underlying stream
	}

	private int computeBlockSize(int blockIndex) {
		if (blockIndex < blocks.length - 1)
			return blockCapacity;
		int blockSize = bufferSize % blockCapacity;
		return blockSize == 0 ? blockCapacity : blockSize;
	}

	private int copyFromBuffer(char[] userBuffer, int userOffset, int needed) {
		int copied = 0;
		int current = offset / blockCapacity;
		while ((needed - copied) > 0 && current < blocks.length) {
			int blockSize = computeBlockSize(current);
			int offsetInBlock = offset % blockCapacity;
			int availableInBlock = blockSize - offsetInBlock;
			int toCopy = Math.min(availableInBlock, needed - copied);
			System.arraycopy(blocks[current], offsetInBlock, userBuffer, userOffset + copied, toCopy);
			copied += toCopy;
			current++;
			offset += toCopy;
		}
		return copied;
	}

	private void ensureAvailable(long charsToRead) throws IOException {
		int loadedBlockSize = blockCapacity;
		while (bufferSize < offset + charsToRead && loadedBlockSize == blockCapacity) {
			try {
				loadedBlockSize = loadBlock();
			} catch (IOException ioe) {
				throw new LowLevelIOException(ioe);
			}
			bufferSize += loadedBlockSize;
		}
	}

	// for testing purposes
	protected int getBlockCount() {
		return blocks.length;
	}

	// for testing purposes
	protected int getBufferSize() {
		return bufferSize;
	}

	// for testing purposes
	protected int getMark() {
		return mark;
	}

	// for testing purposes
	protected int getOffset() {
		return offset;
	}

	public boolean isText() {
		return true;
	}

	private int loadBlock() throws IOException {
		// read a block from the underlying stream
		char[] newBlock = new char[blockCapacity];
		int readCount = in.read(newBlock);
		if (readCount == -1)
			return 0;
		// expand blocks array
		char[][] tmpBlocks = new char[blocks.length + 1][];
		System.arraycopy(blocks, 0, tmpBlocks, 0, blocks.length);
		blocks = tmpBlocks;
		blocks[blocks.length - 1] = newBlock;
		return readCount;
	}

	public void mark(int readlimit) {
		mark = offset;
	}

	public boolean markSupported() {
		return true;
	}

	public int read() throws IOException {
		ensureAvailable(1);
		if (bufferSize <= offset)
			return -1;
		char nextChar = blocks[offset / blockCapacity][offset % blockCapacity];
		offset++;
		return nextChar;
	}

	public int read(char[] c) throws IOException {
		return read(c, 0, c.length);
	}

	public int read(char[] c, int off, int len) throws IOException {
		ensureAvailable(len);
		int copied = copyFromBuffer(c, off, len);
		return copied == 0 ? -1 : copied;
	}

	public boolean ready() throws IOException {
		try {
			return (bufferSize - offset) > 0 || in.ready();
		} catch (IOException ioe) {
			throw new LowLevelIOException(ioe);
		}
	}

	public void reset() {
		offset = mark;
	}

	public void rewind() {
		mark = 0;
		offset = 0;
	}

	public long skip(long toSkip) throws IOException {
		if (toSkip <= 0)
			return 0;
		ensureAvailable(toSkip);
		long skipped = Math.min(toSkip, bufferSize - offset);
		offset += skipped;
		return skipped;
	}
}
