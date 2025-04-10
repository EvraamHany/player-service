package playerService.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import playerService.service.SessionService;

@Component
public class SessionTimeoutScheduler {

    private final SessionService sessionService;

    @Autowired
    public SessionTimeoutScheduler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Scheduled(fixedRate = 60000)
    public void checkTimeLimits() {
        sessionService.checkAndLogoutTimeLimitExceededPlayers();
    }
}

