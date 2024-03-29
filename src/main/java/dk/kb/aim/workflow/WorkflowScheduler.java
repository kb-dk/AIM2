package dk.kb.aim.workflow;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The workflow scheduler for scheduling the workflows.
 * 
 * Basically the timer checks whether to run any of workflows once every second.
 * It is the workflows themselves, who checks their conditions and performs their tasks if the conditions are met.
 */
@Service
public class WorkflowScheduler {
    /** The interval for the timer for how often to check the workflow.*/
    protected static final long TIMER_INTERVAL = 1000L;
    
    /** The workflows running in this scheduler.*/
    @Autowired
    AimWorkflow workflow;

    /** The timer for running the TimerTasks.*/
    ScheduledExecutorService executorService;
    
    /**
     * Method for shutting down this service.
     */
    @PreDestroy
    public void shutDown() {
        workflow.cancel();
        executorService.shutdownNow();
    }
    
    /**
     * Scedules the workflows.
     */
    @PostConstruct
    public void scheduleWorkflows() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        
        executorService.scheduleAtFixedRate(workflow, TIMER_INTERVAL, TIMER_INTERVAL, TimeUnit.MILLISECONDS);
    }
}
