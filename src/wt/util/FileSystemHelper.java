package wt.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import wt.WTContainer;
import wt.WTContainerConfig;
import wt.exception.FileNotAccessibleException;
import wt.exception.RessourceNotAvailableException;

public class FileSystemHelper {

	private static final String basedirStr = "/res";
	private static final String webtestxmlStr = "/res/webtest.xml";
	private static final String scriptdirStr = "/";
	

	
	/**
	 * Checks access for necessary files &amp; directories.
	 */
	public static boolean checkPreConditions(WTContainerConfig wtMonitorConfig) throws FileNotAccessibleException {
		if(!wtMonitorConfig.getBasedir().exists()) {
			throw new FileNotAccessibleException("Can't find base directory: \"" + wtMonitorConfig.getBasedir() + "\"");
		}
		
		if(!wtMonitorConfig.getBasedir().canRead()){
			throw new FileNotAccessibleException("Need read access to basedir: \"" + wtMonitorConfig.getBasedir() + "\"");
		}
		
		if(!wtMonitorConfig.getScriptdir().exists()) {
			throw new FileNotAccessibleException("Can't find script directory: \"" + wtMonitorConfig.getScriptdir() + "\"");
		}
		
		if(!wtMonitorConfig.getScriptdir().canRead()) {
			throw new FileNotAccessibleException("Need read access to scriptdir: \"" + wtMonitorConfig.getScriptdir() + "\"");
		}
		
		if(!wtMonitorConfig.getWebtestxml().exists()) {
			throw new FileNotAccessibleException("Can't find webtest.xml: \"" + wtMonitorConfig.getWebtestxml() + "\"");
		}
		
		if(!wtMonitorConfig.getWebtestxml().canRead()) {
			throw new FileNotAccessibleException("Need read access to webtest.xml: \"" + wtMonitorConfig.getWebtestxml() + "\"");
		}
		
		return true;
	}
	
	public static boolean preInit(WTContainerConfig wtMonitorConfig) throws RessourceNotAvailableException, URISyntaxException, IOException {
		URL tmp = WTContainer.class.getResource(basedirStr);
		URL fileURL = null;
		
		if(tmp!=null) {
			fileURL = org.eclipse.core.runtime.FileLocator.toFileURL(tmp);
			if(fileURL != null) {
				wtMonitorConfig.setBasedir(new File(fileURL.getFile()));
				if(wtMonitorConfig.getBasedir() == null) {
					throw new RessourceNotAvailableException("Directory \""+basedirStr+"\" was null, URL was: " + tmp);
				}
			} else {
				throw new RessourceNotAvailableException("File URL to directory \""+basedirStr+"\" was null, URL was: " + tmp);
			}
		} else {
			throw new RessourceNotAvailableException("URL to directory \""+basedirStr+"\" was null.");
		}
	
		tmp = WTContainer.class.getResource(webtestxmlStr);
		if(tmp!=null) {
			fileURL = org.eclipse.core.runtime.FileLocator.toFileURL(tmp);
			if(fileURL != null) {
				wtMonitorConfig.setWebtestxml(new File(fileURL.getFile()));
				if(wtMonitorConfig.getWebtestxml() == null) {
					throw new RessourceNotAvailableException("File \""+webtestxmlStr+"\" was null, URL was " + tmp);
				}
			} else {
				throw new RessourceNotAvailableException("File URL to file \""+webtestxmlStr+"\" was null, URL was: " + tmp);
			}
		} else {
			throw new RessourceNotAvailableException("URL to file \""+webtestxmlStr+"\" was null.");
		}
	
		tmp = WTContainer.class.getResource(scriptdirStr);
		if(tmp!=null) {
			fileURL = org.eclipse.core.runtime.FileLocator.toFileURL(tmp);
			if(fileURL != null) {
				wtMonitorConfig.setScriptdir(new File(fileURL.getFile()));
				if(wtMonitorConfig.getScriptdir() == null) {
					throw new RessourceNotAvailableException("Directory \""+scriptdirStr+"\" was null, URL was: " + tmp);
				}
			} else {
				throw new RessourceNotAvailableException("File URL to directory \""+scriptdirStr+"\" was null, URL was: " + tmp);
			}
		} else {
			throw new RessourceNotAvailableException("URL to directory \""+scriptdirStr+"\" was null.");
		}
		
		return true;
	}
}
