package hudson.os;

import jenkins.util.SystemProperties;
import jnr.constants.platform.Errno;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import jnr.posix.util.DefaultPOSIXHandler;

/**
 * POSIX API wrapper.
 *
 * @author Kohsuke Kawaguchi
 */
public class PosixAPI {

    private static POSIX posix;
    private static /* Script Console modifiable */ boolean VERBOSE = SystemProperties.getBoolean(PosixAPI.class.getName() + ".verbose");

    /**
     * Load the JNR implementation of the POSIX APIs for the current platform. Runtime exceptions
     * will be of type {@link PosixException}. {@link IllegalStateException} will be thrown for
     * methods not implemented on this platform.
     *
     * @return some implementation (even on Windows or unsupported Unix)
     */
    public static synchronized POSIX jnr() {
        if (posix == null) {
            posix = POSIXFactory.getPOSIX(new DefaultPOSIXHandler() {
                @Override
                public void error(Errno error, String extraData) {
                    throw new PosixException("native error " + error.description() + " " + extraData);
                }

                @Override
                public void error(Errno error, String methodName, String extraData) {
                    throw new PosixException("native error calling " + methodName + ": " + error.description() + " " + extraData);
                }

                @Override
                public boolean isVerbose() {
                    return VERBOSE;
                }
            }, true);
        }
        return posix;
    }
}
