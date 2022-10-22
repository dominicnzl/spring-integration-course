package com.lil.springintegration.service;

import com.lil.springintegration.endpoint.TechSupportMessageHandler;
import com.lil.springintegration.manage.DashboardManager;
import com.lil.springintegration.util.AppSupportStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.channel.AbstractSubscribableChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;

import java.util.Timer;
import java.util.TimerTask;

public class ViewService {

    static Logger logger = LoggerFactory.getLogger(DashboardManager.class);
    private Timer timer = new Timer();

    // TODO - refactor to use Spring Dependency Injection
    private AbstractSubscribableChannel techSupportChannel;
    private PollableChannel updateNotificationChannel;

    public ViewService() {

        // Initialize our updateNotificationChannel
        updateNotificationChannel = (PollableChannel) DashboardManager.getDashboardContext()
                .getBean("updateNotificationQueueChannel");
        AbstractSubscribableChannel techSupportChannel = (DirectChannel) DashboardManager.getDashboardContext().getBean("techSupportChannel");
        techSupportChannel.subscribe(new ViewMessageHandler());
        this.start();
    }

    private void start() {
        // Represents long-running process thread
        timer.schedule(new TimerTask() {
            public void run() {
                checkForNotifications();
            }
        }, 3000, 3000);
    }

    private void checkForNotifications() {
        // Check queue for notifications that the software needs to be updated
        Message msg = updateNotificationChannel.receive(1000L);
        if (msg != null) {
            DashboardManager.setDashboardStatus("softwareBuild", msg.getPayload().toString());
        }
    }

    private static class ViewMessageHandler extends TechSupportMessageHandler {
        protected void receiveAndAcknowledge(AppSupportStatus status) {
            DashboardManager.setDashboardStatus("softwareBuild", status.getVersion());
        }
    }
}
