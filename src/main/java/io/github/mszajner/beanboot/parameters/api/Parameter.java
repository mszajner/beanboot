package io.github.mszajner.beanboot.parameters.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Parameter {
    ParameterName name;
    String value;
    Instant createdAt;
    Instant updatedAt;
}
