/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.internal.ui.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;
import org.eclipse.ant.core.AntSecurityException;
import org.eclipse.ant.internal.ui.AntUIImages;
import org.eclipse.ant.internal.ui.IAntUIConstants;
import org.eclipse.ant.internal.ui.preferences.AntEditorPreferenceConstants;
import org.eclipse.jface.resource.ImageDescriptor;


public class AntTaskNode extends AntElementNode {

	private Task fTask= null;
	protected String fBaseLabel= null;
	protected String fLabel;
	private String fId= null;
	protected boolean fConfigured= false;
	
	public AntTaskNode(Task task) {
		super(task.getTaskName());
		fTask= task;
	}
	
	public AntTaskNode(Task task, String label) {
		super(task.getTaskName());
		fTask= task;
		fBaseLabel= label;
	}	
	
	public String getLabel() {
	    if (fLabel == null) {
			StringBuffer label= new StringBuffer();
			if (fBaseLabel != null) {
				label.append(fBaseLabel);
			} else if (fId != null) {
				label.append(fId);
			} else {
				label.append(fTask.getTaskName());
			}
			if (isExternal()) {
				appendEntityName(label);
			}
			fLabel= label.toString();
	    }
	    return fLabel;
	}
	
	public void setBaseLabel(String label) {
		fBaseLabel= label;
	}
	
	public Task getTask() {
		return fTask;
	}
	
	public void setTask(Task task) {
		fTask= task;
	}
	
	protected ImageDescriptor getBaseImageDescriptor() {
		if (fId != null) {
			return AntUIImages.getImageDescriptor(IAntUIConstants.IMG_ANT_TYPE);
		}
		
		return super.getBaseImageDescriptor();
	}

	/**
	 * The reference id for this task
	 * @param id The reference id for this task
	 */
	public void setId(String id) {
		fId= id;
	}
	
	/**
	 * Returns the reference id for this task or <code>null</code>
	 * if it has no reference id.
	 * @return The reference id for this task
	 */
	public String getId() {
		return fId;
	}
	
	/**
	 * Configures the associated task if required.
	 * Allows subclasses to do specific configuration (such as executing the task) by
	 * calling <code>nodeSpecificConfigure</code>
	 * 
	 * @return whether the configuration of this node could have impact on other nodes
	 */
	public boolean configure(boolean validateFully) {
		if (!validateFully || (getParentNode() instanceof AntTaskNode)) {
			return false;
		}
		if (fConfigured) {
			return false;
		}
		int severity= AntModelProblem.getSeverity(AntEditorPreferenceConstants.PROBLEM_TASKS);
		if (severity != AntModelProblem.NO_PROBLEM) {
		    //only configure if the user cares about the problems
			try {
				getTask().maybeConfigure();
				fConfigured= true;
				return true;
			} catch (BuildException be) {
				handleBuildException(be, AntEditorPreferenceConstants.PROBLEM_TASKS);
			} catch (AntSecurityException se) {
				//either a system exit or setting of system property was attempted
				handleBuildException(new BuildException(AntModelMessages.getString("AntTaskNode.0")), AntEditorPreferenceConstants.PROBLEM_SECURITY); //$NON-NLS-1$
			}
		}
		return false;
	}

	protected void handleBuildException(BuildException be, String preferenceKey) {
		int severity= AntModelProblem.getSeverity(preferenceKey);
		if (severity != AntModelProblem.NO_PROBLEM) {
			getAntModel().handleBuildException(be, this, severity);
		}
	}
	
	public boolean containsOccurrence(String identifier) {
		RuntimeConfigurable wrapper= getTask().getRuntimeConfigurableWrapper();
		Map attributeMap= wrapper.getAttributeMap();
		Set keys= attributeMap.keySet();
		String modifiedIdentifier= new StringBuffer("{").append(identifier).append('}').toString(); //$NON-NLS-1$
		for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
			String key = (String) iter.next();
			String value= (String) attributeMap.get(key);
			if (value.indexOf(modifiedIdentifier) != -1) {
				return true;
			}
		}
		StringBuffer text= wrapper.getText();
		if (text.length() > 0) {
			if (text.indexOf(modifiedIdentifier) != -1) {
				return true;
			}
		}
	
		return false;
	}
    
    public List computeIdentifierOffsets(String identifier) {
        String textToSearch= getAntModel().getText(getOffset(), getLength());
        List results= new ArrayList();
        RuntimeConfigurable wrapper= getTask().getRuntimeConfigurableWrapper();
        Map attributeMap= wrapper.getAttributeMap();
        Set keys= attributeMap.keySet();
        String modifiedIdentifier= new StringBuffer("{").append(identifier).append('}').toString(); //$NON-NLS-1$
        for (Iterator iter = keys.iterator(); iter.hasNext(); ) {
            String key = (String) iter.next();
            String value= (String) attributeMap.get(key);
			//the value stored in the attribute map seems to be modified to not contain control charactes
			//new lines, carriage returns and these are replaced with spaces
			//so if the line separator is greater than 1 in length we need to correct for this
			String lineSep= System.getProperty("line.separator"); //$NON-NLS-1$
			
            if (value.indexOf(modifiedIdentifier) != -1) {
                int keyOffset= textToSearch.indexOf(key);
				while (keyOffset > 0 && !Character.isWhitespace(textToSearch.charAt(keyOffset - 1))) {
					keyOffset= textToSearch.indexOf(key, keyOffset + 1);
				}
                int valueOffset= textToSearch.indexOf('"', keyOffset);
				int valueLine= ((AntModel)getAntModel()).getLine(getOffset() + valueOffset);
				
                int valueEndOffset= textToSearch.indexOf('"', valueOffset);
                valueEndOffset= textToSearch.indexOf('"', valueEndOffset);
                int withinValueOffset= value.indexOf(modifiedIdentifier);
                while(withinValueOffset != -1) {
					int resultLine= ((AntModel)getAntModel()).getLine(getOffset() + valueOffset + withinValueOffset);
					int resultOffset= getOffset() + valueOffset + withinValueOffset + 2 + ((resultLine- valueLine) * (lineSep.length() - 1));
                    results.add(new Integer(resultOffset));
                    withinValueOffset= value.indexOf(modifiedIdentifier, withinValueOffset + 1);
                }
            }
        }
        
        String text= wrapper.getText().toString().trim();
        if (text.length() > 0) {
            int offset= textToSearch.indexOf(text.toString());
            offset= textToSearch.indexOf(modifiedIdentifier, offset);
            results.add(new Integer(offset + getOffset() + 1));
        }
        return results;
    }
}
