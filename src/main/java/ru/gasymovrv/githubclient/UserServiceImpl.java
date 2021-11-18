package ru.gasymovrv.githubclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gasymovrv.githubclient.dto.Contributor;
import ru.gasymovrv.githubclient.dto.Repo;
import ru.gasymovrv.githubclient.dto.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private static final String GET_USERS_URL = "https://api.github.com/users";
  private static final String GET_USER_URL = GET_USERS_URL + "/{0}";
  private static final String GET_USER_REPOS_URL = GET_USER_URL + "/repos?per_page=5";
  private static final String AUTH_HEADER_NAME = "Authorization";

  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final ObjectMapper objectMapper;

  @Override
  public CompletableFuture<User> getUserByLogin(String login, String basicAuth) {
    //Find repos with contributors by the user login
    var reposFuture = httpClient.sendAsync(
            HttpRequest
                .newBuilder()
                .uri(createGetUserReposUrl(login))
                .header(AUTH_HEADER_NAME, basicAuth)
                .build(),
            BodyHandlers.ofString())
        .thenApply(resp -> parseUserRepos(resp.body()))
        //start getting contributors for every repo
        .thenCompose(repos -> {
          CompletableFuture<Repo>[] futures = repos
              .stream()
              .map(repo -> httpClient.sendAsync(HttpRequest
                          .newBuilder()
                          .uri(URI.create(repo.getContributorsUrl()))
                          .header(AUTH_HEADER_NAME, basicAuth)
                          .build(),
                      BodyHandlers.ofString())
                  .thenApply(resp -> {
                    var body = resp.body();
                    if (!body.isBlank()) {
                      repo.getContributors().addAll(parseContributors(body));
                    }
                    return repo;
                  }))
              .toList()
              .toArray(CompletableFuture[]::new);
          //convert the list of futures with repos to the future with list of repos
          return flip(futures);
        });

    //Find the user by the login
    return httpClient.sendAsync(HttpRequest
                .newBuilder()
                .uri(createGetUserUrl(login))
                .header(AUTH_HEADER_NAME, basicAuth)
                .build(),
            BodyHandlers.ofString())
        .thenApply(resp -> parseUser(resp.body()))
        //fill found repos to the user
        .thenCombine(reposFuture, (user, repos) -> {
          user.getRepos().addAll(repos);
          return user;
        });
  }

  @Override
  public CompletableFuture<User> getUserInFuture() {
    final User user = new User();
    user.setName("Petr");
    return CompletableFuture.completedFuture(user);
  }

  @SneakyThrows
  private User parseUser(String userAsString) {
    return objectMapper.readValue(userAsString, User.class);
  }

  @SneakyThrows
  private List<Repo> parseUserRepos(String reposAsString) {
    return objectMapper.readValue(reposAsString, new TypeReference<>() {
    });
  }

  @SneakyThrows
  private List<Contributor> parseContributors(String contributorsAsString) {
    return objectMapper.readValue(contributorsAsString, new TypeReference<>() {
    });
  }

  private URI createGetUserUrl(String login) {
    return URI.create(MessageFormat.format(GET_USER_URL, login));
  }

  private URI createGetUserReposUrl(String login) {
    return URI.create(MessageFormat.format(GET_USER_REPOS_URL, login));
  }

  private <T> CompletableFuture<List<T>> flip(CompletableFuture<T>[] listOfCf) {
    return CompletableFuture.allOf(listOfCf)
        .thenApply(allCompleted -> Arrays.stream(listOfCf).map(CompletableFuture::join).toList());
  }
}