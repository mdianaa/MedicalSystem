package org.nbu.medicalrecord.services;

public interface AppointmentService {
    void makeAppointment();
    void changeAppointmentsDate(); // doctor's view
    void showAllAvailableAppointmentsByDoctor(); // patient's view
    void showAllPatientAppointments();  //  patient's view
    void showPatientAppointmentOnDate();  //  patient's view
    void showAllOccupiedAppointments();  // doctor's view
    void cancelAppointment();
}
