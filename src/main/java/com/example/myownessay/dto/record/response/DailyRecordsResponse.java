package com.example.myownessay.dto.record.response;

import com.example.myownessay.dto.record.RecordDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyRecordsResponse {

    private LocalDate date;
    private List<RecordDto> records;
    private Double completionRate;

}
