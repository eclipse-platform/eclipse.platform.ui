package org.eclipse.core.tests.internal.runtimetests;

import org.eclipse.core.runtime.*;

public class SampleKPB extends Plugin {
		
public SampleKPB (IPluginDescriptor descriptor) {
	super(descriptor);
}

public String getSampleString() {
	return ("A sample string from class sampleKPB");
}
}
