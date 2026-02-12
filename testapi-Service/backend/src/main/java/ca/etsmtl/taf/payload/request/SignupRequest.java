package ca.etsmtl.taf.payload.request;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class SignupRequest {
  @NotBlank
  @Size(min = 3, max = 50)
  @Schema(example = "equipe3")
  private String fullName;
  
  @NotBlank
  @Size(min = 3, max = 20)
  @Schema(example = "equipe3")
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  @Schema(example = "equipe3@etsmtl.ca")
  private String email;

  @Schema(example = "[\"user\"]")
  private Set<String> role;

  @NotBlank
  @Size(min = 6, max = 40)
  @Schema(example = "equipe3")
  private String password;

  public String getFullName() {
	return fullName;
}

  public void setFullName(String fullName) {
	this.fullName = fullName;
}

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<String> getRole() {
    return this.role;
  }

  public void setRole(Set<String> role) {
    this.role = role;
  }
}
