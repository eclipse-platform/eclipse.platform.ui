package org.eclipse.e4.internal.tools.wizards.classes.templates;

import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;

public class AddonTemplate
{
  protected static String nl;
  public static synchronized AddonTemplate create(String lineSeparator)
  {
    nl = lineSeparator;
    AddonTemplate result = new AddonTemplate();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = " ";
  protected final String TEXT_2 = NL + "package ";
  protected final String TEXT_3 = ";";
  protected final String TEXT_4 = NL + NL + "import javax.inject.Inject;" + NL + "" + NL + "import org.eclipse.e4.core.di.annotations.Optional;" + NL + "import org.eclipse.e4.core.di.extensions.EventTopic;" + NL + "import org.eclipse.e4.ui.workbench.UIEvents;" + NL + "import org.osgi.service.event.Event;" + NL + "" + NL + "import org.eclipse.e4.core.services.events.IEventBroker;" + NL + "" + NL + "public class ";
  protected final String TEXT_5 = " {" + NL + "" + NL + "\t@Inject" + NL + "\t@Optional" + NL + "\tpublic void applicationStarted(" + NL + "\t\t\t@EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {" + NL + "\t\t// TODO Modify the UIEvents.UILifeCycle.APP_STARTUP_COMPLETE EventTopic to a certain event you want to listen to." + NL + "\t}" + NL + "" + NL + "}";
  protected final String TEXT_6 = NL;

  public String generate(Object argument)
  {
    final StringBuffer stringBuffer = new StringBuffer();
     JavaClass domainClass = (JavaClass)argument; 
    stringBuffer.append(TEXT_1);
     if( domainClass.getPackageFragment() != null && domainClass.getPackageFragment().getElementName().trim().length() > 0 ) { 
    stringBuffer.append(TEXT_2);
    stringBuffer.append( domainClass.getPackageFragment().getElementName() );
    stringBuffer.append(TEXT_3);
     } 
    stringBuffer.append(TEXT_4);
    stringBuffer.append( domainClass.getName() );
    stringBuffer.append(TEXT_5);
    stringBuffer.append(TEXT_6);
    return stringBuffer.toString();
  }
}
