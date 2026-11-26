
package com.example.bookingapp.Models.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AiClassificationResult {

    private String intentCode;

    private double confidence;

    private String source;
}
