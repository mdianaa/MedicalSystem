package org.nbu.medicalrecord.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Month;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MonthWithSickLeavesDtoResponse {

    private Month month;

    private Long count;
}
