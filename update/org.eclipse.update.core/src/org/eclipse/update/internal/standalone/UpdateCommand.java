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
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.update.configuration.IConfiguredSite;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.operations.*;
import org.eclipse.update.search.*;

public class UpdateCommand extends ScriptedCommand {

	private IConfiguredSite targetSite;
	private UpdateSearchRequest searchRequest;
	private UpdateSearchResultCollector collector;
	private String featureId;
	private IFeature currentFeature;

	public UpdateCommand(
		String featureId,
		String verifyOnly) throws Exception {
		
		super(verifyOnly);
		
		try {
			this.featureId = featureId;
			if (featureId != null) {
				this.targetSite = UpdateUtils.getSiteWithFeature(config, featureId);
				if (targetSite == null) {
					System.out.println("Cannot find configured site for " + featureId);
					throw new Exception();
				}
				IFeature[] currentFeatures = UpdateUtils.searchSite(featureId, targetSite, true);
				if (currentFeatures == null || currentFeatures.length == 0) {
					System.out.println("Cannot find configured feature " + featureId);
					throw new Exception();
				}
				this.currentFeature = currentFeatures[0];
			} else {
				// Get site to install to
				IConfiguredSite[] sites = config.getConfiguredSites();
				for (int i = 0; i < sites.length; i++) {
					if (sites[i].isProductSite()) {
						targetSite = sites[i];
						break;
					}
				}
			}
			if (currentFeature == null)
				searchRequest = UpdateUtils.createNewUpdatesRequest(null);
			else
				searchRequest = UpdateUtils.createNewUpdatesRequest(new IFeature[]{currentFeature});
				
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
//			if (feature
//				.getVersionedIdentifier()
//				.getIdentifier()
//				.equals(featureId) ||
//				UpdateUtils.isPatch()) {
		
			IInstallFeatureOperation op =
				OperationsManager.getOperationFactory().createInstallOperation(
					config,
					null,
					feature,
					null,
					null,
					null);

			IConfiguredSite site = UpdateUtils.getDefaultTargetSite(config, op);
			if (site == null) {
				site = UpdateUtils.getAffinitySite(config, feature);
			if (site == null)
				site = targetSite;

			op.setTargetSite(site);
			operations.add(op);
			
//			}
		}
		public IInstallFeatureOperation[] getOperations() {
			IInstallFeatureOperation[] opsArray = new IInstallFeatureOperation[operations.size()];
			operations.toArray(opsArray);
			return opsArray;
		}
	}
}
