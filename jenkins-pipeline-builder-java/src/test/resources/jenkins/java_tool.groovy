import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;
import hudson.tools.*

dis = new hudson.model.JDK.DescriptorImpl();
dis.setInstallations( new hudson.model.JDK("Java", "/opt/java/openjdk"));