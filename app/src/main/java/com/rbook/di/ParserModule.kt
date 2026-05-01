package com.rbook.di

import com.rbook.data.parser.BookParserImpl
import com.rbook.domain.parser.BookParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {

    @Binds
    @Singleton
    abstract fun bindBookParser(impl: BookParserImpl): BookParser
}
