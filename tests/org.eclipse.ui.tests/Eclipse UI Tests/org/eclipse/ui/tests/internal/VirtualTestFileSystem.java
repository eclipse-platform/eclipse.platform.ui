package org.eclipse.ui.tests.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.IPath;

/**
 * Based on org.eclipse.debug.tests.launching.DebugFileSystem
 */
public class VirtualTestFileSystem extends FileSystem {

	public static final String SCHEME = "virtual-test";

	/**
	 * represents a directory
	 */
	public static final byte[] DIRECTORY_BYTES = new byte[] { 1, 2, 3, 4 };

	private static VirtualTestFileSystem system;

	/**
	 * Keys URIs to file stores for existing files
	 */
	private final Map<URI, byte[]> files = new HashMap<>();

	public VirtualTestFileSystem() {
		system = this;
		// create root of the file system
		try {
			setContents(new URI(SCHEME, IPath.ROOT.toString(), null), DIRECTORY_BYTES); // $NON-NLS-1$
		} catch (URISyntaxException e) {
		}
	}

	/**
	 * Returns the Debug files system.
	 *
	 * @return file system
	 */
	public static VirtualTestFileSystem getDefault() {
		return system;
	}

	@Override
	public IFileStore getStore(URI uri) {
		return new VirtualTestFileStore(uri);
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	/**
	 * Returns whether contents of the file or <code>null</code> if none.
	 *
	 * @param uri
	 * @return bytes or <code>null</code>
	 */
	public byte[] getContents(URI uri) {
		return files.get(uri);
	}

	/**
	 * Deletes the file.
	 *
	 * @param uri
	 */
	public void delete(URI uri) {
		files.remove(uri);
	}

	/**
	 * Sets the content of the given file.
	 *
	 * @param uri
	 * @param bytes
	 */
	public void setContents(URI uri, byte[] bytes) {
		files.put(uri, bytes);
	}

	/**
	 * Returns URIs of all existing files.
	 *
	 * @return
	 */
	public URI[] getFileURIs() {
		return files.keySet().toArray(new URI[files.size()]);
	}

}
