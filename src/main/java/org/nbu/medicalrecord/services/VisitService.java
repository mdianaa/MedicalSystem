package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.VisitDtoRequest;
import org.nbu.medicalrecord.dtos.response.VisitDtoResponse;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

public interface VisitService {

    VisitDtoResponse createNewVisit(VisitDtoRequest visitDtoRequest);

    Set<VisitDtoResponse> showAllVisitsByDoctor(long doctorId);

    Set<VisitDtoResponse> showAllVisitsForPatientInPeriod(long patientId, LocalDate from, LocalDate to);

    Set<VisitDtoResponse> showAllVisitsByDoctorInPeriod(long doctorId, LocalDate from, LocalDate to);

    Set<VisitDtoResponse> showAllVisitsForPatient(long patientId);
}
