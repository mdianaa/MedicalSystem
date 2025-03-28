package org.nbu.medicalrecord.services;

import org.nbu.medicalrecord.dtos.request.AppointmentDtoRequest;
import org.nbu.medicalrecord.dtos.response.DoctorAppointmentDtoResponse;
import org.nbu.medicalrecord.dtos.response.PatientAppointmentDtoResponse;

import java.time.LocalDate;
import java.util.Set;

public interface AppointmentService {

    PatientAppointmentDtoResponse makeAppointment(AppointmentDtoRequest appointmentDtoRequest);

    // DoctorAppointmentDtoResponse changeAppointmentsDate(); // doctor's view

    Set<PatientAppointmentDtoResponse> showAllAvailableAppointmentsByDoctor(int doctorEgn); // patient's view

    Set<PatientAppointmentDtoResponse> showAllPatientAppointments(int patientEgn);  //  patient's view

    PatientAppointmentDtoResponse showPatientAppointmentOnDate(int patientEgn, LocalDate date);  //  patient's view

    Set<DoctorAppointmentDtoResponse> showAllOccupiedAppointments(int doctorEgn);  // doctor's view

    void cancelAppointment(long appointmentId);
}
