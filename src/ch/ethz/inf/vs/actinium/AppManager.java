package ch.ethz.inf.vs.actinium;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.inf.vs.actinium.cfg.AppConfig;
import ch.ethz.inf.vs.actinium.cfg.AppType;
import ch.ethz.inf.vs.actinium.cfg.Config;
import ch.ethz.inf.vs.actinium.plugnplay.AbstractApp;
import ch.ethz.inf.vs.actinium.plugnplay.JavaScriptApp;

/**
 * The AppManager is the connection between all resources that deal with apps
 * and their instances. Apps are installed at InstallResource and a
 * InstalledAppResource is created that represents an app of which one can
 * create a running instance.
 * <p>
 * On a POST request to such a InstalledAppResource, the AppManager creates a
 * new instance of the choosen app. It creates a new AppConfig with the
 * specified properties from the request's payload. Then, AppManager creates a
 * new app (method createApp(AppConfig)) with this AppConfig. This is the only
 * location on the app server, where the type of an app matters, thus, whether
 * it is a JavaScrip app or anything else. If another type comes up, this method
 * must be overriden to recognize the new type and create another AbstractApp
 * than a JavaScriptApp.
 * <p>
 * After an app instance has been created it is available, ready to start, stop
 * and restart. The AppManager then passes the new app instance to the
 * AppResource that manages these available app instances.
 * <p>
 * If an InstalledAppResource is updated, i.e. new code is passes by a PUT
 * request to the InstalledAppResource and stored to disk, the AppManager makes
 * the AppResource restart all instances of the updated app. If an app is
 * deleted, the AppManager makes the AppResource remove all instances of this
 * app, together with their configs.
 * <p>
 * AppManager also is the enables the StatsResource to access the apps to
 * retrieve the start and stop time of an app and all their subresources.
 * <p>
 * Since many different resources of the app server with different
 * responsibilities are required to work together, instead of having every
 * component have a reference to every other component, they have a reference to
 * the AppManager, which delegates commands to the component with the required
 * responsibility.
 * <p>
 * The most important components that have a reference to the AppManager:
 * InstalledAppResource (to install new instances of an app to AppResource) and
 * StatsResource (to access app's properties). AppManager has a reference to
 * AppResource (to delegate commands from install components) and StatsResource
 * (to send a notification when an app instance is created or removed).
 * 
 * @author Martin Lanter
 */
public class AppManager {
	
	// the config of the app server
	private Config config;
	
	// AppResource, the root of all running apps related resources
	private AppResource appresource; // possibly null in the beginning
	
	// StatsResource, which holds stats about all apps
	private StatsResource statsresource; // possibly null in the beginning
	
	/**
	 * Contructs an AppManager with the specified properties.
	 * @param config the app server's config
	 */
	public AppManager(Config config) {
		this.config = config;
	}
	
	/**
	 * Set the AppResource
	 * @param appersource the AppResource
	 */
	public void setAppResource(AppResource appersource) {
		this.appresource = appersource;
	}
	
	/**
	 * Set the StatsResource
	 * @param statsresource the StatsResource
	 */
	public void setStatsResource(StatsResource statsresource) {
		this.statsresource = statsresource;
	}

	/**
	 * Scans the folder for app configs for configs, loads them into AppConfigs
	 * and creates the apps.
	 * 
	 * @return the an array of all apps (instances) stored to the disk
	 */
	public List<AbstractApp> loadAllApps() {
		System.out.println("Load apps from disk");
		
		String path = config.getProperty(Config.APP_CONFIG_PATH);
		File dir = new File(path);
		File[] cfgs = dir.listFiles(new ConfigFilenameFilter());

		List<AbstractApp> list = new LinkedList<AbstractApp>();
		for (File cfg:cfgs) {
			try {
				AppConfig appcfg = new AppConfig(cfg);
				AbstractApp app = createApp(appcfg);
				list.add(app);
				
				System.out.println("	loaded "+cfg.getName()+" for app "+app.getName());
			} catch (IOException e) {
				System.err.println("AppManagar can't read app config from file "+cfg+": "+e.getMessage());
			}
		}
		
		return list;
	}
	
	/**
	 * Returns the app with the specified name.
	 * @param appname the name of the app
	 * @return the app with the specified name.
	 */
	public AbstractApp getApp(String appname) {
		return appresource.getApp(appname);
	}
	
	/**
	 * Returns all available apps from AppResource.
	 * @return all available apps from AppResource.
	 */
	public AbstractApp[] getAllApps() {
		return appresource.getAllApps();
	}

	/**
	 * Restarts all available instances of the specified app in AppsResource.
	 * This method is called, when an app has been updated.
	 * 
	 * @param appname the appname of whichs instances are to be restarted.
	 */
	public void restartApps(String appname) {
		appresource.restartApps(appname);
	}

	/**
	 * Stops and deletes all availables instances of the specified app int
	 * AppResource. This method is called, when an app has been deleted.
	 * 
	 * @param appname the appname that is removed.
	 * @throws IOException
	 */
	public void deleteApps(String appname) throws IOException {
		appresource.deleteApps(appname);
		
		if (statsresource!=null)
			statsresource.ondeleteApp(appname);
	}

