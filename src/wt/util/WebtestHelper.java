package wt.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2.MainHandler;
import org.apache.tools.ant.helper.ProjectHelper2.RootHandler;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import wt.WTContainerConfig;
import wt.exception.WTScriptInvalidException;
import wt.webtest.WebTestConfigWrapper;

import com.canoo.webtest.ant.WebtestTask;
import com.canoo.webtest.reporting.RootStepResult;
import com.canoo.webtest.reporting.StepResult;

public class WebtestHelper {

//	public boolean validateXML() throws IOException, SAXException{
//	XMLReader parser = null;
//	String XMLDocumentUri = "";
//	String SchemaURI = "";
//	parser.setFeature("http://xml.org/sax/features/validation", true);
//	parser.setFeature("http://apache.org/xml/features/validation/schema", true);
//	parser.setFeature("http://apache.org/xml/features/validation/schema-full-checking",true);
//	parser.setProperty("http://apache.org/xml/properties/schema/ external-schemaLocation",SchemaURI); 
//	parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",true);
//	//Validator handler = new Validator();
//	//parser.setErrorHandler(handler);
//	parser.parse(XMLDocumentUri);
//	
//	return false;
//}
	
	private static final Logger logger = Logger.getLogger(WebtestHelper.class.getName());
	
	private static final Object WEBTEST_TASK_NAME = "webtest";
	
	/**
	 *  Wraps serialized XML with Stream. 
	 * 
	 * @param xmlString XML serialized as String
	 * @return InputSource argument xmlString wrapped in stream 
	 */
	public static InputSource prepareTestScript(String xmlString) {
		//prepare test script
		StringReader reader = new StringReader(xmlString);
	    return new InputSource(reader);
	}
	
	/**
	 * Parses ant file respectively test script from XML String.
	 * 
	 * @param parser {@link XMLReader}
	 * @param context {@link AntXMLContext}
	 * @param source wrapped XML String (wrapped as stream)
	 */
	private static void parseXmlString(XMLReader parser, AntXMLContext context, InputSource source) throws IOException, SAXException {
	    DefaultHandler hb = new RootHandler(context, new MainHandler());

        parser.setContentHandler(hb);
        parser.setEntityResolver(hb);
        parser.setErrorHandler(hb);
        parser.setDTDHandler(hb);
		parser.parse(source);
	}
	
	/**
	 * Parses WebTest Script (ant build file) from wrapped XML String.
	 * 
	 * @param context {@link AntXMLContext}
	 * @param source wrapped XML String (wrapped as stream)
	 */
	public static void parseScript(AntXMLContext context, InputSource source) throws IOException, SAXException {
		XMLReader parser = JAXPUtils.getNamespaceXMLReader();
		
		parseXmlString(parser, context, source);
	}

	public static boolean validateScript(InputSource source) {
		// TODO (nice to have) add additional script validation
		return true;
	}

