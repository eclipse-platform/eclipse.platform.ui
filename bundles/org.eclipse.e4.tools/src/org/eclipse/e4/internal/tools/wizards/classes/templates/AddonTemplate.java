package org.eclipse.e4.internal.tools.wizards.classes.templates;

import org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage.JavaClass;
@SuppressWarnings("nls")
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
  protected final String TEXT_4 = NL + NL + "import javax.inject.Inject;" + NL + "import javax.annotation.PostConstruct;" + NL + "import javax.annotation.PreDestroy;" + NL + "" + NL + "import org.eclipse.e4.core.services.events.IEventBroker;" + NL + "" + NL + "public class ";
  protected final String TEXT_5 = " {" + NL + "\t@Inject" + NL + "\tIEventBroker eventBroker;" + NL + "\t" + NL + "\t@PostConstruct" + NL + "\tvoid hookListeners() {" + NL + "\t\t// Hook event listeners" + NL + "\t}" + NL + "\t" + NL + "\t@PreDestroy" + NL + "\tvoid unhookListeners() {" + NL + "\t\t// Unhook event listeners" + NL + "\t}" + NL + "}";

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
    return stringBuffer.toString();
  }
}
