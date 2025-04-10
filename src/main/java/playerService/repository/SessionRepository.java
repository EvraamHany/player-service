package playerService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import playerService.model.Player;
import playerService.model.Session;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    Optional<Session> findByIdAndLoggedOutAtIsNull(String id);

    List<Session> findByPlayerAndLoggedOutAtIsNull(Player player);

    List<Session> findByLoggedOutAtIsNullAndExpiresAtBefore(LocalDateTime now);
}