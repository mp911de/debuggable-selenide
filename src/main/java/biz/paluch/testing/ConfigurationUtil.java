package biz.paluch.testing;

import static com.google.common.base.Preconditions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.Closer;
import com.google.common.io.Resources;

public class ConfigurationUtil {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationUtil.class);

    public static final String ENVIRONMENT_KEY = "env.properties";

    // default configuration file
    private static final String INTEGRATIONTEST_CONFIG_RESOURCE = "env.properties";
    private static final String LOCAL_OVERRIDE_CONFIG_RESOURCE = "services-local.properties";

    private static Properties CACHED_PROPERTIES = null;

    public static Properties readConfiguration() {
        return readConfiguration(INTEGRATIONTEST_CONFIG_RESOURCE);
    }

    /**
     * @param configurationResource
     * @return
     */
    public static Properties readConfiguration(String configurationResource) {
        if (CACHED_PROPERTIES != null) {
            return CACHED_PROPERTIES;
        }

        String environmentConfigurationResource = getEnvironmentResourceName(configurationResource);

        Properties environmentConfiguration = readEnvironmentProperties(environmentConfigurationResource);

        Properties overrideConfiguration = readOverrideConfiguration();

        if (overrideConfiguration != null) {
            LOGGER.info("Found local config file " + LOCAL_OVERRIDE_CONFIG_RESOURCE);
            // override default configuration
            environmentConfiguration.putAll(overrideConfiguration);
        }

        Map<String, String> map = Maps.fromProperties(environmentConfiguration);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            environmentConfiguration.setProperty(entry.getKey(), System.getProperty(entry.getKey(), entry.getValue()));
        }

        // Double interpolate for cyclic resolution
        interpolate(environmentConfiguration);
        interpolate(environmentConfiguration);

        LOGGER.info("Loaded service properties: " + environmentConfiguration);

        CACHED_PROPERTIES = environmentConfiguration;
        return environmentConfiguration;
    }

    private static void interpolate(Properties properties) {
        Map<String, String> master = Maps.fromProperties(properties);

        Map<String, String> lookup = Maps.newHashMap(Maps.fromProperties(properties));
        lookup.putAll(Maps.fromProperties(System.getProperties()));

        for (String key : master.keySet()) {
            String value = StrSubstitutor.replace(properties.getProperty(key), lookup);
            properties.setProperty(key, value);
        }
    }

    /**
     * @param configurationResource
     * @return
     */
    private static String getEnvironmentResourceName(String configurationResource) {

        Properties integrationTestConfiguration = PropertiesFileLoader.readPropertiesFile(configurationResource);
        checkState(integrationTestConfiguration != null, "Cannot read " + configurationResource + " (current directory: "
                + System.getProperty("user.dir") + ")");

        String environmentConfigurationResource = integrationTestConfiguration.getProperty(ENVIRONMENT_KEY);
        checkState(environmentConfigurationResource != null, "Config-File " + configurationResource
                + " does not contain a valid " + ENVIRONMENT_KEY + " key");

        String systemPropertyEnvironment = System.getProperty(ENVIRONMENT_KEY);
        if (StringUtils.isNotEmpty(systemPropertyEnvironment)) {
            environmentConfigurationResource += "," + systemPropertyEnvironment;
        }

        return environmentConfigurationResource;
    }

    /**
     * @param environmentConfigurationResource
     * @return
     */
    private static Properties readEnvironmentProperties(String environmentConfigurationResource) {
        String propertyResources[] = environmentConfigurationResource.split(",");
        Properties environmentConfiguration = new Properties();
        for (String propertyResource : propertyResources) {
            Properties properties = PropertiesFileLoader.readPropertiesFile(propertyResource.trim());

            checkState(properties != null,
                    "Cannot read " + propertyResource + " (current directory: " + System.getProperty("user.dir") + ")");
            environmentConfiguration.putAll(properties);
        }
        return environmentConfiguration;
    }

    /**
     * @return
     */
    protected static Properties readOverrideConfiguration() {

        return PropertiesFileLoader.readPropertiesFile(LOCAL_OVERRIDE_CONFIG_RESOURCE);

    }

    private static class PropertiesFileLoader {

        public static Properties readPropertiesFile(String name) {

            Properties properties = new Properties();

            Closer closer = Closer.create();
            try {
                URL url = Resources.getResource(name);
                ByteSource byteSource = Resources.asByteSource(url);
                try {
                    InputStream inputStream = closer.register(byteSource.openBufferedStream());
                    properties.load(inputStream);
                    return properties;
                } finally {
                    closer.close();
                }
            } catch (IllegalArgumentException e) {
                return properties;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static File baseDir() {
        try {
            URI uri = Resources.getResource(INTEGRATIONTEST_CONFIG_RESOURCE).toURI();
            File file = new File(uri).getParentFile().getParentFile().getParentFile();
            return file.getParentFile();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

}
