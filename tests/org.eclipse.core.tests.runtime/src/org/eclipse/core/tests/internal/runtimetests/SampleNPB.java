package org.eclipse.core.tests.internal.runtimetests;

import org.eclipse.core.runtime.*;

public class SampleNPB extends Plugin {
		
public SampleNPB (IPluginDescriptor descriptor) {
	super(descriptor);
}

public String getSampleString() {
	return ("A sample string from class sampleNPB");
}
}
