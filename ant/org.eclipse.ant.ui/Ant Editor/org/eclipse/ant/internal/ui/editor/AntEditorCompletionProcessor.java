/*******************************************************************************
 * Copyright (c) 2002, 2004 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany) and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 * 	   IBM Corporation - bug fixes
 *     John-Mason P. Shackelford (john-mason.shackelford@pearson.com) - bug 49383
 *******************************************************************************/

package org.eclipse.ant.internal.ui.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Available;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.taskdefs.PathConvert;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.taskdefs.UpToDate;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.eclipse.ant.internal.ui.dtd.IAttribute;
import org.eclipse.ant.internal.ui.dtd.IDfm;
import org.eclipse.ant.internal.ui.dtd.IElement;
import org.eclipse.ant.internal.ui.dtd.ISchema;
import org.eclipse.ant.internal.ui.dtd.ParseError;
import org.eclipse.ant.internal.ui.dtd.Parser;
import org.eclipse.ant.internal.ui.editor.model.AntElementNode;
import org.eclipse.ant.internal.ui.editor.model.AntProjectNode;
import org.eclipse.ant.internal.ui.editor.model.AntTargetNode;
import org.eclipse.ant.internal.ui.editor.model.AntTaskNode;
import org.eclipse.ant.internal.ui.editor.outline.AntModel;
import org.eclipse.ant.internal.ui.editor.templates.AntTemplateAccess;
import org.eclipse.ant.internal.ui.editor.templates.XMLContextType;
import org.eclipse.ant.internal.ui.model.AntUIImages;
import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.ant.internal.ui.model.IAntUIConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.ContextType;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The completion processor for the Ant Editor.
 */
public class AntEditorCompletionProcessor  extends TemplateCompletionProcessor implements IContentAssistProcessor  {       
 
 	private Comparator proposalComparator= new Comparator() {
		public int compare(Object o1, Object o2) {
		    
			int type1= getProposalType(o1);
			int type2= getProposalType(o2);
			if (type1 != type2) {
				if (type1 > type2) {
					return 1;
				}  else {
					return -1;
				}
			}
			String string1 = ((ICompletionProposal)o1).getDisplayString();
			String string2 = ((ICompletionProposal)o2).getDisplayString();
			return string1.compareToIgnoreCase(string2);
		}
		private int getProposalType(Object o){
		    if(o instanceof AntCompletionProposal){
		        return ((AntCompletionProposal) o).getType();
		    } else {
		    	return AntCompletionProposal.TASK_PROPOSAL;    
		    }
		}
 	};
	
	private final static int PROPOSAL_MODE_NONE = 0;
	private final static int PROPOSAL_MODE_TASK_PROPOSAL = 1;
	private final static int PROPOSAL_MODE_ATTRIBUTE_PROPOSAL = 2;
	private final static int PROPOSAL_MODE_TASK_PROPOSAL_CLOSING = 3;
	private final static int PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL = 4;
	private final static int PROPOSAL_MODE_PROPERTY_PROPOSAL = 5;
	
	private final static ICompletionProposal[] NO_PROPOSALS= new ICompletionProposal[0];
	
    /**
     * The line where the cursor sits now.
     * <P>
     * The first line has index '1'.
     */
	protected int lineNumber = -1;

    /**
     * The startingColumn where the cursor sits now.
     * <P>
     * The first startingColumn has index '1'.
     */
	protected int columnNumber = -1;
    
	/**
	 * The additional offset required from a required attribute to
	 * place the cursor for the current proposal
	 */
	private int additionalProposalOffset = -1;
    
    private static final String ANT_DTD_FILENAME = "/ant1.6.0.dtd"; //$NON-NLS-1$

    /**
     * The dtd.
     */
	protected static ISchema dtd;

    /**
     * Cursor position, counted from the beginning of the document.
     * <P>
     * The first position has index '0'.
     */
	protected int cursorPosition = -1;
	
    /**
     * The text viewer.
     */
	private ITextViewer viewer;
	
	/**
	 * The set of characters that will trigger the activation of the
	 * completion proposal computation.
	 */
	private char[] autoActivationChars= null;
	
    /**
     * The provider for all task and attribute descriptions.
     */
	private TaskDescriptionProvider descriptionProvider = TaskDescriptionProvider.getDefault();
	
	private String errorMessage;
	
	protected AntModel antModel;
	
	/**
	 * Constructor for AntEditorCompletionProcessor.
	 */
	public AntEditorCompletionProcessor(AntModel model) {
		super();
		if(dtd == null) {
	        try {
	     	  	dtd = parseDtd();
	        } catch (IOException e) {
	        	AntUIPlugin.log(e);
	        } catch (ParseError e) {
				AntUIPlugin.log(e);
			}
		}
		antModel= model;
	}

    /**
     * Parses the dtd.
     */
    private ISchema parseDtd() throws ParseError, IOException {
        InputStream stream = getClass().getResourceAsStream(ANT_DTD_FILENAME);
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8"); //$NON-NLS-1$
        Parser parser = new Parser();
        ISchema schema= parser.parseDTD(reader, "project"); //$NON-NLS-1$
        reader.close();
        return schema;
    }
    
	/**
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
     */
	public ICompletionProposal[] computeCompletionProposals(
            ITextViewer refViewer, int documentOffset) {
        
        this.viewer = refViewer;
        
        return mergeProposals(super.computeCompletionProposals(refViewer,
                documentOffset), determineProposals());
    }
	
