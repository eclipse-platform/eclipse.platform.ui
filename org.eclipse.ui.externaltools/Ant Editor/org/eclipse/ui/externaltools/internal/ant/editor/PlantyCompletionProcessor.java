/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
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
 *******************************************************************************/

package org.eclipse.ui.externaltools.internal.ant.editor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Available;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.taskdefs.PathConvert;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.taskdefs.UpToDate;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.externaltools.internal.ant.dtd.IAttribute;
import org.eclipse.ui.externaltools.internal.ant.dtd.IDfm;
import org.eclipse.ui.externaltools.internal.ant.dtd.IElement;
import org.eclipse.ui.externaltools.internal.ant.dtd.ISchema;
import org.eclipse.ui.externaltools.internal.ant.dtd.ParseError;
import org.eclipse.ui.externaltools.internal.ant.dtd.Parser;
import org.eclipse.ui.externaltools.internal.ant.editor.utils.ProjectHelper;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.ui.IExternalToolsUIConstants;
import org.eclipse.ui.part.FileEditorInput;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * The text completion processor for Planty.
 * 
 * @author Alf Schiefelbein
 */
public class PlantyCompletionProcessor implements IContentAssistProcessor {

    /**
     * Comparator that is used for sorting 
     * <code>ICompletionProposal</code> instances by their display string.
     */
    public class CompletionComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            String tempString1 = ((ICompletionProposal)o1).getDisplayString();
            String tempString2 = ((ICompletionProposal)o2).getDisplayString();
            return tempString1.compareTo(tempString2);
        }
    }        
 
    /**
     * Helper Set that may only be used to collect 
     * <code>ICompletionProposal</code> instances.
     * <P>
     * This implementation tests the proposal objects by their display string
     * for equality.
     */
    protected class CompletionSet extends TreeSet {
        
        
        private Map displayStringToProposal = new HashMap();


        /**
         * Creates an instance that compares using the 
         * <code>CompletionComparator</code>.
         */
        public CompletionSet() {
           super(new Comparator() {
                public int compare(Object o1, Object o2) {
                    String tempString1 = ((ICompletionProposal)o1).getDisplayString();
                    String tempString2 = ((ICompletionProposal)o2).getDisplayString();
                    return tempString1.compareTo(tempString2);
                }
           });           
        }


        /**
         * Adds the specified <code>ICompletionProposal</code> only if another
         * proposal with the same display string is not contained already.
         * 
         * @param o must be instanceof <code>ICompletionProposal</code>
         */
        public boolean add(Object o) {
            ICompletionProposal tempProposal = (ICompletionProposal)o;
            if(!displayStringToProposal.containsKey(tempProposal.getDisplayString())) {
                boolean tempResult = super.add(o);
                if(tempResult) {
                    displayStringToProposal.put(tempProposal.getDisplayString(), tempProposal);
                }
                return tempResult;
            }
            return false;
        }
            
        
        /**
         * Returns true if no another proposal with the same display string is 
         * contained allready.
         * 
         * @param o must be instanceof <code>ICompletionProposal</code>
         */
        public boolean contains(Object o) {
            ICompletionProposal tempProposal = (ICompletionProposal)o;
            if(!displayStringToProposal.containsKey(tempProposal.getDisplayString())) {
                return true;
            }
            return false;
        }


        /**
         * Removes the specified proposal.
         * 
         * @param o must be instanceof <code>ICompletionProposal</code>
         */
        public boolean remove(Object o) {
            ICompletionProposal tempProposal = (ICompletionProposal)o;
            boolean tempResult = super.remove(o);
            if(tempResult) {
                Object tempObject = displayStringToProposal.remove(tempProposal.getDisplayString());
                if(tempObject == null) {
                    throw new NoSuchElementException(AntEditorMessages.getString("PlantyCompletionProcessor.Serious_Error")); //$NON-NLS-1$
                }
            }
            return tempResult;
        }

    }


    protected final static int PROPOSAL_MODE_NONE = 0;
    protected final static int PROPOSAL_MODE_TASK_PROPOSAL = 1;
    protected final static int PROPOSAL_MODE_ATTRIBUTE_PROPOSAL = 2;
    protected final static int PROPOSAL_MODE_TASK_PROPOSAL_CLOSING = 3;
    protected final static int PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL = 4;
    protected final static int PROPOSAL_MODE_PROPERTY_PROPOSAL = 5;

    protected final static String REQUIRED = "#REQUIRED"; //$NON-NLS-1$
    
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
    
    public static final String ANT_1_5_DTD_FILENAME = "/ant1.5b.dtd"; //$NON-NLS-1$

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
	protected ITextViewer viewer;
	
	private char[] autoActivationChars= new char[]{'<', '$', '{' };
	
    
    /**
     * The provider for all task and attribute descriptions.
     */
    protected TaskDescriptionProvider descriptionProvider = new TaskDescriptionProvider();
	private PlantySaxDefaultHandler lastDefaultHandler;
	
    
	/**
	 * Constructor for PlantyCompletionProcessor.
	 */
	public PlantyCompletionProcessor() {
		super();
		if(dtd == null) {
	        try {
	     	  	dtd = parseDtd();
	        } catch (IOException e) {
	        	ExternalToolsPlugin.getDefault().log(e);
	        } catch (ParseError e) {
				ExternalToolsPlugin.getDefault().log(e);
			}
		}
	}

    /**
     * Parses the dtd.
     */
    private ISchema parseDtd() throws ParseError, IOException {
        InputStream tempStream = getClass().getResourceAsStream(ANT_1_5_DTD_FILENAME);
        InputStreamReader tempReader = new InputStreamReader(tempStream, "UTF-8"); //$NON-NLS-1$
        Parser parser = new Parser();
        return parser.parseDTD(tempReader, "project"); //$NON-NLS-1$
    }
    
	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		this.viewer = viewer;
		return determineProposals();
	
	}
	
	
	/**
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
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
		return AntEditorMessages.getString("PlantyCompletionProcessor.No_Text_Completions_2"); //$NON-NLS-1$
	}
	

    /**
     * Returns the new determined proposals.
     */ 
	private ICompletionProposal[] determineProposals() {
		
		String prefix = getCurrentPrefix();
		cursorPosition = ((ITextSelection)viewer.getSelectionProvider().getSelection()).getOffset();
        
        IDocument tempDocument = viewer.getDocument();
        try {
            lineNumber = tempDocument.getLineOfOffset(cursorPosition);
            columnNumber = cursorPosition - tempDocument.getLineOffset(lineNumber);
        } catch (BadLocationException e) {
            ExternalToolsPlugin.getDefault().log(e);
        }
		
		if (prefix == null || cursorPosition == -1) {
			if (ExternalToolsPlugin.getDefault().isDebugging()) {
				ExternalToolsPlugin.getDefault().log(AntEditorMessages.getString("PlantyCompletionProcessor.Could_not_do_completion_3"), null); //$NON-NLS-1$
			}
			IWorkbenchWindow window= ExternalToolsPlugin.getActiveWorkbenchWindow();
			if (window != null) {
				window.getShell().getDisplay().beep();
			}
			return null;
		}
	
		ICompletionProposal[] proposals = getProposalsFromDocument(tempDocument.get(), prefix);
		return proposals;
		
	}


    /**
     * Returns the proposals for the specified document.
     * 
     * @param aDocmuentString the text of the currently edited file as one string.
     * @param the prefix
     */
    protected ICompletionProposal[] getProposalsFromDocument(String aDocumentString, String aPrefix) {
        String tempTaskString = null;

        /*
         * Completions will be determined depending on the proposal mode.
         */
        switch (determineProposalMode(aDocumentString, cursorPosition, aPrefix)) {

            case PROPOSAL_MODE_NONE :
                break;

            case PROPOSAL_MODE_ATTRIBUTE_PROPOSAL:
                tempTaskString = getTaskStringFromDocumentStringToPrefix(aDocumentString.substring(0, cursorPosition-aPrefix.length()));
                return getAttributeProposals(tempTaskString, aPrefix);

            case PROPOSAL_MODE_TASK_PROPOSAL:
                return getTaskProposals(aDocumentString, findParentElement(aDocumentString, lineNumber, columnNumber), aPrefix);

            case PROPOSAL_MODE_TASK_PROPOSAL_CLOSING:
                return getClosingTaskProposals(findNotClosedParentElement(aDocumentString, lineNumber, columnNumber), aPrefix);

            case PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL:
                tempTaskString = getTaskStringFromDocumentStringToPrefix(aDocumentString.substring(0, cursorPosition-aPrefix.length()));
                String tempAttributeString = getAttributeStringFromDocumentStringToPrefix(aDocumentString.substring(0, cursorPosition-aPrefix.length()));
                return getAttributeValueProposals(tempTaskString, tempAttributeString, aPrefix);

            case PROPOSAL_MODE_PROPERTY_PROPOSAL:
                return getPropertyProposals(aDocumentString, aPrefix, cursorPosition);

            default :
                break;
        } 
        
         return new ICompletionProposal[0];

    }
    
    /**
     * Returns all possible attributes for the specified task.
     * 
     * @param aTaskName the name of the task for that the attribute shall be 
     * completed
     * @param aPrefix prefix, that all proposals should start with. The prefix
     * may be an empty string.
     */
    protected ICompletionProposal[] getAttributeProposals(String aTaskName, String aPrefix) {
        ArrayList tempProposals = new ArrayList();
        IElement tempElement = dtd.getElement(aTaskName);
        if (tempElement != null) {
        	Iterator tempKeys = tempElement.getAttributes().keySet().iterator();
        	while (tempKeys.hasNext()) {
        		String tempAttrName = (String) tempKeys.next();
        		if (tempAttrName.startsWith(aPrefix)) {
        			IAttribute tempDTDAttribute = (IAttribute) tempElement.getAttributes().get(tempAttrName);
					String tempReplacementString = tempAttrName+"=\"\""; //$NON-NLS-1$
					String tempDisplayString = tempAttrName;
					String[] tempItems = tempDTDAttribute.getEnum();
					if (tempItems != null) {					        			
                        if(tempItems.length > 1) {
                            tempDisplayString += " - ("; //$NON-NLS-1$
                        }
                        for (int i = 0; i < tempItems.length; i++) {
                            tempDisplayString += tempItems[i];
                            if(i+1 < tempItems.length) {
                                tempDisplayString += " | "; //$NON-NLS-1$
                            }
                            else {
                                tempDisplayString += ")"; //$NON-NLS-1$
                            }
                        }
                    }
                    
                    String tempProposalInfo = null;
                    String tempRequired = descriptionProvider.getRequiredAttributeForTaskAttribute(aTaskName, tempAttrName);
                    if(tempRequired != null && tempRequired.length() > 0) {
                        tempProposalInfo = AntEditorMessages.getString("PlantyCompletionProcessor.Required___4") + tempRequired; //$NON-NLS-1$
                        tempProposalInfo += "<BR><BR>"; //$NON-NLS-1$
                    }
                    String tempDescription = descriptionProvider.getDescriptionForTaskAttribute(aTaskName, tempAttrName);
                    if(tempDescription != null) {
                        tempProposalInfo = (tempProposalInfo == null ? "" : tempProposalInfo); //$NON-NLS-1$
                        tempProposalInfo += tempDescription;
                    }
                    
                    
                    ICompletionProposal tempProposal = new CompletionProposal(tempReplacementString, cursorPosition - aPrefix.length(), aPrefix.length(), tempAttrName.length()+2, null, tempDisplayString, null, tempProposalInfo);
                    /*
                     * This is how we do it, once we have the documentation.
                     *
                     * IContextInformation tempContextInfo = new ContextInformation("contextDisplayString", "informationDisplayString");
                     * ICompletionProposal tempProposal = new CompletionProposal(tempAttrName+"=\"\"", cursorPosition - aPrefix.length(), aPrefix.length(), tempAttrName.length()+2, null, tempAttrName, tempContextInfo, "Pustekuchen ;-) !!!");
                     */
                    tempProposals.add(tempProposal);
                }       
            }
        }
        return (ICompletionProposal[])tempProposals.toArray(new ICompletionProposal[tempProposals.size()]);
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
     * @param aPrefix prefix, that all proposals should start with. The prefix
     * may be an empty string.
     */
    protected ICompletionProposal[] getAttributeValueProposals(String aTaskName, String anAttributeName, String aPrefix) {
        ArrayList tempProposals = new ArrayList();
        IElement tempElement = dtd.getElement(aTaskName);
        if (tempElement != null) {
        	IAttribute tempAttribute = (IAttribute) tempElement.getAttributes().get(anAttributeName);
        	if (tempAttribute != null) {
        		String[] tempItems = tempAttribute.getEnum();
        		if (tempItems != null) {
                    for (int i = 0; i < tempItems.length; i++) {
                        String tempItem = tempItems[i];
                        if(tempItem.startsWith(aPrefix)) {
                            ICompletionProposal tempProposal = new CompletionProposal(tempItem, cursorPosition - aPrefix.length(), aPrefix.length(), tempItem.length(), null, tempItem, null, null);
                            tempProposals.add(tempProposal);
                        }
                    }
        		}
            }
        }
        return (ICompletionProposal[])tempProposals.toArray(new ICompletionProposal[tempProposals.size()]);
    }

    
    /**
     * Returns all possible properties for the specified prefix.
     * <P>
     * Note that the completion mode must be property mode, otherwise it is not
     * safe to call this method.
     */
    protected ICompletionProposal[] getPropertyProposals(String aDocumentText, String aPrefix, int aCursorPosition) {
        Set tempProposals = new CompletionSet();
        Map tempProperties = findPropertiesFromDocument(aDocumentText);
        for(Iterator i=tempProperties.keySet().iterator(); i.hasNext(); ) {
            String tempPropertyName = (String)i.next();
            if(tempPropertyName.startsWith(aPrefix)) {
                String tempPropertyValue = (String)tempProperties.get(tempPropertyName);
                String tempAdditPropInfo = tempPropertyValue;

                // Determine replacement length and offset
                // String from beginning to the beginning of the prefix
                int tempReplacementLength = aPrefix.length();
                int tempReplacementOffset = 0;
                String tempStringToPrefix = aDocumentText.substring(0, aCursorPosition - aPrefix.length());
                // Property proposal
                String tempLastTwoCharacters = tempStringToPrefix.substring(tempStringToPrefix.length()-2, tempStringToPrefix.length());
                if(tempLastTwoCharacters.equals("${")) { //$NON-NLS-1$
                    tempReplacementLength += 2;
                    tempReplacementOffset = aCursorPosition - aPrefix.length() - 2;
                }
                else if(tempLastTwoCharacters.endsWith("$")) { //$NON-NLS-1$
                    tempReplacementLength += 1;
                    tempReplacementOffset = aCursorPosition - aPrefix.length() - 1;                }
                else {
                    throw new PlantyException(AntEditorMessages.getString("PlantyCompletionProcessor.Error")); //$NON-NLS-1$
                }
                if(aDocumentText.length() > aCursorPosition && aDocumentText.charAt(aCursorPosition) == '}') {
                    tempReplacementLength += 1;
                }
                 
                String tempReplacementString = new StringBuffer("${").append(tempPropertyName).append('}').toString();  //$NON-NLS-1$
                Image tempImage = ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_PROPERTY);
                
                ICompletionProposal tempProposal = 
                    new CompletionProposal(
                        tempReplacementString, 
                        tempReplacementOffset, 
                        tempReplacementLength, 
                        tempReplacementString.length(), 
						tempImage,
                        tempPropertyName, null, 
                        tempAdditPropInfo);
                tempProposals.add(tempProposal);
            }
        }                
        return (ICompletionProposal[])tempProposals.toArray(new ICompletionProposal[tempProposals.size()]);
    }


    /**
     * Returns all possible attributes for the specified parent task element.
     * <P>
     * No completions will be returned if <code>aParentTaskElement</code> is 
     * not known.
     * 
     * @param aParentTaskName name of the parent(surrounding) task or 
     * <code>null</code> if completion should be done for the root element.
     * 
     * @param aPrefix prefix, that all proposals should start with. The prefix
     * may be an empty string.
     */
    protected ICompletionProposal[] getTaskProposals(String aWholeDocumentString, Element aParentTaskElement, String aPrefix) {
		// The code this replaced assumed that child elements
		// are unordered; there was no provision for walking
		// through a child sequence. This works for the Ant
		// 1.5 DTD but not in general. I kept the assumption. bf
        LinkedList tempProposals = new LinkedList();
        Image tempImage = ExternalToolsImages.getImage(IExternalToolsUIConstants.IMAGE_ID_TASK);
        
        if (aParentTaskElement == null) {
        	// DTDs do not designate a root element.
        	// The previous code must have looked for an element that
        	// was not in the content model of any other element.
        	// This test doesn't work with a DTD used to validate
        	// document partitions, a DTD with multiple candidate
        	// roots, etc. The right answer is to get
        	// the root element from the document. If there isn't
        	// one, we assume "project". bf
            String tempRootElementName = null;
	       	if(lastDefaultHandler != null) {
                tempRootElementName = lastDefaultHandler.rootElementName;
        	}
			if (tempRootElementName == null) {
				tempRootElementName = aPrefix + "project"; //$NON-NLS-1$
			}
			IElement tempRootElement = dtd.getElement(tempRootElementName);
			if(tempRootElement != null && tempRootElementName.startsWith(aPrefix)) {
				String tempProposalInfo = null;
				String tempDescription = descriptionProvider.getDescriptionForTask(tempRootElementName);
				if(tempDescription != null) {
					tempProposalInfo = tempDescription;
				}
	                                
				String tempReplacementString = getTaskProposalReplacementString(tempRootElementName);
				int tempReplacementOffset = cursorPosition-aPrefix.length();
				int tempReplacementLength = aPrefix.length();
				if(tempReplacementOffset > 0 && tempReplacementOffset-1 < aWholeDocumentString.length() && aWholeDocumentString.charAt(tempReplacementOffset-1) == '<') {
					tempReplacementOffset--;
					tempReplacementLength++;
				}

				ICompletionProposal tempProposal = new CompletionProposal(
					tempReplacementString, 
					tempReplacementOffset, 
					tempReplacementLength, 
					tempRootElementName.length()+2, 
					tempImage, 
					tempRootElementName, 
					null, 
					tempProposalInfo);
				tempProposals.add(tempProposal);
				//commented this out because it doesn't quite replace the above
				//ICompletionProposal tempProposal = makeProposal(tempRootElementName, aPrefix, aWholeDocumentString);
				//tempProposals.add(tempProposal);
			}
        }
        else {
			IElement parent = dtd.getElement(aParentTaskElement.getTagName());
			if (parent != null) {
				IDfm dfm = parent.getDfm();
				String[] accepts = dfm.getAccepts();
				for (int i = 0; i < accepts.length; i++) {
					String tempElementName = accepts[i];

					if(tempElementName.startsWith(aPrefix)) {
                                        
						String tempProposalInfo = null;
						String tempDescription = descriptionProvider.getDescriptionForTask(tempElementName);
						if(tempDescription != null) {
							tempProposalInfo = tempDescription;
						}
                                                            
						String tempReplacementString = getTaskProposalReplacementString(tempElementName);
						int tempReplacementOffset = cursorPosition-aPrefix.length();
						int tempReplacementLength = aPrefix.length();
						if(tempReplacementOffset > 0 && aWholeDocumentString.charAt(tempReplacementOffset-1) == '<') {
							tempReplacementOffset--;
							tempReplacementLength++;
						}
						ICompletionProposal tempProposal = new CompletionProposal(
							tempReplacementString, 
							tempReplacementOffset, 
							tempReplacementLength, 
							tempElementName.length()+2, 
							tempImage, 
							tempElementName, 
							null, 
							tempProposalInfo);
						tempProposals.add(tempProposal);
					}
					//commented this out because it doesn't quite replace the above
					//ICompletionProposal tempProposal = makeProposal(tempElementName, aPrefix, aWholeDocumentString);
					//tempProposals.add(tempProposal);
				}
			}
        }
        
       return (ICompletionProposal[])tempProposals.toArray(new ICompletionProposal[tempProposals.size()]);
   }

    /**
     * Returns the one possible completion for the specified unclosed task 
     * element.
     * 
     * @param aUnclosedTaskElement the task element that hasn't been closed 
     * last
     * 
     * @param aPrefix prefix, that the one possible proposals should start 
     * with. The prefix may be an empty string.
     * 
     * @return array which may contain either one or none proposals
     */
    private ICompletionProposal[] getClosingTaskProposals(Element aUnclosedTaskElement, String aPrefix) {
        Set tempProposals = new CompletionSet();
        if(aUnclosedTaskElement != null) {
            if(aUnclosedTaskElement.getTagName().startsWith(aPrefix)) {
                String tempReplaceString = aUnclosedTaskElement.getTagName();
                tempProposals.add(new CompletionProposal(tempReplaceString + '>', cursorPosition - aPrefix.length(), aPrefix.length(), tempReplaceString.length()+1, null, tempReplaceString, null, null));
            }
        }
        return (ICompletionProposal[])tempProposals.toArray(new ICompletionProposal[tempProposals.size()]);
    }

    /**
     * Returns the replacement string for the specified task name.
     */
    private String getTaskProposalReplacementString(String aTaskName) {
        StringBuffer tempReplacement = new StringBuffer("<"); //$NON-NLS-1$
        tempReplacement.append(aTaskName); 
        if(isEmpty(aTaskName)) {
            tempReplacement.append(" />"); //$NON-NLS-1$
        }
        else {
            tempReplacement.append(" ></"); //$NON-NLS-1$
            tempReplacement.append(aTaskName);
            tempReplacement.append('>');
        }
        return tempReplacement.toString();               
    }


    /**
     * Returns whether the named element is empty, thus may not have any child
     * elements.
     */
    private boolean isEmpty(String aDTDElementName) {
        IElement element = dtd.getElement(aDTDElementName);
        return element.isEmpty();
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
        NodeList tempNodeList = anElement.getChildNodes();
        for (int i = 0; i < tempNodeList.getLength(); i++) {
            Node tempChildNode = (Node)tempNodeList.item(i);
            if(tempChildNode.getNodeType() == Node.ELEMENT_NODE) {
                if(tempChildNode.getNodeName().equals(aChildElementName)) {
                    return (Element)tempChildNode;
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
     * @param aDocumentString the whole content of the edited file as String
     * @param anOffset the cursor position
     */
    protected String getPrefixFromDocument(String aDocumentText, int anOffset) {
        
        int startOfWordToken = anOffset;
        
        while (startOfWordToken > 0 
                && (Character.isJavaIdentifierPart(aDocumentText.charAt(startOfWordToken - 1)) 
                    || '.' == aDocumentText.charAt(startOfWordToken - 1)
                    )
                && !('$' == aDocumentText.charAt(startOfWordToken - 1))) {
            startOfWordToken--;
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
    protected int determineProposalMode(String aWholeDocumentString, int aCursorPosition, String aPrefix) {

        // String from beginning of document to the beginning of the prefix
        String tempStringToPrefix = aWholeDocumentString.substring(0, aCursorPosition - aPrefix.length());

        // Is trimmable from behind
        String tempTrimmedString = tempStringToPrefix.trim();
        char tempLastChar = 0;
        if(tempTrimmedString.length() > 0) {
	        tempLastChar = tempTrimmedString.charAt(tempTrimmedString.length()-1);
        }
        else {
        	return PROPOSAL_MODE_TASK_PROPOSAL;
        }
        if(tempStringToPrefix.charAt(tempStringToPrefix.length()-1) != tempLastChar && tempLastChar != '>') {
            /*
             * Substring must be trimmable from behind in case of attribute 
             * proposal because a space or a new line must be used as delimiter 
             * between task name and attribute or attribute and attribute.
             * Example: '<property id="bla" name="hups"'
             */
             
            // Attribute proposal
            if(tempLastChar != '>' && tempLastChar != '<') {
                String tempTaskString =
                    getTaskStringFromDocumentStringToPrefix(
                        tempTrimmedString);
                if(tempTaskString != null && isNamedTaskKnown(tempTaskString)) {
                    return PROPOSAL_MODE_ATTRIBUTE_PROPOSAL;
                }
            }                
        }

        // Attribute value proposal
        else if(tempStringToPrefix.charAt(tempStringToPrefix.length()-1) == '"') {
            String tempTaskString =
                getTaskStringFromDocumentStringToPrefix(
                    tempTrimmedString);
            if(tempTaskString != null && isNamedTaskKnown(tempTaskString)) {
                return PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL;
            }
        }
        
        // Task proposal
        else {
            int tempSpaceIndex = tempStringToPrefix.lastIndexOf(' ');
            int tempLessThanIndex = tempStringToPrefix.lastIndexOf('<');
            int tempGreaterThanIndex = tempStringToPrefix.lastIndexOf('>');
            
            // Task proposal
            if(tempLessThanIndex > tempSpaceIndex && tempGreaterThanIndex < tempLessThanIndex) {
                int tempSlashIndex = tempStringToPrefix.lastIndexOf('/');
                if(tempSlashIndex == tempLessThanIndex +1) {
                    return PROPOSAL_MODE_TASK_PROPOSAL_CLOSING; // ... </
                }
                return PROPOSAL_MODE_TASK_PROPOSAL;
            }
            if(tempLessThanIndex < tempGreaterThanIndex && "".equals(aPrefix)) { //$NON-NLS-1$
                
                // no other regular character may be between '>' and cursor position
                int tempActIndex = aCursorPosition;
                do {
                    char tempChar = tempStringToPrefix.charAt(--tempActIndex);
                    if(tempChar != ' ' && tempChar != '\t' && tempChar != '\n' && tempChar != '\r') {
                        break; // found a character -> no task proposal mode
                    }
                } while(tempActIndex > tempGreaterThanIndex);  

                // no character found in between                  
                if(tempActIndex == tempGreaterThanIndex) {           
                    return PROPOSAL_MODE_TASK_PROPOSAL;
                }
            }
        }

        // Property proposal
        if(tempStringToPrefix.length() >= 2) {
	        String tempLastTwoCharacters = tempStringToPrefix.substring(tempStringToPrefix.length()-2, tempStringToPrefix.length());
	        if(tempLastTwoCharacters.equals("${") || //$NON-NLS-1$
	            tempStringToPrefix.charAt(tempStringToPrefix.length()-1) == '$') {
	                return PROPOSAL_MODE_PROPERTY_PROPOSAL;
	        }
        }
        	            
        return PROPOSAL_MODE_NONE;
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
    private String getTaskStringFromDocumentStringToPrefix(
            String aDocumentStringToPrefix) {
            
        int tempLessThanIndex = aDocumentStringToPrefix.lastIndexOf('<');

        if(tempLessThanIndex > -1) {
            String tempTaskString = aDocumentStringToPrefix.trim();
            tempTaskString = tempTaskString.substring(tempLessThanIndex+1, tempTaskString.length());
            int tempIndex = tempTaskString.indexOf(' ');
            if(tempIndex > 0) {
                tempTaskString = tempTaskString.substring(0, tempIndex);
            }
            tempIndex = tempTaskString.indexOf('\n');
            if(tempIndex > 0) {
                tempTaskString = tempTaskString.substring(0, tempIndex);
            }
            tempIndex = tempTaskString.indexOf('\r');
            if(tempIndex > 0) {
                tempTaskString = tempTaskString.substring(0, tempIndex);
            }
            return tempTaskString;
        }
        
        return null;
    }
    

    /**
     * Returns the last occuring attribute string in the specified string.
     * <P>
     * Calling this method is only safe if the current proposal mode is really
     * <code>PROPOSAL_MODE_ATTRIBUTE_VALUE_PROPOSAL</code>.
     */
    private String getAttributeStringFromDocumentStringToPrefix(String aDocumentStringToPrefix) {
        int tempIndex = aDocumentStringToPrefix.lastIndexOf('=');
        String tempSubString = aDocumentStringToPrefix.substring(0, tempIndex);
        tempSubString = tempSubString.trim();
        
        tempIndex = tempSubString.lastIndexOf(' ');
        if(tempIndex > 0) {
            tempSubString = tempSubString.substring(tempIndex+1, tempSubString.length());
        }
        tempIndex = tempSubString.lastIndexOf('\n');
        if(tempIndex > 0) {
            tempSubString = tempSubString.substring(tempIndex+1, tempSubString.length());
        }
        tempIndex = tempSubString.lastIndexOf('\r');
        if(tempIndex > 0) {
            tempSubString = tempSubString.substring(tempIndex+1, tempSubString.length());
        }
        return tempSubString;
    }


    /**
     * Returns whether the specified task name is known according to the DTD.
     */
    private boolean isNamedTaskKnown(String aTaskName) {
        return dtd.getElement(aTaskName) != null;
    }


    /**
     * Finds the parent task element in respect to the cursor position.
     * 
     * @return the parent task element or <code>null</code> if not found.
     */
    protected Element findParentElement(String aWholeDocumentString, int aLineNumber, int aColumnNumber) {

        // Return the parent
        return parseEditedFileSearchingForParent(aWholeDocumentString, aLineNumber, aColumnNumber).getParentElement(true);      
    }
    

    /**
     * Parses the actually edited file as far as possible.
     * <P>
     * The returned handler can be asked about what happend while parsing
     * the document.
     * 
     * @return the handler that has been used for parsing or <code>null</code>
     * if parsing couldn't be done because of some error.
     */
    protected PlantySaxDefaultHandler parseEditedFileSearchingForParent(String aWholeDocumentString, int aLineNumber, int aColumnNumber) {
        // Get a new SAX Parser
        SAXParser tempParser = null;
        try {
            tempParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException e) {
            ExternalToolsPlugin.getDefault().log(e);
            return null;
        } catch (SAXException e) {
			ExternalToolsPlugin.getDefault().log(e);
            return null;
        }
        
        // Set the handler
        PlantySaxDefaultHandler tempHandler = null;
        File editedFile= getEditedFile();
        try {
			File parent = null;
			if(editedFile != null) {
				parent = editedFile.getParentFile();
			}
        	tempHandler = new PlantySaxDefaultHandler(parent, aLineNumber, aColumnNumber);
        } catch (ParserConfigurationException e) {
			ExternalToolsPlugin.getDefault().log(e);
        }
        
        // Parse!
        InputSource tempInputSource = new InputSource(new StringReader(aWholeDocumentString));
		if (editedFile != null) {
			//needed for resolving relative external entities
			tempInputSource.setSystemId(editedFile.getAbsolutePath());
		}
        try {
            tempParser.parse(tempInputSource, tempHandler);
        } catch(SAXParseException e) {
            // Ignore since that happens always if the edited file is not valid. We try to handle that.
        } catch (SAXException e) {
            ExternalToolsPlugin.getDefault().log(e);
        } catch (IOException e) {
            //ignore since can happen when user has incorrect paths / protocols for external entities
        }
        
        lastDefaultHandler = tempHandler; // bf
        return tempHandler;
    }


    /**
     * Parses the actually edited file as far as possible.
     * <P>
     * We use the parsing facilities of the ant plug-in here.
     * 
     * @return a map with all the found properties
     */
    protected Map findPropertiesFromDocument(String aWholeDocumentString) {
		/*
		 * What is implemented here:
		 * - We first use the ant plug-in to create a Project instance.
		 * - We determine the enclosing parent task element
		 * - We determine the dependency Vector for the parent task element
		 * - We work our way through the dependency Vector and execute the
		 *   Property relevant tasks.
		 */

        // Create an initialized project
        Project tempProject = new Project();
        tempProject.init();

        /* 
         * Ant's parsing facilities always works on a file, therefore we need
         * to determine the actual location of the file. Though the file 
         * contents will not be parsed. We parse the passed document string 
         * that is passed.
         */
        File tempFile = getEditedFile();
        String filePath= ""; //$NON-NLS-1$
        if (tempFile != null) {
			filePath= tempFile.getAbsolutePath();
        }
        tempProject.setUserProperty("ant.file", filePath); //$NON-NLS-1$

        try {
            ProjectHelper.configureProject(tempProject, tempFile, aWholeDocumentString);  // File will be parsed here
        }
        catch(BuildException e) {
            // ignore a build exception on purpose, since we also parse invalid
            // build files.
        }    
        Map properties = tempProject.getProperties();
        
        // Determine the parent
        Element tempElement = findEnclosingTargetElement(aWholeDocumentString, lineNumber, columnNumber);
        String tempTargetName = null;
        if(tempElement == null 
        		|| (tempTargetName = tempElement.getAttribute("name")) == null //$NON-NLS-1$
        		|| tempTargetName.length() == 0) {
        	return properties;
        }
        List tempSortedTargets = null;
        try {
        	tempSortedTargets= tempProject.topoSort(tempTargetName, tempProject.getTargets());
        } catch (BuildException be) {
			return tempProject.getProperties();
        }

        int curidx = 0;
        Target curtarget;

        do {
            curtarget = (Target) tempSortedTargets.get(curidx++);
			Task[] tempTasks = curtarget.getTasks();
			
			for (int i = 0; i < tempTasks.length; i++) {
				Task tempTask = tempTasks[i];                

				// sequential
				if(tempTask instanceof Sequential) {
					// (T)
				}
				
				// parallel
				if(tempTask instanceof Parallel) {
					// (T)
				}
				
				// waitfor (@since Ant 1.5)
	//			if(tempTask instanceof WaitFor) { 
	//				// (T)
	//			}
				
				if(tempTask instanceof Property 
					|| tempTask instanceof PathConvert
					|| tempTask instanceof Available
					|| tempTask instanceof UpToDate
					|| tempTask instanceof Condition) {
					((Task)tempTask).perform();					
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
        } while (!curtarget.getName().equals(tempTargetName));

        
        // Need to reget it since tempTable hasn't been updated with Ant 1.5
        return tempProject.getProperties();
    }

    protected File getEditedFile() {
    	IWorkbenchPage page= ExternalToolsPlugin.getActivePage();
    	if (page == null) {
    		return null;
    	}
		IEditorPart editor= page.getActiveEditor();
		if (editor == null) {
			return null;
		}
        FileEditorInput tempInput = (FileEditorInput) editor.getEditorInput();
        String tempProjectPath = tempInput.getFile().getProject().getLocation().toFile().getAbsolutePath();
        String tempProjectRelativeFilePath = tempInput.getFile().getFullPath().removeFirstSegments(1).makeRelative().toString();
        File tempFile = new File(tempProjectPath + File.separator +tempProjectRelativeFilePath);
        return tempFile;
    }

    /**
     * Finds the parent task element in respect to the cursor position which
     * that has not been closed yet.
     * 
     * @return the not closed parent task element or <code>null</code> if not 
     * found.
     */
    protected Element findNotClosedParentElement(String aWholeDocumentString, int aLineNumber, int aColumnNumber) {
        PlantySaxDefaultHandler tempHandler = parseEditedFileSearchingForParent(aWholeDocumentString, aLineNumber, aColumnNumber);
        if(tempHandler != null) {
            
            // A not closed parent element can only be found by guessing.
            if(tempHandler.getParentElement(false) == null) {
                return tempHandler.getParentElement(true);
            }

        }
        return null;
    }
 

    /**
     * Finds the enclosing target element in respect to the cursor position. 
     * 
     * @return the enclosing target element or <code>null</code> if not found.
     */
 	protected Element findEnclosingTargetElement(String aWholeDocumentString, int aLineNumber, int aColumnNumber) {

        // Get a new SAX Parser
        SAXParser tempParser = null;
        try {
            tempParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (ParserConfigurationException e) {
            ExternalToolsPlugin.getDefault().log(e);
            return null;
        } catch (SAXException e) {
            ExternalToolsPlugin.getDefault().log(e);
            return null;
        }
        
        // Set the handler
        EnclosingTargetSearchingHandler tempHandler = null;
		File editedFile= getEditedFile();
        try {
		   File parent = null;
		   if(editedFile != null) {
			   parent = editedFile.getParentFile();
		   }
            tempHandler = new EnclosingTargetSearchingHandler(parent, aLineNumber, aColumnNumber);
        } catch (ParserConfigurationException e) {
            ExternalToolsPlugin.getDefault().log(e);
        }
        
        // Parse!
        InputSource tempInputSource = new InputSource(new StringReader(aWholeDocumentString));
		if (editedFile != null) {
			//needed for resolving relative external entities
			tempInputSource.setSystemId(editedFile.getAbsolutePath());
		}
        try {
            tempParser.parse(tempInputSource, tempHandler);
        } catch(SAXParseException e) {
            // Ignore since that happens always if the edited file is not valid. We try to handle that.
        } catch (SAXException e) {
            ExternalToolsPlugin.getDefault().log(e);
        } catch (IOException e) {
            ExternalToolsPlugin.getDefault().log(e);
        }

		return tempHandler.getParentElement(true);
 	}
}