package com.example.smarthome.repository;

import com.example.smarthome.model.Event;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByDevice_IdOrderByCreatedAtDesc(Long deviceId, Pageable pageable);

    List<Event> findByDevice_Id(Long deviceId);

    /** События всех устройств в комнате (для бизнес-операции «события по комнате»). */
    List<Event> findByDevice_Room_IdOrderByCreatedAtDesc(Long roomId);

    @Query("select e from Event e " +
            "join fetch e.device d " +
            "join fetch d.room r " +
            "order by e.createdAt desc")
    List<Event> findAllForLog();
}
