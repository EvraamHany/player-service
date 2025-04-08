package playerService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import playerService.model.Session;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {
    List<Session> findByPlayerIdAndLogoutTimeIsNull(Long playerId);
}
