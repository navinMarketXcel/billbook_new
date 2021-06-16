package com.billbook.app.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.billbook.app.database.models.User;

import java.util.List;

@Dao
public interface UserDao {
    /*@Query("SELECT * FROM user WHERE id=")
    List<User> getUser();*/

    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE id IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE id =:userId")
    User getUser(int userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserUser(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM user")
    void deleteAll();
}
