package app.station.DTO;

import lombok.Builder;

@Builder
public class ResponseError {
  private int status;
  private String error;
  private String message;
}
