package com.rbook.ui.screens.reader;

import com.rbook.domain.repository.BookRepository;
import com.rbook.domain.repository.StatsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class ReaderViewModel_Factory implements Factory<ReaderViewModel> {
  private final Provider<BookRepository> repositoryProvider;

  private final Provider<StatsRepository> statsRepositoryProvider;

  public ReaderViewModel_Factory(Provider<BookRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider) {
    this.repositoryProvider = repositoryProvider;
    this.statsRepositoryProvider = statsRepositoryProvider;
  }

  @Override
  public ReaderViewModel get() {
    return newInstance(repositoryProvider.get(), statsRepositoryProvider.get());
  }

  public static ReaderViewModel_Factory create(Provider<BookRepository> repositoryProvider,
      Provider<StatsRepository> statsRepositoryProvider) {
    return new ReaderViewModel_Factory(repositoryProvider, statsRepositoryProvider);
  }

  public static ReaderViewModel newInstance(BookRepository repository,
      StatsRepository statsRepository) {
    return new ReaderViewModel(repository, statsRepository);
  }
}