	/**
     * @param proposals1
     * @param proposals2
     * @return
     */
    private ICompletionProposal[] mergeProposals(
            ICompletionProposal[] proposals1, ICompletionProposal[] proposals2) {

        ICompletionProposal[] combinedProposals = new ICompletionProposal[proposals1.length
                + proposals2.length];
                
		System.arraycopy(proposals1,0,combinedProposals,0,proposals1.length);
		System.arraycopy(proposals2,0,combinedProposals,proposals1.length,proposals2.length);		                

		Arrays.sort(combinedProposals,proposalComparator);
        return combinedProposals;
    }

    /**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer refViewer, int documentOffset) {
		return new IContextInformation[0];
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
        return autoActivationChars;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
        return null;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
    /**
     * Returns the new determined proposals.
     */ 
	private ICompletionProposal[] determineProposals() {
		
		cursorPosition = ((ITextSelection)viewer.getSelectionProvider().getSelection()).getOffset();
        
        IDocument doc = viewer.getDocument();
        try {
            lineNumber = doc.getLineOfOffset(cursorPosition);
            columnNumber = cursorPosition - doc.getLineOffset(lineNumber);
        } catch (BadLocationException e) {
            AntUIPlugin.log(e);
        }
		
		String prefix = getCurrentPrefix();
		if (prefix == null || cursorPosition == -1) {
			AntUIPlugin.getStandardDisplay().beep();
			return null;
		}
	
		ICompletionProposal[] proposals = getProposalsFromDocument(doc, prefix);
		return proposals;
		
	}

    /**
     * Returns the proposals for the specified document.
     */
    protected ICompletionProposal[] getProposalsFromDocument(IDocument document, String prefix) {
        String taskString = null;
		ICompletionProposal[] proposals= null;
        switch (determineProposalMode(document, cursorPosition, prefix)) {

            case PROPOSAL_MODE_ATTRIBUTE_PROPOSAL:
                taskString = getTaskStringFromDocumentStringToPrefix(document.get().substring(0, cursorPosition-prefix.length()));
                proposals= getAttributeProposals(taskString, prefix);
                if (proposals.length == 0) {
                	errorMessage= AntEditorMessages.getString("AntEditorCompletionProcessor.28"); //$NON-NLS-1$
                }
                break;
            case PROPOSAL_MODE_TASK_PROPOSAL:
				proposals= getTaskProposals(document, getParentName(document, lineNumber, columnNumber), prefix);
            	if (proposals.length == 0) {
				   errorMessage= AntEditorMessages.getString("AntEditorCompletionProcessor.29"); //$NON-NLS-1$
            	}
				break;
            case PROPOSAL_MODE_TASK_PROPOSAL_CLOSING:
                ICompletionProposal proposal= getClosingTaskProposal(getOpenElementName(), prefix, true);
            	if (proposal == null) {
				   errorMessage= AntEditorMessages.getString("AntEditorCompletionProcessor.30"); //$NON-NLS-1$
				   proposals= NO_PROPOSALS;
            	} else {
	            	proposals= new ICompletionProposal[]{proposal};
            	}
                break;
            case PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL:
            	String textToSearch= document.get().substring(0, cursorPosition-prefix.length());
                taskString = getTaskStringFromDocumentStringToPrefix(textToSearch);
                String attributeString = getAttributeStringFromDocumentStringToPrefix(textToSearch);
                if ("target".equalsIgnoreCase(taskString)) { //$NON-NLS-1$
                	proposals= getTargetAttributeValueProposals(document, textToSearch, prefix, attributeString);
                } else if ("refid".equalsIgnoreCase(attributeString)) { //$NON-NLS-1$
                	proposals= getReferencesValueProposals(prefix);

                } else {
                	proposals=getAttributeValueProposals(taskString, attributeString, prefix);
                }
				if (proposals.length == 0) {
				   errorMessage= AntEditorMessages.getString("AntEditorCompletionProcessor.31"); //$NON-NLS-1$
				}
				break;
            case PROPOSAL_MODE_PROPERTY_PROPOSAL:
				proposals= getPropertyProposals(document, prefix, cursorPosition);
            	if (proposals.length == 0) {
				   errorMessage= AntEditorMessages.getString("AntEditorCompletionProcessor.32"); //$NON-NLS-1$
            	}
				break;
			case PROPOSAL_MODE_NONE :
            default :
                proposals= NO_PROPOSALS;
				errorMessage= AntEditorMessages.getString("AntEditorCompletionProcessor.33"); //$NON-NLS-1$
        }
        Arrays.sort(proposals, proposalComparator);
        if (proposals.length > 0) {
        	errorMessage= ""; //$NON-NLS-1$
        }
        return proposals;

    }
    
	private ICompletionProposal[] getReferencesValueProposals(String prefix) {
		Project project= antModel.getProjectNode().getProject();
		Map references= project.getReferences();
		Set refIds= references.keySet();
		if (refIds.isEmpty()) {
			return NO_PROPOSALS;
		}
		AntElementNode node= antModel.getNode(cursorPosition, false);
		while (node.getParentNode() instanceof AntTaskNode) {
			node= node.getParentNode();
		}
		String id= null;
		if (node instanceof AntTaskNode) {
			id= ((AntTaskNode)node).getId();
		}
		List proposals= new ArrayList(refIds.size());
		int i= 0;
		String refId;
		ICompletionProposal proposal;
		for (Iterator iter = refIds.iterator(); iter.hasNext(); i++) {
			refId= (String) iter.next();
			if (!refId.equals(id) && (prefix.length() == 0 || refId.toLowerCase().startsWith(prefix))) {
				proposal= new AntCompletionProposal(refId, cursorPosition - prefix.length(), prefix.length(), refId.length(), null, refId, null, AntCompletionProposal.TASK_PROPOSAL);
				proposals.add(proposal);
			}
		}
		return (ICompletionProposal[])proposals.toArray(new ICompletionProposal[proposals.size()]);      
	}

