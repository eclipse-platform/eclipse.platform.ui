package org.eclipse.core.internal.boot.update;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
