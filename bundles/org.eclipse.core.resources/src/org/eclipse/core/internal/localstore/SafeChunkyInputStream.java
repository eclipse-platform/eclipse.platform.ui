package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
/**
 * @see SafeChunkyOutputStream
 */
public class SafeChunkyInputStream extends InputStream {
	// Implementation notes:
	// 
	// to be written...
	//
	//
	protected InputStream input;
	protected byte[] buffer;
	protected int nextByteToRead;
	protected int beginUnparsedData;
	protected int endUnparsedData; // non inclusive
	protected static final int BUFFER_SIZE = 8192;
	protected static final int BUFFER_SIZE_LOWER_LIMIT = 1024;
public SafeChunkyInputStream(File target) throws IOException {
	this(target.getAbsolutePath());
}
public SafeChunkyInputStream(String filePath) throws IOException {
	this(filePath, BUFFER_SIZE);
}
/**
 * @parameter bufferSize cannot be set under 1024 bytes
 */
public SafeChunkyInputStream(String filePath, int bufferSize) throws IOException {
	input = new BufferedInputStream(new FileInputStream(filePath));
	bufferSize = (bufferSize < BUFFER_SIZE_LOWER_LIMIT) ? BUFFER_SIZE_LOWER_LIMIT : bufferSize;
	buffer = new byte[bufferSize];
	nextByteToRead = 0;
	beginUnparsedData = 0;
	endUnparsedData = 0;
}
/**
 * Fits the index to the unparsed area. It means that if it is exceeding the area,
 * the end of the area becomes the index. Otherwise, the same index is returned.
 */
protected int adjustToUnparsedArea(int index) {
	return unparsedDataContains(index) ? index : decrement(endUnparsedData);
}
public int available() throws IOException {
	if (isEndOfFile())
		return 0;
	return intervalSize(nextByteToRead, beginUnparsedData);
}
public void close() throws IOException {
	input.close();
}
protected boolean compare(byte[] target, int startIndex) {
	for (int i = 0; i < target.length; i++) {
		if (buffer[startIndex] != target[i])
			return false;
		startIndex = increment(startIndex);
	}
	return true;
}
/**
 * Returns the previous possible value for the given index with relation
 * to the array size.
 */
protected int decrement(int index) {
	return (index == 0) ? (buffer.length - 1) : index - 1;
}
protected int decrement(int index, int amount) {
	for (int i = 0; i < amount; i++)
		index = decrement(index);
	return index;
}
/**
 * This method calculates what part of the buffer is available to be filled.
 * Returns false if we could not read any byte from the file (eof).
 */
protected boolean fillBuffer() throws IOException {
	if (isBufferFull())
		growBuffer();
	if ((endUnparsedData - nextByteToRead) < 0)
		return fillBufferMiddle();
	return fillBufferExtremes();
}
/**
 * Returns -1 if we could not read any byte from the file.
 */
protected int fillBuffer(int startPosition, int endPosition) throws IOException {
	int length = (endPosition - startPosition) + 1;
	return input.read(buffer, startPosition, length);
}
protected boolean fillBufferExtremes() throws IOException {
	// if the end pointer is in the last position of the buffer
	if (endUnparsedData == (buffer.length - 1)) {
		int read = input.read();
		if (read == -1)
			return false;
		buffer[endUnparsedData] = (byte) read;
		endUnparsedData = increment(endUnparsedData);
		fillBufferMiddle(); // this is similar to fill the left side
		return true; // we've read at least one byte
	}
	// fill right side
	int capacity = (buffer.length - 2) - endUnparsedData;
	int bytesAdded = fillBuffer(endUnparsedData, buffer.length - 2);
	if (bytesAdded == -1)
		return false;
	endUnparsedData = increment(endUnparsedData, bytesAdded);
	if (bytesAdded == capacity)
		fillBufferExtremes();  // fill the last byte
	return true;
}
protected boolean fillBufferMiddle() throws IOException {
	int start = endUnparsedData;
	int end = decrement(nextByteToRead);
	int bytesAdded = fillBuffer(start, end);
	endUnparsedData = increment(start, bytesAdded + 1);
	return bytesAdded != -1;
}
protected int find(byte[] pattern, int startIndex) throws IOException {
	return find(pattern, startIndex, -1);
}
/**
 * If -1 is used as the endIndex parameter, the end of the file is the limit.
 * @return -1 if eof and did not find the pattern.
 */
protected int find(byte[] pattern, int startIndex, int endIndex) throws IOException {
	int start = adjustToUnparsedArea(startIndex);
	int end = adjustToUnparsedArea(endIndex);
	int pos = find(pattern[0], start, end);
	if (pos == -1 || intervalSize(start, end) < pattern.length)
		return -1;
	if (compare(pattern, pos))
		return pos;
	else
		return find(pattern, pos + 1, endIndex);
}
/**
 * @param endIndex is exclusive
 */
protected int find(byte target, int startIndex, int endIndex) throws IOException {
	while (startIndex != endIndex) {
		if (buffer[startIndex] == target)
			return startIndex;
		startIndex = increment(startIndex);
	}
	return -1;
}
protected void growBuffer() {
	int newSize = buffer.length * 2;
	byte[] result = new byte[newSize];

	// first assign current values and change later if necessary
	int newBeginUnparsedData = beginUnparsedData;
	int newEndUnparsedData = endUnparsedData;
	int newNextByteToRead = nextByteToRead;

	if (endUnparsedData < nextByteToRead) {
		int size = buffer.length - nextByteToRead;
		newNextByteToRead = result.length - size;
		if (beginUnparsedData > endUnparsedData) {
			int diff = buffer.length - beginUnparsedData;
			newBeginUnparsedData = result.length - diff;
		}
		System.arraycopy(buffer, 0, result, 0, endUnparsedData);
		System.arraycopy(buffer, nextByteToRead, result, newNextByteToRead, size);
	} else {
		int size = endUnparsedData - nextByteToRead;
		newNextByteToRead = 0;
		newEndUnparsedData = size;
		newBeginUnparsedData = beginUnparsedData - nextByteToRead;
		System.arraycopy(buffer, nextByteToRead, result, 0, size);
	}

	nextByteToRead = newNextByteToRead;
	beginUnparsedData = newBeginUnparsedData;
	endUnparsedData = newEndUnparsedData;
	buffer = result;
}
/**
 * Returns the next possible value for the given index with relation
 * to the array size.
 */
protected int increment(int index) {
	return (index == (buffer.length - 1)) ? 0 : index + 1;
}
protected int increment(int index, int amount) {
	for (int i = 0; i < amount; i++)
		index = increment(index);
	return index;
}
/**
 * @param begin is inclusive
 * @param end is exclusive
 */
protected int intervalSize(int begin, int end) {
	if (begin <= end)
		return end - begin;
	return (buffer.length - begin) + Math.max(0, (end - 1));
}
protected boolean isBufferFull() {
	return (beginUnparsedData != endUnparsedData) && (intervalSize(endUnparsedData, nextByteToRead) == 1);
}
protected boolean isEndOfFile() throws IOException {
	if (nextByteToRead != beginUnparsedData)
		return false;
	if (intervalSize(beginUnparsedData, endUnparsedData) > 1)
		if (validateChunk())
			return isEndOfFile();
	// if we could not read the next chunk, so it is the end of the file
	return !readNextChunk();
}
public int read() throws IOException {
	if (isEndOfFile())
		return -1;
	int index = nextByteToRead;
	nextByteToRead = increment(nextByteToRead);
	return buffer[index] & 0xFF;
}
/**
 * Returns false if we are in the end of the file and true otherwise.
 */
protected boolean readNextChunk() throws IOException {
	if (!fillBuffer())
		return false; // eof
	return validateChunk();
}
/**
 * indexes[0] = begin
 * indexes[1] = end
 * Check if in the current chunk there is an occurrence of another begin chunk.
 */
protected int[] refineInterval(int[] indexes) throws IOException {
	int begin = indexes[0];
	int end = indexes[1];
	int startIndex = increment(begin, ILocalStoreConstants.CHUNK_DELIMITER_SIZE);
	begin = find(ILocalStoreConstants.BEGIN_CHUNK, startIndex, end);
	if (begin != -1) {
		indexes[0] = begin;
		indexes = refineInterval(indexes);
	}
	return indexes;
}
/**
 * The end parameter is not inclusive.
 *
 * @return number of removed bytes.
 */
protected int removeFromBuffer(int begin, int end) {
	int step = intervalSize(begin, end);
	while (end != endUnparsedData) {
		buffer[begin] = buffer[end];
		begin = increment(begin);
		end = increment(end);
	}
	endUnparsedData = decrement(endUnparsedData, step);
	return step;
}
protected boolean unparsedDataContains(int index) {
	if (beginUnparsedData < endUnparsedData)
		return (index >= beginUnparsedData) && (index < endUnparsedData);
	return ((index >= beginUnparsedData) && (index < buffer.length)) || ((index >=0) && (index < Math.max(1, endUnparsedData)));
}
protected boolean validateChunk() throws IOException {
	int begin = find(ILocalStoreConstants.BEGIN_CHUNK, beginUnparsedData);
	if (begin == -1)
		return false;
	int searchIndex = increment(begin, ILocalStoreConstants.CHUNK_DELIMITER_SIZE);
	int end = find(ILocalStoreConstants.END_CHUNK, searchIndex);
	if (end == -1) {
		if (fillBuffer())
			return validateChunk();
		else
			return false;
	}
	int[] indexes = refineInterval(new int[] {begin, end});
	begin = indexes[0];
	end = indexes[1];
	int removed = removeFromBuffer(beginUnparsedData, increment(begin, ILocalStoreConstants.CHUNK_DELIMITER_SIZE));
	end = decrement(end, removed);
	removeFromBuffer(end, increment(end, ILocalStoreConstants.CHUNK_DELIMITER_SIZE));
	beginUnparsedData = end;
	return true;
}
}