	protected ICompletionProposal[] getTargetAttributeValueProposals(IDocument document, String textToSearch, String prefix, String attributeName) {
		if (attributeName.equalsIgnoreCase("depends")) { //$NON-NLS-1$
			return getDependsValueProposals(document, prefix);
		} else if (attributeName.equalsIgnoreCase("if") || attributeName.equalsIgnoreCase("unless")) { //$NON-NLS-1$ //$NON-NLS-2$
			if (!textToSearch.trim().endsWith(",")) { //$NON-NLS-1$
				return getPropertyProposals(document, prefix, cursorPosition);
			}
		}
		
		return NO_PROPOSALS;
	}

	private ICompletionProposal[] getDependsValueProposals(IDocument document, String prefix) {
		List possibleDependencies = new ArrayList();
		String currentTargetName= getEnclosingTargetName(document, lineNumber, columnNumber);
		if(currentTargetName == null) {
			return NO_PROPOSALS;
		}
			
		Map targets= getTargets();
		Set targetNames= targets.keySet();
		Iterator itr= targetNames.iterator();
		Enumeration dependencies= null;
		while (itr.hasNext()) {
			String targetName = (String) itr.next();
			if (targetName.equals(currentTargetName)) {
				Target currentTarget= (Target)targets.get(targetName);
				dependencies= currentTarget.getDependencies();
				continue;
			}
			if (targetName.toLowerCase().startsWith(prefix) && targetName.length() > 0){
				possibleDependencies.add(targetName);
			}
		}
		
		if (dependencies != null) {
			while (dependencies.hasMoreElements()) {
				possibleDependencies.remove(dependencies.nextElement());
			}
		}
		
		ICompletionProposal[] proposals= new ICompletionProposal[possibleDependencies.size()];
		int i= 0;
		for (Iterator iter = possibleDependencies.iterator(); iter.hasNext(); i++) {
			String targetName = (String) iter.next();
			ICompletionProposal proposal = new AntCompletionProposal(targetName, cursorPosition - prefix.length(), prefix.length(), targetName.length(), null, targetName, null, AntCompletionProposal.TASK_PROPOSAL);
			proposals[i]= proposal;
		}
		return proposals;
	}

	/**
     * Returns all possible attributes for the specified task.
     * 
     * @param taskName the name of the task for that the attribute shall be 
     * completed
     * @param prefix prefix, that all proposals should start with. The prefix
     * may be an empty string.
     */
    protected ICompletionProposal[] getAttributeProposals(String taskName, String prefix) {
        List proposals = new ArrayList();
        IElement element = dtd.getElement(taskName);
        if (element != null) {
        	Iterator keys = element.getAttributes().keySet().iterator();
        	while (keys.hasNext()) {
        		String attrName = (String) keys.next();
        		if (prefix.length() == 0 || attrName.toLowerCase().startsWith(prefix)) {
        			IAttribute dtdAttributes = (IAttribute) element.getAttributes().get(attrName);
					String replacementString = attrName+"=\"\""; //$NON-NLS-1$
					String displayString = attrName;
					String[] items = dtdAttributes.getEnum();
					if (items != null) {					        			
                        if(items.length > 1) {
                            displayString += " - ("; //$NON-NLS-1$
                        }
                        for (int i = 0; i < items.length; i++) {
                            displayString += items[i];
                            if(i+1 < items.length) {
                                displayString += " | "; //$NON-NLS-1$
                            } else {
                                displayString += ")"; //$NON-NLS-1$
                            }
                        }
                    }
                    
                    addAttributeProposal(taskName, prefix, proposals, attrName, replacementString, displayString);
                }       
            }
        } else { //possibly a user defined task or type
        	Class taskClass= getTaskClass(taskName);
        	if (taskClass != null) {
        		IntrospectionHelper helper= IntrospectionHelper.getHelper(antModel.getProjectNode().getProject(), taskClass);
        		Enumeration attributes= helper.getAttributes();
	        	while (attributes.hasMoreElements()) {
					String attribute = (String) attributes.nextElement();
					if (prefix.length() == 0 || attribute.toLowerCase().startsWith(prefix)) {
						String replacementString = attribute + "=\"\""; //$NON-NLS-1$
						addAttributeProposal(taskName, prefix, proposals, attribute, replacementString, attribute);
					}
				}
        	}
        }
        return (ICompletionProposal[])proposals.toArray(new ICompletionProposal[proposals.size()]);
    }


    private void addAttributeProposal(String taskName, String prefix, List proposals, String attrName, String replacementString, String displayString) {
		String proposalInfo = null;
		String required = descriptionProvider.getRequiredAttributeForTaskAttribute(taskName, attrName);
		if(required != null && required.length() > 0) {
		    proposalInfo = AntEditorMessages.getString("AntEditorCompletionProcessor.Required___4") + required; //$NON-NLS-1$
		    proposalInfo += "<BR><BR>"; //$NON-NLS-1$
		}
		String description = descriptionProvider.getDescriptionForTaskAttribute(taskName, attrName);
		if(description != null) {
		    proposalInfo = (proposalInfo == null ? "" : proposalInfo); //$NON-NLS-1$
		    proposalInfo += description;
		}
		
		ICompletionProposal proposal = new AntCompletionProposal(replacementString, cursorPosition - prefix.length(), prefix.length(), attrName.length()+2, null, displayString, proposalInfo, AntCompletionProposal.TASK_PROPOSAL);
		proposals.add(proposal);
	}

