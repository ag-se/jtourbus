package de.inffub.jtourbus.plugin;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class. It does not host any functionality but to take care of
 * the initial interaction between Eclipse platform and the plugin.
 */
public class JTourBusPlugin extends AbstractUIPlugin {
    // The shared instance.
    private static JTourBusPlugin plugin;

    // Resource bundle.
    private ResourceBundle resourceBundle;

    /**
     * The constructor.
     */
    public JTourBusPlugin() {
        super();
        plugin = this;
    }

    /**
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
        plugin = null;
        resourceBundle = null;
    }

    /**
     * Returns the shared instance.
     */
    public static JTourBusPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = JTourBusPlugin.getDefault().getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        try {
            if (resourceBundle == null)
                resourceBundle = ResourceBundle
                        .getBundle("treeViewPlugin.TreeViewPluginPluginResources");
        } catch (MissingResourceException x) {
            resourceBundle = null;
        }
        return resourceBundle;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR,
                "TreeViewPlugin", 0, "TreeViewPlugin.internal_error", e)); //$NON-NLS-1$
    }

}
