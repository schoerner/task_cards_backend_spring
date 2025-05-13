package de.acosci.tasks.factory;

import de.acosci.tasks.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** https://www.baeldung.com/spring-autowire
 * https://medium.com/devdomain/spring-boots-autowired-vs-constructor-injection-a-detailed-guide-1b19970d828e
 * https://stackoverflow.com/questions/37565186/spring-couldnt-autowired-there-is-more-than-one-bean-of-type
 * Qualifier annotation https://www.baeldung.com/spring-qualifier-annotation
 */
@Configuration
public class ServiceFactory {
    @Primary
    @Bean(name= "MariaDBTaskServiceBean")
    public TaskService createMariaDBTaskService() {
        return new MariaDBTaskService();
    }

    @Bean(name= "RAMTaskServiceBean")
    public TaskService createRAMTaskService() {
        return new RAMTaskService();
    }

    @Primary
    @Bean(name= "MariaDBUserServiceBean")
    public UserService createMariaDBUserService() {
        return new MariaDBUserService();
    }
}
