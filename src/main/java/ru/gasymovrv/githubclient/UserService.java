package ru.gasymovrv.githubclient;

import java.util.concurrent.CompletableFuture;
import ru.gasymovrv.githubclient.dto.User;

public interface UserService {

  CompletableFuture<User> getUserByLogin(String login, String basicAuth);

  CompletableFuture<User> getUserInFuture();
}
