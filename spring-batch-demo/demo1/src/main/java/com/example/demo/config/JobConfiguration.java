package com.example.demo.config;

import com.example.demo.model.Employee;
import com.example.demo.model.EmployeeWithSalary;
import com.example.demo.utils.AppResourceUtils;
import com.example.demo.utils.BatchProfile;
import com.example.demo.utils.TransactionPartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.CompositeJobExecutionListener;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.task.batch.partition.CommandLineArgsProvider;
import org.springframework.cloud.task.batch.partition.DeployerPartitionHandler;
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler;
import org.springframework.cloud.task.batch.partition.SimpleEnvironmentVariablesProvider;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.cloud.task.repository.TaskRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchIntegration
@EnableBatchProcessing
@EnableTask
public class JobConfiguration {
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;



    // ================================================================
    // Worker spring-beans definitions
    // ================================================================
    @Bean
    @Profile(BatchProfile.WORKER)
    public DeployerStepExecutionHandler stepExecutionHandler(ApplicationContext applicationContext, JobRepository jobRepository, JobExplorer jobExplorer) {
        return new DeployerStepExecutionHandler(applicationContext, jobExplorer, jobRepository);
    }



    @Bean
    public Step worker(ItemReader<Employee> employeeReader,
                       ItemProcessor<Employee, EmployeeWithSalary> employeeWithSalaryItemProcessor,
                       ItemWriter<EmployeeWithSalary> employeeWithSalaryItemWriter){
        Step step = stepBuilderFactory.get("worker")
                .<Employee, EmployeeWithSalary>chunk(1000)
                .reader(employeeReader)
                .processor(employeeWithSalaryItemProcessor)
                .writer(employeeWithSalaryItemWriter)
                .build();
        return step;
    }


    @Bean
    @Profile(BatchProfile.MASTER)
    public PartitionHandler partitionHandler(TaskLauncher taskLauncher,
                                             JobExplorer jobExplorer,
                                             Step worker, //
                                             TaskRepository taskRepository, //
                                             Environment environment) {

        final String appName = environment.getProperty("spring.application.name");
        final Resource resource = AppResourceUtils.getResource(environment, null);

        final DeployerPartitionHandler partitionHandler = new DeployerPartitionHandler(taskLauncher, jobExplorer, resource, worker.getName(), taskRepository);
        partitionHandler.setApplicationName(appName);

        // set command line arguments
        final List<String> commandLineArgs = new ArrayList<>(1);
        commandLineArgs.add("--spring.profiles.active=kube,worker");
        final CommandLineArgsProvider commandLineArgsProvider = executionContext -> commandLineArgs;
        partitionHandler.setCommandLineArgsProvider(commandLineArgsProvider);

        // set environment variables
        final SimpleEnvironmentVariablesProvider simpleEnvironmentVariablesProvider = new SimpleEnvironmentVariablesProvider(environment);
        simpleEnvironmentVariablesProvider.setIncludeCurrentEnvironment(false);
        partitionHandler.setEnvironmentVariablesProvider(simpleEnvironmentVariablesProvider);
        return partitionHandler;
    }


    @Bean
    @Profile(BatchProfile.MASTER)
    public Partitioner partitioner() {
        return new TransactionPartitioner();
    }

    @Bean
    @Profile(BatchProfile.MASTER)
    public Step partitionedMaster(StepBuilderFactory stepBuilderFactory,
                                  PartitionHandler partitionHandler,
                                  Partitioner partitioner,
                                  Step worker) {
        return stepBuilderFactory.get("step.master")
                .partitioner(worker.getName(), partitioner)
                .step(worker)
                .partitionHandler(partitionHandler)
                .build();
    }

    @Bean
    @Profile(BatchProfile.MASTER)
    public Job jobMaster(JobBuilderFactory jobBuilderFactory,
                         Step partitionedMaster) {

        final CompositeJobExecutionListener compositeJobExecutionListener = new CompositeJobExecutionListener();
        List<JobExecutionListener> listeners = new ArrayList<>();
        compositeJobExecutionListener.setListeners(listeners);

        return jobBuilderFactory.get("employeeProcessor")
                .listener(compositeJobExecutionListener)
                .incrementer(new RunIdIncrementer())
                .start(partitionedMaster)
                .build();
    }

}
