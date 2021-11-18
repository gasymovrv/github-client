package ru.gasymovrv.githubclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repo {

  private String id;

  @JsonProperty("full_name")
  private String fullName;

  @JsonProperty("contributors_url")
  private String contributorsUrl;

  private List<Contributor> contributors = new ArrayList<>();
}
