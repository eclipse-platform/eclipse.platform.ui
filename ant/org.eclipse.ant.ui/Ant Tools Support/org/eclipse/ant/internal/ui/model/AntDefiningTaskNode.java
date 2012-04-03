/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.eclipse.ant.core.AntCorePlugin;
import org.eclipse.ant.core.AntCorePreferences;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.ant.internal.core.IAntCoreConstants;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.xml.sax.Attributes;

public class AntDefiningTaskNode extends AntTaskNode {
    private String fIdentifier= null;
    
    public AntDefiningTaskNode(Task task, Attributes attributes) {
        super(task);
        String label= attributes.getValue(IAntCoreConstants.NAME);
        if (label == null) {
            label= task.getTaskName();
        
            String file= attributes.getValue(IAntCoreConstants.FILE);
            if(file != null) {
                label=  label + " " + file; //$NON-NLS-1$
                fIdentifier= file;
            } else {    
                String resource=  attributes.getValue(IAntModelConstants.ATTR_RESOURCE);
                if (resource != null) {
                    label= label + " " + resource; //$NON-NLS-1$
                    fIdentifier= resource;
                } 
            }
        } else {
            fIdentifier= label;
        }
        
        setBaseLabel(label);
    }
	
	protected ImageDescriptor getBaseImageDescriptor() {
		String taskName= getTask().getTaskName();
		if ("taskdef".equalsIgnoreCase(taskName) || "typedef".equalsIgnoreCase(taskName)) {  //$NON-NLS-1$//$NON-NLS-2$
			return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_TASKDEF);	
		}
		return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_MACRODEF);	
	}
	
	/**
	 * Execute the defining task.
	 */
	public boolean configure(boolean validateFully) {
        if (fConfigured) {
            return false;
        }
		if (shouldConfigure()) {
			try {
                ComponentHelper helper= ComponentHelper.getComponentHelper(getProjectNode().getProject());
                ((AntModel) getAntModel()).removeDefinerTasks(getIdentifier(), helper.getAntTypeTable());
                Hashtable old= new Hashtable(helper.getAntTypeTable());
				getTask().maybeConfigure();
				getTask().execute();
                Iterator newNames= helper.getAntTypeTable().keySet().iterator();
                List defined= new ArrayList();
                while (newNames.hasNext()) {
                    String name = (String) newNames.next();
                    if (old.get(name) == null) {
                        defined.add(name);
                    }    
                }
                ((AntModel) getAntModel()).addDefinedTasks(defined, this);
				return false;
			} catch (BuildException be) {
                ((AntModel)getAntModel()).removeDefiningTaskNodeInfo(this);
				handleBuildException(be, AntEditorPreferenceConstants.PROBLEM_CLASSPATH);
			} catch (LinkageError e) {
                //A classpath problem with the definer. Possible causes are having multiple
                //versions of the same JAR on the Ant runtime classpath (either explicitly or via the plugin 
                //classloaders. See bug 71888
                ((AntModel)getAntModel()).removeDefiningTaskNodeInfo(this);
                handleBuildException(new BuildException(AntModelMessages.AntDefiningTaskNode_0), AntEditorPreferenceConstants.PROBLEM_CLASSPATH);
            } catch (AntSecurityException se) {
				//either a system exit or setting of system property was attempted
            	((AntModel)getAntModel()).removeDefiningTaskNodeInfo(this);
                handleBuildException(new BuildException(AntModelMessages.AntDefiningTaskNode_1), AntEditorPreferenceConstants.PROBLEM_SECURITY);
			}
		}
		return false;
	}
	
	public Object getRealTask() {
		Task task= getTask();
		if (task instanceof UnknownElement) {
			task.maybeConfigure();
			return ((UnknownElement)task).getRealThing();
		}
		return task;
	}
    
    protected String getIdentifier() {
        return fIdentifier;
    }
	
	/*
	 * Sets the Java class path in org.apache.tools.ant.types.Path
	 * so that the classloaders defined by these "definer" tasks will have the 
	 * correct classpath.
	 */
	public static void setJavaClassPath() {
		
		AntCorePreferences prefs= AntCorePlugin.getPlugin().getPreferences();
		URL[] antClasspath= prefs.getURLs();
		
		setJavaClassPath(antClasspath);
	}
	
	/*
	 * Sets the Java class path in org.apache.tools.ant.types.Path
	 * so that the classloaders defined by these "definer" tasks will have the 
	 * correct classpath.
	 */
	public static void setJavaClassPath(URL[] antClasspath) {
		
		StringBuffer buff= new StringBuffer();
		File file= null;
		for (int i = 0; i < antClasspath.length; i++) {
			try {
				file = new File(FileLocator.toFileURL(antClasspath[i]).getPath());
			} catch (IOException e) {
				continue;
			}
			buff.append(file.getAbsolutePath());
			buff.append("; "); //$NON-NLS-1$
		}

		org.apache.tools.ant.types.Path systemClasspath= new org.apache.tools.ant.types.Path(null, buff.substring(0, buff.length() - 2));
		org.apache.tools.ant.types.Path.systemClasspath= systemClasspath;
	}
	
	public boolean collapseProjection() {
		IPreferenceStore store= AntUIPlugin.getDefault().getPreferenceStore();		
		if (store.getBoolean(AntEditorPreferenceConstants.EDITOR_FOLDING_DEFINING)) {
			return true;
		}
		return false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.ant.internal.ui.model.AntElementNode#setLength(int)
     */
    public void setLength(int length) {
        super.setLength(length);
        if (shouldConfigure()) {
            getAntModel().setDefiningTaskNodeText(this);
        }
    }

    private boolean shouldConfigure() {
        IPreferenceStore store= AntUIPlugin.getDefault().getPreferenceStore();
        return store.getBoolean(AntEditorPreferenceConstants.CODEASSIST_USER_DEFINED_TASKS);
    }

    protected void setNeedsToBeConfigured(boolean configure) {
       fConfigured= !configure;
    }
}
