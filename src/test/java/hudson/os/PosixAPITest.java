package hudson.os;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

import hudson.ChannelRule;
import hudson.FilePath;
import hudson.Functions;
import hudson.Platform;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PosixAPITest {

    @Rule public ChannelRule channels = new ChannelRule();

    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void copyToWithPermissionSpecialPermissions() throws IOException, InterruptedException {
        assumeFalse(
                "Test uses POSIX-specific features", Functions.isWindows() || Platform.isDarwin());
        File tmp = temp.getRoot();
        File original = new File(tmp, "original");
        FilePath originalP = new FilePath(channels.french, original.getPath());
        originalP.touch(0);
        // Read/write/execute for everyone and setuid.
        PosixAPI.jnr().chmod(original.getAbsolutePath(), 02777);

        File sameChannelCopy = new File(tmp, "sameChannelCopy");
        FilePath sameChannelCopyP = new FilePath(channels.french, sameChannelCopy.getPath());
        originalP.copyToWithPermission(sameChannelCopyP);
        assertEquals(
                "Special permissions should be copied on the same machine",
                02777,
                PosixAPI.jnr().stat(sameChannelCopy.getAbsolutePath()).mode() & 07777);

        File diffChannelCopy = new File(tmp, "diffChannelCopy");
        FilePath diffChannelCopyP = new FilePath(channels.british, diffChannelCopy.getPath());
        originalP.copyToWithPermission(diffChannelCopyP);
        assertEquals(
                "Special permissions should not be copied across machines",
                00777,
                PosixAPI.jnr().stat(diffChannelCopy.getAbsolutePath()).mode() & 07777);
    }
}
