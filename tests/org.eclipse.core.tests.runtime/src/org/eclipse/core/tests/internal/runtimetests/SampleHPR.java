package org.eclipse.core.tests.internal.runtimetests;

import org.eclipse.core.runtime.*;

public class SampleHPR extends Plugin {
		
public SampleHPR (IPluginDescriptor descriptor) {
	super(descriptor);
}

public String getSampleString() {
	return ("A sample string from class sampleHPR");
}
}
