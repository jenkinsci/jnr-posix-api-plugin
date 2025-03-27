package hudson.os;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import hudson.FilePath;
import hudson.Functions;
import hudson.Platform;
import hudson.remoting.Channel;
import hudson.remoting.ChannelBuilder;
import hudson.remoting.FastPipedInputStream;
import hudson.remoting.FastPipedOutputStream;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PosixAPITest {

    @TempDir
    private File temp;

    /**
     * Two channels that are connected to each other, but shares the same classloader.
     */
    private Channel french;

    private Channel british;

    private ExecutorService executors;

    @BeforeEach
    void setUp() throws Exception {
        executors = Executors.newCachedThreadPool();
        final FastPipedInputStream p1i = new FastPipedInputStream();
        final FastPipedInputStream p2i = new FastPipedInputStream();
        final FastPipedOutputStream p1o = new FastPipedOutputStream(p1i);
        final FastPipedOutputStream p2o = new FastPipedOutputStream(p2i);

        Future<Channel> f1 = executors.submit(() -> new ChannelBuilder("This side of the channel", executors)
                .withMode(Channel.Mode.BINARY)
                .build(p1i, p2o));
        Future<Channel> f2 = executors.submit(() -> new ChannelBuilder("The other side of the channel", executors)
                .withMode(Channel.Mode.BINARY)
                .build(p2i, p1o));
        french = f1.get();
        british = f2.get();
    }

    @AfterEach
    void tearDown() throws Exception {
        french.close(); // this will automatically initiate the close on the other channel, too.
        french.join();
        british.join();

        executors.shutdownNow();
    }

    @Test
    void copyToWithPermissionSpecialPermissions() throws Exception {
        assumeFalse(Functions.isWindows() || Platform.isDarwin(), "Test uses POSIX-specific features");
        File tmp = temp;
        File original = new File(tmp, "original");
        FilePath originalP = new FilePath(french, original.getPath());
        originalP.touch(0);
        // Read/write/execute for everyone and setuid.
        PosixAPI.jnr().chmod(original.getAbsolutePath(), 02777);

        File sameChannelCopy = new File(tmp, "sameChannelCopy");
        FilePath sameChannelCopyP = new FilePath(french, sameChannelCopy.getPath());
        originalP.copyToWithPermission(sameChannelCopyP);
        assertEquals(
                02777,
                PosixAPI.jnr().stat(sameChannelCopy.getAbsolutePath()).mode() & 07777,
                "Special permissions should be copied on the same machine");

        File diffChannelCopy = new File(tmp, "diffChannelCopy");
        FilePath diffChannelCopyP = new FilePath(british, diffChannelCopy.getPath());
        originalP.copyToWithPermission(diffChannelCopyP);
        assertEquals(
                00777,
                PosixAPI.jnr().stat(diffChannelCopy.getAbsolutePath()).mode() & 07777,
                "Special permissions should not be copied across machines");
    }
}
