package org.eclipse.core.tests.internal.runtimetests;

import org.eclipse.core.runtime.*;

public class SampleMFB extends Plugin {
		
public SampleMFB (IPluginDescriptor descriptor) {
	super(descriptor);
}

public String getSampleString() {
	return ("A sample string from class sampleMFB");
}
}
