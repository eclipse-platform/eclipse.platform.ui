package org.eclipse.core.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.internal.runtime.Assert;
import java.util.*;
import java.io.File;

/** 
 * The standard implementation of the <code>IPath</code> interface.
 * Paths are always maintained in canonicalized form.  That is, parent
 * references (i.e., <code>../../</code>) and duplicate separators are 
 * resolved.  For example,
 * <pre>     new Path("/a/b").append("../foo/bar")</pre>
 * will yield the path
 * <pre>     /a/foo/bar</pre>
 * <p>
 * This class is not intended to be subclassed by clients but
 * may be instantiated.
 * </p>
 * @see IPath
 */
public class Path implements IPath, Cloneable {
	
	/** The path string (never null). */
	private String path = "";

	/** The device id string. May be null if there is no device. */
	private String device = null;

	/** Constant root path string (<code>"/"</code>). */
	private static final String ROOT_STRING = "/";

	/** Constant value containing the root path with no device. */
	public static final Path ROOT = new Path(ROOT_STRING);

	/** Constant empty string value. */
	private static final String EMPTY_STRING = "";

	/** Constant value containing the empty path with no device. */
	public static final Path EMPTY = new Path(EMPTY_STRING);
/** 
 * Constructs a new path from the given string path.
 * The given string path must be valid.
 * The path is canonicalized.
 *
 * @param fullPath the string path
 * @see #isValidPath
 */
public Path(String fullPath) {
	Assert.isNotNull(fullPath);
	path = fullPath.replace(File.separatorChar, SEPARATOR);
	int i = path.indexOf(DEVICE_SEPARATOR);
	if (i != -1) {
		device = path.substring(0, i + 1);
		path = path.substring(i + 1, path.length());
	}
	canonicalize();
}
/** 
 * Constructs a new path from the given device id and string path.
 * The given string path must be valid.
 * The path is canonicalized.
 *
 * @param device the device id
 * @param path the string path
 * @see #isValidPath
 * @see #setDevice
 */
public Path(String device, String path) {
	this(path);
	this.device = device;
}
/* (Intentionally not included in javadoc)
 * @see IPath#addFileExtension
 */
public IPath addFileExtension(String extension) {
	if (isRoot() || isEmpty() || hasTrailingSeparator())
		return this;
	return new Path(device, path + "." + extension);
}
/* (Intentionally not included in javadoc)
 * @see IPath#addTrailingSeparator
 */
public IPath addTrailingSeparator() {
	if (hasTrailingSeparator() || isRoot()) {
		return this;
	}
	Path result = (Path) clone();
	result.setPath(path + SEPARATOR);
	return result;
}
/* (Intentionally not included in javadoc)
 * @see IPath#append(java.lang.String)
 */
public IPath append(String tail) {
	if (tail.length() == 0) {
		return this;
	}
	if (tail.indexOf('.') == -1 && tail.indexOf(SEPARATOR) == -1 && tail.indexOf(DEVICE_SEPARATOR) == -1) {
		Path answer = (Path) clone();
		answer.setPath(isEmpty() || isRoot() || hasTrailingSeparator() ? path.concat(tail) : path + SEPARATOR + tail);
		return answer;
	} else {
		Path tailPath = new Path(tail);
		return append(tailPath);
	}
}
/* (Intentionally not included in javadoc)
 * @see IPath#append(IPath)
 */
public IPath append(IPath tail) {
	if (tail == null || tail.isEmpty() || tail.isRoot()) {
		return this;
	}
	String tailPathPart = ((Path) tail).getPathPart();
	if (tailPathPart.startsWith("/")) {
		tailPathPart = tailPathPart.substring(1);
	}
	String newPath = (isRoot() || isEmpty() || hasTrailingSeparator()) ? path.concat(tailPathPart) : path + SEPARATOR + tailPathPart;
	Path result = (Path) clone();
	result.setPath(newPath);
	if (tailPathPart.startsWith("..") || tailPathPart.startsWith("./")) {
		result.canonicalize();
	}
	return result;
}
/**
 * Destructively converts this path to its canonical form.
 * <p>
 * In its canonical form, a path does not have any
 * "." segments, and parent references ("..") are collapsed
 * where possible.
 * </p>
 * @return the canonicalized path
 */
private void canonicalize() {
	// Test to see if the receiver is already in canonical form.
	if ((path.indexOf('.') != -1)) {
		collapseParentReferences();
	}
}
/* (Intentionally not included in javadoc)
 * Clones this object.
 */
public Object clone() {
	try {
		return super.clone();
	} catch (CloneNotSupportedException e) {
		return null;
	}
}
/**
 * Destructively removes all occurrences of ".." segments from this path.
 */
private void collapseParentReferences() {
	Stack stack = new Stack();
	String[] segments = segments();
	for (int i = 0; i < segments.length; i++) {
		String segment = segments[i];
		if (segment.equals("..")) {
			if (stack.isEmpty()) {
				// if the stack is empty we are going out of our scope 
				// so we need to accumulate segments.  But only if the original
				// path is relative.  If it is absolute then we can't go any higher than
				// root so simply toss the .. references.
				if (!isAbsolute())
					stack.push(segment);
			} else {
				// if the top is '..' then we are accumulating segments so don't pop
				if (stack.peek().equals(".."))
					stack.push("..");
				else
					stack.pop();
			}
			//collapse current references
		} else
			if (!segment.equals(".") || (i == 0 && !isAbsolute()))
				stack.push(segment);
	}
	StringBuffer newPath = new StringBuffer(stack.isEmpty() ? "" : (String) stack.pop());
	while (!stack.isEmpty()) {
		newPath.insert(0, SEPARATOR);
		newPath.insert(0, (String) stack.pop());
	}
	if (isAbsolute()) {
		newPath.insert(0, SEPARATOR);
	}
	if (hasTrailingSeparator()) {
		newPath.append(SEPARATOR);
	}
	path = newPath.toString();
}
/* (Intentionally not included in javadoc)
 * Compares objects for equality.
 */
public boolean equals(Object obj) {
	if (this == obj) {
		return true;
	}
	if (!(obj instanceof IPath)) {
		return false;
	}
	IPath target = (IPath) obj;
	return removeTrailingSeparator().toString().equals(target.removeTrailingSeparator().toString());
}
/* (Intentionally not included in javadoc)
 * @see IPath#getDevice
 */
public String getDevice() {
	return device;
}
/* (Intentionally not included in javadoc)
 * @see IPath#getFileExtension
 */
public String getFileExtension() {
	if (hasTrailingSeparator()) {
		return null;
	}
	String lastSegment = lastSegment();
	if (lastSegment == null) {
		return null;
	}
	int index = lastSegment.lastIndexOf(".");
	if (index == -1) {
		return null;
	}
	return lastSegment.substring(index + 1);
}
/**
 * Returns the string representation of this path, without the device.
 *
 * @return the string path without the device
 */
private String getPathPart() {
	return path;
}
/* (Intentionally not included in javadoc)
 * Computes the hash code for this object.
 */
public int hashCode() {
	return removeTrailingSeparator().toString().hashCode();
}
/* (Intentionally not included in javadoc)
 * @see IPath#hasTrailingSeparator
 */
public boolean hasTrailingSeparator() {
	int len = path.length();
	// len > 1 because ROOT is not considered to have a trailing separator.
	return len > 1 && path.charAt(len-1) == SEPARATOR;
}
/**
 * Returns the index of the first character for the segment
 * with the given index.
 *
 * @param index a 0-based segment index
 * @return a 0-based character index
 */
private int indexOfSegment(int index) {
	int len = path.length();
	int i = 0;
	if (i < len && path.charAt(i) == SEPARATOR) {
		++i;
	}
	int j = index;
	while (i < len && --j >= 0) {
		i = path.indexOf(SEPARATOR, i + 1);
		if (i == -1) {
			return -1;
		}
		++i;
	}
	if (i < len) {
		return i;
	}
	return -1;
}
/* (Intentionally not included in javadoc)
 * @see IPath#isAbsolute
 */
public boolean isAbsolute() {
	return path.length() > 0 && (path.charAt(0) == SEPARATOR);
}
/* (Intentionally not included in javadoc)
 * @see IPath#isEmpty
 */
public boolean isEmpty() {
	return path.length() == 0;
}
/* (Intentionally not included in javadoc)
 * @see IPath#isPrefixOf
 */
public boolean isPrefixOf(IPath anotherPath) {
	Assert.isNotNull(anotherPath);
	if (device == null) {
		if (anotherPath.getDevice() != null) {
			return false;
		}
	} else {
		if (!device.equalsIgnoreCase(anotherPath.getDevice())) {
			return false;
		}
	}
	if (isEmpty() || (isRoot() && anotherPath.isAbsolute())) {
		return true;
	}
	String possiblePrefix = hasTrailingSeparator() ? path.substring(0, path.length() - 1) : path;
	String otherPath = ((Path) anotherPath).getPathPart();
	if (!otherPath.startsWith(possiblePrefix)) {
		return false;
	}
	return otherPath.length() == possiblePrefix.length() || otherPath.charAt(possiblePrefix.length()) == SEPARATOR;
}
/* (Intentionally not included in javadoc)
 * @see IPath#isRoot
 */
public boolean isRoot() {
	return this == ROOT || (path.length() == 1 && path.charAt(0) == SEPARATOR);
}
/* (Intentionally not included in javadoc)
 * @see IPath#isValidPath
 */
public boolean isValidPath(String path) {
	if (path.indexOf("//") >= 0) {
		return false;
	}
	Path test = new Path(path);
	String[] segments = test.segments();
	for (int i = 0; i < segments.length; i++) {
		if (!test.isValidSegment(segments[i])) {
			return false;
		}
	}
	return true;
}
/* (Intentionally not included in javadoc)
 * @see IPath#isValidSegment
 */
public boolean isValidSegment(String segment) {
	int size = segment.length();
	if (size == 0) {
		return false;
	}
	if (Character.isWhitespace(segment.charAt(0)) || Character.isWhitespace(segment.charAt(size - 1))) {
		return false;
	}
	for (int i = 0; i < size; i++) {
		char c = segment.charAt(i);
		if (c == '/' || c == '\\' || c == ':') {
			return false;
		}
	}
	return true;
}
/* (Intentionally not included in javadoc)
 * @see IPath#lastSegment
 */
public String lastSegment() {
	int end = path.length() - 1;
	if (end >= 0 && path.charAt(end) == SEPARATOR) {
		--end;
	}
	if (end < 0) {
		return null;
	}
	int start = path.lastIndexOf(SEPARATOR, end - 1);
	return path.substring((start == -1 ? 0 : start + 1), end + 1);
}
/* (Intentionally not included in javadoc)
 * @see IPath#makeAbsolute
 */
public IPath makeAbsolute() {
	if (isAbsolute()) {
		return this;
	}
	Path result = (Path) clone();
	result.setPath(String.valueOf(SEPARATOR) + path);
	return result;
}
/* (Intentionally not included in javadoc)
 * @see IPath#makeRelative
 */
public IPath makeRelative() {
	if (!isAbsolute()) {
		return this;
	}
	Path result = (Path) clone();
	result.setPath(path.substring(1));
	return result;
}
/* (Intentionally not included in javadoc)
 * @see IPath#matchingFirstSegments
 */
public int matchingFirstSegments(IPath anotherPath) {
	Assert.isNotNull(anotherPath);
	String[] local = segments();
	String[] argument = anotherPath.segments();
	int max = Math.min(local.length, argument.length);
	int count = 0;
	for (int i = 0; i < max; i++) {
		if (!local[i].equals(argument[i])) {
			return count;
		}
		count++;
	}
	return count;
}
/* (Intentionally not included in javadoc)
 * @see IPath#removeFileExtension
 */
public IPath removeFileExtension() {
	String extension = getFileExtension();
	if (extension == null || extension.equals("")) {
		return this;
	}
	String lastSegment = lastSegment();
	int index = lastSegment.lastIndexOf(extension) - 1;
	return removeLastSegments(1).append(lastSegment.substring(0, index));
}
/* (Intentionally not included in javadoc)
 * @see IPath#removeFirstSegments
 */
public IPath removeFirstSegments(int count) {
	if (count == 0) {
		return this;
	}
	Assert.isLegal(count > 0);
	int start = indexOfSegment(count);
	Path result = (Path) clone();
	if (start == -1) {
		result.setPath(EMPTY_STRING);
	} else {
		result.setPath(path.substring(start));
	}
	return result;
}
/* (Intentionally not included in javadoc)
 * @see IPath#removeLastSegments
 */
public IPath removeLastSegments(int count) {
	if (count == 0) {
		return this;
	}
	Assert.isLegal(count > 0);
	int index = hasTrailingSeparator() ? path.length() - 1 : path.length();
	for (int i = 0; i < count; i++) {
		index = path.lastIndexOf(SEPARATOR, index - 1);
		if (index == -1) {
			break;
		}
	}
	Path result = (Path) clone();
	if ((index == -1) || (index == 0)) {
		result.setPath(isAbsolute() ? ROOT_STRING : EMPTY_STRING);
		return result;
	}
	if (hasTrailingSeparator()) {
		result.setPath(path.substring(0, index + 1));
	} else {
		result.setPath(path.substring(0, index));
	}
	return result;
}
/* (Intentionally not included in javadoc)
 * @see IPath#removeTrailingSeparator
 */
public IPath removeTrailingSeparator() {
	if (!hasTrailingSeparator()) {
		return this;
	}
	Path result = (Path) clone();
	result.setPath(path.substring(0, path.length() - 1));
	return result;
}
/* (Intentionally not included in javadoc)
 * @see IPath#segment
 */
public String segment(int index) {
	int start = indexOfSegment(index);
	if (start == -1) {
		return null;
	}
	int end = path.indexOf(SEPARATOR, start + 1);
	return end == -1 ? path.substring(start) : path.substring(start, end);
}
/* (Intentionally not included in javadoc)
 * @see IPath#segmentCount
 */
public int segmentCount() {
	int len = path.length();
	if (len == 0 || (len == 1 && path.charAt(0) == SEPARATOR)) {
		return 0;
	}
	int count = 1;
	int prev = -1;
	int i;
	while ((i = path.indexOf(SEPARATOR, prev + 1)) != -1) {
		if (i != prev + 1 && i != len) {
			++count;
		}
		prev = i;
	}
	if (path.charAt(len - 1) == SEPARATOR) {
		--count;
	}
	return count;
}
/* (Intentionally not included in javadoc)
 * @see IPath#segments
 */
public String[] segments() {
	// performance sensitive --- avoid creating garbage
	int segmentCount = segmentCount();
	String[] segments = new String[segmentCount];
	if (segmentCount == 0) {
		return new String[0];
	}
	int len = path.length();
	// check for initial slash
	int firstPosition = (path.charAt(0) == SEPARATOR) ? 1 : 0;
	int lastPosition = (path.charAt(len - 1) != SEPARATOR) ? len - 1 : len - 2;
	// for non-empty paths, the number of segments is 
	// the number of slashes plus 1, ignoring any leading
	// and trailing slashes
	int next = firstPosition;
	for (int i = 0; i < segmentCount; i++) {
		int start = next;
		int end = path.indexOf(SEPARATOR, next);
		if (end == -1) {
			segments[i] = path.substring(start, lastPosition + 1);
		} else {
			segments[i] = path.substring(start, end);
		}
		next = end + 1;
	}
	return segments;
}
/* (Intentionally not included in javadoc)
 * @see IPath#setDevice
 */
public IPath setDevice(String value) {
	if (value != null) {
		Assert.isTrue(value.indexOf(IPath.DEVICE_SEPARATOR) == (value.length() - 1), "Last character should be the device separator");
	}
	Path result = (Path) clone();
	result.device = value;
	return result;
}
/**
 * Destructively sets the path to the given string.
 *
 * @param value a string path
 */
private void setPath(String value) {
	path = value;
}
/* (Intentionally not included in javadoc)
 * @see IPath#toFile
 */
public File toFile() {
	return new File(toOSString());
}
/* (Intentionally not included in javadoc)
 * @see IPath#toOSString
 */
public String toOSString() {
	String osPath = path.replace(SEPARATOR, File.separatorChar);
	return device == null ? osPath : device.concat(osPath);
}
/* (Intentionally not included in javadoc)
 * @see IPath#toString
 */
public String toString() {
	return device == null ? path : device.concat(path);
}
/* (Intentionally not included in javadoc)
 * @see IPath#uptoSegment
 */
public IPath uptoSegment(int count) {
	if (count == 0) {
		return new Path("");
	}
	String source = hasTrailingSeparator() ? path.substring(0, path.length() - 1) : path;
	int index = isAbsolute() ? 0 : -1;
	for (int i = 0; i < count; i++) {
		index = path.indexOf(SEPARATOR, index + 1);
		if (index == -1) {
			index = source.length();
			break;
		}
	}
	Path result = (Path) clone();
	if ((index == -1) || (index == 0)) {
		result.setPath(isAbsolute() ? ROOT_STRING : EMPTY_STRING);
		return result;
	}
	if (hasTrailingSeparator()) {
		result.setPath(path.substring(0, index + 1));
	} else {
		result.setPath(path.substring(0, index));
	}
	return result;
}
}
