package org.eclipse.team.internal.core.target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.target.TargetLocation;

public class LocationMapping {
	
	private final long SERIAL_ID = 1;
	
	private String type;
	private String locationId;
	private IPath  path;
	
	public LocationMapping(TargetLocation location, IPath path) {
		this.type = location.getType();
		this.locationId = location.getUniqueIdentifier();
		this.path = path;
	}
	
	public LocationMapping(String type, String locationId, IPath path) {
		this.type = type;
		this.locationId = locationId;
		this.path = path;
	}
	
	public LocationMapping(byte[] bytes) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		DataInputStream is = new DataInputStream(bis);
		long id = is.readLong();
		this.type = is.readUTF();
		this.locationId = is.readUTF();
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
	 * Sets the type.
	 * @param type The type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the locationId.
	 * @return Returns a String
	 */
	public String getLocationId() {
		return locationId;
	}

	/**
	 * Sets the locationId.
	 * @param locationId The locationId to set
	 */
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object other) {
		if(this == other) return true;
		if(! (other instanceof LocationMapping)) return false;
		LocationMapping location = (LocationMapping)other;
		return getType().equals(location.getType()) && 
				getLocationId().equals(location.getLocationId());
	}
	/**
	 * Gets the path.
	 * @return Returns a IPath
	 */
	public IPath getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 * @param path The path to set
	 */
	public void setPath(IPath path) {
		this.path = path;
	}

	public byte[] encode() throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream os = new DataOutputStream(bos);
		os.writeLong(SERIAL_ID);
		os.writeUTF(getType());
		os.writeUTF(getLocationId());
		os.writeUTF(getPath().toString());
		return bos.toByteArray();
	}
}