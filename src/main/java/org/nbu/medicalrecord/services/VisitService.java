package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.VisitDtoRequest;
import org.nbu.medicalrecord.dtos.response.VisitDtoResponse;

import java.time.Month;
import java.util.Set;

public interface VisitService {

    VisitDtoResponse createNewVisit(VisitDtoRequest visitDtoRequest);

    Set<VisitDtoResponse> showAllVisitsByDoctor(long doctorId);

    //TODO: how to represent the time period that should be inserted?
    // Set<VisitDtoResponse> showAllVisitsForPeriod();
    // Set<VisitDtoResponse> showAllVisitsByDoctorForPeriod();

    Set<VisitDtoResponse> showAllVisitsForPatient(long patientId);
}