	/**
     * Returns all possible values for the specified attribute of the specified 
     * task.
     * 
     * @param aTaskName the name of the task that the specified attribute 
     * belongs to.
     * 
     * @param anAttributeName the name of the attribute for that the value
     * shall be completed
     * 
     * @param prefix the prefix that all proposals should start with. The prefix
     * may be an empty string.
     */
    private ICompletionProposal[] getAttributeValueProposals(String aTaskName, String anAttributeName, String prefix) {
        List proposals = new ArrayList();
        IElement taskElement = dtd.getElement(aTaskName);
        if (taskElement != null) {
        	IAttribute attribute = (IAttribute) taskElement.getAttributes().get(anAttributeName);
        	if (attribute != null) {
        		String[] items = attribute.getEnum();
        		if (items != null) {
					String item;
                    for (int i = 0; i < items.length; i++) {
                        item= items[i];
                        if(prefix.length() ==0 || item.toLowerCase().startsWith(prefix)) {
                            ICompletionProposal proposal = new AntCompletionProposal(item, cursorPosition - prefix.length(), prefix.length(), item.length(), null, item, null, AntCompletionProposal.TASK_PROPOSAL);
                            proposals.add(proposal);
                        }
                    }
        		}
            }
        }
        return (ICompletionProposal[])proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    
    /**
     * Returns all possible properties for the specified prefix.
     * <P>
     * Note that the completion mode must be property mode, otherwise it is not
     * safe to call this method.
     */
    protected ICompletionProposal[] getPropertyProposals(IDocument document, String prefix, int aCursorPosition) {
        List proposals = new ArrayList();
        Map displayStringToProposals= new HashMap();
        Map properties = findPropertiesFromDocument(document);
		String propertyName;
		Image image = AntUIImages.getImage(IAntUIConstants.IMG_PROPERTY);
		// Determine replacement length and offset
	    // String from beginning to the beginning of the prefix
	   int replacementLength = prefix.length();
	   int replacementOffset = 0;
	   String text= document.get();
	   String stringToPrefix = text.substring(0, aCursorPosition - prefix.length());
	   // Property proposal
	   String lastTwoCharacters = stringToPrefix.substring(stringToPrefix.length()-2, stringToPrefix.length());
	   boolean appendBraces= true;
	   if(lastTwoCharacters.equals("${")) { //$NON-NLS-1$
		   replacementLength += 2;
		   replacementOffset = aCursorPosition - prefix.length() - 2;
	   } else if(lastTwoCharacters.endsWith("$")) { //$NON-NLS-1$
		   replacementLength += 1;
		   replacementOffset = aCursorPosition - prefix.length() - 1;                
	   } else {
			//support for property proposals for the if/unless attributes of targets
	   		replacementOffset= aCursorPosition - prefix.length();
	   		appendBraces= false;
	   }
	   
	   if(text.length() > aCursorPosition && text.charAt(aCursorPosition) == '}') {
		   replacementLength += 1;
	   }
        for(Iterator i=properties.keySet().iterator(); i.hasNext(); ) {
            propertyName= (String)i.next();
            if(prefix.length() == 0 || propertyName.toLowerCase().startsWith(prefix)) {
                String additionalPropertyInfo = (String)properties.get(propertyName);
                
                StringBuffer replacementString = new StringBuffer();
                if (appendBraces) {
                	replacementString.append("${"); //$NON-NLS-1$
                }
                replacementString.append(propertyName);
                if (appendBraces) {
                	replacementString.append('}');
                }
                
				if (displayStringToProposals.get(propertyName) == null) {
                	ICompletionProposal proposal = 
		                new AntCompletionProposal(
		                    replacementString.toString(), replacementOffset, replacementLength, 
		                    replacementString.length(), image, propertyName,
		                    additionalPropertyInfo, AntCompletionProposal.PROPERTY_PROPOSAL);
					proposals.add(proposal);
					displayStringToProposals.put(propertyName, proposal);
				}
            }
        }      
		return (ICompletionProposal[])proposals.toArray(new ICompletionProposal[proposals.size()]);          
    }


    /**
     * Returns all possible proposals for the specified parent name.
     * <P>
     * No completions will be returned if <code>parentName</code> is 
     * not known.
     * 
     * @param document the entire document 
     * @param parentName name of the parent (surrounding) element or 
     * <code>null</code> if completion should be done for the root element.
     * @param prefix the prefix that all proposals should start with. The prefix
     * may be an empty string.
     */
    protected ICompletionProposal[] getTaskProposals(IDocument document, String parentName, String prefix) {
       
        ICompletionProposal proposal;
        if (parentName == null) {
            String rootElementName= "project"; //$NON-NLS-1$
			IElement rootElement = dtd.getElement(rootElementName);
			if (rootElement != null && rootElementName.toLowerCase().startsWith(prefix)) {
				proposal = newCompletionProposal(document, prefix, rootElementName);
				return new ICompletionProposal[] {proposal};
			} else {
				return NO_PROPOSALS;
			}
       } 
       List proposals = new ArrayList(250);
       if (parentName == "project" || parentName == "target") { //$NON-NLS-1$ //$NON-NLS-2$
        	//use the definitions in the project as that includes more than what is defined in the DTD
			Project project= antModel.getProjectNode().getProject();
			Map tasksAndTypes= ComponentHelper.getComponentHelper(project).getAntTypeTable();
			createProposals(document, prefix, proposals, tasksAndTypes);
			if (parentName.equals("project")) { //$NON-NLS-1$
				proposals.add(newCompletionProposal(document, prefix, "target")); //$NON-NLS-1$
			}
		} else {
			IElement parent = dtd.getElement(parentName);
			if (parent != null) {
				IDfm dfm = parent.getDfm();
				String[] accepts = dfm.getAccepts();
				String elementName;
				for (int i = 0; i < accepts.length; i++) {
					elementName = accepts[i];
					if(prefix.length() == 0 || elementName.toLowerCase().startsWith(prefix)) {
						proposal = newCompletionProposal(document, prefix, elementName);
						proposals.add(proposal);
					}
				}
			} else {
				//a nested element of a user defined task/type?
				Class taskClass= getTaskClass(parentName);
	        	if (taskClass != null) {
	        		IntrospectionHelper helper= IntrospectionHelper.getHelper(antModel.getProjectNode().getProject(), taskClass);
	        		Enumeration nested= helper.getNestedElements();
	        		String nestedElement;
		        	while (nested.hasMoreElements()) {
						nestedElement = (String) nested.nextElement();
						if (prefix.length() == 0 || nestedElement.toLowerCase().startsWith(prefix)) {
							proposal = newCompletionProposal(document, prefix, nestedElement);
							proposals.add(proposal);
						}
			        }
	        	}
			}
        }
        
        proposal= getClosingTaskProposal(getOpenElementName(), prefix, false);
        if (proposal != null) {
        	proposals.add(proposal);
        }
        
        // TODO Templates may define something other than tasks / types
        // Here we assume that all templates are for tasks / types. I can't 
        // think of a usecase for templates other than tasks at the moment, but
        // since users can add templates via the preferences we may need to 
        // rethink this.
        // proposals.addAll(getTemplateProposals(document, prefix));
        
        return (ICompletionProposal[])proposals.toArray(new ICompletionProposal[proposals.size()]);
   }

    private void createProposals(IDocument document, String prefix, List proposals, Map tasks) {
		Iterator keys= tasks.keySet().iterator();
		ICompletionProposal proposal;
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (prefix.length() == 0 || key.toLowerCase().startsWith(prefix)) {
				proposal = newCompletionProposal(document, prefix, key);
				proposals.add(proposal);
			}
		}
	}
    
