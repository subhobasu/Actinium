package ch.ethz.inf.vs.actinium.cfg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Observable;
import java.util.Properties;

import ch.ethz.inf.vs.californium.coap.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.DELETERequest;
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.endpoint.LocalResource;
import ch.ethz.inf.vs.californium.endpoint.Resource;

/**
 * AbstractConfig is a Properties which also has tha capabilities of a CoAP
 * resource and an Observable.
 * <p>
 * On a GET request, it returns all properties specified in this configuration.
 * On a POST request, it changes the properties from the request's payload
 * accordingly if possible. On a PUT request, it replaces all properties with
 * the properties from the request's payload, where possible. DELETE requests
 * are not allowed if not implemented by a subclass.
 * <p>
 * Some properties cannot be changed from an extern object with
 * setProperties(...). These are the properties with the key for which the
 * method isModifiable(key) return false, which should be overriden by
 * subclasses.
 * <p>
 * To change one or more properties they must be sent by a POST request and the
 * payload must be of the form "[key] = [value]" for all properties to be
 * changed. E.g. to change the property "name" to the value "myname" send a POST
 * request with "name = myname" as payload.
 * <p>
 * If properties are changed through a POST request or a call to
 * setPropertiesAndNotify(...) AbstractConfig notifies all its observers. As
 * argument for their update method an instance of ConfigChangeSet is used,
 * which contains all keys, that have been changed.
 * 
 * @author Martin Lanter
 */
public abstract class AbstractConfig extends Properties {

	/*
	 * Resource for this config. First need to be created with an identifier
	 */
	private LocalResource cfgres = null;
	
	// The path, where this config shall be stored to if not meantioned otherwise
	private String configPath;
	
//	private boolean changed;
	
	private ConfigObvervable observable;
	
	public AbstractConfig() {
		super();
		this.cfgres = null;
		this.observable = new ConfigObvervable();
	}
	
	public AbstractConfig(String configPath) {
		this();
		this.configPath = configPath;
	}
	
//	public void setChanged() {
//		setChanged(true);
//	}
	
	public LocalResource createConfigResource(String identifier) {
		this.cfgres = new ConfigResource(identifier.toLowerCase());
		return cfgres;
	}
	
	public Resource getConfigResource() {
		return cfgres;
	}
	
//	public void setChanged(boolean bool) {
//		this.changed = bool;
//	}
//
//	public boolean hasChanged() {
//		return changed;
//	}
	
	public Observable getObservable() {
		return observable;
	}
	
	protected void fireNotification(String... changes) {
		fireNotification(new ConfigChangeSet(changes));
	}
	