	/**
	 * Installs a new instance of an app specified by the given AppConfig. First
	 * the AppManager ensures, that name and specified app are valid. If not, a
	 * nIllegalArgumentException is thrown. If they are fine, the AppConfig is
	 * stored to disk, a new AbstractApp is created and added to AppResource and
	 * StatsResource.
	 * 
	 * @param appcfg the AppConfig with the properties for the app
	 * @return the path to the newly created app instance.
	 */
	public String installNewApp(AppConfig appcfg) {
		String configPath = createAppConfigPath(appcfg.getName());
		appcfg.setConfigPath(configPath);

		ensureValidAppPath(appcfg);
		ensureValidName(appcfg);

		appcfg.store();
		
		AbstractApp app = createApp(appcfg);
		appresource.installApp(app);
		
		if (statsresource!=null)
			statsresource.oninstallApp(app.getName());
		
		return app.getPath();
	}
	
	/**
	 * Ensures, that the given AppConfig contains a valid app, i.e. it exists.
	 * @param appcfg the Appconfig to be validated.
	 */
	private void ensureValidAppPath(AppConfig appcfg) {
		String appname = appcfg.getProperty(AppConfig.APP);
		if (appname==null)
			throw new IllegalArgumentException("There is no app specified to be executed (use e.g. app = my_app_name).");
		
		String filename = findApp(appname);
		if (filename==null)
			throw new IllegalArgumentException("There is no app with the specified name "+appname);
		
		appcfg.setProperty(AppConfig.DIR_PATH, config.getProperty(Config.APP_PATH));
//		String apppath = config.getProperty(Config.APP_PATH) + filename;
//		System.out.println("set app path to "+apppath);
//		appcfg.setProperty(AppConfig.PATH, apppath);
	}

	/**
	 * Finds the filename of the app with the specified name in the folder of
	 * apps.
	 * 
	 * @param name the app's name.
	 * @return the path to the app.
	 */
	private String findApp(String name) {
		File appdir = new File(config.getProperty(Config.APP_PATH));
		String[] apps = appdir.list();
		for (String app:apps) {
			int last = app.lastIndexOf('.');
			String filename;
			if (last==-1) filename = app;
			else filename = app.substring(0,last);
			if (filename.equals(name))
				return app;
		}
		return null;
	}

	/**
	 * Ensures that the specified AppConfig contains a valid name for a new app,
	 * i.e. the name is defined and not in use yet.
	 * 
	 * @param appcfg the AppConfig to be validated.
	 */
	private void ensureValidName(AppConfig appcfg) {
		String name = appcfg.getName();
		if (name==null || name.equals(AppConfig.UNNAMED))
			throw new IllegalArgumentException("No name has been specified for the app: "+name);
		
		for (String nm:appresource.getAppsNames()) {
			if (nm.equals(name)) {
				throw new IllegalArgumentException("The name "+name+" is already in use for an app. Please specify a new name");
			}
		}
	}

	/**
	 * Returns the path to the file of the AppConfig with the specified name.
	 * @param name the name of the app instance.
	 * @return the path to the config file of the app instance.
	 */
	private String createAppConfigPath(String name) {
		String path = config.getProperty(Config.APP_CONFIG_PATH);
		String prefix = config.getProperty(Config.APP_CONFIG_PREFIX); 
		String suffix = config.getProperty(Config.APP_CONFIG_SUFFIX);
		return path + prefix + name + suffix;
	}

	/**
	 * Creates a new instance of the app specified in the given AppConfig.
	 * Basically this is the only location in the app server, where the type of
	 * an app matters. If the type of the specified AppConfig is JavaScript the
	 * manager creates a new JavaScriptApp. To support more types, this method
	 * must recognize them and create another kind of AbstractApp.
	 * 
	 * @param appcfg the properties for the new app instance.
	 * @return an AbstractApp with the specified properties. 
	 */
	private AbstractApp createApp(AppConfig appcfg) {
		String type = appcfg.getProperty(AppConfig.TYPE);
		if (type==null)
			throw new NullPointerException("App config "+appcfg+" returned null as app type");
		
		if (type.equals(AppType.JAVASCRIPT)) {
			return new JavaScriptApp(appcfg);
		} else {
			throw new RuntimeException("App config "+appcfg+" retuned an unknown type: "+type);
		}
	}
	
	/**
	 * This Filename filter accepts all configuration files for apps, e.g. whose
	 * names start with the correct prefix and end with the correct suffix.
	 */
	private class ConfigFilenameFilter implements FilenameFilter {
		@Override
		public boolean accept(File file, String name) {
			return name.toLowerCase().startsWith(config.getProperty(Config.APP_CONFIG_PREFIX))
				&& name.toLowerCase().endsWith(config.getProperty(Config.APP_CONFIG_SUFFIX));
		}
	}
}
