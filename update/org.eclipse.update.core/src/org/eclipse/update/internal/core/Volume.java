package org.eclipse.update.internal.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.File;

import org.eclipse.update.configuration.IVolume;
import org.eclipse.update.core.model.ModelObject;

/**
 * Default implementation of a IVolume
 */
public class Volume extends ModelObject implements IVolume {

	private long size;
	private int type;
	private String label;
	private File root;

	/**
	 * Constructor for Volume.
	 */
	public Volume(File root,String label,int type,long size) {
		super();
		this.root = root;
		this.label = label;
		this.type = type;
		this.size = size;
	}

	/**
	 * @see IVolume#getFreeSpace()
	 */
	public long getFreeSpace() {
		return size;
	}

	/**
	 * @see IVolume#getLabel()
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @see IVolume#getType()
	 */
	public int getType() {
		return type;
	}

	/**
	 * @see IVolume#getFile()
	 */
	public File getFile() {
		return root;
	}

	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class arg0) {
		return null;
	}

}