    private ICompletionProposal newCompletionProposal(IDocument document, String aPrefix, String elementName) {
		additionalProposalOffset= 0;
		Image proposalImage = AntUIImages.getImage(IAntUIConstants.IMG_TASK_PROPOSAL);
		String proposalInfo = descriptionProvider.getDescriptionForTask(elementName);
		boolean hasNestedElements= hasNestedElements(elementName);
		String replacementString = getTaskProposalReplacementString(elementName, hasNestedElements);
		int replacementOffset = cursorPosition - aPrefix.length();
		int replacementLength = aPrefix.length();
		if (replacementOffset > 0 && document.get().charAt(replacementOffset - 1) == '<') {
			replacementOffset--;
			replacementLength++;
		}
		int proposalCursorPosition;
		if (hasNestedElements) {
			proposalCursorPosition= elementName.length() + 2 + additionalProposalOffset;
		} else {
			if (additionalProposalOffset > 0) {
				additionalProposalOffset+=2; //<antstructure output="|"/>
			} else {
				additionalProposalOffset+=1; //<arg|/>
			}
			proposalCursorPosition= elementName.length() + additionalProposalOffset;
		}
		return new AntCompletionProposal(replacementString, replacementOffset, 
			replacementLength, proposalCursorPosition, proposalImage, elementName, proposalInfo, AntCompletionProposal.TASK_PROPOSAL);
	}

	/**
     * Returns the one possible completion for the specified unclosed task .
     * 
     * @param openElementName the task that hasn't been closed 
     * last
     * @param prefix The prefix that the one possible proposal should start 
     * with. The prefix may be an empty string.
     * @return the proposal or <code>null</code> if no closing proposal available
     */
    private ICompletionProposal getClosingTaskProposal(String openElementName, String prefix, boolean closingMode) {
    	char previousChar = getPreviousChar();
		ICompletionProposal proposal= null;
        if(openElementName != null) {
            if(prefix.length() == 0 || openElementName.toLowerCase().startsWith(prefix)) {
                StringBuffer replaceString = new StringBuffer();
                if (!closingMode) {
                	if (previousChar != '/') {
	                	if (previousChar != '<') {
	                		replaceString.append('<');
	                	}
	                	replaceString.append('/');
                	}
                }
                replaceString.append(openElementName);
                replaceString.append('>');
                StringBuffer displayString= new StringBuffer("</"); //$NON-NLS-1$
                displayString.append(openElementName);
                displayString.append('>');
                proposal= new AntCompletionProposal(replaceString.toString(), cursorPosition - prefix.length(), prefix.length(), replaceString.length(), null, displayString.toString(), AntEditorMessages.getString("AntEditorCompletionProcessor.39"), AntCompletionProposal.TAG_CLOSING_PROPOSAL); //$NON-NLS-1$
    		}
        }

        return proposal;
    }

	protected char getPreviousChar() {
		ITextSelection selection = (ITextSelection)viewer.getSelectionProvider().getSelection();
    	int offset= selection.getOffset();
    	char previousChar= '?';
    	try {
			previousChar= viewer.getDocument().getChar(offset-1);
		} catch (BadLocationException e) {
			
		}
		return previousChar;
	}

    /**
     * Returns the replacement string for the specified task name.
     */
    private String getTaskProposalReplacementString(String aTaskName, boolean hasNested) {
        StringBuffer replacement = new StringBuffer("<"); //$NON-NLS-1$
        replacement.append(aTaskName); 
        Node attributeNode= descriptionProvider.getAttributesNode(aTaskName);
		
        if (attributeNode != null) {
			appendRequiredAttributes(replacement, attributeNode);
        } else if ("project".equals(aTaskName)){ //$NON-NLS-1$
        	replacement.append(" default=\"\""); //$NON-NLS-1$
			additionalProposalOffset= 9;
        }
        
        if (hasNested) {
        	replacement.append("></"); //$NON-NLS-1$
            replacement.append(aTaskName);
            replacement.append('>');
        } else {
        	replacement.append("/>"); //$NON-NLS-1$
        }
        return replacement.toString();               
    }

