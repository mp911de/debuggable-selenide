package biz.paluch.testing.guice;

import java.util.Map;
import java.util.Properties;

import biz.paluch.testing.ConfigurationUtil;
import com.google.common.collect.Maps;
import com.google.guiceberry.GuiceBerryModule;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class IntegrationTestModule extends AbstractModule {

    @Override
    protected void configure() {
        Properties properties = ConfigurationUtil.readConfiguration();

        install(new GuiceBerryModule());

        Map<String, String> bindMap = Maps.newHashMap(Maps.fromProperties(properties));
        bindMap.putAll(Maps.fromProperties(System.getProperties()));

        Names.bindProperties(binder(), bindMap);
    }
}
