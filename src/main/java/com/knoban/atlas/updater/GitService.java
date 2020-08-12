package com.knoban.atlas.updater;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;

/**
 * @author Alden Bansemer (kNoAPP)
 *
 * While it seemed like a good idea at first, using GitHub commits as a way of storing and pushing code to
 * production is not a great practice. We should instead look to use Jenkins or another CI solution. There is nothing
 * programmatically wrong with this class though. You can use it to pull from Git.
 */
@Deprecated
public class GitService {

    private String passphrase;
    private boolean publicRepo;
    private File saveLocation, privateKey;

    public GitService(File saveLocation) {
        this(saveLocation, null, null);
    }

    public GitService(File saveFolder, File privateKey, String passphrase) {
        this.saveLocation = saveFolder;
        this.privateKey = privateKey;
        this.passphrase = passphrase;
        this.publicRepo = privateKey == null;
    }

    public void clone(String remote, String branch) throws GitAPIException {
        CloneCommand clone = Git.cloneRepository();
        clone.setURI(remote);
        clone.setDirectory(saveLocation);
        if(!publicRepo) {
            SshSessionFactory factory = new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host host, Session session) {
                    if(passphrase != null)
                        session.setPassword(passphrase);
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(final FS fs) throws JSchException {
                    JSch defaultJSch = super.createDefaultJSch(fs);
                    if(privateKey.exists())
                        defaultJSch.addIdentity(privateKey.getAbsolutePath(), passphrase);
                    return defaultJSch;
                }
            };


            clone.setTransportConfigCallback(new TransportConfigCallback() {
                @Override
                public void configure(Transport transport) {
                    if(transport instanceof SshTransport) {
                        SshTransport ssh = (SshTransport) transport;
                        ssh.setSshSessionFactory(factory);
                    }
                }
            });
        }

        // If a specific branch is requested, use it.
        if(branch != null && !branch.equals("master")) {
            Git g = clone.call();
            g.checkout().setCreateBranch(true).setName(branch).
                    setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).setStartPoint("origin/" + branch).call();
        } else clone.call();
    }
}
