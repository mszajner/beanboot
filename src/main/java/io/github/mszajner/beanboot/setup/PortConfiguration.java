package io.github.mszajner.beanboot.setup;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Component
public class PortConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory>,
        ApplicationListener<WebServerInitializedEvent> {

    private static final int START_PORT = 23970;

    @Value("${app.port-file:}")
    String portFilePath;

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        factory.setPort(findFreePortFrom(START_PORT));
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (StringUtils.isNotEmpty(portFilePath)) {
            Path portFile = Path.of(portFilePath);
            int port = event.getWebServer().getPort();
            try {
                if (Objects.nonNull(portFile.getParent())) {
                    Files.createDirectories(portFile.getParent());
                }
                Files.writeString(portFile, String.valueOf(port));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to write port file: " + portFile, e);
            }
        }
    }

    private int findFreePortFrom(int startPort) {
        for (int port = startPort; port <= 65535; port++) {
            try (ServerSocket socket = new ServerSocket(port)) {
                return port;
            } catch (IOException ignored) {
            }
        }
        throw new IllegalStateException("No free TCP port found starting from " + startPort);
    }
}
