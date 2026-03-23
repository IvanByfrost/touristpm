package com.travel.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 100)
  private String fullName;

  @NotBlank
  @Size(min = 5, max = 20)
  @Pattern(regexp = "\\d+", message = "El documento debe contener solo números")
  private String document;

  @NotBlank
  @Size(max = 100)
  @Email
  private String email;

  private Set<String> role;

  @NotBlank
  @Size(min = 6, max = 40)
  private String password;
}
