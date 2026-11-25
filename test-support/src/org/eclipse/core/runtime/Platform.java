package org.eclipse.core.runtime;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.osgi.framework.Bundle;

/**
 * Minimal stand-in for the Eclipse class: a "bundle" is a directory named after it,
 * looked up in the directories listed in the bundle.dirs system property.
 */
public final class Platform {
    private Platform() {
    }

    /** Returns the directory of a bundle, or null if there is none. */
    public static File getBundleDirectory(String symbolicName) {
        String dirs = System.getProperty("bundle.dirs", "");
        for (String dir : dirs.split(File.pathSeparator)) {
            File root = new File(dir, symbolicName);
            if (root.isDirectory()) {
                return root;
            }
        }
        return null;
    }

    public static Bundle getBundle(final String symbolicName) {
        final File root = getBundleDirectory(symbolicName);
        if (root == null) {
            return null;
        }
        return new Bundle() {
            public URL getEntry(String path) {
                if (!new File(root, path).exists()) {
                    return null;
                }
                try {
                    // As in OSGi, the bundle is the root of the URL: the tests rely on that
                    // when they resolve absolute paths against the URL of a document.
                    return new URL("bundleentry", symbolicName, path.startsWith("/") ? path : "/" + path);
                } catch (MalformedURLException e) {
                    return null;
                }
            }
        };
    }
}
