package com.knoban.atlas.updater;

import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;

/**
 * Cannot use this class with Pterodactyl (without modifying the Docker image)
 *
 * @author Alden Bansemer (kNoAPP)
 *
 * While it seemed like a good idea at first, using GitHub commits as a way of storing and pushing code to
 * production is not a great practice. We should instead look to use Jenkins or another CI solution. There is nothing
 * programmatically wrong with this class though. You can use it to pull from Git.
 */
@Deprecated
public class MavenInvoker {

    private File pom;

    public MavenInvoker(File pom) {
        this.pom = pom;
    }

    public int build(String... goals) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(pom);
        request.setGoals(Arrays.asList(goals));

        Invoker invoker = new DefaultInvoker();
        try {
            InvocationResult result = invoker.execute(request);
            return result.getExitCode();
        } catch (MavenInvocationException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
