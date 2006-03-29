package org.eclipse.update.internal.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.eclipse.update.core.Feature;

public class LiteFeature extends Feature {
	
	private boolean fullFeature = true;

	public boolean isFullFeature() {
		return fullFeature;
	}

	public void setFullFeature(boolean fullFeature) {
		this.fullFeature = fullFeature;
	}

}
