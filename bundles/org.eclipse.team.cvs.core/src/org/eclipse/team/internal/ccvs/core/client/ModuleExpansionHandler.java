package org.eclipse.team.internal.ccvs.core.client;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;

public class ModuleExpansionHandler extends ResponseHandler {

	/*
	 * @see ResponseHandler#getResponseID()
	 */
	public String getResponseID() {
		return "Module-expansion";
	}

	/*
	 * @see ResponseHandler#handle(Session, String, IProgressMonitor)
	 */
	public void handle(Session session, String expansion, IProgressMonitor monitor)
		throws CVSException {
			
		session.addModuleExpansion(expansion);
	}

}
