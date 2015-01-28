package wt;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.apache.tools.ant.taskdefs.ImportTask;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import wt.ant.AntFacade;
import wt.exception.InvalidConfigurationException;
import wt.exception.WTScriptInvalidException;
import wt.measure.IMeasureCenter;
import wt.measure.IMeasureCenterKeeper;
import wt.measure.IStatusCenter;
import wt.measure.IStatusCenterKeeper;
import wt.util.FileSystemHelper;
import wt.util.IExceptionHandler;
import wt.util.WebtestHelper;
import wt.webtest.WTWebtestCustomizer;

/**
 *
 * <p>WTContainer class prepares & holds all necessary ant configuration for executing web tests and retrieving measures.</p>
 *
 * @author ardeshir.arfaian
 * @date 17.07.2008
 *
 */
public class WTContainer implements IMeasureCenterKeeper, IStatusCenterKeeper {

	/** measure center reference */
	private IMeasureCenter measureCenter;
	/** status center reference */
	private IStatusCenter statusCenter;

	/** Configuration of this WTContainer */
	private WTContainerConfig wtContainerConfig;

	/** facade to ant API */
	private AntFacade antFacade = null;
	/** ant project object */
	private Project project = null;
	/** ant import task */
	private ImportTask importTask;

	/** environment logger */
	private static final Logger logger = Logger.getLogger(WTContainer.class.getName());

	/**
	 * Ctor.
	 *
	 * @param wtContainerConfig
	 */
	public WTContainer(){

	}

	/**
	 * Setter. See {@link WTContainerConfig}.
	 *
	 * @param config container configuration
	 */
	public void setConfig(WTContainerConfig config) {
		this.wtContainerConfig = config;
	}

	public WTContainerConfig getConfig() {
		return this.wtContainerConfig;
	}

	/**
	 * Sets necessary user properties in ant script for container.
	 */
	public void setContainerProperties() {
		//set script directory
		project.setUserProperty("webtest.home", wtContainerConfig.getScriptdir().getAbsolutePath());

		//set head-less mode
		project.setUserProperty("wt.headless", "true");

		//skip generation of DTD
		project.setUserProperty("wt.generateDtd.skip", "true");

		//skip generation of definitions (not used)
		project.setUserProperty("wt.generateDefinitions.skip", "true");

		//skip generation of HTML reports
		project.setUserProperty("~wt.htmlReports.skip", "true");

		//set host & port to run Web Transaction against
		project.setUserProperty("host", wtContainerConfig.getHost());
		project.setUserProperty("port", "" + wtContainerConfig.getPort());

	}

	/**
	 * Initializes WebTest framework and prepares ant object structure resulting from current
	 * WebTest script.
	 *
	 * @throws WTScriptInvalidException
	 */
	@SuppressWarnings("unchecked")
	private boolean init() throws WTScriptInvalidException, IOException, SAXException {

		//prepare and set step execution listener factory
//		StepExecutionListenerConfig listenerConfig = new StepExecutionListenerConfig();


//		WTStepExecutionListenerFactory factory = new WTStepExecutionListenerFactory();
//		factory.setStepExecutionListenerConfig(listenerConfig);
//		factory.setIMeasureCenter(measureCenter);
//		factory.setIStatusCenter(statusCenter);
		project.addReference("wt.webtestCustomizer", new WTWebtestCustomizer(measureCenter, statusCenter, wtContainerConfig.getProtocol(), wtContainerConfig.isLogContent()));


		//prepare ant + set build file
        project.setBaseDir(wtContainerConfig.getBasedir());

        String script = WebtestHelper.complementScript(wtContainerConfig.getScript());
        InputSource source = WebtestHelper.prepareTestScript(script);

        if(WebtestHelper.validateScript(source)){

	        //prepare project helper
	        ProjectHelper2 helper = new ProjectHelper2();
	        project.addReference("ant.projectHelper", helper);
	        helper.getImportStack().addElement(source);

	        //prepare ant context
	        antFacade  = new AntFacade(project);
	        AntXMLContext context = antFacade.prepareAntXMLContext();
	        context.setCurrentTargets(new HashMap<String,Target>());
	        WebtestHelper.parseScript(context, source);

	        return true;

        } else {

        	throw new WTScriptInvalidException("Provided test script was invalid or not well formed.");
        }
	}


	/**
	 * Method sets up web transactions (WTs) &amp; container.
	 *
	 * @param status
	 * @return
	 */
	public boolean setupTransactions(){

		try{
			if(wtContainerConfig == null)
				throw new Exception("Web Transaction Monitor Configuration was null");

			project = new Project();

			FileSystemHelper.preInit(wtContainerConfig);
			if(FileSystemHelper.checkPreConditions(wtContainerConfig)){

				if(init()) {
			        setContainerProperties();

					//send build started event + init project
					project.init();

					//configure proxy
					wtContainerConfig.configureProxy(antFacade);

					//import webtest functionality
					Target dummy = antFacade.createTarget("");
					importTask = antFacade.createImportTask(wtContainerConfig.getWebtestxml().getAbsolutePath(), dummy);
					importTask.execute();

					return true;
				}
			}
		} catch(Throwable t) {
			IExceptionHandler exceptionHandler = statusCenter.getExceptionHandler();
			if(exceptionHandler.isExceptionHandledByContainer(t) || !exceptionHandler.isExceptionHandledByListener(t)){
				exceptionHandler.handleException(t, logger);
			}
		}

		return false;
	}

	/**
	 * Runs all available web transactions (WTs).
	 *
	 * @param status Status object returned to Client
	 */
	public boolean runTransactions() {
		try{
			if(project != null) {

				project.executeTarget("wt.defineTasks");
				WebtestHelper.configureWebtestTasks(project, wtContainerConfig);
				project.executeTarget("wt.full");

				return true;

			} else {
				throw new InvalidConfigurationException("WebTransaction Container was not configured properly -> configure transactions!");
			}
		}catch(Throwable t){

			IExceptionHandler exceptionHandler = statusCenter.getExceptionHandler();
			if(exceptionHandler.isExceptionHandledByContainer(t) || !exceptionHandler.isExceptionHandledByListener(t)){
				exceptionHandler.handleException(t, logger);
			}

		}

		return false;

	}

	public void teardownTransactions() {
		//StepExecutionListenerFactoryProvider.getInstance().removeFactory(project);
	}

	@Override
	public IMeasureCenter getIMeasureCenter() {
		return measureCenter;
	}

	@Override
	public void setIMeasureCenter(IMeasureCenter measureCenter) {
		this.measureCenter = measureCenter;
	}

	@Override
	public IStatusCenter getIStatusCenter() {
		return statusCenter;
	}

	@Override
	public void setIStatusCenter(IStatusCenter statusCenter) {
		this.statusCenter = statusCenter;
	}

}
