package org.eclipse.core.tests.internal.runtimetests;

import org.eclipse.core.runtime.*;

public class SampleIFR extends Plugin {
		
public SampleIFR (IPluginDescriptor descriptor) {
	super(descriptor);
}

public String getSampleString() {
	return ("A sample string from class sampleIFR");
}
}
