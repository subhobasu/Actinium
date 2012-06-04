package ch.ethz.inf.vs.actinium.plugnplay;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import ch.ethz.inf.vs.californium.endpoint.Resource;

/**
 * JavaScriptStatisAccess defines global functions for JavaScript.
 * 
 * @author Martin Lanter
 */
public class JavaScriptStaticAccess {

	public static void dump(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        for (int i=0;i<args.length;i++) {
            if (i==0)
            	System.out.print("	app: ");
            else
                System.out.print(" ");

            // Convert the arbitrary JavaScript value into a string form.
            String s = Context.toString(args[i]);
            System.out.print(s);
        }
        System.out.println();
    }
	
    public static void addSubResource(Context cx, Scriptable thisObj, Object[] args, Function funObj) {
    	if (args.length<2)
    		throw new IllegalArgumentException("Invalid call to addSubResource. Must have the parent resource as first argument and the child resource as second");
    	
    	if ( !(args[0] instanceof Resource) || !(args[1] instanceof Resource))
    		throw new IllegalArgumentException("Invalid call to addSubResource. The two arguments must be of type Resource");
    	
    	Resource parent = (Resource) Context.jsToJava(args[0],Resource.class);
    	Resource child = (Resource) Context.jsToJava(args[1],Resource.class);
    	parent.add(child);	    	
    }
}
