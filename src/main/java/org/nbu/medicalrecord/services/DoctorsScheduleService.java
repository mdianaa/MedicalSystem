package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.DoctorsScheduleDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorsScheduleDtoResponse;

public interface DoctorsScheduleService {

    DoctorsScheduleDtoResponse createNewSchedule(DoctorsScheduleDtoRequest doctorsScheduleDtoRequest);

    void deleteSchedule(long scheduleId);
}