	public static String complementScript(String source) throws WTScriptInvalidException {
	    StringBuilder result = new StringBuilder();

	    //check if web transaction script was received and not empty
		if(source == null || "".equals(source.trim())){
			throw new WTScriptInvalidException("The provided web transaction script was empty / null.");
	    }

		
		if (!source.contains("<?xml ")){
			result.append("<?xml version=\"1.0\"?>\n");
		}
		
		if(!source.contains("<project")){
			result.append("<project name=\"defaultProject"+System.currentTimeMillis()+"\">\n");
		}
		
		if(!source.contains("<target")){
			result.append("<target name=\"wt.testInWork\">\n");
		} else {
			int index = source.indexOf("<target");
			
			String tmp1 = source.substring(0, index);
			String tmp2 = source.substring(source.indexOf('>',index));
			
			result.append(tmp1);
			result.append("<target name=\"wt.testInWork\">\n");
			result.append(tmp2);
			
			System.err.println(result.toString());
		}
		
		if(!source.contains("<webtest")){
			result.append("<webtest name=\"defaultWebtest"+System.currentTimeMillis()+"\">\n");
		}
		
		result.append(source);
		
		if(!source.contains("<webtest")){
			result.append("</webtest>\n");
		}

		if(!source.contains("<target")){
			result.append("</target>\n");
		}
		
		if(!source.contains("<project")){
			result.append("</project>\n");
		}
		return result.toString();
	}
	
	
	/**
	 * Configures all WebtestTasks with same WTMonitor configuration. One WebtestTask represents one web transaction.
	 */
	@SuppressWarnings("unchecked")
	public static void configureWebtestTasks(Project project, WTContainerConfig wtMonitorConfig) {
		Collection<Target> targets = project.getTargets().values();
		if(targets == null)
			return;
				
		for(Iterator<Target> iter = targets.iterator(); iter.hasNext();){
			Target tmpTarget = (Target)iter.next();

			Task[] tmpTasks = tmpTarget.getTasks();
			for(Task task : tmpTasks){
				if(WEBTEST_TASK_NAME.equals(task.getTaskType())){
					try {
						task.maybeConfigure();
						WebtestTask tmp = (WebtestTask)((UnknownElement)task).getRealThing();
						tmp.setProject(project);
						
						WebTestConfigWrapper configWrapper = new WebTestConfigWrapper(tmp.getConfig());
						configWrapper.configureWebtestTask(wtMonitorConfig);
					}catch(Exception e) {
						if(logger.isLoggable(Level.SEVERE)) {
							logger.severe(e.getLocalizedMessage());
						}
					}
				}
			}
		}
	}
	
	
	public static int getNumberOfSubstepsRecursive(final StepResult stepResult) {
		
		int result = 0;
		
		if(!stepResult.getChildren().isEmpty()) {
			result = stepResult.getChildren().size();
			
			for(Object obj : stepResult.getChildren()){
				StepResult tmp = (StepResult) obj;
				result += getNumberOfSubstepsRecursive(tmp);
			}
		}
		
		return result;
	}
	
	public static String getFailedStepRecursive(final StepResult stepResult, String prefix) {
		int i = 0;
		
		for(Object tmp : stepResult.getChildren()) {
			StepResult tmpResult = (StepResult)tmp;
			i++;
			
			String str = (prefix.length() > 0) ? prefix + "." + i : "" + i ;
			
			
			if(tmpResult.isCompleted() && !tmpResult.isSuccessful()) {
				if( !"condition".equals(tmpResult.getTaskName()) && !"not".equals(tmpResult.getTaskName()) ){
					//current step or one of it's substeps has failed
			
					if(!tmpResult.getChildren().isEmpty()) { //hasChildren -> child failed
						return getFailedStepRecursive(tmpResult, str);
					} else { //no children -> current step failed
						return str;
					}
				
				}
			}
			
		}
		
		return null;
	}
	
	public static String getStepResultSummary(String transactionName, final RootStepResult rootResult) {
		StringBuilder summary = new StringBuilder();
		
		summary.append("\n-----------------------\n");
		summary.append(transactionName);
		summary.append(" overview:\n");
		
		//number of major steps
		summary.append(rootResult.getChildren().size());
		summary.append(" major steps\n");
		
		//number of all steps
		summary.append(getNumberOfSubstepsRecursive(rootResult));
		summary.append(" overall steps\n");
		
		//success of steps
		if(rootResult.getFailingTaskResult()==null) {
			summary.append("All steps successful\n");
		} else {
			summary.append("Step ");
			summary.append(getFailedStepRecursive(rootResult, ""));
			summary.append(" failed\n");
		}
		
		summary.append("-----------------------\n\n");
		
		return summary.toString();
	}
	
	public static String printStep(int currentStepNr, int nrOfSteps, String prefix, String timerName, String taskName, String status) {
		StringBuilder result = new StringBuilder();
		
		result.append(prefix);
		if(prefix.length()>0)
			result.append(".");
		result.append(currentStepNr);
		result.append(", ");
		if(timerName != null && !"".equals(timerName)){
			result.append(timerName);
		} else {
			result.append("- ");
		}
		result.append(": ");
		result.append(taskName);
		result.append(" ");
		result.append(status);
		result.append("\n");
		
		return result.toString();
	}
	
	public static long getThinkTimeRecursive(final StepResult stepResult) {
		long thinkTime = 0;
		
		for(Object tmp : stepResult.getChildren()) {
			
			StepResult tmpResult = (StepResult)tmp;
			
			if("sleep".equals(tmpResult.getTaskName())){
				thinkTime += tmpResult.getDuration();
			}
			
			if(!tmpResult.getChildren().isEmpty()) {
				thinkTime += getThinkTimeRecursive(tmpResult);
				
			}
		}
		
		return thinkTime;
	}
}
