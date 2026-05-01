package com.rbook.data.parser;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class BookParserImpl_Factory implements Factory<BookParserImpl> {
  private final Provider<Context> contextProvider;

  public BookParserImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BookParserImpl get() {
    return newInstance(contextProvider.get());
  }

  public static BookParserImpl_Factory create(Provider<Context> contextProvider) {
    return new BookParserImpl_Factory(contextProvider);
  }

  public static BookParserImpl newInstance(Context context) {
    return new BookParserImpl(context);
  }
}