    private void appendRequiredAttributes(StringBuffer replacement, Node attributeNode) {
		boolean requiredAdded= false;
		NodeList attributes= attributeNode.getChildNodes();
		String required;
		Node attribute;
		for (int i = 0; i < attributes.getLength(); i++) {
			attribute = attributes.item(i);
			required= descriptionProvider.getRequiredOfNode(attribute);
			if (required.equalsIgnoreCase("yes")) { //$NON-NLS-1$
				String attributeName= descriptionProvider.getTaskAttributeName(attribute);
				replacement.append(' ');
				replacement.append(attributeName);
				replacement.append("=\"\""); //$NON-NLS-1$
				if (!requiredAdded){
					additionalProposalOffset= attributeName.length() + 2;
					requiredAdded= true;
				}	
			}
		}
	}

	/**
     * Returns whether the named element supports nested elements.
     */
    private boolean hasNestedElements(String elementName) {
        IElement element = dtd.getElement(elementName);
        if (element != null) {
        	return !element.isEmpty();
        } else {
        	Class taskClass= getTaskClass(elementName);
        	if (taskClass != null) {
        		IntrospectionHelper helper= IntrospectionHelper.getHelper(antModel.getProjectNode().getProject(), taskClass);
        		Enumeration nested= helper.getNestedElements();
        		return nested.hasMoreElements();
    		}
        }
        return false;
    }
    
