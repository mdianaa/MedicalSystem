package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.AppointmentDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorAppointmentDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientAppointmentDtoResponse;

import java.time.LocalDate;
import java.util.Set;

public interface AppointmentService {

    PatientAppointmentDtoResponse makeAppointment(AppointmentDtoRequest appointmentDtoRequest);

    // patient’s view
    Set<PatientAppointmentDtoResponse> showAllAvailableAppointmentsByDoctorId(Long doctorId);

    Set<PatientAppointmentDtoResponse> showAllPatientAppointmentsById(Long patientId);

    Set<PatientAppointmentDtoResponse> showPatientAppointmentOnDateById(Long patientId, LocalDate date);

    // doctor’s view
    Set<DoctorAppointmentDtoResponse> showAllOccupiedAppointmentsById(Long doctorId);

    void cancelAppointment(long appointmentId);
}
