package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.net.URL;

public class UMFactory implements IUMFactory {


public UMFactory() {
	super();

}
public ComponentDescriptorModel createComponentDescriptor() {
	return new ComponentDescriptor();
}
public ComponentEntryDescriptorModel createComponentEntryDescriptor() {
	return new ComponentEntryDescriptor();
}
public FragmentEntryDescriptorModel createFragmentEntryDescriptor() {
	return new FragmentEntryDescriptor();
}
public IdVersionPairModel createIdVersionPair() {
	return new IdVersionPair();
}
public PluginEntryDescriptorModel createPluginEntryDescriptor() {
	return new PluginEntryDescriptor();
}
public ProductDescriptorModel createProductDescriptor() {
	return new ProductDescriptor();
}
public UMRegistryModel createUMRegistry() {
	return new UMRegistry();
}
public URLNamePairModel createURLNamePair() {
	return new URLNamePair();
}
}
