package ru.gasymovrv.githubclient.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  private String name;
  private String email;
  private List<Repo> repos = new ArrayList<>();
}
