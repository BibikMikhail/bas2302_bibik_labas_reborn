package com.example.smarthome.repository;

import com.example.smarthome.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByRoom_Id(Long roomId);

    Optional<Device> findFirstByRoom_IdAndTypeIgnoreCase(Long roomId, String type);

    List<Device> findByRoom_IdAndTypeIgnoreCase(Long roomId, String type);
}
