package org.eclipse.update.ui.model;

import org.eclipse.update.core.*;
import java.util.*;

public class ChecklistJob {
	public static final int INSTALL = 0x1;
	public static final int UNINSTALL = 0x2;
	private int jobType;
	private IFeature feature;
	
	public ChecklistJob(IFeature feature, int jobType) {
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