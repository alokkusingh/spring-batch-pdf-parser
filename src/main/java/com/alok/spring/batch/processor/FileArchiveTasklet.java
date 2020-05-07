package com.alok.spring.batch.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.File;

@Slf4j
public class FileArchiveTasklet implements Tasklet, InitializingBean {

    private Resource[] resources;

    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        for(Resource r: resources) {
            File file = r.getFile();
            log.debug("FIle {} will be archived after processing", file);
           // boolean deleted = file.delete();
            //if (!deleted) {
             //   throw new UnexpectedJobExecutionException("Could not delete file " + file.getPath());
            //}
        }
        return RepeatStatus.FINISHED;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(resources, "directory must be set");
    }
}