	protected void fireNotification(ConfigChangeSet changes) {
		observable.fireNotification(changes);
	}
	
	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}
	
	public void setPropertyAndNotify(String... props) {
		ConfigChangeSet changes = new ConfigChangeSet();
		for (int i=0;i<props.length-1;i+=2) {
			setProperty(props[i], props[i+1]);
			changes.add(props[i]);
		}
		fireNotification(changes);
	}
	
	public void setProperty(String key, int value) {
		setProperty(key, String.valueOf(value));
	}
	
	public void setProperty(String key, boolean value) {
		setProperty(key, String.valueOf(value));
	}
	
	public int getInt(String key) {
		String value = getProperty(key);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				System.err.println("Invalid integer property: "+key+"="+value);
			}
			
		} else {
			System.err.println("Undefined integer property: "+key);
		}
		return 0;
	}
	
	public boolean getBool(String key) {
		String value = getProperty(key);
		if (value != null) {
			try {
				return Boolean.parseBoolean(value);
			} catch (NumberFormatException e) {
				System.err.println("Invalid boolean property: "+key+"="+value);
			}
			
		} else {
			System.err.println("Undefined boolean property: "+key);
		}
		return false;
	}
	
	public void store() {
		System.out.println("Store config to file "+configPath);
		storeProperties(configPath);
//		changed = false;
	}
	
	public void deleteConfig() throws IOException {
		File file = new File(configPath);
		if (!file.exists())
			throw new IOException("The config file "+configPath+" doesn't exist on the disk");
		
		if (!file.canWrite())
			throw new IOException("The config file "+configPath+" is not writable/deletable");
		
		System.out.println("Delete config file "+configPath);
		boolean success = file.delete();
		
		if (!success)
			throw new IOException("The config file "+configPath+" couldn't be deleted. Make sure, no other process is accessing it");
		
		cfgres.remove();
	
	}
	
	protected void loadProperties(String path) {
		try {
			FileInputStream stream = new FileInputStream(path);
//			InputStream stream = Config.class.getResourceAsStream(path);
			try {
				load(stream);
			} finally {
				stream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Property file "+path+" not found. Try to restore");
			storeProperties(path);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Unable to load properties from "+path);
		}
	}
	
	protected void storeProperties(String path) {
		try {
			FileOutputStream fos = new FileOutputStream(path); 
			store(fos, null);
			fos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unable to store properties at "+path);
		}
	}
	
//	@Override
//	public synchronized Object put(Object key, Object value) {
//		Object old = super.put(key, value);
//		if (old!=value) {
////			setChanged();
//		}
//		return old;
//	}
	
	public boolean isModifiable(String key) {
		return true;
	}
	
	public void performGET(GETRequest request) {
		Response response = new Response(CodeRegistry.RESP_CONTENT);

		StringBuffer buffer = new StringBuffer();
		buffer.append("App Server Configuration\n");
		for (String key:stringPropertyNames()) {
			buffer.append("	"+key+": "+get(key)+" ("+(isModifiable(key)?"modifiable":"unmodifiable")+")\n");
		}
		buffer.append("stored at "+getConfigPath()+"\n");
		
		response.setPayload(buffer.toString());
		request.respond(response);
	}

	public void performPUT(PUTRequest request) {Properties p = new Properties();
		try {
			StringReader reader = new StringReader(request.getPayloadString());
			p.load(reader);
			
			System.out.println("update config:");
			for (String key:p.stringPropertyNames()) 
				System.out.println("	"+key+" = >"+p.get(key)+"< "+isModifiable(key));
			
//			clear();
			for (String key:this.stringPropertyNames()) {
				if (isModifiable(key))
					remove(key);
			}

//			putAll(p);
			ConfigChangeSet changes = new ConfigChangeSet();
			for (String key:p.stringPropertyNames()) {
				if (isModifiable(key)) {
					setProperty(key, p.getProperty(key));
					changes.add(key);
				}
			}
			store();
			fireNotification(changes);
			
			request.respond(CodeRegistry.RESP_CHANGED, "successfully changed keys: "+changes);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Configuration was not able to be parsed");
			request.respond(CodeRegistry.RESP_BAD_REQUEST, "Configuration was not able to be parsed");
		}
	}

	public void performPOST(POSTRequest request) {
		Properties p = new Properties();
		try {
			StringReader reader = new StringReader(request.getPayloadString());
			p.load(reader);
			
			System.out.println("update config:");
			for (String key:p.stringPropertyNames()) 
				System.out.println("	"+key+" = >"+p.get(key)+"< "+(isModifiable(key)?"modifiable":"unmodifiable"));

//			putAll(p);
			ConfigChangeSet changes = new ConfigChangeSet();
			for (String key:p.stringPropertyNames()) {
				if (isModifiable(key)) {
					setProperty(key, p.getProperty(key));
					changes.add(key);
				}
			}
			store();
			fireNotification(changes);
			
			request.respond(CodeRegistry.RESP_CHANGED, "successfully changed keys: "+changes);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Configuration was not able to be parsed");
			request.respond(CodeRegistry.RESP_BAD_REQUEST, "Configuration was not able to be parsed");
		}
	}
	
	// must be here, for that subclasses can override it.
	public void performDELETE(DELETERequest request) {
		request.respond(CodeRegistry.RESP_METHOD_NOT_ALLOWED);
	}

	private class ConfigResource extends LocalResource {

		public ConfigResource(String identifier) {
			super(identifier);
		}
		
		@Override
		public void performGET(GETRequest request) {
			AbstractConfig.this.performGET(request);
		}

		@Override
		public void performPUT(PUTRequest request) {
			AbstractConfig.this.performPUT(request);
		}

		@Override
		public void performPOST(POSTRequest request) {
			AbstractConfig.this.performPOST(request);
		}

		@Override
		public void performDELETE(DELETERequest request) {
			AbstractConfig.this.performDELETE(request);
		}
	}
	
	private class ConfigObvervable extends Observable {
		private void fireNotification(ConfigChangeSet changes) {
			setChanged();
			notifyObservers(changes);
		}
	}
	
	public static class ConfigChangeSet extends HashSet<String> {
		public ConfigChangeSet() {}
		public ConfigChangeSet(String... changes) {
			for (String str:changes)
				add(str);
		}
		public String toString() {
			return Arrays.toString(toArray());
		}
	}
}

