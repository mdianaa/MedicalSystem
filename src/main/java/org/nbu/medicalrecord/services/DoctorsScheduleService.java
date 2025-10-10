package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.DoctorsScheduleDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorsScheduleDtoResponse;
import org.nbu.medicalrecord.exceptions.ForbiddenException;
import org.nbu.medicalrecord.exceptions.NotFoundException;

public interface DoctorsScheduleService {

    DoctorsScheduleDtoResponse createNewSchedule(DoctorsScheduleDtoRequest doctorsScheduleDtoRequest);

    void deleteSchedule(long doctorId, long scheduleId);
}
