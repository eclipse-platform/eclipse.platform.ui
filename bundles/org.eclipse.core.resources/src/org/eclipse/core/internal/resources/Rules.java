/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.resources;
import java.util.HashSet;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
/**
 * Class for calculating scheduling rules for various resource operations.
 */
public class Rules {
	static class DefaultFactory implements IResourceRuleFactory {
		private IWorkspace workspace;
		public DefaultFactory(IWorkspace workspace) {
			this.workspace = workspace;
		}
		public ISchedulingRule buildRule() {
			return workspace.getRoot();
		}
		public ISchedulingRule copyRule(IResource source, IResource destination) {
			//source is not modified, destination is created
			return parent(destination);
		}
		public ISchedulingRule createRule(IResource resource) {
			return parent(resource);
		}
		public ISchedulingRule deleteRule(IResource resource) {
			return parent(resource);
		}
		public ISchedulingRule markerRule(IResource resource) {
			return null;
		}
		public ISchedulingRule modifyRule(IResource resource) {
			return resource;
		}
		public ISchedulingRule moveRule(IResource source, IResource destination) {
			//move needs the parent of both source and destination
			return MultiRule.combine(parent(source), parent(destination));
		}
		private ISchedulingRule parent(IResource resource) {
			switch (resource.getType()) {
				case IResource.ROOT :
				case IResource.PROJECT :
					return resource;
				default :
					return resource.getParent();
			}
		}
		public ISchedulingRule refreshRule(IResource resource) {
			return parent(resource);
		}
		public ISchedulingRule validateEditRule(IResource[] resources) {
			if (resources.length == 0)
				return null;
			//optimize rule for single file
			if (resources.length == 1)
				return resources[0].isReadOnly() ? parent(resources[0]) : null;
			//need a lock on the parents of all read-only files
			HashSet rules = new HashSet();
			for (int i = 0; i < resources.length; i++)
				if (resources[i].isReadOnly())
					rules.add(parent(resources[i]));
			if (rules.isEmpty())
				return null;
			if (rules.size() == 1)
				return (ISchedulingRule)rules.iterator().next();
			ISchedulingRule[] ruleArray = (ISchedulingRule[]) rules.toArray(new ISchedulingRule[rules.size()]);
			return new MultiRule(ruleArray);
		}
	}
	private static IResourceRuleFactory factory = ResourcesPlugin.getWorkspace().getRuleFactory();
	public static ISchedulingRule buildRule() {
		return factory.buildRule();
	}
	public static ISchedulingRule copyRule(IResource source, IResource destination) {
		return factory.copyRule(source, destination);
	}
	public static ISchedulingRule createRule(IResource resource) {
		return factory.createRule(resource);
	}
	public static ISchedulingRule deleteRule(IResource resource) {
		return factory.deleteRule(resource);
	}
	public static ISchedulingRule markerRule(IResource resource) {
		return factory.markerRule(resource);
	}
	public static ISchedulingRule modifyRule(IResource resource) {
		return factory.modifyRule(resource);
	}
	public static ISchedulingRule moveRule(IResource source, IResource destination) {
		return factory.moveRule(source, destination);
	}
	public static ISchedulingRule refreshRule(IResource resource) {
		return factory.refreshRule(resource);
	}
	public static ISchedulingRule validateEditRule(IResource[] resources) {
		return factory.validateEditRule(resources);
	}
	/**
	 * Don't allow instantiation
	 */
	private Rules() {
		// not allowed
	}
}