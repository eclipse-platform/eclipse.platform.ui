package org.eclipse.ui.internal.registry;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.Collator;
import java.util.*;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IPluginPrerequisite;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * Provides access to a list of accelerator configurations, a list
 * of accelerator scopes, and a list of accelerator sets.
 */
public class AcceleratorRegistry {
	//All registered AcceleratorConfiguration(s).
	private List configurations;
	//All registered AcceleratorScope(s).
	private List scopes;
	//All registered AcceleratorSet(s).
	private List sets;
	//All registered actions without definitions.
	private List fakeAccelerators;
	//Maps AcceleratorScope's ids to AcceleratorScope
	private HashMap idToScope;
	/**
	 * Create an instance of AcceleratorRegistry and initializes it.
	 */		
	public AcceleratorRegistry() {
		configurations = new ArrayList();
		scopes = new ArrayList();
		sets = new ArrayList();
		fakeAccelerators = new ArrayList();	
	}
	/**
	 * Adds the given accelerator configuration to the registry.
	 */	
	public boolean addConfiguration(AcceleratorConfiguration a) {
		return configurations.add(a);	
	}
	/**
	 * Returns all registered configurations.
	 */
	public AcceleratorConfiguration[] getConfigurations() {
		AcceleratorConfiguration[] result = new AcceleratorConfiguration[configurations.size()];
		configurations.toArray(result);
		return result;
	}
	/**
	 * Returns all registered configurations.
	 */
	public AcceleratorConfiguration getConfiguration(String id) {
		for (Iterator iterator = configurations.iterator(); iterator.hasNext();) {
			AcceleratorConfiguration element = (AcceleratorConfiguration)iterator.next();
			if(element.getId().equals(id))
				return element;
		}
		return null;
	}	
	/**
	 * Adds the given accelerator scope to the registry.
	 */
	public boolean addScope(AcceleratorScope a) {
		return scopes.add(a);	
	}
	/**
	 * Returns all registered scopes.
	 */
	public AcceleratorScope[] getScopes() {
		AcceleratorScope[] result = new AcceleratorScope[scopes.size()];
		scopes.toArray(result);
		return result;
	}
	/**
	 * Adds the given accelerator set to the registry.
	 */	
	public boolean addSet(AcceleratorSet a) {
		return sets.add(a);
	}
	/**
	 * Finds the set with the specified configuration, scope and plugin Ids and
	 * return it. 
	 * 
	 * @return an AcceleratorSet or null
	 */
	public AcceleratorSet getSet(String configId, String scopeId,String pluginId) {
		for (Iterator iterator = sets.iterator(); iterator.hasNext();) {
			AcceleratorSet set = (AcceleratorSet) iterator.next();
			if(set.getConfigurationId().equals(configId) &&
				set.getScopeId().equals(scopeId) &&
					set.getPluginId().equals(pluginId))
						return set;
		}
		return null;
	}
	/**
	 * Loads the accelerator registry from the platform's
	 * plugin registry.
	 */
	public void load() {
		AcceleratorRegistryReader reader = 
			new AcceleratorRegistryReader();
		reader.read(Platform.getPluginRegistry(), this);
	}
	/**
	 * Queries the given accelerator configuration and scope to find accelerators
	 * which belong to both. Returns a mapping between action definition ids and
	 * accelerator keys representing these accelerators.
	 * 
	 * @param configId the accelerator configuration to be queried 
	 * @param scopeId the accelerator scope to be queried
	 */
	public Accelerator[] getAccelerators(String configId, String scopeId) {
		List accelarators = new ArrayList();
		List matchingList = new ArrayList();
		if(scopeId.equals(IWorkbenchConstants.DEFAULT_ACCELERATOR_SCOPE_ID))
			accelarators.addAll(getFakeAccelerators());	
		for(int i=0;i<sets.size();i++) {
			AcceleratorSet set = (AcceleratorSet)(sets.get(i));
			String setConfigId = set.getConfigurationId();
			String setScopeId = set.getScopeId();
			if(configId.equals(setConfigId) && setScopeId.equals(setScopeId)) {
				matchingList.add(set);
			}
		}
		AcceleratorSet matchingSets[] = new AcceleratorSet[matchingList.size()];
		matchingList.toArray(matchingSets);
		sortSets(matchingSets);
		for (int i = 0; i < matchingSets.length; i++) {
			accelarators.addAll(Arrays.asList(matchingSets[i].getAccelerators()));
		}
		Accelerator[] result = new Accelerator[accelarators.size()];
		accelarators.toArray(result);
		return result;
	}
	/*
	 * Sort the AcceleratorSets according to the plugin they are defined in.
	 * If plugin A requeres B. A comes first.
	 * If plugin A does not requere B they are sorted by id.
	 */ 
	private void sortSets(AcceleratorSet matchingSets[]) {
		final IPluginRegistry registry = Platform.getPluginRegistry();
		Arrays.sort(matchingSets,new Comparator() {
			public int compare(Object o1, Object o2) {
				String plug1 = ((AcceleratorSet)o1).getPluginId();
				String plug2 = ((AcceleratorSet)o2).getPluginId();
				IPluginDescriptor desc1 = registry.getPluginDescriptor(plug1);
				IPluginDescriptor desc2 = registry.getPluginDescriptor(plug2);
				if(dependsOn(registry,desc1,desc2))
					return 1;
				else if(dependsOn(registry,desc1,desc2))
					return -1;	
				return plug2.compareTo(plug1);
			}
		});
	}
	private boolean dependsOn(IPluginRegistry registry,IPluginDescriptor descriptor0, IPluginDescriptor descriptor1) {
		IPluginPrerequisite[] prerequisites= descriptor0.getPluginPrerequisites();
		for (int i= 0; i < prerequisites.length; i++) {
			IPluginPrerequisite prerequisite= prerequisites[i];
			String id= prerequisite.getUniqueIdentifier();			
			IPluginDescriptor descriptor= registry.getPluginDescriptor(id);
			if (descriptor != null && (descriptor.equals(descriptor1) || dependsOn(registry,descriptor, descriptor1)))
				return true;
		}
		
		return false;
	}
	/**
	 * Returns a List with all actions without definition registered
	 * in this registry.
	 */	
	private List getFakeAccelerators() {
		return fakeAccelerators;
	}
	/**
	 * Add a new accelerator to this registry for a action without
	 * an action definiton.
	 */
	public void addFakeAccelerator(String id,int accelerator) {
		fakeAccelerators.add(new Accelerator(id,accelerator));
	}
	/**
	 * Remove all actions without definition from this registry.
	 */
	public void clearFakeAccelerators() {
		fakeAccelerators.clear();
	}
	/**
	 * Returns a list of all the configurations in the registry for which
	 * there are registered accelerator sets.
	 */
	public AcceleratorConfiguration[] getConfigsWithSets() {
		List list = new ArrayList();
		for(int i=0; i<configurations.size(); i++) {
			AcceleratorConfiguration config = (AcceleratorConfiguration)configurations.get(i);
			String configId = config.getId();
			for(int j=0; j<sets.size(); j++) {
				AcceleratorSet set = (AcceleratorSet)sets.get(j);
				if(configId.equals(set.getConfigurationId())) {
					list.add(config);
					break;
				}	
			}
			// temporary hack until some sets are registered with default configuration
			if(configId.equals(IWorkbenchConstants.DEFAULT_ACCELERATOR_CONFIGURATION_ID))
				list.add(config);
		}
		AcceleratorConfiguration result[] = new AcceleratorConfiguration[list.size()];
		list.toArray(result);
		return result;	
	}
	/**
	 * Returns the scope associated to the specified id or null if 
	 * none is found.
	 */
	public AcceleratorScope getScope(String scopeID) {
		if(idToScope == null) {
			idToScope = new HashMap();
			AcceleratorScope scopes[] = getScopes();
			for (int i = 0; i < scopes.length; i++) {
				AcceleratorScope s = scopes[i];
				idToScope.put(s.getId(),s);
			}
		}
		return (AcceleratorScope)idToScope.get(scopeID);
	}
}
