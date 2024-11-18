package br.com.fiap.energyapi.domain.deviceAnalysis;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceAnalysisRepository extends JpaRepository<DeviceAnalysis, Long> {
}
