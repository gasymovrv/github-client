package ru.gasymovrv.githubclient;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gasymovrv.githubclient.dto.User;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/{login}")
  public CompletableFuture<User> getUser(@PathVariable String login, @RequestHeader("Authorization") String basicAuth) {
    return userService.getUserByLogin(login, basicAuth);
  }

  @GetMapping("/in-future")
  public CompletableFuture<User> getUser() {
    return userService.getUserInFuture();
  }
}
