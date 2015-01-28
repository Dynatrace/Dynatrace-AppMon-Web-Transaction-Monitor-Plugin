package wt.ant;

import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.helper.AntXMLContext;
import org.apache.tools.ant.taskdefs.ImportTask;
import org.apache.tools.ant.taskdefs.optional.net.SetProxy;

public class AntFacade {
	
	private Project project;
	
	/**
	 * Ctor.
	 * 
	 * @param project ant project file
	 */
	public AntFacade(Project project) {
		this.project = project;
	}
	
	/**
	 * Creates ant ImportTask.
	 * 
	 * @param file URI of file to import.
	 * @param owningTarget owner of created ImportTask
	 * @return {@link ImportTask}
	 * @see created ImportTask
	 */
	public ImportTask createImportTask(String file, Target owningTarget){
		ImportTask result = new ImportTask();
		
		result.setFile(file);
		result.setProject(project);
		result.setOwningTarget(owningTarget);
		result.setProject(project);
		result.setLocation(new Location(file));
		
		return result;
	}

	/**
	 * Method creates SetProxy ant task configured for socks proxy.
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public SetProxy createSetProxySocks(String host, int port){
		SetProxy result = new SetProxy();
		
		result.setSocksProxyHost(host);
		result.setSocksProxyPort(port);
		
		return result;
	}

	/**
	 * Method creates SetProxy ant task configured for http proxy.
	 * 
	 * @param host proxy server hostname
	 * @param port proxy server port
	 * @return created SetProxy task
	 * @see SetProxy
	 */
	public SetProxy createSetProxyHttp(String host, int port) {
		return createSetProxyHttp(host, port, "", "");
	}

	/**
	 * Method creates SetProxy ant task configured for http proxy requiring authentication.
	 * 
	 * @param host proxy server hostname
	 * @param port proxy server port
	 * @param user proxy server username
	 * @param password proxy server password
	 * @return created SetProxy task
	 * @see SetProxy
	 */
	public SetProxy createSetProxyHttp(String host, int port, String user, String password) {
		SetProxy result = new SetProxy();
		
		result.setProject(project);
		result.setProxyHost(host);
		result.setProxyPort(port);
		result.setProxyUser(user);
		result.setProxyPassword(password);

		return result;
	}
	
	/**
	 * Method creates target in project with argument name.
	 * 
	 * @param name name of target (empty means top-level target)
	 * @return created target
	 */
	public Target createTarget(String name) {
		Target result = new Target();
		
		result.setName(name);
		result.setProject(project);
		
		return result;
	}
	
	/**
	 * Method creates and prepares AntXMLContext.
	 * 
	 * @return prepared AntXMLContext
	 */
	public AntXMLContext prepareAntXMLContext(){
		AntXMLContext context = null;
        context = (AntXMLContext) project.getReference("ant.parsing.context");
        if (context == null) {
            context = new AntXMLContext(project);
            project.addReference("ant.parsing.context", context);
            project.addReference("ant.targets", context.getTargets());
        }
        
        return context;
	}

}
