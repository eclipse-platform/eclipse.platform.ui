package org.eclipse.update.ui.internal.model;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.core.*;
import java.util.*;
import org.eclipse.core.runtime.*;

public class PendingChange extends ModelObject {
	public static final int INSTALL = 0x1;
	public static final int UNINSTALL = 0x2;
	public static final int CONFIGURE = 0x3;
	public static final int UNCONFIGURE = 0x4;
	private int jobType;
	private IFeature feature;
	
	public PendingChange(IFeature feature, int jobType) {
		this.jobType = jobType;
		this.feature = feature;
	}

	public int getJobType() {
		return jobType;
	}
	
	public IFeature getFeature() {
		return feature;
	}
	
	public String toString() {
		return feature.toString();
	}
}