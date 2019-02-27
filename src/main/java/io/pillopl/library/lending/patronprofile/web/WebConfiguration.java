package io.pillopl.library.lending.patronprofile.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL_FORMS;

@Configuration
@EnableAutoConfiguration
@EnableHypermediaSupport(type = HAL_FORMS)
@ComponentScan
public class WebConfiguration {

    @Bean
    static HalObjectMapperConfigurer halObjectMapperConfigurer() {
        return new HalObjectMapperConfigurer();
    }

    private static class HalObjectMapperConfigurer
            implements BeanPostProcessor, BeanFactoryAware {

        private BeanFactory beanFactory;

        /**
         * Assume any {@link ObjectMapper} starts with {@literal _hal} and ends with {@literal Mapper}.
         */
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName)
                throws BeansException {
            if (bean instanceof ObjectMapper && beanName.startsWith("_hal") && beanName.endsWith("Mapper")) {
                postProcessHalObjectMapper((ObjectMapper) bean);
            }
            return bean;
        }

        private void postProcessHalObjectMapper(ObjectMapper objectMapper) {
            try {
                Jackson2ObjectMapperBuilder builder = this.beanFactory.getBean(Jackson2ObjectMapperBuilder.class);
                builder.configure(objectMapper);
            } catch (NoSuchBeanDefinitionException ex) {
                // No Jackson configuration required
            }
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName)
                throws BeansException {
            return bean;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }
    }
}