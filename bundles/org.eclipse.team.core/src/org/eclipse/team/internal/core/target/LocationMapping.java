package org.eclipse.team.internal.core.target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.target.Site;

public class LocationMapping {
	
	private final long SERIAL_ID = 1;
	
	private String type;
	private URL url;
	private IPath  path;
	
	public LocationMapping(Site site, IPath path) {
		this.type = site.getType();
		this.url = site.getURL();
		this.path = path;
	}
	
	public LocationMapping(String type, URL url, IPath path) {
		this.type = type;
		this.url = url;
		this.path = path;
	}
	
	public LocationMapping(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream is = new DataInputStream(bis);
		long id = is.readLong();
		this.type = is.readUTF();
		this.url = new URL(is.readUTF());
		this.path = new Path(is.readUTF());
	}

	
	/**
	 * Gets the type.
	 * @return Returns a String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the locationId.
	 * @return Returns a String
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object other) {
		if(this == other) return true;
		if(! (other instanceof LocationMapping)) return false;
		LocationMapping location = (LocationMapping)other;
		return getType().equals(location.getType()) && 
				getURL().equals(location.getURL()) &&
				getPath().equals(location.getPath());
	}
	/**
	 * Gets the path.
	 * @return Returns a IPath
	 */
	public IPath getPath() {
		return path;
	}


	public byte[] encode() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);
		os.writeLong(SERIAL_ID);
		os.writeUTF(getType());
		os.writeUTF(getURL().toExternalForm());
		os.writeUTF(getPath().toString());
		return bos.toByteArray();
	}
}