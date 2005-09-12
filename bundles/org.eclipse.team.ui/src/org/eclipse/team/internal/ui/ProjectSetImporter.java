/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.TeamPlugin;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ProjectSetImporter {
	
	public static IProject[] importProjectSet(String filename, Shell shell, IProgressMonitor monitor) throws InvocationTargetException {
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(filename), "UTF-8"); //$NON-NLS-1$
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			ProjectSetContentHandler handler = new ProjectSetContentHandler();
			InputSource source = new InputSource(reader);
			parser.parse(source, handler);
			
			Map map = handler.getReferences();
			List newProjects = new ArrayList();
			if ((map == null || map.size() == 0) && handler.isVersionOne()) {
				IProjectSetSerializer serializer = Team.getProjectSetSerializer("versionOneSerializer"); //$NON-NLS-1$
				if (serializer != null) {
					IProject[] projects = serializer.addToWorkspace(new String[0], filename, shell, monitor);
					if (projects != null)
						newProjects.addAll(Arrays.asList(projects));
				}
			} else {
				UIProjectSetSerializationContext context = new UIProjectSetSerializationContext(shell, filename);
				Iterator it = map.keySet().iterator();
				List errors = new ArrayList();
				while (it.hasNext()) {
					try {
                        String id = (String)it.next();
                        List references = (List)map.get(id);
                        RepositoryProviderType providerType = RepositoryProviderType.getProviderType(id);
                        if (providerType == null) {
                            // The provider type is absent. Perhaps there is another provider that can import this type
                            providerType = TeamPlugin.getAliasType(id);
                        }
                        if (providerType == null) {
                            throw new TeamException(new Status(IStatus.ERROR, TeamUIPlugin.ID, 0, NLS.bind(TeamUIMessages.ProjectSetImportWizard_0, new String[] { id }), null)); 
                        }
                    	ProjectSetCapability serializer = providerType.getProjectSetCapability();
                    	ProjectSetCapability.ensureBackwardsCompatible(providerType, serializer);
                    	if (serializer != null) {
                    		IProject[] projects = serializer.addToWorkspace((String[])references.toArray(new String[references.size()]), context, monitor);
                    		if (projects != null)
                    			newProjects.addAll(Arrays.asList(projects));
                    	}
                    } catch (TeamException e) {
                        errors.add(e);
                    }
				}
				if (!errors.isEmpty()) {
				    if (errors.size() == 1) {
				        throw (TeamException)errors.get(0);
				    } else {
				        TeamException[] exceptions = (TeamException[]) errors.toArray(new TeamException[errors.size()]);
				        IStatus[] status = new IStatus[exceptions.length];
				        for (int i = 0; i < exceptions.length; i++) {
                            status[i] = exceptions[i].getStatus();
                        }
				        throw new TeamException(new MultiStatus(TeamUIPlugin.ID, 0, status, TeamUIMessages.ProjectSetImportWizard_1, null)); 
				    }
				}
			}
			return (IProject[]) newProjects.toArray(new IProject[newProjects.size()]);
		} catch (IOException e) {
			throw new InvocationTargetException(e);
		} catch (SAXException e) {
			throw new InvocationTargetException(e);
		} catch (TeamException e) {
			throw new InvocationTargetException(e);
		} catch (ParserConfigurationException e) {
			throw new InvocationTargetException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				}
			}
		}
	}
	
}
