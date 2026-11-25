package standalone.protocols.bundleentry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.eclipse.core.runtime.Platform;

/**
 * Handler for bundleentry: URLs, found by the JVM through the
 * java.protocol.handler.pkgs system property (value: standalone.protocols).
 */
public class Handler extends URLStreamHandler {
    protected URLConnection openConnection(URL url) throws IOException {
        File root = Platform.getBundleDirectory(url.getHost());
        if (root == null) {
            throw new FileNotFoundException(url.toString());
        }
        return new File(root, url.getPath()).toURI().toURL().openConnection();
    }
}
