package org.eclipse.ant.internal.ui;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */import java.lang.reflect.InvocationTargetException;import java.util.*;import org.apache.tools.ant.Target;import org.eclipse.ant.core.*;import org.eclipse.core.resources.IFile;import org.eclipse.core.runtime.*;import org.eclipse.jface.operation.IRunnableWithProgress;import org.eclipse.jface.wizard.Wizard;

public class AntLaunchWizard extends Wizard {
	
	private EclipseProject project = null;
	private AntLaunchWizardPage page1 = null;
	private IFile antFile = null;
	
	private final static String SEPARATOR_TARGETS = "\"";
	private final static String PROPERTY_SELECTEDTARGETS = "selectedTargets";
	
	public AntLaunchWizard(EclipseProject project,IFile antFile) {
		super();
		this.project = project;
		this.antFile = antFile;
	}
	
	public void addPages() {
		page1 = new AntLaunchWizardPage(project);
		addPage(page1);
		page1.setInitialTargetSelections(getTargetNamesToPreselect());
	}
	
	public String[] getTargetNamesToPreselect() {
		String propertyString = null;
		try {
			propertyString = antFile.getPersistentProperty(
				new QualifiedName(AntUIPlugin.PI_ANTUI,PROPERTY_SELECTEDTARGETS));
		} catch (CoreException e) {
			new Status(
				IStatus.WARNING,
				AntUIPlugin.PI_ANTUI,
				IStatus.WARNING,
				Policy.bind("status.targetNotRead", antFile.getFullPath().toString()),
				e);
		}
				if (propertyString == null)			return new String[0];
		StringTokenizer tokenizer = new StringTokenizer(propertyString,SEPARATOR_TARGETS);
		String result[] = new String[tokenizer.countTokens()];
		int index = 0;
		while (tokenizer.hasMoreTokens())
			result[index++] = tokenizer.nextToken();

		return result;
	}
			
	
			
	public boolean performFinish() {				Vector targetVect = page1.getSelectedTargets();
		
		// DON'T NEED IT ANYMORE
		// project.executeTargets(targetVect);				//monitor.beginTask("Running Ant", IProgressMonitor.UNKNOWN);				String[] args = createArgumentsArray(targetVect);		try {			//TBD: should remove the build listener somehow						new AntRunner().run(args/*, new UIBuildListener(monitor, antFile)*/);		} 		catch (BuildCanceledException e) {			// build was canceled don't propagate exception			return false;		}		catch (Exception e) {			// should do something here			return false;		}	/*		
		this.getContainer().run(true,true,new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				String buildFileName= null;
				buildFileName= antFile.getLocation().toOSString();

				String[] args= {"-buildfile", buildFileName};
				monitor.beginTask("Running Ant", IProgressMonitor.UNKNOWN);

				try {
					//TBD: should remove the build listener somehow
					new AntRunner().run(args, new UIBuildListener(monitor, antFile));
				} 
				catch (BuildCanceledException e) {
					// build was canceled don't propagate exception
					return;
				}
				catch (Exception e) {
					throw new InvocationTargetException(e);
				}
				finally {
					monitor.done();
				}
			};
		});
*/

		// asuming that all went well...
		storeTargetsOnFile(targetVect);
		return true;
	}		/**	 * Creates an array that contains all the arguments needed to run AntRunner: 	 * 	- the name of the file to build	 *  - the arguments such as "-verbose", ...	 *  - target names	 * 	 * @param targetVector the vector that contains the targets built during the parsing	 * @param String the arguments from the field of the wizard	 * @return String[] the tokenized arguments	 */	private String[] createArgumentsArray(Vector targets) {			Vector argsVector = new Vector();		Vector targetVect = targets;		String argString = page1.getArgumentsFromField().trim();				// if there are arguments, then we have to tokenize them		if (argString.length() != 0) {			int indexOfToken;						// Checks if the string starts with a bracket or not so that we know where the composed arguments start			if (argString.charAt(0)=='"') 				indexOfToken = 1;			else				indexOfToken=0;					// First tokenize the command line with the separator bracket			StringTokenizer tokenizer1 = new StringTokenizer(argString,"\"");					while (tokenizer1.hasMoreTokens()) {				if (indexOfToken%2 == 0) {					// this is a string that needs to be tokenized with the separator space					StringTokenizer tokenizer2 = new StringTokenizer(tokenizer1.nextToken()," ");					while (tokenizer2.hasMoreTokens())						argsVector.add(tokenizer2.nextToken());				} else {					argsVector.add(tokenizer1.nextToken());				}				indexOfToken++;			}				}						// Finally create the array of String for AntRunner		String args[] = new String[argsVector.size()+targetVect.size()+2];		int index = 0;		args[index++] = "-buildfile";		args[index++] = antFile.getLocation().toOSString();				Iterator argsIterator = argsVector.iterator();		while (argsIterator.hasNext())			args[index++] = (String) argsIterator.next();		argsIterator = targetVect.iterator();		while (argsIterator.hasNext())			args[index++] = ((Target) argsIterator.next()).getName();					/*//TEST			int i = args.length;		for (int y=0; y<i; y++)			System.out.println("- "+args[y]);*/						return args;			}

	protected void storeTargetsOnFile(Vector targets) {
		StringBuffer targetString = new StringBuffer();
		Iterator targetsIt = targets.iterator();
		
		while (targetsIt.hasNext()) {
			targetString.append(((Target)targetsIt.next()).getName());
			targetString.append(SEPARATOR_TARGETS);
		}
		
		try {
			antFile.setPersistentProperty(
				new QualifiedName(AntUIPlugin.PI_ANTUI,PROPERTY_SELECTEDTARGETS),
				targetString.toString());
		} catch (CoreException e) {
			AntUIPlugin.getPlugin().getLog().log(
				new Status(
					IStatus.WARNING,
					AntUIPlugin.PI_ANTUI,
					IStatus.WARNING,
					Policy.bind("status.targetNotWritten", antFile.getFullPath().toString()),
					e));
		}
	}
}
