/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.standalone;
import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.search.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;

public class UpdateCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private UpdateSearchRequest searchRequest;
	private UpdateSearchResultCollector collector;
	private String featureId;

	public UpdateCommand(
		String featureId,
		String toSite,
		String verifyOnly) {
		
		super(verifyOnly);
		
		try {
			this.featureId = featureId;
			
			// Get site to install to
			IConfiguredSite[] sites = config.getConfiguredSites();
			if (toSite != null) {
				URL toSiteURL = new URL(toSite);
				if (SiteManager.getSite(toSiteURL, null) == null) {
					System.out.println(
						"Cannot find site to install to: " + toSite);
					return;
				}
				targetSite =
					SiteManager
						.getSite(toSiteURL, null)
						.getCurrentConfiguredSite();
			}
			if (targetSite == null) {
				for (int i = 0; i < sites.length; i++) {
					if (sites[i].isProductSite()) {
						targetSite = sites[i];
						break;
					}
				}
			}
			
			UpdateSearchScope searchScope = new UpdateSearchScope();
			searchScope.setUpdateMapURL(UpdateUtils.getUpdateMapURL());
			UpdatesSearchCategory category = new UpdatesSearchCategory();
			searchRequest = new UpdateSearchRequest(category, searchScope);
			searchRequest.addFilter(new EnvironmentFilter());
			collector = new UpdateSearchResultCollector();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 */
	public boolean run() {
		try {
			searchRequest.performSearch(collector, new NullProgressMonitor());
			IInstallFeatureOperation[] operations = collector.getOperations();
			if (operations == null || operations.length == 0) {
				System.out.println("Feature " + featureId + " cannot be updated.");
				return false;
			}
			JobTargetSite[] jobTargetSites = new JobTargetSite[operations.length];
			for (int i=0; i<operations.length; i++) {
				jobTargetSites[i] = new JobTargetSite();
				jobTargetSites[i].job = operations[i];
				jobTargetSites[i].targetSite = operations[i].getTargetSite();
			}
	
			// Check for duplication conflicts
			ArrayList conflicts =
				DuplicateConflictsValidator.computeDuplicateConflicts(
					jobTargetSites,
					config);
			if (conflicts != null) {
				System.out.println("Duplicate conflicts");
				return false;
			}
			
			if (isVerifyOnly())
				return true;
				
			IBatchOperation installOperation = OperationsManager.getOperationFactory().createBatchInstallOperation(operations);
			try {
				installOperation.execute(
					new NullProgressMonitor(),
					this);
				System.out.println("Feature " + featureId + " has successfully been updated");
				return true;
			} catch (Exception e) {
				System.out.println("Cannot update feature " + featureId);
				e.printStackTrace();
				return false;
			} 
		} catch (CoreException ce) {
			IStatus status = ce.getStatus();
			if (status != null
				&& status.getCode() == ISite.SITE_ACCESS_EXCEPTION) {
				// Just show this but do not throw exception
				// because there may be results anyway.
				System.out.println("Connection Error");
			}
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#afterExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean afterExecute(IOperation operation, Object data) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.operations.IOperationListener#beforeExecute(org.eclipse.update.operations.IOperation)
	 */
	public boolean beforeExecute(IOperation operation, Object data) {
		return true;
	}

	class UpdateSearchResultCollector implements IUpdateSearchResultCollector {
		private ArrayList operations = new ArrayList();

		public void accept(IFeature feature) {
			if (feature
				.getVersionedIdentifier()
				.getIdentifier()
				.equals(featureId)) {
				operations.add(
					OperationsManager
						.getOperationFactory()
						.createInstallOperation(
						config,
						targetSite,
						feature,
						null,
						null,
						null));
			}
		}
		public IInstallFeatureOperation[] getOperations() {
			IInstallFeatureOperation[] opsArray = new IInstallFeatureOperation[operations.size()];
			operations.toArray(opsArray);
			return opsArray;
		}
	}
}