    /**
     * Finds a direct child element with <code>aChildElementName</code> of 
     * <code>anElement</code>.
     * <P>
     * The child will not be searched for in the whole hierarchy but only in
     * the hierarchy step below.
     * 
     * @return the found child or <code>null</code> if not found.
     */
    protected Element findChildElementNamedOf(Element anElement, String aChildElementName) {
        NodeList nodeList = anElement.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                if(childNode.getNodeName().equals(aChildElementName)) {
                    return (Element)childNode;
                }
            }   
        }
        return null;
    }

	/**
     * Determines the current prefix, that should be used for completion.
     */
	private String getCurrentPrefix() {

		ITextSelection selection = (ITextSelection)viewer.getSelectionProvider().getSelection();
		
		if (selection.getLength() > 0) {
			return null;
		}
		
		IDocument doc = viewer.getDocument();

        return getPrefixFromDocument(doc.get(), selection.getOffset()).toLowerCase();
	}


    /**
     * Returns the prefix in the specified document text with respect to the 
     * specified offset.
     * 
     * @param aDocumentText the whole content of the edited file as String
     * @param anOffset the cursor position
     */
    protected String getPrefixFromDocument(String aDocumentText, int anOffset) {
        
        int startOfWordToken = anOffset;
        
        char token= 'a';
        if (startOfWordToken > 0) {
			token= aDocumentText.charAt(startOfWordToken - 1);
        }
        
        while (startOfWordToken > 0 
                && (Character.isJavaIdentifierPart(token) 
                    || '.' == token
					|| '-' == token
        			|| ';' == token)
                && !('$' == token)) {
            startOfWordToken--;
            if (startOfWordToken == 0) {
            	break; //word goes right to the beginning of the doc
            }
			token= aDocumentText.charAt(startOfWordToken - 1);
        }
        
        if (startOfWordToken != anOffset) {
            return aDocumentText.substring(startOfWordToken, anOffset);
        } else {
            return ""; //$NON-NLS-1$
        }
    }
 
 
    /**
     * Returns the current proposal mode.
     */
    protected int determineProposalMode(IDocument document, int aCursorPosition, String aPrefix) {

        // String from beginning of document to the beginning of the prefix
    	String text= document.get();
        String stringToPrefix = text.substring(0, aCursorPosition - aPrefix.length());

        // Is trimmable from behind
        String trimmedString = stringToPrefix.trim();
        char lastChar = 0;
        if(trimmedString.length() > 0) {
	        lastChar = trimmedString.charAt(trimmedString.length()-1);
        } else {
        	return PROPOSAL_MODE_TASK_PROPOSAL;
        }
        if(stringToPrefix.charAt(stringToPrefix.length()-1) != lastChar && lastChar != '>' && lastChar != ',') {
            /*
             * Substring must be trimmable from behind in case of attribute 
             * proposal because a space or a new line must be used as delimiter 
             * between task name and attribute or attribute and attribute.
             * Example: '<property id="bla" name="hups"'
             */
             
            // Attribute proposal
            if(lastChar != '>' && lastChar != '<') {
                String taskString= getTaskStringFromDocumentStringToPrefix(trimmedString);
                if(taskString != null && isKnownElement(taskString)) {
                    return PROPOSAL_MODE_ATTRIBUTE_PROPOSAL;
                }
            }                
        } else if(stringToPrefix.charAt(stringToPrefix.length()-1) == '"' || trimmedString.charAt(trimmedString.length()-1) == ',') {
			// Attribute value proposal
            String taskString= getTaskStringFromDocumentStringToPrefix(trimmedString);
            if (taskString != null && isKnownElement(taskString)) {
                return PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL;
            }
        } else {  // Possibly a Task proposal
            int spaceIndex = stringToPrefix.lastIndexOf(' ');
            int lessThanIndex = stringToPrefix.lastIndexOf('<');
            int greaterThanIndex = stringToPrefix.lastIndexOf('>');
            
            // Task proposal
            if(lessThanIndex > spaceIndex && greaterThanIndex < lessThanIndex) {
                int slashIndex = stringToPrefix.lastIndexOf('/');
                if(slashIndex == lessThanIndex +1) {
                    return PROPOSAL_MODE_TASK_PROPOSAL_CLOSING; // ... </
                }
                return PROPOSAL_MODE_TASK_PROPOSAL;
            }
            if(lessThanIndex < greaterThanIndex) {
            	if (isPropertyProposalMode(stringToPrefix)) {
				   return PROPOSAL_MODE_PROPERTY_PROPOSAL;
			   }
               return PROPOSAL_MODE_TASK_PROPOSAL;
            }
        }

        // Property proposal
		if (isPropertyProposalMode(stringToPrefix)) {
			return PROPOSAL_MODE_PROPERTY_PROPOSAL;
        }
        	            
        return PROPOSAL_MODE_NONE;
    }


	private boolean isPropertyProposalMode(String stringToPrefix) {
		if(stringToPrefix.length() >= 2) {
			String lastTwoChars = stringToPrefix.substring(stringToPrefix.length()-2, stringToPrefix.length());
			if(lastTwoChars.equals("${") || //$NON-NLS-1$
				stringToPrefix.charAt(stringToPrefix.length()-1) == '$') {
					return true;
			}
		}
		return false;
	}
    /**
     * Returns the last occuring task string in the specified string.
     * <P>
     * The returned string must not necessarily be a valid Ant task string.
     * This can be tested with the method <code>inNamedTaskKnown(String)</code>
     * after invoking this method.
     * 
     * @param aDocumentStringToPrefix the String that contains the whole string
     * of the currently edited file from the beginning up to the prefix for code
     * completion. Example: '<project default="name"><property '.
     * 
     * @return the extracted task string or <code>null</code> if no string could
     * be extracted.
     */
    private String getTaskStringFromDocumentStringToPrefix(String aDocumentStringToPrefix) {
            
        int lessThanIndex = aDocumentStringToPrefix.lastIndexOf('<');

        if(lessThanIndex > -1) {
            String taskString = aDocumentStringToPrefix.trim();
            taskString = taskString.substring(lessThanIndex+1, taskString.length());
            int index = taskString.indexOf(' ');
            if(index > 0) {
                taskString = taskString.substring(0, index);
            }
            index = taskString.indexOf('\n');
            if(index > 0) {
                taskString = taskString.substring(0, index);
            }
            index = taskString.indexOf('\r');
            if(index > 0) {
                taskString = taskString.substring(0, index);
            }
            return taskString;
        }
        
        return null;
    }
    

    /**
     * Returns the last occuring attribute string in the specified string.
     * <P>
     * Calling this method is only safe if the current proposal mode is really
     * <code>PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL</code>.
     */
    private String getAttributeStringFromDocumentStringToPrefix(String docStringToPrefix) {
        int index = docStringToPrefix.lastIndexOf('=');
        String subString = docStringToPrefix.substring(0, index);
        subString = subString.trim();
        
        index = subString.lastIndexOf(' ');
        if(index > 0) {
            subString = subString.substring(index+1, subString.length());
        }
        index = subString.lastIndexOf('\n');
        if(index > 0) {
            subString = subString.substring(index+1, subString.length());
        }
        index = subString.lastIndexOf('\r');
        if(index > 0) {
            subString = subString.substring(index+1, subString.length());
        }
        return subString;
    }


    /**
     * Returns whether the specified element name is known
     */
    protected boolean isKnownElement(String elementName) {
    	if (elementName.equals("target") || elementName.equals("project")) { //$NON-NLS-1$ //$NON-NLS-2$
    		return true;
    	} else {
    		AntProjectNode node= antModel.getProjectNode();
        	if (node != null) {
        		Project antProject= node.getProject();
        		return ComponentHelper.getComponentHelper(antProject).getAntTypeTable().get(elementName) != null; 
        	}
    	}
        return false;
    }

    private Class getTaskClass(String taskName) {
    	AntProjectNode node= antModel.getProjectNode();
    	if (node != null) {
    		Project antProject= node.getProject();
    		return (Class)antProject.getTaskDefinitions().get(taskName);
    	}
    	return null;
    }

    /**
     * Finds the parent task element in respect to the cursor position.
     * 
     * @return the parent task element or <code>null</code> if not found.
     */
    protected String getParentName(IDocument document, int aLineNumber, int aColumnNumber) {
    	if (document.getLength() == 0) {
    		return null;
    	}
    	AntProjectNode project= antModel.getProjectNode();
    	if (project == null) {
    		return null;
    	}
    	int offset= getOffset(document, aLineNumber, aColumnNumber);
    	if(offset == -1) {
    		return null;
    	}
    	AntElementNode node= project.getNode(offset);
    	if (node == null) {
    		node= antModel.getOpenElement();
    	}
    	if (node == null) {
    		return ""; //$NON-NLS-1$
    	} else if (node instanceof AntTaskNode) {
    		String name= node.getName();
    		if (offset <= node.getOffset() + name.length() - 1) {
    			//not really the enclosing node as the offset is within the name of the node
    			node= node.getParentNode();
    		} else {
    			return name;
    		}
    	}
    	if (node instanceof AntTaskNode) {
    		return node.getName();
    	} else if (node instanceof AntTargetNode) {
    		return "target"; //$NON-NLS-1$
    	} else {
    		return "project"; //$NON-NLS-1$
    	}
    }

    /**
     * Parses the actually edited file as far as possible.
     * <P>
     * We use the parsing facilities of the ant plug-in here.
     * 
     * @return a map with all the found properties
     */
    private Map findPropertiesFromDocument(IDocument document) {
		/*
		 * What is implemented here:
		 * - Retrieve the project from the Ant model
		 * - Determine the enclosing target
		 * - Determine the dependency Vector for the target
		 * - Work through the dependency Vector and execute the
		 *   Property relevant tasks.
		 */

    	Project project= antModel.getProjectNode().getProject();
        
        // Determine the parent
    	String targetName = getEnclosingTargetName(document, lineNumber, columnNumber);
         
        if(targetName == null) {
        	return project.getProperties();
        }
        List sortedTargets = null;
        try {
        	sortedTargets= project.topoSort(targetName, project.getTargets());
        } catch (BuildException be) {
			return project.getProperties();
        }

        int curidx = 0;
        Target curtarget;

        do {
            curtarget = (Target) sortedTargets.get(curidx++);
			Task[] tasks = curtarget.getTasks();
			
			for (int i = 0; i < tasks.length; i++) {
				Task task = tasks[i];                

				if (task instanceof UnknownElement) {
					try {
						task.maybeConfigure();
					} catch (BuildException be) {
						continue;
					}
					task= ((UnknownElement)task).getTask();
				}
				
				// sequential
				if(task instanceof Sequential) {
					// (T)
				}
				
				// parallel
				if(task instanceof Parallel) {
					// (T)
				}
				
				// waitfor (@since Ant 1.5)
	//			if(tempTask instanceof WaitFor) { 
	//				// (T)
	//			}
				
				if(task instanceof Property 
					|| task instanceof PathConvert
					|| task instanceof Available
					|| task instanceof UpToDate
					|| task instanceof Condition) {
					try {
						task.perform();
					} catch (BuildException be) {
						
					}
				}
			
			
				// Ant 1.5
//				if(tempTask instanceof LoadFile) {
//				
//				}
			
				// Ant 1.5
//				if(tempTask instanceof XmlProperty) {
//				
//				}
			
				// Ant 1.5
//				if(tempTask instanceof Basename) {
//				
//				}
			
				
				// Ant 1.5
//				if(tempTask instanceof Dirname) {
//				
//				}
			
				// Ant 1.5
//				if(tempTask instanceof LoadProperties) {
//				
//				}
            }
        } while (!curtarget.getName().equals(targetName));

        return project.getProperties();
    }
    
    private Map getTargets() {
    	Project project = antModel.getProjectNode().getProject();  
    	return project.getTargets();
    }

    protected File getEditedFile() {
    	IWorkbenchPage page= AntUIPlugin.getActivePage();
    	if (page == null) {
    		return null;
    	}
		IEditorPart editor= page.getActiveEditor();
		if (editor == null) {
			return null;
		}
        FileEditorInput input = (FileEditorInput) editor.getEditorInput();
        String projectPath = input.getFile().getProject().getLocation().toFile().getAbsolutePath();
        String  projectRelativeFilePath = input.getFile().getFullPath().removeFirstSegments(1).makeRelative().toString();
        return new File(projectPath + File.separator + projectRelativeFilePath);
    }
    
    private String getOpenElementName() {
    	AntElementNode node= antModel.getOpenElement();
    	if (node == null) {
    		return null;
    	}
    	return node.getName();
    }

    /**
     * Finds the enclosing target in respect to the cursor position and returns its name  
     * 
     * @return the name of the enclosing target or <code>null</code> if not found 
	 * or the element is not contained in a target.
     */
 	private String getEnclosingTargetName(IDocument document, int aLineNumber, int aColumnNumber) {

       AntProjectNode project= antModel.getProjectNode();
       int offset= getOffset(document, aLineNumber, aColumnNumber);
       if(offset == -1) {
       		return null;
       }
       AntElementNode node= project.getNode(offset);
       if (node instanceof AntTaskNode) {
       		node= node.getParentNode();
       		if (!(node instanceof AntTargetNode)) {
       			//top level task
       			node= null;
       		}
       } else if (node instanceof AntProjectNode) {
       		node= null;
       }
       String targetName = null;
       if(node == null 
       		|| (targetName = ((AntTargetNode)node).getTarget().getName()) == null
			|| targetName.length() == 0) {
       		return null;
       }
       return targetName;
 	}
 	
 	private int getOffset(IDocument document, int line, int column) {
 		try {
			return document.getLineOffset(line ) + column - 1;
		} catch (BadLocationException e) {
			return -1;
		}
 	}
	
	/**
	 * Sets this processor's set of characters triggering the activation of the
	 * completion proposal computation.
	 * 
	 * @param activationSet the activation set
	 */
	public void setCompletionProposalAutoActivationCharacters(char[] activationSet) {
		autoActivationChars= activationSet;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#extractPrefix(org.eclipse.jface.text.ITextViewer, int)
	 */
	protected String extractPrefix(ITextViewer textViewer, int offset) {
		IDocument document= textViewer.getDocument();
		int i= offset;
		if (i > document.getLength())
			return ""; //$NON-NLS-1$
		
		try {
			while (i > 0) {
				char ch= document.getChar(i - 1);
				if (ch != '<' && !Character.isJavaIdentifierPart(ch))
					break;
				i--;
			}
	
			return document.get(i, offset - i);
		} catch (BadLocationException e) {
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Cut out angular brackets for relevance sorting, since the template name
	 * does not contain the brackets.
	 */
	protected int getRelevance(Template template, String prefix) {
		if (prefix.startsWith("<")) //$NON-NLS-1$
			prefix= prefix.substring(1);
		if (template.getName().startsWith(prefix))
			return 90; 
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getTemplates(java.lang.String)
	 */
	protected Template[] getTemplates(String contextTypeId) {
		return AntTemplateAccess.getDefault().getTemplateStore().getTemplates();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getContextType(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	protected ContextType getContextType(ITextViewer textViewer, IRegion region) {
		return AntTemplateAccess.getDefault().getContextTypeRegistry().getContextType(XMLContextType.XML_CONTEXT_TYPE);
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.templates.TemplateCompletionProcessor#getImage(org.eclipse.jface.text.templates.Template)
     */
    protected Image getImage(Template template) {
        return AntUIImages.getImage(IAntUIConstants.IMG_TEMPLATE_PROPOSAL);
    }
}