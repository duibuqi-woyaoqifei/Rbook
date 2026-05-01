package com.rbook.data.readium;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ReadiumManager_Factory implements Factory<ReadiumManager> {
  private final Provider<Context> contextProvider;

  public ReadiumManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ReadiumManager get() {
    return newInstance(contextProvider.get());
  }

  public static ReadiumManager_Factory create(Provider<Context> contextProvider) {
    return new ReadiumManager_Factory(contextProvider);
  }

  public static ReadiumManager newInstance(Context context) {
    return new ReadiumManager(context);
  }
}
