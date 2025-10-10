package org.nbu.medicalrecord.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nbu.medicalrecord.dtos.request.DoctorsScheduleDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorsScheduleDtoResponse;
import org.nbu.medicalrecord.entities.Doctor;
import org.nbu.medicalrecord.entities.DoctorsSchedule;
import org.nbu.medicalrecord.exceptions.ForbiddenException;
import org.nbu.medicalrecord.exceptions.NotFoundException;
import org.nbu.medicalrecord.repositories.DoctorRepository;
import org.nbu.medicalrecord.repositories.DoctorsScheduleRepository;
import org.nbu.medicalrecord.services.DoctorsScheduleService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorsScheduleServiceImpl implements DoctorsScheduleService {

    private final DoctorsScheduleRepository scheduleRepo;
    private final DoctorRepository doctorRepo;

    @Override
    @Transactional
    public DoctorsScheduleDtoResponse createNewSchedule(DoctorsScheduleDtoRequest req) {
        Doctor doctor = doctorRepo.findById(req.getDoctorId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        // one schedule per doctor
        DoctorsSchedule schedule = scheduleRepo.findByDoctor_Id(doctor.getId())
                .orElseGet(DoctorsSchedule::new);

        schedule.setDoctor(doctor);
        schedule.setShift(req.getShift());

        DoctorsSchedule saved = scheduleRepo.save(schedule);

        int count = saved.getAppointments() == null ? 0 : saved.getAppointments().size();
        return new DoctorsScheduleDtoResponse(saved.getId(), doctor.getId(), saved.getShift(), count);
    }

    @Override
    @Transactional
    public void deleteSchedule(long doctorId, long scheduleId) {
        var schedule = scheduleRepo.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        if (schedule.getDoctor().getId() != (doctorId)) {
            throw new IllegalArgumentException("Schedule does not belong to this doctor");
        }

        scheduleRepo.delete(schedule);
    }
}
