package io.github.mszajner.beanboot.licence.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SetKeyRequest {
    String key;
    String secret;
}
