package org.eclipse.core.internal.boot.update;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
 
public interface IUMFactory  {

ComponentDescriptorModel createComponentDescriptor();
ComponentEntryDescriptorModel createComponentEntryDescriptor();
FragmentEntryDescriptorModel createFragmentEntryDescriptor();
IdVersionPairModel createIdVersionPair();
PluginEntryDescriptorModel createPluginEntryDescriptor();
ProductDescriptorModel createProductDescriptor();
UMRegistryModel createUMRegistry();
URLNamePairModel createURLNamePair();
}
