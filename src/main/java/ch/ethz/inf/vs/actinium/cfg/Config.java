package ch.ethz.inf.vs.actinium.cfg;

/**
 * The Config contains all properties that hold globally for the whole app
 * server.
 * 
 * @author Martin Lanter
 */
public class Config extends AbstractConfig {

	private static final long serialVersionUID = 1132829953418308387L;

	// global configuration constants
	public static final String PATH = "appserver/actinium.cfg";
	
	// keys
	public static final String APP_CONFIG_PREFIX = "app_config_prefix";
	public static final String APP_CONFIG_SUFFIX = "app_config_suffix";
	public static final String JAVASCRIPT_SUFFIX = "javascript_suffix";
	
	public static final String START_ON_INSTALL = "start_on_install"; // true, if app instances shall start as soon as installed
	
	public static final String APP_PATH = "app_path"; // path to the apps
	public static final String APP_CONFIG_PATH = "app_config_path"; // path to the configs of apps (which containt the filename)
	public static final String APP_CONFIG_RESOURSES = "app_config_resources";

	public static final String APPS_RESOURCE_ID = "apps_resource_id"; // identifier of AppResource
	public static final String CONFIG_RESOURCE_ID = "config_resource_id"; // identifier of this config's resource
	public static final String INSTALL_RESOURCE_NAME = "install_resource_id"; // identifier of InstallResource
	public static final String RUNNING_RESOURCE_ID = "running_resource_id"; // identifier of RunningResource
	public static final String STATS_RESOURCE_ID = "stats_resource_id"; // identifier of StatsResource
	
	/**
	 * Constructs a new Config from the default path
	 */
	public Config() {
		this(PATH);
	}
	
	/**
	 * Constructs a new Config from the specified path
	 * @param path
	 */
	public Config(String path) {
		super(path);
		init();
		loadProperties(path);
	}
	
	private void init() {
		setProperty(APP_CONFIG_PREFIX, "config_");
		setProperty(APP_CONFIG_SUFFIX, ".cfg");
		
		setProperty(START_ON_INSTALL, false);
		setProperty(APP_PATH, "appserver/installed/");
		setProperty(APP_CONFIG_PATH, "appserver/instances/");
		setProperty(JAVASCRIPT_SUFFIX, ".js");
		
		setProperty(CONFIG_RESOURCE_ID, "config");
		setProperty(STATS_RESOURCE_ID, "stats");
		setProperty(INSTALL_RESOURCE_NAME, "install");
		setProperty(APPS_RESOURCE_ID, "installed");
		setProperty(APP_CONFIG_RESOURSES, "instances");
		setProperty(RUNNING_RESOURCE_ID, "apps");
	}
}
