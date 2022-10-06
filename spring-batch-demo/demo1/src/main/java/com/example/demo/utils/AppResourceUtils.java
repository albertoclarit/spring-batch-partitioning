package com.example.demo.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.GitProperties;
import org.springframework.cloud.deployer.resource.docker.DockerResource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.net.URISyntaxException;

@Slf4j
public class AppResourceUtils {

    public static Resource getResource(Environment environment, GitProperties gitProperties){

        if (environment.containsProperty("VCAP_APPLICATION") || environment.containsProperty("VCAP_SERVICES")) {
            log.info("Running in CloudFoundry");
            return new ClassPathResource("application.yml");
        } else if (environment.containsProperty("INFO_KUBE_ENV") || environment.containsProperty("KUBERNETES_SERVICE_HOST")) {
            log.info("Running in Kubernetes");
            final String appName = environment.getProperty("spring.application.name");
            // assuming application-name == docker-image-name
            return new DockerResource("albertoclaritdocker/" +appName + ":latest");
        } else {
            String appVersionName = "0.0.1-SNAPSHOT";
            if (gitProperties != null) {
                appVersionName = gitProperties.get("build.version");
            } else {
                log.warn("git.properties not found. Add 'git-commit-id-plugin' to pom.xml");
            }
            final String appName = environment.getProperty("spring.application.name");
            File localResourceName = getPathToJar(String.format("%s-%s.jar", appName, appVersionName));
            log.info("Running {}",localResourceName);
            Resource resource = new FileSystemResource(localResourceName);
            if (resource.exists()) {
                log.info("Running locally, Using FileSystemResource {}", localResourceName);
            } else {
                throw new RuntimeException("Run 'mvn package', application jar not found - " + localResourceName);
            }
            return resource;
        }



    }

    private static File getPathToJar(String jarFileName) {
        try {
            File file = getLocationOfMainClass();
            if (file.isFile()) {
                return file;
            } else {
                return new File(file.getAbsolutePath() + File.separator + "../../.." + File.separator + "libs/" + jarFileName);
            }
        } catch (URISyntaxException | ClassNotFoundException e) {
            throw new RuntimeException("Unable to locate JAR file of Main class' - " + jarFileName, e);
        }
    }

    private static Class getMainClass() throws ClassNotFoundException {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        return Class.forName(stackTrace[stackTrace.length - 1].getClassName());
    }

    private static File getLocationOfMainClass() throws URISyntaxException, ClassNotFoundException {
        return new File(getMainClass().getProtectionDomain().getCodeSource().getLocation().toURI());
    }

}
