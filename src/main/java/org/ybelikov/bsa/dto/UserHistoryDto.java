package org.ybelikov.bsa.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserHistoryDto {
  private String date;
  private String query;
  private String path;
}
