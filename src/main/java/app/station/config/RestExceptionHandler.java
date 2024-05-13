package app.station.config;

import app.station.DTO.ResponseError;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class RestExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseError handleResponseStatusException(ResponseStatusException ex) {
    HttpStatusCode httpStatusCode = ex.getStatusCode();
    return ResponseError
      .builder()
      .status(httpStatusCode.value())
      .error(HttpStatus.valueOf(httpStatusCode.value()).getReasonPhrase())
      .message(ex.getMessage())
      .build();
  }
}
