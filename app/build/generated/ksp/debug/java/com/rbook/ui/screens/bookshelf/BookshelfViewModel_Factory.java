package com.rbook.ui.screens.bookshelf;

import com.rbook.domain.parser.BookParser;
import com.rbook.domain.repository.BookRepository;
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
public final class BookshelfViewModel_Factory implements Factory<BookshelfViewModel> {
  private final Provider<BookRepository> repositoryProvider;

  private final Provider<BookParser> bookParserProvider;

  public BookshelfViewModel_Factory(Provider<BookRepository> repositoryProvider,
      Provider<BookParser> bookParserProvider) {
    this.repositoryProvider = repositoryProvider;
    this.bookParserProvider = bookParserProvider;
  }

  @Override
  public BookshelfViewModel get() {
    return newInstance(repositoryProvider.get(), bookParserProvider.get());
  }

  public static BookshelfViewModel_Factory create(Provider<BookRepository> repositoryProvider,
      Provider<BookParser> bookParserProvider) {
    return new BookshelfViewModel_Factory(repositoryProvider, bookParserProvider);
  }

  public static BookshelfViewModel newInstance(BookRepository repository, BookParser bookParser) {
    return new BookshelfViewModel(repository, bookParser);
  }
}
