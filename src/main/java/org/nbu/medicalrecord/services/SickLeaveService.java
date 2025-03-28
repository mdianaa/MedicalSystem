package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.response.SickLeaveDtoResponse;

import java.time.Month;
import java.util.Set;

public interface SickLeaveService {

    Set<SickLeaveDtoResponse> showAllSickLeavesByDoctor(long doctorId);

    Set<SickLeaveDtoResponse> showAllSickLeavesForPatient(long patientId);

    Month showMonthWithMostSickLeaves();
}
