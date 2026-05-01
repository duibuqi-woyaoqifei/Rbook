package com.rbook.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.rbook.data.local.entity.DailyStatsEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StatsDao_Impl implements StatsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DailyStatsEntity> __insertionAdapterOfDailyStatsEntity;

  public StatsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDailyStatsEntity = new EntityInsertionAdapter<DailyStatsEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `daily_stats` (`date`,`readingDurationMinutes`,`booksOpenedCount`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DailyStatsEntity entity) {
        statement.bindString(1, entity.getDate());
        statement.bindLong(2, entity.getReadingDurationMinutes());
        statement.bindLong(3, entity.getBooksOpenedCount());
      }
    };
  }

  @Override
  public Object insertOrUpdate(final DailyStatsEntity stats,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfDailyStatsEntity.insert(stats);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<DailyStatsEntity>> getAllStats() {
    final String _sql = "SELECT * FROM daily_stats ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"daily_stats"}, new Callable<List<DailyStatsEntity>>() {
      @Override
      @NonNull
      public List<DailyStatsEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfReadingDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "readingDurationMinutes");
          final int _cursorIndexOfBooksOpenedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "booksOpenedCount");
          final List<DailyStatsEntity> _result = new ArrayList<DailyStatsEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DailyStatsEntity _item;
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final long _tmpReadingDurationMinutes;
            _tmpReadingDurationMinutes = _cursor.getLong(_cursorIndexOfReadingDurationMinutes);
            final int _tmpBooksOpenedCount;
            _tmpBooksOpenedCount = _cursor.getInt(_cursorIndexOfBooksOpenedCount);
            _item = new DailyStatsEntity(_tmpDate,_tmpReadingDurationMinutes,_tmpBooksOpenedCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getStatsByDate(final String date,
      final Continuation<? super DailyStatsEntity> $completion) {
    final String _sql = "SELECT * FROM daily_stats WHERE date = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DailyStatsEntity>() {
      @Override
      @Nullable
      public DailyStatsEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfReadingDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "readingDurationMinutes");
          final int _cursorIndexOfBooksOpenedCount = CursorUtil.getColumnIndexOrThrow(_cursor, "booksOpenedCount");
          final DailyStatsEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpDate;
            _tmpDate = _cursor.getString(_cursorIndexOfDate);
            final long _tmpReadingDurationMinutes;
            _tmpReadingDurationMinutes = _cursor.getLong(_cursorIndexOfReadingDurationMinutes);
            final int _tmpBooksOpenedCount;
            _tmpBooksOpenedCount = _cursor.getInt(_cursorIndexOfBooksOpenedCount);
            _result = new DailyStatsEntity(_tmpDate,_tmpReadingDurationMinutes,_tmpBooksOpenedCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
