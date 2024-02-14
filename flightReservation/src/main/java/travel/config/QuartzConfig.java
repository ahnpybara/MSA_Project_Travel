package travel.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class QuartzConfig implements ApplicationContextAware{

    private AutowireCapableBeanFactory beanFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }
    
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean() {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        schedulerFactoryBean.setJobFactory(jobFactory); // JobFactory 설정
        return schedulerFactoryBean;
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException{
        return schedulerFactoryBean().getScheduler();   
    }

}
