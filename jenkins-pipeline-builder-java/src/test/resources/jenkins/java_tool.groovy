import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;
import hudson.tools.*

dis = new hudson.model.JDK.DescriptorImpl();
dis.setInstallations( new hudson.model.JDK("Java", "/opt/zulu17.28.13-ca-jdk17.0.0-linux_x64"));