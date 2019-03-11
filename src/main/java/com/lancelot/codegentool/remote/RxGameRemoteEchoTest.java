package com.lancelot.codegentool.remote;

import com.jcraft.jsch.Session;
import com.lizongbo.codegentool.LinuxRemoteCommandUtil;

public class RxGameRemoteEchoTest {

    public static void main(String[] args) {
        // auto-generation finger print
        String linuxUserName = "jenkins";
        String linuxUserPwd = "S3luPXh4JzxsuABa";
        Session versionServerSSHSession = LinuxRemoteCommandUtil.getSSHSession("172.81.235.67", 22, linuxUserName, linuxUserPwd);
        LinuxRemoteCommandUtil.runCmd(versionServerSSHSession, "echo helloworld");
        versionServerSSHSession.disconnect();
    }

}
