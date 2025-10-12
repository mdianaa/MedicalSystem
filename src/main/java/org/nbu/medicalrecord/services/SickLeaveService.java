package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.SickLeaveDtoRequest;
import org.nbu.medicalrecord.dtos.response.SickLeaveDtoResponse;

import java.time.Month;
import java.util.Set;

public interface SickLeaveService {

    SickLeaveDtoResponse createSickLeave(SickLeaveDtoRequest request);

    Set<SickLeaveDtoResponse> showAllSickLeavesByDoctor(long doctorId);

    Set<SickLeaveDtoResponse> showAllSickLeavesForPatient(long patientId);

    Month showMonthWithMostSickLeaves();
}
