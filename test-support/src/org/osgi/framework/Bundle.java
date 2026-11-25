package org.osgi.framework;

import java.net.URL;

/** Minimal stand-in for the OSGi interface: the tests only look up bundle entries. */
public interface Bundle {
    URL getEntry(String path);
}
