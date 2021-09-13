package projekti;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    @Bean
    public DelegatingSecurityContextAsyncTaskExecutor customTaskExecutor() {

        ThreadPoolTaskExecutor delegate = new ThreadPoolTaskExecutor();
        
        delegate.initialize();

        return new DelegatingSecurityContextAsyncTaskExecutor(delegate);
    }

    @Override
    public Executor getAsyncExecutor() {

        return customTaskExecutor();
    }

}
